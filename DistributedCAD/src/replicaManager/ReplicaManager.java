package replicaManager;

import java.io.InputStream;
import java.io.OutputStream;

import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;

public class ReplicaManager extends ReceiverAdapter {
	
	private JChannel channel;

	public ReplicaManager() throws Exception {
		channel = new JChannel();
		channel.setReceiver(this);
		channel.connect("CAD-Service");
		
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
		// TODO Auto-generated method stub
		
	}

	@Override
	public void receive(Message m) {
		System.out.println(m.toString());
	}

	@Override
	public void setState(InputStream is) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void viewAccepted(View view) {
		System.out.println(view.toString());
	}

}
