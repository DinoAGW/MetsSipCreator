package metsSipCreator;
import java.util.Stack;

public class REP {
	String preservationType;
	Stack<FILE> files = new Stack<>();
	SIP sip;
	private boolean moveMode;
	
	/*
	 * input: Preservation Type preservationType
	 * For example
	 * * PRESERVATION_MASTER
	 * * PRE_INGEST_MODIFIED_MASTER
	 * * MODIFIED_MASTER
	 * * selfmade preservationtypes
	 */
	REP(String preservationType, boolean moveMode, SIP sip) {
		if (preservationType==null) {
			this.preservationType = "PRESERVATION_MASTER";
		} else {
			this.preservationType = preservationType;
		}
		this.moveMode = moveMode;
		this.sip = sip;
	}
	
	public void setMoveMode(boolean moveStattCopy) {
		this.moveMode = moveStattCopy;
	}
	
	public FILE addFile(String dateipfad, String zielPfadInnerhalbSip) throws Exception {
		FILE file = new FILE(dateipfad, zielPfadInnerhalbSip, this.moveMode, this);//geht sicher, dass die Datei auch wirklich existiert
		files.push(file);
		return file;
	}

	void moveToTarget(String zielVerzeichnis) throws Exception {
		for (FILE file: files) {
			try {
				file.placeToTarget(zielVerzeichnis);
			} catch (Exception e) {
				System.err.println("Fehler beim platzieren der Datei");
				throw e;
			}
		}
	}
	
	boolean validate() {
		if (this.files.empty()) {
			System.err.println("Repräsentation hat keine Dateien: " + this.preservationType);
			return false;
		}
		for (FILE file : files) {
			if (!file.validate()) {
				System.err.println("Datei nicht valide");
				return false;
			}
		}
		return true;
	}
}
