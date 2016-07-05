package sandbox_client;

/**
 * Holds information about the current stage of your empire and what you must do to advance.
 * 
 * @author dmayans
 */

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
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
	private static String[] REWARDS = {"Reward: tier i policy", "Reward: tier ii policy", "Reward: flagship",
		"Reward: tier iii policy", "Reward: victory", "Reward: -"};
		
	private Text _stage;
	private Text _commandPool;
	private Text _fleetSupply;
	private Text _objective;
	private Text _reward;
	
	private RadioButton[] _policies = new RadioButton[3];
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
		
		_policies[0] = new RadioButton("Red");
		ToggleGroup t = new ToggleGroup();
		_policies[0].setStyle("-fx-font-size: 14px");
		_policies[0].setToggleGroup(t);
		_policies[0].setLayoutX(110);
		_policies[0].setLayoutY(205);
		_policies[0].setOnAction(new ToggleHandler());
		
		_policies[1] = new RadioButton("Blue");
		_policies[1].setStyle("-fx-font-size: 14px");
		_policies[1].setToggleGroup(t);
		_policies[1].setLayoutX(170);
		_policies[1].setLayoutY(205);
		_policies[1].setOnAction(new ToggleHandler());

		_policies[2] = new RadioButton("Green");
		_policies[2].setStyle("-fx-font-size: 14px");
		_policies[2].setToggleGroup(t);
		_policies[2].setLayoutX(230);
		_policies[2].setLayoutY(205);
		_policies[2].setOnAction(new ToggleHandler());
		
		_advance = new Button("Advance");
		_advance.setDisable(true);
		_advance.setLayoutX(50);
		_advance.setLayoutY(240);
		_advance.setPrefWidth(250);
		_advance.setOnAction(e -> {
			_lock = true;
			_advance.setDisable(true);
			Database.setAdvancing(true);
		});

		pane.getChildren().addAll(_stage, _commandPool, _fleetSupply, objective, _objective, _reward, policy,
				_policies[0], _policies[1], _policies[2], _advance);
			
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
		if(i == 2) {
			_policies[0].setDisable(true);
			_policies[1].setDisable(true);
			_policies[2].setDisable(true);
			_advance.setDisable(false);
		} else if(i == 3) {
			_policies[0].setDisable(false);
			_policies[1].setDisable(false);
			_policies[2].setDisable(false);
		}
	}
	
	public int getColor() {
		if(_policies[0].isSelected()) {
			return Database.RED;
		} else if(_policies[1].isSelected()) {
			return Database.BLUE;
		} else if(_policies[2].isSelected()) {
			return Database.GREEN;
		} else {
			return 7;
		}
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
