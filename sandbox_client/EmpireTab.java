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
	public static String[] EMPIRE_STAGE = {"(game start)", "Coalition", "Federation", "Republic", "Empire", "Imperium Rex"};
	private static String[] COMMAND_POOL = {"Command Pool: -", "Command Pool: 2", "Command Pool: 3", "Command Pool: 4", "Command Pool: 5", "Command Pool: -"};
	private static String[] FLEET_SUPPLY = {"Fleet Supply: -", "Fleet Supply: 3", "Fleet Supply: 4", "Fleet Supply: 5", "Fleet Supply: 6", "Fleet Supply: -"};
	private static String[] OBJECTIVES = {"game start", "capture one enemy planet OR destroy one enemy capital ship", 
		"control 3 border systems OR develop 2 tier iii traits OR research 2 unique techs",
		"win six battles OR destroy thirty resources", "control the Mecatol Rex system", "-"};
	private static String[] REWARDS = {"", "", "Reward: flagship","", "Reward: victory", ""};
		
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
		
		_stage = new Text("(game start)");
		_stage.setStyle("-fx-font-weight:bold;-fx-font-size: 24px");
		_stage.setLayoutX(30);
		_stage.setLayoutY(40);
		
		_commandPool = new Text("Command Pool: -");
		_commandPool.setStyle("-fx-font-size: 14px");
		_commandPool.setLayoutX(50);
		_commandPool.setLayoutY(70);
		
		_fleetSupply = new Text("Fleet Supply: -");
		_fleetSupply.setStyle("-fx-font-size: 14px");
		_fleetSupply.setLayoutX(50);
		_fleetSupply.setLayoutY(100);
		
		Text objective = new Text("Objective:");
		objective.setStyle("-fx-font-weight:bold;-fx-font-size: 14px");
		objective.setLayoutX(50);
		objective.setLayoutY(130);
		
		_objective = new Text("game start");
		_objective.setStyle("-fx-font-size: 14px");
		_objective.setLayoutX(130);
		_objective.setLayoutY(130);
		
		_reward = new Text("Reward: Tier II policy");
		_reward.setStyle("-fx-font-size: 14px");
		_reward.setLayoutX(50);
		_reward.setLayoutY(160);
		
		Text policy = new Text("Policy: ");
		policy.setStyle("-fx-font-size: 14px");
		policy.setLayoutX(50);
		policy.setLayoutY(220);
				
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

		pane.getChildren().addAll(_stage, _commandPool, _fleetSupply, objective, _objective, _reward, policy, _advance);
			
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
		_stage.setText(EMPIRE_STAGE[i]);
		_commandPool.setText(COMMAND_POOL[i]);
		_fleetSupply.setText(FLEET_SUPPLY[i]);
		_objective.setText(OBJECTIVES[i]);
		_reward.setText(REWARDS[i]);
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
