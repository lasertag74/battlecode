package newStrat2;


import battlecode.common.*;

import java.util.ArrayList;

public class Soldier {
    private static MapInfo[][] map;
    private static MapLocation needsPaint = null;
    private static MapInfo curRuin = null;
    private static MapInfo HardTarget = null;
    private static MapInfo curRuinSapce = null;
    private static MapInfo curNewSapce = null;
    private static boolean atTarget = false;
    private static boolean atRefilTower = false;
    private static boolean madeMap = false;
    public static ArrayList<MapLocation> knownTowers= new ArrayList<>();

    public static void runAll(RobotController rc) throws GameActionException {

                runTemp(rc);


    }
    public static void runTemp(RobotController rc) throws GameActionException {
        // Sense nearby tiles and initialize variables
        MapInfo[] nearbyTiles = rc.senseNearbyMapInfos();
        int closestDistance = Integer.MAX_VALUE;
        boolean markedThisTurn = false;
        ArrayList<MapInfo> markedTiles = new ArrayList<>();

        // Process nearby tiles
        markedThisTurn = processNearbyTiles(rc, nearbyTiles, markedTiles, closestDistance);
        if (rc.getPaint() <= 75||atRefilTower) {
            if (needsPaint == null) {
                needsPaint = rc.getLocation(); // Save current location
            }
            moveToNearestTowerAndRefill(rc, needsPaint); // Pathfind to nearest tower and refill
            return;
        }
        // Handle actions if at the target ruin
        if (atTarget) {
            handleTargetRuin(rc, nearbyTiles);
        }
        // Pathfinding to the ruin if not yet at the target
        else if (curRuin != null) {
            moveToRuin(rc);
        }
        // No ruin found; move towards marked tiles or randomly
        else {
            handleNoRuin(rc, markedThisTurn, markedTiles);
        }

        // Handle marking and attacking at the current location
        handleCurrentLocation(rc);
    }

    // Helper method to process nearby tiles
    private static boolean processNearbyTiles(RobotController rc, MapInfo[] nearbyTiles, ArrayList<MapInfo> markedTiles, int closestDistance) throws GameActionException {
        boolean markedThisTurn = false;

        for (MapInfo tile : nearbyTiles) {
            MapLocation location = tile.getMapLocation();
            if(tile.hasRuin()&&rc.canSenseRobotAtLocation(location)&&rc.senseRobotAtLocation(location).getTeam().equals(rc.getTeam())) {
                knownTowers.add(location);
            }
            // Mark unpainted tiles
            if (!atTarget && rc.canMark(location) && tile.getPaint() == PaintType.EMPTY && !Util.isRuinNearMark(rc, location)) {
                rc.setIndicatorDot(location, 0, 255, 0);
                rc.mark(location, false);
                System.out.println("Marked " + location);
            }

            // Remove mismatched marks
            if (tile.getMark() != PaintType.EMPTY && tile.getPaint() == tile.getMark() && rc.canRemoveMark(location)) {
                rc.removeMark(location);
            }

            // Track marked tiles
            if (tile.getPaint() == PaintType.EMPTY &&
                    (tile.getMark() == PaintType.ALLY_PRIMARY || tile.getMark() == PaintType.ALLY_SECONDARY)) {
                markedThisTurn = true;
                markedTiles.add(tile);
            }

            // Find the closest ruin
            if (tile.hasRuin() && rc.senseRobotAtLocation(location) == null) {
                int distance = location.distanceSquaredTo(rc.getLocation());
                if (distance < closestDistance) {
                    closestDistance = distance;
                    curRuin = tile;
                }
            }
        }

        return markedThisTurn;
    }
    private static void moveToNearestTowerAndRefill(RobotController rc, MapLocation savedLocation) throws GameActionException {

        MapLocation closestTower = null;
        int minDistance = Integer.MAX_VALUE;

        // Find the nearest tower
        for (MapLocation tower : knownTowers) {
            int distance = tower.distanceSquaredTo(rc.getLocation());
            if (distance < minDistance) {
                minDistance = distance;
                closestTower = tower;
            }
        }

        if (closestTower != null&&!atRefilTower) {
            // Pathfind to the nearest tower
            Util.bug1Step(rc, closestTower);

            // Check if at the tower and refill paint
            if (rc.getLocation().equals(closestTower) && rc.canTransferPaint(closestTower, rc.getPaint()-250)) {
                rc.transferPaint(closestTower, rc.getPaint()-250); // Take as much paint as possible
                System.out.println("Refilled paint at tower: " + closestTower);

                // Return to the saved location
                Util.bug1Step(rc, savedLocation);
            }
        }
        else if(atRefilTower){
            Util.bug1Step(rc, savedLocation);
            if (rc.getLocation().equals(savedLocation)){
                atRefilTower =false;
                savedLocation = null;
            }
        }
        else {
            System.out.println("No known towers to refill paint.");
        }
    }




    // Helper method to handle the target ruin
    private static void handleTargetRuin(RobotController rc, MapInfo[] nearbyTiles) throws GameActionException {
        if (rc.canMarkTowerPattern(UnitType.LEVEL_ONE_MONEY_TOWER, curRuin.getMapLocation()) && !madeMap) {
            rc.markTowerPattern(UnitType.LEVEL_ONE_MONEY_TOWER, curRuin.getMapLocation());
            madeMap = true;
            System.out.println("Marked tower pattern at " + curRuin.getMapLocation());
        } else {
            // Find spaces to mark around the ruin
            MapInfo[] nearbyMarkings = rc.senseNearbyMapInfos(curRuin.getMapLocation(), 8);
            MapInfo[] validSpaces = Util.intersectMapInfoArrays(nearbyMarkings, nearbyTiles, curRuin.getMapLocation());

            if (validSpaces.length > 0) {
                moveToClosestSpace(rc, validSpaces);
            } else {
                completeTowerPattern(rc);
            }
        }
    }

    // Helper method to move to the closest space around the ruin
    private static void moveToClosestSpace(RobotController rc, MapInfo[] validSpaces) throws GameActionException {
        int closestDistance = Integer.MAX_VALUE;

        for (MapInfo tile : validSpaces) {
            if ((tile.getMark() == PaintType.ALLY_PRIMARY || tile.getMark() == PaintType.ALLY_SECONDARY) &&
                    (tile.getPaint() != PaintType.ALLY_PRIMARY && tile.getPaint() != PaintType.ALLY_SECONDARY)) {
                int distance = tile.getMapLocation().distanceSquaredTo(rc.getLocation());
                if (distance < closestDistance) {
                    closestDistance = distance;
                    curRuinSapce = tile;
                }
            }
        }

        if (curRuinSapce != null) {
            Util.bug1Step(rc, curRuinSapce.getMapLocation());
        }
    }

    // Helper method to complete the tower pattern
    private static void completeTowerPattern(RobotController rc) throws GameActionException {
        if (rc.canCompleteTowerPattern(UnitType.LEVEL_ONE_MONEY_TOWER, curRuin.getMapLocation())) {
            rc.completeTowerPattern(UnitType.LEVEL_ONE_MONEY_TOWER, curRuin.getMapLocation());
            resetTargetState();
        } else {
            Util.bug0(rc, curRuin.getMapLocation());
        }
    }

    // Helper method to move to the ruin
    private static void moveToRuin(RobotController rc) throws GameActionException {
        MapLocation targetLocation = curRuin.getMapLocation();
        if (rc.getLocation().distanceSquaredTo(targetLocation) <= 2) {
            atTarget = true;
            System.out.println("Reached target ruin at " + targetLocation);
        } else {
            Util.bug1Step(rc, targetLocation);
        }
    }

    // Helper method to handle no ruin found
    private static void handleNoRuin(RobotController rc, boolean markedThisTurn, ArrayList<MapInfo> markedTiles) throws GameActionException {
        if (markedThisTurn) {
            moveToClosestMarkedTile(rc, markedTiles);
        } else {
            Util.bug1Step(rc, new MapLocation(rc.getMapWidth() / 2, rc.getMapHeight() / 2));
        }
    }

    // Helper method to move to the closest marked tile
    private static void moveToClosestMarkedTile(RobotController rc, ArrayList<MapInfo> markedTiles) throws GameActionException {
        int closestDistance = Integer.MAX_VALUE;

        for (MapInfo tile : markedTiles) {
            int distance = tile.getMapLocation().distanceSquaredTo(rc.getLocation());
            if (distance < closestDistance) {
                closestDistance = distance;
                curNewSapce = tile;
            }
        }

        if (curNewSapce != null) {
            Util.bug1Step(rc, curNewSapce.getMapLocation());
        }
    }

    // Helper method to handle actions at the current location
    private static void handleCurrentLocation(RobotController rc) throws GameActionException {
        MapLocation currentLocation = rc.getLocation();
        MapInfo currentInfo = rc.senseMapInfo(currentLocation);
        PaintType currentMark = currentInfo.getMark();

        if (currentMark != PaintType.EMPTY) {
            if (currentMark == PaintType.ALLY_PRIMARY) {
                rc.attack(currentLocation, false);
            } else if (currentMark == PaintType.ALLY_SECONDARY) {
                rc.attack(currentLocation, true);
            }
        }
    }

    // Helper method to reset the target state
    private static void resetTargetState() {
        atTarget = false;
        curRuin = null;
        curRuinSapce = null;
        madeMap = false;
    }

    static int buildType = 0;
    static int type = 0;
    public static void runSoldier(RobotController rc) throws GameActionException {
        if (RobotPlayer.turnCount < 1500) {
            MapInfo[] nearbyTiles = rc.senseNearbyMapInfos();
            // Search for the closest nearby ruin to complete.
            MapInfo curRuin = null;
            int curDist = 9999999;
            for (MapInfo tile : nearbyTiles) {
                // Make sure the ruin is not already complete (has no tower on it)
                if (tile.hasRuin() && rc.senseRobotAtLocation(tile.getMapLocation()) == null) {
                    int checkDist = tile.getMapLocation().distanceSquaredTo(rc.getLocation());
                    if (checkDist < curDist) {
                        curDist = checkDist;
                        curRuin = tile;
                    }
                }
            }

            if (curRuin != null) {
                MapLocation targetLoc = curRuin.getMapLocation();
                Direction dir = rc.getLocation().directionTo(targetLoc);
                if (rc.canMove(dir))
                    rc.move(dir);
                // Mark the pattern we need to draw to build a tower here if we haven't already.
                MapLocation shouldBeMarked = curRuin.getMapLocation().subtract(dir);
                if (type % 2 == 0) {
                    if (rc.senseMapInfo(shouldBeMarked).getMark() == PaintType.EMPTY && rc.canMarkTowerPattern(UnitType.LEVEL_ONE_PAINT_TOWER, targetLoc) && (type == 0 || type == 2)) {
                        rc.markTowerPattern(UnitType.LEVEL_ONE_PAINT_TOWER, targetLoc);
                        System.out.println("Trying to build a tower at " + targetLoc);
                    }
                    for (MapInfo patternTile : rc.senseNearbyMapInfos(targetLoc, 8)) {
                        if (patternTile.getMark() != patternTile.getPaint() && patternTile.getMark() != PaintType.EMPTY) {
                            boolean useSecondaryColor = patternTile.getMark() == PaintType.ALLY_SECONDARY;
                            if (rc.canAttack(patternTile.getMapLocation()))
                                rc.attack(patternTile.getMapLocation(), useSecondaryColor);
                        }
                    }
                    if (rc.canCompleteTowerPattern(UnitType.LEVEL_ONE_PAINT_TOWER, targetLoc) && (type == 0 || type == 2)) {
                        rc.completeTowerPattern(UnitType.LEVEL_ONE_PAINT_TOWER, targetLoc);
                        rc.setTimelineMarker("Tower built", 0, 255, 0);
                        System.out.println("Built a tower at " + targetLoc + "!");
                    }
                    type++;
                }
                else {
                    if (rc.senseMapInfo(shouldBeMarked).getMark() == PaintType.EMPTY && rc.canMarkTowerPattern(UnitType.LEVEL_ONE_MONEY_TOWER, targetLoc) && (type == 1 || type > 3)) {
                        rc.markTowerPattern(UnitType.LEVEL_ONE_MONEY_TOWER, targetLoc);
                        System.out.println("Trying to build a tower at " + targetLoc);
                        type++;
                    }
                    // Fill in any spots in the pattern with the appropriate paint.
                    for (MapInfo patternTile : rc.senseNearbyMapInfos(targetLoc, 8)) {
                        if (patternTile.getMark() != patternTile.getPaint() && patternTile.getMark() != PaintType.EMPTY) {
                            boolean useSecondaryColor = patternTile.getMark() == PaintType.ALLY_SECONDARY;
                            if (rc.canAttack(patternTile.getMapLocation()))
                                rc.attack(patternTile.getMapLocation(), useSecondaryColor);
                        }
                    }
                    // Complete the ruin if we can
                    if (rc.canCompleteTowerPattern(UnitType.LEVEL_ONE_MONEY_TOWER, targetLoc) && (type == 1 || type > 3)) {
                        rc.completeTowerPattern(UnitType.LEVEL_ONE_MONEY_TOWER, targetLoc);
                        rc.setTimelineMarker("Tower built", 0, 255, 0);
                        System.out.println("Built a tower at " + targetLoc + "!");
                    }
                    type++;
                }

            }

            // Move and attack randomly if no objective.
            Direction dir = RobotPlayer.directions[RobotPlayer.rng.nextInt(RobotPlayer.directions.length)];
            MapLocation nextLoc = rc.getLocation().add(dir);
            if (rc.canMove(dir)) {
                rc.move(dir);
            }
            // Try to paint beneath us as we walk to avoid paint penalties.
            // Avoiding wasting paint by re-painting our own tiles.
            MapInfo currentTile = rc.senseMapInfo(rc.getLocation());
            if (!currentTile.getPaint().isAlly() && rc.canAttack(rc.getLocation())) {
                rc.attack(rc.getLocation());
            }
        }
        else {
            runASoldier(rc);
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




    public static void runRuinFiller(RobotController rc) throws GameActionException {

    }


}