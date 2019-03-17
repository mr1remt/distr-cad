package DCAD;

public class Cad {
	static private GUI gui;

	private CadDocument cd;
	
	public static void main(String[] args) {
		Cad cad = new Cad();
		gui = new GUI(cad.cd, 750, 600);
		gui.addToListener();
	}

	private Cad() {
		cd = new NetworkDocument("127.0.0.1", 50000);
		new Thread(cd).start();

	}
}
