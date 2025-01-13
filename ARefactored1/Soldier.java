package ARefactored1;


import battlecode.common.*;

public class Soldier {
    public static void runSoldier(RobotController rc) throws GameActionException {
        if (RobotPlayer.turnCount > 25) {
            if (rc.getID() % 2 == 0) {
                runASoldier(rc);
            } else {
                ruiner(rc);
            }

        }
        else{
            ruiner(rc);
        }

    }
    public static void runASoldier(RobotController rc) throws GameActionException {


        // Move and attack randomly if no objective.
        Direction dir = RobotPlayer.directions[RobotPlayer.rng.nextInt(RobotPlayer.directions.length)];
        MapLocation nextLoc = rc.getLocation().add(dir);




        if (rc.canMove(dir)) {
            rc.move(dir);
        }


        if (rc.canAttack(nextLoc)) {
            MapInfo nextLocInfo = rc.senseMapInfo(nextLoc);
            if (!nextLocInfo.getPaint().isAlly()) {
                rc.attack(nextLoc);
            }
        }

    }
    public static void ruiner(RobotController rc) throws GameActionException {
        int distance = Integer.MAX_VALUE;
        boolean found = false;
        MapLocation[] nearbyRuins = rc.senseNearbyRuins(-1);

        MapLocation targetLoc = null;
        for (MapLocation ruin : nearbyRuins) {
            if (ruin.distanceSquaredTo(rc.getLocation()) < distance&&rc.senseRobotAtLocation(ruin)==null) {
                targetLoc = ruin;
                distance = ruin.distanceSquaredTo(rc.getLocation());
                found = true;
            }
        }
        if (found) {

            Direction dir = rc.getLocation().directionTo(targetLoc);
            if (rc.canMove(dir)) {
                rc.move(dir);
            }

            MapLocation shouldBeMarked = targetLoc.subtract(dir);
            if (rc.senseMapInfo(targetLoc).getMark() == PaintType.EMPTY && rc.canMarkTowerPattern(UnitType.LEVEL_ONE_PAINT_TOWER, targetLoc) && (ARefactored1.RobotPlayer.turnCount % 10 == 0)) {
                rc.markTowerPattern(UnitType.LEVEL_ONE_PAINT_TOWER, targetLoc);

            }
            // Fill in any spots in the pattern with the appropriate paint.
            for (MapInfo patternTile : rc.senseNearbyMapInfos(targetLoc, -1)) {
                if (patternTile.getMark() != patternTile.getPaint() && patternTile.getMark() != PaintType.EMPTY) {
                    boolean useSecondaryColor = patternTile.getMark() == PaintType.ALLY_SECONDARY;
                    if (rc.canAttack(patternTile.getMapLocation())) {
                        rc.attack(patternTile.getMapLocation(), useSecondaryColor);
                    }
                }
            }
            // Complete the ruin if we can.
            if (rc.canCompleteTowerPattern(UnitType.LEVEL_ONE_PAINT_TOWER, targetLoc)) {
                rc.completeTowerPattern(UnitType.LEVEL_ONE_PAINT_TOWER, targetLoc);
                rc.setTimelineMarker("Tower built", 0, 255, 0);
                System.out.println("Built a tower at " + targetLoc + "!");
            }
        }

        // Move and attack randomly if no objective.
        Direction dir = ARefactored1.RobotPlayer.directions[ARefactored1.RobotPlayer.rng.nextInt(RobotPlayer.directions.length)];
        MapLocation nextLoc = rc.getLocation().add(dir);
        if (rc.canMove(dir)) {
            rc.move(dir);
        }
        // Try to paint beneath us as we walk to avoid paint penalties.
        // Avoiding wasting paint by re-painting our own tiles.
        if (rc.canAttack(nextLoc)) {
            MapInfo nextLocInfo = rc.senseMapInfo(nextLoc);
            if (!nextLocInfo.getPaint().isAlly()) {
                rc.attack(nextLoc);
            }
        }

    }
}
