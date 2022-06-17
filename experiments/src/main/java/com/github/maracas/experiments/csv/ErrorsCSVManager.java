package com.github.maracas.experiments.csv;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.csv.CSVPrinter;

import com.github.maracas.experiments.utils.Constants;

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
			File csv = new File(Constants.ERRORS_CSV_PATH);
			try (Writer writer = new FileWriter(csv);
				CSVPrinter printer = new CSVPrinter(writer, csvFormat);) {
				printer.printRecord(error.cursor(), error.code(), error.owner(),
					error.name(), error.comments());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
