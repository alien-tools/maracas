package com.github.maracas.experiments.csv;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.input.ReversedLinesFileReader;

public abstract class CSVManager {

	public static final String DELIMETER = ",";

	protected final String path;
	protected final CSVFormat csvFormat;
	protected final String stringFormat;
	protected final String header;
	protected final String[] columns;
	protected final Map<String, String> columnsFormat;
	protected final String cursor;

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
		this.cursor = getLastCursor();

		initializeFile();
	}

	private String getLastCursor() throws IOException {
		File file = new File(path);

		if (file.exists()) {
			try (ReversedLinesFileReader reader = new ReversedLinesFileReader(file,
				StandardCharsets.UTF_8)) {
				String line = reader.readLine();

				if (line != null && !line.isEmpty()) {
					int cursorPos = Arrays.binarySearch(columns, "cursor");
					String cursor = line.split(DELIMETER)[cursorPos];
					if (!cursor.equals("cursor"))
						return cursor;
				}
			}
		}

		return null;
	}

	private String buildFormat() {
		String[] format = columns.clone();
		Arrays.setAll(format, c -> columnsFormat.get(c));
		return String.join(",", format) + "\n";
	}

	private String buildHeader() {
		return String.join(",", columns);
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
				//cleanFile();
			}
		} catch (IOException e) {
			// Don't do anything
		}

		if (initialize) {
			try (FileWriter writer = new FileWriter(path);
				CSVPrinter printer = new CSVPrinter(writer, csvFormat)) {
				printer.printRecord(columns);
			}
		}
	}

	public String getCursor() {
		return cursor;
	}

	protected void cleanFile() throws IOException {
		File original = new File(path);

		if (original.exists()) {
			File tmp = Files.createTempFile("output-temp", ".csv").toFile();
			FileUtils.copyFile(original, tmp);
			original.delete();
			original.createNewFile();

			try (Reader reader = new FileReader(tmp);
				Writer writer = new FileWriter(original, true);
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

	public abstract void writeRecord(Object obj);
}
