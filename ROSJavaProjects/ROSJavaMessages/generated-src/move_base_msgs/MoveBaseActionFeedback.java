package move_base_msgs;

public interface MoveBaseActionFeedback extends org.ros.internal.message.Message {
  static final java.lang.String _TYPE = "move_base_msgs/MoveBaseActionFeedback";
  static final java.lang.String _DEFINITION = "# ====== DO NOT MODIFY! AUTOGENERATED FROM AN ACTION DEFINITION ======\n\nHeader header\nactionlib_msgs/GoalStatus status\nMoveBaseFeedback feedback\n";
  std_msgs.Header getHeader();
  void setHeader(std_msgs.Header value);
  actionlib_msgs.GoalStatus getStatus();
  void setStatus(actionlib_msgs.GoalStatus value);
  move_base_msgs.MoveBaseFeedback getFeedback();
  void setFeedback(move_base_msgs.MoveBaseFeedback value);
}