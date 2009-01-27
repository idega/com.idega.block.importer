package com.idega.block.importer.data;

import java.sql.SQLException;
import java.util.Collection;

import javax.ejb.FinderException;

import com.idega.data.IDOQuery;

/**
 * Title: com.idega.block.importer.data.ImportFileClassBMPBean
 * Description: A table of available Import handlers Copyright: Idega Software (c) 2002
 * Company: Idega Software http://www.idega.com
 * 
 * @author <a href="mailto:eiki@idega.is">Eirikur S. Hrafnsson</a>
 * @version 1.0
 */

public class ImportFileClassBMPBean extends com.idega.data.GenericEntity implements ImportFileClass {

	public ImportFileClassBMPBean() {
		super();
	}

	public ImportFileClassBMPBean(int id) throws SQLException {
		super(id);
	}

	public void initializeAttributes() {
		this.addAttribute(this.getIDColumnName());
		this.addAttribute(getNameColumnName(), "Name", true, true, "java.lang.String");
		this.addAttribute(getClassColumnName(), "Class name", true, true, "java.lang.String", 500);
		this.addAttribute(getDescriptionColumnName(), "Description", true, true, "java.lang.String", 1000);
	}

	public String getEntityName() {
		return "im_file_class";
	}

	public static String getNameColumnName() {
		return "name";
	}

	public static String getClassColumnName() {
		return "class_name";
	}

	public static String getDescriptionColumnName() {
		return "description";
	}

	public void setName(String name) {
		this.setColumn(getNameColumnName(), name);
	}

	public String getName() {
		return this.getStringColumnValue(getNameColumnName());
	}

	public void setDescription(String description) {
		this.setColumn(getDescriptionColumnName(), description);
	}

	public String getDescription() {
		return this.getStringColumnValue(getDescriptionColumnName());
	}

	public void setClassName(String className) {
		this.setColumn(getClassColumnName(), className);
	}

	public String getClassName() {
		return this.getStringColumnValue(getClassColumnName());
	}

	public void insertStartData() throws SQLException {
	}

	public Collection ejbFindAllImportFileClasses() throws FinderException {
		return super.idoFindAllIDsBySQL();
	}

	public Integer ejbFindByClassName(String className) throws FinderException {
		IDOQuery query = idoQuery();
		query.appendSelectAllFrom(this).appendWhereEqualsQuoted(getClassColumnName(), className);
		return (Integer) idoFindOnePKByQuery(query);
	}
}