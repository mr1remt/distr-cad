package DCAD;

public class Cad {
	static private GUI gui;

	private CadDocument cd;
	
	public static void main(String[] args) {
		String ip = "127.0.0.1";
		if (args.length >= 1) ip = args[0];
		int port = 55000;
		if (args.length >= 2) port = Integer.parseInt(args[1]);
		System.out.println("Client connecting to ip: " + ip + ", port: " + port);
		
		Cad cad = new Cad(ip, port);
		gui = new GUI(cad.cd, 750, 600);
		gui.addToListener();
		cad.cd.setGui(gui);
	}

	private Cad(String ip, int port) {
		cd = new NetworkDocument(ip, port);
		new Thread(cd).start();

	}
}
