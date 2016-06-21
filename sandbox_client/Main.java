package sandbox_client;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class Main extends Application {
	
	/**
	 * TODO list (it's not that long, I swear...)
	 * 1) DONE
	 * 2) DONE
	 * 3) DONE
	 * 4) fix the mess that is the council room
	 * 5) show racial abilities/flagship in status/players screen
	 * 6) color/order technology and personnel (update language too) in status/players screen
	 * 7) show policy choices in empire screen
	 * 8) show progress in empire screen
	 * 9) allow server to cache / reload old data
	 * 10) new tab: report battle data
	 * 11) new tab: view battle statistics
	 * 12) test reconnection of client a little bit more
	 * 13) abstract repeated code / comment
	 */

	public static void main(String[] args) {
		
		Database.initialize();
		Tile.generateTiles();		
		
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
