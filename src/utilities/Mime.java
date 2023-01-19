package utilities;

public class Mime {
	private static final String fs = System.getProperty("file.separator");	
	
	/*
	 * weist manche MimeTypes zu.
	 * Für weitere, siehe hier: https://wiki.selfhtml.org/wiki/MIME-Type/%C3%9Cbersicht
	 */
	public static String endung2mime(String dateiname) throws Exception {
		int letzterPunkt = dateiname.lastIndexOf(".");
		if (letzterPunkt == -1) {
			System.err.println("Dateiname hat keine Dateiendung".concat(dateiname));
			throw new Exception();
		}
		String dateiendung = dateiname.substring(letzterPunkt + 1);
		PropertiesManager ppm = new PropertiesManager("resources".concat(fs).concat("mimeTypes.txt"));
		String mimeType = ppm.readStringFromProperty(dateiendung);
		if (mimeType == null) {
			System.err.println("Dateiendung nicht erkannt: ".concat(dateiendung));
			throw new Exception();
		}
		return mimeType;
	}
}
