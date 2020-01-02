package com.idega.block.importer.business;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.idega.block.importer.data.ImportFile;
import com.idega.business.IBOLookup;
import com.idega.core.location.business.AddressBusiness;
import com.idega.core.location.data.Commune;
import com.idega.core.location.data.Country;
import com.idega.core.location.data.PostalCode;
import com.idega.idegaweb.IWMainApplication;
import com.idega.user.data.Group;
import com.idega.util.ListUtil;
import com.idega.util.StringHandler;
import com.idega.util.StringUtil;

@Scope("session")
@Service("postalCodeImporter")
public class PostalCodeImporter implements ImportFileHandler {

	private ImportFile importFile = null;

	private List<String> failedRecords, successRecords;

	@Override
	public boolean handleRecords() throws RemoteException {
		failedRecords = new ArrayList<>();
		successRecords = new ArrayList<>();

		AddressBusiness addressBusiness = IBOLookup.getServiceInstance(IWMainApplication.getDefaultIWApplicationContext(), AddressBusiness.class);

		String record;
		while (!(record = (String) this.importFile.getNextRecord()).equals("")) {
			try {
				List<String> values = this.importFile.getValuesFromRecordString(record);
				if (ListUtil.isEmpty(values) || values.size() < 4) {
					failedRecords.add(record);
					continue;
				}

				String postalCode = values.get(0);
				String name = values.get(1);
				String countryId = values.get(2);
				String postalAddress = values.get(3);
				postalCode = StringUtil.isEmpty(postalCode) ? postalCode : postalCode.trim();
				name = StringUtil.isEmpty(name) ? name : name.trim();
				countryId = StringUtil.isEmpty(countryId) ? countryId : countryId.trim();
				postalAddress = StringUtil.isEmpty(postalAddress) ? postalAddress : postalAddress.trim();
				if (StringUtil.isEmpty(postalCode) || StringUtil.isEmpty(name) || !StringHandler.isNumeric(countryId) || StringUtil.isEmpty(postalAddress)) {
					failedRecords.add(record);
					continue;
				}

				Country country = null;
				try {
					country = addressBusiness.getCountryHome().findByPrimaryKey(Integer.valueOf(countryId));
				} catch (Exception e) {}
				PostalCode pc = null;
				if (country == null) {
					pc = addressBusiness.getPostalCodeAndCreateIfDoesNotExist(postalCode, name);
				} else {
					pc = addressBusiness.getPostalCodeAndCreateIfDoesNotExist(postalCode, name, country);
				}
				if (pc == null) {
					failedRecords.add(record);
					continue;
				}

				Commune commune = addressBusiness.getCommuneAndCreateIfDoesNotExist(name, null);
				pc.setCommune(commune);
				pc.setConvertToUpperCase(false);
				pc.setName(name);
				pc.setPostalCode(postalCode);
				pc.store();

				successRecords.add(record);
			} catch (Exception e) {
				Logger.getLogger(getClass().getName()).log(Level.WARNING, "Error process record '" + record + "'", e);
				failedRecords.add(this.importFile.getValuesFromRecordString(record).toString());
			}
		}

		return true;
	}

	@Override
	public void setImportFile(ImportFile file) throws RemoteException {
		this.importFile = file;
	}

	@Override
	public void setRootGroup(Group rootGroup) throws RemoteException {
	}

	@Override
	public <F> List<F> getFailedRecords() throws RemoteException {
		@SuppressWarnings("unchecked")
		List<F> result = (List<F>) failedRecords;
		return result;
	}

	@Override
	public <S> List<S> getSuccessRecords() throws RemoteException {
		@SuppressWarnings("unchecked")
		List<S> result = (List<S>) successRecords;
		return result;
	}

}