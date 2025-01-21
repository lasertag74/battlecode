package newStrat2;


import battlecode.common.*;

import java.util.*;

public class Util
{
    static boolean isTracing = false;
    static int smallestDistance = Integer.MAX_VALUE;
    static MapLocation closestLocation = null;
    static Direction tracingDir= null;
    //bug 2
    static MapLocation prevDest = null;
    static HashSet<MapLocation> line = null;
    static int obstacleStartDist = 0;

    static ArrayList<MapLocation> knownTowers = new ArrayList<>();
    private enum MessageType{
        SAVE_CHIPS
    }
    public static boolean isRuinNearMark(RobotController rc, MapLocation target) throws GameActionException{

        MapInfo[] nearbyTiles = rc.senseNearbyMapInfos(target,8);
        for (MapInfo tile : nearbyTiles) {
            // Make sure the ruin is not already complete (has no tower on it)
            if (tile.hasRuin() && rc.senseRobotAtLocation(tile.getMapLocation()) == null) {

               return true;
            }
        }
        return false;
    }
    public static void updateFriendlyTower(RobotController rc) throws GameActionException{
    RobotInfo[] allyRobots = rc.senseNearbyRobots(-1, rc.getTeam());
    for (RobotInfo allyRobot : allyRobots) {
        if(!allyRobot.getType().isTowerType()) continue;

        MapLocation allyLocation = allyRobot.getLocation();
        if(knownTowers.contains(allyLocation)) {
            if(Tower.isSaving){
                if(rc.canSendMessage(allyLocation)){
                    rc.sendMessage(allyLocation, MessageType.SAVE_CHIPS.ordinal());
                    Tower.isSaving = false;
                }
            }
            continue;
        }


        knownTowers.add(allyLocation);
    }

    }
    public static void checkNearbyRuins(RobotController rc) throws GameActionException {
        MapInfo[] nearbyTiles = rc.senseNearbyMapInfos();
        for (MapInfo nearbyTile : nearbyTiles) {
            if(!nearbyTile.hasRuin()) continue;
            if(rc.senseRobotAtLocation(nearbyTile.getMapLocation()) != null) continue;
            Direction dir = nearbyTile.getMapLocation().directionTo(rc.getLocation());
            MapLocation markTile = nearbyTile.getMapLocation().add(dir);
            if(!rc.senseMapInfo(markTile).getMark().isAlly()) continue;
            Tower.isSaving = true;
            return;
        }
    }
    public static void bug0(RobotController rc, MapLocation target) throws GameActionException{
        // get direction from current location to target
        Direction dir = rc.getLocation().directionTo(target);

        MapLocation nextLoc = rc.getLocation().add(dir);


        // try to move in the target direction
        if(rc.canMove(dir)){
            rc.move(dir);
        }

        // keep turning left until we can move
        for (int i=0; i<8; i++){
            dir = dir.rotateLeft();
            if(rc.canMove(dir)){
                rc.move(dir);
                break;
            }
        }
    }
    public static void bug1Step(RobotController rc, MapLocation target) throws GameActionException {
        if (!isTracing) {
            // Proceed as normal
            Direction dir = rc.getLocation().directionTo(target);
            if (rc.canMove(dir)) {
                rc.move(dir);
            } else {
                isTracing = true;
                tracingDir = dir;
            }
        } else {
            // Tracing mode
            if (rc.getLocation().equals(closestLocation)) {
                // Returned to closest location along the perimeter of the obstacle
                isTracing = false;
                smallestDistance = Integer.MAX_VALUE;
                closestLocation = null;
                tracingDir = null;
            } else {
                // Update closest location and smallest distance
                int distToTarget = rc.getLocation().distanceSquaredTo(target);
                if (distToTarget < smallestDistance) {
                    smallestDistance = distToTarget;
                    closestLocation = rc.getLocation();
                }

                // Go along the perimeter of the obstacle
                if (rc.canMove(tracingDir)) {
                    rc.move(tracingDir);
                    tracingDir = tracingDir.rotateRight().rotateRight();
                } else {
                    // Turn left until movement is possible
                    for (int i = 0; i < 8; i++) {
                        tracingDir = tracingDir.rotateLeft();
                        if (rc.canMove(tracingDir)) {
                            rc.move(tracingDir);
                            tracingDir = tracingDir.rotateRight().rotateRight();
                            break;
                        }
                    }
                }
            }
        }
    }




    public static MapInfo[] intersectMapInfoArrays(MapInfo[] array1, MapInfo[] array2, MapLocation exclude) {
        // Create a HashMap to store unique keys based on MapLocation and Mark
        Map<String, MapInfo> map1 = new HashMap<>();

        // Create a HashSet for the result of the intersection
        Set<MapInfo> intersection = new HashSet<>();

        // Iterate over the first array and store MapInfo in the map with a custom key (MapLocation + Mark)
        for (MapInfo mapInfo : array1) {
            String key = mapInfo.getMapLocation().x + "," + mapInfo.getMapLocation().y + "," + mapInfo.getMark();
            map1.put(key, mapInfo);
        }

        // Iterate over the second array and check if the element exists in the map from array1
        for (MapInfo mapInfo : array2) {
            String key = mapInfo.getMapLocation().x + "," + mapInfo.getMapLocation().y + "," + mapInfo.getMark();
            MapInfo mapInfoFromMap = map1.get(key);

            // Check if the mapInfo from array2 is present in array1 and satisfies the conditions
            if (mapInfoFromMap != null && mapInfo.getPaint() == PaintType.EMPTY &&
                    !mapInfo.getMapLocation().equals(exclude)) {
                intersection.add(mapInfo); // Add to the intersection
            }
        }

        // Convert the intersection set to an array and return
        return intersection.toArray(new MapInfo[0]);
    }





    public static void bug1(RobotController rc, MapLocation target) throws GameActionException{
        if (!isTracing){
            //proceed as normal
            Direction dir = rc.getLocation().directionTo(target);
            MapLocation nextLoc = rc.getLocation().add(dir);
            rc.setIndicatorDot(nextLoc, 255, 0, 0);
            Clock.yield();
            // try to move in the target direction
            if(rc.canMove(dir)){
                rc.move(dir);
            }
            else{
                isTracing = true;
                tracingDir = dir;
            }
        }
        else{
            // tracing mode

            // need a stopping condition - this will be when we see the closestLocation again
            if (rc.getLocation().equals(closestLocation)){
                // returned to closest location along perimeter of the obstacle
                isTracing = false;
                smallestDistance = Integer.MAX_VALUE;
                closestLocation = null;
                tracingDir= null;
            }
            else{
                // keep tracing

                // update closestLocation and smallestDistance
                int distToTarget = rc.getLocation().distanceSquaredTo(target);
                if(distToTarget < smallestDistance){
                    smallestDistance = distToTarget;
                    closestLocation = rc.getLocation();
                }

                // go along perimeter of obstacle
                if(rc.canMove(tracingDir)){
                    //move forward and try to turn right
                    rc.move(tracingDir);
                    tracingDir = tracingDir.rotateRight();
                    tracingDir = tracingDir.rotateRight();
                }
                else{
                    // turn left because we cannot proceed forward
                    // keep turning left until we can move again
                    for (int i=0; i<8; i++){
                        tracingDir = tracingDir.rotateLeft();
                        if(rc.canMove(tracingDir)){
                            rc.move(tracingDir);
                            tracingDir = tracingDir.rotateRight();
                            tracingDir = tracingDir.rotateRight();
                            break;
                        }
                    }
                }

                MapLocation nextLoc = rc.getLocation().add(tracingDir);
                rc.setIndicatorDot(nextLoc, 255, 0, 0);
                Clock.yield();
            }
        }
    }
    public static void bug2(RobotController rc, MapLocation target) throws GameActionException {
        if (!target.equals(prevDest)) {
            prevDest = target;
            line = createLine(rc.getLocation(), target);
        }

        if (!isTracing) {
            Direction dir = rc.getLocation().directionTo(target);

            if (rc.canMove(dir)) {
                rc.move(dir); // Move directly toward the target if possible
            } else {
                // Start tracing if an obstacle is encountered
                isTracing = true;
                obstacleStartDist = rc.getLocation().distanceSquaredTo(target);
                tracingDir = dir;
            }
        } else {
            // Check if we are back on the direct line to the target
            if (line.contains(rc.getLocation()) && rc.getLocation().distanceSquaredTo(target) < obstacleStartDist) {
                isTracing = false;
            }

            // Tracing logic: follow the obstacle
            for (int i = 0; i < 9; i++) {
                if (rc.canMove(tracingDir)) {
                    rc.move(tracingDir); // Move along the obstacle
                    tracingDir = tracingDir.rotateRight().rotateRight(); // Adjust tracing direction
                    break;
                } else {
                    tracingDir = tracingDir.rotateLeft(); // Adjust tracing direction
                }
            }
        }
    }

    // Bresenham's line algorithm to calculate the path between two points
    public static HashSet<MapLocation> createLine(MapLocation a, MapLocation b) {
        HashSet<MapLocation> locs = new HashSet<>();
        int x = a.x, y = a.y;
        int dx = b.x - a.x;
        int dy = b.y - a.y;
        int sx = (int) Math.signum(dx);
        int sy = (int) Math.signum(dy);
        dx = Math.abs(dx);
        dy = Math.abs(dy);
        int d = Math.max(dx, dy);
        int r = d / 2;
        if (dx > dy) {
            for (int i = 0; i < d; i++) {
                locs.add(new MapLocation(x, y));
                x += sx;
                r += dy;
                if (r >= dx) {
                    locs.add(new MapLocation(x, y));
                    y += sy;
                    r -= dx;
                }
            }
        } else {
            for (int i = 0; i < d; i++) {
                locs.add(new MapLocation(x, y));
                y += sy;
                r += dx;
                if (r >= dy) {
                    locs.add(new MapLocation(x, y));
                    x += sx;
                    r -= dy;
                }
            }
        }
        locs.add(new MapLocation(x, y));
        return locs;
    }


}
