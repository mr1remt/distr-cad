package replicaManager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import DCAD.GObject;
import se.his.drts.message.ClientResponseMessage;
import se.his.drts.message.UniqueMessage;

public class CADState implements Serializable {
	
	private static final long serialVersionUID = -2061155938469786198L;

	private ArrayList<GObject> gObjects;
	private HashMap<Long, byte[]> cachedMessages;

	public CADState() {
		setGObjects(new ArrayList<GObject>());
		setCachedMessages(new HashMap<Long, byte[]>());
	}
	
	public CADState(CADState copy) {
		gObjects = new ArrayList<GObject>();
		cachedMessages = new HashMap<Long, byte[]>();
		
		for (GObject o : copy.gObjects)
			gObjects.add(o);
		
		copy.cachedMessages.forEach((id, msg) -> cachedMessages.put(id, msg));
	}
	
	/**
	 * Checks if msg has a cached response
	 * @param msg
	 * @return The previous response or null if there is no cached response
	 */
	public byte[] hasResponse(UniqueMessage msg) {
		return cachedMessages.get(msg.getInstanceID());
	}
	
	public void addGObject(GObject o) {
		gObjects.add(o);
	}
	
	/**
	 * Removes the specified object by marking it as unactive
	 * @param uuid
	 * @return
	 */
	public boolean removeGObject(long id) {
		for (GObject o : gObjects) {
			if (o.getID() == id) {
				o.setActive(false);
				return true;
			}
		}
		return false;
	}
	
	public void cacheResponse(ClientResponseMessage crm) {
		cachedMessages.put(crm.getInstanceID(), crm.serialize());
	}
	
	public List<GObject> getActiveGObjects() {
		return gObjects.stream()
			.filter(o -> o.isActive())
			.collect(Collectors.toList());
	}

	public ArrayList<GObject> getGObjects() {
		return gObjects;
	}

	public void setGObjects(ArrayList<GObject> gObjects) {
		this.gObjects = gObjects;
	}

	public HashMap<Long, byte[]> getCachedMessages() {
		return cachedMessages;
	}

	public void setCachedMessages(HashMap<Long, byte[]> hashMap) {
		this.cachedMessages = hashMap;
	}

}
