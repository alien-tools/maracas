package com.github.maracas.experiments.csv;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.csv.CSVPrinter;

public class ErrorsCSVManager extends CSVManager {

	public ErrorsCSVManager(String path) throws IOException {
		super(path);
	}

	@Override
	protected String[] buildColumns() {
		return new String[] {"cursor", "owner", "name", "code", "comments"};
	}

	@Override
	protected Map<String, String> buildColumnsFormat() {
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("cursor", "%s");
		map.put("code", "%s");
		map.put("owner", "%s");
		map.put("name", "%s");
		map.put("comments", "%s");
		return map;
	}

	@Override
	public void writeRecord(Object obj) {
		if (obj instanceof ErrorRecord error) {
			File csv = new File(path);
			try (Writer writer = new FileWriter(csv, true);
				CSVPrinter printer = new CSVPrinter(writer, csvFormat);) {
				printer.printRecord(error.cursor(), error.owner(), error.name(),
					error.code(), error.comments());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
