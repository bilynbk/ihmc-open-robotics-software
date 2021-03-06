package us.ihmc.humanoidBehaviors.behaviors.primitives;

import us.ihmc.communication.packets.PacketDestination;
import us.ihmc.humanoidBehaviors.behaviors.AbstractBehavior;
import us.ihmc.humanoidBehaviors.communication.OutgoingCommunicationBridgeInterface;
import us.ihmc.humanoidRobotics.communication.packets.HighLevelStateMessage;
import us.ihmc.humanoidRobotics.communication.packets.sensing.DepthDataClearCommand;
import us.ihmc.humanoidRobotics.communication.packets.sensing.DepthDataClearCommand.DepthDataTree;
import us.ihmc.robotics.dataStructures.variable.BooleanYoVariable;

public class ClearLidarBehavior extends AbstractBehavior
{
   private final BooleanYoVariable packetHasBeenSent = new BooleanYoVariable("packetHasBeenSent" + behaviorName, registry);
   private DepthDataClearCommand clearLidarPacket;

   public ClearLidarBehavior(OutgoingCommunicationBridgeInterface outgoingCommunicationBridge)
   {
      super(outgoingCommunicationBridge);

   }

   @Override
   public void doControl()
   {
      clearLidarPacket = new DepthDataClearCommand(DepthDataTree.DECAY_POINT_CLOUD);

      //      clearLidarPacket.setDestination(PacketDestination.NETWORK_PROCESSOR);

      if (!packetHasBeenSent.getBooleanValue() && (clearLidarPacket != null))
      {
         sendPacketToNetworkProcessor();
      }
   }

   private void sendPacketToNetworkProcessor()
   {
      if (!isPaused.getBooleanValue() && !isAborted.getBooleanValue())
      {
         sendPacketToNetworkProcessor(clearLidarPacket);
         packetHasBeenSent.set(true);
      }
   }

   @Override
   public void initialize()
   {
      packetHasBeenSent.set(false);
      super.initialize();
   }

   @Override
   public void doPostBehaviorCleanup()
   {
      packetHasBeenSent.set(false);

        super.doPostBehaviorCleanup();
   }

   @Override
   public boolean isDone()
   {
      return packetHasBeenSent.getBooleanValue() && !isPaused.getBooleanValue();
   }


   public boolean hasInputBeenSet()
   {
      if (clearLidarPacket != null)
         return true;
      else
         return false;
   }
}
