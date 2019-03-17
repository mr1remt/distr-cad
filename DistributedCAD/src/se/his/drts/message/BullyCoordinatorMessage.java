package se.his.drts.message;

import java.util.UUID;

public class BullyCoordinatorMessage extends UniqueMessage{
	
	private static UUID subclassUUID = UUID.fromString("6672ae2e-075b-4c25-82d9-5387c5ae684e");
	
	public BullyCoordinatorMessage() {
		super(subclassUUID);
	}
	
	@Override
	public String toString() {
		return "BullyCoordinatorMessage";
	}
	
}
