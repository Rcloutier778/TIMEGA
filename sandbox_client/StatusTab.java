package sandbox_client;

/**
 * Holds information on all players of the game (race, current empire stage, technology, and personnel)
 * 
 * @author dmayans
 */

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;

public class StatusTab extends AbstractTab{
			
	private ToggleButton[] _players;
	
	private Text _race;
	private Text _stage;
	
	private Label _tech;
	private Label _personnel;
	
	private String _player;
	
	public StatusTab() {
		super(Client.STATUS);
	}
	
	@Override
	public void addNames() {
		
		_players = new ToggleButton[Database.numPlayers()];
				
		Pane pane = new Pane();
		_root.setContent(pane);
		
		ToggleGroup t = new ToggleGroup();
		
		double spacing = (900.0 - 120.0)/(_players.length);
		double width = _players.length == 8 ? 90.0 : 100.0;
		
		for(int i=0; i<_players.length; i++) {
			_players[i] = new ToggleButton(Database.getPlayer(i).name);
			_players[i].setToggleGroup(t);
			_players[i].setPrefWidth(width);
			_players[i].setLayoutX(60 + spacing*i);
			_players[i].setLayoutY(20);
			_players[i].setOnAction(new PlayerListener(Database.getPlayer(i).name));
			pane.getChildren().add(_players[i]);
		}
		
		_race = new Text();
		_race.setVisible(false);
		_race.setLayoutX(40);
		_race.setLayoutY(90);
		_race.setStyle("-fx-font-weight:bold;-fx-font-size: 24px");
		pane.getChildren().add(_race);
		
		_stage = new Text();
		_stage.setVisible(false);
		_stage.setLayoutX(40);
		_stage.setLayoutY(120);
		_stage.setStyle("-fx-font-weight:bold;-fx-font-size: 18px");
		pane.getChildren().add(_stage);
		
		_tech = new Label();
		_tech.setVisible(false);
		_tech.setLayoutX(40);
		_tech.setLayoutY(160);
		_tech.setWrapText(true);
		pane.getChildren().add(_tech);
		
		_personnel = new Label();
		_personnel.setVisible(false);
		_personnel.setLayoutX(490);
		_personnel.setLayoutY(160);
		_personnel.setWrapText(true);
		pane.getChildren().add(_personnel);
		
	}

	public void update() {
		if(_player == null) {
			return;
		}
		
		Platform.runLater(new Runnable() {
			public void run() {
				updateHelper();
			}
		});
	}
		
	private void updateHelper() {
		
		_race.setVisible(true);
		_race.setText(Database.raceOf(_player));
		
		_stage.setVisible(true);
		_stage.setText(EmpireTab.EMPIRE_STAGE[Database.empireStageOf(_player)]);
		
		String techText = "Technology:\n";
		for(String tech : Database.technologyOf(_player)) {
			techText += "-" + tech + "\n";
		}
		
		_tech.setVisible(true);
		_tech.setText(techText);
		
		String personnelText = "Personnel:\n";
		for(String personnel : Database.personnelOfPlayer(_player)) {
			personnelText += "-" + personnel + "\n";
		}
		
		_personnel.setVisible(true);
		_personnel.setText(personnelText);
	}
	
	private class PlayerListener implements EventHandler<ActionEvent> {
		
		private String _name;
		
		public PlayerListener(String name) {
			_name = name;
		}

		@Override
		public void handle(ActionEvent e) {
			_player = _name;
			update();
		}
		
	}
	
}
