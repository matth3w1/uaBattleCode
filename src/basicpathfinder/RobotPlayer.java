package basicpathfinder;
import battlecode.common.*;

public strictfp class RobotPlayer {
    static RobotController rc;

    static Direction[] directions = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};
    static RobotType[] spawnedByMiner = {RobotType.REFINERY, RobotType.VAPORATOR, RobotType.DESIGN_SCHOOL,
            RobotType.FULFILLMENT_CENTER, RobotType.NET_GUN};

    static int turnCount;
    
    static MapLocation TEAM_HQ_LOCATION = new MapLocation(0, 0); //MapLocation object which will be updated with team HQ position on turn 0/1?
    static MapLocation ENEMY_HQ_LOCATION = new MapLocation(0, 0); //MapLocation object which will update with TEAM_HQ_LOCATION based on mirroring
    static MapLocation SPAWN_LOCATION = new MapLocation(0, 0); //MapLocation object which holds where a robot spawned
    
    static MapLocation ClosestRefinnery = new MapLocation(-1, -1);
    
    static MapLocation targetLocation = new MapLocation(0, 0); //MapLocation object which current robot will attempt to go to
    
    static int objective = 0; //int reperesenting objective of robot. 0 means no objective and other numbers represent other objectives.  Reference objectives.txt for number explinations
    static int movement = 0; //int for how the robot should move.  If the robot is a building, ignored.  Reference movement.txt for kinds of movements
    
    //Need different variables to describe what the objective of current robot.  Ex miners want to go to soup unless they have too much
    //Also need to differentiate between robot types

    static boolean[][] obstacles = new boolean[7][7];
    static boolean[][] checked = new boolean[7][7];
    static int[][] elevation = new int[7][7];
    static MapLocation target = new MapLocation(7,28);
    static int moveIndex = 0;
    static Direction[] moves = {Direction.CENTER, Direction.CENTER, Direction.CENTER, Direction.CENTER, Direction.CENTER, Direction.CENTER, Direction.CENTER, Direction.CENTER, Direction.CENTER, Direction.CENTER}; 
    static boolean FollowingPath = false;
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
                System.out.println("I'm a " + rc.getType() + "! Location " + rc.getLocation());
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
    	if(turnCount == 1) {
    		TEAM_HQ_LOCATION = rc.getLocation();
    		System.out.println("I am HQ located at " + TEAM_HQ_LOCATION);
    		
    	}
    	for (Direction dir : directions)
               tryBuild(RobotType.MINER, dir);
    	if(TEAM_HQ_LOCATION.equals(new MapLocation(0, 0))) {
    		rc.resign();
    	}
        
    }

    //Code for the miner
    //Currently want to move around to seek soup and mine that soup
    //Once an amount of soup has been gathered, move back to HQ and deposit
    static void runMiner() throws GameActionException {
    	if(FollowingPath) {
    		followPath();
    	}
    	else {
    		mapObstacles();
    		createPath();
    		FollowingPath = true;
    		System.out.println("succesfully created a path?");
    	}
		
    	/*
    	switch(objective) {
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
    	}
    	*/
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

    }
    
    //Code for the ful center
    static void runFulfillmentCenter() throws GameActionException {

    }
    
    //Code to run landscaper
    //Currently wan to build a wall around HQ
    static void runLandscaper() throws GameActionException {

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
        //System.out.println("I am trying to move " + dir + "; " + rc.isReady() + " " + rc.getCooldownTurns() + " " + rc.canMove(dir));
        if (rc.isReady() && rc.canMove(dir)) {
        	moveIndex++;
    		System.out.println("huh, that's cool");
    		System.out.println("Moved in Direction" + moves[moveIndex-1]);
        	System.out.println("inside the if statement in tryMove()");
            rc.move(dir);
            System.out.println("ran rc.move(dir);");
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
    
    static void followPath() throws GameActionException {
    	
    	//System.out.println("In followPath()");
    	//System.out.println(rc.isReady());
    	//System.out.println(rc.canMove(moves[moveIndex]));
    	
    	//tryMove(moves[moveIndex]); // Tries to move to next direction
    	if(!(tryMove(moves[moveIndex]))) { // If move fails, it tries to figure out why
    	//	System.out.println("Not good, but can still be saved");
    		System.out.println("Code Still Works?");
    		if(!rc.canMove(moves[moveIndex])) { // Checks if it is due to cooldown
    	//		System.out.println("NOW WE'RE FUCKED BOYS!");
    			mapObstacles();
    			createPath();
    		}
    	}
    	else if(moves[moveIndex] == Direction.CENTER) {
    		FollowingPath = false;
    	//	System.out.println("maybe good maybe bad");
    	}
    	
    	
    }
    
    // If there is an Obstacle at that position, it will return false
    // If there isn't, it will return true
    // It begins but sensing nearby robots to detect positions of robots in its path
    // It then goes through the loop and checks for flooding as well as finding elevation of the tile
    static void mapObstacles() throws GameActionException {
    	MapLocation currentLocation = rc.getLocation();
    	int CurX = currentLocation.x;
    	int CurY = currentLocation.y;
    	//System.out.println("in mapObstacles");
    	RobotInfo robots[];
    	if(rc.canSenseRadiusSquared(3)) {
    		robots = rc.senseNearbyRobots(3);
    		for(int i = 0; i < robots.length; i++) {
    			int xCoord = 3+robots[i].getLocation().x-CurX;
    			int yCoord = 3+CurY-robots[i].getLocation().y;
    			obstacles[xCoord][yCoord] = false;
    			checked[xCoord][yCoord] = true;
    		}
    	}
    	// Sensing Robot Works ^
    	for(int i = -3; i < 4; i++) {
    		for(int j = -3; j < 4; j++) {
    			if(!checked[i+3][j+3]) {
    				MapLocation xy = rc.getLocation().translate(i, j);
        			if(!(rc.senseFlooding(xy))) {
        				obstacles[i+3][j+3] = true;
        			}
        			elevation[i+3][j+3] = rc.senseElevation(xy);
        			//System.out.println("Coordinate [" + xy.x + "," + xy.y + "]'s Elevation is " + elevation[i+3][j+3]);
    			}
    		}
    	}
    	// Sensing Elevation Works ^
    }
    
    static void createPath() throws GameActionException {
    	//System.out.println("in createPath");
    	moveIndex = 0; // Resets move index for the array
    	int CurX = 3; // Current Y position
    	int CurY = 3; // Current X position
    	int CloseX = target.x; // Used to find closest X
    	int CloseY = target.y; // Used to find closest Y
    	int small = 1000; // Used for distanceSquare calculations
    	MapLocation CloseLoc = new MapLocation(20,20); // Location of the closest tile to final destination
    	MapLocation CurLoc = rc.getLocation(); // Current Location of Robot
    	for(int i = -3; i < 4; i++) {
    		for(int j = -3; j < 4; j++) {
    			if(obstacles[i+3][j+3]) {
    				MapLocation xy = CurLoc.translate(i, j);
    				int temp = xy.distanceSquaredTo(target);
    				if(temp < small) {
    					small = temp;
    					CloseLoc = xy;
    					CloseX = i;
    					CloseY = j;
    				}
    			}
    		}
    	}
    	//System.out.println("Closest Coordinate to Target is [" + CloseLoc.x + "," + CloseLoc.y + "]");
    	// Closest Target Works ^
    	Direction moveDir = CurLoc.directionTo(target);
    	for(int i = 0; i < 10; i++) {
    		if(CurX == CloseX && CurY == CloseY || moveDir == Direction.CENTER) {
    			i = 10;
    		}
    		else {
    			while(canMove(CurLoc, moveDir, CurX, CurY) == false) {
        			moveDir = moveDir.rotateRight();
        		}
        		if(canMove(CurLoc, moveDir, CurX, CurY)) {
        			MapLocation temp = CurLoc.add(moveDir);
        			CurX += temp.x-CurLoc.x;
        			CurY += CurLoc.y-temp.y;
        			CurLoc = temp;
        			//System.out.println("New Coordinate is [" + CurX + "," + CurY + "]");
        		}
        		moves[i] = moveDir;
        		//System.out.println(moves[i]);
        		moveDir = CurLoc.directionTo(target);
    		}    		
    	}	
    }
    
    static boolean canMove(MapLocation loc, Direction dir, int CurrentX, int CurrentY) {
    	MapLocation temp = loc.add(dir);
    	int newX = CurrentX+temp.x-loc.x;
    	int newY = CurrentY+loc.y-temp.y;
//    	System.out.println(dir);
//    	System.out.println(CurrentX);
//    	System.out.println(CurrentY);
//    	System.out.println(newX);
//    	System.out.println(newY);
//    	System.out.println(obstacles[newX][newY]);
//    	System.out.println(Math.abs(elevation[CurrentX][CurrentY]-elevation[newX][newY]) < 4);
    	if(obstacles[newX][newY] && Math.abs(elevation[CurrentX][CurrentY]-elevation[newX][newY]) < 4) {
			return true;
		}
    	return false;
    }
}
