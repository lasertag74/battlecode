package AAAAAAAAAAAAAAAAAAAAAAA;



import battlecode.common.*;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;


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
        rc.setIndicatorDot(nextLoc, 255, 0, 0);
        Clock.yield();

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

}
