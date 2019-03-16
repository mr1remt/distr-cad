package replicaManager;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;
import org.jgroups.util.Util;

import DCAD.GObject;
import se.his.drts.message.BullyAnswerMessage;
import se.his.drts.message.BullyCoordinatorMessage;
import se.his.drts.message.BullyElectionMessage;
import se.his.drts.message.ClientResponseMessage;
import se.his.drts.message.DeleteObjectRequest;
import se.his.drts.message.DrawObjectRequest;
import se.his.drts.message.FrontendAnnouncement;
import se.his.drts.message.MessagePayload;
import se.his.drts.message.RetrieveObjectsRequest;
import se.his.drts.message.TransferStateMessage;
import se.his.drts.message.UniqueMessage;

public class ReplicaManager extends ReceiverAdapter {
	
	private JChannel		channel;
	private View			currentView;
	private Address			primary, frontend;
	
	// The CAD documents state, everything the replica manager needs to keep track of
	private CADState		state;
	private boolean			stateSynced = false;
	
	private Timer			electionTimer;
	private long			electionWaitTime = 1000;

	public ReplicaManager() {
		state = new CADState();
	}
	
	public void connect() {
		try {
			channel = new JChannel();
			channel.setReceiver(this);
			channel.connect("CAD-Service");
		}catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		try {
			ReplicaManager rm = new ReplicaManager();
			rm.connect();
		}catch (Exception e) {
			e.printStackTrace();
		}
		
		while (true) {
			try {
				Thread.sleep(1000000000);
			}catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void receive(Message m) {
		// Filter away our own messages
		if (m.getSrc().equals(channel.getAddress()))
			return;
		
		System.out.println(m);
		
		// TODO: Message handling
		
		// Receive message and parse it
		Optional<MessagePayload> optMsg = MessagePayload.createMessage(m.getBuffer());
		if (!optMsg.isPresent()) {
			System.out.println("Failed to deserialize message");
			return;
		}
		MessagePayload msg = optMsg.get();
		
		if (msg instanceof FrontendAnnouncement) {
			frontend = m.getSrc();
			System.out.println("Node " + frontend.toString() + " declared itself the frontend");
		}
		
		// Bully algorithm
		
		// An election has begun
		if (msg instanceof BullyElectionMessage) {
			
			// Respond to the message
			try {
				channel.send(null, new BullyAnswerMessage().serialize());
			}catch (Exception e) {
				e.printStackTrace();
			}
			
			// If we know that this RM has the highest ID, we've 'won'
			verifyIfPrimary();
			if (primary.equals(channel.getAddress())) {

				// Then we'll send a coordinator message
				try {
					channel.send(null, new BullyCoordinatorMessage().serialize());
				}catch (Exception e) {
					e.printStackTrace();
				}
			}else {
				// Otherwise, start a new election
				startElection();
			}
			
		}else if (msg instanceof BullyAnswerMessage) {
			
			// There is a RM with a higher ID, therefore, cancel the election we started
			if (electionTimer != null) {
				System.out.println("Election started");
				electionTimer.cancel();
				electionTimer = null;
			}
			
		// A new (maybe) coordinator has arrived
		}else if (msg instanceof BullyCoordinatorMessage) {
			System.out.println("Primary announcement received from " + m.getSrc());
			
			// Make sure it actually has a higher ID than us
			if (channel.getAddress().compareTo(m.getSrc()) > 0) {
				startElection();
			}else {
				// Set 'primary' = address of whoever we received it from
				primary = m.getSrc();
			}
		}
		
		// Client request: Fetch state
		
		// Client request: Add GObject
		
		// Client request: Remove GObject
	}
	
	/**
	 * Start an election. Initiates the bully algorithm.
	 */
	private void startElection() {
		System.out.println("Election started");
		
		// Get all nodes with a higher ID than ours, ignoring the frontend
		List<Address> higherNodes = getHigherNodes().stream()
				.filter(addr -> !addr.equals(frontend))
				.collect(Collectors.toList());
		
		// If we're the highest node, announce that we've won
		if (higherNodes.size() == 0) {
			System.out.println("No higher nodes exist, we're the coordinator");
			try {
				channel.send(null, new BullyCoordinatorMessage().serialize());
			}catch (Exception e) {
				e.printStackTrace();
			}
			return;
		}
		
		// Send an election message to each of them
		BullyElectionMessage bem = new BullyElectionMessage();
		byte[] bemBuffer = bem.serialize();
		
		for (Address addr : higherNodes) {
			try {
				channel.send(addr, bemBuffer);
			}catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		// After time T, if no-one responded then we're the new coordinator
		// The timer is cancelled as soon as a BullyAnswerMessage is received
		electionTimer = new Timer();
		electionTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				// Message that we're the coordinator
				System.out.println("No higher node responded to election, we're the coordinator");
				try {
					channel.send(null, new BullyCoordinatorMessage().serialize());
				}catch (Exception e) {
					e.printStackTrace();
				}
			}
		}, electionWaitTime);
	}

	@Override
	public void getState(OutputStream os) {
		// Send the local CAD state to the new replica manager
		synchronized (state) {
			try {
				// Only send state if we've actually got it
				if (stateSynced) {
					Util.objectToStream(state, new DataOutputStream(os));
				}else {
					Util.objectToStream(null, new DataOutputStream(os));
				}
			}catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void setState(InputStream is) {
		// Read the current CAD state from the inputstream and set the local state
		synchronized (state) {
			try {
				CADState response = (CADState) Util.objectFromStream(new DataInputStream(is));
				if (response == null) return;
				
				state = response;
				stateSynced = true;
			}catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void viewAccepted(View view) {
		System.out.println(view.toString());
		
		currentView = view;
		
		if (view.size() == 1 && stateSynced == false) {
			System.out.println("First RM, creating new state");
			stateSynced = true;
		}

		// When we initially join, ask every other node for the state
		if (stateSynced == false) {
			System.out.println("Requesting state");
			
			for (Address addr : view) {
				if (addr.equals(channel.getAddress())) continue;
				
				try {
					channel.getState(addr, 10000);
				}catch (Exception e) {
					e.printStackTrace();
					continue;
				}

				if (stateSynced) {
					System.out.println("Got state");
					break;
				}
			}
			
			if (!stateSynced) {
				System.out.println("Failed to retrieve state, creating new state");
				stateSynced = true;
			}
		}
		
		// If this is the primary RM, announce that this is the coordinator to everyone
		verifyIfPrimary();
		if (primary != null && primary.equals(channel.getAddress())) {
			try {
				channel.send(null, new BullyCoordinatorMessage().serialize());
			}catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private void verifyIfPrimary() {
		// Get all the nodes with a higher ID
		ArrayList<Address> higherNodes = getHigherNodes();
		
		// Check if there are none, or if the only one is the frontend
		if (higherNodes.size() == 0 || (higherNodes.size() == 1 && higherNodes.get(0).equals(frontend))) {
			
			// Then we're the primary
			primary = channel.getAddress();
		}else
			primary = null;
	}
	
	/**
	 * Get the nodes with a higher ID than this one. Includes the frontend if it's connected 
	 * @return List of nodes with a higher ID than this one
	 */
	public ArrayList<Address> getHigherNodes() {
		if (currentView == null) return null;
		
		ArrayList<Address> result = new ArrayList<Address>();
		
		for (Address addr : currentView) {
			if (addr.compareTo(channel.getAddress()) > 0)
				result.add(addr);
		}
		
		return result;
	}

}
