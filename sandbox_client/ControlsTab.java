package sandbox_client;

/**
 * Allows connection to a server. Displays useful information about the server connection.
 */

import javafx.application.Platform;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;

import java.io.IOException;
import java.net.Socket;

public class ControlsTab extends AbstractTab {
	
	private static final String DEFAULT_HOST = "104.236.42.194";
	private static final String DEFAULT_PORT = "4040";
	
	private Client _client;
	
	// connection fields
	private ComboBox<String> _name;
	private TextField _host;
	private NumberTextField _port;
	private Button _connect;
	private Text _error;
	
	// info fields
	private Text[] _enabled = new Text[Client.NUM_TABS];
	private Text _map;
	
	public ControlsTab(Client c) {
		super(Client.CONTROLS);
		_root.setDisable(false);
		
		_client = c;
				
		// initializing connection fields
		_host = new TextField();
		_host.setText(DEFAULT_HOST);
		_port = new NumberTextField();
		_port.setText(DEFAULT_PORT);
		_name = new ComboBox<String>();
		_name.setDisable(true);
		_connect = new Button("Connect");
		_connect.setOnAction(e -> tryConnection());
		GridPane.setHalignment(_connect, HPos.CENTER);
		_error = new Text("");
		GridPane.setHalignment(_error, HPos.CENTER);
		
		// initializing info fields 
		_map = new Text("<unknown>");
		for(int i=0; i<Client.NUM_TABS; i++) {
			if(i == Client.CONTROLS) continue;
			_enabled[i] = new Text("disabled");
		}

		// formatting
		GridPane grid = new GridPane();
		grid.setPadding(new Insets(15, 15, 15, 15));
		grid.setVgap(15);

		// adding connection fields
		grid.add(new Text("Host:   "), 0, 0);
		grid.add(_host, 1, 0);
		grid.add(new Text("Port:   "), 0, 1);
		grid.add(_port, 1, 1);
		grid.add(new Text("Name:   "), 0, 2);
		grid.add(_name, 1, 2);
		grid.add(_connect, 0, 3, 2, 1);
		grid.add(_error, 0, 4, 2, 1);
		grid.add(new Text(), 0, 5, 2, 1);
		
		// adding info fields
		grid.add(new Text("Map:   "), 0, 6);
		grid.add(_map, 1, 6);
		for(int i=0; i<Client.NUM_TABS; i++) {
			if(i == Client.CONTROLS) continue;
			grid.add(new Text(Client.TAB_NAMES[i] + " Tab:    "), 0, i+7);
			grid.add(_enabled[i], 1, i+7);
		}
		
		// add a spacer
		Pane spacer = new Pane();
		spacer.setPrefWidth(200);
		grid.add(spacer, 2, 0);
		
		// adding some text fields (dev phase order and market)
		Label order = new Label("Development phase order:\n\t-Advance empire\n\t-Research technology\n\t-Hire personnel\n\t-Build ships\n\t-Build space docks\n\t-Return command counters\n\t-Vote on agendas\n\t-Refill command pool\n\t-Repair ships\n\t-Refresh planets\n\t-Refresh space docks\n\t-Reveal new agendas");
		order.setWrapText(true);
		grid.add(order, 3, 0, 1, 6);
		
		Label market = new Label("Development phase marketplace:\n\t-Technology (7 resources/credits each)\n\t-Personnel (1 influence per tier per round)\n\t-Votes (1 influence each)");
		market.setWrapText(true);
		grid.add(market, 3, 6, 1, 3);
		
		_root.setContent(grid);
		
	}
	
	// populate names list
	@Override
	public void addNames() {
		_name.getItems().clear();
		for(String player : Database.playerNames()) {
			_name.getItems().add(player);
		}
		_name.setDisable(false);
		_connect.setOnAction(e -> tryLogin());
	}
	
	// and clean up connection stuff
	@Override
	public void localName(String name) {
		_name.setDisable(true);
		_connect.setOnAction(e -> disconnection());
	}
	
	// fooooocusssssssss
	public void finishInitialization() {
		_connect.requestFocus();
		_name.setMinWidth(_host.getWidth());
	}
	
	// display connection failure
	public void disconnection() {
		this.threadsafeSetText(_error, "Connection error");
		_name.setDisable(true);
		_host.setDisable(false);
		_port.setDisable(false);
		_connect.setOnAction(e -> tryConnection());
		this.threadsafeSetText(_connect, "Reconnect");
		this.threadsafeSetText(_map, "<unknown>");
	}
	
	// report the results of a connection attempt
	public void success() {
		this.threadsafeSetText(_error, "Login successful!");
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
			_connect.setDisable(true);
			_name.setDisable(true);
			_client.tryName(_name.getValue());
		});
	}

	
	// helper method to attempt to connect
	private void tryConnection() {
		// print a helpful error message if it fails
		if(_port.getText().equals("")) {
			_error.setText("Must specify port");
			return;
		} else if(_host.getText().equals("")) {
			_error.setText("Must specify host");
			return;
		}
		
		// try to connect
		_host.setDisable(true);
		_port.setDisable(true);
		_error.setText("Connecting...");
		new ServerListener().start();
	}
	
	// helper method to attempt to log in
	private void tryLogin() {
		String name = _name.getValue();
		if(name == null) {
			_error.setText("Must enter name to login");
		} else {
			_client.tryName(name);
		}
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
			int port = _port.getNumber();
			
			try {
				Socket socket = new Socket(host, port);
				Server server = new Server(socket, _client, ControlsTab.this);
				_client.setServer(server);
				threadsafeSetText(_error, "Found server");
				threadsafeSetText(_connect, "Login");
				_connect.setOnAction(e -> tryLogin());
				
			} catch (IOException e) {
				// e.printStackTrace();
				threadsafeSetText(_error, "Error connecting to server");
				_name.setDisable(true);
				_host.setDisable(false);
				_port.setDisable(false);
			}			
		}
		
	}
	
	// update the name of the map
	public void nameMap(String name) {
		threadsafeSetText(_map, name+".map");
	}
	
	// update enabled/disabled labels
	public void setEnabledGeneric(boolean enabled, int index) {
		threadsafeSetText(_enabled[index], enabled ? "enabled" : "disabled");
	}
	
}
