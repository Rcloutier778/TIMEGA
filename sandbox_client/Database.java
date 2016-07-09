package sandbox_client;

/**
 * Records all relevant game information client-side. Updated by information sent from the server. All data structures are
 * private, intended only to be acquired via accessors.
 * 
 * @author dmayans
 */

import javafx.scene.paint.Color;
import server.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeSet;



public class Database {
	
	//         _                           
	//   _ __ | | __ _ _   _  ___ _ __ ___ 
	//  | '_ \| |/ _` | | | |/ _ \ '__/ __|
	//  | |_) | | (_| | |_| |  __/ |  \__ \
	//  | .__/|_|\__,_|\__, |\___|_|  |___/
	//  |_|            |___/  
	
	private static final ArrayList<Player> PLAYERS = new ArrayList<Player>();
		// ^ maps an index to a Player object
	private static final HashMap<String,Integer> INDICES = new HashMap<String,Integer>();
		// ^ maps a player name to its index in the PLAYERS arraylist
	private static String NAME = null;
		// ^ local player's name
	
	private static final HashMap<String, Color> COLORS = new HashMap<String,Color>();
		// ^ maps a player name to his color
	private static final HashMap<String, String> RACES = new HashMap<String,String>();
		// ^ maps a player name to his race
	
	
	// returns the number of players
	public static int numPlayers() {
		synchronized(PLAYERS) {
			return PLAYERS.size();
		}
	}
	
	// iterates over the list of player names
	public static Iterable<String> playerNames() {
		synchronized(PLAYERS) {
			LinkedList<String> output = new LinkedList<String>();
			for(Player p : PLAYERS) {
				output.addLast(p.name);
			}
			return output;
		}
	}
	
	// returns the information for a given player
	public static Player getPlayer(int i) {
		synchronized(PLAYERS) {
			return PLAYERS.get(i);
		}
	}
	
	// returns the index of a given player
	public static int indexOf(String player) {
		synchronized(INDICES) {
			return INDICES.get(player);
		}
	}
	
	// returns the race of a given player
	public static String raceOf(String player) {
		synchronized(RACES) {
			return RACES.get(player);
		}
	}
	
	// returns the color of a given planet based on its owner
	public static Color colorOfPlanet(String planetName) {
		synchronized(COLORS) {
			return COLORS.get(Database.ownerOf(planetName));
		}
	}
	
	// set/change the name of the local player
	public static void setName(String name) {
		Integer n;
		synchronized(PLANETS) {
			
			for(int i=0; i<4; i++) {
				LOCAL_TECH_DISCOUNT[i] = 0;
			}
			LOCALPLANETS.clear();
			NAME = name;
			
			for(Map.Entry<String, String> e : PLANETS.entrySet()) {
				
				if(e.getValue().equals(name)) {
					LOCALPLANETS.add(e.getKey());
					
					for(int i=0; i<4; i++) {
						if((n = TECH[i].get(e.getKey())) != null) {
							LOCAL_TECH_DISCOUNT[i] += n;
						}
					}
				}
				
			}
		}
	}
	
	public static String getName() {
		return NAME;
	}
	
	
	
	//   _   _ _           
	//  | |_(_) | ___  ___ 
	//  | __| | |/ _ \/ __|
	//  | |_| | |  __/\__ \
	//   \__|_|_|\___||___/
	
	private static final ArrayList<Tile> TILES = new ArrayList<Tile>();
	
	// add a tile to the database
	public static void addTile(Tile t) {
		synchronized(TILES) {
			TILES.add(t);
		}
	}
	
	// access a tile
	public static Tile tile(int index) {
		synchronized(TILES) {
			return TILES.get(index);
		}
	}
	
	
	//         _                  _       
	//   _ __ | | __ _ _ __   ___| |_ ___ 
	//  | '_ \| |/ _` | '_ \ / _ \ __/ __|
	//  | |_) | | (_| | | | |  __/ |_\__ \
	//  | .__/|_|\__,_|_| |_|\___|\__|___/
	//  |_|
	
	private static final HashMap<String,String> PLANETS = new HashMap<String,String>();
		// ^ maps a planet name to the name of its owner - "none" for neutral planets
	private static final HashMap<String,Boolean> SPACEDOCKS = new HashMap<String,Boolean>();
		// ^ maps a planet name to whether it has a space dock
	private static final TreeSet<String> LOCALPLANETS = new TreeSet<String>();
		// ^ holds all planets owned by local player, sorted alphabetically - USES SAME LOCK AS PLANETS
	private static final int[] LOCAL_TECH_DISCOUNT = {0,0,0,0};
		// ^ holds the total technology discounts of the local player
	private static final HashMap<String,Integer> RESOURCES = new HashMap<String,Integer>();
		// ^ maps a planet name to its resource value
	private static final HashMap<String,Integer> INFLUENCE = new HashMap<String,Integer>();
		// ^ maps a planet name to its influence value
	@SuppressWarnings("unchecked")
	private static final HashMap<String,Integer>[] TECH = (HashMap<String,Integer>[]) new HashMap[4];
		// ^ maps a tech index and a planet name to the degree of tech specialty it has (i.e., RED, "Industrex" => 2)
	
	// add a new planet to the database
	public static void addPlanetName(String name) {
		synchronized(PLANETS) {
			PLANETS.put(name, "none");
		}
		synchronized(SPACEDOCKS) {
			SPACEDOCKS.put(name, false);
		}
	}
	
	// resource mutator
	public static void setResources(String planetName, int resources) {
		synchronized(RESOURCES) {
			RESOURCES.put(planetName, resources);
		}
	}
	
	// resource accessor
	public static int resourcesOf(String planetName) {
		synchronized(RESOURCES) {
			return RESOURCES.get(planetName);
		}
	}
	
	// influence mutator
	public static void setInfluence(String planetName, int influence) {
		synchronized(INFLUENCE) {
			INFLUENCE.put(planetName, influence);
		}
	}
	
	// influence accessor
	public static int influenceOf(String planetName) {
		synchronized(INFLUENCE) {
			return INFLUENCE.get(planetName);
		}
	}
	
	// tech spec mutator
	public static void addTechSpec(String planetName, int techType) {
		synchronized(TECH) {
			Integer prev = TECH[techType].get(planetName);
			int newValue = (prev == null) ? 1 : prev + 1;
			TECH[techType].put(planetName, newValue);
		}
	}
	
	// path to empty image
	public static final String PATH_TO_NULL = "file:" + Main.PATH_TO_ASSETS + "Icons/null.png";
	
	// path to image icon for given resource/influence amount
	public static String iconPath(boolean resource, int amount) {
		String output = "file:" + Main.PATH_TO_ASSETS + "Icons/";
		if(resource) {
			output += "resource_";
		} else {
			output += "influence_";
		}
		
		return output + Integer.toString(amount) + ".png";
	}
	
	// finds the owner of a given planet
	public static String ownerOf(String planet) {
		synchronized(PLANETS) {
			return PLANETS.get(planet);
		}
	}
	
	// change the owner of the given planet
	public static void updatePlanet(String planetName, String newOwner) {
		Integer n;
		synchronized(PLANETS) {
			
			PLANETS.put(planetName, newOwner);
			
			if(LOCALPLANETS.contains(planetName)) {
				LOCALPLANETS.remove(planetName);
				
				for(int i=0; i<4; i++) {
					if((n = TECH[i].get(planetName)) != null) {
						LOCAL_TECH_DISCOUNT[i] -= n;
					}
				}
				
			} if(newOwner.equals(NAME)) {
				LOCALPLANETS.add(planetName);
				
				for(int i=0; i<4; i++) {
					if((n = TECH[i].get(planetName)) != null) {
						LOCAL_TECH_DISCOUNT[i] += n;
					}
				}
			}
		}
	}
	
	// iterate over local planets
	public static Iterable<String> getLocalPlanets() {
		synchronized(PLANETS) {
			return new TreeSet<String>(LOCALPLANETS);
		}
	}
	
	// determine whether a planet is owned by the local player
	public static boolean isLocalPlanet(String planet) {
		synchronized(PLANETS) {
			return LOCALPLANETS.contains(planet);
		}
	}
	
	// space dock accessor
	public static boolean hasSD(String planet) {
		synchronized(SPACEDOCKS) {
			return SPACEDOCKS.get(planet);
		}
	}
	
	// space dock mutator
	public static void updateSD(String planetName, boolean sdock) {
		synchronized(SPACEDOCKS) {
			SPACEDOCKS.put(planetName, sdock);
		}
	}
	
	// returns the current technology discount
	public static int localTechDiscount(int index) {
		synchronized(PLANETS) {
			return LOCAL_TECH_DISCOUNT[index];
		}
	}
	
	
	//   _            _                 _                   
	//  | |_ ___  ___| |__  _ __   ___ | | ___   __ _ _   _ 
	//  | __/ _ \/ __| '_ \| '_ \ / _ \| |/ _ \ / _` | | | |
	//  | ||  __/ (__| | | | | | | (_) | | (_) | (_| | |_| |
	//   \__\___|\___|_| |_|_| |_|\___/|_|\___/ \__, |\__, |
	//                                          |___/ |___/ 
	
	// tech indices (used publicly)
	public static final int RED = 0, BLUE = 1, GREEN = 2, YELLOW = 3;
	
	private static final HashMap<String,String> DESCRIPTIONS = new HashMap<String,String>();
		// ^ maps a tech name to its description
	private static final LinkedList<String> TECH_QUEUE = new LinkedList<String>();
		// ^ holds local changes to the technology tree before they're sent to the server - USES SAME LOCK AS TECH_MAP
	private static final HashMap<String,TreeSet<String>> TECH_MAP = new HashMap<String,TreeSet<String>>();
		// ^ maps a player name to the set of all techs he's researched
	
	@SuppressWarnings("unchecked")
	private static final ArrayList<String>[] TECH_NAMES = new ArrayList[4];
		// ^ holds a list of all technology names
	private static final HashMap<String,Integer> TECH_PRIORITY = new HashMap<String,Integer>();
		// ^ maps a tech name to the order in which it should appear
	private static int _redIndex = 0, _blueIndex = 0, _greenIndex = 0, _yellowIndex = 0;
	private static final HashMap<String,String> PREREQUISITES = new HashMap<String,String>();
		// ^ maps a tech name to its first prerequisite, if any
	private static final HashMap<String,String> PREREQ_AND = new HashMap<String,String>();
		// ^ maps a tech name to its second prerequisite, if conjoined by "and"
	private static final HashMap<String,String> PREREQ_OR = new HashMap<String,String>();
		// ^ maps a tech name to its second prerequisite, if conjoined by "or"
	
	
	// add a technology to the local queue
	public static void enqueueTech(String tech) {
		synchronized(TECH_MAP) {
			TECH_QUEUE.addLast(tech);
		}
	}
	
	// remove a technology from the local queue
	public static void removeFromTechQueue(String tech) {
		synchronized(TECH_MAP) {
			TECH_QUEUE.remove(tech);
		}
	}
	
	// iterate over the local tech queue
	public static Iterable<String> getTechQueue() {
		synchronized(TECH_MAP) {
			return new LinkedList<String>(TECH_QUEUE);
		}
	}
	
	// empty the local tech queue
	public static void clearTechQueue() {
		synchronized(TECH_MAP) {
			TECH_QUEUE.clear();
		}
	}
	
	// check if a tech is currently enqueued
	public static boolean isTechEnqueued(String tech) {
		synchronized(TECH_MAP) {
			return TECH_QUEUE.contains(tech);
		}
	}
	
	// check whether a player currently has a tech (as far as the server is concerned)
	public static boolean hasTech(String player, String tech) {
		synchronized(TECH_MAP) {
			return TECH_MAP.get(player).contains(tech);
		}
	}
	
	// same as above, but accounts for local database
	public static boolean hasTechLocal(String player, String tech) {
		synchronized(TECH_MAP) {
			boolean b = TECH_MAP.get(player).contains(tech);
			if(player.equals(NAME) && TECH_QUEUE.contains(tech)) {
				return !b;
			}
			return b;
		}
	}
	
	// adds a player, tech pair to the database in response to information from the server
	public static void research(String player, String tech) {
		synchronized(TECH_MAP) {
			TECH_MAP.get(player).add(tech);
			if(TECH_QUEUE.contains(tech)) {
				TECH_QUEUE.remove(tech);
			}
		}
	}
	
	// removes a player, tech pair from the database in response to information from the server
	public static void forget(String player, String tech) {
		synchronized(TECH_MAP) {
			TECH_MAP.get(player).remove(tech);
			if(TECH_QUEUE.contains(tech)) {
				TECH_QUEUE.remove(tech);
			}
		}
	}
	
	// iterate over all of the technology owned by one player
	public static Iterable<String> technologyOf(String player) {
		synchronized(TECH_MAP) {
			return new TreeSet<String>(TECH_MAP.get(player));
		}
	}
	
	// add a tech to the database
	public static void addTechToDatabase(String name, String color, String effect,
			String prereq1, String prereqand, String prereqor) {
		int colorIndex = -1, priority = 0;
		color = color.toLowerCase();
		if(color.equals("red")) {
			colorIndex = RED;
			priority += _redIndex++;
		} else if(color.equals("blue")) {
			colorIndex = BLUE;
			priority += 1000 + _blueIndex++;
		} else if(color.equals("green")) {
			colorIndex = GREEN;
			priority += 2000 + _greenIndex++;
		} else if(color.equals("yellow")) {
			colorIndex = YELLOW;
			priority += 3000 + _yellowIndex++;
		}
		
		TECH_NAMES[colorIndex].add(name);
		TECH_PRIORITY.put(name, priority);
		DESCRIPTIONS.put(name, effect);
		PREREQUISITES.put(name, prereq1);
		PREREQ_AND.put(name, prereqand);
		PREREQ_OR.put(name, prereqor);

	}
	
	// compare two technology strings to see which comes first
	public static int compareTech(String tech1, String tech2) {
		synchronized(TECH_PRIORITY) {
			return TECH_PRIORITY.get(tech1) - TECH_PRIORITY.get(tech2);
		}
	}
	
	// return the description of a given tech name
	public static String descriptionOf(String tech) {
		synchronized(DESCRIPTIONS) {
			return DESCRIPTIONS.get(tech);
		}
	}
	
	// given the color and index, return the name of the technology
	public static String getTechName(int color, int index) {
		synchronized(TECH_NAMES) {
			return TECH_NAMES[color].get(index);
		}
	}
	
	// given the name of a tech, return its prerequisites
	public static String[] prereqsOfTech(String tech) {
		synchronized(PREREQUISITES) {
			return new String[]{PREREQUISITES.get(tech), PREREQ_AND.get(tech), PREREQ_OR.get(tech)};
		}
	}
	
	// given the name of a tech, return its color
	public static int colorOfTech(String name) {
		for(int i=0; i<4; i++) {
			if(TECH_NAMES[i].contains(name)) {
				return i;
			}
		}
		
		return -1;
	}
	
	
	//                                              _ 
	//   _ __   ___ _ __ ___  ___  _ __  _ __   ___| |
	//  | '_ \ / _ \ '__/ __|/ _ \| '_ \| '_ \ / _ \ |
	//  | |_) |  __/ |  \__ \ (_) | | | | | | |  __/ |
	//  | .__/ \___|_|  |___/\___/|_| |_|_| |_|\___|_|
	//  |_|                                           

	
	private static final HashMap<String,Integer> TIERS = new HashMap<String,Integer>();
		// ^ maps a personnel to its tier
	
	private static final HashSet<String> PERSONNEL_QUEUE = new HashSet<String>();
		// ^ holds changes to local personnel before it's sent to the server - SHARES LOCK WITH PERSONNEL_MAP
	private static final HashMap<String,TreeSet<String>> PERSONNEL_MAP = new HashMap<String,TreeSet<String>>();
		// ^ maps a player name to the set of owned personnel
	private static final HashMap<String,Integer> PERSONNEL_PRIORITY = new HashMap<String,Integer>();
		// ^ maps a personnel to its order
	private static final int[] _tierIndices = {0, 0, 0, 0, 0};
	@SuppressWarnings("unchecked")
	private static final TreeSet<String>[] PERSONNEL_SET = new TreeSet[3];
	
	// returns the personnel tier of a given player for that color
	public static int personnelTier(String player, int color) {
		int output = 0;
		synchronized(PLANETS) {
			synchronized(SPACEDOCKS) {
				for(String planet : LOCALPLANETS) {
					if(SPACEDOCKS.get(planet)) { 
						output += TECH[color].containsKey(planet) ? 1 : 0;
					}
				}
			}
		}
		return output;
	}
	
	// hire a person
	public static void localHirePerson(String person) {
		synchronized(PERSONNEL_MAP) {
			if(PERSONNEL_MAP.get(NAME).contains(person)) {
				PERSONNEL_QUEUE.remove(person);
			} else {
				PERSONNEL_QUEUE.add(person);
			}
		}
	}
	
	// technically just an alias
	public static void localReleasePerson(String person) {
		Database.localHirePerson(person);
	}
	
	// iterate over the personnel queue
	public static Iterable<String> getPersonnelQueue() {
		synchronized(PERSONNEL_MAP) {
			return new HashSet<String>(PERSONNEL_QUEUE);
		}
	}
	
	// clear the personnel queue
	public static void clearPersonnelQueue() {
		synchronized(PERSONNEL_MAP) {
			PERSONNEL_QUEUE.clear();
		}
	}
	
	// determine if a person is in the local queue
	public static boolean isPersonEnqueued(String person) {
		synchronized(PERSONNEL_MAP) {
			return PERSONNEL_QUEUE.contains(person);
		}
	}
	
	// respond to server output and add a player, personnel pair to the database
	public static void hire(String player, String person) {
		synchronized(PERSONNEL_MAP) {
			PERSONNEL_MAP.get(player).add(person);
			if(PERSONNEL_QUEUE.contains(person)) {
				PERSONNEL_QUEUE.remove(person);
			}
		}
	}
	
	// respond to server output and remove a player, personnel pair from the database
	public static void release(String player, String person) {
		synchronized(PERSONNEL_MAP) {
			PERSONNEL_MAP.get(player).remove(person);
			if(PERSONNEL_QUEUE.contains(person)) {
				PERSONNEL_QUEUE.remove(person);
			}
		}
	}
	
	// return whether the given player has the given personnel in the server database
	public static boolean hasPerson(String player, String person) {
		synchronized(PERSONNEL_MAP) {
			return PERSONNEL_MAP.get(player).contains(person);
		}
	}
	
	// iterate over the personnel of the given player
	public static Iterable<String> personnelOfPlayer(String player) {
		synchronized(PERSONNEL_MAP) {
			return new TreeSet<String>(PERSONNEL_MAP.get(player));
		}
	}
	
	// iterate over personnel of a given color
	public static Iterable<String> personnelOfColor(int color) {
		synchronized(PERSONNEL_SET) {
			return new LinkedList<String>(PERSONNEL_SET[color]);
		}
	}
	
	// return whether the given player has the given personnel in the local database
	public static boolean localHasPerson(String player, String person) {
		synchronized(PERSONNEL_MAP) {
			boolean b = PERSONNEL_MAP.get(player).contains(person);
			if(player.equals(NAME)) {
				return b ^ PERSONNEL_QUEUE.contains(person);
			}
			return b;
		}
	}
	
	// populate database from xml
	public static void addPersonnelToDatabase(String name, String color, String effect, String tier) {
		DESCRIPTIONS.put(name, effect);
		int tierNum = Integer.parseInt(tier);
		TIERS.put(name, tierNum);
		
		int index = -1, priority = 0;
		if(color.equals("red")) {
			index = RED;
			priority += _tierIndices[tierNum]++;
		} else if(color.equals("blue")) {
			index = BLUE;
			priority += 1000 + _tierIndices[tierNum]++;
		} else if(color.equals("green")) {
			index = GREEN;
			priority += 2000 + _tierIndices[tierNum]++;
		}
		
		PERSONNEL_SET[index].add(name);
		PERSONNEL_PRIORITY.put(name, priority);
	}
	
	// compares two personnel
	public static int comparePersonnel(String person1, String person2) {
		synchronized(PERSONNEL_PRIORITY) {
			return PERSONNEL_PRIORITY.get(person1) - PERSONNEL_PRIORITY.get(person2);
		}
	}
	
	// given the name of a person, return its tier
	public static int tierOfPersonnel(String personnel) {
		synchronized(TIERS) {
			return TIERS.get(personnel);
		}
	}
	
	
	
	//                       _          
	//   ___ _ __ ___  _ __ (_)_ __ ___ 
	//  / _ \ '_ ` _ \| '_ \| | '__/ _ \
	// |  __/ | | | | | |_) | | | |  __/
	//  \___|_| |_| |_| .__/|_|_|  \___|
	//                |_|  
	
	private static boolean ADVANCING = false;
		// ^ set to true of the local player is advancing
	private static HashMap<String,Integer> STAGE_MAP = new HashMap<String,Integer>();
		// ^ maps a player name to his empire stage
	
	private static ArrayList<String> STAGE_NAMES = new ArrayList<String>();
		// ^ maps a stage index to its name
	private static ArrayList<Integer> STAGE_COMMAND = new ArrayList<Integer>();
		// ^ maps a stage index to its command pool
	private static ArrayList<Integer> STAGE_FLEET = new ArrayList<Integer>();
		// ^ maps a stage index to its fleet supply
	private static ArrayList<String> STAGE_OBJECTIVES = new ArrayList<String>();
		// ^ maps a stage index to its list of objectives
	private static ArrayList<String> STAGE_REWARDS = new ArrayList<String>();
		// ^ maps a stage index to the reward for reaching the next stage
	
	// add a new empire stage to the database
	public static void addEmpireStage(String name, int commandPool, int fleetSupply, String objectives, String reward) {
		STAGE_NAMES.add(name);
		STAGE_COMMAND.add(commandPool);
		STAGE_FLEET.add(fleetSupply);
		STAGE_OBJECTIVES.add(objectives);
		STAGE_REWARDS.add(reward);
	}
	
	// returns the maximum number of empire stages
	public static int numStages() {
		return STAGE_NAMES.size();
	}
	
	// accessors for empire stage info
	public static String nameOfStage(int index) {
		return STAGE_NAMES.get(index);
	}
	
	public static int commandPoolOfStage(int index) {
		return STAGE_COMMAND.get(index);
	}
	
	public static int fleetSupplyOfStage(int index) {
		return STAGE_FLEET.get(index);
	}
	
	public static String objectivesOfStage(int index) {
		return STAGE_OBJECTIVES.get(index);
	}
	
	public static String rewardsOfNextStage(int index) {
		return STAGE_REWARDS.get(index);
	}
		
	// advancing mutator
	public static void setAdvancing(boolean b) {
		ADVANCING = b;
	}
	
	// advancing accessor
	public static boolean isAdvancing() {
		return ADVANCING;
	}
	
	// respond to server input to advance an player in the database
	public static void advancePlayer(String player) {
		synchronized(STAGE_MAP) {
			int prevStage = STAGE_MAP.get(player);
			if(prevStage == STAGE_MAP.size() - 1) {
				return;
			}
			STAGE_MAP.put(player, prevStage + 1);
		}
	}
	
	// return empire stage of a given player
	public static int empireStageOf(String player) {
		synchronized(STAGE_MAP) {
			return STAGE_MAP.get(player);
		}
	}
	
	
	//                       _       _   _                 
	//   _ __ ___  ___  ___ | |_   _| |_(_) ___  _ __  ___ 
	//  | '__/ _ \/ __|/ _ \| | | | | __| |/ _ \| '_ \/ __|
	//  | | |  __/\__ \ (_) | | |_| | |_| | (_) | | | \__ \
	//  |_|  \___||___/\___/|_|\__,_|\__|_|\___/|_| |_|___/

	private static final HashMap<String,String> PROS = new HashMap<String,String>();
		// ^ maps a resolution name to its "for" effect
	private static final HashMap<String,String> CONS = new HashMap<String,String>();
		// ^ maps a resolution name to its "against" effect
	private static final HashMap<String,String> EXTRAS = new HashMap<String,String>();
		// ^ maps a resolution name to its extra effect, if present
	
	// populate the resolutions from the xml file
	public static void addResolutionToDatabase(String name, String pro, String con, String extra) {
		PROS.put(name, pro);
		CONS.put(name, con);
		EXTRAS.put(name, extra == null ? "" : extra);
	}
	
	// access the pro of a resolution
	public static String getPro(String resolution) {
		synchronized(PROS) {
			return PROS.get(resolution);
		}
	}
	
	// access the con
	public static String getCon(String resolution) {
		synchronized(CONS) {
			return CONS.get(resolution);
		}	}
	
	// access the extra
	public static String getExtra(String resolution) {
		synchronized(EXTRAS) {
			return EXTRAS.get(resolution);
		}
	}
	
	
	//      _     _           
	//  ___| |__ (_)_ __  ___ 
	// / __| '_ \| | '_ \/ __|
	// \__ \ | | | | |_) \__ \
	// |___/_| |_|_| .__/|___/
	//             |_|   
	
	public static final int FIGHTER = 0;
	public static final int DESTROYER = 1;
	public static final int CRUISER = 2;
	public static final int DREADNOUGHT = 3;
	public static final int WAR_SUN = 4;
	// keep codes and array synchronized!
	private static final String[] SHIP_NAMES = {"Fighter", "Destroyer", "Cruiser", "Dreadnought", "War Sun"};
	
	public static final int NUM_SHIPS = SHIP_NAMES.length;
	
	private static final int[] HIT_RATES = new int[NUM_SHIPS];
	private static final int[] DICE = new int[NUM_SHIPS];

	// returns a ship's name given its integer code
	public static String nameOfShip(int code) {
		return SHIP_NAMES[code];
	}
	
	// given a ship's name, find it's code
	public static int codeOfShip(String name) {
		for(int i=0; i<NUM_SHIPS; i++) {
			if(SHIP_NAMES[i].equals(name)) {
				return i;
			}
		}
		return -1;
	}
	
	// add a ship to the database
	public static void addShip(String name, int hit, int dice) {
		int index = Database.codeOfShip(name);
		HIT_RATES[index] = hit;
		DICE[index] = dice;
	}
	
	// access ship info
	public static int[] getBaseHitRates() {
		int[] output = new int[NUM_SHIPS];
		for(int i=0; i<NUM_SHIPS; i++) {
			output[i] = HIT_RATES[i];
		}
		return output;
	}
	
	public static int[] getBaseDiceRolled() {
		int[] output = new int[NUM_SHIPS];
		for(int i=0; i<NUM_SHIPS; i++) {
			output[i] = DICE[i];
		}
		return output;
	}

	
	
	
	
	
	
	
	
	
	
	// clear the database upon disconnecting
	public static void disconnection() {
		synchronized(PLANETS) {PLANETS.clear();}
		synchronized(SPACEDOCKS) {SPACEDOCKS.clear();}
		synchronized(PLANETS) {LOCALPLANETS.clear();} // intentional- LOCALPLANETS shares a lock with PLANETS
		for(int i=0; i<4; i++) {LOCAL_TECH_DISCOUNT[i] = 0;}
		synchronized(TECH_QUEUE) {TECH_QUEUE.clear();}
		synchronized(TECH_MAP) {TECH_MAP.clear();}
		synchronized(PERSONNEL_QUEUE) {PERSONNEL_QUEUE.clear();}
		synchronized(PERSONNEL_MAP) {PERSONNEL_MAP.clear();}
		ADVANCING = false;
		synchronized(STAGE_MAP) {STAGE_MAP.clear();}
		synchronized(COLORS) {COLORS.clear();}
		synchronized(RACES) {RACES.clear();}
		synchronized(PLAYERS) {PLAYERS.clear();}
		NAME = null;
	}
	
	
	// initialization	
	
	public static void addPlayer(Player player) {
		
		TECH_MAP.put(player.name, new TreeSet<String>(ComparatorFactory.generateTechnologyComparator()));
		PERSONNEL_MAP.put(player.name, new TreeSet<String>(ComparatorFactory.generatePersonnelComparator2()));
		STAGE_MAP.put(player.name, 0);
		COLORS.put(player.name, Color.rgb(player.red, player.green, player.blue));
		RACES.put(player.name, player.race);
		INDICES.put(player.name, PLAYERS.size());
		PLAYERS.add(player);

	}
	
	
	// hints used to paint the overlay for the map
	private static final HashMap<String, Integer> HS_HINTS = new HashMap<String, Integer>();
	public static final int SINGLE_PLANET = 0;
	public static final int TOP_LEFT = 1;
	public static final int BOTTOM_RIGHT = 2;
	
	public static void initialize() {

		for(int i=0; i<4; i++) {
			TECH[i] = new HashMap<String,Integer>();
			TECH_NAMES[i] = new ArrayList<String>();
		}
		for(int i=0; i<3; i++) {
			PERSONNEL_SET[i] = new TreeSet<String>(ComparatorFactory.generatePersonnelComparator());
		}

		// populate database from XML
		Tile.generateTiles();
		TechSAXHandler.generateTechs();
		PersonnelSAXHandler.generatePersonnel();
		ResolutionSAXHandler.generateResolutions();
		EmpireSAXHandler.generateStages();
		ShipSAXHandler.generateShips();
		
		// color neutral planets
		COLORS.put("none", Color.GRAY);
		
		// maps several home systems to generic hints
		HS_HINTS.put("Darien", SINGLE_PLANET);
		HS_HINTS.put("Muaat", SINGLE_PLANET);
		HS_HINTS.put("Jord", SINGLE_PLANET);
		HS_HINTS.put("[0.0.0]", SINGLE_PLANET);
		HS_HINTS.put("Moll Primus", SINGLE_PLANET);
		HS_HINTS.put("Winnu", SINGLE_PLANET);
		
		HS_HINTS.put("Arc Prime", TOP_LEFT);
		HS_HINTS.put("Wren Terra", BOTTOM_RIGHT);
		
		HS_HINTS.put("Maaluuk", TOP_LEFT);
		HS_HINTS.put("Druaa", BOTTOM_RIGHT);
		
		HS_HINTS.put("Tren'lak", TOP_LEFT);
		HS_HINTS.put("Quinarra", BOTTOM_RIGHT);
		
		HS_HINTS.put("Nar", TOP_LEFT);
		HS_HINTS.put("Jol", BOTTOM_RIGHT);
		
		HS_HINTS.put("Archon Ren", TOP_LEFT);
		HS_HINTS.put("Archon Tau", BOTTOM_RIGHT);
		
		HS_HINTS.put("Retillion", TOP_LEFT);
		HS_HINTS.put("Shalloq", BOTTOM_RIGHT);
		

	}
	
	public static int getHints(String planetName) {
		synchronized(HS_HINTS) {
			if(HS_HINTS.containsKey(planetName))
				return HS_HINTS.get(planetName);
			return -1;
		}
	}
	

	
}
