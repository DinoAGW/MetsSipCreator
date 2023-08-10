package metsSipCreator;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import com.exlibris.core.sdk.formatting.DublinCore;
import com.exlibris.digitool.common.dnx.DnxDocument;
import com.exlibris.digitool.common.dnx.DnxDocumentHelper;
import com.exlibris.digitool.common.dnx.DnxDocumentHelper.AccessRightsPolicy;
import com.exlibris.digitool.common.dnx.DnxDocumentHelper.FileFixity;

import gov.loc.mets.DivType;
import gov.loc.mets.FileType;
import gov.loc.mets.MetsType.FileSec.FileGrp;

public class FILE {
	private static final String fs = System.getProperty("file.separator");
	private String zielPfadInnerhalbSip; // FileOriginalPath, also Pfad nach "/content/streams/" vor Anfang des
											// Dateinamens
	private String fileOriginalName; // Dateiname
	private String fileOriginalPath; // zielPfadInnerhalbSip+fileOriginalName
	private String pfadDerDatei;
	REP rep;
	SIP sip;
	private boolean moveMode = false;
	private String mimeType = null;
	private String md5sum = null;
	private String fileTypeId;
	private String label;
	private Stack<String> metadataXPathKey = new Stack<>();
	private Stack<String> metadataValue = new Stack<>();
	private String arPolicyId = "AR_EVERYONE";
	private String arPolicyDescription = "Keine Beschränkung";

	FILE(String dateipfad, String fileOriginalPath, String mimeType, REP rep) throws Exception {
		this.rep = rep;
		this.sip = rep.sip;
		File file = new File(dateipfad);
		if (!file.exists()) {
			System.err.println("Datei existiert nicht");
			throw new Exception();
		}
		this.pfadDerDatei = dateipfad;
		if ((fileOriginalPath == null) || (fileOriginalPath.length() == 0)) {
			this.zielPfadInnerhalbSip = "";
			this.fileOriginalName = file.getName();
		} else if (fileOriginalPath.charAt(fileOriginalPath.length() - 1) == fs.charAt(0)) {
			this.zielPfadInnerhalbSip = fileOriginalPath;
			this.fileOriginalName = file.getName();
		} else if (fileOriginalPath.contains(fs)) {
			this.zielPfadInnerhalbSip = fileOriginalPath.substring(0, fileOriginalPath.lastIndexOf(fs) + 1);
			this.fileOriginalName = fileOriginalPath.substring(fileOriginalPath.lastIndexOf(fs) + 1);
		} else {
			this.zielPfadInnerhalbSip = "";
			this.fileOriginalName = fileOriginalPath;
		}
		this.fileOriginalPath = this.rep.label.concat(fs).concat(this.zielPfadInnerhalbSip).concat(this.fileOriginalName);

		this.label = this.fileOriginalName;
	}
	
	public FILE setMimeType(String mimeType) {
		this.mimeType = mimeType;
		return this;
	}
	
	void placeInsideStructMap(HashMap<String, DivType> divTypes) {
		int last = -1;
		int next;
		while ((next = this.zielPfadInnerhalbSip.indexOf(fs, last+1)) != -1) {
			String nextString = this.zielPfadInnerhalbSip.substring(0, next+1);
			if (!divTypes.containsKey(nextString)) {
				String stelleString = this.zielPfadInnerhalbSip.substring(0, last+1);
				DivType div1 = divTypes.get(stelleString);
				DivType div2 = div1.addNewDiv();
				div2.setLABEL(this.zielPfadInnerhalbSip.substring(last+1, next));
				divTypes.put(nextString, div2);
			}
			last = next;
		}
		DivType div = divTypes.get(this.zielPfadInnerhalbSip).addNewDiv();
		div.setLABEL(this.label);
		div.setTYPE("FILE");
		div.addNewFptr().setFILEID(this.fileTypeId);
	}
	
	public FILE setARPolicy(String arPolicyId, String arPolicyDescription) {
		this.arPolicyId = arPolicyId;
		this.arPolicyDescription = arPolicyDescription;
		return this;
	}

	public FILE setLabel(String label) {
		this.label = label;
		return this;
	}

	public FILE setMd5sum(String md5sum) {
		this.md5sum = md5sum;
		return this;
	}

	public FILE setMoveMode(boolean moveStattCopy) {
		this.moveMode = moveStattCopy;
		return this;
	}

	void placeToTarget(String zielVerzeichnis) throws Exception {
		File file = new File(this.pfadDerDatei);
		if (!file.exists()) {
			System.err.println("Datei ist verschwunden von " + this.pfadDerDatei);
			throw new Exception();
		}

		String target = zielVerzeichnis.concat(this.fileOriginalPath);
		File targetfile = new File(target);
		if (targetfile.exists()) {
			System.err
					.println("Datei konnte nicht platziert werden, da " + this.fileOriginalPath + " bereits existiert");
			throw new Exception();
		}
		targetfile.mkdirs();
		try {
			if (moveMode) {
				Files.move(file.toPath(), Paths.get(target), StandardCopyOption.REPLACE_EXISTING);
			} else {
				Files.copy(file.toPath(), Paths.get(target), StandardCopyOption.REPLACE_EXISTING);
			}
		} catch (Exception e) {
			System.err.println("Datei konnte nicht platziert werden: " + file.toPath() + " -> " + Paths.get(target));
			System.err.println("targetfile = " + targetfile);
			throw new Exception();
		}
	}

	boolean validate() throws Exception {
		File file = new File(this.pfadDerDatei);
		if (!file.exists()) {
			System.err.println("Datei ist verschwunden von " + this.pfadDerDatei);
			return false;
		}

		if (this.metadataXPathKey.size() != this.metadataValue.size()) {
			System.err.println("Irgendwas ist schief gelaufen. metadataXPathKey.size()=" + this.metadataXPathKey.size()
					+ ", aber metadataValue.size()=" + this.metadataValue.size()
					+ ". Sollte eigentlich gleich lang sein");
			throw new Exception();
		}

		return true;
	}

	public FILE addMetadata(String xPathKey, String value) {
		if (value == null) {
			value = "";
		}
		this.metadataXPathKey.push(xPathKey);
		this.metadataValue.push(value);
		return this;
	}

	void deploy(FileGrp fGrp) throws Exception {
		FileType fileType = this.sip.ie.addNewFile(fGrp, this.mimeType, this.fileOriginalPath.replace('\\', '/'),
				this.label);
		this.fileTypeId = fileType.getID();

		if (!this.metadataXPathKey.empty()) {
			// Füge Metadaten hinzu
			Iterator<String> xPathKey = this.metadataXPathKey.iterator();
			Iterator<String> value = this.metadataValue.iterator();
			DublinCore dc = this.sip.ie.getDublinCoreParser();
			while (xPathKey.hasNext()) {
				dc.addElement(xPathKey.next(), value.next());
			}
			sip.ie.setDublinCore(dc, this.fileTypeId);
		}
		
		DnxDocument fileDnx = this.sip.ie.getFileDnx(this.fileTypeId);
		DnxDocumentHelper fileDnxHelper = new DnxDocumentHelper(fileDnx);
		AccessRightsPolicy ar = fileDnxHelper.new AccessRightsPolicy(this.arPolicyId, null, this.arPolicyDescription);
		fileDnxHelper.setAccessRightsPolicy(ar);
		this.sip.ie.setFileDnx(fileDnxHelper.getDocument(), this.fileTypeId);
	}

	void checkMd5sums() throws Exception {
		if (md5sum != null) {
			// add dnx - A new DNX is constructed and added on the file level
			DnxDocument dnx = sip.ie.getFileDnx(this.fileTypeId);
			DnxDocumentHelper fileDocumentHelper = new DnxDocumentHelper(dnx);
			List<FileFixity> ffxtyList = fileDocumentHelper.getFileFixitys();
			for (FileFixity ffxty : ffxtyList) {
				if (ffxty.getFixityType().contentEquals("MD5")) {
					if (!ffxty.getFixityValue().contentEquals(this.md5sum)) {
						System.err.println("MD5-Summe stimmt nicht. Ist = '" + ffxty.getFixityValue() + "' Soll = '"
								+ this.md5sum + "'");
						throw new Exception();
					}
				}
			}
		}
	}
}
