package us.ihmc.commonWalkingControlModules.controlModuleInterfaces;

import us.ihmc.commonWalkingControlModules.partNamesAndTorques.LegJointAccelerations;
import us.ihmc.commonWalkingControlModules.partNamesAndTorques.LegJointPositions;
import us.ihmc.commonWalkingControlModules.partNamesAndTorques.LegJointVelocities;
import us.ihmc.commonWalkingControlModules.partNamesAndTorques.LegTorques;
import us.ihmc.robotSide.RobotSide;

public interface SwingLegTorqueControlOnlyModule
{
   public abstract void compute(LegTorques legTorquesToPackForSwingLeg, LegJointPositions jointPositions, LegJointVelocities jointVelocities, LegJointAccelerations jointAccelerations);
   
   public abstract void computePreSwing(RobotSide swingSide);
   
   public abstract void setAnkleGainsSoft(RobotSide swingSide);

   public abstract void setAnkleGainsDefault(RobotSide swingSide);
}
