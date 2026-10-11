#!/usr/bin/env bash
# Ordered NARA bringup for Bifrost goal demo.
# Critical: only ONE /clock publisher (orphan ros_gz_global_bridge breaks TF).
set -eo pipefail
source /opt/ros/jazzy/setup.bash
source "$HOME/Desktop/noblenara/nara-sim/install/setup.bash"
export ROS_DOMAIN_ID=0

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
LOGDIR="${ROOT}/.data/logs/nara"
mkdir -p "$LOGDIR"

kill_match() {
  local pat="$1"
  local pids
  pids=$(pgrep -f "$pat" 2>/dev/null || true)
  if [ -n "$pids" ]; then
    # shellcheck disable=SC2086
    kill -9 $pids 2>/dev/null || true
  fi
}

echo "[0/6] kill orphan gz/ros (duplicate /clock = TF hell)"
kill_match '/opt/ros/.*/bin/ros2 launch smartwheelchair'
kill_match '/opt/ros/.*/lib/ros_gz_bridge/parameter_bridge'
kill_match '/opt/ros/.*/lib/slam_toolbox/'
kill_match '/opt/ros/.*/lib/nav2_'
kill_match 'rosbridge_websocket'
kill_match 'web_video_server'
kill_match 'goal_pose_to_nav2.py'
kill_match 'gz sim'
sleep 2

echo "[1/6] worldmuseum (Gazebo + single /clock)"
ros2 launch smartwheelchair worldmuseum.launch.py >"$LOGDIR/world.log" 2>&1 &
WORLD_PID=$!
for i in $(seq 1 90); do
  clocks=$(ros2 topic info /clock 2>/dev/null | awk '/Publisher count:/{print $3}' || echo 0)
  if [ "${clocks:-0}" = "1" ]; then
    echo "  /clock publishers=1"
    break
  fi
  sleep 1
done
sleep 4

echo "[2/6] noblenara (spawn 0,0)"
ros2 launch smartwheelchair noblenara.launch.py >"$LOGDIR/robot.log" 2>&1 &
ROBOT_PID=$!
for i in $(seq 1 40); do
  if ros2 topic info /noblenara/alfa/odom 2>/dev/null | grep -q 'Publisher count: [1-9]'; then
    echo "  odom publishing"
    break
  fi
  sleep 1
done
sleep 2

echo "[3/6] bridgelaunch"
ros2 launch smartwheelchair bridgelaunch.xml >"$LOGDIR/bridge.log" 2>&1 &
BRIDGE_PID=$!
sleep 3

echo "[4/6] slam (needs map TF before Nav2)"
ros2 launch smartwheelchair slam.launch.py >"$LOGDIR/slam.log" 2>&1 &
SLAM_PID=$!
for i in $(seq 1 60); do
  if grep -q 'Managed nodes are active' "$LOGDIR/slam.log" 2>/dev/null; then
    echo "  slam active"
    break
  fi
  sleep 1
done
# Wait until map→base TF exists (Nav2 lifecycle fails without it).
for i in $(seq 1 60); do
  if timeout 2 ros2 run tf2_ros tf2_echo noblenara/alfa/map noblenara/alfa/robot_footprint 2>/dev/null \
    | grep -q 'Translation:'; then
    echo "  map TF ok"
    break
  fi
  # nudge so slam_toolbox publishes map→odom
  ros2 topic pub --once /noblenara/alfa/cmd_vel geometry_msgs/msg/Twist \
    "{linear: {x: 0.05}, angular: {z: 0.05}}" >/dev/null 2>&1 || true
  sleep 1
done
ros2 topic pub --once /noblenara/alfa/cmd_vel geometry_msgs/msg/Twist "{}" >/dev/null 2>&1 || true
sleep 2

echo "[5/6] nav2"
ros2 launch smartwheelchair nav2_launch.py >"$LOGDIR/nav2.log" 2>&1 &
NAV_PID=$!
for i in $(seq 1 60); do
  if ros2 action list 2>/dev/null | grep -q navigate_to_pose; then
    echo "  navigate_to_pose up"
    break
  fi
  sleep 1
done
sleep 3

echo "[6/6] goal relay"
python3 "$ROOT/scripts/goal_pose_to_nav2.py" >"$LOGDIR/relay.log" 2>&1 &
RELAY_PID=$!

clocks=$(ros2 topic info /clock 2>/dev/null | awk '/Publisher count:/{print $3}' || echo '?')
echo "PIDs world=$WORLD_PID robot=$ROBOT_PID bridge=$BRIDGE_PID slam=$SLAM_PID nav=$NAV_PID relay=$RELAY_PID"
echo "/clock publishers=${clocks} (must be 1)"
echo "Logs: $LOGDIR"
echo "Ready. Bifrost UI goal → Nav2."
wait
