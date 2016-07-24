package sandbox_client;

/**
 * Holds information about the current stage of your empire and what you must do to advance.
 * 
 * @author dmayans
 */

import javafx.application.Platform;
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
	private Text _objectiveTitle;
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
		
		_objectiveTitle = new Text("Next Objective:");
		_objectiveTitle.setStyle("-fx-font-weight:bold;-fx-font-size: 14px");
		_objectiveTitle.setLayoutX(50);
		_objectiveTitle.setLayoutY(130);
		
		_objective = new Text(Database.objectivesOfStage(0));
		_objective.setStyle("-fx-font-size: 14px");
		_objective.setLayoutX(165);
		_objective.setLayoutY(130);
		
		_reward = new Text(Database.rewardsOfNextStage(0));
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

		pane.getChildren().addAll(_stage, _commandPool, _fleetSupply, _objectiveTitle, _objective, _reward, _advance);
			
	}
	
	public void lock() {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				_lock = true;
				_advance.setDisable(true);
			}
		});
		//_lock = true;
		//_advance.setDisable(true);
	}
	
	public void unlock() {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				_lock = false;
				_advance.setDisable(false);
			}
		});
		//_lock = false;
		//_advance.setDisable(false);
	}
	
	public void advance() {
		_stageIndex++;
		
		_stage.setText(Database.nameOfStage(_stageIndex));
		
		int i = Database.commandPoolOfStage(_stageIndex);
		if(i >= 0) {
			_commandPool.setText("Command Pool: " + i);
		} else {
			_commandPool.setText("");
		}
		
		i = Database. fleetSupplyOfStage(_stageIndex);
		if(i >= 0) {
			_fleetSupply.setText("Fleet Supply: " + i);
		} else {
			_fleetSupply.setText("");
		}

		String s = Database.objectivesOfStage(_stageIndex);
		if(s == null || s.equals("")) {
			_objectiveTitle.setText("");
			_objective.setText("");
			_commandPool.setText("Victory!");
		} else {
			_objective.setText(s);
		}
		
		s = Database.rewardsOfNextStage(_stageIndex);
		if(s == null || s.equals("")) {
			_reward.setText("");
		} else {
			_reward.setText("Reward: " + s);
		}
		
		if(_stageIndex == Database.numStages() - 1) {
			_advance.setDisable(true);
		}
	}
	
	public class ToggleHandler implements EventHandler<ActionEvent> {

		@Override
		public void handle(ActionEvent e) {
			System.out.println("test");
			if(!_lock) {
				_advance.setDisable(false);
			}
		}
		
	}
	
}
