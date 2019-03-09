package replicaManager;

import java.io.InputStream;
import java.io.OutputStream;

import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.Receiver;
import org.jgroups.View;

public class ReplicaManager implements Receiver {
	
	private JChannel channel;

	public ReplicaManager() throws Exception {
		channel = new JChannel();
		channel.setReceiver(this);
		channel.connect("CAD-Service");
		
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
		// TODO Auto-generated method stub
		
	}

	@Override
	public void receive(Message m) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setState(InputStream is) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void block() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void suspect(Address addr) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void unblock() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void viewAccepted(View view) {
		// TODO Auto-generated method stub
		
	}

}
