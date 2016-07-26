package sandbox_client;

/**
 * Central organizer for main tabs. Passes information between the server object and the client tabs.
 * 
 * @author dmayans
 */

import javafx.application.Platform;
import javafx.scene.control.TabPane;

public class Client {
	
	public static final int TAB_WIDTH = 85;
	
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
	
	// and their names (keep synchronized)
	public static final String[] TAB_NAMES = {"Home", "Map", "Planets", "Research",
		"Personnel", "Empire", "Players", "Council", "Simulator"};
	
	public static final int NUM_TABS = TAB_NAMES.length;
	
	
	
	// contained tabs
	private ControlsTab _controls;
	private MapTab _map;
	private PlanetsTab _planets;
	private ResearchTab _research;
	private PersonnelTab _personnel;
	private EmpireTab _empire;
	private StatusTab _status;
	private CouncilTab _council;
	@SuppressWarnings("unused")
	private CombatSimTab _simulator;
	
	private AbstractTab[] _tabs = new AbstractTab[NUM_TABS];

	private Server _server;
	
	// graphics
	protected final TabPane _root = new TabPane();

	public Client() {
		
		// the tabs
		_tabs[CONTROLS] = _controls = new ControlsTab(this);
		_tabs[MAP] = _map = new MapTab(this);
		_tabs[PLANETS] = _planets = new PlanetsTab();
		_tabs[RESEARCH] = _research = new ResearchTab();
		_tabs[PERSONNEL] = _personnel = new PersonnelTab();
		_tabs[EMPIRE] = _empire = new EmpireTab();
		_tabs[STATUS] = _status = new StatusTab();
		_tabs[COUNCIL] = _council = new CouncilTab();
		_tabs[SIMULATOR] = _simulator = new CombatSimTab();


		// formatting
		_root.setTabMinWidth(TAB_WIDTH);
		for(AbstractTab tab : _tabs) {
			_root.getTabs().add(tab._root);
		}

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
	
	// initialize tabs based on player information from server
	public void namesReceived() {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				for(AbstractTab t : _tabs) {
					t.addNames();
				}
			}
		}
	);}
	
	
	// enables or disables a tab
	public void setEnabledGeneric(boolean enabled, int index) {
		_tabs[index]._root.setDisable(!enabled);
		_controls.setEnabledGeneric(enabled, index);
		if(_root.getSelectionModel().getSelectedItem() == _tabs[index]._root) {
			_root.getSelectionModel().select(CONTROLS);
		}
	}
	
	// handling player names
	public void tryName(String name) {
		_server.tryLogin(name);
	}
	
	public void validName(String name) {
		if(name != null) {
			_controls.success();
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					for(AbstractTab t : _tabs) {
						t.localName(name);
					}
				}
			});
		} else {
			_controls.invalidName();
		}
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
	
	// write information to the server
	public void write(int protocol, String text) {
		System.out.println("In Client write" + System.nanoTime());
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
		if(player.equals(Database.getName())) {
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					_empire.advance();
					_personnel.updateSD();
				}
			});
		}
		_status.update();
	}
	
	// RESOLUTION
	public void resolution(String resolution1, String resolution2) {
		_council.resolved(resolution1, resolution2);
	}

	// RESOLUTION RESULT
	public void resolutionResult(){
		_council.result();
	}
	
}
