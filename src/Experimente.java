import java.io.File;

import com.exlibris.dps.sdk.deposit.IEParser;
import com.exlibris.dps.sdk.deposit.IEParserFactory;

import gov.loc.mets.MetsType.FileSec.FileGrp;

@SuppressWarnings("unused")
public class Experimente {
	private static final String fs = System.getProperty("file.separator");
	private static final String home = System.getProperty("user.home").concat(fs);
	private static final String workspace = home.concat("workspace").concat(fs);
	
	static void test1() throws Exception {
		File file = new File(workspace.concat("ie.xml"));
		IEParser ie = IEParserFactory.parse(file);
		String fileId = ie.getFileGrpArray()[0].getFileArray()[0].getID();
		String fileDmd = ie.getFileDmdId(fileId);
//		System.out.println(ie.getDMDDC(fileDmd).);
		System.out.println(ie.getDMDDC(fileDmd).toXml());
	}
	
	static void test2() throws Exception {
		IEParser ie = IEParserFactory.create();
//		ie.
//		System.out.println(fileDmd);
//		FileType ft = sip.ie.getFile(this.fileTypeId);
//		System.out.println(ft);
//		DublinCore fileDc1 = sip.ie.getDMDDC(this.fileTypeId);
//		System.out.println(fileDc1.getTitle());
//		DublinCore fileDc2 = sip.ie.getDMDDC(fileDmd);
//		System.out.println(fileDc2);
//		fileDc.addElement("dc:title", "a file title");
	}

	public static void main(String[] args) throws Exception {
		test2();
	}

}
