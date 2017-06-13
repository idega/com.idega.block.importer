package com.idega.block.importer.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.idega.block.importer.business.NoRecordsException;
import com.idega.util.CoreConstants;
import com.idega.util.Timer;
import com.idega.util.text.TextSoap;

/**
 * Title: IdegaWeb classes
 * Description:
 * Copyright: Copyright (c) 2002
 * Company: Idega Software
 *
 * @author <a href="mailto:eiki@idega.is"> Eirikur Sveinn Hrafnsson</a>
 * @version 1.0
 */

public class GenericImportFile implements ImportFile {

	private static final Logger LOGGER = Logger.getLogger(GenericImportFile.class.getName());

	private File file;
	private String recordDilimiter = CoreConstants.NEWLINE;
	private String valueSeparator = CoreConstants.SEMICOLON;
	private String emptyValueString = CoreConstants.SPACE;
	private InputStreamReader fr;
	private BufferedReader br;
	protected boolean addNewLineAfterRecord = false;

	public GenericImportFile() {
	}

	public GenericImportFile(File file) {
		this.file = file;
	}

	/**
	 * @return the String value of recordDilimiter.
	 */
	public String getRecordDilimiter() {
		return this.recordDilimiter;
	}

	/**
	 * @param aRecordDilimiter - the new value for recordDilimiter
	 */
	public void setRecordDilimiter(String aRecordDilimiter) {
		this.recordDilimiter = aRecordDilimiter;
	}

	/**
	 * This method works like an iterator. When the end of the file is reached it returns null
	 */
	@Override
	public Object getNextRecord() {
		String line;
		StringBuffer buf = new StringBuffer();

		try {
			if (this.fr == null) {
				this.fr = new FileReader(getFile());
				this.br = new BufferedReader(this.fr);
			}

			while (((line = this.br.readLine()) != null) && (line.indexOf(getRecordDilimiter()) == -1)) {
				buf.append(line);
				if (this.addNewLineAfterRecord) {
					buf.append('\n');
				}

				if (getRecordDilimiter().equals(recordDilimiter)) {
					break;// need to check because readline strips this token away.
				}

			}

			return buf.toString();
		}
		catch (FileNotFoundException ex) {
			LOGGER.log(Level.WARNING, "File not found: " + getFile(), ex);
			return null;
		}
		catch (IOException ex) {
			LOGGER.log(Level.WARNING, "Error reading file " + getFile(), ex);
			return null;
		}

	}

	/**
	 * @deprecated This method parses the file into records (ArrayList) and returns the complete list.
	 *             it throws a NoRecordsFoundException if no records where found.
	 */
	@Deprecated
	@Override
	public Collection<String> getRecords() throws NoRecordsException {
		try {
			if (getEncoding() == null) {
				this.fr = new FileReader(getFile());
			}
			else {
				this.fr = new InputStreamReader(new FileInputStream(getFile()), getEncoding());
			}
			this.br = new BufferedReader(this.fr);
			String line;
			StringBuffer buf = new StringBuffer();
			List<String> list = new ArrayList<String>();

			int cnt = 0;
			int records = 0;

			Timer clock = new Timer();
			clock.start();

			while ((line = this.br.readLine()) != null) {
				if (buf == null) {
					buf = new StringBuffer();
				}

				buf.append(line);

				/** @todo this should be an option with a setMethod? **/
				if (this.addNewLineAfterRecord) {
					buf.append('\n');
				}

				if (line.indexOf(getRecordDilimiter()) != -1) {
					records++;
					if ((records % 1000) == 0) {
						LOGGER.info("Importer: Reading record nr.: " + records + " from file " + getFile().getName());
					}

					list.add(buf.toString());
					buf = null;
				}

				cnt++;
			}

			line = null;
			buf = null;

			this.br.close();
			this.fr = null;
			this.br = null;

			clock.stop();

			if (records == 0) {
				throw new NoRecordsException("No records where found in the selected file" + this.file.getAbsolutePath());
			}

			LOGGER.info("Time for operation: " + clock.getTime() + " ms  OR " + ((int) (clock.getTime() / 1000)) + " s. Number of lines: " + cnt + ", number of records = " + records);

			return list;
		}
		catch (FileNotFoundException ex) {
			LOGGER.log(Level.WARNING, "File not found: " + getFile(), ex);
			return null;
		}
		catch (IOException ex) {
			LOGGER.log(Level.WARNING, "Error reading file " + getFile(), ex);
			return null;
		}

	}

	@Override
	public File getFile() {
		return this.file;
	}

	/**
	 * Returns the addNewLineAfterRecord.
	 *
	 * @return boolean
	 */
	public boolean isAddNewLineAfterRecord() {
		return this.addNewLineAfterRecord;
	}

	/**
	 * Sets if to add a \n after each record
	 *
	 * @param addNewLineAfterRecord	The addNewLineAfterRecord to set
	 */
	public void setAddNewLineAfterRecord(boolean addNewLineAfterRecord) {
		this.addNewLineAfterRecord = addNewLineAfterRecord;
	}

	/**
	 * Returns the valueSeparator.
	 *
	 * @return String
	 */
	public String getValueSeparator() {
		return this.valueSeparator;
	}

	/**
	 * Sets the file.
	 *
	 * @param file	The file to set
	 */
	@Override
	public void setFile(File file) {
		this.file = file;
	}

	/**
	 * Sets the valueSeparator.
	 *
	 * @param valueSeparator	The valueSeparator to set
	 */
	public void setValueSeparator(String valueSeparator) {
		this.valueSeparator = valueSeparator;
	}

	/**
	 * Method getValueAtIndexFromRecordString. Uses the valueSeparator and a stringtokenizer to read and return a value at a selected index.
	 *
	 * @param index	The index starts at 1
	 * @param recordString
	 * @return The value at the selected index. Remember if the value was empty the returned value will be getEmptyValueString().
	 */
	@Override
	public String getValueAtIndexFromRecordString(int index, String recordString) {
		int i = 1;
		recordString = TextSoap.findAndReplace(recordString, this.valueSeparator + this.valueSeparator, this.valueSeparator + this.emptyValueString + this.valueSeparator);
		recordString = TextSoap.findAndReplace(recordString, this.valueSeparator + this.valueSeparator, this.valueSeparator + this.emptyValueString + this.valueSeparator);
		StringTokenizer tokens = new StringTokenizer(recordString, this.valueSeparator);
		String value = null;
		while (tokens.hasMoreTokens() && i <= index) {
			value = tokens.nextToken();
			// LOGGER.info("GenericImportFile : index = "+index+" value = "+value);
			if (tokens.hasMoreTokens()) {
				i++;
			}
		}
		if (i < index) {
			return getEmptyValueString();
		}

		return value;
	}

	/**
	 * Method getValuesFromRecordString. Uses the valueSeparator and a stringtokenizer to read the record and create an ArrayList of values.
	 *
	 * @param recordString
	 * @return An ArrayList of values or null is no value was found
	 */
	@Override
	public List<String> getValuesFromRecordString(String recordString) {
		List<String> values = null;
		recordString = TextSoap.findAndReplace(recordString, this.valueSeparator + this.valueSeparator, this.valueSeparator + this.emptyValueString + this.valueSeparator);
		recordString = TextSoap.findAndReplace(recordString, this.valueSeparator + this.valueSeparator, this.valueSeparator + this.emptyValueString + this.valueSeparator);
		StringTokenizer tokens = new StringTokenizer(recordString, this.valueSeparator);
		String value = null;
		while (tokens.hasMoreTokens()) {
			if (values == null) {
				values = new ArrayList<String>();
			}
			value = tokens.nextToken();
			values.add(value);
		}

		return values;
	}

	/**
	 * Returns the ignoreIfFoundValue.
	 *
	 * @return String
	 */
	@Override
	public String getEmptyValueString() {
		return this.emptyValueString;
	}

	@Override
	public void setEmptyValueString(String emptyValueString) {
		this.emptyValueString = emptyValueString;
	}

	/**
	 * Closes the FileReader and BufferedReader
	 * @see com.idega.block.importer.data.ImportFile#close()
	 */
	@Override
	public void close() {
		if (null != this.br) {
			try {
				this.br.close();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (null != this.fr) {
			try {
				this.fr.close();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public String getEncoding() {
		return null;
	}
}