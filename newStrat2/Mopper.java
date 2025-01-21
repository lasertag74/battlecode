package newStrat2;


import battlecode.common.*;

public class Mopper {
    private static boolean atFirstTarget = false;
    private static boolean foundCurrEnem = false;
    private static MapLocation currTarget = null;
    private static MapLocation StandTarget = null;
    public static boolean yayIgotOne = false;

    public static void runMopper(RobotController rc) throws GameActionException {

        runCleaner(rc);

    }
    public static void runOldCleaner(RobotController rc) throws GameActionException {
        // Move and attack randomly.
        Direction dir = RobotPlayer.directions[RobotPlayer.rng.nextInt(RobotPlayer.directions.length)];
        MapLocation nextLoc = rc.getLocation().add(dir);
        if (rc.canMove(dir)) {
            rc.move(dir);
        }
        if (rc.canMopSwing(dir)) {
            rc.mopSwing(dir);

        } else if (rc.canAttack(nextLoc)) {
            rc.attack(nextLoc);
        }
        RobotPlayer.updateEnemyRobots(rc);

    }
    public static void runCleaner(RobotController rc) throws GameActionException {
        // Move and attack randomly.
        int curDist = 9999999;
        int safeDist = 999999;
        MapLocation Middle = new MapLocation(rc.getMapWidth() / 2, rc.getMapHeight() / 2);
        if (!atFirstTarget) {
            currTarget = Middle;
        }

        Util.bug1Step(rc, currTarget);
        if (rc.getLocation().distanceSquaredTo(Middle) <= 2 || atFirstTarget) {
            System.out.println("at middle");
            atFirstTarget = true; // Set the boolean to true
            MapInfo[] curView = rc.senseNearbyMapInfos(-1);
            for (MapInfo tile : curView) {

                // Make sure the ruin is not already complete (has no tower on it)

                if (foundCurrEnem) continue;

                if (tile.getPaint() == PaintType.ENEMY_PRIMARY || tile.getPaint() == PaintType.ENEMY_SECONDARY) {
                    System.out.println("found current enem");
                    int checkDist = tile.getMapLocation().distanceSquaredTo(rc.getLocation());
                    if (checkDist < curDist) {
                        curDist = checkDist;
                        currTarget = tile.getMapLocation();
                        rc.setIndicatorDot(currTarget, 255, 255, 0);
                    }
                }
                if ((currTarget != null && currTarget != Middle) && tile.getPaint().isAlly()) {
                    int checkDist = tile.getMapLocation().distanceSquaredTo(currTarget);
                    if (checkDist < safeDist) {
                        safeDist = checkDist;
                        StandTarget = tile.getMapLocation();
                        rc.setIndicatorDot(currTarget, 255, 255, 0);
                    }
                }


            }
            if (currTarget != null && StandTarget != null && currTarget != Middle && rc.getLocation().distanceSquaredTo(currTarget) <= 2) {
                foundCurrEnem = true;
                rc.setIndicatorDot(StandTarget, 255, 0, 0);

                Util.bug1Step(rc, StandTarget);
                if(rc.canAttack(currTarget)){
                    rc.attack(currTarget);
                }
                if (rc.canMopSwing(rc.getLocation().directionTo(currTarget))) {
                    rc.mopSwing(rc.getLocation().directionTo(currTarget));
                }

            }


        }
    }

    public static void runMessenger(RobotController rc) throws GameActionException {
        rc.setIndicatorDot(rc.getLocation(), 255, 0, 0);
        if (Tower.isSaving && !Util.knownTowers.isEmpty()) {
            MapLocation dst = Util.knownTowers.get(0);
            Util.bug1(rc, dst);
        }
        runOldCleaner(rc);
        Util.updateFriendlyTower(rc);
        Util.checkNearbyRuins(rc);
    }




}
