/*******************************************************************************
* Copyright (c) 2012 Firestar Software, Inc.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*     Firestar Software, Inc. - initial API and implementation
*
* Author:
*     Gabriel Oancea
*
*******************************************************************************/
package org.openhealthtools.mdht.mdmi.engine;

import java.io.File;
import java.io.IOException;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.module.SimpleModule;
import org.openhealthtools.mdht.mdmi.*;
import org.openhealthtools.mdht.mdmi.engine.json.ElementValueSetSerializer;
import org.openhealthtools.mdht.mdmi.engine.json.XDataStructSerializer;
import org.openhealthtools.mdht.mdmi.engine.json.XElementValueSerializer;
import org.openhealthtools.mdht.mdmi.model.*;

/**
 * MDMI Unit of Work. Will execute a transfer from the source to the target messages, based on the given maps.
 * 
 * @author goancea
 */
public class MdmiUow implements Runnable {
   MdmiEngine       owner;
   MdmiTransferInfo transferInfo;
   ISyntaxNode      srcSyntaxModel;
   ElementValueSet  srcSemanticModel;
   ISyntaxNode      trgSyntaxModel;
   ElementValueSet  trgSemanticModel;

   /**
    * Construct a new unit of work instance with the given owner and transfer info.
    * 
    * @param owner The actual owner.
    * @param transferInfo The transfer info - data used to execute a transfer.
    */
   MdmiUow( MdmiEngine owner, MdmiTransferInfo transferInfo ) {
      this.owner = owner;
      this.transferInfo = transferInfo;
   }

   @Override
   public void run() {
      try {
         preProcess();
         processInboundSourceMessage();
         processInboundTargetMessage();
         processConversions();
         processOutboundTargetMessage();
         postProcess();
      }
      catch( MdmiException ex ) {
         throw ex;
      }
      catch( Exception ex ) {
         throw new MdmiException(ex, "MdmiUow: Unexpected exception caught, static is {0}", toString());
      }
   }

   // 0. Call the pre-processors, if any
   void preProcess() {
      System.out.println("");
      System.out.println("---------- PRE-PROCESSORS START ----------");
      Mdmi.INSTANCE.getPreProcessors().preProcess(transferInfo);
      System.out.println("---------- PRE-PROCESSORS END ----------");
   }

	private void serializeSemanticModel( String name, ElementValueSet semanticModel ) {
		try {
			ObjectMapper mapper = new ObjectMapper();

			SimpleModule dateTimeSerializerModule = new SimpleModule("DateTimeSerializerModule", new Version(1, 0, 0, null));
			dateTimeSerializerModule.addSerializer(ElementValueSet.class, new ElementValueSetSerializer());
			mapper.registerModule(dateTimeSerializerModule);

			SimpleModule xElementValueSerializerModule = new SimpleModule("XElementValueSerializer", new Version(1, 0, 0, null));
			xElementValueSerializerModule.addSerializer(XElementValue.class, new XElementValueSerializer());
			mapper.registerModule(xElementValueSerializerModule);

			SimpleModule semanticElementSerializerModule = new SimpleModule("SemanticElementSerializer", new Version(1, 0, 0, null));
			xElementValueSerializerModule.addSerializer(SemanticElement.class, new SemanticElementSerializer());
			mapper.registerModule(semanticElementSerializerModule);

			SimpleModule xDataStructSerializerrModule = new SimpleModule("XDataStructSerializer", new Version(1, 0, 0, null));
			xDataStructSerializerrModule.addSerializer(XDataStruct.class, new XDataStructSerializer());
			mapper.registerModule(xDataStructSerializerrModule);

			File jsonFile = new File(String.format("./logs/%s.json", name));
			mapper.writeValue(jsonFile, semanticModel);
		}
		catch( JsonGenerationException ex ) {
			ex.printStackTrace();
		}
		catch( JsonMappingException ex ) {
			ex.printStackTrace();
		}
		catch( IOException ex ) {
			ex.printStackTrace();
		}
	}

   // 1. Build the source syntax tree and semantic model
   void processInboundSourceMessage() {
      System.out.println("");
      System.out.println("---------- SOURCE MESSAGE START ----------");
      ISyntacticParser srcSynProv = getSynProv(transferInfo.getSourceMessageGroup());
      ISemanticParser srcSemProv = getSemProv(transferInfo.getSourceMessageGroup());
      srcSyntaxModel = (YNode)srcSynProv.parse(transferInfo.sourceModel.getModel(), transferInfo.sourceMessage);
//      System.out.println(srcSyntaxModel.toString());
      System.out.println("");
      srcSemanticModel = new ElementValueSet();
      srcSemProv.buildSemanticModel(transferInfo.sourceModel.getModel(), srcSyntaxModel, srcSemanticModel);
      
      serializeSemanticModel("SourceSemanticModel", srcSemanticModel);
      
	


   }

   // 2. Build the target syntax tree and semantic model (if any)
   void processInboundTargetMessage() {
      trgSemanticModel = new ElementValueSet();
      byte[] data = transferInfo.targetMessage.getData();
      if( data == null || data.length <= 0 )
         return;
      ISemanticParser trgSemProv = getSemProv(transferInfo.getTargetMessageGroup());
      ISyntacticParser trgSynProv = getSynProv(transferInfo.getTargetMessageGroup());
      System.out.println("");
      System.out.println("---------- TARGET MESSAGE START ----------");
      trgSyntaxModel = (YNode)trgSynProv.parse(transferInfo.targetModel.getModel(), transferInfo.targetMessage);
//      System.out.println(trgSyntaxModel.toString());
      System.out.println("");
      trgSemProv.buildSemanticModel(transferInfo.targetModel.getModel(), trgSyntaxModel, trgSemanticModel);
//      System.out.println(trgSemanticModel.toString());
      System.out.println("---------- TARGET MESSAGE END ----------");
      
      
   }

   // 3. execute the data conversions
   void processConversions() {
      System.out.println("");
      System.out.println("---------- CONVERSION START ----------");
      Conversion c = new Conversion(this);
      c.execute();
      System.out.println("---------- CONVERSION END ----------");
      System.out.println("");
      System.out.println("---------- TARGET MESSAGE START ----------");
      serializeSemanticModel("TargetSemanticModel", trgSemanticModel);
//      System.out.println(trgSemanticModel.toString());
      System.out.println("---------- TARGET MESSAGE END ----------");
   }

   // 4. Build the target syntax tree from the target semantic model
   void processOutboundTargetMessage() {
      ISemanticParser trgSemProv = getSemProv(transferInfo.getTargetMessageGroup());
      ISyntacticParser trgSynProv = getSynProv(transferInfo.getTargetMessageGroup());
      if( trgSyntaxModel != null ) {
    	 
         trgSemProv.updateSyntacticModel(transferInfo.targetModel.getModel(), trgSemanticModel, trgSyntaxModel);
      }
      else {
         trgSyntaxModel = trgSemProv.createNewSyntacticModel(transferInfo.targetModel.getModel(), trgSemanticModel);
      }
      System.out.println("");
      System.out.println("---------- PROCESSING OUTPUT MESSAGE START ----------");
//      System.out.println(trgSyntaxModel.toString());
      trgSynProv.serialize(transferInfo.targetModel.getModel(), transferInfo.targetMessage, trgSyntaxModel);
      System.out.println("---------- PROCESSING OUTPUT MESSAGE END ----------");
   }

   // 5. Call the post-processors, if any
   void postProcess() {
      System.out.println("");
      System.out.println("---------- POST-PROCESSORS START ----------");
      Mdmi.INSTANCE.getPostProcessors().postProcess(transferInfo);
      System.out.println("---------- POST-PROCESSORS END ----------");
   }

   private MdmiResolver resolver() {
      return owner.getOwner().getResolver();
   }

   private ISyntacticParser getSynProv( MessageGroup messageGroup ) {
      return resolver().getSyntacticParser(messageGroup.getName());
   }

   private ISemanticParser getSemProv( MessageGroup messageGroup ) {
      return resolver().getSemanticParser(messageGroup.getName());
   }

   @Override
   public String toString() {
      if( transferInfo == null )
         return super.toString();
      return transferInfo.toString();
   }
} // MdmiUow
