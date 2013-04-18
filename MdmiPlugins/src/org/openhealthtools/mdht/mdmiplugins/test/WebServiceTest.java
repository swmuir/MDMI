package org.openhealthtools.mdht.mdmiplugins.test;

import java.net.*;
import javax.ws.rs.core.*;

import org.openhealthtools.mdht.mdmi.model.*;
import org.openhealthtools.mdht.mdmi.service.*;

public class WebServiceTest {
   public static void main( String[] args ) {
      try {
         MessageGroup testMap = new MessageGroup();
         testMap.setName("TestMap");
         testMap.setDomainDictionary(new MdmiDomainDictionaryReference());
         
         MdmiDatatypeProxy pdt = new MdmiDatatypeProxy(getBaseUri(), getToken(), testMap);
         MdmiBusinessElementProxy pbe = new MdmiBusinessElementProxy(getBaseUri(), getToken(), testMap, pdt);

         System.out.println("--- DATA TYPES -----------");
         MdmiDatatype[] lstDT = pdt.getAll(0); // offset 0
         for( int i = 0; i < lstDT.length; i++ ) {
            System.out.println(lstDT[i].toString());
         }
         System.out.println("--------------------------");
         
         System.out.println("--- BUSINESS ELEMENTS ----");
         MdmiBusinessElementReference[] lstBE = pbe.getAll(0);
         for( int i = 0; i < lstBE.length; i++ ) {
            System.out.println(lstBE[i].toString());
         }
         System.out.println("--------------------------");
         
         DTExternal testDataDT = new DTExternal();
         testDataDT.setTypeName("CodeList");
         testDataDT.setTypeSpec(new URI("http://dictionary.mdmi.org/enums/CodeList"));
         try {
            pdt.delete(testDataDT);
         }
         catch( Exception ignoreDeleteFails ) {}
         
         MdmiDatatype responseDT = pdt.add(testDataDT);
         System.out.println(responseDT);
         System.out.println("--------------------------");
         
         String nameDT = responseDT.getName();
         responseDT = pdt.get(nameDT);
         System.out.println(responseDT);
         System.out.println("--------------------------");
         
         testDataDT = new DTExternal();
         testDataDT.setTypeName("CodeList");
         testDataDT.setTypeSpec(new URI("http://dictionary.mdmi.org/enums/CodeListV2"));
         
         responseDT = pdt.update(testDataDT);
         System.out.println(responseDT);
         System.out.println("--------------------------");
         
         MdmiBusinessElementReference testDataBE = new MdmiBusinessElementReference();
         testDataBE.setName("TestBer");
         testDataBE.setUniqueIdentifier("TestBer");
         testDataBE.setReference(new URI("http://dictionary.mdmi.org/bers/TestBer"));
         testDataBE.setReferenceDatatype(testDataDT);
         try {
            pbe.delete(testDataBE);
         }
         catch( Exception ignoreDeleteFails ) {}
         
         MdmiBusinessElementReference responseBE = pbe.add(testDataBE);
         System.out.println(responseBE);
         System.out.println("--------------------------");
         
         String nameBE = responseBE.getName();
         responseBE = pbe.get(nameBE);
         System.out.println(responseBE);
         System.out.println("--------------------------");
         
         testDataBE = new MdmiBusinessElementReference();
         testDataBE.setName("TestBer");
         testDataBE.setUniqueIdentifier("TestBer");
         testDataBE.setReference(new URI("http://dictionary.mdmi.org/bers/TestBerV2"));
         testDataBE.setReferenceDatatype(testDataDT);
         
         responseBE = pbe.update(testDataBE);
         System.out.println(responseBE);
         System.out.println("--------------------------");
         
         pdt.delete(testDataDT);
         pbe.delete(testDataBE);
         
         System.out.println("--- DATA TYPES -----------");
         lstDT = pdt.getAll(0);
         for( int i = 0; i < lstDT.length; i++ ) {
            System.out.println(lstDT[i].toString());
         }
         System.out.println("--------------------------");

         System.out.println("--- BUSINESS ELEMENTS ----");
         lstBE = pbe.getAll(0);
         for( int i = 0; i < lstBE.length; i++ ) {
            System.out.println(lstBE[i].toString());
         }
         System.out.println("--------------------------");
      }
      catch( Exception ex ) {
         ex.printStackTrace();
      }
   }

   private static URI getBaseUri() {
      return UriBuilder.fromUri("http://localhost:8080/MdmiSvc").build();
   }

   private static String getToken() {
      return "KenLord-MDMI2013";
   }
} // WebServiceTest
