package com.idega.block.importer.business;


import javax.ejb.CreateException;
import com.idega.business.IBOHomeImpl;

public class ImportBusinessHomeImpl extends IBOHomeImpl implements ImportBusinessHome {

	public Class getBeanInterfaceClass() {
		return ImportBusiness.class;
	}

	public ImportBusiness create() throws CreateException {
		return (ImportBusiness) super.createIBO();
	}
}