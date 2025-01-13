package ARefactored1;


import battlecode.common.*;

import java.util.ArrayList;


public class Util
{

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
}
