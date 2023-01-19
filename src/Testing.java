import java.io.File;

import org.apache.commons.io.FileUtils;

import metsSipCreator.*;
import utilities.PropertiesManager;

public class Testing {
	private static final String fs = System.getProperty("file.separator");
	
	private static String testDatei = "bin" + fs + "Test.txt";
	
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
		rep1.newFile(testDatei, "1".concat(fs), null);
		REP rep2 = sip.newREP("MODIFIED_MASTER");
		FILE file = rep2.newFile(testDatei, "2".concat(fs).concat("Test2.txt"), null);
		file.setMd5sum("dfcd625d3138ed3d84e077161d579617");
		file.setLabel("Eine Datei");
		rep2.newFile(testDatei, "Test3.txt", null);
		sip.deploy("bin" + fs + "maximalSip");
	}
	
	public static void main(String[] args) throws Exception {
		deleteExamples();
		createExamples();
		System.out.println("Example Ende");
	}
}
