package us.ihmc.darpaRoboticsChallenge.drcRobot;

import us.ihmc.communication.net.AtomicSettableTimestampProvider;
import us.ihmc.simulationconstructionset.robotController.RobotController;
import us.ihmc.utilities.time.TimeTools;
import us.ihmc.yoUtilities.dataStructure.registry.YoVariableRegistry;

public class SimulatedDRCRobotTimeProvider extends AtomicSettableTimestampProvider implements RobotController
{
   private final long nanoSecondsPerTick;
   
   public SimulatedDRCRobotTimeProvider(double controlDT)
   {
      nanoSecondsPerTick = TimeTools.secondsToNanoSeconds(controlDT);
   }

   public void initialize()
   {
      set(0);
   }

   public YoVariableRegistry getYoVariableRegistry()
   {
      return new YoVariableRegistry(getName());
   }

   public String getName()
   {
      return getClass().getSimpleName();
   }

   public String getDescription()
   {
      return getName();
   }

   public void doControl()
   {
      increment(nanoSecondsPerTick);
   }

}