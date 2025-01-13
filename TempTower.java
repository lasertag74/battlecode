package AAAAAAAAAAAAAAAAAAAAAAA;



import battlecode.common.*;

public class TempTower
{
    private enum MessageType{
        SAVE_CHIPS
    }
    static boolean isSaving = false;
    static int savingTurns = 25;
    static boolean build = true;

    public static void runTower(RobotController rc) throws GameActionException {
        if (rc.canUpgradeTower(rc.getLocation()) && RobotPlayer.turnCount > 50) {
            rc.upgradeTower(rc.getLocation());
        }
        if (savingTurns == 0) {
        isSaving = false;
        Direction dir = RobotPlayer.directions[RobotPlayer.rng.nextInt(RobotPlayer.directions.length)];
        MapLocation nextLoc = rc.getLocation().add(dir);
        int robotType = 0;

        if (build && rc.getType() == UnitType.LEVEL_ONE_PAINT_TOWER &&robotType == 0 && rc.canBuildRobot(UnitType.SOLDIER, nextLoc)) {
            rc.buildRobot(UnitType.SOLDIER, nextLoc);
            build = false;

        } else if (robotType == 1 && rc.canBuildRobot(UnitType.MOPPER, nextLoc)) {
            //rc.buildRobot(UnitType.MOPPER, nextLoc);

        } else if (robotType >= 2 && rc.canBuildRobot(UnitType.SPLASHER, nextLoc)) {
            rc.buildRobot(UnitType.SPLASHER, nextLoc);

        }
    } else{
            rc.setIndicatorString("Saving for " + savingTurns + "more turns");
            savingTurns--;

        }


        // Read incoming messages
        Message[] messages = rc.readMessages(-1);
        for (Message m : messages) {
            System.out.println("Tower received message: '#" + m.getSenderID() + " " + m.getBytes());
            if(m.getBytes() == MessageType.SAVE_CHIPS.ordinal()) {
                savingTurns = 30;
                System.out.println("isSaving: " + isSaving);
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
