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
import se.his.drts.message.DrawObject;
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

	@Override
	public void receive(Message m) {
		System.out.println(m);
		
		// Parse message
		Optional<MessagePayload> optMsg = MessagePayload.createMessage(m.getBuffer());
		if (!optMsg.isPresent()) {
			System.out.println("Failed to deserialize message");
			return;
		}
		MessagePayload msg = optMsg.get();
		
		// If it's a primary announcement message
		if (msg instanceof BullyCoordinatorMessage) {

			// Set 'primary' equal to the sender
			primary = m.getSrc();
			System.out.println("Node " + primary.toString() + " registered as primary");
		}
		else if (msg instanceof DrawObject) {
			//TODO send message to the clients 
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
	}

	@Override
	public void getState(OutputStream os) throws Exception {
		Util.objectToStream(null, new DataOutputStream(os));
	}

}
