package us.ihmc.valkyrie.obstacleCourse;

import org.junit.Test;

import us.ihmc.darpaRoboticsChallenge.drcRobot.DRCRobotModel;
import us.ihmc.darpaRoboticsChallenge.obstacleCourseTests.DRCObstacleCourseTrialsTerrainTest;
import us.ihmc.simulationconstructionset.bambooTools.BambooTools;
import us.ihmc.simulationconstructionset.util.simulationRunner.BlockingSimulationRunner.SimulationExceededMaximumTimeException;
import us.ihmc.tools.continuousIntegration.IntegrationCategory;
import us.ihmc.tools.continuousIntegration.ContinuousIntegrationAnnotations.ContinuousIntegrationPlan;
import us.ihmc.tools.continuousIntegration.ContinuousIntegrationAnnotations.ContinuousIntegrationTest;
import us.ihmc.valkyrie.ValkyrieRobotModel;

@ContinuousIntegrationPlan(categories = {IntegrationCategory.SLOW, IntegrationCategory.VIDEO})
public class ValkyrieObstacleCourseTrialsTerrainTest extends DRCObstacleCourseTrialsTerrainTest
{
   private final ValkyrieRobotModel robotModel = new ValkyrieRobotModel(DRCRobotModel.RobotTarget.SCS, false);

   @Override
   public DRCRobotModel getRobotModel()
   {
      return robotModel;
   }

   @Override
   protected DRCRobotModel getRobotModelWithAdditionalFootContactPoints()
   {
      ValkyrieRobotModel robotModel = new ValkyrieRobotModel(DRCRobotModel.RobotTarget.SCS, false);
      robotModel.addMoreFootContactPointsSimOnly(5, 3, true);
      return robotModel;
   }

   @Override
   public String getSimpleRobotName()
   {
      return BambooTools.getSimpleRobotNameFor(BambooTools.SimpleRobotNameKeys.VALKYRIE);
   }

   @Override
	@ContinuousIntegrationTest(estimatedDuration = 56.0)
   @Test(timeout = 280000)
   public void testTrialsTerrainZigzagHurdlesScript() throws SimulationExceededMaximumTimeException
   {
      super.testTrialsTerrainZigzagHurdlesScript();
   }

   @Override
	@ContinuousIntegrationTest(estimatedDuration = 72.4)
   @Test(timeout = 360000)
   public void testWalkingOntoAndOverSlopesSideways() throws SimulationExceededMaximumTimeException
   {
      super.testWalkingOntoAndOverSlopesSideways();
   }

   @Override
	@ContinuousIntegrationTest(estimatedDuration = 96.6)
   @Test(timeout = 480000)
   public void testTrialsTerrainSlopeScriptRandomFootSlip() throws SimulationExceededMaximumTimeException
   {
      super.testTrialsTerrainSlopeScriptRandomFootSlip();
   }

   @Override
	@ContinuousIntegrationTest(estimatedDuration = 84.0)
   @Test(timeout = 420000)
   public void testTrialsTerrainSlopeScript() throws SimulationExceededMaximumTimeException
   {
      super.testTrialsTerrainSlopeScript();
   }

   @Override
	@ContinuousIntegrationTest(estimatedDuration = 49.1)
   @Test(timeout = 250000)
   public void testTrialsTerrainZigzagHurdlesScriptRandomFootSlip() throws SimulationExceededMaximumTimeException
   {
      robotModel.addMoreFootContactPointsSimOnly(8, 3, true);
      super.testTrialsTerrainZigzagHurdlesScriptRandomFootSlip();
   }
}
