package com.idega.block.importer.data;

import java.rmi.RemoteException;
import javax.ejb.FinderException;
import java.util.Iterator;
import java.util.Collection;
import com.idega.data.*;
import java.sql.SQLException;


/**
 * Title:        com.idega.block.importer.data.ImportHandlerBMPBean
 * Description: A table of available Import handlers
 * Copyright:    Idega Software (c) 2002
 * Company:      Idega Software http://www.idega.com
 * @author <a href="mailto:eiki@idega.is">Eirikur S. Hrafnsson</a>
 * @version 1.0
 */

public class ImportHandlerBMPBean extends com.idega.data.GenericEntity implements ImportHandler {


  public ImportHandlerBMPBean() {
    super();
  }

  public ImportHandlerBMPBean(int id) throws SQLException {
    super(id);
  }


  public void initializeAttributes() {
    this.addAttribute(this.getIDColumnName());
    this.addAttribute(getNameColumnName(),"Name",true,true,"java.lang.String");
    this.addAttribute(getClassColumnName(),"Class name",true,true,"java.lang.String",500);
    this.addAttribute(getDescriptionColumnName(),"Description",true,true,"java.lang.String",500);
  }
  public String getEntityName() {
    return "im_handler";
  }

  public static String getNameColumnName(){
    return "name";
  }
  public static String getClassColumnName(){
      return "class_name";
  }

  public static String getDescriptionColumnName(){
    return "description";
  }

  public void setName(String name){
    this.setColumn(getNameColumnName(),name);
  }

  public void setDescription(String description){
    this.setColumn(getDescriptionColumnName(),description);
  }

  public String getName(){
    return this.getStringColumnValue(getNameColumnName());
  }

  public String getDescription(){
    return this.getStringColumnValue(getDescriptionColumnName());
  }
  
    public void setClassName(String className){
    this.setColumn(getClassColumnName(),className);
  }
  
  public String getClassName(){
    return this.getStringColumnValue(getClassColumnName());
  }
  
  public void insertStartData() throws SQLException {
	//temporary remove also these should be the interfaces
	try{
		ImportHandler nacka = ((ImportHandlerHome)IDOLookup.getHome(ImportHandler.class)).create();
		nacka.setName("Nacka citizen importer");
		nacka.setDescription("Imports the KIR data for Nacka.");
		nacka.setClassName("se.idega.idegaweb.commune.block.importer.business.NackaImportFileHandlerBean");
      	nacka.store();
    }
    catch (Exception ex) {
      ex.printStackTrace();
    }
    
	try{
		ImportHandler nacka2 = ((ImportHandlerHome)IDOLookup.getHome(ImportHandler.class)).create();
		nacka2.setName("Nacka student importer");
		nacka2.setDescription("Imports the students in Nacka");
		nacka2.setClassName("se.idega.idegaweb.commune.block.importer.business.NackaStudentImportFileHandlerBean");
      	nacka2.store();
    }
    catch (Exception ex) {
      ex.printStackTrace();
    }
    
	try{
		ImportHandler KR = ((ImportHandlerHome)IDOLookup.getHome(ImportHandler.class)).create();
		KR.setName("KR data importer");
		KR.setDescription("Les inn gogn kn.d. KR.");
		KR.setClassName("is.idega.idegaweb.member.business.KRImportFileHandlerBean");
      	KR.store();
    }
    catch (Exception ex) {
      ex.printStackTrace();
    }
	
  }

  public Collection ejbFindAllImportHandlers()throws FinderException{
    return super.idoFindAllIDsBySQL();
  }

}