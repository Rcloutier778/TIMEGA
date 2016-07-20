package sandbox_client;

/**
 * Models the server connection. Handles all server I/O.
 */

import server.Player;
import server.ServerDatabase;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;

public class Server implements Runnable {
	
	private Socket _socket;
	private PrintWriter _out;
	private BufferedReader _in;
	
	private Client _client;
	
	public Server(Socket socket, Client client, ControlsTab tab) {
		_client = client;

		// set up a connection
		try {
			_out = new PrintWriter(socket.getOutputStream(), true);
			_in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			
			// initialize handshake
			_out.write(Protocol.HELLO);
			_out.flush();
			
			if(_in.read() == Protocol.WELCOME) {
				// if handshake successful, try to connect
				this.readNames();
				_client.namesReceived();
			}
						
		} catch (IOException e) {
			// e.printStackTrace();
			_client.disconnection();
		}
		
	}
	
	// get names from server
	private void readNames() throws IOException {
		
		int numPlayers = _in.read();
		for(int i=0; i<numPlayers; i++) {
			Player p = new Player();
			p.name = _in.readLine();
			p.race = _in.readLine();
			p.red = Integer.parseInt(_in.readLine());
			p.green = Integer.parseInt(_in.readLine());
			p.blue = Integer.parseInt(_in.readLine());
			
			Database.addPlayer(p);
		}

	}
	
	// try to log in
	public void tryLogin(String name) {
		_out.write(Protocol.NAME);
		_out.write(name);
		_out.write("\n");
		_out.flush();
		
		int message = 0;
		try {
			message = _in.read();
		} catch (IOException e) {
			_client.disconnection();
		}
		
		if(message == Protocol.VALID) {
			Database.setName(name);
			_client.validName(name);
			// request map
			_out.write(Protocol.MAP);
			_out.flush();
			Thread t = new Thread(this);
			t.setDaemon(true);
			t.start();
			return;
		} else if(message == Protocol.INVALID) {
			_client.validName(null);
			return;
		}
		
	}
	
	// disconnect the server
	public void cleanup() {
		try {
			if(_socket != null)
				_socket.close();
			_out.close();
			_in.close();
			_client.disconnection();
		} catch (IOException e) {
			// e.printStackTrace();
		}
	}

	// loop to handle tcp input
	@Override
	public void run() {
		while(true) {
			try {
				
				int command = _in.read();
				if(command == -1) {
					break;
				}
				
				this.handle(command);
				
			} catch(IOException e) {
				e.printStackTrace();
				break;
			}
		}
		
		this.cleanup();
	}

	// read and respond to a message
	private synchronized void handle(int message) throws IOException {
		
		if(message == Protocol.MAP) {
			String name = _in.readLine();
			_client.nameMap(name);
			int length = _in.read();
			if (length == 0) {
				return;
			}
			
			int[] mapdata = new int[length];
			for(int i=0; i<length; i++) {
				mapdata[i] = _in.read();
			}
			_client.writeMap(mapdata);
			
		} else if(message == Protocol.ENABLE) {
			String tab = _in.readLine();
			_client.setEnabledGeneric(true, Arrays.asList(Client.TAB_NAMES).indexOf(tab));
		} else if(message == Protocol.DISABLE) {
			String tab = _in.readLine();
			_client.setEnabledGeneric(false, Arrays.asList(Client.TAB_NAMES).indexOf(tab));
			
		} else if(message == Protocol.PLANET_CHOWN) {
			String planetName = _in.readLine();
			String newOwner = _in.readLine();
			String oldOwner = Database.ownerOf(planetName);
			Database.updatePlanet(planetName, newOwner);
			_client.notifyChown(planetName, newOwner, oldOwner);

		} else if(message == Protocol.NEW_SDOCK) {
			String planetName = _in.readLine();
			Database.updateSD(planetName, true);
			_client.notifySD(planetName, true);

		} else if(message == Protocol.REMOVE_SDOCK) {
			String planetName = _in.readLine();
			Database.updateSD(planetName, false);
			_client.notifySD(planetName, false);

		} else if(message == Protocol.END_ROUND) {
			_client.endRoundStart();
			
			for(String tech : Database.getTechQueue()) {
				this.write(Protocol.SEND_TECH, Database.getName() + "\n" + tech);
			}
			
			for(String person : Database.getPersonnelQueue()) {
				if(Database.hasPerson(Database.getName(), person)) {
					this.write(Protocol.REMOVE_PERSON, Database.getName() + "\n" + person);
				} else {
					this.write(Protocol.SEND_PERSON, Database.getName() + "\n" + person);
				}
			}
			
			this.write(Protocol.END_ROUND);
			
			if(Database.isAdvancing()) {
				this.write(Protocol.ADVANCE, Database.getName());
			}
						
			Database.clearTechQueue();
			Database.clearPersonnelQueue();
			Database.setAdvancing(false);
			
		} else if(message == Protocol.SEND_TECH) {
			String player = _in.readLine();
			String tech = _in.readLine();
			
			Database.research(player, tech);
			
			_client.research(player, tech);
			
		} else if(message == Protocol.REMOVE_TECH) {
			String player = _in.readLine();
			String tech = _in.readLine();
			
			Database.forget(player, tech);
			
			_client.forget(player, tech);
			
		} else if(message == Protocol.SEND_PERSON) {
			String player = _in.readLine();
			String person = _in.readLine();
			
			Database.hire(player, person);
			
			_client.hire(player, person);
			
		} else if(message == Protocol.REMOVE_PERSON) {
			String player = _in.readLine();
			String person = _in.readLine();
			
			Database.release(player, person);
			
			_client.release(player, person);
		}
		
		else if(message == Protocol.ADVANCE) {
			String player = _in.readLine();
			
			Database.advancePlayer(player);
			_client.advancePlayer(player);
			
		} else if(message == Protocol.ROUND_OK) {
			_client.endRoundFinish();
		} else if(message == Protocol.SEND_RESOLUTION) {
			String resolution1 = _in.readLine();
			String resolution2 = _in.readLine();

			_client.resolution(resolution1, resolution2);
		} else if(message == Protocol.RESOLUTION_RESULT){
			String result[] = new String[4];
			result[0] = _in.readLine();
			result[1] = _in.readLine();
			result[2] = _in.readLine();
			result[3] = _in.readLine();

			//todo make sure repeal doesn't slip past.
			for(int i=0; i<2; i++){
				if(result[i*2].equals("Repeal") && result[(i*2)+1].equals("for")){
					ServerDatabase.RESOLUTION_LOCK.lock();
					for(String s : Database.resolutionKeys()){
						if(!ServerDatabase.PAST_RESOLUTION.keySet().contains(s)){
							Database.removePast(s);
						}
					}
					ServerDatabase.RESOLUTION_LOCK.unlock();
				}
				else if(result[i*2].equals("Revote")){
					//do nothing
				}
				else{
					Database.putRes(result[i*2], result[(i*2)+1]);
				}
			}

			for(int i=0; i<2; i++) {
				if (result[i*2].equals("New Constitution") && result[(i*2)+1].equals("for")) {
					Database.clearPast();
				}
			}

			_client.resolutionResult();
		}
	}
	
	public synchronized void write(int protocol, String text) {
		_out.write(protocol);
		_out.write(text + "\n");
		_out.flush();
	}
	
	public synchronized void write(int protocol) {
		_out.write(protocol);
		_out.flush();
	}
	

}
