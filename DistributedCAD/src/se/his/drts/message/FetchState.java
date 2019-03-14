package se.his.drts.message;

import java.util.LinkedList;
import java.util.UUID;

import DCAD.GObject;

public class FetchState extends UniqueMessage {
	private static UUID uuid = UUID.fromString("979fde02-de91-4fe1-bd21-4a6253758218");
	
	private LinkedList<GObject> gObjectList = new LinkedList<>();
		
	protected FetchState() {
		super(FetchState.uuid);
	}
	
	public FetchState(String ID) {
		super(FetchState.uuid);
	}
	public LinkedList<GObject> getGObjectList() {
		return gObjectList;
	}
	public void setGObjectList(LinkedList<GObject> gObjectList) {
		this.gObjectList = gObjectList;
	}
}
