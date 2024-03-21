package com.idega.block.importer.data;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import com.idega.block.importer.business.NoRecordsException;
import com.idega.util.CoreConstants;
import com.idega.util.IOUtil;
import com.idega.util.Timer;

public class ExcelImportFile extends GenericImportFile {

	private static final Logger LOGGER = Logger.getLogger(ExcelImportFile.class.getName());

	private Iterator<String> iter;

	private boolean getValueAsString;

	public boolean isGetValueAsString() {
		return getValueAsString;
	}

	public void setGetValueAsString(boolean getValueAsString) {
		this.getValueAsString = getValueAsString;
	}

	@Override
	public Object getNextRecord() {
		return getNextRecord(isGetValueAsString());
	}

	public Object getNextRecord(boolean getValueAsString) {
		if (iter == null) {
			Collection<String> records = getAllRecords(getValueAsString);
			if (records != null) {
				iter = records.iterator();
			}
		}

		if (iter != null) {
			while (iter.hasNext()) {
				return iter.next();
			}
		}

		return CoreConstants.EMPTY;
	}

	public Collection<String> getAllRecords() throws NoRecordsException {
		return getAllRecords(false);
	}

	public static Workbook getWorkbook(InputStream input) throws Exception {
		return WorkbookFactory.create(input);
	}

	public Collection<String> getAllRecords(boolean getValuesAsStrings) throws NoRecordsException {
		FileInputStream input = null;
		try {
			input = new FileInputStream(getFile());
			Workbook wb = null;
			try {
				wb = getWorkbook(input);
			} catch (Exception e) {
				LOGGER.log(Level.WARNING, "Error getting workbook from " + getFile(), e);
			}
			if (wb == null) {
				return null;
			}

			DataFormatter dataFormatter = new DataFormatter();
			int numberOfSheets = wb.getNumberOfSheets();
			int records = 0;
			List<String> list = new ArrayList<>();
			for (int sheetIndex = 0; sheetIndex < numberOfSheets; sheetIndex++) {
				Sheet sheet = wb.getSheetAt(sheetIndex);

				Timer clock = new Timer();
				clock.start();

				StringBuffer buffer = new StringBuffer();
				for (int i = sheet.getFirstRowNum(); i <= sheet.getLastRowNum(); i++) {
					Row row = sheet.getRow(i);
					if (buffer == null) {
						buffer = new StringBuffer();
					}

					if (row != null) {
						for (int j = row.getFirstCellNum(); j < row.getLastCellNum(); j++) {
							Cell cell = row.getCell(j);
							if (cell != null) {
								Serializable value = null;

								if (getValuesAsStrings) {
									value = dataFormatter.formatCellValue(cell);
								} else if (cell.getCellType() == CellType.STRING) {
									value = cell.getStringCellValue();
								} else if (cell.getCellType() == CellType.NUMERIC) {
									value = cell.getNumericCellValue();
								} else if (cell.getCellType() == CellType.FORMULA) {
									switch (cell.getCachedFormulaResultType()) {
										case NUMERIC: {
											value = cell.getNumericCellValue();
											break;
										}
										case STRING: {
											value = cell.getStringCellValue();
											break;
										}
										case BLANK: {
											value = CoreConstants.EMPTY;
											break;
										}
										case BOOLEAN: {
											value = cell.getBooleanCellValue();
											break;
										}
									default:
										value = CoreConstants.EMPTY;
										break;
									}
								} else {
									value = cell.getStringCellValue();
								}

								value = value == null ? CoreConstants.EMPTY : value;
								buffer.append(value);
							}
							buffer.append(getValueSeparator());
						}

						records++;
						if ((records % 1000) == 0) {
							LOGGER.info("Importer: Reading record nr.: " + records + " from file " + getFile().getName());
						}

						list.add(buffer.toString());
						buffer = null;
					}
				}
			}

			if (records == 0) {
				throw new NoRecordsException("No records where found in the selected file" + getFile().getAbsolutePath());
			}

			return list;
		} catch (Exception e) {
			LOGGER.log(Level.WARNING, "Error reading from " + getFile(), e);
			return null;
		} finally {
			IOUtil.close(input);
		}
	}

}