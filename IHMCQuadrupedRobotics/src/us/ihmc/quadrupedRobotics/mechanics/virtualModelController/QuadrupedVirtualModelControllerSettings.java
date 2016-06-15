package us.ihmc.quadrupedRobotics.mechanics.virtualModelController;

import gnu.trove.map.hash.TObjectDoubleHashMap;
import us.ihmc.SdfLoader.partNames.QuadrupedJointName;

public class QuadrupedVirtualModelControllerSettings
{
   private double defaultJointDamping;
   private double defaultJointEffortBreakFrequency;
   private double defaultJointPositionLimitStiffness;
   private double defaultJointPositionLimitDamping;
   private final TObjectDoubleHashMap<QuadrupedJointName> jointDamping = new TObjectDoubleHashMap<>();
   private final TObjectDoubleHashMap<QuadrupedJointName> jointEffortBreakFrequency = new TObjectDoubleHashMap<>();
   private final TObjectDoubleHashMap<QuadrupedJointName> jointPositionLimitStiffness = new TObjectDoubleHashMap<>();
   private final TObjectDoubleHashMap<QuadrupedJointName> jointPositionLimitDamping = new TObjectDoubleHashMap<>();

   public QuadrupedVirtualModelControllerSettings()
   {
      setDefaults();
   }

   public QuadrupedVirtualModelControllerSettings(QuadrupedVirtualModelControllerSettings quadrupedVirtualModelControllerSettings)
   {
      setDefaults();
      jointDamping.putAll(quadrupedVirtualModelControllerSettings.jointDamping);
      jointEffortBreakFrequency.putAll(quadrupedVirtualModelControllerSettings.jointEffortBreakFrequency);
      jointPositionLimitStiffness.putAll(quadrupedVirtualModelControllerSettings.jointPositionLimitStiffness);
      jointPositionLimitDamping.putAll(quadrupedVirtualModelControllerSettings.jointPositionLimitDamping);
   }

   public void setDefaults()
   {
      defaultJointDamping = 0.0;
      defaultJointEffortBreakFrequency = 1e9;
      defaultJointPositionLimitStiffness = 100.0;
      defaultJointPositionLimitDamping = 10.0;
      jointDamping.clear();
      jointEffortBreakFrequency.clear();
      jointPositionLimitStiffness.clear();
      jointPositionLimitDamping.clear();
   }

   public void setJointDamping(double value)
   {
      defaultJointDamping = value;
      jointDamping.clear();
   }

   public void setJointDamping(QuadrupedJointName jointName, double value)
   {
      jointDamping.put(jointName, value);
   }

   public void setJointEffortBreakFrequency(double value)
   {
      defaultJointEffortBreakFrequency = value;
      jointEffortBreakFrequency.clear();
   }

   public void setJointEffortBreakFrequency(QuadrupedJointName jointName, double value)
   {
      jointEffortBreakFrequency.put(jointName, value);
   }

   public void setJointPositionLimitStiffness(double value)
   {
      defaultJointPositionLimitStiffness = value;
      jointPositionLimitStiffness.clear();
   }

   public void setJointPositionLimitStiffness(QuadrupedJointName jointName, double value)
   {
      jointPositionLimitStiffness.put(jointName, value);
   }

   public void setJointPositionLimitDamping(double value)
   {
      defaultJointPositionLimitDamping = value;
      jointPositionLimitDamping.clear();
   }
   public void setJointPositionLimitDamping(QuadrupedJointName jointName, double value)
   {
      jointPositionLimitDamping.put(jointName, value);
   }

   public double getJointDamping(QuadrupedJointName jointName)
   {
      return jointDamping.contains(jointName) ? jointDamping.get(jointName) : defaultJointDamping;
   }

   public double getJointEffortBreakFrequency(QuadrupedJointName jointName)
   {
      return jointEffortBreakFrequency.contains(jointName) ? jointEffortBreakFrequency.get(jointName) : defaultJointEffortBreakFrequency;
   }

   public double getJointPositionLimitStiffness(QuadrupedJointName jointName)
   {
      return jointPositionLimitStiffness.contains(jointName) ? jointPositionLimitStiffness.get(jointName) : defaultJointPositionLimitStiffness;
   }

   public double getJointPositionLimitDamping(QuadrupedJointName jointName)
   {
      return jointPositionLimitDamping.contains(jointName) ? jointPositionLimitDamping.get(jointName) : defaultJointPositionLimitDamping;
   }
}
