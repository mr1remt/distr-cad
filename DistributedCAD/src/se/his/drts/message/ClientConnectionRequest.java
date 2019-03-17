package se.his.drts.message;

import java.util.UUID;

public class ClientConnectionRequest extends UniqueMessage {

	private static UUID uuid = UUID.fromString("e15150ba-fc69-4526-b7d2-ffec7d97bcd5");
	
	private String clientID;
	
	protected ClientConnectionRequest() {
		super(ClientConnectionRequest.uuid);
	}
	
	public ClientConnectionRequest(String ID) {
		super(ClientConnectionRequest.uuid);
		this.clientID = ID;
	}
	public String getClientID() {
		return clientID;
	}
	
	@Override
	public String toString() {
		return "ClientConnectionRequest";
	}
	
}
