package basicminerplayer.copy;
import battlecode.common.*;

public strictfp class RobotPlayer {
    static RobotController rc;

    static Direction[] directions = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};
    static RobotType[] spawnedByMiner = {RobotType.REFINERY, RobotType.VAPORATOR, RobotType.DESIGN_SCHOOL,
            RobotType.FULFILLMENT_CENTER, RobotType.NET_GUN};

    static int turnCount;
    
    static MapLocation TEAM_HQ_LOCATION = new MapLocation(5, 26); //MapLocation object which will be updated with team HQ position on turn 0/1?
    static MapLocation ENEMY_HQ_LOCATION = new MapLocation(0, 0); //MapLocation object which will update with TEAM_HQ_LOCATION based on mirroring
    static MapLocation SPAWN_LOCATION = new MapLocation(0, 0); //MapLocation object which holds where a robot spawned
    
    static MapLocation ClosestRefinnery = new MapLocation(-1, -1);
    
    static MapLocation targetLocation = new MapLocation(0, 0); //MapLocation object which current robot will attempt to go to
    
    static int objective = 0; //int reperesenting objective of robot. 0 means no objective and other numbers represent other objectives.  Reference objectives.txt for number explinations
    static int movement = 0; //int for how the robot should move.  If the robot is a building, ignored.  Reference movement.txt for kinds of movements
    
    //Need different variables to describe what the objective of current robot.  Ex miners want to go to soup unless they have too much
    //Also need to differentiate between robot types

    /**
     * run() is the method that is called when a robot is instantiated in the Battlecode world.
     * If this method returns, the robot dies!
     **/
    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws GameActionException {

        // This is the RobotController object. You use it to perform actions from this robot,
        // and to get information on its current status.
        RobotPlayer.rc = rc;

        turnCount = 0;

        System.out.println("I'm a " + rc.getType() + " and I just got created!");
        while (true) {
            turnCount += 1;
            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
            try {
                // Here, we've separated the controls into a different method for each RobotType.
                // You can add the missing ones or rewrite this into your own control structure.
               // System.out.println("I'm a " + rc.getType() + "! Location " + rc.getLocation());
                switch (rc.getType()) {
                    case HQ:                 runHQ();                break;
                    case MINER:              runMiner();             break;
                    case REFINERY:           runRefinery();          break;
                    case VAPORATOR:          runVaporator();         break;
                    case DESIGN_SCHOOL:      runDesignSchool();      break;
                    case FULFILLMENT_CENTER: runFulfillmentCenter(); break;
                    case LANDSCAPER:         runLandscaper();        break;
                    case DELIVERY_DRONE:     runDeliveryDrone();     break;
                    case NET_GUN:            runNetGun();            break;
                }

                // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
                Clock.yield();

            } catch (Exception e) {
                System.out.println(rc.getType() + " Exception");
                e.printStackTrace();
            }
        }
    }
    
    //Code for the HQ
    static void runHQ() throws GameActionException {
    	if(turnCount == 1)
    	{
    		tryBuild(RobotType.MINER, Direction.EAST);
    	}
    	if(turnCount == 1) {
    		TEAM_HQ_LOCATION = rc.getLocation();
    		System.out.println("I am HQ located at " + TEAM_HQ_LOCATION);
    	}
    	
    	if(TEAM_HQ_LOCATION.equals(new MapLocation(0, 0))) {
    		rc.resign();
    	}
        
    }

    //Code for the miner
    //Currently want to move around to seek soup and mine that soup
    //Once an amount of soup has been gathered, move back to HQ and deposit
    static void runMiner() throws GameActionException {
    	
    	/*switch(objective) {
    		case 0: //Wander randomly and look for soup and refineries
    		
    			Direction d;
    			//Add check case if cooldown is greater than 1
    			
    			do {
    				d  = randomDirection();
    			} while(!(tryMove(d)));
    			
    			//Scans the area around miner for soup
    			int maxSoup = -1;
    			MapLocation maxSoupLocation = new MapLocation(-1, -1);
    			for(int x = 5; x >= -5; x--) {
    				for(int y = 5; y >= - 5; y--) {
    					if(rc.canSenseLocation(rc.getLocation().translate(x, y))) {
    						if(rc.senseSoup(rc.getLocation().translate(x, y)) > maxSoup) {
    							maxSoupLocation = rc.getLocation().translate(x, y);
    							maxSoup = rc.senseSoup(rc.getLocation().translate(x, y));
    						}
    					}
    				}
    			}
		    	
		    	
		    	break;
		    	
    		case 1: //Goes to sensed food
    			
    			if(rc.getLocation().isAdjacentTo(targetLocation)) {
    				objective = 2; //Sets objective to mining soup on adjacent tile
    			} else {
    				rc.move(rc.getLocation().directionTo(targetLocation));
    			}
    	}*/
    	tryBuild(RobotType.DESIGN_SCHOOL, Direction.EAST);
    } 
    
    //Code for the ref
    static void runRefinery() throws GameActionException {
        // System.out.println("Pollution: " + rc.sensePollution(rc.getLocation()));
    }
    
    //Code for the vap
    static void runVaporator() throws GameActionException {

    }

    //Code for the des school
    static void runDesignSchool() throws GameActionException {

    		tryBuild(RobotType.LANDSCAPER, Direction.EAST);
    	
    }
    
    //Code for the ful center
    static void runFulfillmentCenter() throws GameActionException {

    }
    
    //Code to run landscaper
    //Currently wan to build a wall around HQ
    static void runLandscaper() throws GameActionException {
    	Direction d = TEAM_HQ_LOCATION.directionTo(TEAM_HQ_LOCATION);
        if(turnCount < 200)
        {
        	for(int i = 0; i < 5; i ++)
        	{
        		tryMove(Direction.SOUTH);
        	}
        }
        if(turnCount < 250)
        {
        	for(int a = 0; a < 10; a++)
    		{
        		rc.digDirt(Direction.SOUTH);
        		System.out.println("Yuh");
    		}
        }
       
        	tryMove(d);
   
        	/*for(int b = 0; b < 10; b++)
    		{
    		tryMove(Direction.NORTH);
    		}*/
    	
        	//TEAM_HQ_LOCATION;
    	
    		
        
    }
    
    //Code to run delivery drones
    static void runDeliveryDrone() throws GameActionException {
       
    }

    //code to run net gun
    static void runNetGun() throws GameActionException {

    }

    /**
     * Returns a random Direction.
     *
     * @return a random Direction
     */
    static Direction randomDirection() {
        return directions[(int) (Math.random() * directions.length)];
    }

    /**
     * Returns a random RobotType spawned by miners.
     *
     * @return a random RobotType
     */
    static RobotType randomSpawnedByMiner() {
        return spawnedByMiner[(int) (Math.random() * spawnedByMiner.length)];
    }
    
    //Code to check if a robot can move
    //@return true if can move, else false
    static boolean tryMove() throws GameActionException {
        for (Direction dir : directions)
            if (tryMove(dir))
                return true;
        return false;
        // MapLocation loc = rc.getLocation();
        // if (loc.x < 10 && loc.x < loc.y)
        //     return tryMove(Direction.EAST);
        // else if (loc.x < 10)
        //     return tryMove(Direction.SOUTH);
        // else if (loc.x > loc.y)
        //     return tryMove(Direction.WEST);
        // else
        //     return tryMove(Direction.NORTH);
    }

    /**
     * Attempts to move in a given direction.
     *
     * @param dir The intended direction of movement
     * @return true if a move was performed
     * @throws GameActionException
     */
    static boolean tryMove(Direction dir) throws GameActionException {
        // System.out.println("I am trying to move " + dir + "; " + rc.isReady() + " " + rc.getCooldownTurns() + " " + rc.canMove(dir));
        if (rc.isReady() && rc.canMove(dir)) {
            rc.move(dir);
            return true;
        } else return false;
    }

    /**
     * Attempts to build a given robot in a given direction.
     *
     * @param type The type of the robot to build
     * @param dir The intended direction of movement
     * @return true if a move was performed
     * @throws GameActionException
     */
    static boolean tryBuild(RobotType type, Direction dir) throws GameActionException {
        if (rc.isReady() && rc.canBuildRobot(type, dir)) {
            rc.buildRobot(type, dir);
            return true;
        } else return false;
    }

    /**
     * Attempts to mine soup in a given direction.
     *
     * @param dir The intended direction of mining
     * @return true if a move was performed
     * @throws GameActionException
     */
    static boolean tryMine(Direction dir) throws GameActionException {
        if (rc.isReady() && rc.canMineSoup(dir)) {
            rc.mineSoup(dir);
            return true;
        } else return false;
    }

    /**
     * Attempts to refine soup in a given direction.
     *
     * @param dir The intended direction of refining
     * @return true if a move was performed
     * @throws GameActionException
     */
    static boolean tryRefine(Direction dir) throws GameActionException {
        if (rc.isReady() && rc.canDepositSoup(dir)) {
            rc.depositSoup(dir, rc.getSoupCarrying());
            return true;
        } else return false;
    }


    static void tryBlockchain() throws GameActionException {
        if (turnCount < 3) {
            int[] message = new int[10];
            for (int i = 0; i < 10; i++) {
                message[i] = 123;
            }
            if (rc.canSubmitTransaction(message, 10))
                rc.submitTransaction(message, 10);
        }
        // System.out.println(rc.getRoundMessages(turnCount-1));
    }
}
