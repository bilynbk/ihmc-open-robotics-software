package us.ihmc.humanoidBehaviors.behaviors.primitives;

import us.ihmc.humanoidBehaviors.behaviors.AbstractBehavior;
import us.ihmc.humanoidBehaviors.communication.OutgoingCommunicationBridgeInterface;
import us.ihmc.humanoidRobotics.communication.packets.sensing.DepthDataFilterParameters;
import us.ihmc.robotics.dataStructures.variable.BooleanYoVariable;

public class SetLidarParametersBehavior extends AbstractBehavior
{
   private final BooleanYoVariable packetHasBeenSent = new BooleanYoVariable("packetHasBeenSent" + behaviorName, registry);
   private DepthDataFilterParameters lidarParamPacket;

   public SetLidarParametersBehavior(OutgoingCommunicationBridgeInterface outgoingCommunicationBridge)
   {
      super(outgoingCommunicationBridge);

   }
   
   public void setInput(DepthDataFilterParameters clearLidarPacket)
   {
      this.lidarParamPacket = clearLidarPacket;
   }

   @Override
   public void doControl()
   {
      if (!packetHasBeenSent.getBooleanValue() && (lidarParamPacket != null))
      {
         sendPacketToNetworkProcessor();
      }
   }

   private void sendPacketToNetworkProcessor()
   {
      if (!isPaused.getBooleanValue() && !isAborted.getBooleanValue())
      {
         sendPacketToNetworkProcessor(lidarParamPacket);
         packetHasBeenSent.set(true);
      }
   }

   @Override
   public void initialize()
   {
      packetHasBeenSent.set(false);
      lidarParamPacket = null;
      isPaused.set(false);
      isAborted.set(false);
   }

   @Override
   public void doPostBehaviorCleanup()
   {
      packetHasBeenSent.set(false);

      isPaused.set(false);
      isAborted.set(false);
   }



   @Override
   public boolean isDone()
   {
      return packetHasBeenSent.getBooleanValue() && !isPaused.getBooleanValue();
   }

   

   public boolean hasInputBeenSet()
   {
      if (lidarParamPacket != null)
         return true;
      else
         return false;
   }
}
