package us.ihmc.valkyrie;

import java.util.List;

import javax.vecmath.Quat4d;
import javax.vecmath.Vector3d;

import us.ihmc.SdfLoader.SDFRobot;
import us.ihmc.darpaRoboticsChallenge.initialSetup.DRCRobotInitialSetup;
import us.ihmc.simulationconstructionset.GroundContactPoint;
import us.ihmc.utilities.humanoidRobot.partNames.ArmJointName;
import us.ihmc.utilities.humanoidRobot.partNames.LegJointName;
import us.ihmc.utilities.math.geometry.FrameOrientation;
import us.ihmc.utilities.math.geometry.ReferenceFrame;
import us.ihmc.utilities.math.geometry.RigidBodyTransform;
import us.ihmc.utilities.robotSide.RobotSide;
import us.ihmc.wholeBodyController.DRCRobotJointMap;

public class ValkyrieInitialSetup implements DRCRobotInitialSetup<SDFRobot>
{
   private double groundZ;
   private double initialYaw;
   private final RigidBodyTransform rootToWorld = new RigidBodyTransform();
   private final Vector3d positionInWorld = new Vector3d();
   private final Vector3d offset = new Vector3d();
   private final Quat4d rotation = new Quat4d();
   private boolean robotInitialized = false;

   public ValkyrieInitialSetup(double groundZ, double initialYaw)
   {
      this.groundZ = groundZ;
      this.initialYaw = initialYaw;
   }

   @Override
   public void initializeRobot(SDFRobot robot, DRCRobotJointMap jointMap)
   {
      if(!robotInitialized)
      {
         setActuatorPositions(robot, jointMap);
         positionRobotInWorld(robot);
         robotInitialized = true;
      }
   }
   
   private void setActuatorPositions(SDFRobot robot, DRCRobotJointMap jointMap)
   {
      for (RobotSide robotSide : RobotSide.values)
      {
         String hipPitch = jointMap.getLegJointName(robotSide, LegJointName.HIP_PITCH);
         String knee = jointMap.getLegJointName(robotSide, LegJointName.KNEE);
         String anklePitch = jointMap.getLegJointName(robotSide, LegJointName.ANKLE_PITCH);
         String hipRoll = jointMap.getLegJointName(robotSide, LegJointName.HIP_ROLL);
         String ankleRoll = jointMap.getLegJointName(robotSide, LegJointName.ANKLE_ROLL);
         
         robot.getOneDegreeOfFreedomJoint(hipPitch).setQ(-0.6);
         robot.getOneDegreeOfFreedomJoint(knee).setQ(1.3);
         robot.getOneDegreeOfFreedomJoint(anklePitch).setQ(-0.65);
         robot.getOneDegreeOfFreedomJoint(hipRoll).setQ(0.0);
         robot.getOneDegreeOfFreedomJoint(ankleRoll).setQ(0.0);

         String shoulderRoll = jointMap.getArmJointName(robotSide, ArmJointName.SHOULDER_ROLL);
         String shoulderPitch = jointMap.getArmJointName(robotSide, ArmJointName.SHOULDER_PITCH);
         String elbowPitch = jointMap.getArmJointName(robotSide, ArmJointName.ELBOW_PITCH);
         String elbowRoll = jointMap.getArmJointName(robotSide, ArmJointName.ELBOW_ROLL);
         
         if (shoulderRoll != null)
            robot.getOneDegreeOfFreedomJoint(shoulderRoll).setQ(robotSide.negateIfRightSide(-1.2));//inv
         if (shoulderPitch != null)
            robot.getOneDegreeOfFreedomJoint(shoulderPitch).setQ(-0.2);
         if (elbowPitch != null)
            robot.getOneDegreeOfFreedomJoint(elbowPitch).setQ(robotSide.negateIfRightSide(-1.5));//inv
         if (elbowRoll != null)
            robot.getOneDegreeOfFreedomJoint(elbowRoll).setQ(1.3);
      }
      robot.update();
   }
   
   private void positionRobotInWorld(SDFRobot robot)
   {
      robot.getRootJointToWorldTransform(rootToWorld);
      rootToWorld.get(rotation, positionInWorld);
      positionInWorld.setZ(groundZ + getPelvisToFoot(robot));
      positionInWorld.add(offset);
      robot.setPositionInWorld(positionInWorld);
      
      FrameOrientation frameOrientation = new FrameOrientation(ReferenceFrame.getWorldFrame(), rotation);
      double[] yawPitchRoll = frameOrientation.getYawPitchRoll();
      yawPitchRoll[0] = initialYaw;
      frameOrientation.setYawPitchRoll(yawPitchRoll);
      
      robot.setOrientation(frameOrientation.getQuaternionCopy());
      robot.update();
   }
   
   private double getPelvisToFoot(SDFRobot robot)
   {
      List<GroundContactPoint> contactPoints = robot.getFootGroundContactPoints(RobotSide.LEFT);
      double height = Double.POSITIVE_INFINITY;
      for(GroundContactPoint gc : contactPoints)
      {
         if(gc.getPositionPoint().getZ() < height)
         {
            height = gc.getPositionPoint().getZ();
         }
      }
      return offset.getZ() - height;
   }
   
   public void getOffset(Vector3d offsetToPack)
   {
      offsetToPack.set(offset);
   }

   public void setOffset(Vector3d offset)
   {
      this.offset.set(offset);
   }

   @Override
   public void setInitialYaw(double yaw)
   {
      initialYaw = yaw;
   }

   @Override
   public void setInitialGroundHeight(double groundHeight)
   {
      groundZ = groundHeight;
   }

   @Override
   public double getInitialYaw()
   {
      return initialYaw;
   }

   @Override
   public double getInitialGroundHeight()
   {
      return groundZ;
   }
}