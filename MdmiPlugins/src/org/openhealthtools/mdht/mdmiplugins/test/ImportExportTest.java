package org.openhealthtools.mdht.mdmiplugins.test;

import org.openhealthtools.mdht.mdmi.model.*;
import org.openhealthtools.mdht.mdmi.service.*;

public class ImportExportTest {
   public static void main( String[] args ) {
      try {
         MessageGroup testMap = new MessageGroup();
         testMap.setName("TestMap");
         testMap.setDomainDictionary(new MdmiDomainDictionaryReference());
         
         String fileNameImport = "C:\\Gabriel\\misc\\ExampleRI.xml";
         String fileNameExport = "C:\\Gabriel\\misc\\ExampleRI_out.xml";
         MdmiImportExportUtility.Data data = MdmiImportExportUtility.Import(testMap, fileNameImport, 0);
         
         MdmiImportExportUtility.Export(data.bers, fileNameExport);
      }
      catch( Exception ex ) {
         ex.printStackTrace();
      }
   }
} // ImportExportTest
