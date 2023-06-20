

import gov.loc.mets.DivType;
import gov.loc.mets.FileType;
import gov.loc.mets.MetsDocument;
import gov.loc.mets.MetsDocument.Mets;
import gov.loc.mets.MetsType.FileSec.FileGrp;
import gov.loc.mets.StructMapType;

import java.io.File;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.apache.xmlbeans.XmlOptions;
import org.xml.sax.InputSource;

import com.exlibris.core.infra.common.util.IOUtil;
import com.exlibris.core.sdk.consts.Enum;
import com.exlibris.core.sdk.formatting.DublinCore;
import com.exlibris.core.sdk.utils.FileUtil;
import com.exlibris.digitool.common.dnx.DnxDocument;
import com.exlibris.digitool.common.dnx.DnxDocumentHelper;
import com.exlibris.dps.sdk.deposit.IEParser;
import com.exlibris.dps.sdk.deposit.IEParserFactory;

public class FullFlowExample {

	static final String userName = "admin1";
	static final String institution = "INS00";
	static final String password = "a12345678A";
	static final String materialflowId = "5";
	static final String depositSetId = "1";

	//should be placed under where submission format of MF is configured
	static final String folder_on_working_machine = "C:\\Users\\hixel\\workspace";
	static final String filesRootFolder = folder_on_working_machine + "/DepositExample1/content/streams/";
	static final String IEfullFileName = folder_on_working_machine + "/DepositExample1/content/ie1.xml";

	static final String DEPOSIT_WSDL_URL = "http://localhost:1801/dpsws/deposit/DepositWebServices?wsdl";
	static final String PRODUCER_WSDL_URL = "http://localhost:1801/dpsws/backoffice/ProducerWebServices?wsdl";
	static final String SIP_STATUS_WSDL_URL = "http://localhost:1801/dpsws/repository/SipWebServices?wsdl";

	public static final String ROSETTA_METS_SCHEMA = "http://www.exlibrisgroup.com/xsd/dps/rosettaMets";
	public static final String METS_SCHEMA = "http://www.loc.gov/METS/";
	public static final String XML_SCHEMA = "http://www.w3.org/2001/XMLSchema-instance";
	public static final String XML_SCHEMA_REPLACEMENT = "http://www.exlibrisgroup.com/XMLSchema-instance";
	public static final String METS_XSD = "mets.xsd";
	public static final String ROSETTA_METS_XSD = "mets_rosetta.xsd";

	/**
	 * Full Flow Example with all stages to create and make a Deposit.
	 *
	 */

	public static void main(String[] args) {


		try {
			// 1. Create a SIP directory

			// 2. Create the IE using IE parser

			//list of files we are depositing
			File streamDir = new File(filesRootFolder);
			File[] files = streamDir.listFiles();

			//create parser
			IEParser ie = IEParserFactory.create();

			// add ie dc
			DublinCore dc = ie.getDublinCoreParser();
			dc.addElement("dc:creator", "Exlibris");
			dc.addElement("dc:identifier", "ISBN 1-56389-016-X");
			dc.addElement("dc:title", "SDK - TEST DC");
			ie.setIEDublinCore(dc);
			List<FileGrp> fGrpList = new ArrayList<FileGrp>();

			// add fileGrp
			FileGrp fGrp = ie.addNewFileGrp(Enum.UsageType.VIEW, Enum.PreservationType.PRESERVATION_MASTER);

			// add dnx - A new DNX is constructed and added on the file group level
			DnxDocument dnxDocument = ie.getFileGrpDnx(fGrp.getID());
			DnxDocumentHelper documentHelper = new DnxDocumentHelper(dnxDocument);
			documentHelper.getGeneralRepCharacteristics().setRevisionNumber("1");
			documentHelper.getGeneralRepCharacteristics().setLabel("test");

			//adding an event to the rep DNX
			List<DnxDocumentHelper.Event> eventList = new ArrayList<DnxDocumentHelper.Event>();
			DnxDocumentHelper.Event event = documentHelper.new Event();
			event.setEventIdentifierValue("Test");
			eventList.add(event);
			documentHelper.setEvents(eventList);

			ie.setFileGrpDnx(documentHelper.getDocument(), fGrp.getID());

			fGrpList.add(fGrp);

			for(int i=0;i<files.length;i++){

	            //add file and dnx metadata on file
	            String mimeType = "image/jpeg";
	            FileType fileType = ie.addNewFile(fGrp, mimeType, files[i].getName(), "test file " + i);

	            // add dnx - A new DNX is constructed and added on the file level
	            DnxDocument dnx = ie.getFileDnx(fileType.getID());
	            DnxDocumentHelper fileDocumentHelper = new DnxDocumentHelper(dnx);
	            fileDocumentHelper.getGeneralFileCharacteristics().setNote("note to test");
	            fileDocumentHelper.getGeneralFileCharacteristics().setLabel("test");
	            fileDocumentHelper.getGeneralFileCharacteristics().setFileOriginalPath(files[i].getAbsolutePath());
	            ie.setFileDnx(fileDocumentHelper.getDocument(), fileType.getID());
			}

            ie.generateChecksum(filesRootFolder, Enum.FixityType.MD5.toString());
            ie.updateSize(filesRootFolder);

            //default Structural Map
            ie.generateStructMap(fGrpList.get(0),null, "Table of Contents");

            //example for adding a logical Struct Map.
            MetsDocument metsDoc = MetsDocument.Factory.parse(ie.toXML());
            Mets mets = metsDoc.getMets();
            for(FileGrp fgrp:fGrpList){
            	StructMapType sm = mets.addNewStructMap();
                sm.setID(fgrp.getID() + "-" + 2);
                sm.setTYPE("LOGICAL");
                DivType div1 = sm.addNewDiv();
                div1.setLABEL("Test Struct Map");
                DivType div2 = div1.addNewDiv();
                div2.setLABEL("Table of Contents");
                DivType div3 = div2.addNewDiv();
                div3.setLABEL("Chapter 1");
                FileType file[] = fgrp.getFileArray();
                for(int i=0;i<file.length;i++){
                	DivType div = div3.addNewDiv(); // new div structure
                	div.setLABEL("Page " + i);
                    div.setTYPE("FILE");
                	div.addNewFptr().setFILEID(file[i].getID());
                }
            }

            //insert IE created in content directory
            File ieXML = new File(IEfullFileName);
            XmlOptions opt = new XmlOptions();
            opt.setSavePrettyPrint();
            String xmlMetsContent = metsDoc.xmlText(opt);
            FileUtil.writeFile(ieXML, xmlMetsContent);


            //validate against mets_rosetta.xsd

            //Need to replace manually the namespace with Rosetta Mets schema in order to pass validation against mets_rosetta.xsd
            String xmlRosettaMetsContent = xmlMetsContent.replaceAll(XML_SCHEMA, XML_SCHEMA_REPLACEMENT);
            xmlRosettaMetsContent = xmlMetsContent.replaceAll(METS_SCHEMA, ROSETTA_METS_SCHEMA);

            validateXML(ieXML.getAbsolutePath(), xmlRosettaMetsContent, ROSETTA_METS_XSD);

		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
	}

	private static void validateXML(String fileFullName, String xml, String xsdName) throws Exception{
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);
			factory.setSchema(getSchema(xsdName));
			DocumentBuilder builder = factory.newDocumentBuilder();
			builder.parse(new InputSource(new StringReader(xml)));
		} catch (Exception e) {
			System.out.println("XML '" + fileFullName + "' doesn't pass validation by :" + xsdName + " with the following validation error: " + e.getMessage());
		}
	}

	private static Schema getSchema(String xsdName) throws Exception {
		Map<String, Schema> schemas = new HashMap<String, Schema>();
		if (schemas.get(xsdName) == null ) {
			InputStream inputStream = null;
			try {
				File xsd = new File("src/xsd/mets_rosetta.xsd");
				Source xsdFile = new StreamSource(xsd);
				SchemaFactory schemaFactory = SchemaFactory.newInstance("http://www.w3.org/XML/XMLSchema/v1.1");
				schemas.put(xsdName, schemaFactory.newSchema(xsdFile));
			} catch (Exception e) {
				System.out.println("Failed to create Schema with following error: " + e.getMessage());
			} finally {
				IOUtil.closeQuietly(inputStream);
			}
		}
		return schemas.get(xsdName);
	}
}
