package com.github.joelgodofwar.sps.api;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import lib.github.joelgodofwar.coreutils.util.Strings;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.file.YamlConstructor;
import org.bukkit.configuration.file.YamlRepresenter;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.representer.Representer;

import com.google.common.base.Charsets;


/*
 * @author: Aoife (Josh)
 * @date: 2020-04-25
 * @project: WarpConverter
 */
@SuppressWarnings("deprecation")
public class YmlConfiguration extends YamlConfiguration {

	private final DumperOptions yamlOptions = new DumperOptions();
	private final Representer yamlRepresenter = new YamlRepresenter();
	private final Yaml yaml = new Yaml(new YamlConstructor(), yamlRepresenter, yamlOptions);
	private Map<Integer, String> commentContainer = new HashMap<>();

	@Override public void save(File file) throws IOException {
		Validate.notNull(file);

		try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), Charsets.UTF_8)) {
			writer.write(saveToString());
		}

	}

	public static void saveConfig(File file, YmlConfiguration config) {
		try {
			config.save(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public String saveToString(){
		yamlOptions.setIndent(options().indent());
		yamlOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
		yamlRepresenter.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);

		String header = "";//buildHeader();
		String dump = yaml.dump(getValues(false));

		if (dump.equals(BLANK_CONFIG)) {
			dump = Strings.EMPTY;
		} else {
			StringBuilder sb = new StringBuilder();
			int line = 0;
			for(String s : dump.split("\n")) {
				line++;
				while (commentContainer.containsKey(line)) {
					sb.append(commentContainer.get(line)).append("\n");
					line++;
				}
				sb.append(s).append("\n");
			}

			dump = sb.toString();
		}

		return header.length() > 0 ? header + dump : dump;
	}

	@Override
	public void load(Reader reader) throws IOException, InvalidConfigurationException {
		BufferedReader input = reader instanceof BufferedReader ? (BufferedReader) reader : new BufferedReader(reader);

		StringBuilder builder = new StringBuilder();

		try {
			String line;
			int count = 0;
			while ((line = input.readLine()) != null) {
				count++;
				if (line.contains(COMMENT_PREFIX)|| line.isEmpty()) {
					commentContainer.put(count, line);
				}
				builder.append(line).append('\n');
			}
		} finally {
			input.close();
		}

		loadFromString(builder.toString());
	}

	public static YamlConfiguration loadConfiguration(File file, YmlConfiguration config) {
		Validate.notNull(file, "File cannot be null");

		try {
			config.load(file);
		} catch (FileNotFoundException ex) {
		} catch (IOException | InvalidConfigurationException ex) {
			Bukkit.getLogger().log(Level.SEVERE, "Cannot load " + file, ex);
		}

		return config;
	}

}