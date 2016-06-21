package sandbox_client;

/**
 * Central organizer for the four main tabs. Passes information between the server object and the client tabs.
 */

import javafx.application.Platform;
import javafx.scene.control.TabPane;

public class Client {
		
	private ControlsTab _controls;
	private MapTab _map;
	private PlanetsTab _planets;
	private ResearchTab _research;
	private PersonnelTab _personnel;
	private EmpireTab _empire;
	private StatusTab _status;
	private CouncilTab _council;
	private CombatSimTab _combat;

	private String _name;
	
	private Server _server;
	
	protected final TabPane _root = new TabPane();

	public Client() {
		
		// the tabs
		_controls = new ControlsTab(this);
		_map = new MapTab(this);
		_planets = new PlanetsTab(this);
		_research = new ResearchTab(this);
		_personnel = new PersonnelTab(this);
		_empire = new EmpireTab(this);
		_status = new StatusTab(this);
		_council = new CouncilTab(this);
		_combat = new CombatSimTab(this);
				
		// set the initially disabled until input from server
		_map._root.setDisable(true);
		_planets._root.setDisable(true);
		_research._root.setDisable(true);
		_personnel._root.setDisable(true);
		_empire._root.setDisable(true);
		_status._root.setDisable(true);
		_council._root.setDisable(true);
		_combat._root.setDisable(true);	//should be true

		// formatting
		_root.setTabMinWidth(90);
		_root.getTabs().addAll(_controls._root, _map._root, _planets._root, _research._root, _personnel._root, _empire._root, _status._root, _council._root, _combat._root);

	}
	
	// used for focus
	public void finishInitialization() {
		_controls.finishInitialization();
	}
	
	// cleanly inform user about a disconnection
	public void disconnection() {
		this.setEnabledMap(false);
		this.setEnabledPlanets(false);
		this.setEnabledResearch(false);
		this.setEnabledPersonnel(false);
		this.setEnabledEmpire(false);
		this.setEnabledStatus(false);
		this.setEnabledCouncil(false);
		this.setEnabledCombat(false);
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
				_combat.initialize();
			}
		}
	);}
	
	// ENABLE/DISABLE COMMANDS
	
	public void setEnabledMap(boolean enabled) {
		_map._root.setDisable(!enabled);
		_controls.setEnabledMap(enabled);
		if(_root.getSelectionModel().getSelectedItem() == _map._root) {
			_root.getSelectionModel().select(0);
		}
	}
	
	public void setEnabledPlanets(boolean enabled) {
		_planets._root.setDisable(!enabled);
		_controls.setEnabledPlanets(enabled);
		if(_root.getSelectionModel().getSelectedItem() == _planets._root) {
			_root.getSelectionModel().select(0);
		}
	}
	
	public void setEnabledResearch(boolean enabled) {
		_research._root.setDisable(!enabled);
		_controls.setEnabledResearch(enabled);
		if(_root.getSelectionModel().getSelectedItem() == _research._root) {
			_root.getSelectionModel().select(0);
		}
	}
	
	public void setEnabledPersonnel(boolean enabled) {
		_personnel._root.setDisable(!enabled);
		_controls.setEnabledPersonnel(enabled);
		if(_root.getSelectionModel().getSelectedItem() == _personnel._root) {
			_root.getSelectionModel().select(0);
		}
	}
	
	public void setEnabledEmpire(boolean enabled) {
		_empire._root.setDisable(!enabled);
		_controls.setEnabledEmpire(enabled);
		if(_root.getSelectionModel().getSelectedItem() == _empire._root) {
			_root.getSelectionModel().select(0);
		}
	}
	
	public void setEnabledStatus(boolean enabled) {
		_status._root.setDisable(!enabled);
		_controls.setEnabledStatus(enabled);
		if(_root.getSelectionModel().getSelectedItem() == _status._root) {
			_root.getSelectionModel().select(0);
		}
	}
	
	public void setEnabledCouncil(boolean enabled) {
		_council._root.setDisable(!enabled);
		_controls.setEnabledCouncil(enabled);
		if(_root.getSelectionModel().getSelectedItem() == _council._root) {
			_root.getSelectionModel().select(0);
		}
	}

	public void setEnabledCombat(boolean enabled) {
		_combat._root.setDisable(!enabled);
		_controls.setEnabledCombat(enabled);
		if(_root.getSelectionModel().getSelectedItem() == _combat._root) {
			_root.getSelectionModel().select(0);
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
