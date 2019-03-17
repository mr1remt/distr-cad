package se.his.drts.message;

import java.util.List;
import java.util.UUID;

import DCAD.GObject;

public class ClientResponseMessage extends UniqueMessage {
	private static UUID subclassUUID = UUID.fromString("3906e0a3-683c-4e84-9ec2-9f882810c80c");
	
	/**
	 * Whether or not the clients request was successfully executed
	 */
	private boolean operationSuccess;
	
	/**
	 * List of active objects in CAD document. Used to transfer state to clients
	 */
	private List<GObject> objectList;
	
	public ClientResponseMessage() {
		super(subclassUUID);
	}

	public ClientResponseMessage(UniqueMessage reqMsg, boolean success) {
		super(subclassUUID, reqMsg);
		setOperationSuccess(success);
		setObjectList(null);
	}

	public ClientResponseMessage(ClientResponseMessage crm) {
		super(subclassUUID, crm);
		setOperationSuccess(crm.operationSuccess);
		setObjectList(crm.objectList);
	}

	public boolean getOperationSuccess() {
		return operationSuccess;
	}

	public void setOperationSuccess(boolean operationSuccess) {
		this.operationSuccess = operationSuccess;
	}

	public List<GObject> getObjectList() {
		return objectList;
	}

	public void setObjectList(List<GObject> gObjectList) {
		this.objectList = gObjectList;
	}
	
	@Override
	public String toString() {
		return "ClientResponseMessage[operationSuccess: " + operationSuccess + "]";
	}

}
