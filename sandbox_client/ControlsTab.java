package sandbox_client;

/**
 * Allows connection to a server. Displays useful information about the server connection.
 */

import javafx.application.Platform;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;

import java.io.IOException;
import java.net.Socket;

public class ControlsTab {

	protected final Tab _root = new Tab("Home");
	
	private Client _client;
	
	// connection fields
	private TextField _name;
	private TextField _host;
	private NumberTextField _port;
	private Button _connect;
	private Text _error;
	
	// info fields
	private Text _map;
	private Text _mapEnabled;
	private Text _planetsEnabled;
	private Text _researchEnabled;
	private Text _personnelEnabled;
	private Text _empireEnabled;
	private Text _statusEnabled;
	private Text _councilEnabled;
	private Text _combatEnabled;
	
	public ControlsTab(Client c) {
		_root.setClosable(false);
		
		_client = c;
				
		// initializing connection fields
		_name = new TextField();
		_host = new TextField();
		_host.setText("104.236.42.194");
		_port = new NumberTextField();
		_port.setText("4040");
		_connect = new Button("Connect");
		_connect.setOnAction(e -> tryConnection());
		GridPane.setHalignment(_connect, HPos.CENTER);
		_error = new Text("");
		GridPane.setHalignment(_error, HPos.CENTER);
		
		// initializing info fields 
		_map = new Text("<unknown>");
		_mapEnabled = new Text("disabled");
		_planetsEnabled = new Text("disabled");
		_researchEnabled = new Text("disabled");
		_personnelEnabled = new Text("disabled");
		_empireEnabled = new Text("disabled");
		_statusEnabled = new Text("disabled");
		_councilEnabled = new Text("disabled");
		_combatEnabled = new Text("disabled");

		// formatting
		GridPane grid = new GridPane();
		grid.setPadding(new Insets(15, 15, 15, 15));
		grid.setVgap(15);

		// adding connection fields
		grid.add(new Text("Name:   "), 0, 0);
		grid.add(_name, 1, 0);
		grid.add(new Text("Host:   "), 0, 1);
		grid.add(_host, 1, 1);
		grid.add(new Text("Port:   "), 0, 2);
		grid.add(_port, 1, 2);
		grid.add(_connect, 0, 3, 2, 1);
		grid.add(_error, 0, 4, 2, 1);
		grid.add(new Text(), 0, 5, 2, 1);
		
		// adding info fields
		grid.add(new Text("Map:   "), 0, 6);
		grid.add(_map, 1, 6);
		grid.add(new Text("Map Tab:   "), 0, 7);
		grid.add(_mapEnabled, 1, 7);
		grid.add(new Text("Planets Tab:   "), 0, 8);
		grid.add(_planetsEnabled, 1, 8);
		grid.add(new Text("Research Tab:   "), 0, 9);
		grid.add(_researchEnabled, 1, 9);
		grid.add(new Text("Personnel Tab:    "), 0, 10);
		grid.add(_personnelEnabled, 1, 10);
		grid.add(new Text("Empire Tab:"), 0, 11);
		grid.add(_empireEnabled, 1, 11);
		grid.add(new Text("Players Tab:    "), 0, 12);
		grid.add(_statusEnabled, 1, 12);
		grid.add(new Text("Council Tab:    "), 0, 13);
		grid.add(_councilEnabled, 1, 13);
		grid.add(new Text("Combat Tab:    "), 0, 14);
		grid.add(_combatEnabled, 1, 14);
		
		// add a spacer
		Pane spacer = new Pane();
		spacer.setPrefWidth(200);
		grid.add(spacer, 2, 0);
		
		// adding some text fields
		Label order = new Label("Development phase order:\n\t-Advance empire\n\t-Research technology\n\t-Hire personnel\n\t-Build ships\n\t-Build space docks\n\t-Return command counters\n\t-Vote on agendas\n\t-Refill command pool\n\t-Repair ships\n\t-Refresh planets\n\t-Refresh space docks\n\t-Reveal new agendas");
		order.setWrapText(true);
		grid.add(order, 3, 0, 1, 6);
		
		Label market = new Label("Development phase marketplace:\n\t-Technology (8 resources each)\n\t-Personnel (2 influence each per round)\n\t-Votes (1 influence each)");
		market.setWrapText(true);
		grid.add(market, 3, 6, 1, 3);
		
		_root.setContent(grid);
	}
	
	public void finishInitialization() {
		_name.requestFocus();
	}
	
	// display connection failure
	public void disconnection() {
		this.threadsafeSetText(_error, "Connection error");
		this.disableConnectionAttempt(false);
		_connect.setOnAction(e -> tryConnection());
		this.threadsafeSetText(_connect, "Reconnect");
		this.threadsafeSetText(_map, "<unknown>");
	}
	
	// report the results of a connection attempt
	public void success() {
		this.threadsafeSetText(_error, "Connection successful!");
		_connect.setDisable(false);
		this.threadsafeSetText(_connect, "Disconnect");
		_connect.setOnAction(e -> {
			this.disconnection();
			_client.killServer();
		});
	}
	
	public void invalidName() {
		this.threadsafeSetText(_error, "Invalid name.");
		_connect.setDisable(false);
		_host.setDisable(true);
		_port.setDisable(true);
		_name.setDisable(false);
		this.threadsafeSetText(_connect, "Try again");
		_connect.setOnAction(e -> {
			this.threadsafeSetText(_connect, "Connect");
			_connect.setText("Connect");
			_connect.setDisable(true);
			_name.setDisable(true);
			_client.tryName(_name.getText());
		});
	}
	
	// helper method to disable or enable all connection fields
	private void disableConnectionAttempt(boolean state) {
		_connect.setDisable(state);
		_host.setDisable(state);
		_port.setDisable(state);
		_name.setDisable(state);
	}
	
	// helper method to attempt to connect
	private void tryConnection() {
		if(_port.getText().equals("")) {
			_error.setText("Must specify port");
			return;
		} else if(_host.getText().equals("")) {
			_error.setText("Must specify host");
			return;
		} else if(_name.getText().equals("")) {
			_error.setText("Must enter username");
			return;
		}
		
		this.disableConnectionAttempt(true);
		_error.setText("Connecting...");
		
		new ServerListener().start();
	}
	
	// overloaded methods to allow the program to safely change the text in labels and buttons
	private void threadsafeSetText(Text text, String data) {
		Platform.runLater(new Runnable() {
			public void run() {
				text.setText(data);
			}
		});
	}
	
	private void threadsafeSetText(Button button, String data) {
		Platform.runLater(new Runnable() {
			public void run() {
				button.setText(data);
			}
		});
	}
	
	// private thread to connect to a server 
	private class ServerListener extends Thread {
		
		public ServerListener() {
			super();
			setDaemon(true);
		}

		@Override
		public void run() {
			String host = _host.getText();
			int port = Integer.parseInt(_port.getText());
			
			try {
				Socket socket = new Socket(host, port);
				Server server = new Server(socket, _client, ControlsTab.this, _name.getText());
				_client.setServer(server);
				_client.setName(_name.getText());
				threadsafeSetText(_error, "Found server");
				
			} catch (IOException e) {
				// e.printStackTrace();
				threadsafeSetText(_error, "Error connecting to server");
				disableConnectionAttempt(false);
			}			
		}
		
	}
	
	// update the name of the map
	public void nameMap(String name) {
		threadsafeSetText(_map, name+".map");
	}
	
	// update enabled/disabled labels
	public void setEnabledMap(boolean enabled) {
		threadsafeSetText(_mapEnabled, enabled ? "enabled" : "disabled");
	}
	
	public void setEnabledPlanets(boolean enabled) {
		threadsafeSetText(_planetsEnabled, enabled ? "enabled" : "disabled");
	}
	
	public void setEnabledResearch(boolean enabled) {
		threadsafeSetText(_researchEnabled, enabled ? "enabled" : "disabled");
	}
	
	public void setEnabledPersonnel(boolean enabled) {
		threadsafeSetText(_personnelEnabled, enabled ? "enabled" : "disabled");
	}
	
	public void setEnabledEmpire(boolean enabled) {
		threadsafeSetText(_empireEnabled, enabled ? "enabled" : "disabled");
	}
	
	public void setEnabledStatus(boolean enabled) {
		threadsafeSetText(_statusEnabled, enabled ? "enabled" : "disabled");
	}
	
	public void setEnabledCouncil(boolean enabled) {
		threadsafeSetText(_councilEnabled, enabled ? "enabled" : "disabled");
	}
	public void setEnabledCombat(boolean enabled) {
		threadsafeSetText(_combatEnabled, enabled ? "enabled" : "disabled");
	}
}
