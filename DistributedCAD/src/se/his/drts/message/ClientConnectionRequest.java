package se.his.drts.message;

import java.util.UUID;

public class ClientConnectionRequest extends UniqueMessage {

	private static UUID uuid = UUID.fromString("979fde02-de91-4fe1-bd21-4a6253758218");
	
	protected ClientConnectionRequest() {
		super(ClientConnectionRequest.uuid);
	}
	
}
