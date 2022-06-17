package com.github.maracas.experiments;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

public abstract class CSVManager {

	public static final String DELIMETER = ",";

	protected final String path;
	protected final CSVFormat csvFormat;
	protected final String stringFormat;
	protected final String header;
	protected final String[] columns;
	protected final Map<String, String> columnsFormat;

	public CSVManager(String path) throws IOException {
		this.path = path;
		this.columns = buildColumns();
		this.columnsFormat = buildColumnsFormat();
		this.stringFormat = buildFormat();
		this.header = buildHeader();
		this.csvFormat = CSVFormat.Builder.create(CSVFormat.DEFAULT)
			.setAllowMissingColumnNames(false)
			.setAutoFlush(true)
			.setDelimiter(DELIMETER)
			.setHeader(columns)
			.setSkipHeaderRecord(true)
			.setTrim(true)
			.build();

		initializeFile();
	}

	private String buildFormat() {
		String[] format = columns.clone();
		Arrays.setAll(format, c -> columnsFormat.get(c));
		return String.join(",", format) + "\n";
	}

	private String buildHeader() {
		return String.join(",", columns) + "\n";
	}

	protected abstract String[] buildColumns();
	protected abstract Map<String, String> buildColumnsFormat();

	public boolean csvExists() {
		File csv = new File(path);
		return csv.exists();
	}

	private void initializeFile() throws IOException{
		File csv = new File(path);
		boolean initialize = true;

		try (Reader reader = new FileReader(path)) {
			Iterable<CSVRecord> records = csvFormat.parse(reader);
			boolean hasContent = records.iterator().hasNext();
			initialize = hasContent;

			if (hasContent) {
				initialize = false;
				cleanFile();
			}
		} catch (IOException e) {
			// Don't do anything
		}

		if (initialize) {
			try (FileWriter writer = new FileWriter(path)) {
				csvFormat.print(csv, StandardCharsets.UTF_8);
			}
		}
	}

	protected abstract void cleanFile() throws IOException;

	public int getColumnPosition(String column) {
		return Arrays.binarySearch(columns, column);
	}
}
