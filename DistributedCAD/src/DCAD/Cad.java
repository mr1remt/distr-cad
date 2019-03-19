package DCAD;

public class Cad {
	static private GUI gui;

	private CadDocument cd;
	
	public static void main(String[] args) {
		int port = 55000;
		if (args.length == 1) port = Integer.parseInt(args[0]);
		System.out.println("Client starting on port: " + port);
		
		Cad cad = new Cad(port);
		gui = new GUI(cad.cd, 750, 600);
		gui.addToListener();
		cad.cd.setGui(gui);
	}

	private Cad(int port) {
		cd = new NetworkDocument("127.0.0.1", port);
		new Thread(cd).start();

	}
}
