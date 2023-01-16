
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Stack;

public class SIP {
	private static final String fs = System.getProperty("file.separator");
	String arbeitsverzeichnis;
	Stack<REP> reps = new Stack<>();
	Stack<String> metadataXPathKey = new Stack<>();
	Stack<String> metadataValue = new Stack<>();
	Stack<String> validXPathKeyOptions = new Stack<>();

	public SIP(String arbeitsverzeichnis) throws Exception {
		if (arbeitsverzeichnis == null) {
			this.arbeitsverzeichnis = Drive.getArbeitsverzeichnis();
		} else {
			this.arbeitsverzeichnis = arbeitsverzeichnis;
		}

		// Lade valide xPathKey Optionen in einen Stack
		File whitelistForXPathKey = new File("resources".concat(fs).concat("WhiteListForXPathKey.txt"));
		try (BufferedReader br = new BufferedReader(new FileReader(whitelistForXPathKey))) {
			String line;
			while ((line = br.readLine()) != null) {
				validXPathKeyOptions.push(line);
			}
		} catch (Exception e) {
			System.err.println("Fehler beim Einlesen der xPathKey-Whitelist");
			throw e;
		}
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
			if (rep.files.empty()) {
				System.err.println("Repräsentation hat keine Dateien: " + rep.preservationType);
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
		if (!checkIfMetadataIsValid(xPathKey, value)) {
			System.err.println("Füge Metadatum nicht hinzu");
			return;
		}
		metadataXPathKey.push(xPathKey);
		metadataValue.push(value);
	}

	private boolean checkIfMetadataIsValid(String xPathKey, String value) {
		if (!validXPathKeyOptions.contains(xPathKey)) {
			System.err.println("xPathKey ist nicht in der Whitelist: '" + xPathKey + "' = '" + value + "'");
			return false;
		}
		// TODO: weiter Ausarbeiten
		if (value == null) {
			System.err.println("Es fehlt ein Value für das Metadatum: '" + xPathKey + "' = '" + value + "'");
			return false;
		}
		if (value.contains("<![CDATA[")) {
			System.err.println("Info: CDATA vorhanden in: '" + xPathKey + "' = '" + value + "'");
		}
		if (value.contains("&")) {
			System.err
					.println("Warnung: & im Metadatum potentiell problematisch: '" + xPathKey + "' = '" + value + "'");
		}
		if (value.contains("<")) {
			System.err
					.println("Warnung: < im Metadatum potentiell problematisch: '" + xPathKey + "' = '" + value + "'");
		}
		if (value.contains(">")) {
			System.err
					.println("Warnung: > im Metadatum potentiell problematisch: '" + xPathKey + "' = '" + value + "'");
		}
		return true;
	}

	public void deploy(String ziel) throws Exception {
		File zielFile = new File(ziel);
		if (zielFile.exists()) {
			System.err.println("Ziel schon belegt");
			throw new Exception();
		}

		if (!validate()) {
			System.err.println("SIP kann nicht ausgegeben werden. Etwas stimmt nicht");
			return;
		}

		//Dateien an Zielort verschieben
		for (REP rep : reps) {
			try {
				rep.moveToTarget(ziel);
			} catch (Exception e) {
				System.err.println("SIP-Auslieferung nicht erfolgreich: Es konnte eine Datei nicht platziert werden.");
				System.err.println("SIP hat damit einen inkonsistenten Zustand und ist nun unbrauchbar.");
				System.err.println("Ich weiß nicht wie das passieren konnte.");
				System.err.println("Es tut mir Leid =(");
				throw e;
			} finally {
				System.out.println("PLatzierung der Dateien erfolgreich. Lösche Arbeitsverzeichnis");
				deleteSip();
			}
		}
		
		//TODO: mets erstellen
	}
	
	public void deleteSip() throws IOException {
		File arbeitsverzeichnisFile = new File(this.arbeitsverzeichnis);
		if (arbeitsverzeichnisFile.exists()) {
			Files.delete(arbeitsverzeichnisFile.toPath());
		}
	}
}
