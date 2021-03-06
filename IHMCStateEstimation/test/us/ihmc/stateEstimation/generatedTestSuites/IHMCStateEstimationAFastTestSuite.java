package us.ihmc.stateEstimation.generatedTestSuites;

import org.junit.runner.RunWith;
import org.junit.runners.Suite.SuiteClasses;

import us.ihmc.tools.continuousIntegration.ContinuousIntegrationSuite;
import us.ihmc.tools.continuousIntegration.ContinuousIntegrationSuite.ContinuousIntegrationSuiteCategory;
import us.ihmc.tools.continuousIntegration.IntegrationCategory;

/** WARNING: AUTO-GENERATED FILE. DO NOT MAKE MANUAL CHANGES TO THIS FILE. **/
@RunWith(ContinuousIntegrationSuite.class)
@ContinuousIntegrationSuiteCategory(IntegrationCategory.FAST)
@SuiteClasses
({
   us.ihmc.stateEstimation.humanoid.kinematicsBasedStateEstimation.ClippedSpeedOffsetErrorInterpolatorTest.class,
   us.ihmc.stateEstimation.humanoid.kinematicsBasedStateEstimation.JointStateUpdaterTest.class,
   us.ihmc.stateEstimation.humanoid.kinematicsBasedStateEstimation.NewPelvisPoseHistoryCorrectionTest.class,
   us.ihmc.stateEstimation.humanoid.kinematicsBasedStateEstimation.OutdatedPoseToUpToDateReferenceFrameUpdaterTest.class,
   us.ihmc.stateEstimation.humanoid.kinematicsBasedStateEstimation.PelvisRotationalStateUpdaterTest.class
})

public class IHMCStateEstimationAFastTestSuite
{
   public static void main(String[] args)
   {

   }
}
