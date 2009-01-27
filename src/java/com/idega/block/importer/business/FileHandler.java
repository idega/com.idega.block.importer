/**
 * 
 */
package com.idega.block.importer.business;

import java.rmi.RemoteException;
import java.util.List;

import com.idega.business.IBOLookup;
import com.idega.business.IBOLookupException;
import com.idega.business.IBORuntimeException;
import com.idega.core.builder.presentation.ICPropertyHandler;
import com.idega.presentation.IWContext;
import com.idega.presentation.PresentationObject;
import com.idega.presentation.ui.DropdownMenu;


/**
 * <p>
 * TODO laddi Describe Type FileClassHandler
 * </p>
 *  Last modified: $Date: 2009/01/27 11:55:09 $ by $Author: laddi $
 * 
 * @author <a href="mailto:laddi@idega.com">laddi</a>
 * @version $Revision: 1.1 $
 */
public class FileHandler implements ICPropertyHandler {

	/* (non-Javadoc)
	 * @see com.idega.core.builder.presentation.ICPropertyHandler#getDefaultHandlerTypes()
	 */
	public List<?> getDefaultHandlerTypes() {
		return null;
	}

	/* (non-Javadoc)
	 * @see com.idega.core.builder.presentation.ICPropertyHandler#getHandlerObject(java.lang.String, java.lang.String, com.idega.presentation.IWContext, boolean, java.lang.String, java.lang.String)
	 */
	public PresentationObject getHandlerObject(String name, String stringValue, IWContext iwc, boolean oldGenerationHandler, String instanceId, String method) {
		try {
			DropdownMenu menu = getImportBusiness(iwc).getImportHandlers(iwc, name);
			menu.setSelectedElement(stringValue);
			return menu;
		}
		catch (RemoteException re) {
			throw new IBORuntimeException(re);
		}
	}

	/* (non-Javadoc)
	 * @see com.idega.core.builder.presentation.ICPropertyHandler#onUpdate(java.lang.String[], com.idega.presentation.IWContext)
	 */
	public void onUpdate(String[] values, IWContext iwc) {
	}

	private ImportBusiness getImportBusiness(IWContext iwc) {
		try {
			return (ImportBusiness) IBOLookup.getServiceInstance(iwc, ImportBusiness.class);
		}
		catch (IBOLookupException ile) {
			throw new IBORuntimeException(ile);
		}
	}
}