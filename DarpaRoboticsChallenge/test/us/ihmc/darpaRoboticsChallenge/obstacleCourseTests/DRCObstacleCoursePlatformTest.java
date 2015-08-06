package us.ihmc.darpaRoboticsChallenge.obstacleCourseTests;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;

import javax.vecmath.Point3d;
import javax.vecmath.Quat4d;
import javax.vecmath.Vector3d;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import us.ihmc.communication.packets.walking.ChestOrientationPacket;
import us.ihmc.communication.packets.walking.ComHeightPacket;
import us.ihmc.communication.packets.walking.FootstepData;
import us.ihmc.communication.packets.walking.FootstepDataList;
import us.ihmc.darpaRoboticsChallenge.DRCObstacleCourseStartingLocation;
import us.ihmc.darpaRoboticsChallenge.MultiRobotTestInterface;
import us.ihmc.darpaRoboticsChallenge.testTools.DRCSimulationTestHelper;
import us.ihmc.darpaRoboticsChallenge.testTools.ScriptedFootstepGenerator;
import us.ihmc.simulationconstructionset.SimulationConstructionSet;
import us.ihmc.simulationconstructionset.UnreasonableAccelerationException;
import us.ihmc.simulationconstructionset.bambooTools.BambooTools;
import us.ihmc.simulationconstructionset.bambooTools.SimulationTestingParameters;
import us.ihmc.simulationconstructionset.util.simulationRunner.BlockingSimulationRunner.SimulationExceededMaximumTimeException;
import us.ihmc.simulationconstructionset.util.simulationRunner.SimulationRewindabilityVerifier;
import us.ihmc.simulationconstructionset.util.simulationRunner.SimulationRewindabilityVerifierWithStackTracing;
import us.ihmc.simulationconstructionset.util.simulationRunner.VariableDifference;
import us.ihmc.utilities.MemoryTools;
import us.ihmc.utilities.ThreadTools;
import us.ihmc.utilities.code.agileTesting.BambooAnnotations.EstimatedDuration;
import us.ihmc.utilities.code.agileTesting.BambooAnnotations.QuarantinedTest;
import us.ihmc.robotics.geometry.BoundingBox3d;
import us.ihmc.robotics.geometry.FrameOrientation;
import us.ihmc.robotics.referenceFrames.ReferenceFrame;
import us.ihmc.robotics.geometry.RotationFunctions;
import us.ihmc.robotics.trajectories.TrajectoryType;
import us.ihmc.robotics.robotSide.RobotSide;

public abstract class DRCObstacleCoursePlatformTest implements MultiRobotTestInterface
{   
   protected SimulationTestingParameters simulationTestingParameters;
   protected DRCSimulationTestHelper drcSimulationTestHelper;

   @Before
   public void showMemoryUsageBeforeTest()
   {
      MemoryTools.printCurrentMemoryUsageAndReturnUsedMemoryInMB(getClass().getSimpleName() + " before test.");
   }

   @After
   public void destroySimulationAndRecycleMemory()
   {
      if (simulationTestingParameters.getKeepSCSUp())
      {
         ThreadTools.sleepForever();
      }

      // Do this here in case a test fails. That way the memory will be recycled.
      if (drcSimulationTestHelper != null)
      {
         drcSimulationTestHelper.destroySimulation();
         drcSimulationTestHelper = null;
      }

      simulationTestingParameters = null;
      MemoryTools.printCurrentMemoryUsageAndReturnUsedMemoryInMB(getClass().getSimpleName() + " after test.");
   }

   @Ignore
   @QuarantinedTest("This test is flaky. Sometimes it works, sometimes it doesn't due to threading of the various globalDataProducer and communicators. We need to be able to shut those off or make them not screw up the robot run.")
   @EstimatedDuration
   @Test(timeout=300000)
   public void testRunsTheSameWayTwiceJustStanding() throws UnreasonableAccelerationException, SimulationExceededMaximumTimeException
   {
      BambooTools.reportTestStartedMessage();
      
      DRCObstacleCourseStartingLocation selectedLocation = DRCObstacleCourseStartingLocation.SMALL_PLATFORM;
      simulationTestingParameters = SimulationTestingParameters.createFromEnvironmentVariables();
      simulationTestingParameters.setRunMultiThreaded(false);
      
      DRCSimulationTestHelper drcSimulationTestHelper1 = new DRCSimulationTestHelper("DRCWalkingOverSmallPlatformTest", "", selectedLocation,  simulationTestingParameters, getRobotModel());
      DRCSimulationTestHelper drcSimulationTestHelper2 = new DRCSimulationTestHelper("DRCWalkingOverSmallPlatformTest", "", selectedLocation,  simulationTestingParameters, getRobotModel());
      
      ArrayList<String> exceptions = DRCSimulationTestHelper.createVariableNamesStringsToIgnore();
      
      SimulationConstructionSet scs1 = drcSimulationTestHelper1.getSimulationConstructionSet();
      SimulationConstructionSet scs2 = drcSimulationTestHelper2.getSimulationConstructionSet();
      SimulationRewindabilityVerifier checker = new SimulationRewindabilityVerifier(scs1, scs2, exceptions);
      
//      setupCameraForWalkingOverSmallPlatform(scs1);
//      setupCameraForWalkingOverSmallPlatform(scs2);

      SimulationRewindabilityVerifierWithStackTracing helper = null;
      boolean useVariableListenerTestHelper = false; // Make this true if you want to record a history of variable changes and find the first instance where they are different. Don't check in as true though since takes lots of time.

      if (useVariableListenerTestHelper)
      {
         helper = new SimulationRewindabilityVerifierWithStackTracing(scs1, scs2, exceptions);
         helper.setRecordDifferencesForSimOne(true);
         helper.setRecordDifferencesForSimTwo(true);
         helper.clearChangesForSimulations();
      }
      
      if (useVariableListenerTestHelper)
      {
         int numberOfTicks = 10;
         for (int i=0; i<numberOfTicks ; i++)
         {
            System.out.println("Tick : " + i);
            scs1.simulateOneRecordStepNow();
            scs2.simulateOneRecordStepNow();

            boolean areTheVariableChangesDifferent = helper.areTheVariableChangesDifferent();
            if (areTheVariableChangesDifferent) helper.printOutStackTracesOfFirstChangedVariable();
         }
      
      }
      
      double runTime = 10.18;

      boolean success = drcSimulationTestHelper1.simulateAndBlockAndCatchExceptions(runTime);
      success = success && drcSimulationTestHelper2.simulateAndBlockAndCatchExceptions(runTime);

      if (useVariableListenerTestHelper)
      {
         System.out.println("Checking for variable differences at the end of the run using SimulationRewindabilityHelper");

         boolean areTheVariableChangesDifferent = helper.areTheVariableChangesDifferent();
         if (areTheVariableChangesDifferent) helper.printOutStackTracesOfFirstChangedVariable();
      }

      System.out.println("Checking for variable differences at the end of the run using SimulationRewindabilityVerifier");
      ArrayList<VariableDifference> variableDifferences = checker.verifySimulationsAreSameToStart();

      if (!variableDifferences.isEmpty())
      {
         System.err.println("variableDifferences: \n" + VariableDifference.allVariableDifferencesToString(variableDifferences));
         if (simulationTestingParameters.getKeepSCSUp())
            ThreadTools.sleepForever();
         fail("Found Variable Differences!\n variableDifferences: \n" + VariableDifference.allVariableDifferencesToString(variableDifferences));
      }


      drcSimulationTestHelper1.destroySimulation();
      drcSimulationTestHelper2.destroySimulation();

      BambooTools.reportTestFinishedMessage();
   }
   
	@EstimatedDuration(duration = 30.4)
	@Test(timeout = 151825)
   public void testWalkingOverSmallPlatformQuickly() throws SimulationExceededMaximumTimeException
   {
      BambooTools.reportTestStartedMessage();

      DRCObstacleCourseStartingLocation selectedLocation = DRCObstacleCourseStartingLocation.SMALL_PLATFORM;
      simulationTestingParameters = SimulationTestingParameters.createFromEnvironmentVariables();
      drcSimulationTestHelper = new DRCSimulationTestHelper("DRCWalkingOverSmallPlatformTest", "", selectedLocation,  simulationTestingParameters, getRobotModel());

      SimulationConstructionSet simulationConstructionSet = drcSimulationTestHelper.getSimulationConstructionSet();
      ScriptedFootstepGenerator scriptedFootstepGenerator = drcSimulationTestHelper.createScriptedFootstepGenerator();

      setupCameraForWalkingOverSmallPlatform(simulationConstructionSet);

      ThreadTools.sleep(1000);
      boolean success = drcSimulationTestHelper.simulateAndBlockAndCatchExceptions(2.0); //2.0);

      FootstepDataList footstepDataList = createFootstepsForSteppingPastSmallPlatform(scriptedFootstepGenerator);
      drcSimulationTestHelper.send(footstepDataList);

      success = success && drcSimulationTestHelper.simulateAndBlockAndCatchExceptions(4.0);

      drcSimulationTestHelper.createMovie(getSimpleRobotName(), 1);
      drcSimulationTestHelper.checkNothingChanged();

      assertTrue(success);
      
      Point3d center = new Point3d(-3.7944324216932475, -5.38051322671167, 0.7893380490431007);
      Vector3d plusMinusVector = new Vector3d(0.2, 0.2, 0.5);
      BoundingBox3d boundingBox = BoundingBox3d.createUsingCenterAndPlusMinusVector(center, plusMinusVector);
      drcSimulationTestHelper.assertRobotsRootJointIsInBoundingBox(boundingBox);

      
      BambooTools.reportTestFinishedMessage();
   }

	@EstimatedDuration(duration = 48.8)
   @Test(timeout = 243954)
   public void testSidestepOverSmallPlatform() throws SimulationExceededMaximumTimeException
   {
      BambooTools.reportTestStartedMessage();

      DRCObstacleCourseStartingLocation selectedLocation = DRCObstacleCourseStartingLocation.SMALL_PLATFORM_TURNED;
      simulationTestingParameters = SimulationTestingParameters.createFromEnvironmentVariables();
      drcSimulationTestHelper = new DRCSimulationTestHelper("DRCWalkingOverSmallPlatformTest", "", selectedLocation,  simulationTestingParameters, getRobotModel());

      SimulationConstructionSet simulationConstructionSet = drcSimulationTestHelper.getSimulationConstructionSet();

      setupCameraForWalkingOverSmallPlatform(simulationConstructionSet);

      ThreadTools.sleep(1000);
      boolean success = drcSimulationTestHelper.simulateAndBlockAndCatchExceptions(2.0); //2.0);

      FootstepDataList footstepDataList = createFootstepsForSideSteppingOverSmallPlatform();
      drcSimulationTestHelper.send(footstepDataList);

      success = success && drcSimulationTestHelper.simulateAndBlockAndCatchExceptions(11.0);

      drcSimulationTestHelper.createMovie(getSimpleRobotName(), 1);
      drcSimulationTestHelper.checkNothingChanged();

      assertTrue(success);

      Point3d center = new Point3d(-3.7944324216932475, -5.38051322671167, 0.7893380490431007);
      Vector3d plusMinusVector = new Vector3d(0.2, 0.2, 0.5);
      BoundingBox3d boundingBox = BoundingBox3d.createUsingCenterAndPlusMinusVector(center, plusMinusVector);
      drcSimulationTestHelper.assertRobotsRootJointIsInBoundingBox(boundingBox);

      BambooTools.reportTestFinishedMessage();
   }

	@EstimatedDuration(duration = 49.2)
   @Test(timeout = 246010)
   public void testSidestepOverSmallWall() throws SimulationExceededMaximumTimeException
   {
      BambooTools.reportTestStartedMessage();

      DRCObstacleCourseStartingLocation selectedLocation = DRCObstacleCourseStartingLocation.SMALL_WALL;
      simulationTestingParameters = SimulationTestingParameters.createFromEnvironmentVariables();
      drcSimulationTestHelper = new DRCSimulationTestHelper("DRCWalkingOverSmallPlatformTest", "", selectedLocation,  simulationTestingParameters, getRobotModel());

      SimulationConstructionSet simulationConstructionSet = drcSimulationTestHelper.getSimulationConstructionSet();

      setupCameraForWalkingOverSmallPlatform(simulationConstructionSet);

      ThreadTools.sleep(1000);
      boolean success = drcSimulationTestHelper.simulateAndBlockAndCatchExceptions(2.0); //2.0);

      FootstepDataList footstepDataList = createFootstepsForSideSteppingOverSmallWall();
      drcSimulationTestHelper.send(footstepDataList);

      success = success && drcSimulationTestHelper.simulateAndBlockAndCatchExceptions(11.0);

      drcSimulationTestHelper.createMovie(getSimpleRobotName(), 1);
      drcSimulationTestHelper.checkNothingChanged();

      assertTrue(success);

      Point3d center = new Point3d(-4.7944324216932475, -4.38051322671167, 0.7893380490431007);
      Vector3d plusMinusVector = new Vector3d(0.2, 0.2, 0.5);
      BoundingBox3d boundingBox = BoundingBox3d.createUsingCenterAndPlusMinusVector(center, plusMinusVector);
      drcSimulationTestHelper.assertRobotsRootJointIsInBoundingBox(boundingBox);

      BambooTools.reportTestFinishedMessage();
   }

	@EstimatedDuration(duration = 43.5)
   @Test(timeout = 217348)
   public void testWalkingOverSmallPlatform() throws SimulationExceededMaximumTimeException
   {
      BambooTools.reportTestStartedMessage();

      DRCObstacleCourseStartingLocation selectedLocation = DRCObstacleCourseStartingLocation.SMALL_PLATFORM;
      simulationTestingParameters = SimulationTestingParameters.createFromEnvironmentVariables();
      drcSimulationTestHelper = new DRCSimulationTestHelper("DRCWalkingOverSmallPlatformTest", "", selectedLocation,  simulationTestingParameters, getRobotModel());

      SimulationConstructionSet simulationConstructionSet = drcSimulationTestHelper.getSimulationConstructionSet();
      ScriptedFootstepGenerator scriptedFootstepGenerator = drcSimulationTestHelper.createScriptedFootstepGenerator();

      setupCameraForWalkingOverSmallPlatform(simulationConstructionSet);

      ThreadTools.sleep(1000);
      boolean success = drcSimulationTestHelper.simulateAndBlockAndCatchExceptions(2.0); //2.0);

      FootstepDataList footstepDataList = createFootstepsForSteppingOntoSmallPlatform(scriptedFootstepGenerator);
      drcSimulationTestHelper.send(footstepDataList);

      success = success && drcSimulationTestHelper.simulateAndBlockAndCatchExceptions(4.0);

      if (success)
      {
         footstepDataList = createFootstepsForSteppingOffOfSmallPlatform(scriptedFootstepGenerator);
         drcSimulationTestHelper.send(footstepDataList);

         success = success && drcSimulationTestHelper.simulateAndBlockAndCatchExceptions(4.0);
      }

      drcSimulationTestHelper.createMovie(getSimpleRobotName(), 1);
      drcSimulationTestHelper.checkNothingChanged();

      assertTrue(success);

      Point3d center = new Point3d(-3.7944324216932475, -5.38051322671167, 0.7893380490431007);
      Vector3d plusMinusVector = new Vector3d(0.2, 0.2, 0.5);
      BoundingBox3d boundingBox = BoundingBox3d.createUsingCenterAndPlusMinusVector(center, plusMinusVector);
      drcSimulationTestHelper.assertRobotsRootJointIsInBoundingBox(boundingBox);


      BambooTools.reportTestFinishedMessage();
   }



	@EstimatedDuration(duration = 29.9)
	@Test(timeout = 149558)
   public void testWalkingOntoMediumPlatformToesTouching() throws SimulationExceededMaximumTimeException
   {
      BambooTools.reportTestStartedMessage();

      DRCObstacleCourseStartingLocation selectedLocation = DRCObstacleCourseStartingLocation.MEDIUM_PLATFORM;
      simulationTestingParameters = SimulationTestingParameters.createFromEnvironmentVariables();
      drcSimulationTestHelper = new DRCSimulationTestHelper("DRCWalkingOntoMediumPlatformToesTouchingTest", "", selectedLocation,  simulationTestingParameters, getRobotModel());

      SimulationConstructionSet simulationConstructionSet = drcSimulationTestHelper.getSimulationConstructionSet();
      ScriptedFootstepGenerator scriptedFootstepGenerator = drcSimulationTestHelper.createScriptedFootstepGenerator();

      setupCameraForWalkingOverMediumPlatform(simulationConstructionSet);

      ThreadTools.sleep(1000);
      boolean success = drcSimulationTestHelper.simulateAndBlockAndCatchExceptions(2.0);

      FootstepDataList footstepDataList = createFootstepsForSteppingOntoMediumPlatform(scriptedFootstepGenerator);
      drcSimulationTestHelper.send(footstepDataList);

      success = success && drcSimulationTestHelper.simulateAndBlockAndCatchExceptions(4.0);

      drcSimulationTestHelper.createMovie(getSimpleRobotName(), 1);
      drcSimulationTestHelper.checkNothingChanged();

      assertTrue(success);
      
      Point3d center = new Point3d(-4.0997851961824665, -5.797669618987603, 0.9903260891750866);
      Vector3d plusMinusVector = new Vector3d(0.2, 0.2, 0.5);
      BoundingBox3d boundingBox = BoundingBox3d.createUsingCenterAndPlusMinusVector(center, plusMinusVector);
      drcSimulationTestHelper.assertRobotsRootJointIsInBoundingBox(boundingBox);
      
      BambooTools.reportTestFinishedMessage();
   }


	@EstimatedDuration(duration = 30.0)
	@Test(timeout = 149768)
   public void testWalkingOffOfMediumPlatform() throws SimulationExceededMaximumTimeException
   {
      BambooTools.reportTestStartedMessage();
      
      DRCObstacleCourseStartingLocation selectedLocation = DRCObstacleCourseStartingLocation.ON_MEDIUM_PLATFORM;
      simulationTestingParameters = SimulationTestingParameters.createFromEnvironmentVariables();
      drcSimulationTestHelper = new DRCSimulationTestHelper("DRCWalkingOntoMediumPlatformToesTouchingTest", "", selectedLocation,  simulationTestingParameters, getRobotModel());
   
      SimulationConstructionSet simulationConstructionSet = drcSimulationTestHelper.getSimulationConstructionSet();
      ScriptedFootstepGenerator scriptedFootstepGenerator = drcSimulationTestHelper.createScriptedFootstepGenerator();
      
      setupCameraForWalkingOffOfMediumPlatform(simulationConstructionSet);
      
      ThreadTools.sleep(1000);
      boolean success = drcSimulationTestHelper.simulateAndBlockAndCatchExceptions(2.0);
      
      FootstepDataList footstepDataList = createFootstepsForSteppingOffOfMediumPlatform(scriptedFootstepGenerator);
      drcSimulationTestHelper.send(footstepDataList);
      
      success = success && drcSimulationTestHelper.simulateAndBlockAndCatchExceptions(4.0);
      
      drcSimulationTestHelper.createMovie(getSimpleRobotName(), 1);
      drcSimulationTestHelper.checkNothingChanged();
      
      assertTrue(success);
      
      Point3d center = new Point3d(-4.4003012528878935, -6.046150532235836, 0.7887649325247877);
      Vector3d plusMinusVector = new Vector3d(0.2, 0.2, 0.5);
      BoundingBox3d boundingBox = BoundingBox3d.createUsingCenterAndPlusMinusVector(center, plusMinusVector);
      drcSimulationTestHelper.assertRobotsRootJointIsInBoundingBox(boundingBox);
      
      BambooTools.reportTestFinishedMessage();
   }


	@EstimatedDuration(duration = 30.0)
	@Test(timeout = 149768)
	public void testWalkingOffOfMediumPlatformSlowSteps() throws SimulationExceededMaximumTimeException
	{
	   BambooTools.reportTestStartedMessage();

	   DRCObstacleCourseStartingLocation selectedLocation = DRCObstacleCourseStartingLocation.ON_MEDIUM_PLATFORM;
	   simulationTestingParameters = SimulationTestingParameters.createFromEnvironmentVariables();
	   drcSimulationTestHelper = new DRCSimulationTestHelper("DRCWalkingOntoMediumPlatformToesTouchingTest", "", selectedLocation,  simulationTestingParameters, getRobotModel());

	   SimulationConstructionSet simulationConstructionSet = drcSimulationTestHelper.getSimulationConstructionSet();
	   ScriptedFootstepGenerator scriptedFootstepGenerator = drcSimulationTestHelper.createScriptedFootstepGenerator();

	   setupCameraForWalkingOffOfMediumPlatform(simulationConstructionSet);

	   ThreadTools.sleep(1000);
	   boolean success = drcSimulationTestHelper.simulateAndBlockAndCatchExceptions(2.0);

	   FrameOrientation desiredChestFrameOrientation = new FrameOrientation(ReferenceFrame.getWorldFrame());
	   double leanAngle = 30.0;
	   desiredChestFrameOrientation.setIncludingFrame(ReferenceFrame.getWorldFrame(), -2.36, Math.toRadians(leanAngle), Math.toRadians(0.0));
      Quat4d desiredChestQuat = new Quat4d();
      desiredChestFrameOrientation.getQuaternion(desiredChestQuat);
      
      double trajectoryTime = 0.5;
      ChestOrientationPacket pitchForward = new ChestOrientationPacket(desiredChestQuat, false, trajectoryTime);
      drcSimulationTestHelper.send(pitchForward);
      
      success = success && drcSimulationTestHelper.simulateAndBlockAndCatchExceptions(0.5);

      double heightOffset = 0.12;
      double heightTrajectoryTime = 0.5;
      ComHeightPacket comHeightPacket = new ComHeightPacket(heightOffset, heightTrajectoryTime);
      drcSimulationTestHelper.send(comHeightPacket);

	   success = success && drcSimulationTestHelper.simulateAndBlockAndCatchExceptions(1.0);

	   double swingTime = 1.0;
      double transferTime = 0.6;
      FootstepDataList footstepDataList = createFootstepsForSteppingOffOfMediumPlatformNarrowFootSpacing(scriptedFootstepGenerator, swingTime, transferTime);
	   drcSimulationTestHelper.send(footstepDataList);

	   success = success && drcSimulationTestHelper.simulateAndBlockAndCatchExceptions(7.0);

	   drcSimulationTestHelper.createMovie(getSimpleRobotName(), 1);
	   drcSimulationTestHelper.checkNothingChanged();

	   assertTrue(success);

	   Point3d center = new Point3d(-4.4003012528878935, -6.046150532235836, 0.7887649325247877);
	   Vector3d plusMinusVector = new Vector3d(0.2, 0.2, 0.5);
	   BoundingBox3d boundingBox = BoundingBox3d.createUsingCenterAndPlusMinusVector(center, plusMinusVector);
	   drcSimulationTestHelper.assertRobotsRootJointIsInBoundingBox(boundingBox);

	   BambooTools.reportTestFinishedMessage();
	}

	private void setupCameraForWalkingOverSmallPlatform(SimulationConstructionSet scs)
   {
      Point3d cameraFix = new Point3d(-3.0, -4.6, 0.8);
      Point3d cameraPosition = new Point3d(-11.5, -5.8, 2.5);

      drcSimulationTestHelper.setupCameraForUnitTest(cameraFix, cameraPosition);
   }
   
   private void setupCameraForWalkingOverMediumPlatform(SimulationConstructionSet scs)
   {
      Point3d cameraFix = new Point3d(-3.9, -5.6, 0.55);
      Point3d cameraPosition = new Point3d(-7.5, -2.3, 0.58);

      drcSimulationTestHelper.setupCameraForUnitTest(cameraFix, cameraPosition);
   }
   
   private void setupCameraForWalkingOffOfMediumPlatform(SimulationConstructionSet scs)
   {
      Point3d cameraFix = new Point3d(-3.9, -5.6, 0.55);
      Point3d cameraPosition = new Point3d(-7.6, -2.4, 0.58);

      drcSimulationTestHelper.setupCameraForUnitTest(cameraFix, cameraPosition);
   }


   private FootstepDataList createFootstepsForSteppingOntoSmallPlatform(ScriptedFootstepGenerator scriptedFootstepGenerator)
   {
      double[][][] footstepLocationsAndOrientations = new double[][][]
            {{{-3.3303508964136372, -5.093152916934431, 0.2361869051765919}, {-0.003380023644676521, 0.01519186055257256, 0.9239435001894032, -0.3822122332825927}},
            {{-3.4980005080184333, -4.927710662235891, 0.23514263035532196}, {-6.366244432153206E-4, -2.2280928201561157E-4, 0.9240709626189128, -0.3822203567445069}}
            };

      RobotSide[] robotSides = drcSimulationTestHelper.createRobotSidesStartingFrom(RobotSide.LEFT, footstepLocationsAndOrientations.length);
      return scriptedFootstepGenerator.generateFootstepsFromLocationsAndOrientations(robotSides, footstepLocationsAndOrientations);
   }

   private FootstepDataList createFootstepsForSideSteppingOverSmallPlatform()
   {
      Quat4d orientation = new Quat4d();
      Vector3d verticalVector = new Vector3d(0.0, 0.0, 1.0);
      FootstepDataList footstepDataList = new FootstepDataList();
      RotationFunctions.getQuaternionFromYawAndZNormal(3.0/4.0*Math.PI, verticalVector, orientation);
      footstepDataList.add(new FootstepData(RobotSide.LEFT, new Point3d(-3.40, -5.03, .24), new Quat4d(orientation)));
      footstepDataList.add(new FootstepData(RobotSide.RIGHT, new Point3d(-3.22, -4.85, 0.0), new Quat4d(orientation)));
      footstepDataList.add(new FootstepData(RobotSide.LEFT, new Point3d(-3.50, -5.13, .24), new Quat4d(orientation)));
      footstepDataList.add(new FootstepData(RobotSide.RIGHT, new Point3d(-3.37, -5.00, .24), new Quat4d(orientation)));
      footstepDataList.add(new FootstepData(RobotSide.LEFT, new Point3d(-3.67, -5.30, 0.0), new Quat4d(orientation)));
      footstepDataList.add(new FootstepData(RobotSide.RIGHT, new Point3d(-3.50, -5.13, .24), new Quat4d(orientation)));
      footstepDataList.add(new FootstepData(RobotSide.LEFT, new Point3d(-3.84, -5.47, 0.0), new Quat4d(orientation)));
      footstepDataList.add(new FootstepData(RobotSide.RIGHT, new Point3d(-3.67, -5.30, 0.0), new Quat4d(orientation)));
      return footstepDataList;
   }

   private FootstepDataList createFootstepsForSideSteppingOverSmallWall()
   {
      Quat4d orientation = new Quat4d();
      Vector3d verticalVector = new Vector3d(0.0, 0.0, 1.0);
      FootstepDataList footstepDataList = new FootstepDataList();
      RotationFunctions.getQuaternionFromYawAndZNormal(3.0/4.0*Math.PI, verticalVector, orientation);
      footstepDataList.add(new FootstepData(RobotSide.LEFT, new Point3d(-4.30, -3.93, 0.0), new Quat4d(orientation)));
      footstepDataList.add(new FootstepData(RobotSide.RIGHT, new Point3d(-4.18, -3.81, 0.0), new Quat4d(orientation)));
      footstepDataList.add(new FootstepData(RobotSide.LEFT, new Point3d(-4.58, -4.21, 0.0), new Quat4d(orientation), TrajectoryType.OBSTACLE_CLEARANCE, 0.24));
      footstepDataList.add(new FootstepData(RobotSide.RIGHT, new Point3d(-4.30, -3.93, 0.0), new Quat4d(orientation)));
      footstepDataList.add(new FootstepData(RobotSide.LEFT, new Point3d(-4.71, -4.34, 0.0), new Quat4d(orientation)));
      footstepDataList.add(new FootstepData(RobotSide.RIGHT, new Point3d(-4.58, -4.21, 0.0), new Quat4d(orientation), TrajectoryType.OBSTACLE_CLEARANCE, 0.24));
      footstepDataList.add(new FootstepData(RobotSide.LEFT, new Point3d(-4.84, -4.47, 0.0), new Quat4d(orientation)));
      footstepDataList.add(new FootstepData(RobotSide.RIGHT, new Point3d(-4.67, -4.30, 0.0), new Quat4d(orientation)));
      return footstepDataList;
   }

   private FootstepDataList createFootstepsForSteppingOffOfSmallPlatform(ScriptedFootstepGenerator scriptedFootstepGenerator)
   {
      double[][][] footstepLocationsAndOrientations = new double[][][]
            {{{-3.850667406347062, -5.249955436839419, 0.08402883817600326}, {-0.0036296745847858064, 0.003867481752280881, 0.9236352342329301, -0.38323598752046323}},
            {{-3.6725725349280296, -5.446807690769805, 0.08552806597763604}, {-6.456929194763128E-5, -0.01561897825296648, 0.9234986484659182, -0.3832835629540643}}
            };

      RobotSide[] robotSides = drcSimulationTestHelper.createRobotSidesStartingFrom(RobotSide.RIGHT, footstepLocationsAndOrientations.length);
      return scriptedFootstepGenerator.generateFootstepsFromLocationsAndOrientations(robotSides, footstepLocationsAndOrientations);
   }

   private FootstepDataList createFootstepsForSteppingPastSmallPlatform(ScriptedFootstepGenerator scriptedFootstepGenerator)
   {
      double[][][] footstepLocationsAndOrientations = new double[][][]
            {{{-3.3303508964136372, -5.093152916934431, 0.2361869051765919}, {-0.003380023644676521, 0.01519186055257256, 0.9239435001894032, -0.3822122332825927}},
            {{-3.850667406347062, -5.249955436839419, 0.08402883817600326}, {-0.0036296745847858064, 0.003867481752280881, 0.9236352342329301, -0.38323598752046323}},
            {{-3.6725725349280296, -5.446807690769805, 0.08552806597763604}, {-6.456929194763128E-5, -0.01561897825296648, 0.9234986484659182, -0.3832835629540643}}
            };

      RobotSide[] robotSides = drcSimulationTestHelper.createRobotSidesStartingFrom(RobotSide.LEFT, footstepLocationsAndOrientations.length);
      FootstepDataList desiredFootsteps = scriptedFootstepGenerator.generateFootstepsFromLocationsAndOrientations(robotSides, footstepLocationsAndOrientations);
      double zClearHeight = desiredFootsteps.get(0).getLocation().getZ() + 0.07;
      double swingHeightForClear = zClearHeight - desiredFootsteps.get(2).getLocation().getZ(); //should really be the last height (height before swing), not step 2, but they're approximate.
      desiredFootsteps.get(1).setSwingHeight(swingHeightForClear);
      desiredFootsteps.get(1).setTrajectoryType(TrajectoryType.OBSTACLE_CLEARANCE);
      return desiredFootsteps;
   }

   private FootstepDataList createFootstepsForSteppingOntoMediumPlatform(ScriptedFootstepGenerator scriptedFootstepGenerator)
   {
      double[][][] footstepLocationsAndOrientations = new double[][][]
            {{{-4.144889177599215, -5.68009276450442, 0.2841471307289875}, {-0.012979910123161926, 0.017759854548746876, 0.9232071519598507, -0.3836726001029824}},
            {{-3.997325285359919, -5.8527640256176685, 0.2926905844610473}, {-0.022159348866436335, -0.014031420240348416, 0.9230263369316307, -0.3838417171627259}}
            };
      
      RobotSide[] robotSides = drcSimulationTestHelper.createRobotSidesStartingFrom(RobotSide.RIGHT, footstepLocationsAndOrientations.length);
      return scriptedFootstepGenerator.generateFootstepsFromLocationsAndOrientations(robotSides, footstepLocationsAndOrientations);
   }
   
   private FootstepDataList createFootstepsForSteppingOffOfMediumPlatform(ScriptedFootstepGenerator scriptedFootstepGenerator)
   {
      double[][][] footstepLocationsAndOrientations = new double[][][]
            {{{-4.304392715667327, -6.084498586699763, 0.08716704456087025}, {-0.0042976203878775715, -0.010722204803598987, 0.9248070170408506, -0.38026115501738456}},
            {{-4.4394706079327255, -5.9465856725464565, 0.08586305720146342}, {-8.975861226689934E-4, 0.002016837110644428, 0.9248918980282926, -0.380223754740342}},
            };
      
      RobotSide[] robotSides = drcSimulationTestHelper.createRobotSidesStartingFrom(RobotSide.LEFT, footstepLocationsAndOrientations.length);
      return scriptedFootstepGenerator.generateFootstepsFromLocationsAndOrientations(robotSides, footstepLocationsAndOrientations);
   }
   
   private FootstepDataList createFootstepsForSteppingOffOfMediumPlatformNarrowFootSpacing(ScriptedFootstepGenerator scriptedFootstepGenerator, double swingTime, double transferTime)
   {
      double[][][] footstepLocationsAndOrientations = new double[][][]
            {{{-4.27, -5.67, 0.28}, {-8.975861226689934E-4, 0.002016837110644428, 0.9248918980282926, -0.380223754740342}},
            {{-4.34, -6.0, 0.08716704456087025}, {-0.0042976203878775715, -0.010722204803598987, 0.9248070170408506, -0.38026115501738456}},
            {{-4.5, -5.9465856725464565, 0.08586305720146342}, {-8.975861226689934E-4, 0.002016837110644428, 0.9248918980282926, -0.380223754740342}},
            };
      
      RobotSide[] robotSides = drcSimulationTestHelper.createRobotSidesStartingFrom(RobotSide.RIGHT, footstepLocationsAndOrientations.length);
      return scriptedFootstepGenerator.generateFootstepsFromLocationsAndOrientations(robotSides, footstepLocationsAndOrientations, swingTime, transferTime);
   }
   
 

}