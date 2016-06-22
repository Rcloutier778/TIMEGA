package sandbox_client;

/**
 * Central organizer for main tabs. Passes information between the server object and the client tabs.
 * 
 * @author dmayans
 */

import javafx.application.Platform;
import javafx.scene.control.TabPane;

public class Client {
	
	// indices of tabs
	public static final int CONTROLS = 0;
	public static final int MAP = 1;
	public static final int PLANETS = 2;
	public static final int RESEARCH = 3;
	public static final int PERSONNEL = 4;
	public static final int EMPIRE = 5;
	public static final int STATUS = 6;
	public static final int COUNCIL = 7;
	public static final int SIMULATOR = 8;
	public static final int NUM_TABS = 9;
	
	// and their names (keep synchronized)
	public static final String[] TAB_NAMES = {"Home", "Map", "Planets", "Research",
		"Personnel", "Empire", "Players", "Council", "Simulator"};
	
	// contained tabs
	private ControlsTab _controls;
	private MapTab _map;
	private PlanetsTab _planets;
	private ResearchTab _research;
	private PersonnelTab _personnel;
	private EmpireTab _empire;
	private StatusTab _status;
	private CouncilTab _council;
	private CombatSimTab _simulator;
	private AbstractTab[] _tabs = new AbstractTab[NUM_TABS];

	// local name
	private String _name;
	
	private Server _server;
	
	// graphics
	protected final TabPane _root = new TabPane();

	public Client() {
		
		// the tabs
		_tabs[CONTROLS] = _controls = new ControlsTab(this);
		_tabs[MAP] = _map = new MapTab(this);
		_tabs[PLANETS] = _planets = new PlanetsTab(this);
		_tabs[RESEARCH] = _research = new ResearchTab(this);
		_tabs[PERSONNEL] = _personnel = new PersonnelTab(this);
		_tabs[EMPIRE] = _empire = new EmpireTab(this);
		_tabs[STATUS] = _status = new StatusTab(this);
		_tabs[COUNCIL] = _council = new CouncilTab(this);
		_tabs[SIMULATOR] = _simulator = new CombatSimTab(this);


		// formatting
		_root.setTabMinWidth(90);
		_root.getTabs().addAll(_controls._root, _map._root, _planets._root, _research._root,
				_personnel._root, _empire._root, _status._root, _council._root, _simulator._root);

	}
	
	// used for focus
	public void finishInitialization() {
		_controls.finishInitialization();
	}
	
	// cleanly inform user about a disconnection
	public void disconnection() {
		for(int i=0; i<NUM_TABS; i++) {
			if(i == STATUS) continue;
			this.setEnabledGeneric(false, i);
		}
		_controls.disconnection();
		Database.disconnection();
	}
	
	// add the server once created
	public void setServer(Server s) {
		_server = s;
	}
	
	// kill the server
	public void killServer() {
		_server.cleanup();
	}
	
	// FINISH SETTING STUFF UP
	
	public void databaseFinished() {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				_status.initialize();
				_research.initialize();
				_personnel.initialize();
				_simulator.initialize();
			}
		}
	);}
	
	// ENABLE/DISABLE COMMANDS
	
	public void setEnabledGeneric(boolean enabled, int index) {
		_tabs[index]._root.setDisable(!enabled);
		_controls.setEnabledGeneric(enabled, index);
		if(_root.getSelectionModel().getSelectedItem() == _tabs[index]._root) {
			_root.getSelectionModel().select(CONTROLS);
		}
	}
	
	// NAME COMMANDS
	
	public void tryName(String name) {
		_server.sendName(name);
	}
	
	public void validName(boolean valid) {
		if(valid) {
			_controls.success();
		} else {
			_controls.invalidName();
		}
	}
	
	public void setName(String name) {
		_name = name;
	}
	
	public String getName() {
		return _name;
	}
		
	// MAP COMMANDS
	
	// writes the name of the map to the controls page
	public void nameMap(String name) {
		_controls.nameMap(name);
	}
	
	// writes the contents of the map to the diplomacy page
	public void writeMap(int[] mapdata) {
		_map.writeMap(mapdata);
	}
	
	// SERVER WRITE COMMANDS
	public void write(int protocol, String text) {
		_server.write(protocol, text);
	}
	
	// PLANET CHANGE OWNER
	public void notifyChown(String planetName, String newOwner, String oldOwner) {
		_map.planetChown(planetName, newOwner);
		_planets.planetChown(newOwner, oldOwner);
		_research.updatePlanet();
		_personnel.updateSD();
	}
	
	// SPACE DOCK
	public void notifySD(String planetName, boolean sdock) {
		_map.notifySD(planetName, sdock);
		_personnel.updateSD();
	}
	
	// END ROUND
	public void endRoundStart() {
		_empire.lock();
	}
	
	public void endRoundFinish() {
		_empire.unlock();
	}
	
	// TECH AND PERSONNEL
	public void research(String player, String tech) {
		_status.update();
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				_research.research(player, tech);
			}
		});
	}
	
	public void forget(String player, String tech) {
		_status.update();
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				_research.forget(player, tech);
			}
		});
	}
	
	public void hire(String player, String person) {
		_status.update();
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				_personnel.hire(player, person);
			}
		});
		
	}
	
	public void release(String player, String person) {
		_status.update();
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				_personnel.release(player, person);
			}
		});
	}
	
	// EMPIRE
	public void advancePlayer(String player) {
		if(player.equals(_name)) {
			_empire.advance();
			_personnel.updateSD();
		}
		_status.update();
	}
	
	public int getColor() {
		return _empire.getColor();
	}
	
	// RESOLUTION
	public void resolution(String resolution1, String resolution2) {
		_council.resolved(resolution1, resolution2);
	}
	
}
