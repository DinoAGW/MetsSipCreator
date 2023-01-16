import metsSipCreator.SIP;

public class Testing {
	
	public static void createSimpleSip() throws Exception {
		SIP sip = new SIP();
		sip.addMetadata("dc:title", "Titel");
	}
	
	public static void createSipMitArbeitsverzeichnis() throws Exception {
		SIP sip = new SIP();
		sip.useArbeitsverzeichnis(null);
		sip.setMoveMode(true);
		sip.addMetadata("dc:title", "Titel");
	}
	
	public static void deleteExamples() {
		//TODO: wenn etwas ausgeliefert wird hier löschen
	}

	public static void main(String[] args) throws Exception {
		createSimpleSip();
		System.out.println("Example Ende");
	}
}
