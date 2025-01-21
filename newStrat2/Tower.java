package newStrat2;




import battlecode.common.*;

public class Tower
{
    private enum MessageType{
        SAVE_CHIPS
    }
    static boolean isSaving = false;
    static int savingTurns = 0;
    static int buildType = 0;
    public static void runTower(RobotController rc) throws GameActionException {
        if (rc.canUpgradeTower(rc.getLocation()) && RobotPlayer.turnCount > 50) {
            rc.upgradeTower(rc.getLocation());
        }
        if (savingTurns == 0|| RobotPlayer.turnCount > 1900) {
            if(RobotPlayer.turnCount < 1900) {
                isSaving = false;
                Direction dir = RobotPlayer.directions[RobotPlayer.rng.nextInt(RobotPlayer.directions.length)];
                MapLocation nextLoc = rc.getLocation().add(dir);
                int robotType = RobotPlayer.rng.nextInt(2);

                if (robotType == 0 && rc.canBuildRobot(UnitType.SOLDIER, nextLoc) && (rc.getMoney() > 1200|| RobotPlayer.turnCount >= 1500)) {
                    rc.buildRobot(UnitType.SOLDIER, nextLoc);

                } else if (robotType == 1 && rc.canBuildRobot(UnitType.MOPPER, nextLoc) && rc.getMoney() > 1200 && RobotPlayer.turnCount <= 1500) {
                    rc.buildRobot(UnitType.MOPPER, nextLoc);

                }/* else if (robotType >= 2 && rc.canBuildRobot(UnitType.SPLASHER, nextLoc) && rc.getMoney() > 700) {
                    rc.buildRobot(UnitType.SPLASHER, nextLoc);

                }*/
            }
            else {
                MapInfo[] mapInfos = rc.senseNearbyMapInfos(-1);
                MapLocation[] mapLoc = new MapLocation[mapInfos.length];
                for(int x =0; x < mapInfos.length;x++){
                    mapLoc[x] = mapInfos[x].getMapLocation();
                }
                for (MapLocation nextLoc: mapLoc) {
                    if (rc.canBuildRobot(UnitType.SOLDIER, nextLoc)) {
                        rc.buildRobot(UnitType.SOLDIER, nextLoc);
                    }
                }
            }


    } else{
            rc.setIndicatorString("Saving for " + savingTurns + "more turns");
            savingTurns--;

        }


        // Read incoming messages
        Message[] messages = rc.readMessages(-1);
        for (Message m : messages) {
            System.out.println("Tower received message: '#" + m.getSenderID() + " " + m.getBytes());
            if(m.getBytes() == MessageType.SAVE_CHIPS.ordinal()&& !isSaving) {
                savingTurns = 30;

                isSaving = true;
            }
        }
        RobotInfo[] nearbyRobots = rc.senseNearbyRobots();
        for (RobotInfo Curr : nearbyRobots) {
            if (rc.canAttack(Curr.getLocation())) {
                rc.attack(Curr.getLocation());
            }
        }
    }
}
