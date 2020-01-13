package basicminerplayer;
import battlecode.common.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public strictfp class RobotPlayer {
    static RobotController rc;

    static Direction[] directions = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};
    static RobotType[] spawnedByMiner = {RobotType.REFINERY, RobotType.VAPORATOR, RobotType.DESIGN_SCHOOL,
            RobotType.FULFILLMENT_CENTER, RobotType.NET_GUN};

    static int turnCount;
    
    /**
     * Private Instance Variables used by all robots (note - some may not be valid or used)
     * 
     * TEAM_HQ_LOCATION is MapLocation of where the team HQ is positioned
     * ENEMEY_HQ_LOCATION is MapLocation of where the enemy HQ is positioned
     * SPAWN_LOCATION is MapLocation of where this spesific unit has spawned
     * 
     * opponentTeam is enemy Team, updates on robot's first turn
     * team is team Team, updates on robot's first turn
     * 
     * MAP_HEIGHT is an int which represents the map height
     * MAP_WIDTH is an int which represents the map width
     * 
     * objective is a numerical representation of what the current robot wants to accomplish.  See objectives.txt to find objective description
     * 
     * targetLocation is MapLocation of where the current target
     * INVALID_LOCATION is a constant used to replace null values for a location to keep the code happy (basically means no location)
     * 
     * transIdent is a number added to transactions to identify that transaction as from this team (encryption)
     * lastRoundBlock is block of transactions which happened last round
     * 
     * obstacles is a matrix which shows where a robot can currently not move
     * checked is a matrix which ___
     * elevation is a matrix which holds information about elevation around the robot
     * target is a MapLocaiton which the robot tries to move toward
     * moveIndex is the current index for movement to occur
     * moves is a series of directions which the robot will try to move toward a target location
     * followingPath is boolean which 
     */
    static MapLocation TEAM_HQ_LOCATION = new MapLocation(-1, -1);  
    static MapLocation ENEMY_HQ_LOCATION = new MapLocation(-1, -1); 
    static MapLocation SPAWN_LOCATION = new MapLocation(-1, -1); //can this be updates as the robot is created
    
    static Team opponentTeam;
    static Team team;
    
    static int MAP_HEIGHT = 0;
    static int MAP_WIDTH = 0;
    
    //Maybe add a transaction array for the round one block for every robot to add to?
    //Round 1 block will need more useful information for this to be useful
    
    static int objective = 0; 
    
    static MapLocation targetLocation = new MapLocation(-1, -1);
    static final MapLocation INVALID_LOCATION = new MapLocation(-1, -1);
    
    static int transIdent = 420; //Blaze it
    static Transaction[] lastRoundBlock;
    
    static boolean[][] obstacles = new boolean[7][7];
    static boolean[][] checked = new boolean[7][7];
    static int[][] elevation = new int[7][7];
    static MapLocation target = new MapLocation(7,28);
    static int moveIndex = 0;
    static Direction[] moves = {Direction.CENTER, Direction.CENTER, Direction.CENTER, Direction.CENTER, Direction.CENTER, Direction.CENTER, Direction.CENTER, Direction.CENTER, Direction.CENTER, Direction.CENTER}; 
    static boolean followingPath = false;
    
    
    /**
     * Private Instance Variables only used by the Miner robot
     * 
     * MIN_SOUP_AMOUNT is the minimum amount of soup a tile must have so the miner goes toward that tile
     * MIN_SOUP_DEPOSIT_AMOUNT is the minimum amount of soup a miner must collect before refining 
     * 
     * closestRefinery is a MapLocation of the last closest refinery.  New refinery are searched for every (4) rounds
     * soupLocations is a key, value pair of loc, amount for all soup seen (or read) by a miner.  Continuous memory
     * hasSpawnedRefinery is false until miner has spawned in a refinery.  Latches true
     */
    static final int MIN_SOUP_AMOUNT = 200;
    static final int MIN_SOUP_DEPOSIT_AMOUNT = 100;
    
    static MapLocation closestRefinery = new MapLocation(-1, -1);
    static HashMap<MapLocation, Integer> soupLocations = new HashMap<MapLocation, Integer>();
    static boolean hasSpawnedRefinery = false;
    
    
    /**
     * Private instance Variables used by the HQ robot
     * 
     * numUnknownRobots is number of robots not identified within knownRobots
     * 
     * newMinerSoupThreshold is minimum amount of soup needed to spawn in a new miner.  Will update each round from HQ updating robots
     * numMinersSpawned is number of miners spawned by the HQ starts at 2 from first round.  Used to decide to spawn more miners or not 
     */
    static int numUnknownRobots = 0;
    
    static int newMinerSoupThreshold = 300;
    static int numMinersSpawned = 2;
    
    
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

        //System.out.println("I'm a " + rc.getType() + " and I just got created!");
        while (true) {
            turnCount += 1;
            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
            try {
                // Here, we've separated the controls into a different method for each RobotType.
                // You can add the missing ones or rewrite this into your own control structure.
                //System.out.println("I'm a " + rc.getType() + "! Location " + rc.getLocation());
            	
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
    	updateTransIdent();
    	getLastBlock();
    	
    	
    	if(turnCount == 1) { //First turn of the HQ
    		TEAM_HQ_LOCATION = rc.getLocation();
    		MAP_HEIGHT = rc.getMapHeight();
    		MAP_WIDTH = rc.getMapWidth();
    		System.out.println(turnCount);
    		System.out.println("I am the HQ located at " + TEAM_HQ_LOCATION);
    		System.out.println("The map is " + MAP_HEIGHT + " by " + MAP_WIDTH);
    		
    		MapLocation maxSoupLocation = scanForSoup(rc.getCurrentSensorRadiusSquared());
    		//System.out.println("The maximum amount of soup is located at (" + maxSoup.x + ", " + maxSoup.y + ")");
    		int[] message = {TEAM_HQ_LOCATION.x, TEAM_HQ_LOCATION.y, maxSoupLocation.x, maxSoupLocation.y, 0, 0, transIdent}; //first two ints are the HQ xy pos and second are closest soup locations
    		rc.submitTransaction(message, 1);
    		
    		if(maxSoupLocation.x != -1) { // If there is soup near, spawn closest to soup.  Otherwise go towards center of map
    			//cnage these to tryBuild in case there is an unhandeled exception
    			tryBuild(RobotType.MINER, TEAM_HQ_LOCATION.directionTo(maxSoupLocation));
    			
    		} else {
    			tryBuild(RobotType.MINER, TEAM_HQ_LOCATION.directionTo(new MapLocation(MAP_WIDTH / 2, MAP_HEIGHT / 2)));
    		}
    		
    	}
    	
    	//Scans the area for nearby robots
    	//Scans the area for topography (high/low dirt walls and water)
    	
    	
    	//Code for shooting down enemy robots
    	RobotInfo[] enemyRobots = rc.senseNearbyRobots(rc.getCurrentSensorRadiusSquared(), rc.getTeam().opponent());
    	for(RobotInfo robot : enemyRobots) {
    		if(robot.getType().equals(RobotType.DELIVERY_DRONE)) {
    			tryShoot(robot.getID());
    		}
    	}
    	
    	//Code for spawning in miner
    	if(shouldSpawnMiner()) {
    		for(Direction d : directions)
    		if(tryBuild(RobotType.MINER, d)) {
    			numMinersSpawned++;
    			break;
    		}
    	}
        
    }

    //Code for the miner
    //Currently want to move around to seek soup and mine that soup
    //Once an amount of soup has been gathered, move back to HQ and deposit
    static void runMiner() throws GameActionException {
    	System.out.println("Miner turn " + turnCount);
    	System.out.println("Soup: " + rc.getSoupCarrying());
    	System.out.println("Objective: " + objective);
    	System.out.println("B Length: " + lastRoundBlock.length);
    	
    	updateTransIdent();
    	getLastBlock();
    	
    	debugPrintTransactionBlock(lastRoundBlock);
    	
    	
    	//First turn setup for nearest soup and blockchain
    	if(turnCount == 1) {
    		Transaction[] firstRoundBlock = rc.getBlock(1);
    		Transaction[] roundOneTeamBlock = seperateTransactionsFromTeam(firstRoundBlock, 1, rc.getTeam());
    		int[] roundOneMessage = roundOneTeamBlock[0].getMessage();
    		
    		TEAM_HQ_LOCATION = new MapLocation(roundOneMessage[0], roundOneMessage[1]);
    		MAP_HEIGHT = rc.getMapHeight();
    		MAP_WIDTH = rc.getMapWidth();
    		closestRefinery = TEAM_HQ_LOCATION;
    		
    		if(rc.getRoundNum() < 5) { //If this is the first miner spawned
	    		targetLocation = new MapLocation(roundOneMessage[2], roundOneMessage[3]);
	    		if(!(targetLocation.equals(INVALID_LOCATION))) { //If there is soup near the HQ
	    			objective = 1;
	    		} else { //No nearby soup so look for soup.  Go to center and look for soup
	    			targetLocation = new MapLocation(rc.getMapHeight() / 2, rc.getMapWidth() / 2);
	    			objective = 1;
	    		}
    		} else { //No nearby soup so look for soup.  Go to center and look for soup
    			targetLocation = new MapLocation(rc.getMapHeight() / 2, rc.getMapWidth() / 2);
    			objective = 1;
    		}
    	}
    	
    	
    	//Add to and update the soupLocations Map
    	//Currently uses ~7,000 bytecode
    	//Should make more efficient later
    	if(turnCount % 4 == 0) {
    		advancedScanForSoup(rc.getCurrentSensorRadiusSquared());
    		System.out.println("Updating Soup");
    		System.out.println(soupLocations);
    	}
    	
    	//update closest refinery
    	if(turnCount % 4 == 1) {
    		updateClosestRefinery();
    		System.out.println("New CLosest Refinery");
    		System.out.println(closestRefinery);
    	}

    	
    	//Preform different actions based off of objective
    	switch(objective) {
    		case 0: //do nothing
    		break;
    		
    		case 1: //Search for soup
    			if(soupLocations.size() == 0) {
    				moveToward(targetLocation);
    			} else {
	    			
	    			Set<MapLocation> locs = soupLocations.keySet();
	    			int closestSoupDist = 500;
	    			MapLocation closestSoup = INVALID_LOCATION;
	    			
	    			for(MapLocation loc : locs) {
	    				if(rc.getLocation().distanceSquaredTo(loc) < closestSoupDist) {
	    					closestSoupDist = rc.getLocation().distanceSquaredTo(loc);
	    					closestSoup = loc;
	    				}
	    			}
	    			
	    			targetLocation = closestSoup;
	    			objective = 2;
    			}
    			
    		break;
    		
    		
    		case 2: //Go to soup
    			rc.setIndicatorLine(rc.getLocation(), targetLocation, 0, 255, 0);
    			
    			if(rc.getLocation().isAdjacentTo(targetLocation)) {
    				if(rc.senseSoup(targetLocation) == 0) {
    					soupLocations.remove(targetLocation);
    					objective = 1;
    				} else {
    				objective = 3;
    				tryMine(rc.getLocation().directionTo(targetLocation));
    				}
    			} else {
    				if(!(hasSpawnedRefinery) && turnCount % 5 == 0) {
    					if(shouldSpawnRefinery()) {
    						hasSpawnedRefinery = tryBuild(RobotType.REFINERY, randomDirection());
    					}
    				}
    				moveToward(targetLocation);
    			}
    		break;
    		
    		
    		case 3: //Mine soup
    			if(rc.getSoupCarrying() >= MIN_SOUP_DEPOSIT_AMOUNT) { // Carrying enough soup
    				objective = 4;
    				moveToward(closestRefinery);
    			} else if(!(tryMine(rc.getLocation().directionTo(targetLocation)))) { //Mining is innefective and not enough soup
    				objective = 1;
    			}
    		break;
    		
    		
    		case 4: //Move to refinery and then deposit
    			if(rc.getLocation().isAdjacentTo(closestRefinery)) {
    				if(tryRefine(rc.getLocation().directionTo(closestRefinery)))
    					objective = 1;
    			} else {
    				moveToward(closestRefinery);
    			}
    			
    			
    	}
    } 

	//Code for the ref
    static void runRefinery() throws GameActionException {
        // System.out.println("Pollution: " + rc.sensePollution(rc.getLocation()));
    	updateTransIdent();
    	getLastBlock();
    	
    	if(turnCount==1) {
         	Team team = rc.getTeam();
         	Team opponent = team.opponent();
    	 }
    }
    
    //Code for the vap
    static void runVaporator() throws GameActionException {
    	updateTransIdent();
    	getLastBlock();
    	
    	if(turnCount==1) {
         	Team team = rc.getTeam();
         	Team opponent = team.opponent();
    	 }

    }

    //Code for the des school
    static void runDesignSchool() throws GameActionException {
    	updateTransIdent();
    	getLastBlock();
    	
    	if(turnCount==1) {
         	Team team = rc.getTeam();
         	Team opponent = team.opponent();
    	 }

    }
    
    //Code for the ful center
    static void runFulfillmentCenter() throws GameActionException {
    	updateTransIdent();
    	getLastBlock();
    	
    	 if(turnCount==1) {
         	Team team = rc.getTeam();
         	Team opponent = team.opponent();
    	 }

    }
    
    //Code to run landscaper
    //Currently wan to build a wall around HQ
    static void runLandscaper() throws GameActionException {
    	updateTransIdent();
    	getLastBlock();
    	
    	 if(turnCount==1) {
         	Team team = rc.getTeam();
            Team opponent = team.opponent();
    	 }

    }
    
    //Code to run delivery drones
    static void runDeliveryDrone() throws GameActionException {
    	updateTransIdent();
    	getLastBlock();

        if(turnCount==1) {
        	Team team = rc.getTeam();
            Team opponent = team.opponent();
            objective = 1;
        }
        
        switch(objective) {
            case 0: //do nothing
            break;
                
            case 1: //determine bot to carry and move to it
                RobotInfo[] nearbyBots = rc.senseNearbyRobots(rc.getCurrentSensorRadiusSquared(), opponentTeam);
                if(nearbyBots.length==0){
                    tryMove(randomDirection());
                }
                RobotInfo targetBot;
                for(int i = nearbyBots.length-1; i>=0 ; i--){
                    if(nearbyBots[i].getType().equals(RobotType.LANDSCAPER)||nearbyBots[i].getType().equals(RobotType.MINER)){
                        targetBot=nearbyBots[i];
                    }
                }
                if(rc.canPickUpUnit(targetBot.ID)) {
                    rc.pickUpUnit(targetBot.ID);
                    objective=2;
                }  else {
                    rc.move(rc.getLocation().directionTo(targetBot.getLocation()));
                }
            case 2: //Find water and drop
                MapLocation targetLocation = scanForNearbyWater();
                ArrayList<MapLocation> flooding= getFlooding();
                if(flooding.size()==0){
                    tryMove(randomDirection());
                }else{
                    //Find closest flooded location
                    for(MapLocation x : flooding){
                        if(rc.getLocation().distanceSquaredTo(x)<minDistanceSquared){
                            targetLocation=x;
                            minDistanceSquared=rc.getLocation().distanceSquaredTo(x);
                        }
                    }
                }
                if(rc.getLocation().isAdjacentTo(x)){
                    rc.dropUnit(rc.getLocation().directionTo(targetLocation.getLocation()));
                    objective=1;
                }else{
                    if(rc.canMove(rc.getLocation().directionTo(targetLocation.getLocation()))){
                    rc.move(rc.getLocation().directionTo(targetLocation.getLocation());
                    }
                }
            case 3:

        }
    }

    //code to run net gun
    static void runNetGun() throws GameActionException {
    	updateTransIdent();
    	getLastBlock();
    	
    	//Scan for enemy drones and shoot them down
    	RobotInfo[] enemyRobots = rc.senseNearbyRobots(rc.getCurrentSensorRadiusSquared(), rc.getTeam().opponent());
    	for(RobotInfo robot : enemyRobots) {
    		if(robot.getType().equals(RobotType.DELIVERY_DRONE)) {
    			tryShoot(robot.getID());
    		}
    	}
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
    
    /**
     * Attempts to shoot down a drone
     * 
     * @param ID of drone
     * @return true if the robot is shot down, false otherwise
     * @throws GameActionException
     */
    static boolean tryShoot(int id) throws GameActionException {
    	if(rc.canShootUnit(id)) {
    		rc.shootUnit(id);
    		return true;
    	}
    	return false;
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
     * Moves toward the objective location 
     * 
     * @param loc to move toward
     * @return true if the movement was performed
     * @throws GameActionException
     */
    static boolean moveToward(MapLocation loc) throws GameActionException {
    	Direction dir = rc.getLocation().directionTo(loc);
    	
    	if(!(rc.senseFlooding(rc.getLocation().add(dir))) && rc.canMove(dir)) {
    		rc.move(dir);
    		return true;
    	}
    	return false;
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
    	//System.out.println(turnCount);
        if (rc.isReady() && rc.canMineSoup(dir) && rc.senseSoup(rc.getLocation().add(dir)) != 0) {
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
    
    /**
     * Decided wether or not to spawn in a design school
     * 
     * @param none
     * @returns ture if design school should be created
     * @throws GameActionException
     */
    static boolean shoudlSpawnDesignSchool() throws GameActionException {
    	//Implement
    	return false;
    }
    
    /**
     * Decided if a vaparator should be spawned
     * @return true if a vaparator should be spawned 
     * @throws GameActionException
     */
    static boolean shouldSpawnVaparator() throws GameActionException {
    	//Implement
    	return false;
    }
    
    /**
     * Decided if a net gun should be spawned
     * 
     * @return true if a net gun should be spawned
     * @throws GameActionException
     */
    static boolean shouldSpawnNetGun() throws GameActionException {
    	//Implement
    	return false;
    }
    
    /**
     * Decided if a furfillment center should be spawned
     * 
     * @return true if a furfillment center should be spawned
     * @throws GameActionException
     */
    static boolean shouldSpawnCenter() throws GameActionException {
    	//Implement
    	return false;
    }
    
    /**
     * Decided if a miner should be spawned based off of current game parameters
     * 
     * @param numMiners
     * @return true if algorithm dictates new miner
     * @throws GameActionException
     */
    static boolean shouldSpawnMiner() throws GameActionException {
    	System.out.print("Spawned Miners: " + numMinersSpawned);
    	//Make more advanced later
    	if(rc.getTeamSoup() >= 200 && numMinersSpawned < 4) {
    		System.out.println("True");
    		return true;
    	}
    	return false;
    }
    
    /**
     * Decides if a miner should create a refienry based off of current game parameters
     * 
     * @return true if a refinery should be spawned
     * @throws GameActionException
     */
    static boolean shouldSpawnRefinery() throws GameActionException {
    	if(rc.getLocation().distanceSquaredTo(TEAM_HQ_LOCATION) < 50) {
    		return false;
    	}
    	return true;
    }
    
    /**
     * Scans the area for the highest amount of soup
     * 
     * @param None
     * @return MapLocation of the highest amount of soup
     * @throws GameActionException
     */
    static MapLocation scanForSoup(int radius) throws GameActionException {
    	int maxSoup = 0;
    	MapLocation maxSoupLocation = new MapLocation(-1, -1); 
    	
    	int rootRadius = (int) Math.floor(Math.sqrt(radius));
    	
    	for(int x = -rootRadius; x <= rootRadius; x++) {
    		for(int y = -rootRadius; y <= rootRadius; y++) {
    			//System.out.println(x + " " + y);
    			if(rc.canSenseLocation(rc.getLocation().translate(x, y))) {
    				if(rc.senseSoup(rc.getLocation().translate(x, y)) > maxSoup && !(rc.senseFlooding(rc.getLocation().translate(x, y)))) {
    					maxSoup = rc.senseSoup(rc.getLocation().translate(x, y));
    					maxSoupLocation = rc.getLocation().translate(x, y);
    				}
    			}
    		}
    	}
    	return maxSoupLocation;
    }
    
    /**
     * Scans the area for soup, adding locations to an Map<loc, amount>
     * @param radius
     * @return
     * @throws GameActionException
     */
    static void advancedScanForSoup(int radius) throws GameActionException { 	
    	int rootRadius = (int) Math.floor(Math.sqrt(radius));
    	
    	searchloop:
    	for(int x = -rootRadius; x <= rootRadius; x++) {
    		for(int y = -rootRadius; y <= rootRadius; y++) {
    			//System.out.println(x + " " + y);
    			MapLocation testLoc = rc.getLocation().translate(x, y);
    			if(rc.canSenseLocation(testLoc)) {
    				if(rc.senseFlooding(testLoc) || rc.senseSoup(testLoc) == 0) {
    					soupLocations.remove(testLoc);
    				} else if(soupLocations.containsKey(testLoc)){
    					soupLocations.replace(testLoc, rc.senseSoup(testLoc));
    				} else {
    					soupLocations.put(testLoc, rc.senseSoup(testLoc));
    				}
    			}
    			if(soupLocations.size() > 5) {
    				break searchloop;
    			}
    		}
    	}
    }
    
    /**
     * Scans the area for all nearby enemy robots and does stuff based off of what they find
     * 
     * @param none
     * @returns none
     * @throws GameActonException 
     */
    static void scanForEnemyRobots() throws GameActionException {
    	RobotInfo[] robots = rc.senseNearbyRobots(rc.getCurrentSensorRadiusSquared(), rc.getTeam().opponent());
    	
    	for(RobotInfo robot : robots) {
    		if(robot.getType().equals(RobotType.HQ)) {
    			int[] message = {1, robot.getLocation().x, robot.getLocation().y, 1, 0, 0, transIdent};
    			if(rc.canSubmitTransaction(message, 10)) {
    				rc.submitTransaction(message, 10);
    			}
    		}
    	}
    }
    
    /**
     * Find the nearest water tile within the robots current sensor radius
     * 
     * @param none
     * @returns MapLocation of nearest water tile
     * @throws GameActionException
     */
    static MapLocation scanForNearbyWater() throws GameActionException {
    	
    	return INVALID_LOCATION;
    }
    
    /**
     * Finds the nearest type of robot
     * 
     * @param type of robot to look for
     * @return MapLocation of the robot, -1, -1 otherwise (invalid MapLocation)
     * @throws GameActionException
     */
    static MapLocation findNearestRobotType(RobotType type) {
    	RobotInfo[] robots = rc.senseNearbyRobots();
    	for(RobotInfo robot : robots) {
    		if(robot.type.equals(type)) {
    			return robot.location;
    		}
    	}
    	return INVALID_LOCATION;
    }
    
    /**
     * Finds the nearest type of robot
     * 
     * @param type of robot to look for and which team
     * @return MapLocation of the robot, invalid otherwise
     * @throws GameActionException
     */
    static MapLocation findNearestRobotTypeOnTeam(RobotType type, Team team) {
    	RobotInfo[] robots = rc.senseNearbyRobots();
    	for(RobotInfo robot : robots) {
    		if(robot.type.equals(type) && robot.team.equals(team)) {
    			return robot.location;
    		}
    	}
    	return INVALID_LOCATION;
    }
    
    /**
     * Prints off block of transactions
     * 
     * @param array of transactions
     * @throws GameActionException
     */
    static void debugPrintTransactionBlock(Transaction[] block) throws GameActionException {
    	for(Transaction trans : block) {
			for(int test : trans.getMessage()) {
				System.out.print(test + ", ");
			}
			System.out.println("Cost: " + trans.getCost());
		}
    }
    
    /**
     * Updates transIdent based off of round
     * 
     * @param none
     * @return none
     * @throws GameActionException
     */
    static void updateTransIdent() throws GameActionException {
    	//Implement later
    	transIdent = transIdent;
    }
    
    /**
     * Updates the lastRoundBlock to whatever messages were send to the blockchain
     * 
     * @param none
     * @return none
     * @throws GameActionException
     */
    static void getLastBlock() throws GameActionException {
    	try {
    		lastRoundBlock = rc.getBlock(rc.getRoundNum() - 1);
    	} catch(GameActionException e) {
    		System.out.println("Something went wrong");
    		System.out.println("Probably no block to get");
    	}
    }
    
    /**
     * Verifies a message is from our team
     * 
     * @param message (int array) to be verified
     * @returns true if the message is from our team, false otherwise
     * @throws GameActionException
     */
    static boolean verifyMessage(int[] message, int roundNum) throws GameActionException {
    	return(message[6] == transIdent);
    }
    
    /**
     * Returns transactions from a specific team from an array of transactions (block)
     * 
     * @param block of transactions and team
     * @returns block of transactions from specific team
     * @throws GameActionException
     */
    static Transaction[] seperateTransactionsFromTeam(Transaction[] block, int roundNum, Team team) throws GameActionException {
    	Transaction[] teamBlock = new Transaction[7];
    	int teamBlockIndex = 0;
    	
    	for(int i = 0; i < block.length; i++) {
    		if(verifyMessage(block[i].getMessage(), roundNum)) {
    			teamBlock[teamBlockIndex++] = block[i];
    		}
    	}
    	
    	return teamBlock;
    }
    
    /**
     * Decodes the given message and preforms the tasks described
     * 
     * @param int[] message to be decoded
     * @returns none
     * @throws GameActionException
     */
    static void decodeMessage(int[] message) throws GameActionException {
    	switch(message[0]) {
    		case 1: //Enemy HQ position
    			ENEMY_HQ_LOCATION = new MapLocation(message[1], message[2]);
    		break;
    	}
    }
    
    /**
     * Scans the nearby area for team refineries
     * 
     * @param none
     * @return true if closest refinery has been updated, false otherwise
     * @throws GameActionEception
     */
    static boolean updateClosestRefinery() throws GameActionException {
    	if(findNearestRobotTypeOnTeam(RobotType.REFINERY, rc.getTeam()).equals(INVALID_LOCATION)) {
    		return false;
    	}
    	closestRefinery = findNearestRobotTypeOnTeam(RobotType.REFINERY, rc.getTeam());
    	return true;
    }
    
    /**
     * Moves along the path, updating path if the robot cannot move along a path
     * 
     * @param none
     * @returns none 
     * @throws GameActionException
     */
    static void followPath() throws GameActionException {
    	//System.out.println("In followPath()");
    	//System.out.println(rc.isReady());
    	//System.out.println(rc.canMove(moves[moveIndex]));
    	
    	//TODO tryMove is called twice, check with JC
    	
    	tryMove(moves[moveIndex]);
    	if(!(tryMove(moves[moveIndex]))) {
    	//	System.out.println("Not good, but can still be saved");
    		if(rc.isReady() && rc.canMove(moves[moveIndex])) {
    	//		System.out.println("NOW WE'RE FUCKED BOYS!");
    			mapObstacles();
    			createPath();
    		}
    	}
    	else if(moves[moveIndex] == Direction.CENTER) {
    		followingPath = false;
    	//	System.out.println("maybe good maybe bad");
    	}
    	
    	
    }
    
    /**
     * Maps the obstacles around the robot, updating obstacles matrix
     * 
     * @param none
     * @return none
     * @throws GameActionException
     */
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
    
    /**
     * Creates a new path for the robot to follow to a target location
     * 
     * @param none
     * @return none
     * @throws GameActionException
     */
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
    
    /**
     * 
     * @param loc is current location of robot
     * @param dir is the direction wanted to move
     * @param CurrentX is x index of the matrix
     * @param CurrentY is y index of the matrix
     * @return true if the robot can move onto the indicated location
     */
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
