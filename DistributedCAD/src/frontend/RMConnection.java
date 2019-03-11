package frontend;

import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;

/**
 * Manages communication with the replica managers
 */
public class RMConnection extends ReceiverAdapter {
	
	private JChannel	channel;
	private Address		primary = null;

	public RMConnection() throws Exception {
		channel = new JChannel();
		channel.setReceiver(this);
		channel.connect("CAD-Service");
	}

	@Override
	public void receive(Message m) {
		System.out.println((String) m.getObject());
		
		// Parse message
		
		// If it's a primary announcement message
			// Set 'primary' equal to the sender
	}

	@Override
	public void viewAccepted(View view) {
		System.out.println(view.toString());
	}

}
