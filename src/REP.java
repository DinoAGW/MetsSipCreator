import java.util.Stack;

public class REP {
	String preservationType;
	Stack<FILE> files = new Stack<>();
	private int lastFileId = 0;
	SIP sip;
	
	/*
	 * input: Preservation Type preservationType
	 * For example
	 * * PRESERVATION_MASTER
	 * * PRE_INGEST_MODIFIED_MASTER
	 * * MODIFIED_MASTER
	 * * selfmade preservationtypes
	 */
	REP(String preservationType, SIP sip) {
		if (preservationType==null) {
			this.preservationType = "PRESERVATION_MASTER";
		} else {
			this.preservationType = preservationType;
		}
		
		this.sip = sip;
	}
	
	public FILE addFile(String dateipfad, String zielPfadInnerhalbSip) throws Exception {
		FILE file = new FILE(dateipfad, zielPfadInnerhalbSip, lastFileId++, this);//geht sicher, dass die Datei auch wirklich existiert
		files.push(file);
		return file;
	}

	void moveToTarget(String zielVerzeichnis) throws Exception {
		for (FILE file: files) {
			try {
				file.moveToTarget(zielVerzeichnis);
			} catch (Exception e) {
				System.err.println("Fehler beim platzieren der Datei");
				throw e;
			}
		}
	}
}
