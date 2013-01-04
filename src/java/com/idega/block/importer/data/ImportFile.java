package com.idega.block.importer.data;

import java.io.File;
import java.util.Collection;
import java.util.List;

public interface ImportFile{

  public Collection<String> getRecords();
  public Object getNextRecord();
  public List<String> getValuesFromRecordString(String recordString);
  public String getValueAtIndexFromRecordString(int index, String recordString);
  public void setFile(File file);
  public String getEmptyValueString();
  public void close();
/**
 * Method setEmptyValueString. This will be the value returned if a column you want is empty in the import file.
 * @param emptyValueString
 */
  public void setEmptyValueString(String emptyValueString);
  public File getFile();
  public String getEncoding();
}
