package us.ihmc.sensorProcessing.simulatedSensors;

import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;

import us.ihmc.SdfLoader.SDFRobot;
import us.ihmc.sensorProcessing.sensors.RawJointSensorDataHolderMap;
import us.ihmc.simulationconstructionset.FloatingJoint;
import us.ihmc.simulationconstructionset.IMUMount;
import us.ihmc.simulationconstructionset.Joint;
import us.ihmc.simulationconstructionset.simulatedSensors.WrenchCalculatorInterface;
import us.ihmc.utilities.IMUDefinition;
import us.ihmc.utilities.humanoidRobot.model.ContactSensorHolder;
import us.ihmc.utilities.humanoidRobot.model.DesiredJointDataHolder;
import us.ihmc.utilities.humanoidRobot.model.ForceSensorDataHolder;
import us.ihmc.utilities.humanoidRobot.model.ForceSensorDefinition;
import us.ihmc.utilities.screwTheory.SixDoFJoint;
import us.ihmc.yoUtilities.dataStructure.registry.YoVariableRegistry;

public class DRCPerfectSensorReaderFactory implements SensorReaderFactory
{
   private final SDFRobot robot;

   private StateEstimatorSensorDefinitions stateEstimatorSensorDefinitions;
   private final DRCPerfectSensorReader perfectSensorReader;
   private final ForceSensorDataHolder forceSensorDataHolderToUpdate;

   public DRCPerfectSensorReaderFactory(SDFRobot robot, ForceSensorDataHolder forceSensorDataHolderToUpdate, double estimateDT)
   {
      this.robot = robot;
      perfectSensorReader = new DRCPerfectSensorReader(estimateDT);
      this.forceSensorDataHolderToUpdate = forceSensorDataHolderToUpdate;
   }

   @Override
   public void build(SixDoFJoint rootJoint, IMUDefinition[] imuDefinitions, ForceSensorDefinition[] forceSensorDefinitions,
         ContactSensorHolder contactSensorDataHolder, RawJointSensorDataHolderMap rawJointSensorDataHolderMap,
         DesiredJointDataHolder estimatorDesiredJointDataHolder, YoVariableRegistry parentRegistry)
   {
      final Joint scsRootJoint = robot.getRootJoints().get(0);
      SCSToInverseDynamicsJointMap scsToInverseDynamicsJointMap = SCSToInverseDynamicsJointMap.createByName((FloatingJoint) scsRootJoint, rootJoint);

      ArrayList<WrenchCalculatorInterface> groundContactPointBasedWrenchCaclculators = new ArrayList<WrenchCalculatorInterface>();
      robot.getForceSensors(groundContactPointBasedWrenchCaclculators);
      StateEstimatorSensorDefinitionsFromRobotFactory stateEstimatorSensorDefinitionsFromRobotFactory = new StateEstimatorSensorDefinitionsFromRobotFactory(
            scsToInverseDynamicsJointMap, new ArrayList<IMUMount>(), groundContactPointBasedWrenchCaclculators);
      stateEstimatorSensorDefinitions = stateEstimatorSensorDefinitionsFromRobotFactory.getStateEstimatorSensorDefinitions();

      SDFPerfectSimulatedSensorReader sdfPerfectSimulatedSensorReader = new SDFPerfectSimulatedSensorReader(robot, rootJoint, forceSensorDataHolderToUpdate, null);
      perfectSensorReader.setSensorReader(sdfPerfectSimulatedSensorReader);
      perfectSensorReader.setSensorOutputMapReadOnly(sdfPerfectSimulatedSensorReader);
      perfectSensorReader.setSensorRawOutputMapReadOnly(sdfPerfectSimulatedSensorReader);

      Map<WrenchCalculatorInterface, ForceSensorDefinition> forceSensors = stateEstimatorSensorDefinitionsFromRobotFactory.getForceSensorDefinitions();

      createAndAddForceSensors(sdfPerfectSimulatedSensorReader, forceSensors);
   }

   @Override
   public DRCPerfectSensorReader getSensorReader()
   {
      return perfectSensorReader;
   }

   @Override
   public StateEstimatorSensorDefinitions getStateEstimatorSensorDefinitions()
   {
      return stateEstimatorSensorDefinitions;
   }

   @Override
   public boolean useStateEstimator()
   {
      return false;
   }

   private void createAndAddForceSensors(SDFPerfectSimulatedSensorReader sdfPerfectSimulatedSensorReader,
         Map<WrenchCalculatorInterface, ForceSensorDefinition> forceSensors)
   {
      for (Entry<WrenchCalculatorInterface, ForceSensorDefinition> forceSensorDefinitionEntry : forceSensors.entrySet())
      {
         WrenchCalculatorInterface groundContactPointBasedWrenchCalculator = forceSensorDefinitionEntry.getKey();
         sdfPerfectSimulatedSensorReader.addForceTorqueSensorPort(forceSensorDefinitionEntry.getValue(), groundContactPointBasedWrenchCalculator);
      }
   }
}