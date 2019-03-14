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

import se.his.drts.message.ClientConnectionRequest;
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
		
		//TODO start a send thread
		
		
		
		//set up socket
		while(true) {
			if (!(setupSocket(serverAddress, serverPort))) {
				continue;
			}
			handshake();
			
			while(receive()) { }
		}
	}
	public boolean setupSocket(String serverAddress, int serverPort) {
		try {
			socket = new Socket(serverAddress, serverPort);
			writer = new PrintWriter(socket.getOutputStream(), true);
			reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		} catch (IOException e) {
			e.printStackTrace();			
			return false;
		}
		return true;
	}
	
	public void handshake() {
		//TODO send name + id OR SOMETHING LIKE THAT to the front end 
		ClientConnectionRequest clientConnectionRequestMessage = new ClientConnectionRequest(socket.getInetAddress().toString() + socket.getLocalPort());
		
		String message = clientConnectionRequestMessage.serializeAsString();
		
		writer.println("message");
	}
	
	public boolean receive() {
		// TODO receive an object that should be added to the list, or removed
		
		String message = "";
		try {
			message = reader.readLine();
		} catch (IOException e) {
			// frontend has crashed 
			try {
				socket.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			return false;
		}
		
		byte[] bytes = message.getBytes();
		
		Optional<MessagePayload> mp = MessagePayload.createMessage(bytes);
		
		MessagePayload messagePayload = mp.get();
		
		if (messagePayload instanceof DrawObject) {			
			DrawObject drawObjectMessage = (DrawObject) messagePayload;
			
			LocalAddGObject(drawObjectMessage.getGObject());
		}
		else if (messagePayload instanceof RemoveObject) {			
			RemoveObject removeObjectMessage = (RemoveObject) messagePayload;
			
			//TODO maybe just an uuid for death certificate?
			LocalRemoveGObject(removeObjectMessage.getGObject());
		}
		else if(messagePayload instanceof RemoveObject) { // TODO change to some acc message -> for sendMessage method?
			// TODO notify the "send" thread that it can continue to send messages and not overwrite them 
		}
		else {
			
		}
		return true;
	}
	
	public void sendMessage() { 
		
		//TODO is invoked by a timer thread or something, if empty/ first message added then send immediately ?? maybe ?? 
		
		MessagePayload messagePayload = null;
		
		synchronized (messagesToSend) {
			messagePayload = messagesToSend.removeFirst();
		}
		String message = messagePayload.serializeAsString();

		writer.println(message);
		
		// TODO wait for an acknowledgement from the front end that the message has been sent on to the replica managers
		try {
			wait();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
	}
	
	public void addMessageToQueue(MessagePayload messagePayload) {
		synchronized (messagesToSend) {
			messagesToSend.add(messagePayload);
		}
		
	}
	public void LocalRemoveGObject(GObject object) { 
		
		for (GObject go : this) {
			if (go.getUuid().equals(object.getUuid())) {
				go.setRemoved(true);
			}
		}
		
		// check if object already exists
		
	}

	public void LocalAddGObject(GObject object) { 
		
		for (GObject go : this) {
			if (go.getUuid().equals(object.getUuid())) {
				return;
			}
		}
		
		synchronized (objectList) {
			objectList.add(object);
		}
	}

	@Override
	public void addGObject(GObject object) {
		
		synchronized (objectList) {
			objectList.add(object);
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
