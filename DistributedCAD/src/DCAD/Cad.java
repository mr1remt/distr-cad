package DCAD;

public class Cad {
	static private GUI gui = new GUI(750, 600);

	private ConnectionManager cm;
	private CadDocument cd;
	
	public static void main(String[] args) {
		gui.addToListener();
		new Cad();
	}

	private Cad() {
		gui.addCad(this);
		cm = new ConnectionManager(this, "127.0.0.1", 50000);
	}
	
	public void addObject(GObject object) {
		// TODO when a new object is drawn send the object to the connectionManager
		cm.sendObject(object);
	}
	public void addObjectToGUI(GObject object) {
		gui.addObject(object);
		
	}
}
