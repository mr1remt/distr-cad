package replicaManager;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;
import org.jgroups.util.Util;

public class ReplicaManager extends ReceiverAdapter {
	
	private JChannel	channel;
	private View		currentView;
	private Address		primary;
	
	// The CAD documents state, everything the replica manager needs to keep track of
	private CADState	state;

	public ReplicaManager() throws Exception {
		state = new CADState();
		
		channel = new JChannel();
		channel.setReceiver(this);
		channel.connect("CAD-Service");
		
		channel.getState(null, 10000);
		
		channel.send(new Message(null, null, "Test"));
		
		while (true) {
			Thread.sleep(1000000000);
		}
	}

	public static void main(String[] args) {
		try {
			new ReplicaManager();
		}catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void getState(OutputStream os) throws Exception {
		// Send the local CAD state to the new replica manager
		synchronized (state) {
			Util.objectToStream(state, new DataOutputStream(os));
		}
	}

	@Override
	public void receive(Message m) {
		System.out.println((String) m.getObject());
		
		// TODO: Message handling
		
		// Receive message and parse it
		
		// If we received a "Primary Announcement" message
			// Set 'primary' = address of whoever we received it from
		
		// Bully algorithm
		
		// Client request: Fetch state
		
	}

	@Override
	public void setState(InputStream is) throws Exception {
		// Read the current CAD state from the inputstream and set the local state
		synchronized (state) {
			state = (CADState) Util.objectFromStream(new DataInputStream(is));
		}
	}

	@Override
	public void viewAccepted(View view) {
		System.out.println(view.toString());
		
		currentView = view;
		
		// TODO: If this is the primary RM, announce that this is the coordinator to everyone
	}

}
