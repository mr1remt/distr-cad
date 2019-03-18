package DCAD;

import java.io.PrintWriter;
import java.util.LinkedList;

import se.his.drts.message.ClientResponseMessage;
import se.his.drts.message.UniqueMessage;

public class NetworkSend implements Runnable{

	private PrintWriter writer = null;
	private LinkedList<UniqueMessage> messagesToSend = new LinkedList<UniqueMessage>();
	private LinkedList<ClientResponseMessage> messagesConfirmed = new LinkedList<ClientResponseMessage>();
	private final Object waitNotifyLock = new Object();
	private final Object messagesConfirmedLock= new Object();
	
	private boolean socketIsClosed = true;
		
	public NetworkSend() {
	}
	
	public void addMessageConfirmed(ClientResponseMessage messageConfirmed) {
		synchronized (messagesConfirmedLock) {
			for (ClientResponseMessage crm : messagesConfirmed) {
				if (crm.getInstanceID() == messageConfirmed.getInstanceID()) {
					crm.setOperationSuccess(messageConfirmed.getOperationSuccess());
					return;
				}
			}
			messagesConfirmed.add(messageConfirmed);
		}
	}
	public boolean requestSuccessful(long instanceID) {
		synchronized (messagesConfirmedLock) {
			for (ClientResponseMessage crm : messagesConfirmed) {
				if (crm.getInstanceID() == instanceID && crm.getOperationSuccess()) {
					return true;
				}
			}
		}
		return false;
	}
	
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
			// send a message if there are messages to send and if the socket is open
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
	//add a message to the list at the FIRST position
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
			if (requestSuccessful(uniqueMessage.getInstanceID())) {
System.out.println("1");
				break;
			}
			else if (socketIsClosed()){
System.out.println("2");
				addMessageToSendFirst(uniqueMessage);
				break;
			}
			else {
System.out.println("3");
				addMessageToSendFirst(uniqueMessage);
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

			}
		}
	}
}
