package com.idega.block.importer.data;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import com.idega.block.importer.business.NoRecordsException;
import com.idega.util.CoreConstants;
import com.idega.util.Timer;

public class ExcelImportFile extends GenericImportFile {

	private Iterator<String> iter;

	@Override
	public Object getNextRecord() {
		if (iter == null) {
			Collection<String> records = getAllRecords();
			if (records != null) {
				iter = records.iterator();
			}
		}

		if (iter != null) {
			while (iter.hasNext()) {
				return iter.next();
			}
		}

		return "";
	}
	
	public Collection<String> getAllRecords() throws NoRecordsException {
		FileInputStream input = null;
		try {
			input = new FileInputStream(getFile());
			Workbook wb;
			
			try {
				wb = WorkbookFactory.create(input);
			} catch (InvalidFormatException e) {
				Logger.getLogger(getClass().getName()).log(Level.WARNING, "", e);
				return null;
			} 
			
			Sheet sheet = wb.getSheetAt(0);

			int records = 0;

			Timer clock = new Timer();
			clock.start();

			StringBuffer buffer = new StringBuffer();
			ArrayList<String> list = new ArrayList<String>();
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

							if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
								value = cell.getStringCellValue();
							} else if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
								value = cell.getNumericCellValue();
							} else if (cell.getCellType() == Cell.CELL_TYPE_FORMULA) {
								switch (cell.getCachedFormulaResultType()) {
									case Cell.CELL_TYPE_NUMERIC: {
										value = cell.getNumericCellValue();
										break;
									}
									case Cell.CELL_TYPE_STRING: {
										value = cell.getStringCellValue();
										break;
									}
									case Cell.CELL_TYPE_BLANK: {
										value = CoreConstants.EMPTY;
										break;
									}
									case Cell.CELL_TYPE_BOOLEAN: {
										value = cell.getBooleanCellValue();
										break;
									}
								}
							} else {
								value = cell.getStringCellValue();
							}

							buffer.append(value);
						}
						buffer.append(getValueSeparator());
					}

					records++;
					if ((records % 1000) == 0) {
						System.out.println("Importer: Reading record nr.: " + records + " from file " + getFile().getName());
					}

					list.add(buffer.toString());
					buffer = null;
				}
			}

			if (records == 0) {
				throw new NoRecordsException("No records where found in the selected file" + getFile().getAbsolutePath());
			}

			return list;
		}
		catch (FileNotFoundException ex) {
			ex.printStackTrace(System.err);
			return null;
		}
		catch (IOException ex) {
			ex.printStackTrace(System.err);
			return null;
		}
		finally {
			if (input != null) {
				try {
					input.close();
				}
				catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
}