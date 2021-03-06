package us.ihmc.humanoidBehaviors.behaviors.simpleBehaviors;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import us.ihmc.humanoidBehaviors.behaviors.AbstractBehavior;
import us.ihmc.humanoidBehaviors.communication.OutgoingCommunicationBridgeInterface;
import us.ihmc.robotics.dataStructures.variable.BooleanYoVariable;

public class WaitForUserValidationBehavior extends AbstractBehavior
{

   private BooleanYoVariable validClicked;
   private BooleanYoVariable validAcknoledged;

   ExecutorService executorService = Executors.newFixedThreadPool(2);

   public WaitForUserValidationBehavior(OutgoingCommunicationBridgeInterface outgoingCommunicationBridge, BooleanYoVariable validClicked,
         BooleanYoVariable validAcknoledged)
   {
      super(outgoingCommunicationBridge);
      this.validAcknoledged = validAcknoledged;
      this.validClicked = validClicked;

   }

   public void reset()
   {
      validAcknoledged.set(false);
   }

   @Override
   public void doControl()
   {
      if (validClicked.getBooleanValue())
      {
         validAcknoledged.set(true);
      }
   }

   @Override
   public boolean isDone()
   {
     // return true;
      return validAcknoledged.getBooleanValue();
   }



  

   @Override
   public void initialize()
   {
   }
}
