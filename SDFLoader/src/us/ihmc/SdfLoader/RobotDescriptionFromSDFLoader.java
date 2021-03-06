package us.ihmc.SdfLoader;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import javax.vecmath.Matrix3d;
import javax.vecmath.Quat4d;
import javax.vecmath.Vector3d;
import javax.xml.bind.JAXBException;

import org.apache.commons.lang3.tuple.ImmutablePair;

import us.ihmc.SdfLoader.xmlDescription.SDFSensor;
import us.ihmc.SdfLoader.xmlDescription.SDFSensor.Camera;
import us.ihmc.SdfLoader.xmlDescription.SDFSensor.IMU;
import us.ihmc.SdfLoader.xmlDescription.SDFSensor.IMU.IMUNoise;
import us.ihmc.SdfLoader.xmlDescription.SDFSensor.IMU.IMUNoise.NoiseParameters;
import us.ihmc.SdfLoader.xmlDescription.SDFSensor.Ray;
import us.ihmc.SdfLoader.xmlDescription.SDFSensor.Ray.Noise;
import us.ihmc.SdfLoader.xmlDescription.SDFSensor.Ray.Range;
import us.ihmc.SdfLoader.xmlDescription.SDFSensor.Ray.Scan;
import us.ihmc.SdfLoader.xmlDescription.SDFSensor.Ray.Scan.HorizontalScan;
import us.ihmc.SdfLoader.xmlDescription.SDFSensor.Ray.Scan.VerticalScan;
import us.ihmc.graphics3DAdapter.graphics.Graphics3DObject;
import us.ihmc.graphics3DAdapter.graphics.appearances.YoAppearance;
import us.ihmc.robotics.geometry.InertiaTools;
import us.ihmc.robotics.geometry.RigidBodyTransform;
import us.ihmc.robotics.lidar.LidarScanParameters;
import us.ihmc.robotics.partNames.JointNameMap;
import us.ihmc.robotics.robotDescription.CameraSensorDescription;
import us.ihmc.robotics.robotDescription.ExternalForcePointDescription;
import us.ihmc.robotics.robotDescription.FloatingJointDescription;
import us.ihmc.robotics.robotDescription.ForceSensorDescription;
import us.ihmc.robotics.robotDescription.GroundContactPointDescription;
import us.ihmc.robotics.robotDescription.IMUSensorDescription;
import us.ihmc.robotics.robotDescription.JointDescription;
import us.ihmc.robotics.robotDescription.LidarSensorDescription;
import us.ihmc.robotics.robotDescription.LinkDescription;
import us.ihmc.robotics.robotDescription.LinkGraphicsDescription;
import us.ihmc.robotics.robotDescription.PinJointDescription;
import us.ihmc.robotics.robotDescription.RobotDescription;
import us.ihmc.robotics.robotDescription.SliderJointDescription;
import us.ihmc.simulationconstructionset.simulatedSensors.SimulatedLIDARSensorLimitationParameters;
import us.ihmc.simulationconstructionset.simulatedSensors.SimulatedLIDARSensorNoiseParameters;
import us.ihmc.simulationconstructionset.simulatedSensors.SimulatedLIDARSensorUpdateParameters;

public class RobotDescriptionFromSDFLoader
{
   private static final boolean SHOW_CONTACT_POINTS = true;
   private static final boolean SHOW_COM_REFERENCE_FRAMES = false;
   private static final boolean SHOW_INERTIA_ELLIPSOIDS = false;
   private static final boolean SHOW_SENSOR_REFERENCE_FRAMES = false;
   private static final boolean DEBUG = false;

//   private final SDFParameters sdfParameters;
   private List<String> resourceDirectories;

   private RobotDescription robotDescription;
   private LinkedHashMap<String, JointDescription> jointDescriptions = new LinkedHashMap<>();
   private GeneralizedSDFRobotModel generalizedSDFRobotModel;

   public RobotDescriptionFromSDFLoader() //SDFParameters sdfParameters)
   {
//      this.sdfParameters = sdfParameters;
   }

   public RobotDescription getRobotDescription()
   {
      return robotDescription;
   }

   public RobotDescription loadRobotDescriptionFromSDF(String modelName, InputStream inputStream, List<String> resourceDirectories, SDFDescriptionMutator mutator, JointNameMap jointNameMap, boolean useCollisionMeshes,
         boolean enableTorqueVelocityLimits, boolean enableDamping)
   {
      GeneralizedSDFRobotModel generalizedSDFRobotModel = loadSDFFile(modelName, inputStream, resourceDirectories, mutator);
      return loadRobotDescriptionFromSDF(generalizedSDFRobotModel, jointNameMap, useCollisionMeshes, enableTorqueVelocityLimits, enableDamping);
   }

   public RobotDescription loadRobotDescriptionFromSDF(GeneralizedSDFRobotModel generalizedSDFRobotModel, JointNameMap jointNameMap, boolean useCollisionMeshes, boolean enableTorqueVelocityLimits, boolean enableDamping)
   {
      this.generalizedSDFRobotModel = generalizedSDFRobotModel;
      this.resourceDirectories = generalizedSDFRobotModel.getResourceDirectories();

      String name = generalizedSDFRobotModel.getName();
      robotDescription = new RobotDescription(name);

      ArrayList<SDFLinkHolder> rootLinks = generalizedSDFRobotModel.getRootLinks();

      if (rootLinks.size() > 1)
      {
         throw new RuntimeException("Can only accomodate one root link for now");
      }

      SDFLinkHolder rootLink = rootLinks.get(0);

      Vector3d offset = new Vector3d();
      Quat4d orientation = new Quat4d();
      generalizedSDFRobotModel.getTransformToRoot().get(orientation, offset);
      FloatingJointDescription rootJointDescription = new FloatingJointDescription(rootLink.getName());

      LinkDescription rootLinkDescription = createLinkDescription(rootLink, new RigidBodyTransform(), useCollisionMeshes);
      rootJointDescription.setLink(rootLinkDescription);
      addSensors(rootJointDescription, rootLink);

      robotDescription.addRootJoint(rootJointDescription);
      jointDescriptions.put(rootJointDescription.getName(), rootJointDescription);

      if (jointNameMap != null)
      {
         enableTorqueVelocityLimits = enableTorqueVelocityLimits && jointNameMap.isTorqueVelocityLimitsEnabled();
      }

      for (SDFJointHolder child : rootLink.getChildren())
      {
         // System.out.println("Joint name: " + child.getName());

         Set<String> lastSimulatedJoints;

         if (jointNameMap != null)
         {
            lastSimulatedJoints = jointNameMap.getLastSimulatedJoints();
         }
         else
         {
            lastSimulatedJoints = new HashSet<>();
         }
         addJointsRecursively(child, rootJointDescription, useCollisionMeshes, enableTorqueVelocityLimits, enableDamping, lastSimulatedJoints, false);
      }

      // Ground Contact Points:

      LinkedHashMap<String, Integer> counters = new LinkedHashMap<String, Integer>();
      if (jointNameMap != null)
      {
         for (ImmutablePair<String, Vector3d> jointContactPoint : jointNameMap.getJointNameGroundContactPointMap())
         {
            String jointName = jointContactPoint.getLeft();

            int count;
            if (counters.get(jointName) == null)
               count = 0;
            else
               count = counters.get(jointName);

            Vector3d gcOffset = jointContactPoint.getRight();

            GroundContactPointDescription groundContactPoint = new GroundContactPointDescription("gc_" + SDFConversionsHelper.sanitizeJointName(jointName) + "_" + count++, gcOffset);
            ExternalForcePointDescription externalForcePoint = new ExternalForcePointDescription("ef_" + SDFConversionsHelper.sanitizeJointName(jointName) + "_" + count++, gcOffset);

            JointDescription jointDescription = jointDescriptions.get(jointName);

            jointDescription.addGroundContactPoint(groundContactPoint);
            jointDescription.addExternalForcePoint(externalForcePoint);

            counters.put(jointName, count);

//            PrintTools.info("Joint Contact Point: " + jointContactPoint);

            if (SHOW_CONTACT_POINTS)
            {
               Graphics3DObject graphics = jointDescription.getLink().getLinkGraphics();
               if (graphics == null) graphics = new Graphics3DObject();

               graphics.identity();
               graphics.translate(jointContactPoint.getRight());
               double radius = 0.01;
               graphics.addSphere(radius, YoAppearance.Orange());

            }
         }
      }

      for (SDFJointHolder child : rootLink.getChildren())
      {
         addForceSensorsIncludingDescendants(child, jointNameMap);
      }

      return robotDescription;
   }

   private LinkDescription createLinkDescription(SDFLinkHolder link, RigidBodyTransform rotationTransform, boolean useCollisionMeshes)
   {
      LinkDescription scsLink = new LinkDescription(link.getName());

      //TODO: Get collision meshes working.
      if (useCollisionMeshes)
      {
         LinkGraphicsDescription linkGraphicsDescription = new SDFGraphics3DObject(link.getCollisions(), resourceDirectories, rotationTransform);

         scsLink.setLinkGraphics(linkGraphicsDescription);
      }
      else if (link.getVisuals() != null)
      {
         LinkGraphicsDescription linkGraphicsDescription = new SDFGraphics3DObject(link.getVisuals(), resourceDirectories, rotationTransform);
         scsLink.setLinkGraphics(linkGraphicsDescription);
      }

      double mass = link.getMass();
      Matrix3d inertia = InertiaTools.rotate(rotationTransform, link.getInertia());
      Vector3d CoMOffset = new Vector3d(link.getCoMOffset());

      if (link.getJoint() != null)
      {
         if (isJointInNeedOfReducedGains(link.getJoint()))
         {
            inertia.mul(100.0);
         }
      }

      rotationTransform.transform(CoMOffset);

      scsLink.setCenterOfMassOffset(CoMOffset);
      scsLink.setMass(mass);
      scsLink.setMomentOfInertia(inertia);

      if (SHOW_COM_REFERENCE_FRAMES)
      {
         scsLink.addCoordinateSystemToCOM(0.1);
      }
      if (SHOW_INERTIA_ELLIPSOIDS)
      {
         scsLink.addEllipsoidFromMassProperties(YoAppearance.Orange());
      }

      return scsLink;
   }

   protected void addJointsRecursively(SDFJointHolder joint, JointDescription scsParentJoint, boolean useCollisionMeshes, boolean enableTorqueVelocityLimits, boolean enableDamping, Set<String> lastSimulatedJoints,
         boolean doNotSimulateJoint)
   {
      Vector3d jointAxis = new Vector3d(joint.getAxisInModelFrame());
      Vector3d offset = new Vector3d(joint.getOffsetFromParentJoint());

      RigidBodyTransform visualTransform = new RigidBodyTransform();
      visualTransform.setRotation(joint.getLinkRotation());

      String sanitizedJointName = SDFConversionsHelper.sanitizeJointName(joint.getName());

      JointDescription scsJoint;


      switch (joint.getType())
      {
      case REVOLUTE:
         PinJointDescription pinJoint = new PinJointDescription(sanitizedJointName, offset, jointAxis);

         if (joint.hasLimits())
         {
            if (isJointInNeedOfReducedGains(joint))
            {
               pinJoint.setLimitStops(joint.getLowerLimit(), joint.getUpperLimit(), 10.0, 2.5);
            }
            else
            {
               if ((joint.getContactKd() == 0.0) && (joint.getContactKp() == 0.0))
               {
                  pinJoint.setLimitStops(joint.getLowerLimit(), joint.getUpperLimit(), 100.0, 20.0);
                  //                     pinJoint.setLimitStops(joint.getLowerLimit(), joint.getUpperLimit(), 1000.0, 200.0);
               }
               else
               {
                  pinJoint.setLimitStops(joint.getLowerLimit(), joint.getUpperLimit(), 0.0001 * joint.getContactKp(), 0.1 * joint.getContactKd());
               }

               if (!Double.isNaN(joint.getVelocityLimit()))
                  pinJoint.setVelocityLimits(joint.getVelocityLimit(), 0.0);
               //System.out.println("SDFRobot: joint.getVelocityLimit()=" + joint.getVelocityLimit());

            }
         }

         if (enableDamping)
         {
            pinJoint.setDamping(joint.getDamping());
            pinJoint.setStiction(joint.getFriction());
         }
         else
         {
            // TODO: Huh? What's this all about?
            //                  pinJoint.setDampingParameterOnly(joint.getDamping());
            //                  pinJoint.setStictionParameterOnly(joint.getFriction());
         }

         if (enableTorqueVelocityLimits)
         {
            if (!isJointInNeedOfReducedGains(joint))
            {
               if (!Double.isNaN(joint.getEffortLimit()))
               {
                  pinJoint.setEffortLimit(joint.getEffortLimit());
               }

               if (!Double.isNaN(joint.getVelocityLimit()))
               {
                  if (!isJointInNeedOfReducedGains(joint))
                  {
                     pinJoint.setVelocityLimits(joint.getVelocityLimit(), 500.0);
                  }
               }
            }
         }

         //               oneDoFJoints.put(joint.getName(), pinJoint);
         scsJoint = pinJoint;

         break;

      case PRISMATIC:
         SliderJointDescription sliderJoint = new SliderJointDescription(sanitizedJointName, offset, jointAxis);
         if (joint.hasLimits())
         {
            if ((joint.getContactKd() == 0.0) && (joint.getContactKp() == 0.0))
            {
               sliderJoint.setLimitStops(joint.getLowerLimit(), joint.getUpperLimit(), 100.0, 20.0);
            }
            else
            {
               sliderJoint.setLimitStops(joint.getLowerLimit(), joint.getUpperLimit(), 0.0001 * joint.getContactKp(), joint.getContactKd());
            }
         }

         if (enableDamping)
         {
            sliderJoint.setDamping(joint.getDamping());
            sliderJoint.setStiction(joint.getFriction());
         }
         else
         {
            // Huh? What's this all about?
            //                  sliderJoint.setDampingParameterOnly(joint.getDamping());
         }

         //               oneDoFJoints.put(joint.getName(), sliderJoint);

         scsJoint = sliderJoint;

         break;

      default:
         throw new RuntimeException("Joint type not implemented: " + joint.getType());
      }

      if (doNotSimulateJoint) scsJoint.setIsDynamic(false);

      scsJoint.setLink(createLinkDescription(joint.getChildLinkHolder(), visualTransform, useCollisionMeshes));
      scsParentJoint.addJoint(scsJoint);

      jointDescriptions.put(scsJoint.getName(), scsJoint);
      addSensors(scsJoint, joint.getChildLinkHolder());

      if (!doNotSimulateJoint && lastSimulatedJoints.contains(joint.getName()))
      {
         doNotSimulateJoint = true;
      }

      for (SDFJointHolder child : joint.getChildLinkHolder().getChildren())
      {
         addJointsRecursively(child, scsJoint, useCollisionMeshes, enableTorqueVelocityLimits, enableDamping, lastSimulatedJoints, doNotSimulateJoint);
      }

   }

   ///TODO:
   ///XXX: pull these names from the sdfJointNameMap
   private boolean isJointInNeedOfReducedGains(SDFJointHolder pinJoint)
   {
      String jointName = pinJoint.getName();
      return jointName.contains("f0") || jointName.contains("f1") || jointName.contains("f2") || jointName.contains("f3") || jointName.contains("palm") || jointName.contains("finger");
   }

//   private void loadSDFFile()
//   {
//      resourceDirectories = Arrays.asList(sdfParameters.getResourceDirectories());
//      loadSDFFile(sdfParameters.getSdfAsInputStream(), resourceDirectories, null);
//   }

   private static GeneralizedSDFRobotModel loadSDFFile(String modelName, InputStream inputStream, List<String> resourceDirectories, SDFDescriptionMutator mutator)
   {
      GeneralizedSDFRobotModel generalizedSDFRobotModel = null;
      try
      {
         JaxbSDFLoader loader = new JaxbSDFLoader(inputStream, resourceDirectories, mutator);
         generalizedSDFRobotModel = loader.getGeneralizedSDFRobotModel(modelName);
//         resourceDirectories = generalizedSDFRobotModel.getResourceDirectories();
      }
      catch (FileNotFoundException | JAXBException e)
      {
         throw new RuntimeException("Cannot load model", e);
      }

      return generalizedSDFRobotModel;
   }

   private void showCordinateSystem(JointDescription scsJoint, RigidBodyTransform offsetFromLink)
   {
      if (SHOW_SENSOR_REFERENCE_FRAMES)
      {
         Graphics3DObject linkGraphics = scsJoint.getLink().getLinkGraphics();
         linkGraphics.identity();
         linkGraphics.transform(offsetFromLink);
         linkGraphics.addCoordinateSystem(1.0);
         linkGraphics.identity();
      }
   }

   private void addSensors(JointDescription scsJoint, SDFLinkHolder child)
   {
      if (child.getSensors() != null)
      {
         for (SDFSensor sensor : child.getSensors())
         {
            switch (sensor.getType())
            {
            case "camera":
            case "multicamera":
               addCameraMounts(sensor, scsJoint, child);
               break;
            case "imu":
               addIMUMounts(sensor, scsJoint, child);
               break;
            case "gpu_ray":
            case "ray":
               addLidarMounts(sensor, scsJoint, child);
               break;
            }
         }
      }
   }

   private void addCameraMounts(SDFSensor sensor, JointDescription scsJoint, SDFLinkHolder child)
   {
      // TODO: handle left and right sides of multicamera
      final List<Camera> cameras = sensor.getCamera();

      if (cameras != null)
      {
         for (Camera camera : cameras)
         {
            // The linkRotation transform is to make sure that the linkToSensor is in a zUpFrame.
            RigidBodyTransform linkRotation = new RigidBodyTransform(child.getTransformFromModelReferenceFrame());
            linkRotation.setTranslation(0.0, 0.0, 0.0);
            RigidBodyTransform linkToSensor = SDFConversionsHelper.poseToTransform(sensor.getPose());
            RigidBodyTransform sensorToCamera = SDFConversionsHelper.poseToTransform(camera.getPose());
            RigidBodyTransform linkToCamera = new RigidBodyTransform();
            linkToCamera.multiply(linkRotation, linkToSensor);
            linkToCamera.multiply(sensorToCamera);
            showCordinateSystem(scsJoint, linkToCamera);

            double fieldOfView = Double.parseDouble(camera.getHorizontalFov());
            double clipNear = Double.parseDouble(camera.getClip().getNear());
            double clipFar = Double.parseDouble(camera.getClip().getFar());
            String cameraName = sensor.getName() + "_" + camera.getName();
            CameraSensorDescription mount = new CameraSensorDescription(cameraName, linkToCamera, fieldOfView, clipNear, clipFar);

            int imageHeight = Integer.parseInt(camera.getImage().getHeight());
            int imageWidth = Integer.parseInt(camera.getImage().getWidth());

            mount.setImageHeight(imageHeight);
            mount.setImageWidth(imageWidth);

            scsJoint.addCameraSensor(mount);

//            SDFCamera sdfCamera = new SDFCamera(Integer.parseInt(camera.getImage().getWidth()), Integer.parseInt(camera.getImage().getHeight()));
            //            this.cameras.put(cameraName, sdfCamera);
         }
      }
      else
      {
         System.err.println("JAXB loader: No camera section defined for camera sensor " + sensor.getName() + ", ignoring sensor.");
      }
   }

   private void addIMUMounts(SDFSensor sdfSensor, JointDescription jointDescription, SDFLinkHolder child)
   {
      // TODO: handle left and right sides of multicamera
      final IMU imu = sdfSensor.getImu();

      if (imu != null)
      {
         // The linkRotation transform is to make sure that the linkToSensor is in a zUpFrame.
         RigidBodyTransform linkRotation = new RigidBodyTransform(child.getTransformFromModelReferenceFrame());
         linkRotation.setTranslation(0.0, 0.0, 0.0);
         RigidBodyTransform linkToSensorInZUp = new RigidBodyTransform();
         linkToSensorInZUp.multiply(linkRotation, SDFConversionsHelper.poseToTransform(sdfSensor.getPose()));

         showCordinateSystem(jointDescription, linkToSensorInZUp);
         IMUSensorDescription imuMount = new IMUSensorDescription(child.getName() + "_" + sdfSensor.getName(), linkToSensorInZUp);

         IMUNoise noise = imu.getNoise();
         if (noise != null)
         {
            if ("gaussian".equals(noise.getType()))
            {
               NoiseParameters accelerationNoise = noise.getAccel();
               NoiseParameters angularVelocityNoise = noise.getRate();

               imuMount.setAccelerationNoiseParameters(Double.parseDouble(accelerationNoise.getMean()), Double.parseDouble(accelerationNoise.getStddev()));
               imuMount.setAccelerationBiasParameters(Double.parseDouble(accelerationNoise.getBias_mean()), Double.parseDouble(accelerationNoise.getBias_stddev()));

               imuMount.setAngularVelocityNoiseParameters(Double.parseDouble(angularVelocityNoise.getMean()), Double.parseDouble(angularVelocityNoise.getStddev()));
               imuMount.setAngularVelocityBiasParameters(Double.parseDouble(angularVelocityNoise.getBias_mean()), Double.parseDouble(angularVelocityNoise.getBias_stddev()));
            }
            else
            {
               throw new RuntimeException("Unknown IMU noise model: " + noise.getType());
            }
         }

         jointDescription.addIMUSensor(imuMount);

      }
      else
      {
         System.err.println("JAXB loader: No imu section defined for imu sensor " + sdfSensor.getName() + ", ignoring sensor.");
      }
   }

   private void addLidarMounts(SDFSensor sensor, JointDescription scsJoint, SDFLinkHolder child)
   {
      Ray sdfRay = sensor.getRay();
      if (sdfRay == null)
      {
         System.err.println("SDFRobot: lidar not present in ray type sensor " + sensor.getName() + ". Ignoring this sensor.");
      }
      else
      {
         Range sdfRange = sdfRay.getRange();
         Scan sdfScan = sdfRay.getScan();
         double sdfMaxRange = Double.parseDouble(sdfRange.getMax());
         double sdfMinRange = Double.parseDouble(sdfRange.getMin());
         HorizontalScan sdfHorizontalScan = sdfScan.getHorizontal();
         VerticalScan sdfVerticalScan = sdfScan.getVertical();
         double sdfMaxSweepAngle = Double.parseDouble(sdfHorizontalScan.getMaxAngle());
         double sdfMinSweepAngle = Double.parseDouble(sdfHorizontalScan.getMinAngle());
         double sdfMaxHeightAngle = sdfVerticalScan == null ? 0.0 : Double.parseDouble(sdfVerticalScan.getMaxAngle());
         double sdfMinHeightAngle = sdfVerticalScan == null ? 0.0 : Double.parseDouble(sdfVerticalScan.getMinAngle());

         // double sdfAngularResolution = Double.parseDouble(sdfHorizontalScan.getSillyAndProbablyNotUsefulResolution());
         int sdfSamples = (Integer.parseInt(sdfHorizontalScan.getSamples()) / 3) * 3;
         int sdfScanHeight = sdfVerticalScan == null ? 1 : Integer.parseInt(sdfVerticalScan.getSamples());
         double sdfRangeResolution = Double.parseDouble(sdfRay.getRange().getResolution());

         boolean sdfAlwaysOn = true;

         double sdfGaussianStdDev = 0.0;
         double sdfGaussianMean = 0.0;
         int sdfUpdateRate = (int) (1000.0 / Double.parseDouble(sensor.getUpdateRate()));

         Noise sdfNoise = sdfRay.getNoise();
         if (sdfNoise != null)
         {
            if ("gaussian".equals(sdfNoise.getType()))
            {
               sdfGaussianStdDev = Double.parseDouble(sdfNoise.getStddev());
               sdfGaussianMean = Double.parseDouble(sdfNoise.getMean());
            }
            else
            {
               System.err.println("Unknown noise model: " + sdfNoise.getType());
            }
         }

         //         System.err.println("[SDFRobot]: FIXME: Setting LIDAR angle to 0.5 pi due to current GPULidar limitations");
         //         sdfMinAngle = -Math.PI/4;
         //         sdfMaxAngle = Math.PI/4;

         LidarScanParameters polarDefinition = new LidarScanParameters(sdfSamples, sdfScanHeight, (float) sdfMinSweepAngle, (float) sdfMaxSweepAngle, (float) sdfMinHeightAngle, (float) sdfMaxHeightAngle, 0.0f,
               (float) sdfMinRange, (float) sdfMaxRange, 0.0f, 0l);

         // The linkRotation transform is to make sure that the linkToSensor is in a zUpFrame.
         RigidBodyTransform linkRotation = new RigidBodyTransform(child.getTransformFromModelReferenceFrame());
         linkRotation.setTranslation(0.0, 0.0, 0.0);
         RigidBodyTransform linkToSensorInZUp = new RigidBodyTransform();
         linkToSensorInZUp.multiply(linkRotation, SDFConversionsHelper.poseToTransform(sensor.getPose()));
         showCordinateSystem(scsJoint, linkToSensorInZUp);

         SimulatedLIDARSensorNoiseParameters noiseParameters = new SimulatedLIDARSensorNoiseParameters();
         noiseParameters.setGaussianNoiseStandardDeviation(sdfGaussianStdDev);
         noiseParameters.setGaussianNoiseMean(sdfGaussianMean);

         SimulatedLIDARSensorLimitationParameters limitationParameters = new SimulatedLIDARSensorLimitationParameters();
         limitationParameters.setMaxRange(sdfMaxRange);
         limitationParameters.setMinRange(sdfMinRange);
         limitationParameters.setQuantization(sdfRangeResolution);

         SimulatedLIDARSensorUpdateParameters updateParameters = new SimulatedLIDARSensorUpdateParameters();
         updateParameters.setAlwaysOn(sdfAlwaysOn);
         updateParameters.setUpdatePeriodInMillis(sdfUpdateRate);

         LidarSensorDescription lidarMount = new LidarSensorDescription(sensor.getName(), linkToSensorInZUp, polarDefinition);
         //         scsJoint.addLidarSensor(lidarMount);
         scsJoint.addLidarSensor(lidarMount);
      }
   }

   private void addForceSensorsIncludingDescendants(SDFJointHolder joint, JointNameMap jointNameMap)
   {
      addForceSensor(joint, jointNameMap);

      for (SDFJointHolder child : joint.getChildLinkHolder().getChildren())
      {
         addForceSensorsIncludingDescendants(child, jointNameMap);
      }
   }

   private void addForceSensor(SDFJointHolder joint, JointNameMap jointNameMap)
   {
      if (joint.getForceSensors().size() > 0)
      {
         String[] jointNamesBeforeFeet = jointNameMap.getJointNamesBeforeFeet();

         String jointName = joint.getName();
         String sanitizedJointName = SDFConversionsHelper.sanitizeJointName(jointName);
         JointDescription scsJoint = jointDescriptions.get(sanitizedJointName);

         boolean jointIsParentOfFoot = false;
         for(int i = 0; i < jointNamesBeforeFeet.length; i++)
         {
            if(jointName.equals(jointNamesBeforeFeet[i]))
            {
               jointIsParentOfFoot = true;
            }
         }

         for (SDFForceSensor forceSensor : joint.getForceSensors())
         {
            ForceSensorDescription forceSensorDescription = new ForceSensorDescription(forceSensor.getName(), forceSensor.getTransform());
            forceSensorDescription.setUseGroundContactPoints(jointIsParentOfFoot);
            scsJoint.addForceSensor(forceSensorDescription);
         }
      }
   }
}
