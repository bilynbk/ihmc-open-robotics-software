package us.ihmc.quadrupedRobotics.controller.force;

import us.ihmc.quadrupedRobotics.estimator.GroundPlaneEstimator;
import us.ihmc.quadrupedRobotics.model.QuadrupedPhysicalProperties;
import us.ihmc.quadrupedRobotics.model.QuadrupedRuntimeEnvironment;
import us.ihmc.quadrupedRobotics.estimator.referenceFrames.QuadrupedReferenceFrames;
import us.ihmc.quadrupedRobotics.controller.force.toolbox.*;
import us.ihmc.robotics.dataStructures.registry.YoVariableRegistry;
import us.ihmc.robotics.robotSide.QuadrantDependentList;
import us.ihmc.robotics.robotSide.RobotQuadrant;

public class QuadrupedForceControllerToolbox
{
   private final QuadrupedReferenceFrames referenceFrames;
   private final QuadrupedTaskSpaceEstimator taskSpaceEstimator;
   private final QuadrupedTaskSpaceController taskSpaceController;
   private final LinearInvertedPendulumModel linearInvertedPendulumModel;
   private final DivergentComponentOfMotionEstimator dcmPositionEstimator;
   private final DivergentComponentOfMotionController dcmPositionController;
   private final QuadrupedComPositionController comPositionController;
   private final QuadrupedBodyOrientationController bodyOrientationController;
   private final QuadrantDependentList<QuadrupedSolePositionController> solePositionController;
   private final QuadrupedTimedStepController timedStepController;
   private final GroundPlaneEstimator groundPlaneEstimator;
   private final QuadrupedFallDetector fallDetector;
   private final QuadrupedSoleWaypointController soleWaypointController;

   public QuadrupedForceControllerToolbox(QuadrupedRuntimeEnvironment runtimeEnvironment, QuadrupedPhysicalProperties physicalProperties, YoVariableRegistry registry)
   {
      double mass = runtimeEnvironment.getFullRobotModel().getTotalMass();
      double gravity = 9.81;

      // create controllers and estimators
      referenceFrames = new QuadrupedReferenceFrames(runtimeEnvironment.getFullRobotModel(), physicalProperties);
      taskSpaceEstimator = new QuadrupedTaskSpaceEstimator(runtimeEnvironment.getFullRobotModel(), referenceFrames, registry, runtimeEnvironment.getGraphicsListRegistry());
      taskSpaceController = new QuadrupedTaskSpaceController(runtimeEnvironment.getFullRobotModel(), referenceFrames, runtimeEnvironment.getControlDT(), registry, runtimeEnvironment.getGraphicsListRegistry());
      linearInvertedPendulumModel = new LinearInvertedPendulumModel(referenceFrames.getCenterOfMassZUpFrame(), mass, gravity, 1.0, registry);
      dcmPositionEstimator = new DivergentComponentOfMotionEstimator(referenceFrames.getCenterOfMassZUpFrame(), linearInvertedPendulumModel, registry, runtimeEnvironment.getGraphicsListRegistry());
      dcmPositionController = new DivergentComponentOfMotionController(referenceFrames.getCenterOfMassZUpFrame(), runtimeEnvironment.getControlDT(), linearInvertedPendulumModel, registry, runtimeEnvironment.getGraphicsListRegistry());
      comPositionController = new QuadrupedComPositionController(referenceFrames.getCenterOfMassZUpFrame(), runtimeEnvironment.getControlDT(), registry);
      bodyOrientationController = new QuadrupedBodyOrientationController(referenceFrames.getBodyFrame(), runtimeEnvironment.getControlDT(), registry);
      solePositionController = new QuadrantDependentList<>();
      for (RobotQuadrant robotQuadrant : RobotQuadrant.values)
      {
         solePositionController.set(robotQuadrant,
               new QuadrupedSolePositionController(robotQuadrant, referenceFrames.getFootReferenceFrames().get(robotQuadrant),
                     runtimeEnvironment.getControlDT(), registry));
      }
      timedStepController = new QuadrupedTimedStepController(solePositionController, runtimeEnvironment.getRobotTimestamp(), registry, runtimeEnvironment.getGraphicsListRegistry());
      soleWaypointController = new QuadrupedSoleWaypointController(referenceFrames.getBodyFrame(), solePositionController, runtimeEnvironment.getRobotTimestamp(), registry);
      groundPlaneEstimator = new GroundPlaneEstimator(registry, runtimeEnvironment.getGraphicsListRegistry());
      fallDetector = new QuadrupedFallDetector(taskSpaceEstimator, dcmPositionEstimator, registry);
   }

   public QuadrupedReferenceFrames getReferenceFrames()
   {
      return referenceFrames;
   }

   public QuadrupedTaskSpaceEstimator getTaskSpaceEstimator()
   {
      return taskSpaceEstimator;
   }

   public QuadrupedTaskSpaceController getTaskSpaceController()
   {
      return taskSpaceController;
   }

   public LinearInvertedPendulumModel getLinearInvertedPendulumModel()
   {
      return linearInvertedPendulumModel;
   }

   public DivergentComponentOfMotionEstimator getDcmPositionEstimator()
   {
      return dcmPositionEstimator;
   }

   public DivergentComponentOfMotionController getDcmPositionController()
   {
      return dcmPositionController;
   }

   public QuadrupedComPositionController getComPositionController()
   {
      return comPositionController;
   }

   public QuadrupedBodyOrientationController getBodyOrientationController()
   {
      return bodyOrientationController;
   }

   public QuadrantDependentList<QuadrupedSolePositionController> getSolePositionController()
   {
      return solePositionController;
   }

   public QuadrupedSolePositionController getSolePositionController(RobotQuadrant robotQuadrant)
   {
      return solePositionController.get(robotQuadrant);
   }

   public QuadrupedTimedStepController getTimedStepController()
   {
      return timedStepController;
   }

   public GroundPlaneEstimator getGroundPlaneEstimator()
   {
      return groundPlaneEstimator;
   }

   public QuadrupedFallDetector getFallDetector()
   {
      return fallDetector;
   }

   public QuadrupedSoleWaypointController getSoleWaypointController()
   {
      return soleWaypointController;
   }
}
