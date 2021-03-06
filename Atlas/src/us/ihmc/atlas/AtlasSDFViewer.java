package us.ihmc.atlas;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import us.ihmc.simulationconstructionset.FloatingRootJointRobot;
import us.ihmc.humanoidRobotics.HumanoidFloatingRootJointRobot;
import us.ihmc.darpaRoboticsChallenge.drcRobot.DRCRobotModel;
import us.ihmc.graphics3DAdapter.graphics.Graphics3DObject;
import us.ihmc.graphics3DAdapter.graphics.appearances.AppearanceDefinition;
import us.ihmc.graphics3DAdapter.graphics.appearances.YoAppearance;
import us.ihmc.robotics.geometry.RigidBodyTransform;
import us.ihmc.simulationconstructionset.IMUMount;
import us.ihmc.simulationconstructionset.Joint;
import us.ihmc.simulationconstructionset.Link;
import us.ihmc.simulationconstructionset.OneDegreeOfFreedomJoint;
import us.ihmc.simulationconstructionset.SimulationConstructionSet;

public class AtlasSDFViewer
{
   private static final boolean SHOW_ELLIPSOIDS = false;
   private static final boolean SHOW_COORDINATES_AT_JOINT_ORIGIN = false;
   private static final boolean SHOW_IMU_FRAMES = true;

   public static void main(String[] args)
   {
      DRCRobotModel robotModel = new AtlasRobotModel(AtlasRobotVersion.ATLAS_UNPLUGGED_V5_NO_HANDS, DRCRobotModel.RobotTarget.SCS, false);
      HumanoidFloatingRootJointRobot sdfRobot = robotModel.createHumanoidFloatingRootJointRobot(false);

      if (SHOW_ELLIPSOIDS)
      {
         addIntertialEllipsoidsToVisualizer(sdfRobot);
      }

      if (SHOW_COORDINATES_AT_JOINT_ORIGIN)
      {
         addJointAxis(sdfRobot);
      }

      if (SHOW_IMU_FRAMES)
      {
         showIMUFrames(sdfRobot);
      }

      SimulationConstructionSet scs = new SimulationConstructionSet(sdfRobot);
      scs.setGroundVisible(false);
      scs.startOnAThread();
   }


   private static void showIMUFrames(HumanoidFloatingRootJointRobot sdfRobot)
   {
      ArrayList<IMUMount> imuMounts = new ArrayList<>();
      sdfRobot.getIMUMounts(imuMounts);

      for (IMUMount imuMount : imuMounts)
      {
         Link imuLink = imuMount.getParentJoint().getLink();
         if (imuLink.getLinkGraphics() == null)
            imuLink.setLinkGraphics(new Graphics3DObject());

         Graphics3DObject linkGraphics = imuLink.getLinkGraphics();
         linkGraphics.identity();
         RigidBodyTransform mountToJoint = new RigidBodyTransform();
         imuMount.getTransformFromMountToJoint(mountToJoint);
         linkGraphics.transform(mountToJoint);
         linkGraphics.addCoordinateSystem(0.3);
         linkGraphics.identity();
      }
   }


   private static void addIntertialEllipsoidsToVisualizer(FloatingRootJointRobot sdfRobot)
   {
      ArrayList<Joint> joints = new ArrayList<>();
      joints.add(sdfRobot.getRootJoint());

      HashSet<Link> links = getAllLinks(joints, new HashSet<Link>());

      for (Link link : links)
      {
         if (link.getLinkGraphics() == null)
            link.setLinkGraphics(new Graphics3DObject());

         AppearanceDefinition appearance = YoAppearance.Green();
         appearance.setTransparency(0.6);
         link.addEllipsoidFromMassProperties(appearance);
         link.addCoordinateSystemToCOM(0.1);
      }
   }

   private static HashSet<Link> getAllLinks(ArrayList<Joint> joints, HashSet<Link> links)
   {
      for (Joint joint : joints)
      {
         links.add(joint.getLink());

         if (!joint.getChildrenJoints().isEmpty())
         {
            links.addAll(getAllLinks(joint.getChildrenJoints(), links));
         }
      }

      return links;
   }

   public static void addJointAxis(FloatingRootJointRobot sdfRobot)
   {
      ArrayList<OneDegreeOfFreedomJoint> joints = new ArrayList<>(Arrays.asList(sdfRobot.getOneDegreeOfFreedomJoints()));

      for (OneDegreeOfFreedomJoint joint : joints)
      {
         Graphics3DObject linkGraphics = new Graphics3DObject();
         linkGraphics.addCoordinateSystem(0.1);
         linkGraphics.combine(joint.getLink().getLinkGraphics());
         joint.getLink().setLinkGraphics(linkGraphics);
      }
   }
}
