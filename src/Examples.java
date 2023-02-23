import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;

import gov.loc.mets.MdSecType.MdWrap.MDTYPE;
import metsSipCreator.*;

public class Examples {
	private static final String fs = System.getProperty("file.separator");
	
	private static String testDatei = "bin" + fs + "Test.txt";
	private static String sourceMD = "bin" + fs + "SourceMD.xml";
	
	private static XmlObject createSourceMd() throws XmlException, IOException {
		return XmlObject.Factory.parse(new File(sourceMD));
	}
	
	public static void deleteExamples() throws Exception {
		File sip = new File("bin" + fs + "minimalSip");
		if (sip.exists()) {
			FileUtils.deleteDirectory(sip);
		}
		
		sip = new File("bin" + fs + "maximalSip");
		if (sip.exists()) {
			FileUtils.deleteDirectory(sip);
		}
	}
	
	public static void createExamples() throws Exception {
		SIP sip = new SIP();
		sip.addMetadata("dc:title", "Titel");
		REP rep1 = sip.newREP(null); //Erstelle PRESERVATION_MASTER
		rep1.newFile(testDatei, null, null);
		sip.deploy("bin" + fs + "minimalSip");
		
		sip.setUserDefined("A", "Ich bins");
		sip.setCMS("cmsSystem", "cmsRecordId");
		sip.setSourceMD(MDTYPE.DC, createSourceMd(), null);
		sip.setARPolicy("433120", "ZB MED_STAFF only");
		rep1.newFile(testDatei, "1".concat(fs), null).setARPolicy("433120", "ZB MED_STAFF only");
		REP rep2 = sip.newREP("MODIFIED_MASTER");
		rep2.newFile(testDatei, "2".concat(fs).concat("Test2.txt"), null).setMd5sum("dfcd625d3138ed3d84e077161d579617").setLabel("Eine Datei");
		rep2.newFile(testDatei, "Test3.txt", null).addMetadata("dc:title", "DateiTitel");
		sip.deploy("bin" + fs + "maximalSip");
	}
	
	public static void main(String[] args) throws Exception {
		deleteExamples();
		createExamples();
		System.out.println("Example Ende");
	}
}
