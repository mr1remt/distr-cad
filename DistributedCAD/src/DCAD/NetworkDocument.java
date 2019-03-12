package DCAD;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Optional;

import se.his.drts.message.ConnectionRequest;
import se.his.drts.message.DrawObject;
import se.his.drts.message.MessagePayload;
import se.his.drts.message.RemoveObject;

public class NetworkDocument extends CadDocument{
	
	private Socket socket;
	private PrintWriter writer;
	private BufferedReader reader;
	
	private LinkedList<GObject> objectList = new LinkedList<GObject>();
	
	private LinkedList<MessagePayload> messagesToSend = new LinkedList<MessagePayload>();

	public NetworkDocument(String serverAddress, int serverPort) {		
		//set up socket
		try {
			socket = new Socket(serverAddress, serverPort);
			writer = new PrintWriter(socket.getOutputStream(), true);
			reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		} catch (IOException e) {
			//TODO retry setup?
		}
		while(true) {
			handshake();
			
			while(receive()) {
			}
		}
		
		
	}
	
	public void handshake() {
		//TODO send name or id to the front end 
		ConnectionRequest connectionRequestMessage = new ConnectionRequest("IP + something else?");
		
		String message = connectionRequestMessage.serializeAsString();

		writer.println(message);
		
		message = "";
		try {
			message = reader.readLine();
		} catch (SocketException e) {
			// server has crashed ???? 
			
			//TODO maybe this is right i dont know :)
			if(!(socket.isClosed())) {
				try {
					socket.shutdownInput();
					socket.shutdownOutput();
					socket.close();
				} catch (IOException e1) {
					e.printStackTrace();
					// Failed to close socket	
				}
			}
						
		} catch (Exception e) {
			//failed to read message?
			e.printStackTrace();
		}
		
		byte[] bytes = message.getBytes();
		
		Optional<MessagePayload> mp = MessagePayload.createMessage(bytes);
		
		MessagePayload messagePayload = mp.get();
		
		if (messagePayload instanceof ConnectionRequest) {
			//TODO add message
			
			ConnectionRequest connectionReplyMessage = (ConnectionRequest) messagePayload;
			
			connectionReplyMessage.getID();
		}
		else {
			
		}
		
	}
	
	public boolean receive() {
		// TODO receive an object that should be added to the list, or removed
		
		String message = "";
		try {
			message = reader.readLine();
		} catch (SocketException e) {
			// server has crashed ???? 
			
			//TODO maybe this is right i dont know :)
			if(!(socket.isClosed())) {
				try {
					socket.shutdownInput();
					socket.shutdownOutput();
					socket.close();
				} catch (IOException e1) {
					e.printStackTrace();
					// Failed to close socket	
				}
			}
			
			return false;
			
		} catch (Exception e) {
			//failed to read message?
			e.printStackTrace();
		}
		
		byte[] bytes = message.getBytes();
		
		Optional<MessagePayload> mp = MessagePayload.createMessage(bytes);
		
		MessagePayload messagePayload = mp.get();
		
		if (messagePayload instanceof DrawObject) {
			//TODO add message
			
			DrawObject drawObjectMessage = (DrawObject) messagePayload;
			
			LocalAddGObject(drawObjectMessage.getGObject());
		}
		else {
			
		}
		return true;
	}
	
	public void sendMessage() { 
		
		//TODO is invoked by a timer thread or something, if empty/ first message added then send immediately
		
		MessagePayload messagePayload = null;
		
		synchronized (messagesToSend) {
			messagePayload = messagesToSend.removeFirst();
		}
		String message = messagePayload.serializeAsString();

		writer.println(message);
		
		// TODO wait for an acknowledgement from the front end that the message has been sent on to the replica managers
		
	}
	
	
	public void addMessageToQueue(MessagePayload messagePayload) {
		synchronized (messagesToSend) {
			messagesToSend.add(messagePayload);
		}
		
		
	}
	public void LocalRemoveGObject(GObject object) { 
		
		// in order to remove an object received from the rm/fe
		
		// check if object already exists
		
	}

	public void LocalAddGObject(GObject object) { 
		
		// in order to add an object received from the rm/fe
		
		// check if object already exists
		
		
		
	}

	@Override
	public void addGObject(GObject object) {
		
		synchronized (objectList) {
			objectList.addLast(object);
		}
		
		//send a message with the object that should be added
		DrawObject drawObjectMessage = new DrawObject(object);		
		addMessageToQueue(drawObjectMessage);
	}

	@Override
	public void removeLastGObject() {
		
		GObject removed = null;
		
		synchronized (objectList) {
			removed = objectList.removeLast();
		}		
		
		//send a message with the object that should be removed
		RemoveObject removeObjectMessage = new RemoveObject(removed);		
		addMessageToQueue(removeObjectMessage);

	}

	@Override
	public int size() {
		synchronized (objectList) {
			return objectList.size();
		}
	}
	
	//TODO maybe something can go wrong if removing/ adding during iteration
	public Iterator<GObject> iterator() {
		return objectList.iterator(); 
	}
}
