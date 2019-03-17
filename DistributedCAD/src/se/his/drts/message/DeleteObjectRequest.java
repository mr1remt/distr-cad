package se.his.drts.message;

import java.util.UUID;

public class DeleteObjectRequest extends UniqueMessage {
	private static UUID subclassUUID = UUID.fromString("e43d5de8-0186-420b-be41-d6ca3ada833c");
	
	private long objectID;
	
	protected DeleteObjectRequest() {
		super(DeleteObjectRequest.subclassUUID);
	}
	
	public DeleteObjectRequest(long objectID) {
		super(DeleteObjectRequest.subclassUUID);
		this.objectID = objectID;
	}
	
	public long getGObjectID() {
		return objectID;
	}
	
	@Override
	public String toString() {
		return "DeleteObjectRequest";
	}
}
