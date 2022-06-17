package com.github.maracas.experiments;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.input.ReversedLinesFileReader;

public class ClientsCSVManager extends CSVManager {

	private final String cursor;

	public ClientsCSVManager(String path) throws IOException {
		super(path);
		this.cursor = getLastCursor();
	}

	public String getCursor() {
		return cursor;
	}

	private String getLastCursor() throws IOException {
		File file = new File(path);

		try (ReversedLinesFileReader reader = new ReversedLinesFileReader(file,
			StandardCharsets.UTF_8)) {
			String line = reader.readLine();

			if (line != null && !line.startsWith(header)) {
				int cursorPos = getColumnPosition("cursor");
				return line.split(DELIMETER)[cursorPos];
			}
		}

		return null;
	}

	@Override
	protected String[] buildColumns() {
		return new String[] {"cursor", "owner", "name", "stars", "groupId",
			"artifactId", "version", "path", "clients", "relevantClients",
			"cowner", "cname", "cstars"};
	}

	@Override
	protected Map<String, String> buildColumnsFormat() {
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("cursor", "%s");
		map.put("owner", "%s");
		map.put("name", "%s");
		map.put("url", "%s");
		map.put("stars", "%d");
		map.put("groupId", "%s");
		map.put("artifactId", "%s");
		map.put("version", "%s");
		map.put("path", "%s");
		map.put("clients", "%d");
		map.put("relevantClients", "%d");
		map.put("cowner", "%s");
		map.put("cname", "%s");
		map.put("curl", "%s");
		map.put("cstars", "%d");
		return map;
	}

	@Override
	protected void cleanFile() throws IOException {
		File original = new File(path);

		if (original.exists()) {
			File tmp = Files.createTempFile("output-temp", ".csv").toFile();
			FileUtils.copyFile(original, tmp);
			original.delete();
			original.createNewFile();

			try (Reader reader = new FileReader(tmp);
				Writer writer = new FileWriter(original);
				CSVPrinter printer = new CSVPrinter(writer, csvFormat);) {
				Iterable<CSVRecord> records = csvFormat.parse(reader);

				for (CSVRecord record : records) {
					String cursor = record.get("cursor");
					if (cursor.equals(this.cursor))
						break;

					printer.printRecord(record);
				}
			} catch (Exception e) {
				FileUtils.copyFile(tmp, original); // Back to initial state
			}
		}
	}

}
