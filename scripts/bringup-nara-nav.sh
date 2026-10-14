#!/usr/bin/env bash
# Ordered NARA bringup for the Bifrost goal demo.
# Critical: only ONE /clock publisher (an orphan ros_gz_global_bridge breaks TF).
# Ctrl+C stops the stack this script started.
set -eo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
LOGDIR="${ROOT}/.data/logs/nara"
STARTED=0

kill_match() {
  local pat="$1"
  local pids
  pids=$(pgrep -f "$pat" 2>/dev/null || true)
  if [ -n "$pids" ]; then
    # shellcheck disable=SC2086
    kill -9 $pids 2>/dev/null || true
  fi
}

cleanup() {
  if [ "$STARTED" != 1 ]; then
    return 0
  fi
  kill_match '/opt/ros/.*/bin/ros2 launch smartwheelchair'
  kill_match '/opt/ros/.*/lib/ros_gz_bridge/parameter_bridge'
  kill_match '/opt/ros/.*/lib/slam_toolbox/'
  kill_match '/opt/ros/.*/lib/nav2_'
  kill_match 'rosbridge_websocket'
  kill_match 'web_video_server'
  kill_match 'goal_pose_to_nav2.py'
  kill_match 'gz sim'
}

trap cleanup EXIT
trap 'cleanup; exit 130' INT
trap 'cleanup; exit 143' TERM

source /opt/ros/jazzy/setup.bash
source "$HOME/Desktop/noblenara/nara-sim/install/setup.bash"
export ROS_DOMAIN_ID=0

mkdir -p "$LOGDIR"
STARTED=1

echo "[0/6] kill orphan gz/ros (duplicate /clock breaks TF)"
cleanup
sleep 2

echo "[1/6] worldmuseum (Gazebo + single /clock)"
ros2 launch smartwheelchair worldmuseum.launch.py >"$LOGDIR/world.log" 2>&1 &
WORLD_PID=$!
clocks=0
clock_ok=0
for _ in $(seq 1 90); do
  if ! kill -0 "$WORLD_PID" 2>/dev/null; then
    echo "ERROR: worldmuseum exited. See $LOGDIR/world.log" >&2
    exit 1
  fi
  clocks=$(ros2 topic info /clock 2>/dev/null | awk '/Publisher count:/{print $3}' || echo 0)
  if [ "${clocks:-0}" = "1" ]; then
    echo "  /clock publishers=1"
    clock_ok=1
    break
  fi
  sleep 1
done
if [ "$clock_ok" != 1 ]; then
  echo "ERROR: /clock publishers=${clocks:-0} (need exactly 1). See $LOGDIR/world.log" >&2
  exit 1
fi
sleep 4

echo "[2/6] noblenara (spawn 0,0)"
ros2 launch smartwheelchair noblenara.launch.py >"$LOGDIR/robot.log" 2>&1 &
ROBOT_PID=$!
odom_ok=0
for _ in $(seq 1 40); do
  if ! kill -0 "$ROBOT_PID" 2>/dev/null; then
    echo "ERROR: noblenara exited. See $LOGDIR/robot.log" >&2
    exit 1
  fi
  if ros2 topic info /noblenara/alfa/odom 2>/dev/null | grep -q 'Publisher count: [1-9]'; then
    echo "  odom publishing"
    odom_ok=1
    break
  fi
  sleep 1
done
if [ "$odom_ok" != 1 ]; then
  echo "ERROR: /noblenara/alfa/odom never published. See $LOGDIR/robot.log" >&2
  exit 1
fi
sleep 2

echo "[3/6] bridgelaunch"
ros2 launch smartwheelchair bridgelaunch.xml >"$LOGDIR/bridge.log" 2>&1 &
BRIDGE_PID=$!
sleep 3
if ! kill -0 "$BRIDGE_PID" 2>/dev/null; then
  echo "ERROR: bridgelaunch exited. See $LOGDIR/bridge.log" >&2
  exit 1
fi

echo "[4/6] slam (needs map TF before Nav2)"
ros2 launch smartwheelchair slam.launch.py >"$LOGDIR/slam.log" 2>&1 &
SLAM_PID=$!
slam_ok=0
for _ in $(seq 1 60); do
  if ! kill -0 "$SLAM_PID" 2>/dev/null; then
    echo "ERROR: slam exited. See $LOGDIR/slam.log" >&2
    exit 1
  fi
  if grep -q 'Managed nodes are active' "$LOGDIR/slam.log" 2>/dev/null; then
    echo "  slam active"
    slam_ok=1
    break
  fi
  sleep 1
done
if [ "$slam_ok" != 1 ]; then
  echo "ERROR: slam did not become active. See $LOGDIR/slam.log" >&2
  exit 1
fi

tf_ok=0
for _ in $(seq 1 60); do
  if timeout 2 ros2 run tf2_ros tf2_echo noblenara/alfa/map noblenara/alfa/robot_footprint 2>/dev/null \
    | grep -q 'Translation:'; then
    echo "  map TF ok"
    tf_ok=1
    break
  fi
  # Nudge so slam_toolbox publishes map→odom.
  ros2 topic pub --once /noblenara/alfa/cmd_vel geometry_msgs/msg/Twist \
    "{linear: {x: 0.05}, angular: {z: 0.05}}" >/dev/null 2>&1 || true
  sleep 1
done
ros2 topic pub --once /noblenara/alfa/cmd_vel geometry_msgs/msg/Twist "{}" >/dev/null 2>&1 || true
if [ "$tf_ok" != 1 ]; then
  echo "ERROR: no TF noblenara/alfa/map → noblenara/alfa/robot_footprint. See $LOGDIR/slam.log" >&2
  exit 1
fi
sleep 2

echo "[5/6] nav2"
ros2 launch smartwheelchair nav2_launch.py >"$LOGDIR/nav2.log" 2>&1 &
NAV_PID=$!
action=""
for _ in $(seq 1 60); do
  if ! kill -0 "$NAV_PID" 2>/dev/null; then
    echo "ERROR: nav2 exited. See $LOGDIR/nav2.log" >&2
    exit 1
  fi
  action=$(ros2 action list 2>/dev/null | awk '/navigate_to_pose/{print; exit}' | tr -d '[:space:]' || true)
  if [ -n "$action" ]; then
    echo "  $action"
    break
  fi
  sleep 1
done
if [ -z "$action" ]; then
  echo "ERROR: navigate_to_pose was not advertised. See $LOGDIR/nav2.log" >&2
  exit 1
fi
sleep 3

echo "[6/6] goal relay → $action"
python3 "$ROOT/scripts/goal_pose_to_nav2.py" --ros-args -p "action_name:=${action}" \
  >"$LOGDIR/relay.log" 2>&1 &
RELAY_PID=$!
sleep 2
if ! kill -0 "$RELAY_PID" 2>/dev/null; then
  echo "ERROR: goal relay exited. See $LOGDIR/relay.log" >&2
  exit 1
fi

clocks=$(ros2 topic info /clock 2>/dev/null | awk '/Publisher count:/{print $3}' || echo '?')
if [ "${clocks:-0}" != "1" ]; then
  echo "ERROR: /clock publishers=${clocks:-0} after bringup (need exactly 1)" >&2
  exit 1
fi

echo "PIDs world=$WORLD_PID robot=$ROBOT_PID bridge=$BRIDGE_PID slam=$SLAM_PID nav=$NAV_PID relay=$RELAY_PID"
echo "/clock publishers=${clocks}"
echo "Logs: $LOGDIR"
echo "Ready. Bifrost UI goal → Nav2. Ctrl+C stops this stack."
wait
