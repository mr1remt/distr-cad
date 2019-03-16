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

	public CADState() {
		setGObjects(new ArrayList<GObject>());
	}
	
	public CADState(CADState copy) {
		gObjects = new ArrayList<GObject>();
		
		for (GObject o : copy.gObjects)
			gObjects.add(o);
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

}
