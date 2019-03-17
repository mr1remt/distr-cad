package DCAD;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.jgroups.stack.GossipData;

import se.his.drts.message.ClientConnectionRequest;
import se.his.drts.message.ClientResponseMessage;
import se.his.drts.message.DeleteObjectRequest;
import se.his.drts.message.DrawObjectRequest;
import se.his.drts.message.MessageConfirmed;
import se.his.drts.message.MessagePayload;
import se.his.drts.message.RetrieveObjectsRequest;
import se.his.drts.message.UniqueMessage;

public class NetworkDocument extends CadDocument implements Runnable{
	
	private Socket socket = null;
	private PrintWriter writer;
	private BufferedReader reader;
	private String clientID;
	
	private NetworkSend ns = null;
	
	private String serverAddress;
	private int serverPort;
	
	private ArrayList<GObject> objectList = new ArrayList<GObject>();
	
	public NetworkDocument(String serverAddress, int serverPort) {			
		this.serverAddress = serverAddress;
		this.serverPort = serverPort;
		
		if (ns == null) {
			// start a send thread if it doesn't already exist
			new Thread(ns = new NetworkSend()).start();
		}
	}
	
	@Override
	public void run() {
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
		if (socket == null || socket.isClosed()) {
			try {
				socket = new Socket(serverAddress, serverPort);
				writer = new PrintWriter(socket.getOutputStream(), true);
				reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			} catch (IOException e) {
				return false;
			}
			ns.setWriter(writer);

			handshake();
			
			ns.setSocketIsClosed(false);

System.out.println("handshaked");
			return true;
		}
		return false;
	}
	
	public void handshake() {
		ClientConnectionRequest clientConnectionRequestMessage = new ClientConnectionRequest(clientID);
		
		String message = clientConnectionRequestMessage.serializeAsString();
		
		writer.println(message);
		
		RetrieveObjectsRequest retrieveObjectsRequest = new RetrieveObjectsRequest(); 
		ns.addMessageToSendFirst(retrieveObjectsRequest);
	}
	
	public boolean receive() {
		
		String message = "";
		try {
			message = reader.readLine();
		} catch (IOException e) {
			// frontend has crashed 
			try {
				ns.setSocketIsClosed(true);
				ns.setWriter(null);
				socket.close();
System.out.println("frontend crash");
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			return false;
		}
		
		byte[] bytes = message.getBytes();
		Optional<MessagePayload> mp = MessagePayload.createMessage(bytes);
		if(!mp.isPresent()) {
			return true;
		}
		UniqueMessage uniqueMessage = (UniqueMessage) mp.get();

System.out.println("message received: " + uniqueMessage);

		if (uniqueMessage instanceof DrawObjectRequest) {
			
			DrawObjectRequest drawObjectMessage = (DrawObjectRequest) uniqueMessage;
			
			localAddGObject(drawObjectMessage.getObject());
		}
		else if (uniqueMessage instanceof DeleteObjectRequest) {			
			DeleteObjectRequest deleteObjectRequest = (DeleteObjectRequest) uniqueMessage;
			
			localRemoveGObject(deleteObjectRequest.getGObjectID());
		}
		else if(uniqueMessage instanceof MessageConfirmed) { 
			ClientResponseMessage clientResponseMessage = (ClientResponseMessage) uniqueMessage;
			// notify the "send" thread that it can continue to send messages and not overwrite them
			ns.setMessageConfirmed(clientResponseMessage);
			ns.notify();
		}
		else if (uniqueMessage instanceof ClientResponseMessage) {
			ClientResponseMessage clientResponseMessage = (ClientResponseMessage) uniqueMessage;
			
			localAddGObjectList(clientResponseMessage.getObjectList());
			
		}
		else {
			//message not recognized
		}
		return true;
	}

	public void localAddGObjectList(List<GObject> list) {
		for (GObject go : list) {
			localAddGObject(go);
		}
	}

	public void localAddGObject(GObject object) { 
		
		for (GObject go : this) {
			if (go.getID() == object.getID()) {
				// if object already exists change Active to the newest version of the object
				if (go.isActive()) {
					go.setActive(object.isActive());
				}
				return;
			}
		}
		// if object is new then add
		synchronized (objectList) {
			objectList.add(object);
		}
	}
	
	public void localRemoveGObject(Long objectID) { 
		// find the object if it already exists and remove it
		for (GObject go : this) {
			if (go.getID() == objectID) {
				go.setActive(false);
			}
		}
	}

	@Override
	public void addGObject(GObject object) {
		
		synchronized (objectList) {
			objectList.add(object);
		}
		
		//send a message with the object that should be added
		DrawObjectRequest drawObjectMessage = new DrawObjectRequest(object);		
		ns.addMessageToSend(drawObjectMessage);
	}

	@Override
	public void removeLastGObject() {
				
		DeleteObjectRequest deleteObjectRequest = null;
		
		synchronized (objectList) {
			if(objectList.size() < 1) {
				return;
			}
			for (int i = objectList.size()-1; i >= 0; i--) {
				GObject gobject = objectList.get(i);
				if (gobject.isActive()) {
					gobject.setActive(false);
					deleteObjectRequest = new DeleteObjectRequest(gobject.getID());
					break;
				}
			}				
		}		
		
		//send a message with the object that should be deleted
		if (deleteObjectRequest != null) {
			ns.addMessageToSend(deleteObjectRequest);
		}
	}

	@Override
	public int size() {
		synchronized (objectList) {
			return objectList.size();
		}
	}
	
	public Iterator<GObject> iterator() {
		return objectList.iterator(); 
	}
	
	public static String getClientID(Socket socket) {
		return socket.getInetAddress().toString() + ":" + socket.getLocalPort();
	}
}
