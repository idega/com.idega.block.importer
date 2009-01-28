/**
 * 
 */
package com.idega.block.importer.business;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.idega.block.importer.data.ImportFile;
import com.idega.user.data.Group;


/**
 * <p>
 * TODO laddi Describe Type AddressImportFileHandlerBean
 * </p>
 *  Last modified: $Date: 2009/01/28 10:44:44 $ by $Author: laddi $
 * 
 * @author <a href="mailto:laddi@idega.com">laddi</a>
 * @version $Revision: 1.1 $
 */
@Scope("singleton")
@Service("streetNameImporter")
public class StreetNameImportFileHandler implements ImportFileHandler {

	ImportFile importFile = null;
	List failedRecords;
	
	public boolean handleRecords() throws RemoteException {
		int counter = 0; 
		String record;
		while (!(record = (String) this.importFile.getNextRecord()).equals("")) {
			try {
				ArrayList values = this.importFile.getValuesFromRecordString(record);
				String name = (String) values.get(0);
				Integer code = new Integer(Double.valueOf((String) values.get(1)).intValue());
				String area = values.size() > 2 ? (String) values.get(2) : null;
				counter++;
				
				System.out.println("name = " + name + ", code = " + code);
				
				if (counter % 50 == 0) {
					System.out.println("[StreetNameImportFileHandler]: "+counter+" records imported");
				}
			}
			catch (NumberFormatException nfe) {
				if (failedRecords == null) {
					failedRecords = new ArrayList();
				}
				
				failedRecords.add(this.importFile.getValuesFromRecordString(record).toString());
			}
		}
		System.out.println("[StreetNameImportFileHandler]: "+counter+" records imported");
		
		return true;
	}
	
	public void setImportFile(ImportFile file) throws RemoteException {
		this.importFile = file;
	}
	
	public void setRootGroup(Group rootGroup) throws RemoteException {
	}
	
	public List getFailedRecords() throws RemoteException {
		return failedRecords;
	}
}