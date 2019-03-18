package DCAD;

import java.io.PrintWriter;
import java.util.LinkedList;

import se.his.drts.message.ClientResponseMessage;
import se.his.drts.message.UniqueMessage;

public class NetworkSend implements Runnable{

	private PrintWriter writer = null;
	private LinkedList<UniqueMessage> messagesToSend = new LinkedList<UniqueMessage>();
	private final Object waitNotifyLock = new Object();
	
	private ClientResponseMessage messageConfirmed;
	private boolean socketIsClosed = true;
		
	public NetworkSend() {
	}
	
	public ClientResponseMessage getMessageConfirmed() {return messageConfirmed;}
	public void setMessageConfirmed(ClientResponseMessage messageConfirmed) {this.messageConfirmed = messageConfirmed;}
	
	public boolean socketIsClosed() {return socketIsClosed;}
	public void setSocketIsClosed(boolean isClosed) {this.socketIsClosed = isClosed;}
	
	public void setWriter(PrintWriter writer) {this.writer = writer;}
	public boolean writerExists() {
		if (writer != null) {
			return true;
		}
		return false;
	}
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

	public void sendMessage() { 
				
		UniqueMessage uniqueMessage = null;
		
		while(true) {
			synchronized (messagesToSend) {
				uniqueMessage = (UniqueMessage) messagesToSend.removeFirst();
			}
			String message = uniqueMessage.serializeAsString();
		
			if(writerExists()){
				writer.println(message);
				System.out.println("ns: sendm: " + message);
			}
			else {
				addMessageToSendFirst(uniqueMessage);
				break;
			}
			// wait for an acknowledgement from the front end that the message has been sent  
			// on to the replica managers so that the client can continue sending messages
			try {
				synchronized (waitNotifyLock) {
					waitNotifyLock.wait(500);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if (getMessageConfirmed() == null) {
				break;
			}
			else if (getMessageConfirmed().getInstanceID() == uniqueMessage.getInstanceID() && getMessageConfirmed().getOperationSuccess()) {
				setMessageConfirmed(null);
				break;
			}
			else if (socketIsClosed()){
				addMessageToSendFirst(uniqueMessage);
				break;
			}
			else {
				addMessageToSendFirst(uniqueMessage);
			}
		}
	}
}
