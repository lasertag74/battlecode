package newStrat2;




import battlecode.common.*;

public class TempTower
{
    private enum MessageType{
        SAVE_CHIPS
    }
    static boolean isSaving = false;
    static int savingTurns = 0;
    static int timeSince=150;
    static boolean build = false;
    static int soldierCount = 0;


    public static void runTower(RobotController rc) throws GameActionException {
        if ((rc.canUpgradeTower(rc.getLocation())&&rc.getMoney()>2500)&&timeSince == 0) {
            rc.upgradeTower(rc.getLocation());
            timeSince =600;
        }
        if(timeSince>0){
            timeSince--;
        }
        if (savingTurns == 0) {
        isSaving = false;
        Direction dir = RobotPlayer.directions[RobotPlayer.rng.nextInt(RobotPlayer.directions.length)];
        MapLocation nextLoc = rc.getLocation().add(dir);
        int robotType = RobotPlayer.rng.nextInt(15);

        if (robotType < 13  && rc.canBuildRobot(UnitType.SOLDIER, nextLoc)) {
            rc.buildRobot(UnitType.SOLDIER, nextLoc);
            soldierCount++;

        } else if (robotType >= 13 && rc.canBuildRobot(UnitType.MOPPER, nextLoc)&&soldierCount%3 ==0) {
            rc.buildRobot(UnitType.MOPPER, nextLoc);

        }
        /*else if (robotType >= 2 && rc.canBuildRobot(UnitType.SPLASHER, nextLoc)) {
            rc.buildRobot(UnitType.SPLASHER, nextLoc);

        }*/
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
