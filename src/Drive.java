import java.io.File;

public class Drive {
	public static final String fs = System.getProperty("file.separator");
	public static final String home = System.getProperty("user.home");
	public static final String workspace = home.concat(fs).concat("MetsSipCreator");
	public static final String sipsVerzeichnis = workspace.concat(fs).concat("SIPs");
	
	public static final String getArbeitsverzeichnis() throws Exception {
		//TODO: hänge individuelles Verzeichnis an
		String verzeichnis = null;
		for (int i = 1; i < Integer.MAX_VALUE; ++i) {
			File test = new File("sip".concat(Integer.toString(i)));
			if (!test.exists()) {
				verzeichnis = "sip".concat(Integer.toString(i));
				break;
			}
			if (i>Integer.MAX_VALUE-100) {
				System.err.println("Warnung: bald erreicht SIP-Nummer MaxInt");
			}
		}
		if (verzeichnis == null) {
			System.err.println("Es konnte kein Verzeichnis vorgeschlagen werden.");
			throw new Exception();
		}
		return getArbeitsverzeichnis(verzeichnis);
	}
	
	public static final String getArbeitsverzeichnis(String verzeichnis) {
		return sipsVerzeichnis.concat(fs).concat(verzeichnis);
	}
	
	public static final String getFileOrt(String arbeitsverzeichnis, int fileId) {
		return arbeitsverzeichnis.concat(fs).concat("File").concat(Integer.toString(fileId));
	}
}
