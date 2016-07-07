package sandbox_client;

/**
 * Holds information about the current stage of your empire and what you must do to advance.
 * 
 * @author dmayans
 */

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;

public class EmpireTab extends AbstractTab {
	
	private int _stageIndex = 0;
		
	private Text _stage;
	private Text _commandPool;
	private Text _fleetSupply;
	private Text _objective;
	private Text _reward;
	
	private Button _advance = new Button("Advance");

	private boolean _lock = false;
	
	public EmpireTab() {
		super(Client.EMPIRE);
		
		Pane pane = new Pane();
		_root.setContent(pane);
		
		_stage = new Text(Database.nameOfStage(0));
		_stage.setStyle("-fx-font-weight:bold;-fx-font-size: 24px");
		_stage.setLayoutX(30);
		_stage.setLayoutY(40);
		
		_commandPool = new Text("Command Pool: " + Database.commandPoolOfStage(0));
		_commandPool.setStyle("-fx-font-size: 14px");
		_commandPool.setLayoutX(50);
		_commandPool.setLayoutY(70);
		
		_fleetSupply = new Text("Fleet Supply: " + Database.fleetSupplyOfStage(0));
		_fleetSupply.setStyle("-fx-font-size: 14px");
		_fleetSupply.setLayoutX(50);
		_fleetSupply.setLayoutY(100);
		
		Text objective = new Text("Objective:");
		objective.setStyle("-fx-font-weight:bold;-fx-font-size: 14px");
		objective.setLayoutX(50);
		objective.setLayoutY(130);
		
		_objective = new Text(Database.objectivesOfStage(0));
		_objective.setStyle("-fx-font-size: 14px");
		_objective.setLayoutX(130);
		_objective.setLayoutY(130);
		
		_reward = new Text(Database.rewardsOfStage(0));
		_reward.setStyle("-fx-font-size: 14px");
		_reward.setLayoutX(50);
		_reward.setLayoutY(160);
				
		_advance = new Button("Advance");
		_advance.setDisable(false);
		_advance.setLayoutX(50);
		_advance.setLayoutY(240);
		_advance.setPrefWidth(250);
		_advance.setOnAction(e -> {
			_lock = true;
			_advance.setDisable(true);
			Database.setAdvancing(true);
		});

		pane.getChildren().addAll(_stage, _commandPool, _fleetSupply, objective, _objective, _reward, _advance);
			
	}
	
	public void lock() {
		_lock = true;
		_advance.setDisable(true);
	}
	
	public void unlock() {
		_lock = false;
		_advance.setDisable(false);
	}
	
	public void advance() {
		int i = ++_stageIndex;
		_stage.setText(Database.nameOfStage(i));
		_commandPool.setText("Command Pool: " + Database.commandPoolOfStage(i));
		_fleetSupply.setText("Fleet Supply: " + Database.fleetSupplyOfStage(i));
		_objective.setText(Database.objectivesOfStage(i));
		_reward.setText(Database.rewardsOfStage(i));
	}
	
	public class ToggleHandler implements EventHandler<ActionEvent> {

		@Override
		public void handle(ActionEvent e) {
			if(!_lock) {
				_advance.setDisable(false);
			}
		}
		
	}
	
}
