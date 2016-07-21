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
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

public class StatusTab extends AbstractTab{
			
	private ToggleButton[] _players;
	
	private Text _race;
	private Text _stage;
	
	private Label[] _tech = new Label[5];
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

		for(int i=0; i<5; i++){
			_tech[i] = new Label();
			_tech[i].setVisible(false);
			_tech[i].setLayoutX(40);
			_tech[i].setWrapText(true);
			pane.getChildren().add(_tech[i]);
		}
		_tech[0].setLayoutY(160);

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
		_stage.setText(Database.nameOfStage(Database.empireStageOf(_player)));

		int yOffset[] = {160,176,176,176,176};
		String techText[] = new String[5];
		for(int i=1; i<5;i++){
			techText[i] = "";
		}
		techText[0] = "Technology:";
		for(String tech : Database.technologyOf(_player)) {
			//Will always go red, blue, green, yellow tech
			int color = Database.colorOfTech(tech);

			if(color == 0){ //red
				techText[1] += "-" + tech + "\n";
				yOffset[2] += 16;
				yOffset[3] += 16;
				yOffset[4] += 16;
			}else if(color==1){ //blue
				techText[2] += "-" + tech + "\n";
				yOffset[3] += 16;
				yOffset[4] += 16;
			}else if(color==2){ //green
				techText[3] += "-" + tech + "\n";
				yOffset[4] += 16;
			}else if(color==3){ //yellow
				techText[4] += "-" + tech + "\n";
			}
		}

		for(int i=0; i<5; i++) {
			_tech[i].setVisible(true);
			_tech[i].setText(techText[i]);
			_tech[i].setLayoutY(yOffset[i]);
		}

		_tech[1].setTextFill(Color.RED);
		_tech[2].setTextFill(Color.BLUE);
		_tech[3].setTextFill(Color.GREEN);
		_tech[4].setTextFill(Color.YELLOW);

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
