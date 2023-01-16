package metsSipCreator;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import utilities.Drive;

public class FILE {
	private static final String fs = System.getProperty("file.separator");
	private String zielPfadInnerhalbSip; // FileOriginalPath, also Pfad nach "/content/streams/"
	private String pfadDerDatei;
	REP rep;
	private boolean moveMode;

	FILE(String dateipfad, String zielPfadInnerhalbSip, boolean moveMode, REP rep) throws Exception {
		File file = new File(dateipfad);
		if (!file.exists()) {
			System.err.println("Datei existiert nicht");
			throw new Exception();
		}
		if (rep.sip.arbeitsverzeichnis != null) {
			pfadDerDatei = Drive.getFileOrt(rep.sip.arbeitsverzeichnis, rep.sip.lastFileId++);
			Files.copy(file.toPath(), Paths.get(pfadDerDatei));
		} else {
			pfadDerDatei = dateipfad;
		}
		if (zielPfadInnerhalbSip == null) {
			this.zielPfadInnerhalbSip = "";
		} else {
			if (zielPfadInnerhalbSip.length() > 0
					&& zielPfadInnerhalbSip.charAt(zielPfadInnerhalbSip.length() - 1) != fs.charAt(0)) {
				System.err.println("Pfad muss leer sein oder mit " + fs + " enden");
				throw new Exception();
			}
			this.zielPfadInnerhalbSip = zielPfadInnerhalbSip;
		}
		this.moveMode = moveMode;
		this.rep = rep;
	}
	
	public void setMoveMode(boolean moveStattCopy) {
		this.moveMode = moveStattCopy;
	}

	void placeToTarget(String zielVerzeichnis) throws Exception {
		File file = new File(pfadDerDatei);
		if (!file.exists()) {
			System.err.println("Datei ist verschwunden von " + pfadDerDatei);
			throw new Exception();
		}

		if (moveMode) {
			Files.move(file.toPath(), Paths.get(zielVerzeichnis.concat(fs).concat(zielPfadInnerhalbSip)),
					StandardCopyOption.REPLACE_EXISTING);
		} else {
			Files.copy(file.toPath(), Paths.get(zielVerzeichnis.concat(fs).concat(zielPfadInnerhalbSip)),
					StandardCopyOption.REPLACE_EXISTING);
		}

	}

	boolean validate() {
		File file = new File(pfadDerDatei);
		if (!file.exists()) {
			System.err.println("Datei ist verschwunden von " + pfadDerDatei);
			return false;
		}
		return true;
	}
}
