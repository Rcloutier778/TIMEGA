package server;

/**
 * Not encapsulated. Grab the information you need from the public information, but make sure the locks are held.
 * 
 * @author dmayans
 */

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import sandbox_client.Client;

public class ServerDatabase {
		
	// Holds the array of all 60 tiles
	public static final ArrayList<Tile> TILES = new ArrayList<Tile>();
	
	// Maps planet names to their owners and space dock status
	public static final HashMap<String,String> PLANETS = new HashMap<String,String>();
	public static final Lock PLANETS_LOCK = new ReentrantLock();

	public static final HashMap<String,Boolean> SPACEDOCKS = new HashMap<String,Boolean>();
	public static final Lock SPACEDOCKS_LOCK = new ReentrantLock();
	
	public static Player[] PLAYERS;

	public static final HashMap<String,HashSet<String>> TECH = new HashMap<String,HashSet<String>>();
	public static final Lock TECH_LOCK = new ReentrantLock();
	
	public static final HashMap<String,HashSet<String>> PERSONNEL = new HashMap<String,HashSet<String>>();
	public static final Lock PERSONNEL_LOCK = new ReentrantLock();
	
	public static final HashMap<String,String> EMPIRE_STAGE = new HashMap<String,String>();
	public static final Lock EMPIRE_LOCK = new ReentrantLock();
	
	
	public static final HashSet<String> TECH_SET = new HashSet<String>();
	public static final HashSet<String> PERSONNEL_SET = new HashSet<String>();
	public static final HashSet<String> RESOLUTION_SET = new HashSet<String>();
	
	
	public static final HashMap<String,String> TABS = new HashMap<String,String>();

	public static boolean hasName(String name) {
		for(Player n : ServerDatabase.PLAYERS) {
			if(name.equals(n.name)) {
				return true;
			}
		}
		
		return false;
	}
	
	public static void initialize(List<Player> players) {
		PLAYERS = new Player[players.size()];
		int i=0;
		for(Player p : players) {
			PLAYERS[i++] = p;
			TECH.put(p.name, new HashSet<String>());
			PERSONNEL.put(p.name, new HashSet<String>());
			EMPIRE_STAGE.put(p.name, "");
		}

		for(String tab : Client.TAB_NAMES) {
			if(tab.equals("Home")) continue;
			TABS.put(tab.toLowerCase(), tab);
		}
		
		RESOLUTION_SET.add("Vote of No Confidence");
		RESOLUTION_SET.add("Imperial Peace");
		RESOLUTION_SET.add("New Constitution");
		RESOLUTION_SET.add("Closing the Wormholes");
		RESOLUTION_SET.add("Technology Tariffs");
		RESOLUTION_SET.add("Research Grant");
		RESOLUTION_SET.add("War Funding");
		RESOLUTION_SET.add("Ancient Artifact");
		RESOLUTION_SET.add("Holder of Mecatol Rex");
		RESOLUTION_SET.add("Humane Labor");
		RESOLUTION_SET.add("Repeal");
		RESOLUTION_SET.add("Revote");
		RESOLUTION_SET.add("Subsidized Industry");
		RESOLUTION_SET.add("Arms Reduction");
		RESOLUTION_SET.add("null");
		
		// Since Mallice and Creuss don't actually exist on the map, we kind of have to add them in by hard code :(
		PLANETS.put("Mallice", "none");
		SPACEDOCKS.put("Mallice", false);
		// PLANETS.put("Creuss", "none");
		// SPACEDOCKS.put("Creuss", true);

	}
	
	public static void placeTech(Collection<String> tech) {
		for(String t : tech) {
			TECH_SET.add(t);
		}
	}
	
	public static void placePersonnel(Collection<String> personnel) {
		for(String p : personnel) {
			PERSONNEL_SET.add(p);
		}
	}
	
	
}
