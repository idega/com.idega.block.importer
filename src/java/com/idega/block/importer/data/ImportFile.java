package com.idega.block.importer.data;

import java.util.Collection;
import java.util.ArrayList;
import java.io.File;

public interface ImportFile{

  public Collection getRecords();
  public Object getNextRecord(); 
  //public Object getRecordAtIndex(int index);
 // public boolean parse();
}
