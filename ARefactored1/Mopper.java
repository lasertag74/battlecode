package ARefactored1;


import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public class Mopper {

    public static void runMopper(RobotController rc) throws GameActionException {
        if(rc.getID() % 2 == 0){
            runCleaner(rc);
        }
        else{
           runMessenger(rc);
        }
    }
    public static void runCleaner(RobotController rc) throws GameActionException {
        // Move and attack randomly.
        Direction dir = RobotPlayer.directions[RobotPlayer.rng.nextInt(RobotPlayer.directions.length)];
        MapLocation nextLoc = rc.getLocation().add(dir);
        if (rc.canMove(dir)){
            rc.move(dir);
        }
        if (rc.canMopSwing(dir)){
            rc.mopSwing(dir);

        }
        else if (rc.canAttack(nextLoc)){
            rc.attack(nextLoc);
        }
        RobotPlayer.updateEnemyRobots(rc);

    }
    public static void runMessenger(RobotController rc) throws GameActionException {
        rc.setIndicatorDot(rc.getLocation(),255,0,0);
        if(Tower.isSaving&& !Util.knownTowers.isEmpty()){

        }
        runCleaner(rc);
        Util.updateFriendlyTower(rc);
        Util.checkNearbyRuins(rc);
    }
}
