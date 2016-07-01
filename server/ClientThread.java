package server;

/**
 * Thread that handles reading from and writing to a single client.
 * 
 * @author dmayans
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Map;

import sandbox_client.Protocol;

public class ClientThread implements Runnable {
	
	// idk what this is used for, it's been a while
	private Socket _socket;
	
	// input/output streams
	private PrintWriter _out;
	private BufferedReader _in;
	
	// some client information
	private String _name = null; // name of client
	private int _id = 0; // id number passed from ClientListener
	private boolean _validated = false; // set to true when the client has gone through the laborious login protocol
	private Object _idlock = new Object(); // generic object used for its implicit lock
	
	private Main _main;
	
	public ClientThread(Socket socket, int id, Main main) {
				
		_socket = socket;
		_id = id;
		_name = "[" + Integer.toString(_id) + "] ";
		_main = main;
		
		// notify server user that the client has connected
		this.output("connected");
		
		// open up input and output streams
		try {
			_out = new PrintWriter(socket.getOutputStream(), true);
			_in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		} catch (IOException e) {
			// oopsie
			e.printStackTrace();
		}
		
	}
	
	// helper method used to print the name and message to stdout
	private void output(String s) {
		synchronized(_idlock) {
			Main.writeColortext(_name + s, Main.CLIENTOUT);
		}
	}
	
	private void output(String s, int color) {
		synchronized(_idlock) {
			Main.writeColortext(_name + s, color);
		}
	}

	// thread method
	@Override
	public void run() {
		while(true) {
			// try to parse the input as it comes in
			try {
				int command = _in.read();
				
				// if the input is an error, tell someone
				if(command == -1) {
					this.output("disconnected");
					_main.removeClient(this);
					break;
				}
				
				// interprets commands
				this.parseCommandWrapper(command);
				
			} catch (IOException e) {
				// this is usually thrown when the server closes, completely harmless (I hope)
				this.output("threw IOException", Main.ERROR);
				break;
			}
		}
	}
	
	// interprets commands read by run()
	private void parseCommandWrapper(int i) throws IOException {
		// its job is to hold the _out lock and then flush when the client is done writing (very exciting)
		synchronized(_out) {
			this.parseCommand(i);
			_out.flush();
		}
	}
	
	// assumes that the _out lock is already held when method is invoked- should only be called from the wrapper
	private void parseCommand(int i) throws IOException {
		// reply to each hello with a welcome and all player information
		if(i == Protocol.HELLO) {
			
			_out.write(Protocol.WELCOME);
			
			_out.write(ServerDatabase.PLAYERS.length);
			
			for(Player p : ServerDatabase.PLAYERS) {
				_out.write(Protocol.NEW_PLAYER);
				_out.write(p.name + "\n");
				_out.write(p.race + "\n");
				_out.write(p.red + "\n");
				_out.write(p.green + "\n");
				_out.write(p.blue + "\n");
			}
			
			this.output("handshake");
			return;
		}
		
		// read the name and validate it, renaming the client and responding accordingly
		if (i == Protocol.NAME) {
			String name = _in.readLine();
			
			boolean isValid = false;
			
			for(Player valid : ServerDatabase.PLAYERS) {
				if(name.equals(valid.name)) {
					isValid = true;
					break;
				}
			}
			
			if(!isValid) {
				this.output("entered invalid name \"" + name + "\"", Main.ERROR);
				_out.write(Protocol.INVALID);
				return;
			}
			
			this.output("renamed \"" + name + "\"", Main.CLIENTOUT);
			_out.write(Protocol.VALID);
			
			synchronized(_idlock) {
				_validated = true;
				_name = "[" + name + "] ";
			}
			
			// after the client has been accepted, update it about all of the exciting new information it missed out on
			
			// report which tabs are enabled
			for(String tab : ServerDatabase.TABS.keySet()) {
				if(_main.getEnabled(tab)) {
					_out.write(Protocol.ENABLE);
					_out.write(ServerDatabase.TABS.get(tab));
					_out.write("\n");
				}
			}
			
			// report planet ownership
			ServerDatabase.PLANETS_LOCK.lock();
			for(Map.Entry<String, String> entry : ServerDatabase.PLANETS.entrySet()) {
				if(entry.getValue().equals("none")) {
					// do nothing
				} else {
					_out.write(Protocol.PLANET_CHOWN);
					_out.write(entry.getKey() + "\n");
					_out.write(entry.getValue() + "\n");
				}
			}
			ServerDatabase.PLANETS_LOCK.unlock();
			
			// report space dock construction
			ServerDatabase.SPACEDOCKS_LOCK.lock();
			for(Map.Entry<String, Boolean> entry : ServerDatabase.SPACEDOCKS.entrySet()) {
				if(entry.getValue()) {
					_out.write(Protocol.NEW_SDOCK);
					_out.write(entry.getKey() + "\n");
				}
			}
			ServerDatabase.SPACEDOCKS_LOCK.unlock();
			
			// report technology
			ServerDatabase.TECH_LOCK.lock();
			for(Player player : ServerDatabase.PLAYERS) {
				for(String tech : ServerDatabase.TECH.get(player.name)) {
					_out.write(Protocol.SEND_TECH);
					_out.write(player.name + "\n" + tech + "\n");
				}
			}
			ServerDatabase.TECH_LOCK.unlock();
			
			// report personnel
			ServerDatabase.PERSONNEL_LOCK.lock();
			for(Player player : ServerDatabase.PLAYERS) {
				for(String trait : ServerDatabase.PERSONNEL.get(player.name)) {
					_out.write(Protocol.SEND_PERSON);
					_out.write(player.name + "\n" + trait + "\n");
				}
			}
			ServerDatabase.PERSONNEL_LOCK.unlock();

			// report empire stages
			ServerDatabase.EMPIRE_LOCK.lock();
			for(Player player : ServerDatabase.PLAYERS) {
				String sequence = ServerDatabase.EMPIRE_STAGE.get(player.name);
				for(int j=0; j<sequence.length(); j++) {
					_out.write(Protocol.ADVANCE);
					_out.write(player.name + "\n" + sequence.charAt(j) + "\n");
				}
			}
			ServerDatabase.EMPIRE_LOCK.unlock();
			
			return;

		}
		
		// all other requests (besides hello and name) must be made by validated clients		
		if (!_validated) {
			_out.write(Protocol.INVALID);			
			return;
		}
		
		// respond to map request with mapdata
		else if(i == Protocol.MAP) {
			int[] mapdata = _main.requestMap();
			String mapname = _main.requestMapName();
			
			_out.write(Protocol.MAP);
			_out.write(mapname+"\n");
			if(mapdata == null) {
				_out.write(0);
			} else {
				_out.write(mapdata.length);
				for(int tile : mapdata) {
					_out.write(tile);
				}
			}
			
		}
		
		// respond to requests to change planet owner
		else if(i == Protocol.PLANET_CHOWN) {
			String planet = _in.readLine();
			String newOwner = _in.readLine();
			
			// defers database updating and server output to Main
			
			new Thread(new Runnable() {
				@Override
				public void run() {
					_main.broadcastChown(planet, newOwner, _name, Main.CLIENTOUT);
				}
			}).start();
		}
		
		// respond to requests to change spacedock status
		else if(i == Protocol.NEW_SDOCK) {
			String planet = _in.readLine();
			
			new Thread(new Runnable() {
				@Override
				public void run() {
					_main.broadcastSD(planet, true, _name);
				}
			}).start();

		}
		
		else if(i == Protocol.REMOVE_SDOCK) {
			String planet = _in.readLine();
			new Thread(new Runnable() {
				@Override
				public void run() {
					_main.broadcastSD(planet, false, _name);
				}
			}).start();
		}
		
		else if(i == Protocol.SEND_TECH) {
			String player = _in.readLine();
			String tech = _in.readLine();
			
			new Thread(new Runnable() {
				@Override
				public void run() {
					_main.broadcastTech(player, tech, _name, Main.CLIENTOUT);
				}
			}).start();
			
		}
		
		else if(i == Protocol.SEND_PERSON) {
			String player = _in.readLine();
			String person = _in.readLine();
						
			new Thread(new Runnable() {
				@Override
				public void run() {
					_main.broadcastHire(player, person, _name, Main.CLIENTOUT);
				}
			}).start();
		}
		
		else if(i == Protocol.REMOVE_PERSON) {
			String player = _in.readLine();
			String person = _in.readLine();
			
			new Thread(new Runnable() {
				@Override
				public void run() {
					_main.broadcastRelease(player, person, _name, Main.CLIENTOUT);
				}
			}).start();
		}
		
		else if(i == Protocol.ADVANCE) {
			String player = _in.readLine();
			int color = Integer.parseInt(_in.readLine());
			
			new Thread(new Runnable() {
				@Override
				public void run() {
					_main.broadcastAdvance(player, color, _name);
				}
			}).start();
		}
		
		else if(i == Protocol.END_ROUND) {
			this.write(Protocol.ROUND_OK);
		}
		
	}
	
	public void close() {
		try {
			_socket.close();
			this.output("closed", Main.CLIENTOUT);
		} catch (IOException e) {
			this.output("already closed", Main.CLIENTOUT);
		}
	}
	
	// used to write the map (can be safely used for any generic string of ints, as long as the client can handle)
	// can also be used to write a protocol without a message
	public void write(int protocol, int ... message) {
		synchronized(_out) {
			_out.write(protocol);
			for(int i : message) {
				_out.write(i);
			}
			
			_out.flush();
		}
	}
	
	// writes a protocol and its corresponding message
	public void write(int protocol, String message) {
		synchronized(_out) {
			_out.write(protocol);
			_out.write(message);
			_out.flush();
		}
	}
	
}
