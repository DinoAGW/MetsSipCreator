package metsSipCreator;

import java.io.File;
import java.util.Iterator;
import java.util.Stack;

import org.apache.xmlbeans.XmlOptions;

import com.exlibris.core.sdk.formatting.DublinCore;
import com.exlibris.core.sdk.utils.FileUtil;
import com.exlibris.digitool.common.dnx.DnxDocument;
import com.exlibris.digitool.common.dnx.DnxDocumentHelper;
import com.exlibris.digitool.common.dnx.DnxDocumentHelper.CMS;
import com.exlibris.digitool.common.dnx.DnxDocumentHelper.GeneralIECharacteristics;
import com.exlibris.dps.sdk.deposit.IEParser;
import com.exlibris.dps.sdk.deposit.IEParserFactory;

import gov.loc.mets.MetsDocument;

import com.exlibris.core.sdk.consts.Enum;

public class SIP {
	private static final String fs = System.getProperty("file.separator");
	Stack<REP> reps = new Stack<>();
	Stack<String> metadataXPathKey = new Stack<>();
	Stack<String> metadataValue = new Stack<>();
	IEParser ie;
	private boolean userDefinedSet = false;
	private String userDefinedA = null;
	private String userDefinedB = null;
	private String userDefinedC = null;
	private String cmsSystem = null;
	private String cmsRecordId = null;

	private static final String ROSETTA_METS_SCHEMA = "http://www.exlibrisgroup.com/xsd/dps/rosettaMets";
	private static final String METS_SCHEMA = "http://www.loc.gov/METS/";
	private static final String XML_SCHEMA = "http://www.w3.org/2001/XMLSchema-instance";
	private static final String XML_SCHEMA_REPLACEMENT = "http://www.exlibrisgroup.com/XMLSchema-instance";

	public SIP() throws Exception {

	}

	public void setCMS(String system, String recordId) throws Exception {
		if ((system == null) || (system.length() == 0)) {
			System.err.println("system muss definiert sein");
			throw new Exception();
		}
		if ((recordId == null) || (recordId.length() == 0)) {
			System.err.println("recordId muss definiert sein");
			throw new Exception();
		}
		this.cmsSystem = system;
		this.cmsRecordId = recordId;
	}

	public void setUserDefined(String ABorC, String value) throws Exception {
		switch (ABorC) {
		case "A":
			this.userDefinedA = value;
			break;
		case "B":
			this.userDefinedB = value;
			break;
		case "C":
			this.userDefinedC = value;
			break;
		default:
			System.err.println("ABorC sollte A, B oder C sein, ist aber = '" + ABorC + "'");
			throw new Exception();
		}
		userDefinedSet = true;
	}

	public REP newREP(String preservationType) {
		REP rep = new REP(preservationType, this);
		reps.push(rep);
		return rep;
	}

	public boolean validate() throws Exception {
		if (reps.empty()) {
			System.err.println("SIP hat keine Repräsentation");
			return false;
		}

		boolean hasPreservationMaster = false;
		for (REP rep : reps) {
			if (rep.preservationType.contentEquals("PRESERVATION_MASTER")) {
				hasPreservationMaster = true;
			}
			if (!rep.validate()) {
				System.err.println("Repräsentation invalide");
				return false;
			}
		}
		if (!hasPreservationMaster) {
			System.err.println("Es gibt keinen 'PRESERVATION_MASTER'");
			return false;
		}

		if (metadataXPathKey.size() != metadataValue.size()) {
			System.err.println("Irgendwas ist schief gelaufen. metadataXPathKey.size()=" + metadataXPathKey.size()
					+ ", aber metadataValue.size()=" + metadataValue.size() + ". Sollte eigentlich gleich lang sein");
			throw new Exception();
		}

		if (metadataValue.empty()) {
			System.err.println("Es gibt keine Metadaten");
			return false;
		}

		return true;
	}

	public void addMetadata(String xPathKey, String value) {
		if (value == null) {
			value = "";
		}
		metadataXPathKey.push(xPathKey);
		metadataValue.push(value);
	}

	public void deploy(String ziel) throws Exception {
		File zielFile = new File(ziel);
		if (zielFile.exists()) {
			System.err.println("Ziel schon belegt");
			throw new Exception();
		}
		if (ziel.charAt(ziel.length() - 1) != fs.charAt(0)) {
			ziel = ziel.concat(fs);
		}

		if (!validate()) {
			System.err.println("SIP kann nicht ausgegeben werden. Etwas stimmt nicht");
			return;
		}

		// Dateien an Zielort verschieben
		for (REP rep : reps) {
			try {
				rep.placeToTarget(ziel);
			} catch (Exception e) {
				System.err.println("SIP-Auslieferung nicht erfolgreich: Es konnte eine Datei nicht platziert werden.");
				System.err.println("SIP hat damit einen inkonsistenten Zustand und ist nun unbrauchbar.");
				System.err.println("Ich weiß nicht wie das passieren konnte.");
				System.err.println("Es tut mir Leid =(");
				throw e;
			}
		}

		// Erstelle mets
		this.ie = IEParserFactory.create();
		DublinCore dc = this.ie.getDublinCoreParser();

		// Füge Metadaten hinzu
		Iterator<String> xPathKey = metadataXPathKey.iterator();
		Iterator<String> value = metadataValue.iterator();
		while (xPathKey.hasNext()) {
			dc.addElement(xPathKey.next(), value.next());
		}
		ie.setIEDublinCore(dc);

		// Füge Repräsentationen hinzu
		for (REP rep : reps) {
			rep.deploy();
		}

		String filesRootFolder = ziel.concat("content").concat(fs).concat("streams").concat(fs);
		ie.generateChecksum(filesRootFolder, Enum.FixityType.MD5.toString());
		for (REP rep : reps) {
			rep.checkMd5sums();
		}

		this.ie.updateSize(filesRootFolder);

		if (this.userDefinedSet) {
			DnxDocument ieDnx = this.ie.getDnxParser();
			DnxDocumentHelper ieDnxHelper = new DnxDocumentHelper(ieDnx);
			GeneralIECharacteristics generalIeCharacteristics = ieDnxHelper.new GeneralIECharacteristics(null, null,
					null, null, null, this.userDefinedA, this.userDefinedB, this.userDefinedC);
			ieDnxHelper.setGeneralIECharacteristics(generalIeCharacteristics);
			ie.setIeDnx(ieDnxHelper.getDocument());
		}

		if (this.cmsSystem != null) {
			DnxDocument ieDnx = this.ie.getDnxParser();
			DnxDocumentHelper ieDnxHelper = new DnxDocumentHelper(ieDnx);
			CMS cms = ieDnxHelper.getCMS();
			if (cms == null) {
				cms = ieDnxHelper.new CMS();
			}
			cms.setSystem(this.cmsSystem);
			cms.setRecordId(this.cmsRecordId);
			ieDnxHelper.setCMS(cms);
			ie.setIeDnx(ieDnxHelper.getDocument());
		}

		// example for adding a logical Struct Map.
		MetsDocument metsDoc = MetsDocument.Factory.parse(this.ie.toXML());

		// insert IE created in content directory
		String mets = ziel.concat("content").concat(fs).concat("mets.xml");

		File ieXML = new File(mets);
		XmlOptions opt = new XmlOptions();
		opt.setSavePrettyPrint();
		String xmlMetsContent = metsDoc.xmlText(opt);

		// Need to replace manually the namespace with Rosetta Mets schema in order to
		// pass validation against mets_rosetta.xsd
		String xmlRosettaMetsContent = xmlMetsContent.replaceAll(XML_SCHEMA, XML_SCHEMA_REPLACEMENT);
		xmlRosettaMetsContent = xmlMetsContent.replaceAll(METS_SCHEMA, ROSETTA_METS_SCHEMA);
		FileUtil.writeFile(ieXML, xmlRosettaMetsContent);

	}
}