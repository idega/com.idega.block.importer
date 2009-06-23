package com.idega.block.importer.business;


import javax.ejb.CreateException;
import com.idega.business.IBOHome;
import java.rmi.RemoteException;

public interface ImportBusinessHome extends IBOHome {

	public ImportBusiness create() throws CreateException, RemoteException;
}