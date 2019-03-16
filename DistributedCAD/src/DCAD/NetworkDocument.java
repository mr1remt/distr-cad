package DCAD;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Optional;

import se.his.drts.message.ClientConnectionRequest;
import se.his.drts.message.DrawObjectRequest;
import se.his.drts.message.MessageConfirmed;
import se.his.drts.message.MessagePayload;
import se.his.drts.message.RemoveObject;

public class NetworkDocument extends CadDocument{
	
	private Socket socket;
	private PrintWriter writer;
	private BufferedReader reader;
	
	private NetworkSend ns;
	
	private LinkedList<GObject> objectList = new LinkedList<GObject>();
	
	public NetworkDocument(String serverAddress, int serverPort) {			
		//set up socket
		while(true) {
			if (!(setupSocket(serverAddress, serverPort))) {
				// if the front end is down then the client cannot connect and should try again
				continue;
			}
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
		if(ns == null) {		
			// start a send thread
			new Thread(ns = new NetworkSend(this, writer)).start();
		}
		
		handshake();

		ns.notify();
		
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
		

		if (messagePayload instanceof DrawObjectRequest) {
			//TODO add message
			
			DrawObjectRequest drawObjectMessage = (DrawObjectRequest) messagePayload;
			
			LocalAddGObject(drawObjectMessage.getObject());
		}
		else if (messagePayload instanceof RemoveObject) {			
			RemoveObject removeObjectMessage = (RemoveObject) messagePayload;
			
			LocalRemoveGObject(removeObjectMessage.getGObject());
		}
		else if(messagePayload instanceof MessageConfirmed) { // TODO change to some acc message -> for sendMessage method?
			// notify the "send" thread that it can continue to send messages and not overwrite them
			MessageConfirmed messageConfirmedMessage = (MessageConfirmed) messagePayload;
			ns.setMessageConfirmed(true);
			ns.notify();
		}
		else {
			
		}
		return true;
	}

	
	public void addMessageToQueue(MessagePayload messagePayload) {
		ns.addMessageToSend(messagePayload);
		
	}
	public void LocalRemoveGObject(GObject object) { 

		// find the object if it already exists and remove it
		for (GObject go : this) {
			if (go.getID() == object.getID()) {
				objectList.remove(go);
			}
		}
	}

	public void LocalAddGObject(GObject object) { 
		
		for (GObject go : this) {
			if (go.getID() == object.getID()) {
				// if object already exists
				return;
			}
		}
		// add if object is new
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
		DrawObjectRequest drawObjectMessage = new DrawObjectRequest(object);		
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
