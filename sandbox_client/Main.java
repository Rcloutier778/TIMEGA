package sandbox_client;

/**
 * Main class, constructs program, calls database initialization methods, etc.
 * 
 * @author dmayans
 */

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class Main extends Application {
	
	public static final String PATH_TO_ASSETS = System.getProperty("user.dir") + "/assets/";
	
	/**
	 * TODO list (it's not that long, I swear...)
	 * 1) fix the mess that is the council room
	 * 2) show racial abilities/flagship in status/players screen
	 * 3) color/order technology and personnel (update language too) in status/players screen
	 * 4) remove policy choices, update other deviations from new rules
	 * 5) show progress in empire screen
	 * 6) new tab: report battle data
	 * 7) new tab: view battle statistics
	 * 8) test reconnection of client a little bit more
	 * 9) allow a client to connect, then choose name from drop down list
	 * 10) have client load style choices from local text file
	 * 11) sort technology/personnel on players page
	 * 12) clean up server SAX handlers
	 * 13) use xml for empire stages and ships
	 * 14) add creuss (planet) to game if ghosts of creuss are a race
	 * 
	 * Bugs:
	 * 1) client crashes on server disconnect (low priority)
	 */

	public static void main(String[] args) {
		
		Database.initialize();
		Tile.generateTiles();
		TechSAXHandler.generateTechs();
		PersonnelSAXHandler.generatePersonnel();
		ResolutionSAXHandler.generateResolutions();
		
		launch();
	}

	@Override
	public void start(Stage stage) throws Exception {

		stage.setTitle("Client");	
		Pane pane = new Pane();
		Scene scene = new Scene(pane, 900, 600);
		stage.setScene(scene);
		
		Client c = new Client();
		
		pane.getChildren().addAll(c._root);
		
		stage.setResizable(false);
		stage.show();
		
		c.finishInitialization();
		
	}

}
