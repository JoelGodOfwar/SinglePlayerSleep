package com.github.joelgodofwar.sps.i18n;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.TreeMap;


public class Translator {
	private static String lang;
	private static String dataFolder;
	@SuppressWarnings("static-access")
	public Translator(String lang, String dataFolder){
		this.lang = formatLanguageCode(lang);
		this.dataFolder = dataFolder;
	}
	public static String get(String key, String... defaultValue) {
		Translator language = new Translator(lang, dataFolder);
	    return language.get(lang, key, defaultValue);
	}
	public String get(String lang, String key, String... defaultValue) {
		String value = Arrays.stream(defaultValue).findFirst().orElse(null);
		ResourceBundle bundle = ResourceBundle.getBundle("lang/lang", new Locale(lang));
		//ResourceBundle bundle = ResourceBundle.getBundle("lang/" + lang);
		
		try {
			value = bundle.getString(key);
		} catch (MissingResourceException e) {
			// Key not found in bundle
		}
		File langFile = new File(dataFolder + File.separatorChar + "lang" + File.separatorChar + lang + ".properties");
		Properties props = new Properties();
		try {
			if (langFile.exists()) {
				FileInputStream inputStream = new FileInputStream(langFile);
				props.load(inputStream);
				inputStream.close();
				if (!props.containsKey(key)) {
					props.setProperty(key, value);
					sortPropertiesFile(langFile, props);
				}
			} else {
				// File doesn't exist, use default value from bundle or null if defaultValue is not present
				return value;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		// Get value from properties file (either updated or existing)
		value = props.getProperty(key);
		return value;
	}
	public void sortPropertiesFile(File file, Properties props) throws IOException {
		// Create a TreeMap to store the sorted properties
	    Map<String, String> sortedProps = new TreeMap<>();
	    // Iterate over the Properties object and add each key-value pair to the TreeMap
	    for (String key : props.stringPropertyNames()) {
	        sortedProps.put(key, props.getProperty(key));
	    }
		// Clear the properties file
		try (FileOutputStream outputStream = new FileOutputStream(file)) {
			outputStream.getChannel().truncate(0);
		}
		// Write sorted properties to file
		try (FileWriter writer = new FileWriter(file)) {
			for (Map.Entry<String, String> entry : sortedProps.entrySet()) {
				writer.write(entry.getKey() + "=" + entry.getValue() + "\n");
			}
		}
	}
	public String formatLanguageCode(String lang) {
	    String[] parts = lang.split("_");
	    return parts[0].toLowerCase() + "_" + parts[1].toUpperCase();
	}

}