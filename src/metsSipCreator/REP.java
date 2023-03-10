package metsSipCreator;
import java.util.Stack;

import com.exlibris.core.sdk.consts.Enum;
import com.exlibris.digitool.common.dnx.DnxDocument;
import com.exlibris.digitool.common.dnx.DnxDocumentHelper;

import gov.loc.mets.MetsType.FileSec.FileGrp;

public class REP {
	private static final String fs = System.getProperty("file.separator");
	String preservationType;
	Stack<FILE> files = new Stack<>();
	SIP sip;
	private FileGrp fGrp;
	private String label;
	
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
		this.label = "Repräsentation".concat(Integer.toString(sip.reps.size())).concat(" (").concat(this.preservationType).concat(")");
	}
	
	public REP setLabel(String label) {
		this.label = label;
		return this;
	}
	
	public FILE newFile(String dateipfad, String fileOriginalPath, String mimeType) throws Exception {
		FILE file = new FILE(dateipfad, fileOriginalPath, mimeType, this);//geht sicher, dass die Datei auch wirklich existiert
		files.push(file);
		return file;
	}

	void placeToTarget(String zielVerzeichnis) throws Exception {
		for (FILE file: files) {
			try {
				file.placeToTarget(zielVerzeichnis.concat("content").concat(fs).concat("streams").concat(fs));
			} catch (Exception e) {
				System.err.println("Fehler beim platzieren der Datei");
				throw e;
			}
		}
	}
	
	boolean validate() throws Exception {
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
	
	void deploy() throws Exception {
		this.fGrp = sip.ie.addNewFileGrp(Enum.UsageType.VIEW, preservationType);
		DnxDocument dnxDocument = sip.ie.getFileGrpDnx(this.fGrp.getID());
		DnxDocumentHelper documentHelper = new DnxDocumentHelper(dnxDocument);
		documentHelper.getGeneralRepCharacteristics().setRevisionNumber("1");
		documentHelper.getGeneralRepCharacteristics().setLabel(this.label);
		sip.ie.setFileGrpDnx(documentHelper.getDocument(), fGrp.getID());
		
		for (FILE file : this.files) {
			file.deploy(this.fGrp);
		}
	}
	
	void checkMd5sums() throws Exception {
		for (FILE file : this.files) {
			file.checkMd5sums();
		}
	}
}
