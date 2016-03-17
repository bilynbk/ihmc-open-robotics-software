package us.ihmc.commonWalkingControlModules.highLevelHumanoidControl.factories;

import us.ihmc.SdfLoader.models.FullHumanoidRobotModel;
import us.ihmc.commonWalkingControlModules.configurations.ArmControllerParameters;
import us.ihmc.commonWalkingControlModules.configurations.CapturePointPlannerParameters;
import us.ihmc.commonWalkingControlModules.configurations.WalkingControllerParameters;
import us.ihmc.commonWalkingControlModules.controlModules.ChestOrientationManager;
import us.ihmc.commonWalkingControlModules.controlModules.PelvisOrientationManager;
import us.ihmc.commonWalkingControlModules.controlModules.foot.FeetManager;
import us.ihmc.commonWalkingControlModules.controlModules.head.HeadOrientationManager;
import us.ihmc.commonWalkingControlModules.controllerAPI.output.ControllerStatusOutputManager;
import us.ihmc.commonWalkingControlModules.controllerCore.command.feedbackController.FeedbackControlCommandList;
import us.ihmc.commonWalkingControlModules.highLevelHumanoidControl.manipulation.ManipulationControlModule;
import us.ihmc.commonWalkingControlModules.instantaneousCapturePoint.BalanceManager;
import us.ihmc.commonWalkingControlModules.instantaneousCapturePoint.CenterOfMassHeightManager;
import us.ihmc.commonWalkingControlModules.momentumBasedController.MomentumBasedController;
import us.ihmc.commonWalkingControlModules.momentumBasedController.optimization.MomentumOptimizationSettings;
import us.ihmc.robotics.controllers.YoOrientationPIDGainsInterface;
import us.ihmc.robotics.dataStructures.registry.YoVariableRegistry;
import us.ihmc.robotics.robotSide.RobotSide;

public class VariousWalkingManagers
{
   private final BalanceManager balanceManager;
   private final CenterOfMassHeightManager centerOfMassHeightManager;
   private final HeadOrientationManager headOrientationManager;
   private final ChestOrientationManager chestOrientationManager;
   private final ManipulationControlModule manipulationControlModule;
   private final FeetManager feetManager;
   private final PelvisOrientationManager pelvisOrientationManager;

   public VariousWalkingManagers(ControllerStatusOutputManager statusOutputManager, MomentumBasedController momentumBasedController,
         WalkingControllerParameters walkingControllerParameters, CapturePointPlannerParameters capturePointPlannerParameters,
         ArmControllerParameters armControlParameters, YoVariableRegistry registry)
   {
      FullHumanoidRobotModel fullRobotModel = momentumBasedController.getFullRobotModel();
      MomentumOptimizationSettings momentumOptimizationSettings = walkingControllerParameters.getMomentumOptimizationSettings();

      balanceManager = new BalanceManager(momentumBasedController, walkingControllerParameters, capturePointPlannerParameters, registry);
      balanceManager.setMomentumWeight(momentumOptimizationSettings.getLinearMomentumWeight());

      centerOfMassHeightManager = new CenterOfMassHeightManager(momentumBasedController, walkingControllerParameters, registry);

      if (fullRobotModel.getHead() != null)
      {
         YoOrientationPIDGainsInterface headControlGains = walkingControllerParameters.createHeadOrientationControlGains(registry);
         double[] initialHeadYawPitchRoll = walkingControllerParameters.getInitialHeadYawPitchRoll();
         double headWeight = momentumOptimizationSettings.getHeadWeight();
         headOrientationManager = new HeadOrientationManager(momentumBasedController, walkingControllerParameters, headControlGains, headWeight, initialHeadYawPitchRoll,
               registry);
      }
      else
      {
         headOrientationManager = null;
      }

      if (fullRobotModel.getChest() != null)
      {
         double trajectoryTimeHeadOrientation = walkingControllerParameters.getTrajectoryTimeHeadOrientation();
         YoOrientationPIDGainsInterface chestControlGains = walkingControllerParameters.createChestControlGains(registry);
         double chestWeight = momentumOptimizationSettings.getChestWeight();
         chestOrientationManager = new ChestOrientationManager(momentumBasedController, chestControlGains, chestWeight, trajectoryTimeHeadOrientation, registry);
      }
      else
      {
         chestOrientationManager = null;
      }

      if (fullRobotModel.getChest() != null && fullRobotModel.getHand(RobotSide.LEFT) != null && fullRobotModel.getHand(RobotSide.RIGHT) != null)
      {
         // Setup arm+hand manipulation state machines
         manipulationControlModule = new ManipulationControlModule(armControlParameters, momentumBasedController, registry);
         double handJointspaceWeight = momentumOptimizationSettings.getHandJointspaceWeight();
         double handTaskspaceWeight = momentumOptimizationSettings.getHandTaskspaceWeight();
         double handUserModeWeight = momentumOptimizationSettings.getHandUserModeWeight();
         manipulationControlModule.setWeights(handJointspaceWeight, handTaskspaceWeight, handUserModeWeight);
      }
      else
      {
         manipulationControlModule = null;
      }

      feetManager = new FeetManager(momentumBasedController, walkingControllerParameters, registry);
      double highFootWeight = momentumOptimizationSettings.getHighFootWeight();
      double defaultFootWeight = momentumOptimizationSettings.getDefaultFootWeight();
      feetManager.setWeights(highFootWeight, defaultFootWeight);

      pelvisOrientationManager = new PelvisOrientationManager(walkingControllerParameters, momentumBasedController, registry);
      pelvisOrientationManager.setWeight(momentumOptimizationSettings.getPelvisWeight());
   }

   public void initializeManagers()
   {
      if (balanceManager != null)
         balanceManager.initialize();
      if (centerOfMassHeightManager != null)
         centerOfMassHeightManager.initialize();
      if (manipulationControlModule != null)
         manipulationControlModule.initialize();
      if (headOrientationManager != null)
         headOrientationManager.initialize();
      if (chestOrientationManager != null)
         chestOrientationManager.initialize();
   }

   public BalanceManager getBalanceManager()
   {
      return balanceManager;
   }

   public CenterOfMassHeightManager getCenterOfMassHeightManager()
   {
      return centerOfMassHeightManager;
   }

   public HeadOrientationManager getHeadOrientationManager()
   {
      return headOrientationManager;
   }

   public ChestOrientationManager getChestOrientationManager()
   {
      return chestOrientationManager;
   }

   public ManipulationControlModule getManipulationControlModule()
   {
      return manipulationControlModule;
   }

   public FeetManager getFeetManager()
   {
      return feetManager;
   }

   public PelvisOrientationManager getPelvisOrientationManager()
   {
      return pelvisOrientationManager;
   }

   public FeedbackControlCommandList createFeedbackControlTemplate()
   {
      FeedbackControlCommandList ret = new FeedbackControlCommandList();

      if (manipulationControlModule != null)
      {
         FeedbackControlCommandList template = manipulationControlModule.createFeedbackControlTemplate();
         for (int i = 0; i < template.getNumberOfCommands(); i++)
            ret.addCommand(template.getCommand(i));
      }

      if (feetManager != null)
      {
         FeedbackControlCommandList template = feetManager.createFeedbackControlTemplate();
         for (int i = 0; i < template.getNumberOfCommands(); i++)
            ret.addCommand(template.getCommand(i));
      }

      if (headOrientationManager != null)
      {
         ret.addCommand(headOrientationManager.getFeedbackControlCommand());
      }

      if (chestOrientationManager != null)
      {
         ret.addCommand(chestOrientationManager.getFeedbackControlCommand());
      }

      if (pelvisOrientationManager != null)
      {
         ret.addCommand(pelvisOrientationManager.getFeedbackControlCommand());
      }

      return ret;
   }
}
