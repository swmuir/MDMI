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

import org.openhealthtools.mdht.mdmi.*;
import org.openhealthtools.mdht.mdmi.model.*;

/**
 * MDMI Unit of Work. Will execute a transfer from the source to the target messages, based on the given maps.
 * 
 * @author goancea
 */
public class MdmiUow implements Runnable {
   MdmiEngine       owner;
   MdmiTransferInfo transferInfo;
   YNode            srcSyntaxModel;
   ElementValueSet  srcSemanticModel;
   YNode            trgSyntaxModel;
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
   
   // 1. Build the source syntax tree and semantic model
   void processInboundSourceMessage() {
      System.out.println("");
      System.out.println("---------- SOURCE MESSAGE START ----------");
      ISyntacticParser srcSynProv = getSynProv(transferInfo.getSourceMessageGroup());
      ISemanticParser srcSemProv = getSemProv(transferInfo.getSourceMessageGroup());
      srcSyntaxModel = (YNode)srcSynProv.parse(transferInfo.sourceModel.getModel(), transferInfo.sourceMessage);
      System.out.println(srcSyntaxModel.toString());
      System.out.println("");
      srcSemanticModel = new ElementValueSet();
      srcSemProv.buildSemanticModel(transferInfo.sourceModel.getModel(), srcSyntaxModel, srcSemanticModel);
      System.out.println(srcSemanticModel.toString());
      System.out.println("---------- SOURCE MESSAGE END ----------");
   }

   // 2. Build the target syntax tree and semantic model (if any)
   void processInboundTargetMessage() {
      trgSemanticModel = new ElementValueSet();
      byte[] data = transferInfo.targetMessage.getData();
      if( data == null || data.length <= 0 )
         return;
      ISemanticParser trgSemProv = getSemProv(transferInfo.getTargetMessageGroup());
      ISyntacticParser trgSynProv = getSynProv(transferInfo.getTargetMessageGroup());
      trgSemanticModel = new ElementValueSet();
      System.out.println("");
      System.out.println("---------- TARGET MESSAGE START ----------");
      trgSyntaxModel = (YNode)trgSynProv.parse(transferInfo.targetModel.getModel(), transferInfo.targetMessage);
      System.out.println(trgSyntaxModel.toString());
      System.out.println("");
      trgSemProv.buildSemanticModel(transferInfo.targetModel.getModel(), trgSyntaxModel, trgSemanticModel);
      System.out.println(trgSemanticModel.toString());
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
      System.out.println(trgSemanticModel.toString());
      System.out.println("---------- TARGET MESSAGE END ----------");
   }

   // 4. Build the target syntax tree from the target semantic model
   void processOutboundTargetMessage() {
      ISemanticParser trgSemProv = getSemProv(transferInfo.getTargetMessageGroup());
      ISyntacticParser trgSynProv = getSynProv(transferInfo.getTargetMessageGroup());
      trgSyntaxModel = (YNode)trgSemProv.createSyntacticModel(transferInfo.targetModel.getModel(), trgSemanticModel);
      System.out.println("");
      System.out.println("---------- PROCESSING OUTPUT MESSAGE START ----------");
      System.out.println(trgSyntaxModel.toString());
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
