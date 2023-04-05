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
	private static String sourceMDQuelle = "bin" + fs + "SourceMD.xml";
	private static XmlObject sourceMD = createSourceMd();
	
	private static XmlObject createSourceMd() {
		try {
			return XmlObject.Factory.parse(new File(sourceMDQuelle));
		} catch (Exception e) {
			System.err.println("Fehler beim parsen der SourceMD.");
			return null;
		}
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
		SIP sip = new SIP(); //Erstelle neue SIP
		sip.addMetadata("dc:title", "Titel"); //Füge ein Metadatum hinzu
		REP rep1 = sip.newREP(null); //Erstelle Repräsentation. null bedeutet PRESERVATION_MASTER
		rep1.newFile(testDatei, null, null); //Füge eine Datei hinzu mit null bedeutet in Hauptverzeichnis der SIP und null bedeutet erkenne mimeType selbst
		sip.deploy("bin" + fs + "minimalSip"); //Prüfe ob SIP vollständig ist und liefere sie an die Stelle aus
		
		sip.setUserDefined("A", "Ich bins"); //Füge UserDefinedA hinzu
		sip.setCMS("HBZ01", "HT020566828"); //Füge CMS hinzu
		sip.setSourceMD(MDTYPE.DC, sourceMD, null); //Füge SourceMD vom Type DC hinzu. Bei Type OTHER wird das dritte Argument benötigt um den Type zu spezifizieren.
		sip.setARPolicy("1349113", "ZB MED_STAFF only"); //Setze ARPolicy auf SIP-Ebene
		rep1.newFile(testDatei, "1".concat(fs), null).setARPolicy("1349113", "ZB MED_STAFF only"); //Füge Datei in den Unterordner 1\ ein und setze ARPolicy auf File-Ebene 
		REP rep2 = sip.newREP("MODIFIED_MASTER"); //Füge neue Repräsentation hinzu
		/*
		 *  Speichere testDatei unter einen anderen Namen in den Unterordner 2\
		 *  und notiere md5Summe dazu (wird beim deploy überprüft, ob die stimmt)
		 */
		rep2.newFile(testDatei, "2".concat(fs).concat("Test2.txt"), null).setMd5sum("dfcd625d3138ed3d84e077161d579617");
		rep2.newFile(testDatei, "Test3.txt", null).addMetadata("dc:title", "DateiTitel"); //Füge dc-Metadatum auf File-Ebene hinzu
		rep2.newFile(testDatei, "2".concat(fs).concat("a").concat(fs), null).setLabel("Eine Datei"); //Füge eine Datei mit alternativem Label hinzu
		sip.deploy("bin" + fs + "maximalSip");
	}
	
	public static void main(String[] args) throws Exception {
		deleteExamples();
		createExamples();
		System.out.println("Example Ende");
	}
}
