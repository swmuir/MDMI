package org.openhealthtools.mdht.mdmiplugins.test;

import java.util.*;

import org.openhealthtools.mdht.mdmi.model.*;
import org.openhealthtools.mdht.mdmi.service.*;

public class ImportExportTest {
   public static void main( String[] args ) {
      try {
         MessageGroup testMap = new MessageGroup();
         testMap.setName("TestMap");
         testMap.setDomainDictionary(new MdmiDomainDictionaryReference());
         
         String fileNameImport = "D:\\Gabriel\\misc\\ExampleRI.xml";
         String fileNameExport = "D:\\Gabriel\\misc\\ExampleRI_out.xml";
         MdmiImportExportUtility.Data data = MdmiImportExportUtility.Import(testMap, fileNameImport, 0);
         
         MdmiImportExportUtility.Export(data.bers, fileNameExport);
      }
      catch( Exception ex ) {
         ex.printStackTrace();
      }
   }
} // ImportExportTest
