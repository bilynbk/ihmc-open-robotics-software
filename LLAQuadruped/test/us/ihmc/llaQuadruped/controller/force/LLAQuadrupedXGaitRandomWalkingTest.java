package us.ihmc.llaQuadruped.controller.force;

import java.io.IOException;

import org.junit.Test;

import us.ihmc.llaQuadruped.LLAQuadrupedTestFactory;
import us.ihmc.quadrupedRobotics.QuadrupedTestFactory;
import us.ihmc.quadrupedRobotics.controller.force.QuadrupedXGaitRandomWalkingTest;
import us.ihmc.simulationconstructionset.util.simulationRunner.BlockingSimulationRunner.SimulationExceededMaximumTimeException;
import us.ihmc.simulationconstructionset.util.simulationRunner.ControllerFailureException;
import us.ihmc.tools.continuousIntegration.IntegrationCategory;
import us.ihmc.tools.continuousIntegration.ContinuousIntegrationAnnotations.ContinuousIntegrationPlan;
import us.ihmc.tools.continuousIntegration.ContinuousIntegrationAnnotations.ContinuousIntegrationTest;

@ContinuousIntegrationPlan(categories = IntegrationCategory.IN_DEVELOPMENT)
public class LLAQuadrupedXGaitRandomWalkingTest extends QuadrupedXGaitRandomWalkingTest
{
   @Override
   public QuadrupedTestFactory createQuadrupedTestFactory()
   {
      return new LLAQuadrupedTestFactory();
   }

   @Override
   @ContinuousIntegrationTest(estimatedDuration = 100.0)
   @Test(timeout = 500000)
   public void testExtremeRandomWalking() throws SimulationExceededMaximumTimeException, ControllerFailureException, IOException
   {
      super.testExtremeRandomWalking();
   }

   @Override
   @ContinuousIntegrationTest(estimatedDuration = 100.0)
   @Test(timeout = 500000)
   public void testWalkingRandomly() throws SimulationExceededMaximumTimeException, ControllerFailureException, IOException
   {
      super.testWalkingRandomly();
   }

   @Override
   @ContinuousIntegrationTest(estimatedDuration = 75.0)
   @Test(timeout = 600000)
   public void testWalkingAtRandomSpeedsWithStops() throws SimulationExceededMaximumTimeException, ControllerFailureException, IOException
   {
      super.testWalkingAtRandomSpeedsWithStops();
   }

   @Override
   @ContinuousIntegrationTest(estimatedDuration = 100.0)
   @Test(timeout = 600000)
   public void testWalkingRandomVelocitiesStoppingAndTurning() throws SimulationExceededMaximumTimeException, ControllerFailureException, IOException
   {
      super.testWalkingRandomVelocitiesStoppingAndTurning();
   }
}
