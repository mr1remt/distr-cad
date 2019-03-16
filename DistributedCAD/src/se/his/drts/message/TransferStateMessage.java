package se.his.drts.message;

import java.util.UUID;

import replicaManager.CADState;

public class TransferStateMessage extends MessagePayload {

	private static UUID subclassUUID = UUID.fromString("3851a037-b15a-4ddb-9e82-e3ce5eedfcd7");
	
	private CADState state;

	public TransferStateMessage() {
		super(subclassUUID);
	}

	public TransferStateMessage(CADState state) {
		super(subclassUUID);
		
		this.state = state;
	}
	
	public final CADState getState() {
		return state;
	}

}
