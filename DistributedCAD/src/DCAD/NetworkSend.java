package DCAD;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedList;

import se.his.drts.message.UniqueMessage;

public class NetworkSend implements Runnable{

	private PrintWriter writer;
	private LinkedList<UniqueMessage> messagesToSend = new LinkedList<UniqueMessage>();
	private ArrayList<UniqueMessage> messagesss = new ArrayList<>();
	private final Object waitNotifyLock = new Object();
	private final Object isClosedLock = new Object();
	
	private UniqueMessage messageConfirmed;
	private boolean socketIsClosed = false;
	
	public NetworkSend(PrintWriter writer) {
		this.writer = writer;
	}
	
	public UniqueMessage getMessageConfirmed() {return messageConfirmed;}
	public void setMessageConfirmed(UniqueMessage messageConfirmed) {this.messageConfirmed = messageConfirmed;}
	
	public boolean socketIsClosed() {
		synchronized (isClosedLock) {return socketIsClosed;}
	}
	public void setSocketIsClosed(boolean isClosed) {
		synchronized (isClosedLock) {this.socketIsClosed = isClosed;}
	}
	
	public void setWriter(PrintWriter writer) {this.writer = writer;}
	
	public void notifySend() {
		synchronized(waitNotifyLock){
			waitNotifyLock.notify();	
		}
	}
	
	@Override
	public void run() {
		while(true) {
			if (messageAvailable() && !(socketIsClosed())) {
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
	
	public void addMessageToSendFirst(UniqueMessage mp) {
		synchronized (messagesToSend) {
			messagesToSend.addFirst(mp);
		}
	}
	
	public void addMessageToSend(UniqueMessage mp) {
		synchronized (messagesToSend) {
			messagesToSend.add(mp);
		}
	}

	public boolean sendMessage() { 
				
		UniqueMessage uniqueMessage = null;
		
		synchronized (messagesToSend) {
			uniqueMessage = (UniqueMessage) messagesToSend.removeFirst();
		}
		String message = uniqueMessage.serializeAsString();
		
		while(true) {
System.out.println("sending message: " + uniqueMessage);
			writer.println(message);
			
			// wait for an acknowledgement from the front end that the message has been sent  
			// on to the replica managers so that the client can continue sending messages
			
			try {
				synchronized (waitNotifyLock) {
					waitNotifyLock.wait(1000);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if (getMessageConfirmed().getInstanceID() == uniqueMessage.getInstanceID()) {
				setMessageConfirmed(null);
				break;
			}
			else if (socketIsClosed()){
				addMessageToSendFirst(uniqueMessage);
				break;
			}
		}
		return true;
	}
}
