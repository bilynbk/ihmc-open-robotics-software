package control_msgs;

public interface JointTrajectoryActionFeedback extends org.ros.internal.message.Message {
  static final java.lang.String _TYPE = "control_msgs/JointTrajectoryActionFeedback";
  static final java.lang.String _DEFINITION = "# ====== DO NOT MODIFY! AUTOGENERATED FROM AN ACTION DEFINITION ======\n\nHeader header\nactionlib_msgs/GoalStatus status\nJointTrajectoryFeedback feedback\n";
  std_msgs.Header getHeader();
  void setHeader(std_msgs.Header value);
  actionlib_msgs.GoalStatus getStatus();
  void setStatus(actionlib_msgs.GoalStatus value);
  control_msgs.JointTrajectoryFeedback getFeedback();
  void setFeedback(control_msgs.JointTrajectoryFeedback value);
}