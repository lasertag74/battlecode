package newStrat2;



import battlecode.common.*;

import java.util.Random;


public class RobotPlayer {
    static int turnCount = 0;
    static final Random rng = new Random();


    static final Direction[] directions = {
            Direction.NORTH,
            Direction.NORTHEAST,
            Direction.EAST,
            Direction.SOUTHEAST,
            Direction.SOUTH,
            Direction.SOUTHWEST,
            Direction.WEST,
            Direction.NORTHWEST,
    };


    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws GameActionException {
        while (true) {
            turnCount++;

            try {
                switch (rc.getType()) {
                    case SOLDIER:
                        Soldier.runAll(rc);
                        break;
                    case MOPPER:
                        Mopper.runMopper(rc);
                        break;
                    case SPLASHER:
                        Splasher.runSplasher(rc);
                        break;
                    default:
                        TempTower.runTower(rc);
                        break;
                }

            } catch (GameActionException e) {
                e.printStackTrace();

            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            } finally {
                Clock.yield();
            }

        }

    }


    public static void updateEnemyRobots(RobotController rc) throws GameActionException {

        // Sensing methods can be passed in a radius of -1 to automatically
        // use the largest possible value.
        RobotInfo[] enemyRobots = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
        if (enemyRobots.length != 0) {
            rc.setIndicatorString("There are nearby enemy robots! Scary!");
            // Save an array of locations with enemy robots in them for possible future use.
            MapLocation[] enemyLocations = new MapLocation[enemyRobots.length];
            for (int i = 0; i < enemyRobots.length; i++) {
                enemyLocations[i] = enemyRobots[i].getLocation();
            }
            RobotInfo[] allyRobots = rc.senseNearbyRobots(-1, rc.getTeam());
            // Occasionally try to tell nearby allies how many enemy robots we see.

        }
    }







}