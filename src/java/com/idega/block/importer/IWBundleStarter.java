package com.idega.block.importer;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import javax.ejb.CreateException;
import javax.ejb.FinderException;

import org.sadun.util.polling.DirectoryPoller;

import com.idega.block.importer.business.AddressCoordinateImportHandler;
import com.idega.block.importer.business.AutoImportPollManager;
import com.idega.block.importer.data.ColumnSeparatedImportFile;
import com.idega.block.importer.data.ExcelImportFile;
import com.idega.block.importer.data.GenericImportFile;
import com.idega.block.importer.data.ImportFileClass;
import com.idega.block.importer.data.ImportFileClassHome;
import com.idega.block.importer.data.ImportHandler;
import com.idega.block.importer.data.ImportHandlerHome;
import com.idega.block.importer.presentation.ManagerImporter;
import com.idega.business.IBOLookupException;
import com.idega.core.view.ViewNode;
import com.idega.data.IDOLookup;
import com.idega.data.IDOLookupException;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWBundleStartable;
import com.idega.manager.view.ManagerViewManager;
import com.idega.repository.data.RefactorClassRegistry;
import com.idega.workspace.view.WorkspaceClassViewNode;

/**
 * Activats pollers for automatic imports configured by the user (AutoImporter).
 * Copyright: Copyright (c) 2004 Company: idega software
 * 
 * @author Joakim@idega.is
 * @see com.idega.block.importer.presentation.AutoImporter
 */
public class IWBundleStarter implements IWBundleStartable {

	private static HashMap pollers = new HashMap();

	/**
	 * Starts all the pollers for automatic imports
	 * 
	 * @see com.idega.idegaweb.IWBundleStartable#start(com.idega.idegaweb.IWBundle)
	 */
	public void start(IWBundle starterBundle) {
		RefactorClassRegistry rfregistry = RefactorClassRegistry.getInstance();
		rfregistry.registerRefactoredClass("com.idega.core.location.business.AddressCoordinateImportHandler", AddressCoordinateImportHandler.class);
		addStartData();

		System.out.println("Activating pollers for automatic imports");
		try {
			Collection coll = ((ImportHandlerHome) IDOLookup.getHome(ImportHandler.class)).findAllAutomaticUpdates();
			Iterator iter = coll.iterator();
			while (iter.hasNext()) {
				ImportHandler importHandler = (ImportHandler) iter.next();
				addPoller(importHandler);
			}
		}
		catch (IDOLookupException e) {
			System.out.println("WARNING: Could not start the pollers for automatic imports");
			e.printStackTrace();
		}
		catch (FinderException e) {
			System.out.println("WARNING: Could not start the pollers for automatic imports");
			e.printStackTrace();
		}
		catch (IBOLookupException e) {
			System.out.println("WARNING: Could not start the pollers for automatic imports");
			e.printStackTrace();
		}
		catch (ClassNotFoundException e) {
			System.out.println("WARNING: Could not start the pollers for automatic imports");
			e.printStackTrace();
		}
		registerViewNodes(starterBundle);
	}

	private void registerViewNodes(IWBundle starterBundle) {

		ManagerViewManager managerView = ManagerViewManager.getInstance(starterBundle.getApplication());
		ViewNode managerNode = managerView.getManagerNode();
		WorkspaceClassViewNode importerNode = new WorkspaceClassViewNode("dataimport", managerNode);
		importerNode.setName("Data Import");
		importerNode.setComponentClass(ManagerImporter.class);
		importerNode.setMaximizeBlockVertically(true);

	}

	/**
	 * Stops all the pollers for automatic imports
	 * 
	 * @see com.idega.idegaweb.IWBundleStartable#stop(com.idega.idegaweb.IWBundle)
	 */
	public void stop(IWBundle starterBundle) {
		System.out.println("Shutting down pollers");
		Iterator iter = pollers.values().iterator();
		while (iter.hasNext()) {
			DirectoryPoller poller = (DirectoryPoller) iter.next();
			poller.shutdown();
		}
	}

	/**
	 * Helper function to stop a specific poller
	 * 
	 * @param handlerClassName
	 */
	public static void shutdown(String handlerClassName) {
		System.out.println("Shutting down poller:" + handlerClassName);
		DirectoryPoller poller = (DirectoryPoller) pollers.get(handlerClassName);
		if (null != poller) {
			poller.shutdown();
			pollers.remove(handlerClassName);
		}
		else {
			System.out.println("WARNING: Could not find the specified poller");
		}
	}

	/**
	 * Adds a new poller
	 * 
	 * @param importHandler
	 * @throws ClassNotFoundException
	 * @throws IBOLookupException
	 */
	public static void addPoller(ImportHandler importHandler) throws IBOLookupException, ClassNotFoundException {
		File autoImpFolder = new File(importHandler.getAutoImpFolder());
		if (autoImpFolder.isDirectory()) {
			DirectoryPoller poller = new DirectoryPoller(autoImpFolder);
			poller.setAutoMove(true); // Moves the files to a subfolder before
			// handling
			poller.addPollManager(new AutoImportPollManager(importHandler.getClassName(), importHandler.getAutoImpFileType()));
			poller.setPollInterval(10 * 60 * 1000);
			poller.start();
			pollers.put(importHandler.getClassName(), poller);
			System.out.println("Starting automatic import poller: " + importHandler.getName() + " for folder " + importHandler.getAutoImpFolder());
		}
		else {
			System.out.println("WARNING: The configured folder '" + autoImpFolder + "' could not be found. Automatic import not started!");
		}
	}
	
	private void addStartData() {
		try {
			ImportFileClassHome home = (ImportFileClassHome) IDOLookup.getHome(ImportFileClass.class);
			
			try {
				home.findByClassName(GenericImportFile.class.getName());
			}
			catch (FinderException fe) {
				try {
					ImportFileClass generic = home.create();
					generic.setName("Generic import file");
					generic.setDescription("A generic file reader. Reads both column based and row based record files. Adjustible through some properties. The default is reading a column based file where each record is separated with a new line character (\n) and each value is separated by a semi colon (;). ");
					generic.setClassName(GenericImportFile.class.getName());
					generic.store();
				}
				catch (CreateException ce) {
					ce.printStackTrace();
				}
			}
	
			try {
				home.findByClassName(ColumnSeparatedImportFile.class.getName());
			}
			catch (Exception ex) {
				try {
					ImportFileClass column = ((ImportFileClassHome) IDOLookup.getHome(ImportFileClass.class)).create();
					column.setName("Column separated file");
					column.setDescription("A column separated file reader. By default each record is separated with a new line character (\n) and each value is separated by a semi colon (;) but it can be adjusted by properties.");
					column.setClassName(ColumnSeparatedImportFile.class.getName());
					column.store();
				}
				catch (CreateException ce) {
					ce.printStackTrace();
				}
			}
			
			try {
				home.findByClassName(ExcelImportFile.class.getName());
			}
			catch (Exception ex) {
				try {
					ImportFileClass column = ((ImportFileClassHome) IDOLookup.getHome(ImportFileClass.class)).create();
					column.setName("Excel file");
					column.setDescription("An excel file reader. By default each record is separated with a new line character (\n) and each value is separated by a semi colon (;).");
					column.setClassName(ExcelImportFile.class.getName());
					column.store();
				}
				catch (CreateException ce) {
					ce.printStackTrace();
				}
			}
		}
		catch (IDOLookupException ile) {
			ile.printStackTrace();
		}
	}
}
