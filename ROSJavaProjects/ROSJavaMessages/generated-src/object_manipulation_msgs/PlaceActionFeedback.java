package object_manipulation_msgs;

public interface PlaceActionFeedback extends org.ros.internal.message.Message {
  static final java.lang.String _TYPE = "object_manipulation_msgs/PlaceActionFeedback";
  static final java.lang.String _DEFINITION = "# ====== DO NOT MODIFY! AUTOGENERATED FROM AN ACTION DEFINITION ======\n\nHeader header\nactionlib_msgs/GoalStatus status\nPlaceFeedback feedback\n";
  std_msgs.Header getHeader();
  void setHeader(std_msgs.Header value);
  actionlib_msgs.GoalStatus getStatus();
  void setStatus(actionlib_msgs.GoalStatus value);
  object_manipulation_msgs.PlaceFeedback getFeedback();
  void setFeedback(object_manipulation_msgs.PlaceFeedback value);
}