package metsSipCreator;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

import com.exlibris.digitool.common.dnx.DnxDocument;
import com.exlibris.digitool.common.dnx.DnxDocumentHelper;
import com.exlibris.digitool.common.dnx.DnxDocumentHelper.FileFixity;

import gov.loc.mets.FileType;
import gov.loc.mets.MetsType.FileSec.FileGrp;
import utilities.Mime;

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
	private String mimeType;
	private String md5sum = null;
	private String fileTypeId;
	private String label;

	FILE(String dateipfad, String fileOriginalPath, String mimeType, REP rep) throws Exception {
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
		this.fileOriginalPath = this.zielPfadInnerhalbSip.concat(this.fileOriginalName);
		
		this.label = this.fileOriginalPath;

		if (mimeType == null) {
			this.mimeType = Mime.endung2mime(pfadDerDatei);
		} else {
			this.mimeType = mimeType;
		}
		this.rep = rep;
		this.sip = rep.sip;
	}
	
	public void setLabel(String label) {
		this.label = label;
	}

	public void setMd5sum(String md5sum) {
		this.md5sum = md5sum;
	}

	public void setMoveMode(boolean moveStattCopy) {
		this.moveMode = moveStattCopy;
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

	boolean validate() {
		File file = new File(this.pfadDerDatei);
		if (!file.exists()) {
			System.err.println("Datei ist verschwunden von " + this.pfadDerDatei);
			return false;
		}
		return true;
	}

	void deploy(FileGrp fGrp) throws Exception {
		FileType fileType = sip.ie.addNewFile(fGrp, this.mimeType, this.fileOriginalPath, this.label);
		this.fileTypeId = fileType.getID();
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
