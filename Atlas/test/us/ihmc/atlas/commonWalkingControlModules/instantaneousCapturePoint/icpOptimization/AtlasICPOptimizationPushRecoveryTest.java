package us.ihmc.atlas.commonWalkingControlModules.instantaneousCapturePoint.icpOptimization;

import us.ihmc.atlas.AtlasRobotModel;
import us.ihmc.atlas.AtlasRobotVersion;
import us.ihmc.atlas.parameters.AtlasWalkingControllerParameters;
import us.ihmc.commonWalkingControlModules.configurations.WalkingControllerParameters;
import us.ihmc.darpaRoboticsChallenge.drcRobot.DRCRobotModel;
import us.ihmc.commonWalkingControlModules.instantaneousCapturePoint.icpOptimization.ICPOptimizationPushRecoveryTest;
import us.ihmc.simulationconstructionset.util.simulationRunner.BlockingSimulationRunner.SimulationExceededMaximumTimeException;

public class AtlasICPOptimizationPushRecoveryTest extends ICPOptimizationPushRecoveryTest
{
   protected DRCRobotModel getRobotModel()
   {
      AtlasRobotModel atlasRobotModel = new AtlasRobotModel(AtlasRobotVersion.ATLAS_UNPLUGGED_V5_NO_HANDS, DRCRobotModel.RobotTarget.SCS, false)
      {
         @Override
         public WalkingControllerParameters getWalkingControllerParameters()
         {
            return new AtlasWalkingControllerParameters(RobotTarget.SCS, getJointMap())
            {
               @Override
               public boolean useOptimizationBasedICPController()
               {
                  return true;
               }
            };
         }
      };

      return atlasRobotModel;
   }

   public static void main(String[] args)
   {
      AtlasICPOptimizationPushRecoveryTest test = new AtlasICPOptimizationPushRecoveryTest();
      try
      {
         test.testPushICPOptimizationOutwardPushInSwing();
      }
      catch(SimulationExceededMaximumTimeException e)
      {

      }
   }
}
