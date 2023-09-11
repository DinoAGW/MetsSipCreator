package metsSipCreator;

import java.util.HashMap;
import java.util.Stack;

import com.exlibris.core.sdk.consts.Enum;
import com.exlibris.digitool.common.dnx.DnxDocument;
import com.exlibris.digitool.common.dnx.DnxDocumentHelper;
import com.exlibris.digitool.common.dnx.DnxDocumentHelper.AccessRightsPolicy;

import gov.loc.mets.DivType;
import gov.loc.mets.FileType;
import gov.loc.mets.MetsDocument.Mets;
import gov.loc.mets.MetsType.FileSec.FileGrp;
import gov.loc.mets.StructMapType;

public class REP {
	private static final String fs = System.getProperty("file.separator");
	String preservationType;
	Stack<FILE> files = new Stack<>();
	SIP sip;
	private FileGrp fGrp;
	String label;
	private String arPolicyId = "AR_EVERYONE";
	private String arPolicyDescription = "Keine Beschränkung";

	/**
	 * input: Preservation Type preservationType For example PRESERVATION_MASTER
	 * PRE_INGEST_MODIFIED_MASTER MODIFIED_MASTER selfmade preservationtypes
	 */
	REP(String preservationType, SIP sip) {
		if (preservationType == null) {
			this.preservationType = "PRESERVATION_MASTER";
		} else {
			this.preservationType = preservationType;
		}
		this.sip = sip;
		this.label = "Rep".concat(Integer.toString(sip.reps.size() + 1)).concat(" (").concat(this.preservationType)
				.concat(")");
	}

	void addStructMap(Mets mets) throws Exception {
		StructMapType sm = mets.addNewStructMap();
		sm.setID(this.fGrp.getID() + "-1"); // je nachdem welche ID schon vergeben ist
		sm.setTYPE("LOGICAL");
		HashMap<String, DivType> divTypes = new HashMap<String, DivType>();
		DivType div1 = sm.addNewDiv();
		div1.setLABEL(this.label);
		divTypes.put("", div1);
		for (FILE file : files) {
			file.placeFolderInsideStructMap(divTypes);
		}
		for (FILE file : files) {
			file.placeFileInsideStructMap(divTypes);
		}
	}

	public REP setLabel(String label) {
		this.label = label;
		return this;
	}

	public FILE newFile(String dateipfad, String fileOriginalPath) throws Exception {
		FILE file = new FILE(dateipfad, fileOriginalPath, this);// geht sicher, dass die Datei auch wirklich
																// existiert
		files.push(file);
		return file;
	}

	public REP setARPolicy(String arPolicyId, String arPolicyDescription) {
		this.arPolicyId = arPolicyId;
		this.arPolicyDescription = arPolicyDescription;
		return this;
	}

	void placeToTarget(String zielVerzeichnis) throws Exception {
		for (FILE file : files) {
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
		AccessRightsPolicy ar = documentHelper.new AccessRightsPolicy(this.arPolicyId, null, this.arPolicyDescription);
		documentHelper.setAccessRightsPolicy(ar);
		this.sip.ie.setFileGrpDnx(documentHelper.getDocument(), this.fGrp.getID());

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
