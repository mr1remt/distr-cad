package se.his.drts.message;

import java.util.UUID;

import DCAD.GObject;

public class DrawObjectRequest extends UniqueMessage {
	private static UUID subclassUUID = UUID.fromString("8399e3d0-8a41-40e1-bfe9-20a1ac91773e");
	
	private GObject object;
	
	protected DrawObjectRequest() {
		super(DrawObjectRequest.subclassUUID);
	}
	
	public DrawObjectRequest(GObject obj) {
		super(DrawObjectRequest.subclassUUID);
		this.object = obj;
	}
	
	public GObject getObject() {
		return object;
	}
	
	public void setObjectID(GObject object) {
		this.object = object;
	}
	
	@Override
	public String toString() {
		return "DrawObjectRequest[object: " + object + "]";
	}
	
}
