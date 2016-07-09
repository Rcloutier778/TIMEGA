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
	 * 1) add past resolutions to council room    RICHARD
	 * 2) show racial abilities/flagship in status/players screen (and add XML)
	 * 3) color technology and personnel in status/players screen
	 * 4) new tab: report battle data
	 * 5) new tab: view battle statistics
	 * 6) show progress in empire screen
	 * 7) test reconnection of client a little bit more
	 * 8) have client load style choices from local text file
	 * 9) add creuss (planet) to game if ghosts of creuss are a race
	 * 
	 * Bugs:
	 * 1) client crashes on server disconnect (low priority)
	 * 2) personnel not updating properly
	 */

	public static void main(String[] args) {
		
		Database.initialize();
		
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
