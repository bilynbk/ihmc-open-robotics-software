package head_monitor_msgs;

public interface PreplanHeadScanAction extends org.ros.internal.message.Message {
  static final java.lang.String _TYPE = "head_monitor_msgs/PreplanHeadScanAction";
  static final java.lang.String _DEFINITION = "# ====== DO NOT MODIFY! AUTOGENERATED FROM AN ACTION DEFINITION ======\n\nPreplanHeadScanActionGoal action_goal\nPreplanHeadScanActionResult action_result\nPreplanHeadScanActionFeedback action_feedback\n";
  head_monitor_msgs.PreplanHeadScanActionGoal getActionGoal();
  void setActionGoal(head_monitor_msgs.PreplanHeadScanActionGoal value);
  head_monitor_msgs.PreplanHeadScanActionResult getActionResult();
  void setActionResult(head_monitor_msgs.PreplanHeadScanActionResult value);
  head_monitor_msgs.PreplanHeadScanActionFeedback getActionFeedback();
  void setActionFeedback(head_monitor_msgs.PreplanHeadScanActionFeedback value);
}