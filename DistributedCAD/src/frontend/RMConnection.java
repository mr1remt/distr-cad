package frontend;

import java.io.DataOutputStream;
import java.io.OutputStream;
import java.util.Optional;

import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;
import org.jgroups.util.Util;

import se.his.drts.message.BullyCoordinatorMessage;
import se.his.drts.message.BullyElectionMessage;
import se.his.drts.message.ClientResponseMessage;
import se.his.drts.message.DeleteObjectRequest;
import se.his.drts.message.DrawObjectRequest;
import se.his.drts.message.FrontendAnnouncement;
import se.his.drts.message.MessagePayload;

/**
 * Manages communication with the replica managers
 */
public class RMConnection extends ReceiverAdapter {
	
	private JChannel	channel;
	private Address		primary = null;
	private View		currentView;

	public void connect() throws Exception {
		channel = new JChannel();
		channel.setReceiver(this);
		channel.connect("CAD-Service");
	}
	
	/**
	 * Send a client message to the primary replica manager. Returns true if the message was successfully sent
	 * @param msg
	 * @return
	 */
	public boolean send(MessagePayload msg) {
		if (primary == null) {
			startElection();
			return false;
		}

		System.out.println("Sending to RM " + msg.toString());
		try {
			channel.send(primary, msg.serialize());
			return true;
		}catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * Starts an election among the replica managers
	 */
	private void startElection() {
		// Don't bother if we're the only member
		if (currentView.size() == 1) return;
		
		// Find the first other member in the group and send a BullyElectionMessage to them
		for (Address addr : currentView) {
			if (addr.equals(channel.getAddress())) continue;
			
			try {
				channel.send(addr, new BullyElectionMessage());
			}catch (Exception e) {
				continue;
			}
			break;
		}
	}

	@Override
	public void receive(Message m) {
		
		// Parse message
		Optional<MessagePayload> optMsg = MessagePayload.createMessage(m.getBuffer());
		if (!optMsg.isPresent()) {
			System.out.println("Failed to deserialize message");
			return;
		}
		MessagePayload msg = optMsg.get();

		System.out.println("Received from RM " + msg.toString());
		
		// If it's a primary announcement message
		if (msg instanceof BullyCoordinatorMessage) {

			// Set 'primary' equal to the sender
			primary = m.getSrc();
			System.out.println("Node " + primary.toString() + " registered as primary");
			
		}else if (msg instanceof ClientResponseMessage) {
			ClientResponseMessage response = (ClientResponseMessage) msg;
			
			// Forward response to the correct client
			Frontend.frontend.forwardResponse(response);
			
			// If this request added to the document, message all other connected clients
			if (response.getDrawnObject() != null) {
				DrawObjectRequest req = new DrawObjectRequest(response.getDrawnObject());
				Frontend.frontend.messageAllClients(req);
			}
			
			// If this request added deleted from the document, message all other connected clients
			if (response.getDeletedObject() != 0) {
				DeleteObjectRequest req = new DeleteObjectRequest(response.getDeletedObject());
				Frontend.frontend.messageAllClients(req);
			}
		}
	}

	@Override
	public void viewAccepted(View view) {
		System.out.println(view.toString());
		
		currentView = view;
		
		// The view has changed, announce to everyone that this is the frontend
		FrontendAnnouncement fa = new FrontendAnnouncement();
		
		try {
			channel.send(new Message(null, null, fa.serialize()));
		}catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void getState(OutputStream os) throws Exception {
		Util.objectToStream(null, new DataOutputStream(os));
	}
	
}
