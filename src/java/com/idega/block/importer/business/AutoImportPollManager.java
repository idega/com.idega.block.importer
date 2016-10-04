package com.idega.block.importer.business;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.sadun.util.polling.BasePollManager;
import org.sadun.util.polling.FileFoundEvent;
import org.sadun.util.polling.FileSetFoundEvent;

import com.idega.block.importer.data.ImportFile;
import com.idega.business.IBOLookup;
import com.idega.business.IBOLookupException;
import com.idega.business.IBOService;
import com.idega.idegaweb.IWMainApplication;
import com.idega.repository.data.RefactorClassRegistry;

/**
 * AutoImportPollManager handles the action when file(s) are found in folders for automatic imports
 * One AutoImportPollManager is created for each folder to be polled from.
 * It basically calls the specified importer class with the files found in the folder.
 *
 * Copyright:    Copyright (c) 2004
 * Company:      idega software
 * @author Joakim@idega.is
 */
public class AutoImportPollManager extends BasePollManager {

	private static final Logger LOGGER = Logger.getLogger(AutoImportPollManager.class.getName());

	private String fileClass;
	private ImportFileHandler handler;

	public AutoImportPollManager(String importerClass, String fc) throws IBOLookupException, ClassNotFoundException {
		this.fileClass = fc;
		this.handler = getImportFileHandler(importerClass);
	}

	/**
	 * Implemented interface (callback)
	 * @see org.sadun.util.polling.BasePollManager
	 */
	@Override
	public void fileFound(FileFoundEvent evt){
		File file = evt.getFile();
		processFile(file);

	}

	/**
	 * Implemented interface (callback)
	 * @see org.sadun.util.polling.BasePollManager
	 */
	@Override
	public void fileSetFound(FileSetFoundEvent evt){
		File[] files = evt.getFiles();
		for(int i=0;i<files.length;i++){
			processFile(files[i]);
		}
	}

	/**
	 * Calls the import and creates a report file if needed for the
	 * @param filePath
	 */
	private void processFile(File filePath) {
		try {
			ImportFile file = (ImportFile) RefactorClassRegistry.forName(this.fileClass).newInstance();
			file.setFile(filePath);
			this.handler.setImportFile(file);

			this.handler.handleRecords();
			createReport(this.handler, filePath);
			filePath.delete();
		} catch (RemoteException e) {
			LOGGER.log(Level.WARNING, "Automatic import of " + filePath + " did not succeed", e);
		} catch (NoRecordsException e) {
			LOGGER.log(Level.WARNING, "Automatic import of " + filePath + " did not succeed", e);
		} catch (Exception e) {
			LOGGER.log(Level.WARNING, "Automatic import of " + filePath + " did not succeed", e);
		}
	}

	/**
	 * Creates a report under ../Reports/ with the same name as the import file
	 * The report contains all the lines that did not execute OK
	 * @param handler
	 * @param path
	 */
	private void createReport(ImportFileHandler handler, File path) {
		try {
			List<String> failedRecords = handler.getFailedRecords();
			if (failedRecords.size() > 0) {
				String pathString = path.toString();
				int folderPointer = pathString.lastIndexOf('/');
				folderPointer = Math.max(folderPointer, pathString.lastIndexOf('\\'));
				String fileName = pathString.substring(folderPointer+1);
				pathString = pathString.substring(0, folderPointer);
				folderPointer = pathString.lastIndexOf('/') + 1;
				folderPointer = Math.max(folderPointer, pathString.lastIndexOf('\\') + 1);

				LOGGER.info("folderPointer = " + folderPointer);
				String reportPathString = pathString
						.substring(0, folderPointer)
						+ "Reports/";
				LOGGER.info("reportPathString = " + reportPathString);
				String filePath = reportPathString + fileName;
				File reportPath = new File(reportPathString);
				if (!reportPath.exists()) {
					LOGGER.info("ReportPath not existing, trying to create");
					if(!reportPath.mkdir()){
						LOGGER.info("Could not create the report folder. No import reports can be created!");
						return;
					}
				}
				LOGGER.info("pathString = " + filePath);
				try {
					BufferedWriter out = new BufferedWriter(new FileWriter(filePath));

					Iterator<String> iter = failedRecords.iterator();
					while (iter.hasNext()) {
						String line = iter.next();
						out.write(line + '\n');
					}
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (IndexOutOfBoundsException e){
					e.printStackTrace();
				}
			}
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

  	public ImportFileHandler getImportFileHandler(String handlerClass) throws ClassNotFoundException, IBOLookupException {
  		Class<?> importHandlerInterfaceClass = RefactorClassRegistry.forName(handlerClass);
		@SuppressWarnings("unchecked")
		ImportFileHandler handler = (ImportFileHandler) getServiceInstance((Class<IBOService>) importHandlerInterfaceClass);
	    return handler;
  	}

    /**
     * Get an instance of the service bean specified by serviceClass
     */
	protected <T extends IBOService> T getServiceInstance(Class<T> serviceClass)throws IBOLookupException{
		return IBOLookup.getServiceInstance(IWMainApplication.getDefaultIWMainApplication().getIWApplicationContext(), serviceClass);
    }
}