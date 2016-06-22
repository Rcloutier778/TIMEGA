package sandbox_client;

/**
 * Records all relevant game information client-side. Updated by information sent from the server. All data structures are
 * private, intended only to be acquired via accessors.
 * 
 * @author dmayans
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeSet;

import server.Player;
import javafx.scene.paint.Color;


public class Database {
	
	// names
	private static final ArrayList<Player> PLAYERS = new ArrayList<Player>();
	private static final HashMap<String,Integer> INDICES = new HashMap<String,Integer>();
	
	// Holds the array of all 60 tiles
	private static final ArrayList<Tile> TILES = new ArrayList<Tile>();
	
	// Maps planet names to their owners and space dock status
	private static final HashMap<String,String> PLANETS = new HashMap<String,String>();
	private static final HashMap<String,Boolean> SPACEDOCKS = new HashMap<String,Boolean>();
	// holds all planets owned by current client- USES SAME LOCK AS PLANETS
	private static final TreeSet<String> LOCALPLANETS = new TreeSet<String>();
	private static final int[] LOCAL_TECH = {0,0,0,0};
	
	// Map planet names to their influence and resource values and tech specs
	private static final HashMap<String,Integer> RESOURCES = new HashMap<String,Integer>();
	private static final HashMap<String,Integer> INFLUENCE = new HashMap<String,Integer>();
	@SuppressWarnings("unchecked")
	private static final HashMap<String,Integer>[] TECH = (HashMap<String,Integer>[]) new HashMap[4];
	public static final int RED = 0, BLUE = 1, GREEN = 2, YELLOW = 3;
	
	// tech descriptions, queue, and maps
	// TECH_QUEUE SHARES A LOCK WITH TECH_MAP (use TECH_MAP's implicit lock);
	private static final HashMap<String,String> DESCRIPTIONS = new HashMap<String,String>();
	private static final LinkedList<String> TECH_QUEUE = new LinkedList<String>();
	private static final HashMap<String,HashSet<String>> TECH_MAP = new HashMap<String,HashSet<String>>();
	
	@SuppressWarnings("unchecked")
	private static final ArrayList<String>[] TECH_NAMES = new ArrayList[4];
	private static final HashMap<String,Integer> TECH_CONJUNCTIONS = new HashMap<String,Integer>();
	public static final int NO_PREREQS = 0, ONE_PREREQ = 1, AND = 2, OR = 3;
	private static final HashMap<String,String> PREREQUISITES = new HashMap<String,String>();
	
	private static final HashMap<String,Integer> TIERS = new HashMap<String,Integer>();
	
	// holds some policy tier info
	// PERSONNEL_QUEUE SHARES A LOCK WITH PERSONNEL_MAP (use PERSONNEL_MAP's implicit lock)
	private static final HashSet<String> PERSONNEL_QUEUE = new HashSet<String>();
	private static final HashMap<String,HashSet<String>> PERSONNEL_MAP = new HashMap<String,HashSet<String>>();
	
	private static boolean ADVANCING = false;
	private static HashMap<String,String> STAGE_MAP = new HashMap<String,String>();
		
	private static String NAME = null;
	
	private static final HashMap<String, Color> COLORS = new HashMap<String,Color>();
	private static final HashMap<String, String> RACES = new HashMap<String,String>();
	private static final HashMap<String, Integer> HS_HINTS = new HashMap<String, Integer>();
	
	// holds the resolution info
	private static final HashMap<String,String> PROS = new HashMap<String,String>();
	private static final HashMap<String,String> CONS = new HashMap<String,String>();
	private static final HashMap<String,String> EXTRAS = new HashMap<String,String>();
	
	public static void disconnection() {
		synchronized(PLANETS) {PLANETS.clear();}
		synchronized(SPACEDOCKS) {SPACEDOCKS.clear();}
		synchronized(PLANETS) {LOCALPLANETS.clear();} // intentional- LOCALPLANETS shares a lock with PLANETS
		for(int i=0; i<4; i++) {LOCAL_TECH[i] = 0;}
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
	
	// general data updates
	public static void addPlanetName(String name) {
		synchronized(PLANETS) {
			PLANETS.put(name, "none");
		}
		synchronized(SPACEDOCKS) {
			SPACEDOCKS.put(name, false);
		}
	}
	
	public static void name(String name) {
		Integer n;
		synchronized(PLANETS) {
			
			for(int i=0; i<4; i++) {
				LOCAL_TECH[i] = 0;
			}
			LOCALPLANETS.clear();
			NAME = name;
			
			for(Map.Entry<String, String> e : PLANETS.entrySet()) {
				
				if(e.getValue().equals(name)) {
					LOCALPLANETS.add(e.getKey());
					
					for(int i=0; i<4; i++) {
						if((n = TECH[i].get(e.getKey())) != null) {
							LOCAL_TECH[i] += n;
						}
					}
				}
				
			}
		}
	}
	
	// handles some tech information
	public static void enqueueTech(String tech) {
		synchronized(TECH_MAP) {
			TECH_QUEUE.addLast(tech);
		}
	}
	
	public static void removeFromTechQueue(String tech) {
		synchronized(TECH_MAP) {
			TECH_QUEUE.remove(tech);
		}
	}
	
	public static Iterable<String> getTechQueue() {
		synchronized(TECH_MAP) {
			return new LinkedList<String>(TECH_QUEUE);
		}
	}
	
	public static void clearTechQueue() {
		synchronized(TECH_MAP) {
			TECH_QUEUE.clear();
		}
	}
	
	public static boolean isTechEnqueued(String tech) {
		synchronized(TECH_MAP) {
			return TECH_QUEUE.contains(tech);
		}
	}
	
	public static boolean hasTech(String player, String tech) {
		synchronized(TECH_MAP) {
			return TECH_MAP.get(player).contains(tech);
		}
	}
	
	public static void research(String player, String tech) {
		synchronized(TECH_MAP) {
			TECH_MAP.get(player).add(tech);
			if(TECH_QUEUE.contains(tech)) {
				TECH_QUEUE.remove(tech);
			}
		}
	}
	
	public static void forget(String player, String tech) {
		synchronized(TECH_MAP) {
			TECH_MAP.get(player).remove(tech);
			if(TECH_QUEUE.contains(tech)) {
				TECH_QUEUE.remove(tech);
			}
		}
	}
	
	public static Iterable<String> technologyOf(String player) {
		synchronized(TECH_MAP) {
			return new HashSet<String>(TECH_MAP.get(player));
		}
	}
		
	
	// planet resource and influence and tech accessors and mutators
	public static void setResources(String planetName, int resources) {
		synchronized(RESOURCES) {
			RESOURCES.put(planetName, resources);
		}
	}
	
	public static int resourcesOf(String planetName) {
		synchronized(RESOURCES) {
			return RESOURCES.get(planetName);
		}
	}
	
	public static void setInfluence(String planetName, int influence) {
		synchronized(INFLUENCE) {
			INFLUENCE.put(planetName, influence);
		}
	}
	
	public static int influenceOf(String planetName) {
		synchronized(INFLUENCE) {
			return INFLUENCE.get(planetName);
		}
	}
	
	public static void addTech(String planetName, int techType) {
		synchronized(TECH) {
			Integer prev = TECH[techType].get(planetName);
			int newValue = prev == null ? 1 : prev + 1;
			TECH[techType].put(planetName, newValue);
		}
	}
	
	// some file stuff
	
	public static final String PATH_TO_NULL = "file:assets/Icons/null.png";
	
	public static String iconPath(boolean resource, int amount) {
		String output = "file:assets/Icons/";
		if(resource) {
			output += "resource_";
		} else {
			output += "influence_";
		}
		
		return output + Integer.toString(amount) + ".png";
	}
	
	// Handles TILES
	public static void addTile(Tile t) {
		synchronized(TILES) {
			TILES.add(t);
		}
	}
	
	public static Tile tile(int index) {
		synchronized(TILES) {
			return TILES.get(index);
		}
	}
	
	// Handles PLANETS
	public static String ownerOf(String planet) {
		synchronized(PLANETS) {
			return PLANETS.get(planet);
		}
	}
	
	public static void updatePlanet(String planetName, String newOwner) {
		Integer n;
		synchronized(PLANETS) {
			
			PLANETS.put(planetName, newOwner);
			
			if(LOCALPLANETS.contains(planetName)) {
				LOCALPLANETS.remove(planetName);
				
				for(int i=0; i<4; i++) {
					if((n = TECH[i].get(planetName)) != null) {
						LOCAL_TECH[i] -= n;
					}
				}
				
			} if(newOwner.equals(NAME)) {
				LOCALPLANETS.add(planetName);
				
				for(int i=0; i<4; i++) {
					if((n = TECH[i].get(planetName)) != null) {
						LOCAL_TECH[i] += n;
					}
				}
			}
		}
	}
	
	public static Iterable<String> getLocalPlanets() {
		synchronized(PLANETS) {
			return new TreeSet<String>(LOCALPLANETS);
		}
	}
	
	public static boolean isLocalPlanet(String planet) {
		synchronized(PLANETS) {
			return LOCALPLANETS.contains(planet);
		}
	}
	
	public static int localTech(int index) {
		synchronized(PLANETS) {
			return LOCAL_TECH[index];
		}
	}
	
	public static String descriptionOf(String tech) {
		synchronized(DESCRIPTIONS) {
			return DESCRIPTIONS.get(tech);
		}
	}

	// holds some trait information
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
		synchronized(STAGE_MAP) {
			String sequence = STAGE_MAP.get(player);
			for(int i=0; i<sequence.length(); i++) {
				if(i == 2 || i > 3) {
					// do nothing
				} else if(Integer.toString(color).charAt(0) == sequence.charAt(i)) {
					output++;
				}
			}
		}
		return output;
	}
	
	public static void localHirePerson(String person) {
		synchronized(PERSONNEL_MAP) {
			if(PERSONNEL_MAP.get(NAME).contains(person)) {
				PERSONNEL_QUEUE.remove(person);
			} else {
				PERSONNEL_QUEUE.add(person);
			}
		}
	}
	
	public static void localReleasePerson(String person) {
		synchronized(PERSONNEL_MAP) {
			if(PERSONNEL_MAP.get(NAME).contains(person)) {
				PERSONNEL_QUEUE.add(person);
			} else {
				PERSONNEL_QUEUE.remove(person);
			}
		}
	}
	
	public static Iterable<String> getPersonnelQueue() {
		synchronized(PERSONNEL_MAP) {
			return new HashSet<String>(PERSONNEL_QUEUE);
		}
	}
	
	public static void clearPersonnelQueue() {
		synchronized(PERSONNEL_MAP) {
			PERSONNEL_QUEUE.clear();
		}
	}
	
	public static boolean isPersonEnqueued(String person) {
		synchronized(PERSONNEL_MAP) {
			return PERSONNEL_QUEUE.contains(person);
		}
	}
	
	public static void hire(String player, String person) {
		synchronized(PERSONNEL_MAP) {
			PERSONNEL_MAP.get(player).add(person);
			if(PERSONNEL_QUEUE.contains(person)) {
				PERSONNEL_QUEUE.remove(person);
			}
		}
	}
	
	public static void release(String player, String person) {
		synchronized(PERSONNEL_MAP) {
			PERSONNEL_MAP.get(player).remove(person);
			if(PERSONNEL_QUEUE.contains(person)) {
				PERSONNEL_QUEUE.remove(person);
			}
		}
	}
	
	public static boolean hasPerson(String player, String person) {
		synchronized(PERSONNEL_MAP) {
			return PERSONNEL_MAP.get(player).contains(person);
		}
	}
	
	public static Iterable<String> personnelOf(String player) {
		synchronized(PERSONNEL_MAP) {
			return new HashSet<String>(PERSONNEL_MAP.get(player));
		}
	}
	
	public static boolean localHasPerson(String player, String person) {
		synchronized(PERSONNEL_MAP) {
			return PERSONNEL_MAP.get(player).contains(person) ^ PERSONNEL_QUEUE.contains(person);
		}
	}
	
	// Handles SPACEDOCKS
	public static boolean hasSD(String planet) {
		synchronized(SPACEDOCKS) {
			return SPACEDOCKS.get(planet);
		}
	}
	
	public static void updateSD(String planetName, boolean sdock) {
		synchronized(SPACEDOCKS) {
			SPACEDOCKS.put(planetName, sdock);
		}
	}
	
	// Handles advancing
	public static void setAdvancing(boolean b) {
		ADVANCING = b;
	}
	
	public static boolean isAdvancing() {
		return ADVANCING;
	}
	
	public static void advancePlayer(String player, String color) {
		synchronized(STAGE_MAP) {
			String prevSequence = STAGE_MAP.get(player);
			STAGE_MAP.put(player, prevSequence + color);
		}
	}
	
	public static int empireStageOf(String player) {
		synchronized(STAGE_MAP) {
			return STAGE_MAP.get(player).length();
		}
	}
	
	public static String empireSequence(String player) {
		synchronized(STAGE_MAP) {
			return STAGE_MAP.get(player);
		}
	}
	
	// player info
	public static int numPlayers() {
		synchronized(PLAYERS) {
			return PLAYERS.size();
		}
	}
	
	public static Player getPlayer(int i) {
		synchronized(PLAYERS) {
			return PLAYERS.get(i);
		}
	}
		
	
	
	
	
	
	
	// initialization	
	
	public static final int SINGLE_PLANET = 0;
	public static final int TOP_LEFT = 1;
	public static final int BOTTOM_RIGHT = 2;
	
	public static void addPlayer(Player player) {
		
		TECH_MAP.put(player.name, new HashSet<String>());
		PERSONNEL_MAP.put(player.name, new HashSet<String>());
		STAGE_MAP.put(player.name, "");
		COLORS.put(player.name, Color.rgb(player.red, player.green, player.blue));
		RACES.put(player.name, player.race);
		INDICES.put(player.name, PLAYERS.size());
		PLAYERS.add(player);

	}
	
	public static int indexOf(String player) {
		synchronized(INDICES) {
			return INDICES.get(player);
		}
	}
	
	public static void initialize() {
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
		
		for(int i=0; i<4; i++) {
			TECH[i] = new HashMap<String,Integer>();
			TECH_NAMES[i] = new ArrayList<String>();
		}
		
		// let's add some names and prereqs
		TECH_NAMES[RED].add("Hylar V Assault Laser");
			TECH_CONJUNCTIONS.put("Hylar V Assault Laser", NO_PREREQS);
		TECH_NAMES[RED].add("Ion Cannons");
			TECH_CONJUNCTIONS.put("Ion Cannons", ONE_PREREQ);
			PREREQUISITES.put("Ion Cannons", "Hylar V Assault Laser");
		TECH_NAMES[RED].add("ADT");
			TECH_CONJUNCTIONS.put("ADT", ONE_PREREQ);
			PREREQUISITES.put("ADT", "Hylar V Assault Laser");
		TECH_NAMES[RED].add("Hyper Metabolism");
			TECH_CONJUNCTIONS.put("Hyper Metabolism", OR);
			PREREQUISITES.put("Hyper Metabolism", "Hylar V Assault Laser&Cybernetics");
		TECH_NAMES[RED].add("Magen Defence Grid");
			TECH_CONJUNCTIONS.put("Magen Defence Grid", AND);
			PREREQUISITES.put("Magen Defence Grid", "Ion Cannons&Enviro Compensator");
		TECH_NAMES[RED].add("Auxiliary Drones");
			TECH_CONJUNCTIONS.put("Auxiliary Drones", ONE_PREREQ);
			PREREQUISITES.put("Auxiliary Drones", "Magen Defence Grid");
		TECH_NAMES[RED].add("Assault Cannon");
			TECH_CONJUNCTIONS.put("Assault Cannon", AND);
			PREREQUISITES.put("Assault Cannon", "Magen Defence Grid&Cybernetics");
		TECH_NAMES[RED].add("Deep Space Cannon");
			TECH_CONJUNCTIONS.put("Deep Space Cannon", AND);
			PREREQUISITES.put("Deep Space Cannon", "Ion Cannons&Fighter Bays");
		
		TECH_NAMES[BLUE].add("Antimass Deflectors");
			TECH_CONJUNCTIONS.put("Antimass Deflectors", NO_PREREQS);
		TECH_NAMES[BLUE].add("Striker Fleets");
			TECH_CONJUNCTIONS.put("Striker Fleets", ONE_PREREQ);
			PREREQUISITES.put("Striker Fleets", "Antimass Deflectors");
		TECH_NAMES[BLUE].add("XRD Transporters");
			TECH_CONJUNCTIONS.put("XRD Transporters", ONE_PREREQ);
			PREREQUISITES.put("XRD Transporters", "Antimass Deflectors");
		TECH_NAMES[BLUE].add("Fighter Bays");
			TECH_CONJUNCTIONS.put("Fighter Bays", AND);
			PREREQUISITES.put("Fighter Bays", "XRD Transporters&Hylar V Assault Laser");
		TECH_NAMES[BLUE].add("Type IV Drive");
			TECH_CONJUNCTIONS.put("Type IV Drive", AND);
			PREREQUISITES.put("Type IV Drive", "XRD Transporters&Neural Computing");
		TECH_NAMES[BLUE].add("Advanced Fighters"); 
			TECH_CONJUNCTIONS.put("Advanced Fighters", ONE_PREREQ);
			PREREQUISITES.put("Advanced Fighters", "Type IV Drive");
		TECH_NAMES[BLUE].add("Fleet Logistics");
			TECH_CONJUNCTIONS.put("Fleet Logistics", OR);
			PREREQUISITES.put("Fleet Logistics", "Arkan Iridium&Integrated Economy");
		TECH_NAMES[BLUE].add("Wave Deflectors");
			TECH_CONJUNCTIONS.put("Wave Deflectors", AND);
			PREREQUISITES.put("Wave Deflectors", "Striker Fleets&Magen Defence Grid");
		
		TECH_NAMES[GREEN].add("Stasis Capsules");
			TECH_CONJUNCTIONS.put("Stasis Capsules", OR);
			PREREQUISITES.put("Stasis Capsules", "Enviro Compensator&Antimass Deflectors");
		TECH_NAMES[GREEN].add("Hydroponics");
			TECH_CONJUNCTIONS.put("Hydroponics", AND);
			PREREQUISITES.put("Hydroponics", "Stasis Capsules&Enviro Compensator");
		TECH_NAMES[GREEN].add("Cybernetics");
			TECH_CONJUNCTIONS.put("Cybernetics", OR);
			PREREQUISITES.put("Cybernetics", "Stasis Capsules&Antimass Deflectors");
		TECH_NAMES[GREEN].add("Biomechanical Circuits");
			TECH_CONJUNCTIONS.put("Biomechanical Circuits", ONE_PREREQ);
			PREREQUISITES.put("Biomechanical Circuits", "Cybernetics");
		TECH_NAMES[GREEN].add("Xeno Psychology");
			TECH_CONJUNCTIONS.put("Xeno Psychology", ONE_PREREQ);
			PREREQUISITES.put("Xeno Psychology", "Cybernetics");
		TECH_NAMES[GREEN].add("Neural Computing");
			TECH_CONJUNCTIONS.put("Neural Computing", ONE_PREREQ);
			PREREQUISITES.put("Neural Computing", "Xeno Psychology");
		TECH_NAMES[GREEN].add("Biosphere Influence");
			TECH_CONJUNCTIONS.put("Biosphere Influence", OR);
			PREREQUISITES.put("Biosphere Influence", "Hydroponics&Xeno Psychology");
		TECH_NAMES[GREEN].add("Organ Printing");
			TECH_CONJUNCTIONS.put("Organ Printing", ONE_PREREQ);
			PREREQUISITES.put("Organ Printing", "Assault Cannon");
		
		TECH_NAMES[YELLOW].add("Enviro Compensator");
			TECH_CONJUNCTIONS.put("Enviro Compensator", NO_PREREQS);
		TECH_NAMES[YELLOW].add("Transfabrication");
			TECH_CONJUNCTIONS.put("Transfabrication", ONE_PREREQ);
			PREREQUISITES.put("Transfabrication", "Enviro Compensator");
		TECH_NAMES[YELLOW].add("Sarween Tools");
			TECH_CONJUNCTIONS.put("Sarween Tools", ONE_PREREQ);
			PREREQUISITES.put("Sarween Tools", "Enviro Compensator");
		TECH_NAMES[YELLOW].add("Duranium Armor");
			TECH_CONJUNCTIONS.put("Duranium Armor", AND);
			PREREQUISITES.put("Duranium Armor", "Transfabrication&ADT");
		TECH_NAMES[YELLOW].add("War Sun");
			TECH_CONJUNCTIONS.put("War Sun", AND);
			PREREQUISITES.put("War Sun", "Sarween Tools&Ion Cannons");
		TECH_NAMES[YELLOW].add("Arkan Iridium");
			TECH_CONJUNCTIONS.put("Arkan Iridium", AND);
			PREREQUISITES.put("Arkan Iridium", "Cybernetics&Sarween Tools");
		TECH_NAMES[YELLOW].add("Space Gates");
			TECH_CONJUNCTIONS.put("Space Gates", AND);
			PREREQUISITES.put("Space Gates", "Sarween Tools&XRD Transporters");
		TECH_NAMES[YELLOW].add("Integrated Economy");
			TECH_CONJUNCTIONS.put("Integrated Economy", AND);
			PREREQUISITES.put("Integrated Economy", "Ion Cannons&Cybernetics");

		
		// and now add some tech descriptions
		DESCRIPTIONS.put("Hylar V Assault Laser", "Each of your cruisers and destroyers receives +1 on all combat rolls.");
		DESCRIPTIONS.put("Ion Cannons", "Each of your dreadnoughts receives +2 on all combat rolls.");
		DESCRIPTIONS.put("ADT", "Each of your destroyers receives +1 on anti-fighter barrage, further increased by one for every fifth enemy fighter.");
		DESCRIPTIONS.put("Hyper Metabolism", "All tier i personnel are unlocked.");
		DESCRIPTIONS.put("Magen Defence Grid", "You may now build spaceborne artillery systems. Your dreadnoughts have a spaceborne artillery system capacity of one.");
		DESCRIPTIONS.put("Auxiliary Drones", "Your fleets that contain at least one dreadnought roll an additional combat die at 9+. This die benefits from all modifiers to dreadnought combat dice.");
		DESCRIPTIONS.put("Assault Cannon", "Up to two of your cruisers may take one pre-combat shot each at +1.");
		DESCRIPTIONS.put("Deep Space Cannon", "During your turns, dreadnoughts in adjacent systems may immediately take two shots. These hits will cause double casualties against fighters.");

		DESCRIPTIONS.put("Antimass Deflectors", "Your ships may move through asteroid field and ion storm systems. Your ships suffer no movement penalty when leaving nebulae.");
		DESCRIPTIONS.put("Striker Fleets", "Your destroyers may now move through nebulae. When moving through a hazard, destroyers receive +1 on all combat rolls.");
		DESCRIPTIONS.put("XRD Transporters", "Your carriers and dreadnoughts receive +1 movement.");
		DESCRIPTIONS.put("Fighter Bays", "Your dreadnoughts now have a fighter capacity of three.");
		DESCRIPTIONS.put("Type IV Drive", "Your cruisers and flagship now receive +1 movement.");
		DESCRIPTIONS.put("Advanced Fighters", "Your fighters may now move independently with a movement rate of 2 and receive +1 on all combat rolls.");
		DESCRIPTIONS.put("Fleet Logistics", "Once per round, you may flip one of your command counters on the board. Your ships may leave that system, but you may not activate it again for the remainder of the round.");
		DESCRIPTIONS.put("Wave Deflectors", "Your ships may now pass through systems containing enemy fleets and continue their movement to the activated system.");		
		
		DESCRIPTIONS.put("Stasis Capsules", "Your cruisers and dreadnoughts may now carry one ground force unit each.");
		DESCRIPTIONS.put("Hydroponics", "Your fleet supply is increased by one in all systems that don't contain any of your capital ships.");
		DESCRIPTIONS.put("Cybernetics", "Each of your fighters receives +1 on all combat rolls.");
		DESCRIPTIONS.put("Biomechanical Circuits", "Your command pool is permanently increased by one.");
		DESCRIPTIONS.put("Xeno Psychology", "Receive two additional vote for each unique personnel you have.");
		DESCRIPTIONS.put("Neural Computing", "Once per development phase, you may spend six resources to research a random technology advance (for which you have the necessary prerequisites).");
		DESCRIPTIONS.put("Biosphere Influence", "Each of your planets without a space dock has a production capacity equal to its resource value and a fighter capacity equal to its influence value.");
		DESCRIPTIONS.put("Organ Printing", "Each of your dreadnoughts has a production capacity of one.");

		DESCRIPTIONS.put("Enviro Compensator", "The production capacity of your space docks is increased by one.");
		DESCRIPTIONS.put("Transfabrication", "Upon losing a dreadnought, immediately replace it with a destroyer. At the end of combat, if at least one such destroyer remains, you may replace one of them with a damaged dreadnought.");
		DESCRIPTIONS.put("Sarween Tools", "Whenever you produce units at any space dock, you now receive one additional resource with which to build units.");
		DESCRIPTIONS.put("Duranium Armor", "After taking casualties, you may repair one damaged ship.");
		DESCRIPTIONS.put("War Sun", "You may now produce war suns. War suns receive one bonus movement if they do not end their turn in a system containing enemy ships.");
		DESCRIPTIONS.put("Arkan Iridium", "The cost of your space docks is reduced by two. You may build at your space docks immediately after construction.");
		DESCRIPTIONS.put("Space Gates", "When leaving or moving through a system containing a friendly space dock towards a destination without enemy ships, friendly ships receive three bonus movement.");
		DESCRIPTIONS.put("Integrated Economy", "When building ships at a space dock, you may place the newly constructed units in friendly or empty adjacent systems.");

		// and some personnel descriptions
		DESCRIPTIONS.put("Fleet Control", "Increase your fleet supply by one in systems containing at least one of your space docks.");
		DESCRIPTIONS.put("Moneylender", "After building ships, you may exhaust any number of space docks to receive their resource value in destroyers in that system. When refreshing a space dock, exhaust its planet.");
		DESCRIPTIONS.put("Marauder", "Receive one mercenary in any friendly or empty system containing one of your units or planets.");
		DESCRIPTIONS.put("Cultist", "When you have three or more ships of the same type in a system, they each receive +1 on all normal combat rolls. This bonus is lost for the rest of the battle when one of them is taken as a casualty.");
		DESCRIPTIONS.put("Usurper", "Receive bonus votes equal to the number of capital ships you control in or adjacent to Mecatol Rex, up to a maximum of six.");
		DESCRIPTIONS.put("Advisor", "Increase your command pool by one. The first time you activate a system each round, you may activate it twice. Each of your ships in that system receives +1 during the first round of space combat.");
		DESCRIPTIONS.put("Conqueror", "Choose a type of ship (except fighters). When destroying ships of that type, set them aside. Return them at the end of the development phase.");
		
		DESCRIPTIONS.put("Astronomer", "Your carriers may collect deep space tokens. Upon collecting your third, receive one blue technology at no cost, ignoring all of its prerequisites. (Do not collect any more deep space tokens.)");
		DESCRIPTIONS.put("Lookout", "At the end of each battle phase, do this twice: you may move one destroyer or cruiser to an adjacent empty system.");
		DESCRIPTIONS.put("Cartographer", "Your ships may travel to the wormhole nexus.");
		DESCRIPTIONS.put("Scavenger", "When passing through an asteroid field, receive two free fighters.");
		DESCRIPTIONS.put("Admiral", "Each of your capital ships receives +1 movement if its destination does not have any enemy ships.");
		DESCRIPTIONS.put("Technician", "You may place an alpha or beta wormhole token on one of your space docks. Only you may use this wormhole.");
		DESCRIPTIONS.put("Champion", "During each round of each space battle in which you are participating, your non-fighter ships receive a combat bonus equal to the difference between your and your opponent's non-fighter ships (zero if not outnumbered).");
		
		DESCRIPTIONS.put("Envoy", "Immediately after the speaker votes, you may cast two free votes in the same way");
		DESCRIPTIONS.put("Salvager", "When scuttling non-fighter ships, receive one bonus resource each during the upcoming development phase.");
		DESCRIPTIONS.put("Engineer", "Your space docks have a minimum production capacity of four (before technology and racial abilities).");
		DESCRIPTIONS.put("Mechanic", "Each of your SASs may fire one pre-combat shot as if it were a cruiser.");
		DESCRIPTIONS.put("Chancellor", "Each round, the first time a system containing one of your ships is activated by another player, it must be activated twice.");
		DESCRIPTIONS.put("Explorer", "Each of your carriers rolls normal combat dice at 6+.");
		DESCRIPTIONS.put("Tactician", "During each round of a space battle, each of your ships receives +1 on combat rolls if there is no opposing ship of the same type.");
		
		// and finally some tiers
		TIERS.put("Fleet Control", 1);
		TIERS.put("Moneylender", 1);
		TIERS.put("Marauder", 2);
		TIERS.put("Cultist", 2);
		TIERS.put("Usurper", 3);
		TIERS.put("Advisor", 3);
		TIERS.put("Conqueror", 4);

		TIERS.put("Astronomer", 1);
		TIERS.put("Lookout", 1);
		TIERS.put("Cartographer", 2);
		TIERS.put("Scavenger", 2);
		TIERS.put("Admiral", 3);
		TIERS.put("Technician", 3);
		TIERS.put("Champion", 4);
		
		TIERS.put("Envoy", 1);
		TIERS.put("Salvager", 1);
		TIERS.put("Engineer", 2);
		TIERS.put("Mechanic", 2);
		TIERS.put("Chancellor", 3);
		TIERS.put("Explorer", 3);
		TIERS.put("Tactician", 4);

		PROS.put("Vote of No Confidence", "The player who put the most votes for this resolution becomes the speaker. No player may place a duplicate number of votes for this resolution.");
		PROS.put("Imperial Peace", "Mecatol Rex may not be invaded this round.");
		PROS.put("New Constitution", "Clear all resolutions in effect. Players that voted for this resolution may exhaust planets for more votes. Flip over two new resolutions and vote on them.");
		PROS.put("Closing the Wormholes", "No player may use wormholes.");
		PROS.put("Wormhole Reconstruction", "All systems with a wormhole are considered adjacent to all other systems with a wormhole.");
		PROS.put("Technology Tariffs", "The cost of all red technology is increased by two.");
		PROS.put("Research Grant", "All technology specialties apply to the first (and only the first) technology you research each round, regardless of color.");
		PROS.put("War Funding", "During the action phase, players may ignore fleet supply restrictions.");
		PROS.put("Ancient Artifact", "Roll a die. On 6+, all players in or adjacent to Mecatol Rex receive one free technology. On 5-, all units in or adjacent to Mecatol Rex are destroyed.");
		PROS.put("Holder of Mecatol Rex", "The player controlling Mecatol Rex may pick any law and discard it.");
		PROS.put("Humane Labor", "All space docks may produce at most three units each round.");
		PROS.put("Repeal", "The chosen law is discarded without effect.");
		PROS.put("Revote", "");
		PROS.put("Subsidized Industry", "All players receive one free space dock.");
		PROS.put("Arms Reduction", "Each player must destroy all but two of his dreadnoughts and all but three of his cruisers (all other ships are unaffected).");
		PROS.put("null", "");
		
		CONS.put("Vote of No Confidence", "All players who voted for this resolution must choose two planets. Those planets are not refreshed during this development phase.");
		CONS.put("Imperial Peace", "Mecatol Rex is the only planet that may be invaded this round.");
		CONS.put("New Constution", "All planets exhausted for votes this round are not refreshed.");
		CONS.put("Closing the Wormholes", "All fleets in wormhole systems are destroyed.");
		CONS.put("Wormhole Reconstruction", "All fleets in wormhole systems are destroyed.");
		CONS.put("Technology Tariffs", "Green technology may not be purchased for one round.");
		CONS.put("Research Grant", "Yellow technology may not be purchased for one round.");
		CONS.put("War Funding", "All fighters receive +1 on all combat rolls.");
		CONS.put("Ancient Artifact", "The Winnaran guards receive an extra dreadnought.");
		CONS.put("Holder of Mecatol Rex", "The player controlling Mecatol Rex may add a biohazard token to the planet. When invading Mecatol Rex, other players may consume one mechanized unit, one shock troop, or three ground forces to remove the biohazard token. Mecatol Rex may not be invaded until the token is removed.");
		CONS.put("Humane Labor", "No space docks may produce units for one round.");
		CONS.put("Repeal", "The speaker keeps the token this round, regardless of votes cast.");
		CONS.put("Revote", "");
		CONS.put("Subsidized Industry", "All space docks have their production capacity increased by one.");
		CONS.put("Arms Reduction", "Planets with red technology specialties are not refreshed this development phase.");
		CONS.put("null", "");
		
		EXTRAS.put("Vote of No Confidence", "After this agenda has been resolved, flip over and resolve another. Only the speaker may vote on this next agenda.");
		EXTRAS.put("Imperial Peace", "While this agenda is in effect, no player may advance his empire.");
		EXTRAS.put("New Constitution", "While this agenda is under deliberation, discard any other agenda that comes into play. This agenda may not be vetoed.");
		EXTRAS.put("Closing the Wormholes", "");
		EXTRAS.put("Wormhole Reconstruction", "");
		EXTRAS.put("Technology Tariffs", "");
		EXTRAS.put("Research Grant", "");
		EXTRAS.put("War Funding", "");
		EXTRAS.put("Ancient Artifact", "");
		EXTRAS.put("Holder of Mecatol Rex", "");
		EXTRAS.put("Humane Labor", "");
		EXTRAS.put("Repeal", "When this agenda is revealed, the speaker chooses an existing law.");
		EXTRAS.put("Revote", "When this agenda is revealed, the speaker chooses an existing law. Revote on that law instead.");
		EXTRAS.put("Subsidized Industry", "");
		EXTRAS.put("Arms Reduction", "");
		EXTRAS.put("null", "");
	}
	
	public static String getTechName(int color, int index) {
		synchronized(TECH_NAMES) {
			return TECH_NAMES[color].get(index);
		}
	}
	
	public static int conjunctionOfTech(String tech) {
		synchronized(TECH_CONJUNCTIONS) {
			return TECH_CONJUNCTIONS.get(tech);
		}
	}
	
	public static String prereqsOfTech(String tech) {
		synchronized(PREREQUISITES) {
			return PREREQUISITES.get(tech);
		}
	}
	
	public static int colorOfTech(String name) {
		for(int i=0; i<4; i++) {
			if(TECH_NAMES[i].contains(name)) {
				return i;
			}
		}
		
		return -1;
	}
	
	public static int tierOfPersonnel(String personnel) {
		synchronized(TIERS) {
			return TIERS.get(personnel);
		}
	}
	
	public static Color colorOfPlanet(String planetName) {
		synchronized(COLORS) {
			return COLORS.get(ownerOf(planetName));
		}
	}
	
	public static int getHints(String planetName) {
		synchronized(HS_HINTS) {
			if(HS_HINTS.containsKey(planetName))
				return HS_HINTS.get(planetName);
			return -1;
		}
	}
	
	public static String raceOf(String player) {
		synchronized(RACES) {
			return RACES.get(player);
		}
	}
	
	public static String getPro(String resolution) {
		synchronized(PROS) {
			return PROS.get(resolution);
		}
	}
	
	public static String getCon(String resolution) {
		synchronized(CONS) {
			return CONS.get(resolution);
		}	}
	
	public static String getExtra(String resolution) {
		synchronized(EXTRAS) {
			return EXTRAS.get(resolution);
		}
	}
	
}
