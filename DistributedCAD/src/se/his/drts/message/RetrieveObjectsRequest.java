package se.his.drts.message;

import java.util.UUID;

public class RetrieveObjectsRequest extends UniqueMessage {
	private static UUID subclassUUID = UUID.fromString("cec9ee49-bfac-420a-bf9d-e31554220c31");
	
	protected RetrieveObjectsRequest() {
		super(RetrieveObjectsRequest.subclassUUID);
	}
}
