package com.idega.block.importer.presentation;

import java.io.File;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import javax.faces.context.FacesContext;

import com.idega.block.importer.business.ImportBusiness;
import com.idega.business.IBOLookup;
import com.idega.business.IBORuntimeException;
import com.idega.core.file.data.ICFile;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.io.UploadFile;
import com.idega.presentation.IWBaseComponent;
import com.idega.presentation.IWContext;
import com.idega.presentation.Layer;
import com.idega.presentation.Span;
import com.idega.presentation.Table2;
import com.idega.presentation.TableCell2;
import com.idega.presentation.TableRow;
import com.idega.presentation.TableRowGroup;
import com.idega.presentation.text.ListItem;
import com.idega.presentation.text.Lists;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.CheckBox;
import com.idega.presentation.ui.DropdownMenu;
import com.idega.presentation.ui.FileInput;
import com.idega.presentation.ui.Form;
import com.idega.presentation.ui.HiddenInput;
import com.idega.presentation.ui.Label;
import com.idega.presentation.ui.SubmitButton;
import com.idega.util.PresentationUtil;

/**
 * Title: Importer
 * Description: This is a block for managing,importing and keeping track of your
 * import files.
 *
 * Copyright: Copyright (c) 2002
 * Company: Idega Software
 * 
 * @author <a href="mailto:eiki@idega.is">Eirikur Sveinn Hrafnsson</a>
 * @version 1.0
 */
public class Importer extends IWBaseComponent {

	private String folderPath;
	private boolean usingLocalFileSystem, importFiles = false;
	private IWResourceBundle iwrb;
	private boolean showFileUploader = false;

	private static final String ACTION_PARAMETER = "prm_action";
	private static final String IMPORT_FILES = "prm_import_files";
	private static final String IMPORT_FILE_PATHS = "prm_import_file_paths";
	private static final String IMPORT_FILE_IDS = "prm_import_file_ids";
	public static final String PARAMETER_GROUP_ID = "ic_group_id";
	public static final String PARAMETER_IMPORT_HANDLER = "prm_import_handler";
	public static final String PARAMETER_IMPORT_FILE = "prm_import_file";

	public final static String IW_BUNDLE_IDENTIFIER = "com.idega.block.importer";
	
	private Object importFileHandler;
	private Object importFileType;

	/**
	 * Method setFolderPath. Use this set method if you want to select files from the local filesystem.
	 * 
	 * @param path
	 */
	public void setLocalFolderPath(String path) {
		this.folderPath = path;
		this.usingLocalFileSystem = true;
	}

	public String getLocalFolderPath() {
		return this.folderPath;
	}

	/**
	 * @deprecated Removed IWFileSystem support...
	 * Method setImportFolder. Use this method if you want use the idegaWeb filesystem.
	 * 
	 * @param folder
	 */
	public void setImportFolder(ICFile folder) {
		//Does nothing...
	}

	private void parseAction(IWContext iwc) {
		if (iwc.isParameterSet(ACTION_PARAMETER)) {
			if (iwc.getParameter(ACTION_PARAMETER).equals(IMPORT_FILES)) {
				this.importFiles = true;
			}
		}
	}

	public void initializeComponent(FacesContext context) {
		IWContext iwc = IWContext.getIWContext(context);
		iwrb = iwc.getIWMainApplication().getBundle(getBundleIdentifier()).getResourceBundle(iwc);
		PresentationUtil.addStyleSheetToHeader(iwc, iwc.getIWMainApplication().getBundle(getBundleIdentifier()).getVirtualPathWithFileNameString("style/importer.css"));

		parseAction(iwc);
		try {
			if (importFiles) {
				importFiles(iwc);
			}

			if (this.usingLocalFileSystem) {
				showLocalFileSystemSelection(iwc);
			}
			else if (showFileUploader()) {
				showFileUploader(iwc);
			}
			else {
				add(new Text(this.iwrb.getLocalizedString("importer.no.folder.selected", "No folder is selected. Open the properties window and select a folder.")));
			}
		}
		catch (RemoteException re) {
			throw new IBORuntimeException(re);
		}
	}

	private void showFileUploader(IWContext iwc) throws RemoteException {
		Form form = new Form();
		form.setStyleClass("importerForm");
		form.setMultiPart();
		form.add(new HiddenInput(ACTION_PARAMETER, IMPORT_FILES));

		Layer section = new Layer();
		section.setStyleClass("section");
		form.add(section);
		
		FileInput chooser = new FileInput();
		
		Layer item = new Layer();
		item.setStyleClass("item");
		Label label = new Label(iwrb.getLocalizedString("importer.select_file", "Select a file to import"), chooser);
		item.add(label);
		item.add(chooser);
		section.add(item);
		
		if (getImportFileHandler() != null) {
			form.add(new HiddenInput(PARAMETER_IMPORT_HANDLER, getImportFileHandler().toString()));
		}
		else {
			DropdownMenu handler = getImportBusiness(iwc).getImportHandlers(iwc, PARAMETER_IMPORT_HANDLER);

			item = new Layer();
			item.setStyleClass("item");
			label = new Label(iwrb.getLocalizedString("importer.select.import.handler", "Select import handler"), handler);
			item.add(label);
			item.add(handler);
			section.add(item);
		}

		if (getImportFileType() != null) {
			form.add(new HiddenInput(PARAMETER_IMPORT_FILE, getImportFileType().toString()));
		}
		else {
			DropdownMenu fileType = getImportBusiness(iwc).getImportFileClasses(iwc, Importer.PARAMETER_IMPORT_FILE);
	
			item = new Layer();
			item.setStyleClass("item");
			label = new Label(iwrb.getLocalizedString("importer.select.import.file.type", "Select file type"), fileType);
			item.add(label);
			item.add(fileType);
			section.add(item);
		}
		
		Layer buttons = new Layer();
		buttons.setStyleClass("buttons");
		form.add(buttons);
		
		SubmitButton button = new SubmitButton(iwrb.getLocalizedString("confirm", "Confirm"));
		buttons.add(button);
		
		add(form);
	}

	/**
	 * Method importFiles.
	 * 
	 * @param iwc
	 */
	private void importFiles(IWContext iwc) {
		Layer layer = new Layer();
		layer.setStyleClass("importerStatus");
		
		String handler = iwc.getParameter(Importer.PARAMETER_IMPORT_HANDLER);
		String fileClass = iwc.getParameter(Importer.PARAMETER_IMPORT_FILE);

		String[] values = null;
		if (this.usingLocalFileSystem) {
			values = iwc.getParameterValues(IMPORT_FILE_PATHS);
		}
		else if (showFileUploader()) {
			if (iwc.getUploadedFile() != null) {
				UploadFile file = iwc.getUploadedFile();
				String[] temp = { file.getAbsolutePath() };
				values = temp;
			}
		}
		else {
			values = iwc.getParameterValues(IMPORT_FILE_IDS);
		}

		if (values != null) {
			for (int i = 0; i < values.length; i++) {
				boolean success = false;
				String path = values[i];
				
				Layer fileStatus = new Layer();
				fileStatus.setStyleClass("fileStatus");
				layer.add(fileStatus);

				List failedRecords = new ArrayList();
				try {
					success = getImportBusiness(iwc).importRecords(handler, fileClass, path, iwc, failedRecords);
				}
				catch (RemoteException re) {
					re.printStackTrace();
				}
				
				String status = null;
				if (!success) {
					status = this.iwrb.getLocalizedString("importer.failure", "Failed");
					fileStatus.setStyleClass("failed");
				}
				else if (failedRecords.size() != 0) {
					status = this.iwrb.getLocalizedString("importer.not_all imported", "Not all records imported");
					fileStatus.setStyleClass("notAllImported");
				}
				else {
					status = this.iwrb.getLocalizedString("importer.success", "Success");
					fileStatus.setStyleClass("success");
				}
				
				Layer item = new Layer();
				item.setStyleClass("statusItem");
				Label label = new Label();
				label.setLabel(iwrb.getLocalizedString("file.name", "File name"));
				item.add(label);
				item.add(new Span(new Text(path.substring(path.lastIndexOf(com.idega.util.FileUtil.getFileSeparator()) + 1, path.length()))));
				fileStatus.add(item);
				
				item = new Layer();
				item.setStyleClass("statusItem");
				label = new Label();
				label.setLabel(iwrb.getLocalizedString("file.status", "Status"));
				item.add(label);
				item.add(new Span(new Text(status)));
				fileStatus.add(item);
				
				if (failedRecords.size() != 0) {
					item = new Layer();
					item.setStyleClass("statusItem");
					label = new Label();
					label.setLabel(iwrb.getLocalizedString("importer.number_of_failed_records", "Number of failed records"));
					item.add(label);
					item.add(new Span(new Text(String.valueOf(failedRecords.size()))));
					fileStatus.add(item);

					Lists list = new Lists();
					fileStatus.add(list);
					for (int j = 0; j < failedRecords.size(); j++) {
						ListItem listItem = new ListItem();
						listItem.add(new Text(String.valueOf(failedRecords.get(j))));
						list.add(listItem);
					}
				}
			}
		}
		else {
			layer.add(new Text(this.iwrb.getLocalizedString("importer.failure", "Failed") + " - " + this.iwrb.getLocalizedString("importer.no.file.selected", "No file selected!")));
			layer.setStyleClass("failed");
		}

		add(layer);
	}

	/**
	 * Method showLocalFileSystemSelection.
	 * 
	 * @param iwc
	 */
	private void showLocalFileSystemSelection(IWContext iwc) throws RemoteException {
		Form form = new Form();
		form.setStyleClass("importerForm");
		form.add(new HiddenInput(ACTION_PARAMETER, IMPORT_FILES));

		File folder = new File(getLocalFolderPath());
		if (folder.isDirectory()) {
			File[] files = folder.listFiles();

			Table2 table = new Table2();
			table.setCellpadding(0);
			table.setCellspacing(0);
			table.setWidth("100%");
			form.add(table);
			
			TableRowGroup group = table.createHeaderRowGroup();
			TableRow row = group.createRow();
			
			TableCell2 cell = row.createHeaderCell();
			cell.setStyleClass("firstColumn");
			cell.add(new Text(iwrb.getLocalizedString("file.name", "File name")));
			
			cell = row.createHeaderCell();
			cell.setStyleClass("lastColumn");
			cell.add(Text.getNonBrakingSpace());
			
			group = table.createBodyRowGroup();
			
			for (int i = 0; i < files.length; i++) {
				if (!files[i].isDirectory()) {
					row = group.createRow();
					row.setStyleClass(((i + 1) % 2) == 0 ? "even" : "odd");
					
					CheckBox box = new CheckBox(Importer.IMPORT_FILE_PATHS, files[i].getAbsolutePath());
					
					cell = row.createCell();
					cell.setStyleClass("firstColumn");
					cell.add(new Text(files[i].getName()));
					
					cell = row.createCell();
					cell.setStyleClass("lastColumn");
					cell.add(box);

					/*try {
						addReportInfo(iwc, files[i].getName(), fileTable, 3, i + 2);
					}
					catch (CreateException ce) {
						ce.printStackTrace();
					}
					catch (RemoteException re) {
						re.printStackTrace();
					}*/
				}
			}
			
			Layer section = new Layer();
			section.setStyleClass("section");
			form.add(section);
			
			if (getImportFileHandler() != null) {
				form.add(new HiddenInput(PARAMETER_IMPORT_HANDLER, getImportFileHandler().toString()));
			}
			else {
				DropdownMenu handler = getImportBusiness(iwc).getImportHandlers(iwc, PARAMETER_IMPORT_HANDLER);

				Layer item = new Layer();
				item.setStyleClass("item");
				Label label = new Label(iwrb.getLocalizedString("importer.select.import.handler", "Select import handler"), handler);
				item.add(label);
				item.add(handler);
				section.add(item);
			}

			if (getImportFileType() != null) {
				form.add(new HiddenInput(PARAMETER_IMPORT_FILE, getImportFileType().toString()));
			}
			else {
				DropdownMenu fileType = getImportBusiness(iwc).getImportFileClasses(iwc, Importer.PARAMETER_IMPORT_FILE);
		
				Layer item = new Layer();
				item.setStyleClass("item");
				Label label = new Label(iwrb.getLocalizedString("importer.select.import.file.type", "Select file type"), fileType);
				item.add(label);
				item.add(fileType);
				section.add(item);
			}
			
			Layer buttons = new Layer();
			buttons.setStyleClass("buttons");
			form.add(buttons);
			
			SubmitButton button = new SubmitButton(iwrb.getLocalizedString("confirm", "Confirm"));
			buttons.add(button);
			
		}
		else {
			form.add(new Text(iwrb.getLocalizedString("importer.nosuchfolder", "No such folder.")));
		}
		
		add(form);
	}

	/*private void addReportInfo(IWContext iwc, String fileName, Table fileTable, int column, int row) throws RemoteException, CreateException {
		ICFile reportFile = getImportBusiness(iwc).getReportFolder(fileName, false);
		if (reportFile != null) {
			Iterator reports = reportFile.getChildrenIterator();
			ICFile report;
			if (reports != null && reports.hasNext()) {
				while (reports.hasNext()) {
					report = (ICFile) reports.next();
					Link link = new Link(((Integer) report.getPrimaryKey()).intValue());
					link.setText(report.getName());
					link.setOutgoing(true);

					fileTable.add(link, column, row);
				}
			}
			else if (reports != null) {
				// Backwards thingy
				if (reportFile != null) {
					fileTable.add(new Text(reportFile.getName() + " available"), column, row);
				}
			}
		}
	}*/

	public ImportBusiness getImportBusiness(IWContext iwc) throws RemoteException {
		return (ImportBusiness) IBOLookup.getServiceInstance(iwc, ImportBusiness.class);
	}

	/**
	 * @see com.idega.presentation.PresentationObject#getBundleIdentifier()
	 */
	public String getBundleIdentifier() {
		return IW_BUNDLE_IDENTIFIER;
	}

	public boolean showFileUploader() {
		return this.showFileUploader;
	}
	
	public void setShowFileUploader(boolean showUploader) {
		this.showFileUploader = showUploader;
	}

	
	/**
	 * @return the importFileHandler
	 */
	public Object getImportFileHandler() {
		return this.importFileHandler;
	}

	
	/**
	 * @param importFileHandler the importFileHandler to set
	 */
	public void setImportFileHandler(Object importFileHandler) {
		this.importFileHandler = importFileHandler;
	}

	
	/**
	 * @return the importFileType
	 */
	public Object getImportFileType() {
		return this.importFileType;
	}

	
	/**
	 * @param importFileType the importFileType to set
	 */
	public void setImportFileType(Object importFileType) {
		this.importFileType = importFileType;
	}
}