import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class FILE {
	private static final String fs = System.getProperty("file.separator");
	private int fileId;
	private String zielPfadInnerhalbSip; //FileOriginalPath, also Pfad nach "/content/streams/"
	REP rep;

	FILE(String dateipfad, String zielPfadInnerhalbSip, int fileId, REP rep) throws Exception {
		moveToWorkspace(dateipfad);
		if (zielPfadInnerhalbSip==null) {
			this.zielPfadInnerhalbSip = "";
		} else {
			if (zielPfadInnerhalbSip.length()>0 && zielPfadInnerhalbSip.charAt(zielPfadInnerhalbSip.length()-1)!=fs.charAt(0)) {
				System.err.println("Pfad muss leer sein oder mit " + fs + " enden");
				throw new Exception();
			}
			this.zielPfadInnerhalbSip = zielPfadInnerhalbSip;
		}
		this.fileId = fileId;
		this.rep = rep;
	}
	
	private void moveToWorkspace(String dateipfad) throws Exception {
		File file = new File(dateipfad);
		if(!file.exists()) {
			System.err.println("Datei existiert nicht");
			throw new Exception();
		}
		Files.copy(file.toPath(), Paths.get(Drive.getFileOrt(rep.sip.arbeitsverzeichnis, fileId)));
	}
	
	void moveToTarget(String zielVerzeichnis) throws Exception {
		String from = Drive.getFileOrt(rep.sip.arbeitsverzeichnis, fileId);
		File file = new File(from);
		if (!file.exists()) {
			System.err.println("Datei ist verschwunden von " + from);
			throw new Exception();
		}
		
		Files.move(file.toPath(), Paths.get(zielVerzeichnis.concat(fs).concat(zielPfadInnerhalbSip)), StandardCopyOption.REPLACE_EXISTING);
	}
}
