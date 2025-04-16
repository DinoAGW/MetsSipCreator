package utilities;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Properties;

public class PropertiesManager {
	private String propertyDateiPfad = null;
	private Properties properties;
	
	public PropertiesManager(String propertyDateiPfad) {
		this.propertyDateiPfad = propertyDateiPfad;
		this.properties = new Properties();
	}

	public String getPropertyDateiPfad() {
		return propertyDateiPfad;
	}

	public Properties getProperties() {
		return properties;
	}

	public void saveStringToProperty(String prop, String value) throws IOException {
		Writer writer = new FileWriter(propertyDateiPfad);
		properties.setProperty(prop, value);
		properties.store(writer, "default Kommentar");
		writer.close();
	}

	public String readStringFromProperty(String prop) throws IOException {
		BufferedInputStream reader = new BufferedInputStream(new FileInputStream(propertyDateiPfad));
		properties.load(reader);
		reader.close();
		return properties.getProperty(prop);
	}

}
