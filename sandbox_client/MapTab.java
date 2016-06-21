package sandbox_client;

/**
 * Displays the map
 */

import javafx.application.Platform;
import javafx.scene.control.Tab;
import javafx.scene.layout.Pane;

public class MapTab {
	
	protected final Tab _root = new Tab("Map");
	
	private Pane _pane = new Pane();
	private MapCanvas _map;
		
	public MapTab(Client client) {
		// formatting
		_map = new MapCanvas(client);
		_pane.getChildren().add(_map._root);
		_root.setContent(_pane);
		_root.setClosable(false);
	}
	
	// update the map data
	public void writeMap(int[] mapdata) {
		_map.writeMap(mapdata);
	}
	
	// change of planet owner
	public void planetChown(String planetName, String newOwner) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				_map.planetChown(planetName, newOwner);
			}
		});
	}
	
	// change of space docks
	public void notifySD(String planetName, boolean sdock) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				_map.notifySD(planetName, sdock);
			}
		});
	}

}
