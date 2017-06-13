package com.idega.block.importer.data;

import java.io.File;

import com.idega.util.CoreConstants;

public class CommaSeparatedImportFile extends GenericImportFile {

	public CommaSeparatedImportFile() {
		super();

		this.setAddNewLineAfterRecord(false);
		this.setRecordDilimiter(CoreConstants.NEWLINE);
		this.setValueSeparator(CoreConstants.COMMA);
	}

	public CommaSeparatedImportFile(File file) {
		this();

		setFile(file);
	}

}