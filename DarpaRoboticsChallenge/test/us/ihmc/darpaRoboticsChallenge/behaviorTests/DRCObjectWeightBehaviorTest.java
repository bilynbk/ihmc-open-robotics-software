package us.ihmc.darpaRoboticsChallenge.behaviorTests;

import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import us.ihmc.communication.packets.manipulation.ObjectWeightPacket;
import us.ihmc.communication.util.NetworkPorts;
import us.ihmc.darpaRoboticsChallenge.DRCObstacleCourseStartingLocation;
import us.ihmc.darpaRoboticsChallenge.MultiRobotTestInterface;
import us.ihmc.darpaRoboticsChallenge.environment.DRCDemo01NavigationEnvironment;
import us.ihmc.darpaRoboticsChallenge.testTools.DRCBehaviorTestHelper;
import us.ihmc.humanoidBehaviors.behaviors.primitives.ObjectWeightBehavior;
import us.ihmc.simulationconstructionset.bambooTools.BambooTools;
import us.ihmc.simulationconstructionset.bambooTools.SimulationTestingParameters;
import us.ihmc.simulationconstructionset.util.simulationRunner.BlockingSimulationRunner.SimulationExceededMaximumTimeException;
import us.ihmc.utilities.MemoryTools;
import us.ihmc.utilities.ThreadTools;
import us.ihmc.utilities.code.agileTesting.BambooAnnotations.EstimatedDuration;
import us.ihmc.utilities.math.MathTools;
import us.ihmc.utilities.robotSide.RobotSide;
import us.ihmc.yoUtilities.dataStructure.variable.DoubleYoVariable;
import us.ihmc.yoUtilities.time.GlobalTimer;

public abstract class DRCObjectWeightBehaviorTest implements MultiRobotTestInterface
{
   private static final SimulationTestingParameters simulationTestingParameters = SimulationTestingParameters.createFromEnvironmentVariables();
   private static final double epsilon = 10e-8;
   private DRCBehaviorTestHelper drcBehaviorTestHelper;
   
   @Before
   public void setUp()
   {
      MemoryTools.printCurrentMemoryUsageAndReturnUsedMemoryInMB(getClass().getSimpleName() + " before test.");
      BambooTools.reportTestStartedMessage();
     
      DRCDemo01NavigationEnvironment testEnvironment = new DRCDemo01NavigationEnvironment();
      drcBehaviorTestHelper = new DRCBehaviorTestHelper(testEnvironment, getSimpleRobotName(), null,
            DRCObstacleCourseStartingLocation.DEFAULT, simulationTestingParameters, getRobotModel());
   }

   @After
   public void destroySimulationAndRecycleMemory()
   {
      if (simulationTestingParameters.getKeepSCSUp())
      {
         ThreadTools.sleepForever();
      }

      // Do this here in case a test fails. That way the memory will be recycled.
      if (drcBehaviorTestHelper != null)
      {
         drcBehaviorTestHelper.destroySimulation();
         drcBehaviorTestHelper = null;
      }

      GlobalTimer.clearTimers();
      MemoryTools.printCurrentMemoryUsageAndReturnUsedMemoryInMB(getClass().getSimpleName() + " after test.");
   }
   
   @EstimatedDuration(duration = 15.0)
   @Test(timeout = 45000)
   public void testConstructorAndSetInput()
   {
      ObjectWeightBehavior behavior = new ObjectWeightBehavior(drcBehaviorTestHelper.getBehaviorCommunicationBridge());
      behavior.setInput(new ObjectWeightPacket(RobotSide.LEFT, 0.0));
      assertTrue(behavior.hasInputBeenSet());
   }
   
   @EstimatedDuration(duration = 20.0)
   @Test(timeout = 60000)
   public void testSettingWeight() throws SimulationExceededMaximumTimeException
   {
      boolean success = drcBehaviorTestHelper.simulateAndBlockAndCatchExceptions(1.0);
      assertTrue(success);
      
      ObjectWeightBehavior objectWeightBehavior = new ObjectWeightBehavior(drcBehaviorTestHelper.getBehaviorCommunicationBridge());
      DoubleYoVariable rightMass = (DoubleYoVariable) drcBehaviorTestHelper.getSimulationConstructionSet().getVariable("rightTool", "rightToolObjectMass");
      DoubleYoVariable leftMass = (DoubleYoVariable) drcBehaviorTestHelper.getSimulationConstructionSet().getVariable("leftTool", "leftToolObjectMass");
      
      double weightLeft = 1.5;
      objectWeightBehavior.initialize();
      objectWeightBehavior.setInput(new ObjectWeightPacket(RobotSide.LEFT, weightLeft));
      success = drcBehaviorTestHelper.executeBehaviorUntilDone(objectWeightBehavior);
      assertTrue(success);
      assertTrue(MathTools.epsilonEquals(leftMass.getDoubleValue(), weightLeft, epsilon));
      assertTrue(MathTools.epsilonEquals(rightMass.getDoubleValue(), 0.0, epsilon));
      
      double weightRight = 0.8;
      objectWeightBehavior.initialize();
      objectWeightBehavior.setInput(new ObjectWeightPacket(RobotSide.RIGHT, weightRight));
      success = drcBehaviorTestHelper.executeBehaviorUntilDone(objectWeightBehavior);
      assertTrue(success);
      assertTrue(MathTools.epsilonEquals(leftMass.getDoubleValue(), weightLeft, epsilon));
      assertTrue(MathTools.epsilonEquals(rightMass.getDoubleValue(), weightRight, epsilon));
   }
}