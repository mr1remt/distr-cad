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

import org.omg.Messaging.SyncScopeHelper;

import se.his.drts.message.ClientConnectionRequest;
import se.his.drts.message.ClientResponseMessage;
import se.his.drts.message.DeleteObjectRequest;
import se.his.drts.message.DrawObjectRequest;
import se.his.drts.message.MessagePayload;
import se.his.drts.message.RetrieveObjectsRequest;
import se.his.drts.message.UniqueMessage;

public class NetworkDocument extends CadDocument implements Runnable{
	
	private GUI gui;
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
	public void setGui(GUI gui) {
		this.gui = gui;
	}
	
	@Override
	public void run() {
			while(true) {
				//set up socket
				if (!(setupSocket(serverAddress, serverPort))) {
					// if the frontend is down, the client cannot connect and should try again, 
					// skipping receive
					continue;
				}
				while(receive()) { }
			}
	}
	
	public boolean setupSocket(String serverAddress, int serverPort) {
		//create socket if it doesn't exist or is closed
		if (socket == null || socket.isClosed()) {
			try {
				socket = new Socket(serverAddress, serverPort);
				writer = new PrintWriter(socket.getOutputStream(), true);
				reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			} catch (IOException e) {
System.out.println("socket setup fail");
				return false;
			}
			
			//initialize client id which can be different depending on socket 
			this.clientID = getClientID(socket);
			
			handshake();
			
			ns.setWriter(writer);
			ns.setSocketIsClosed(false);
System.out.println("socket set up");
			return true;
		}
		return false;
	}
	
	public void handshake() {
		//send a message with this clientiID to frontend so that i can keep track of the correct clients
		ClientConnectionRequest clientConnectionRequestMessage = new ClientConnectionRequest(clientID);
		String message = clientConnectionRequestMessage.serializeAsString();
		writer.println(message);
		System.out.println("clientID: " + clientID);
		//send a message via the networksend requesting all perviously drawn objects
		RetrieveObjectsRequest retrieveObjectsRequest = new RetrieveObjectsRequest(); 
		retrieveObjectsRequest.setClientID(clientID);
		ns.addMessageToSendFirst(retrieveObjectsRequest);
System.out.println("handshaked");
	}
	
	public boolean receive() {
		
		String message = "";
		try {
			message = reader.readLine();
		} catch (IOException e) {
			// frontend has crashed 
			try {
System.out.println("crashed: " + message);
				ns.setSocketIsClosed(true);
				socket.close();
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
System.out.println("received " + uniqueMessage);
		//when a client has drawn an object
		if (uniqueMessage instanceof DrawObjectRequest) {
			
			DrawObjectRequest drawObjectMessage = (DrawObjectRequest) uniqueMessage;
			
			localAddGObject(drawObjectMessage.getObject());
		}
		else if (uniqueMessage instanceof DeleteObjectRequest) {			
			DeleteObjectRequest deleteObjectRequest = (DeleteObjectRequest) uniqueMessage;
			
			localRemoveGObject(deleteObjectRequest.getObjectID());
		}
		else if(uniqueMessage instanceof ClientResponseMessage) { 
			ClientResponseMessage clientResponseMessage = (ClientResponseMessage) uniqueMessage;
			
			localAddGObjectList(clientResponseMessage.getObjectList());
			
			//update the response message ns will read 
			ns.addMessageConfirmed(clientResponseMessage);
			// notify the "send" thread that it can continue the sending process
			ns.notifySend();
		}
		else {
			//message not recognized
		}
		return true;
	}

	public void localAddGObjectList(List<GObject> list) {
System.out.print("loc:addlist ");
		//if the list is empty; do nothing, otherwise add all objects to the list
		if (list != null) {
System.out.println(" size:" + list.size());
			for (GObject go : list) {
				localAddGObject(go);
			}
		}
	}

	public void localAddGObject(GObject object) { 
System.out.println("loc:addobj");
		for (GObject go : this) {
			if (go.getID() == object.getID()) {
System.out.println("object exists already");
				// if object already exists change Active to the newest version of the object
				if (go.isActive()) {
System.out.println("object active changed from: " + go.isActive() + " to: " + object.isActive());
					go.setActive(object.isActive());
				}
				return;
			}
		}
		// if object is new then add
		synchronized (objectList) {
			objectList.add(object);
			System.out.println("object added, active? " + object.isActive() + " list: " + objectList.size());
		}
		gui.repaint();

	}
	
	public void localRemoveGObject(Long objectID) { 
System.out.println("loc:removeobj");
		// find the object if it already exists and remove it by changing Active
		for (GObject go : this) {
			if (go.getID() == objectID) {
				go.setActive(false);
			}
		}
		gui.repaint();
	}

	@Override
	public void addGObject(GObject object) {
		//add the object locally
		synchronized (objectList) {
			objectList.add(object);
		}
		
		//add a message with the object that should be added to the queue of messages to send
		DrawObjectRequest drawObjectMessage = new DrawObjectRequest(object);
		drawObjectMessage.setClientID(clientID);
		ns.addMessageToSend(drawObjectMessage);
System.out.println("addobj");
	}

	@Override
	public void removeLastGObject() {
				
		DeleteObjectRequest deleteObjectRequest = null;
		
		// if there are objects in the list then remove the last one by changeing the Active variable
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
		
		//add a message containing the object's ID of the object that should be deleted
		if (deleteObjectRequest != null) {
			deleteObjectRequest.setClientID(clientID);
			ns.addMessageToSend(deleteObjectRequest);
		}
System.out.println("removeobj");
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
