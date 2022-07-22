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
import org.gradle.internal.impldep.org.apache.commons.lang.ArrayUtils;

/**
 * Class in charge of managing the output CSV files of the experiment.
 */
public abstract class CSVManager {
	/**
	 * CSV delimeter
	 */
	public static final String DELIMETER = ",";


	/**
	 * Path to the managed CSV file
	 */
	protected final String path;

	/**
	 * Array with the CSV column names
	 */
	protected final String[] columns;

	/**
	 * Map between columns' names and the corresponding string format of their
	 * values (e.g. "%s", "%d", "%b")
	 */
	protected final Map<String, String> columnsFormat;

	/**
	 * String format of a row in the CSV file
	 */
	protected final String stringFormat;

	/**
	 * Format of the CSV file
	 */
	protected final CSVFormat csvFormat;

	/**
	 * Last result cursor printed in the CSV file
	 */
	protected final String cursor;


	/**
	 * Creates a {@link CSVManager} instance.
	 *
	 * @param path  Path to the CSV file to manage
	 * @throws IOException
	 */
	public CSVManager(String path) throws IOException {
		this.path = path;
		this.columns = buildColumns();
		this.columnsFormat = buildColumnsFormat();
		this.stringFormat = buildFormat();
		this.csvFormat = CSVFormat.Builder.create(CSVFormat.DEFAULT)
			.setAllowMissingColumnNames(false)
			.setAutoFlush(true)
			.setDelimiter(DELIMETER)
			.setHeader(columns)
			.setSkipHeaderRecord(true)
			.setTrim(true)
			.build();
		this.cursor = getLastValue("cursor");

		initializeFile();
	}

	/**
	 * Builds the string array with the columns' names of the CSV file.
	 *
	 * @return string array with the columns' names of the CSV file.
	 */
	protected abstract String[] buildColumns();

	/**
	 * Maps columns' names to the corresponding string format of their values
	 * (e.g. "%s", "%d", "%b")
	 *
	 * @return map between columns' names and the corresponding string format.
	 */
	protected abstract Map<String, String> buildColumnsFormat();

	/**
	 * Writes a record on the CSV file.
	 *
	 * @param obj  Object to write on the CSV file.
	 */
	public abstract void writeRecord(Object obj);

	/**
	 * Initializes the CSV file. I.e., if the file does not exist, it prints
	 * the header of the CSV file.
	 *
	 * @throws IOException
	 */
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

	/**
	 * Gets the last printed value on the CSV file of a given column.
	 *
	 * @param column  Name of the column
	 * @return last printed value on the CSV file of a given column. If there
	 * is no value returns {@code null}.
	 * @throws IOException
	 */
	protected String getLastValue(String column) throws IOException {
		File file = new File(path);

		if (file.exists()) {
			try (ReversedLinesFileReader reader = new ReversedLinesFileReader(file,
				StandardCharsets.UTF_8)) {
				String line = reader.readLine();

				if (line != null && !line.isEmpty()) {
					int columnPos = ArrayUtils.indexOf(columns, column);
					String value = line.split(DELIMETER)[columnPos];
					if (!value.equals(column))
						return value;
				}
			}
		}

		return null;
	}

	/**
	 * Builds the string with the format of a CSV file row.
	 *
	 * @return string array with the columns' names of the CSV file.
	 */
	private String buildFormat() {
		String[] format = new String[columns.length];
		Arrays.setAll(format, i -> columnsFormat.get(columns[i]));
		return String.join(",", format) + "\n";
	}

	/**
	 * Verifies if the managed CSV file exists in the file system.
	 *
	 * @return {@code true} if the CSV file exists; {@code false} otherwise.
	 */
	public boolean csvExists() {
		File csv = new File(path);
		return csv.exists();
	}

	/**
	 * Returns the last printed cursor on the CSV file.
	 *
	 * @return last printed cursor on the CSV file; {@code null} if it does not
	 * exist.
	 */
	public String getCursor() {
		return cursor;
	}

	/**
	 * Cleans the CSV file if the execution of the experiment is abruptly
	 * interrupted. That is, it will remove the last records pointing to the
	 * last cursor. Then in the next execution of the program, it will start
	 * again from the repository pointing to such cursor.
	 *
	 * @throws IOException
	 */
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

}
