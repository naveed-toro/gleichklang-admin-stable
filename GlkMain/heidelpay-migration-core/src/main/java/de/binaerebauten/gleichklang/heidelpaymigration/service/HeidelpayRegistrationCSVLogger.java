package de.binaerebauten.gleichklang.heidelpaymigration.service;

import de.binaerebauten.gleichklang.heidelpaymigration.model.MigrationResult;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Log results to a CSV file.
 */
public class HeidelpayRegistrationCSVLogger
{
	private static final char COMMA_SEPARATOR = ',';
	
	private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
	
	private FileWriter writer;
	
	public HeidelpayRegistrationCSVLogger(String csvFile)
	{
		try
		{
			this.writer = new FileWriter(csvFile, true);
			if (!new File(csvFile).exists())
			{
				this.writeLine("User ID", "Message", "Date");
			}
		}
		catch (IOException e)
		{
			throw new IllegalArgumentException("Unable to create file with the given path.");
		}
	}
	
	public void writeLine(MigrationResult migrationResult)
	{
		try
		{
			writeLine(migrationResult.getId(), migrationResult.getMsg());
		}
		catch (IOException ignored)
		{
		}
	}
	
	private void writeLine(String... values) throws IOException
	{
		boolean first = true;
		
		StringBuilder sb = new StringBuilder();
		for (String value : values)
		{
			if (!first)
			{
				sb.append(COMMA_SEPARATOR);
			}
			sb.append(format(value));
			first = false;
		}
		sb.append(COMMA_SEPARATOR)
				.append(String.format("%s\n", formatter.format(LocalDateTime.now())));
		writer.append(sb.toString());
	}
	
	private static String format(String value) {
		String result = value == null ? "" : value;
		if (result.contains("\"")) {
			result = result.replace("\"", "\"\"");
		}
		return result;
	}
	
	public void close() {
		try
		{
			writer.close();
		}
		catch (IOException ignored)
		{
		}
	}
	
}
