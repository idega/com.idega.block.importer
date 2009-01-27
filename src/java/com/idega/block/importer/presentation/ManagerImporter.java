package com.idega.block.importer.presentation;

import com.idega.block.media.business.MediaBusiness;
import com.idega.core.file.data.ICFile;
import com.idega.presentation.Block;
import com.idega.presentation.IWContext;
import com.idega.user.data.Group;

public class ManagerImporter extends Block {

	
	public ManagerImporter() {
	}
		
	public void main(IWContext iwc){
		try {
			Importer importer = new Importer();
			add(importer);
			
			//IWContext iwc = IWContext.getInstance();
			Group group = iwc.getCurrentUser().getPrimaryGroup();
			ICFile homeFolder = MediaBusiness.getGroupHomeFolder(group, iwc);
			//ICFile homeFolder = iwc.getAccessController().getAdministratorUser().getHomeFolder();
			importer.setImportFolder(homeFolder);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
