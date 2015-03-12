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
         
         MdmiDatatypeProxy pdt = new MdmiDatatypeProxy(getBaseUri(), getToken());
         MdmiBusinessElementProxy pbe = new MdmiBusinessElementProxy(getBaseUri(), getToken(), pdt);

         System.out.println("--- DATA TYPES -----------");
         MdmiDatatype[] lstDT = pdt.getAll(testMap, 0); // offset 0
         for( int i = 0; i < lstDT.length; i++ ) {
            System.out.println(lstDT[i].toString());
         }
         System.out.println("--------------------------");
         
         System.out.println("--- BUSINESS ELEMENTS ----");
         MdmiBusinessElementReference[] lstBE = pbe.getAll(0, testMap, 0);
         for( int i = 0; i < lstBE.length; i++ ) {
            System.out.println(lstBE[i].toString());
         }
         System.out.println("--------------------------");
         
         DTExternal testDataDT = new DTExternal();
         testDataDT.setTypeName("CodeList");
         testDataDT.setTypeSpec(new URI("http://dictionary.mdmi.org/enums/CodeList"));
         try {
            // delete if already exists
            pdt.delete(testMap, testDataDT);
         }
         catch( Exception ignoreDeleteFails ) {}
         
         MdmiDatatype responseDT = pdt.add(testMap, testDataDT);
         System.out.println(responseDT);
         System.out.println("--------------------------");
         
         String nameDT = responseDT.getName();
         responseDT = pdt.get(testMap, nameDT);
         System.out.println(responseDT);
         System.out.println("--------------------------");
         
         testDataDT = new DTExternal();
         testDataDT.setTypeName("CodeList");
         testDataDT.setTypeSpec(new URI("http://dictionary.mdmi.org/enums/CodeListV2"));
         
         responseDT = pdt.update(testMap, testDataDT);
         System.out.println(responseDT);
         System.out.println("--------------------------");
         
         MdmiBusinessElementReference testDataBE = new MdmiBusinessElementReference();
         testDataBE.setName("TestBer");
         testDataBE.setUniqueIdentifier("TestBer");
         testDataBE.setReference(new URI("http://dictionary.mdmi.org/bers/TestBer"));
         testDataBE.setReferenceDatatype(testDataDT);
         try {
            pbe.delete(testDataBE.getUniqueIdentifier());
         }
         catch( Exception ignoreDeleteFails ) {}
         
         MdmiBusinessElementReference responseBE = pbe.add(testDataBE, testMap, 0);
         System.out.println(responseBE);
         System.out.println("--------------------------");
         
         String nameBE = responseBE.getName();
         responseBE = pbe.get(nameBE, testMap, 0);
         System.out.println(responseBE);
         System.out.println("--------------------------");
         
         testDataBE = new MdmiBusinessElementReference();
         testDataBE.setName("TestBer");
         testDataBE.setUniqueIdentifier("TestBer");
         testDataBE.setReference(new URI("http://dictionary.mdmi.org/bers/TestBerV2"));
         testDataBE.setReferenceDatatype(testDataDT);
         responseBE = pbe.update(testDataBE, testMap, 0);
         System.out.println(responseBE);
         System.out.println("--------------------------");
         
         pdt.delete(testMap, testDataDT);
         pbe.delete(testDataBE.getUniqueIdentifier());
         
         System.out.println("--- DATA TYPES -----------");
         lstDT = pdt.getAll(testMap, 0);
         for( int i = 0; i < lstDT.length; i++ ) {
            System.out.println(lstDT[i].toString());
         }
         System.out.println("--------------------------");

         System.out.println("--- BUSINESS ELEMENTS ----");
         lstBE = pbe.getAll(0, testMap, 0);
         for( int i = 0; i < lstBE.length; i++ ) {
            System.out.println(lstBE[i].toString());
         }
         System.out.println("--------------------------");

         MdmiBusinessElementReference stringBE = new MdmiBusinessElementReference();
         stringBE.setName("StringBer");
         stringBE.setUniqueIdentifier("StringBer");
         stringBE.setReference(new URI("http://dictionary.mdmi.org/bers/StringBer"));
         stringBE.setReferenceDatatype(DTSPrimitive.STRING);
         try {
            pbe.delete(pbe.fromModel(stringBE));
         }
         catch( Exception ignoreDeleteFails ) {}
         
         MdmiBusinessElementReference rsBE = pbe.add(stringBE, testMap, 0);
         System.out.println(rsBE);
         System.out.println("--------------------------");
         
         nameBE = rsBE.getName();
         rsBE = pbe.get(nameBE, testMap, 0);
         System.out.println(rsBE);
         System.out.println("--------------------------");
         
         stringBE = new MdmiBusinessElementReference();
         stringBE.setName("StringBer");
         stringBE.setUniqueIdentifier("StringBer");
         stringBE.setReference(new URI("http://dictionary.mdmi.org/bers/StringBerV2"));
         stringBE.setReferenceDatatype(DTSPrimitive.STRING);
         
         rsBE = pbe.update(stringBE, testMap, 0);
         System.out.println(rsBE);
         System.out.println("--------------------------");
         
         pbe.delete(stringBE.getUniqueIdentifier());
      }
      catch( Exception ex ) {
         ex.printStackTrace();
      }
   }

   private static URI getBaseUri() {
      return UriBuilder.fromUri("http://localhost:8080/MdmiSvc").build(); // 107.22.213.68
   }

   private static String getToken() {
      return "KenLord-MDMI2013";
   }
} // WebServiceTest
