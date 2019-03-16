package DCAD;

import java.io.PrintWriter;
import java.util.LinkedList;

import se.his.drts.message.MessagePayload;

public class NetworkSend implements Runnable{

	private NetworkDocument nd;
	private PrintWriter writer;
	private LinkedList<MessagePayload> messagesToSend = new LinkedList<MessagePayload>();
	private Object lock = new Object();
	
	//TODO use uuid?
	private boolean messageConfirmed;
	
	public NetworkSend(NetworkDocument nd, PrintWriter writer) {
		this.nd = nd;
		this.writer = writer;
		
		writer.println("sss");
	}
	
	public boolean isMessageConfirmed() {return messageConfirmed;}
	public void setMessageConfirmed(boolean messageConfirmed) {this.messageConfirmed = messageConfirmed;}
	
	public void notifySend() {
		synchronized(lock){
			lock.notify();	
		}
	}
	
	@Override
	public void run() {
		while(true) {
			if (messageAvailable()) {
				sendMessage();
			}
			else {
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public boolean messageAvailable() {
		synchronized (messagesToSend){
			if (messagesToSend.size() > 0) {
				return true;
			}
		}
		return false;
	}
	
	public void addMessageToSend(MessagePayload mp) {
		synchronized (messagesToSend) {
			messagesToSend.add(mp);
		}
	}

	public boolean sendMessage() { 
				
		MessagePayload messagePayload = null;
		
		synchronized (messagesToSend) {
			messagePayload = messagesToSend.removeFirst();
		}
		String message = messagePayload.serializeAsString();
		
		while(true) {
			writer.println(message);
			
			/* wait for an acknowledgement from the front end that the message has been sent on to the replica 
			* managers so that the client can continue sending messages 
			* TODO use uuid ??
			*/
			try {
				lock.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if (isMessageConfirmed()) {
				setMessageConfirmed(false);
				break;
			}
			
		}
		return true;
	}
}
