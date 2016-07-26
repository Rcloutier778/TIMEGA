package server;

/**
 * Initializes program, handles the client list, broadcasts information to the client list, reads and responds to stdin
 * 
 * @author dmayans
 */

import sandbox_client.Protocol;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
//import server.CommandMap;

public class Main {
	
	// some colors aren't used
	private static final int BLACK = 30;
	private static final int RED = 31;
	private static final int GREEN = 32;
//	private static final int YELLOW = 33;
	private static final int BLUE = 34;
//	private static final int MAGENTA = 35;
//	private static final int CYAN = 36;
	
	public static final int ERROR = RED;
	public static final int STDIN = GREEN;
	public static final int SERVEROUT = BLACK;
	public static final int CLIENTOUT = BLUE;
	
	private static void setOutColor(int color) {
		// this isn't hacky at all, I swear
		System.out.print((char)27 + "[" + color + "m");
	}
	
	// common method used pretty much everywhere to pring colorful text to stdout
	public static void writeColortext(String s, int color) {
		setOutColor(color);
		System.out.println(s);
		setOutColor(STDIN);
	}
	
	// thread list
	private LinkedList<ClientThread> _clients = new LinkedList<ClientThread>();
	private boolean _running = true;
	
	// handles user input
	private CommandMap _map = new CommandMap(this);
	
	// map information to spit back to clients
	private static int[] _mapdata = null;
	private static String _mapname = "<unknown>";
		
	public Main() {
		
		// open up stdin and clientlisteners
		BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
		
		ClientListener listener = new ClientListener(this);
		new Thread(listener).start();
				
		// parse stdin
		while(true) {
			try {
				String text = stdin.readLine();
				// exit- closes all threads, then closes the program
				if(text == null || text.equals("exit")) {
					listener.cancel();
					this.cancel();
					setOutColor(BLACK);
					break;
				}
				// otherwise, remove all trailing whitespace and pass on the text to the CommandMap to handle it
				text = text.trim();
				if(text.length() == 0) {
					// do nothing
				} else {
					_map.parse(text);
				}
			} catch (IOException e) {
				// not good...
				e.printStackTrace();
			}
		}
		
	}
	
	// Client list methods
	// to keep the list thread-safe, all access is synchronized
	
	// add a new client
	public boolean addClient(ClientThread c) {
		synchronized(_clients) {
			if(_running) {
				_clients.add(c);
			}
		}
		
		return _running;
	}
	
	// pull a client from the list
	public void removeClient(ClientThread c) {
		synchronized(_clients) {
			_clients.remove(c);
		}
	}
	
	// kill all client threads
	private void cancel() {
		synchronized(_clients) {
			_running = false;

			for(ClientThread c : _clients) {
				c.close();
			}
		}
	}
	
	// broadcast a single protocol
	public void broadcast(int protocol) {
		synchronized(_clients) {
			for(ClientThread c : _clients) {
				c.write(protocol);
			}
		}
	}
	
	// broadcast a protocol/message pair
	public void broadcast(int protocol, String message) {
		synchronized(_clients) {
			for(ClientThread c : _clients) {
				c.write(protocol, message);
			}
		}
	}
	
	// EXPANDED PROTOCOLS: lots of them, not a lot of good ways of condensing them

	//Reloads server data from log file produced on shutdown.
	public void broadcastReload(String filename){
		try(BufferedReader br = new BufferedReader(new FileReader(filename))){
			//Planets, Space Docks, Tech, Personnel, Empire Stage, Resolution, End
			boolean[] passed = {false, false, false, false, false, false, false, false, false};
			ArrayList<String> playernames = new ArrayList<>();
			for(int i = 0; i<ServerDatabase.PLAYERS.length;i++) {
				playernames.add(ServerDatabase.PLAYERS[i].name);
			}
			String pname = "none";
			for(String line; (line  = br.readLine()) != null;){
				String[] splitline = line.split(" ");
				if(playernames.contains(line)){
					pname = line;
					continue;
				}
				if(line.equals("Planets")){
					passed[0] = true;
					continue;
				}
				if(line.equals("Spacedocks")){
					passed[1] = true;
					continue;
				}
				if(line.equals("Tech")){
					passed[2] = true;
					continue;
				}
				if(line.equals("Personnel")){
					passed[3] = true;
					continue;
				}
				if(line.equals("Empire stage")){
					passed[4] = true;
					continue;
				}
				if(line.equals("Resolutions")){
					passed[5] = true;
					continue;
				}
				if(line.equals("Current Resolutions")){
					passed[6] = true;
					continue;
				}
				if(line.equals("Total Votes")){
					passed[7] = true;
					continue;
				}
				if(line.equals("EndFile")){
					passed[8] = true;
					break;
				}
				//Planets: Owner, Planet name
				if(passed[0] && !passed[1]){
					_map.parse("chown " + line);
				}
				//Spacedocks: Planet_name, boolean
				if(passed[1] && !passed[2]){
					if(splitline.length == 2) {
						broadcastSD(splitline[0], Boolean.parseBoolean(splitline[1]), ServerDatabase.PLANETS.get(splitline[0]) + " ");
					}else if(splitline.length == 3){
						broadcastSD(splitline[0] + " " + splitline[1], Boolean.parseBoolean(splitline[2]), ServerDatabase.PLANETS.get(splitline[0] + " " + splitline[1]));
					}
				}
				//Tech
				//Name
				//Tech
				//Tech
				//Name2
				//Tech ...
				if(passed[2] && !passed[3]){
					_map.parse("research " + pname + " " + line);
				}
				//Personnel
				if(passed[3] && !passed[4]){
					_map.parse("hire " + pname + " " + line);
				}
				//Empire Stage
				//Starts at Coalition
				if(passed[4] && !passed[5]){
					if(splitline.length > 1) {
						for(int i = 0; i < Integer.parseInt(splitline[1]); i++){
							broadcastAdvance(splitline[0], "[" + splitline[0] + "] ");
						}
					}
				}
				//Resolution
				//
				if(passed[5] && !passed[6]){
					_currentResolutions[0] = splitline[0].replace("_"," ");
					_currentResolutions[1] = splitline[2].replace("_"," ");
					//result1, result2
					String[] ret = {splitline[1],splitline[3]};
					broadcastResolutionResult(ret);
				}
				//Current Resolutions
				if(passed[6] && !passed[7]){
					_currentResolutions[0] = splitline[0].replace("_", " ");
					_currentResolutions[1] = splitline[1].replace("_", " ");
				}
				//Total Votes
				if(passed[7] && !passed[8]){
					ServerDatabase.TOTAL_VOTES_LOCK.lock();
					ServerDatabase.TOTAL_VOTES.put(splitline[0],Integer.parseInt(splitline[1]));
				}

			}
		}catch (IOException e){
			System.out.println("Error reading file to reload from");
		}
		writeColortext("Reload Successful", 255);
		//this.broadcast(Protocol.RELOAD, );
	}

	// updates database for planet changing owner, prints info to stdout, and broadcasts info to all clients
	public void broadcastChown(String planetName, String newOwner, String clientName, int color) {
		ServerDatabase.PLANETS_LOCK.lock();
		String oldOwner = ServerDatabase.PLANETS.get(planetName);
		if(oldOwner.equals("none")) {
			writeColortext(clientName + "captured neutral planet " + planetName, color);
		} else if(newOwner.equals("none")) {
			writeColortext(clientName + "released " + planetName, color);
		} else {
			writeColortext(clientName + "captured " + planetName + " from " + oldOwner, color);
		}
		ServerDatabase.PLANETS.put(planetName, newOwner);
		this.broadcast(Protocol.PLANET_CHOWN, planetName + "\n" + newOwner + "\n");
		ServerDatabase.PLANETS_LOCK.unlock();
	}
	
	// same but for new and destroyed space docks
	public void broadcastSD(String planetName, boolean sdock, String clientName) {
		ServerDatabase.SPACEDOCKS_LOCK.lock();
		ServerDatabase.SPACEDOCKS.put(planetName, sdock);
		if(sdock) {
			writeColortext(clientName + "new space dock on " + planetName, CLIENTOUT);
			this.broadcast(Protocol.NEW_SDOCK, planetName + "\n");
		} else {
			writeColortext(clientName + "space dock removed from " + planetName, CLIENTOUT);
			this.broadcast(Protocol.REMOVE_SDOCK, planetName + "\n");
		}
		ServerDatabase.SPACEDOCKS_LOCK.unlock();
	}
	
	// same but for tech
	public void broadcastTech(String player, String tech, String clientName, int color) {
		ServerDatabase.TECH_LOCK.lock();
		ServerDatabase.TECH.get(player).add(tech);
		this.broadcast(Protocol.SEND_TECH, player + "\n" + tech + "\n");
		writeColortext(clientName + "researched " + tech, color);
		ServerDatabase.TECH_LOCK.unlock();
	}
	
	public void broadcastForget(String player, String tech, String clientName, int color) {
		ServerDatabase.TECH_LOCK.lock();
		ServerDatabase.TECH.get(player).remove(tech);
		this.broadcast(Protocol.REMOVE_TECH, player + "\n" + tech + "\n");
		writeColortext(clientName + "forgot " + tech, color);
		ServerDatabase.TECH_LOCK.unlock();
	}
	
	// same but for personnel
	public void broadcastHire(String player, String person, String clientName, int color) {
		ServerDatabase.PERSONNEL_LOCK.lock();
		ServerDatabase.PERSONNEL.get(player).add(person);
		this.broadcast(Protocol.SEND_PERSON, player + "\n" + person + "\n");
		writeColortext(clientName + "hired " + person, color);
		ServerDatabase.PERSONNEL_LOCK.unlock();
	}
	
	public void broadcastRelease(String player, String person, String clientName, int color) {
		ServerDatabase.PERSONNEL_LOCK.lock();
		ServerDatabase.PERSONNEL.get(player).remove(person);
		this.broadcast(Protocol.REMOVE_PERSON, player + "\n" + person + "\n");
		writeColortext(clientName + "released " + person, color);
		ServerDatabase.PERSONNEL_LOCK.unlock();
	}
	
	// same but for empire stage
	public void broadcastAdvance(String player, String clientName) {
		ServerDatabase.EMPIRE_LOCK.lock();
		int stage = ServerDatabase.EMPIRE_STAGE.get(player);
		ServerDatabase.EMPIRE_STAGE.put(player, stage + 1);
		this.broadcast(Protocol.ADVANCE, player + "\n");
		writeColortext(clientName + "advanced his empire.", CLIENTOUT);
		ServerDatabase.EMPIRE_LOCK.unlock();
	}

	private String[] _currentResolutions = new String[2];
	private String[] _repealResolution = new String[2];

	public void broadcastResolution(String res1, String res2, String rep1, String rep2) {
		_currentResolutions[0] = res1;
		_currentResolutions[1] = res2;
		_repealResolution[0] = rep1;
		_repealResolution[1] = rep2;

		ServerDatabase.CURRENT_RESOLUTIONS_LOCK.lock();
		ServerDatabase.CURRENT_RESOLUTIONS[0] = _currentResolutions[0];
		ServerDatabase.CURRENT_RESOLUTIONS[1] = _currentResolutions[1];
		ServerDatabase.CURRENT_RESOLUTIONS_LOCK.unlock();

		this.broadcast(Protocol.SEND_RESOLUTION, res1 + "\n" + res2 + "\n");
	}

	public void broadcastResolutionResult(String result[]) {
		ServerDatabase.RESOLUTION_LOCK.lock();
		for(int i=0; i<2; i++){
			if(_currentResolutions[i].equals("New Constitution") && result[i+2].equals("for")){
				ServerDatabase.PAST_RESOLUTION.clear();
				writeColortext("Enacted New Constitution", CLIENTOUT);
				ServerDatabase.RESOLUTION_LOCK.unlock();
				this.broadcast(Protocol.RESOLUTION_RESULT, _currentResolutions[i] + "\n" + result[i]);
				break;
			} else if(_currentResolutions[i].equals("Repeal") && result[i+2].equals("for")){
				ServerDatabase.PAST_RESOLUTION.remove(result[i + 2]);
				writeColortext( "Repealed " + result[i+2], CLIENTOUT);
				this.broadcast(Protocol.RESOLUTION_RESULT, _currentResolutions[i] + "\n" + result[i] + "\n" + _repealResolution[i]);
			} else if (_currentResolutions[i].equals("Revote")) {
				//Is never actually passed in, just vote on the agenda to be revoted
			} else if(result[i+2].equals("tie")){
				//do nothing
			} else {
				ServerDatabase.PAST_RESOLUTION.put(_currentResolutions[i], result[i]);
				writeColortext("Voted on resolution.", CLIENTOUT);
				this.broadcast(Protocol.RESOLUTION_RESULT, _currentResolutions[i] + "\n" + result[i]);
			}
		}
		//Current1, Result1, Current2, Result2, Repeal1, Repeal2
		ServerDatabase.RESOLUTION_LOCK.unlock();
	}

	public void broadcastVoteTally(String player, int resolution, int numFor, int numAgainst){
		ServerDatabase.VOTES_LOCK.lock();
		Integer res[] = {numFor,numAgainst};
		ServerDatabase.VOTES[resolution].put(player,res);
		ServerDatabase.VOTES_LOCK.unlock();

		ServerDatabase.TOTAL_VOTES_LOCK.lock();
		ServerDatabase.TOTAL_VOTES.put(player, ServerDatabase.TOTAL_VOTES.get(player) + numFor + numAgainst);
		ServerDatabase.TOTAL_VOTES_LOCK.unlock();
	}

	public void broadcastVote(){
		ServerDatabase.VOTES_LOCK.lock();
		Integer _for[] = {0,0};
		Integer _against[] = {0,0};
		System.out.println(ServerDatabase.PLAYERS.length);
		for(int i=0; i<2; i++) {
			for (int k = 0; k < ServerDatabase.PLAYERS.length; k++) {
				_for[i] += ServerDatabase.VOTES[i].get(ServerDatabase.PLAYERS[k].name)[0];
				_against[i] += ServerDatabase.VOTES[i].get(ServerDatabase.PLAYERS[k].name)[1];
			}
		}

		if(_for[0] > _against[0] && _for[1] > _against[1]){//for for
			this.broadcastResolutionResult(new String[]{"for", "for"});
		}else if(_for[0] > _against[0] && _for[1] < _against[1]) {//for against
			this.broadcastResolutionResult(new String[]{"for", "against"});
		}else if(_for[0] < _against[0] && _for[1] > _against[1]) {//against for
			this.broadcastResolutionResult(new String[]{"against", "for"});
		}else if(_for[0] < _against[0] && _for[1] < _against[1]) {//against against
			this.broadcastResolutionResult(new String[]{"against", "against"});
		}

		ServerDatabase.VOTES_LOCK.unlock();
	}
	
	// MAP INFO
	
	// Accessors used to send map name and data on request
	public int[] requestMap() {
		return _mapdata;
	}
	
	public String requestMapName() {
		return _mapname;
	}
	
	// TAB INFO
	
	// Accessors to send enabled tabs on request
	public boolean getEnabled(String tab) {
		return _map.getEnabled(tab);
	}
	
	public static void main(String[] args) {
		
		// server needs a game file to read- see comments on game file parsing to understand format
		if(args.length < 1) {
			System.err.println("Usage: server <game>");
			return;
		}
		
		// make sure that when the JVM crashes for one reason or another, the terminal isn't stuck printing green
	    Runtime.getRuntime().addShutdownHook(new Thread() {
	        @Override
	        public void run() {
	            //add method to cache data
				//dont need hook here
				//everything in server database that has locks needs to be cached
				//empire stage info stored is going to be changed at some point, probably int instead of string
				//
				BufferedWriter writer = null;
				try {
					//create a temporary file
					File logFile = new File("savefile" + ".txt");

					// This will output the full path where the file will be written to...
					System.out.println(logFile.getCanonicalPath());

					writer = new BufferedWriter(new FileWriter(logFile));

					//Write Planets
					//Planet_name Owner
					writer.write("Planets\n");
					ServerDatabase.PLANETS_LOCK.lock();
					for(String s : ServerDatabase.PLANETS.keySet()){
						writer.write(ServerDatabase.PLANETS.get(s) + " ");
						writer.write(s + "\n");
					}
					ServerDatabase.PLANETS_LOCK.unlock();

					//Spacedocks
					//Planet_name boolean
					writer.write("Spacedocks\n");
					ServerDatabase.SPACEDOCKS_LOCK.lock();
					for(String s : ServerDatabase.SPACEDOCKS.keySet()){
						writer.write(s + " ");
						writer.write(ServerDatabase.SPACEDOCKS.get(s) + "\n");
					}
					ServerDatabase.SPACEDOCKS_LOCK.unlock();

					//Tech
					//Player_name tech 1 ... tech n
					writer.write("Tech\n");
					ServerDatabase.TECH_LOCK.lock();
					for(String s : ServerDatabase.TECH.keySet()){
						writer.write(s + "\n");
						for(String t : ServerDatabase.TECH.get(s)){
							writer.write(t + "\n");
						}
					}
					ServerDatabase.TECH_LOCK.unlock();

					//Personnel
					//Player name, personnel
					writer.write("Personnel\n");
					ServerDatabase.PERSONNEL_LOCK.lock();
					for(String s : ServerDatabase.PERSONNEL.keySet()){
						writer.write(s + "\n");
						for(String t : ServerDatabase.PERSONNEL.get(s)){
							writer.write(t + "\n");
						}
					}
					ServerDatabase.PERSONNEL_LOCK.unlock();


					//Empire stage
					writer.write("Empire stage\n");
					ServerDatabase.EMPIRE_LOCK.lock();
					for(String s : ServerDatabase.EMPIRE_STAGE.keySet()){
						writer.write(s + " ");
						writer.write(ServerDatabase.EMPIRE_STAGE.get(s) + "\n");
					}
					ServerDatabase.EMPIRE_LOCK.unlock();

					//Resolutions
					//Resolution1, Result1, Resolution2, Result2
					writer.write("Resolutions\n");
					ServerDatabase.RESOLUTION_LOCK.lock();
					int place=0;
					for(String s: ServerDatabase.PAST_RESOLUTION.keySet()){
						writer.write(s.replace(" ","_") + " ");
						writer.write(ServerDatabase.PAST_RESOLUTION.get(s));
						if(place==0){
							writer.write(" ");
							place++;
						}else{
							writer.write("\n");
							place--;
						}
					}
					ServerDatabase.RESOLUTION_LOCK.unlock();

					//Current Resolution
					writer.write("Current Resolutions\n");
					ServerDatabase.CURRENT_RESOLUTIONS_LOCK.lock();
					writer.write(ServerDatabase.CURRENT_RESOLUTIONS[0].replace(" ", "_") + " " + ServerDatabase.CURRENT_RESOLUTIONS[1].replace(" ", "_") + "\n");
					ServerDatabase.CURRENT_RESOLUTIONS_LOCK.unlock();

					//Total Votes
					writer.write("Total Votes\n");
					ServerDatabase.TOTAL_VOTES_LOCK.lock();
					for(String s : ServerDatabase.TOTAL_VOTES.keySet()){
						writer.write(s + " " + ServerDatabase.TOTAL_VOTES.get(s) + "\n");
					}
					ServerDatabase.TOTAL_VOTES_LOCK.unlock();

					writer.write("EndFile");
					String timeLog = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(Calendar.getInstance().getTime());
					writer.write("\n" + timeLog);

				} catch (Exception e) {
					e.printStackTrace();
					System.out.println("Caching Error");
				} finally {
					try {
						// Close the writer regardless of what happens...
						writer.close();
					} catch (Exception e) {
						System.out.println("Error closing writer");
					}
				}
				setOutColor(BLACK);
	        }
	    });
		
	    // time to read the game file (stored as a text file in in assets/server/)
	    BufferedReader reader;
	    try {
			reader = new BufferedReader(new FileReader(sandbox_client.Main.PATH_TO_ASSETS + "server/" + args[0] + ".txt"));
		} catch (FileNotFoundException e) {
			writeColortext("file not found", Main.ERROR);
			return;
		}
	    
	    // GAME FILE FORMAT:
	    // <map name>
	    // <player name> <r> <g> <b> <race>
	    // <player name> <r> <g> <b> <race>
	    // ...
	    // <player name> <r> <g> <b> <race>
	    
	    // try to read the mapfile name
		try {
			_mapname = reader.readLine();
		} catch (IOException e1) {
			writeColortext("error reading from game file", Main.ERROR);
			try {
				reader.close();
			} catch (IOException e) {
				// yes, I have to catch an IOException from within a catch block for an IOException.
				// short version: java
				// long version: compiler doesn't like exiting program without closing reader, and closing the reader
				// 		needs to be done within a try/catch block
				writeColortext("error closing game file", Main.ERROR);
			}
			
			return;
		}
		
		// given a mapfile name, try to read the map data
		String map = sandbox_client.Main.PATH_TO_ASSETS + "maps/" + _mapname + ".map";
		try {
			BufferedReader mapfile = new BufferedReader(new FileReader(map));
			// haha, look at me being optimistic that the program can still handle three-ring maps
			int mapsize = (mapfile.read() == 8 ? 61 : 37);
			_mapdata = new int[mapsize];
			for(int i=0; i<mapsize; i++) {
				_mapdata[i] = mapfile.read();
			}
			
			mapfile.close();
		} catch(IOException e) {
			writeColortext("no map file found", ERROR);
			return;
		}
				
		// start reading from the file and store the players temporarily before adding them to the database
		LinkedList<Player> names = new LinkedList<Player>();
		try {
			String line;
			while((line = reader.readLine()) != null) {
				// recall that each line of the file looks like:
				// <player name> <r> <g> <b> <race>
				// rgb should be integers in [0,255], player names should be a single word
				Player p = new Player();
				String[] details = line.trim().split(" ");
				p.name = details[0];
				p.red = Integer.parseInt(details[1]);
				p.green = Integer.parseInt(details[2]);
				p.blue = Integer.parseInt(details[3]);
				p.race = details[4];
				// races might be multiple words
				for(int i=5; i<details.length; i++) {
					p.race += " " + details[i];
				}
				
				names.addLast(p);
			}
		} catch(IOException e) {
			writeColortext("error reading from game file", Main.ERROR);
			return;
		} catch(ArrayIndexOutOfBoundsException e) {
			writeColortext("malformed game file", Main.ERROR);
			try {
				reader.close();
			} catch (IOException e1) {
				writeColortext("error closing game file", Main.ERROR);
			}
			return;
		} catch(NumberFormatException e) {
			writeColortext("malformed game file", Main.ERROR);
			try {
				reader.close();
			} catch (IOException e1) {
				writeColortext("error closing game file", Main.ERROR);
			}
			return;
		}
		
		// set up the database
		ServerDatabase.initialize(names);
		Tile.generateTiles();
		NameSAXHandler.generateNames("technology.xml", ServerDatabase.CODE_TECH);
		NameSAXHandler.generateNames("personnel.xml", ServerDatabase.CODE_PERSONNEL);
		NameSAXHandler.generateNames("resolutions.xml", ServerDatabase.CODE_RESOLUTIONS);
		
		try {
			reader.close();
		} catch (IOException e) {
			writeColortext("error closing game file", Main.ERROR);
			// we don't terminate the program. who the hell cares if the map file couldn't close properly.
		}
		
		// tell the user we're all set up
		writeColortext("server initialized", SERVEROUT);
		
		new Main();
	}

}
