package replicaManager;

import java.io.Serializable;
import java.util.ArrayList;

import DCAD.GObject;

public class CADState implements Serializable {
	
	private static final long serialVersionUID = -2061155938469786198L;
	
	private ArrayList<GObject> gObjects;

	public CADState() {
		setGObjects(new ArrayList<GObject>());
	}

	public ArrayList<GObject> getGObjects() {
		return gObjects;
	}

	public void setGObjects(ArrayList<GObject> gObjects) {
		this.gObjects = gObjects;
	}

}
