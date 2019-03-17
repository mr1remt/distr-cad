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

import DCAD.GColor;
import DCAD.GObject;
import DCAD.Shape;
import se.his.drts.message.BullyCoordinatorMessage;
import se.his.drts.message.ClientResponseMessage;
import se.his.drts.message.DrawObjectRequest;
import se.his.drts.message.FrontendAnnouncement;
import se.his.drts.message.MessagePayload;

/**
 * Manages communication with the replica managers
 */
public class RMConnection extends ReceiverAdapter {
	
	private JChannel	channel;
	private Address		primary = null;

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
		if (primary == null) return false;
		
		try {
			channel.send(primary, msg.serialize());
			return true;
		}catch (Exception e) {
			e.printStackTrace();
		}
		return false;
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

		System.out.println("Received " + msg.toString());
		
		// If it's a primary announcement message
		if (msg instanceof BullyCoordinatorMessage) {

			// Set 'primary' equal to the sender
			primary = m.getSrc();
			System.out.println("Node " + primary.toString() + " registered as primary");
			
		}else if (msg instanceof ClientResponseMessage) {
			
			// TODO: Forward response to the correct client
			Frontend.frontend.forwardResponse((ClientResponseMessage) msg);
		}
	}

	@Override
	public void viewAccepted(View view) {
		System.out.println(view.toString());
		
		// The view has changed, announce to everyone that this is the frontend
		FrontendAnnouncement fa = new FrontendAnnouncement();
		
		try {
			channel.send(new Message(null, null, fa.serialize()));
		}catch (Exception e) {
			e.printStackTrace();
		}
		
		// Test, Send a client request to add a GObject
		GObject testObj = new GObject(Shape.LINE, GColor.WHITE, 0, 1, 1, 1);
		try {
			channel.send(primary, new DrawObjectRequest(testObj).serialize());
		}catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void getState(OutputStream os) throws Exception {
		Util.objectToStream(null, new DataOutputStream(os));
	}
	
}
