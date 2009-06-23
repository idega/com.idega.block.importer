package com.idega.block.importer.business;


import com.idega.core.file.data.ICFile;
import com.idega.presentation.IWContext;
import com.idega.user.business.GroupBusiness;
import com.idega.presentation.ui.DropdownMenu;
import javax.ejb.CreateException;
import java.rmi.RemoteException;
import java.util.Collection;
import com.idega.business.IBOService;
import java.util.List;
import java.io.File;
import com.idega.idegaweb.IWUserContext;
import com.idega.block.importer.data.ImportFile;

public interface ImportBusiness extends IBOService {

	/**
	 * @see com.idega.block.importer.business.ImportBusinessBean#getImportHandlers
	 */
	public Collection getImportHandlers() throws RemoteException, RemoteException;

	/**
	 * @see com.idega.block.importer.business.ImportBusinessBean#getImportFileTypes
	 */
	public Collection getImportFileTypes() throws RemoteException, RemoteException;

	/**
	 * @see com.idega.block.importer.business.ImportBusinessBean#importRecords
	 */
	public boolean importRecords(String handlerClass, String fileClass, String filePath, Integer groupId, IWUserContext iwuc, List failedRecords) throws RemoteException, RemoteException;

	/**
	 * @see com.idega.block.importer.business.ImportBusinessBean#importRecords
	 */
	public boolean importRecords(String handlerClass, String fileClass, String filePath, Integer groupId, IWUserContext iwuc, List failedRecords, List successRecords) throws RemoteException, RemoteException;

	/**
	 * @see com.idega.block.importer.business.ImportBusinessBean#importRecords
	 */
	public boolean importRecords(String handlerClass, String fileClass, String filePath, IWUserContext iwuc, List failedRecords) throws RemoteException, RemoteException;

	/**
	 * @see com.idega.block.importer.business.ImportBusinessBean#importRecords
	 */
	public boolean importRecords(String handlerClass, String fileClass, String filePath, IWUserContext iwuc, List failedRecords, List successRecords) throws RemoteException, RemoteException;

	/**
	 * @see com.idega.block.importer.business.ImportBusinessBean#getGroupBusiness
	 */
	public GroupBusiness getGroupBusiness() throws Exception, RemoteException;

	/**
	 * @see com.idega.block.importer.business.ImportBusinessBean#getImportFileHandler
	 */
	public ImportFileHandler getImportFileHandler(String handlerClass, IWUserContext iwuc) throws Exception, RemoteException;

	/**
	 * @see com.idega.block.importer.business.ImportBusinessBean#getImportFile
	 */
	public ImportFile getImportFile(String fileClass) throws Exception, RemoteException;

	/**
	 * @see com.idega.block.importer.business.ImportBusinessBean#getImportHandlers
	 */
	public DropdownMenu getImportHandlers(IWContext iwc, String name) throws RemoteException, RemoteException;

	/**
	 * @see com.idega.block.importer.business.ImportBusinessBean#getImportFileClasses
	 */
	public DropdownMenu getImportFileClasses(IWContext iwc, String name) throws RemoteException, RemoteException;

	/**
	 * @see com.idega.block.importer.business.ImportBusinessBean#getReportFolder
	 */
	public ICFile getReportFolder(String importFileName, boolean createIfNotFound) throws RemoteException, CreateException, RemoteException;

	/**
	 * @see com.idega.block.importer.business.ImportBusinessBean#addReport
	 */
	public void addReport(File importFile, File reportFile) throws RemoteException, CreateException, RemoteException;

	/**
	 * @see com.idega.block.importer.business.ImportBusinessBean#addReport
	 */
	public void addReport(File importFile, String name, Collection data, String separator) throws RemoteException, CreateException, RemoteException;

	/**
	 * @see com.idega.block.importer.business.ImportBusinessBean#addExcelReport
	 */
	public void addExcelReport(File importFile, String name, Collection data, String separator) throws RemoteException, CreateException, RemoteException;

	/**
	 * @see com.idega.block.importer.business.ImportBusinessBean#getReport
	 */
	public File getReport(String name, Collection data, String separator) throws RemoteException;

	/**
	 * @see com.idega.block.importer.business.ImportBusinessBean#getExcelReport
	 */
	public File getExcelReport(String name, Collection data, String separator) throws RemoteException;
}