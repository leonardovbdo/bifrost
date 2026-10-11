#!/usr/bin/env python3
"""Relay Bifrost / RViz PoseStamped goals to Nav2 NavigateToPose action."""

from __future__ import annotations

import math
import sys

import rclpy
from geometry_msgs.msg import PoseStamped
from nav2_msgs.action import NavigateToPose
from rclpy.action import ActionClient
from rclpy.node import Node
from rclpy.parameter import Parameter
from rclpy.qos import DurabilityPolicy, HistoryPolicy, QoSProfile, ReliabilityPolicy


class GoalPoseRelay(Node):
    def __init__(self) -> None:
        super().__init__(
            "bifrost_goal_pose_relay",
            parameter_overrides=[
                Parameter("use_sim_time", Parameter.Type.BOOL, True),
            ],
        )
        self.declare_parameter("goal_topic", "/noblenara/alfa/goal_pose")
        self.declare_parameter("action_name", "/noblenara/alfa/navigate_to_pose")
        self.declare_parameter("default_frame", "noblenara/alfa/map")

        goal_topic = self.get_parameter("goal_topic").get_parameter_value().string_value
        action_name = self.get_parameter("action_name").get_parameter_value().string_value
        self.default_frame = (
            self.get_parameter("default_frame").get_parameter_value().string_value
        )

        qos = QoSProfile(
            history=HistoryPolicy.KEEP_LAST,
            depth=1,
            reliability=ReliabilityPolicy.RELIABLE,
            durability=DurabilityPolicy.VOLATILE,
        )
        self._client = ActionClient(self, NavigateToPose, action_name)
        self._sub = self.create_subscription(PoseStamped, goal_topic, self._on_goal, qos)
        self.get_logger().info(
            f"Relaying {goal_topic} → action {action_name} "
            f"(frame fallback={self.default_frame}, use_sim_time + stamp=now)"
        )

    def _on_goal(self, msg: PoseStamped) -> None:
        if not self._client.wait_for_server(timeout_sec=5.0):
            self.get_logger().error("NavigateToPose action server not available")
            return

        goal = NavigateToPose.Goal()
        goal.pose = msg
        # Zero stamps from the browser break TF lookups under use_sim_time.
        goal.pose.header.stamp = self.get_clock().now().to_msg()
        if not goal.pose.header.frame_id or goal.pose.header.frame_id == "map":
            goal.pose.header.frame_id = self.default_frame

        yaw = quat_to_yaw(msg.pose.orientation)
        self.get_logger().info(
            f"Goal → x={msg.pose.position.x:.2f} y={msg.pose.position.y:.2f} "
            f"yaw={yaw:.2f} frame={goal.pose.header.frame_id} "
            f"t={goal.pose.header.stamp.sec}.{goal.pose.header.stamp.nanosec:09d}"
        )
        self._client.send_goal_async(goal)


def quat_to_yaw(q) -> float:
    siny_cosp = 2.0 * (q.w * q.z + q.x * q.y)
    cosy_cosp = 1.0 - 2.0 * (q.y * q.y + q.z * q.z)
    return math.atan2(siny_cosp, cosy_cosp)


def main(argv: list[str] | None = None) -> int:
    rclpy.init(args=argv)
    node = GoalPoseRelay()
    try:
        rclpy.spin(node)
    except KeyboardInterrupt:
        pass
    finally:
        node.destroy_node()
        rclpy.shutdown()
    return 0



if __name__ == "__main__":
    sys.exit(main())
