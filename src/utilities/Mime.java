package utilities;

public class Mime {
	private static final String fs = System.getProperty("file.separator");

	/*
	 * weist manche MimeTypes zu. Für weitere, siehe hier:
	 * https://wiki.selfhtml.org/wiki/MIME-Type/%C3%9Cbersicht
	 */
	public static String endung2mime(String dateiname) throws Exception {
		int letzterPunkt = dateiname.lastIndexOf(".");
		if (letzterPunkt == -1) {
			System.err.println("Dateiname hat keine Dateiendung".concat(dateiname));
			throw new Exception();
		}
		String dateiendung = dateiname.substring(letzterPunkt + 1);
		String mimeType = getMime(dateiendung);
		if (mimeType == null) {
			System.err.println("Dateiendung nicht erkannt: ".concat(dateiendung));
			throw new Exception();
		}
		return mimeType;
	}

	private static String getMime(String dateiendung) {
		switch (dateiendung) {
		case "txt":
			return "text/plain";
		case "pdf":
			return "application/pdf";
		case "xml":
			return "text/xml";
		case "zip":
			return "application/zip";
		case "tar":
			return "application/x-tar";
		case "png":
			return "image/png";
		case "avi":
			return "video/x-msvideo";
		case "mp4":
			return "video/mp4";
		case "json":
			return "application/json";
		default:
			return null;
		}
	}
}
