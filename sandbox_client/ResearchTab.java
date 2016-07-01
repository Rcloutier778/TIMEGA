package sandbox_client;

/**
 * Tab that tracks and updates research progress by local player
 * 
 */

import java.util.HashMap;

import server.Player;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

public class ResearchTab extends AbstractTab {
	
	private static int PANE_WIDTH = 140;
	private static int RECT_SIZE = 9;
	
	private Client _client;
	private int _index;
		
	private GridPane _pane = new GridPane();
	private TechButton _selected;
	private Button _research;
		
	// {red, blue, green, yellow}
	private static String[] BRIGHT = {"#C80000", "#000096", "#006400", "#E6D600"};
	private static String[] GRAYED = {"#aaa", "#aaa", "#aaa", "#aaa"};
		
	private HashMap<String, TechButton> _buttons;
	
	private Text _redDiscount;
	private Text _blueDiscount;
	private Text _greenDiscount;
	private Text _yellowDiscount;
	
	private Prerequisites _prereqs;
		
	private Text _techTitle;
	private HBox _hbox;
	private ResearchLight[] _boxes;
	private Label _techDescription;
	
	public ResearchTab(Client client) {
		super(Client.RESEARCH);
		
		_client = client;
		
		_root.setContent(_pane);
		_pane.setHgap(50);
		_pane.setVgap(12);
		_pane.setPadding(new Insets(30, 30, 20, 30));
		
		_pane.add(this.createTitlePane("Red"), 0, 0);
		_redDiscount = new Text("discount: -0");
		_pane.add(this.createDiscountPane(_redDiscount), 0, 1);
		
		_pane.add(this.createTitlePane("Blue"), 1, 0);
		_blueDiscount = new Text("discount: -0");
		_pane.add(this.createDiscountPane(_blueDiscount), 1, 1);
		
		_pane.add(this.createTitlePane("Green"), 2, 0);
		_greenDiscount = new Text("discount: -0");
		_pane.add(this.createDiscountPane(_greenDiscount), 2, 1);
		
		_pane.add(this.createTitlePane("Yellow"), 3, 0);
		_yellowDiscount = new Text("discount: -0");
		_pane.add(this.createDiscountPane(_yellowDiscount), 3, 1);
		
		_buttons = new HashMap<String, TechButton>();
		TechButton b;
		
		String red, blue, green, yellow;
		
		for(int i=0; i<8; i++) {	
			red = Database.getTechName(Database.RED, i);
			b = new TechButton(red, 0);
			_pane.add(b, 0, i+2);
			_buttons.put(red, b);
			
			blue = Database.getTechName(Database.BLUE, i);
			b = new TechButton(blue, 1);
			_pane.add(b, 1, i+2);
			_buttons.put(blue, b);
			
			green = Database.getTechName(Database.GREEN, i);
			b = new TechButton(green, 2);
			_pane.add(b, 2, i+2);
			_buttons.put(green, b);
			
			yellow = Database.getTechName(Database.YELLOW, i);
			b = new TechButton(yellow, 3);
			_pane.add(b, 3, i+2);
			_buttons.put(yellow, b);
		}
				
		VBox v = new VBox(10);
		
		_hbox = new HBox(10);
		_techTitle = new Text();
		GridPane.setMargin(v, new Insets(10, 25, 25, 25));
		_techTitle.setStyle("-fx-font-weight:bold;-fx-font-size: 14px");
		_prereqs = new Prerequisites();
		_hbox.getChildren().addAll(_techTitle, _prereqs.root());
		
		_research = new Button("Research");
		_research.setVisible(false);
		_research.setOnAction(new ResearchHandler(_research));
		_research.setPrefWidth(100);
		
		_techDescription = new Label();
		_techDescription.setWrapText(true);
		_techDescription.setMaxWidth(600);
		v.getChildren().addAll(_hbox, _research, _techDescription);
		
				
		_pane.add(v, 0, 10, 4, 1);
	}
		
	@Override
	public void addNames() {
		_boxes = new ResearchLight[Database.numPlayers()];
		for(int i=0; i<_boxes.length; i++) {
			Player p = Database.getPlayer(i);
			_boxes[i] = new ResearchLight(Color.rgb(p.red, p.green, p.blue));
			_hbox.getChildren().add(_boxes[i]);
			_boxes[i].empty();
			_boxes[i].setVisible(false);
		}
	}
	
	@Override
	public void localName(String name) {
		_index = Database.indexOf(name);
	}
	
	private Pane createTitlePane(String text) {
		Text t = new Text(text);
		t.setStyle("-fx-font-weight:bold;-fx-font-size: 14px");
		Pane output = new Pane();
		output.setMinWidth(PANE_WIDTH);
		output.setMaxWidth(PANE_WIDTH);
		output.getChildren().add(t);
		return output;
	}
	
	private Pane createDiscountPane(Text text) {
		Pane output = new Pane();
		output.setMinWidth(PANE_WIDTH-10);
		output.setMaxWidth(PANE_WIDTH-10);
		output.getChildren().add(text);
		GridPane.setMargin(output, new Insets(0, 0, 0, 10));
		return output;
	}
	
	public void updatePlanet() {
		_redDiscount.setText("discount: -" + Database.localTech(Database.RED));
		_blueDiscount.setText("discount: -" + Database.localTech(Database.BLUE));
		_greenDiscount.setText("discount: -" + Database.localTech(Database.GREEN));
		_yellowDiscount.setText("discount: -" + Database.localTech(Database.YELLOW));
	}
	
	public void research(String player, String tech) {
		if(player.equals(_client.getName())) {
			_buttons.get(tech).brighten();
			if(_selected != null && _selected.getText().equals(tech)) {
				_research.setText("Remove");
				_research.setDisable(true);
			}
		}
		
		if(_selected != null && _selected.getText().equals(tech)) {
			this.paintTechBoxes(tech);
		}

	}
	
	public void forget(String player, String tech) {
		if(player.equals(_client.getName())) {
			_buttons.get(tech).gray();
			if(_selected != null && _selected.getText().equals(tech)) {
				_research.setText("Research");
				_research.setDisable(false);
			}
		}
		
		if(_selected != null && _selected.getText().equals(tech)) {
			this.paintTechBoxes(tech);
		}

	}
	
	private void paintTechBoxes(String tech) {
		for(int i=0; i<_boxes.length; i++) {
			String player = Database.getPlayer(i).name;
			if(Database.hasTech(player, tech)) {
				_boxes[i].fill();
			} else {
				_boxes[i].empty();
			}
		}
	}
	
	private class TechButton extends ToggleButton {
		
		private int _color;
		private String _name;
		private String[] _array;
		private String _status;
				
		public TechButton(String text, int color) {
			super(text);
			_color = color;
			_name = text;
			_array = GRAYED;
			_status = "transparent";
			this.setOnAction(new TechHandler(this));
			this.unhighlight();
			this.setWidth(PANE_WIDTH);
		}
				
		
		
		public void repaint() {
			this.setStyle("-fx-focus-color:transparent;-fx-background-color:" + _status + ";-fx-text-fill:" + _array[_color]);
		}
		
		public void highlight() {
			_status = "#ddd";
			this.repaint();
			_techTitle.setText(_name);
			_techDescription.setText(Database.descriptionOf(_name));
		}
		
		public void unhighlight() {
			_status = "transparent";
			this.repaint();
		}
		
		public void brighten() {
			_array = BRIGHT;
			this.repaint();
		}
		
		public void gray() {
			_array = GRAYED;
			this.repaint();
		}
				
	}
	
	private class TechHandler implements EventHandler<ActionEvent> {

		private TechButton _b;
		
		public TechHandler(TechButton b) {
			_b = b;
		}
		
		@Override
		public void handle(ActionEvent e) {
			if(_selected != null) {
				_selected.unhighlight();
			} else {
				_research.setVisible(true);
				for(int i=0; i<_boxes.length; i++) {
					_boxes[i].setVisible(true);
				}
			}
			
			_selected = _b;
			_b.highlight();
			
			paintTechBoxes(_b.getText());
			_prereqs.update(_b.getText());
			
			if(Database.isTechEnqueued(_b.getText())) {
				_research.setText("Remove");
				_research.setDisable(false);
				_boxes[_index].fill();
			} else if(Database.hasTech(_client.getName(), _b.getText())) {
				_research.setText("Remove");
				_research.setDisable(true);
				_boxes[_index].fill();
			} else {
				_research.setText("Research");
				_research.setDisable(false);
				_boxes[_index].empty();
			}
			
		}
		
	}
	
	private class ResearchHandler implements EventHandler<ActionEvent> {
		
		private Button _b;
		
		public ResearchHandler(Button b) {
			_b = b;
		}

		@Override
		public void handle(ActionEvent e) {
			if(_b.getText().equals("Remove")) {
				Database.removeFromTechQueue(_selected.getText());
				_b.setText("Research");
				_buttons.get(_selected.getText()).gray();
				_boxes[_index].empty();
			} else {
				Database.enqueueTech(_selected.getText());
				_b.setText("Remove");
				_buttons.get(_selected.getText()).brighten();
				_boxes[_index].fill();
			}
		}
		
	}
	
	
	private class ResearchLight extends Rectangle {
		
		private Color _c;
		
		public ResearchLight(Color c) {
			super(RECT_SIZE, RECT_SIZE);
			this._c = c;
			this.setStroke(c);
			HBox.setMargin(this, new Insets(4, 0, 0, 0));
		}
		
		public void fill() {
			this.setFill(_c);
		}
		
		public void empty() {
			this.setFill(Color.TRANSPARENT);
		}
		
	}
	
	private class Prerequisites {
				
		private TextFlow _root = new TextFlow();
		
		public TextFlow root() {
			return _root;
		}
		
		public void update(String tech) {
			String[] prereqs = Database.prereqsOfTech(tech);
			_root.getChildren().clear();
			if(prereqs[0] == null) {
				// no prereqs, leave it empty
			} else if(prereqs[1] == null && prereqs[2] == null) {
				Text prereqText = new Text(prereqs[0]);
				prereqText.setFill(Color.web(BRIGHT[Database.colorOfTech(prereqs[0])]));
				_root.getChildren().addAll(new Text("("), prereqText, new Text(")"));
			} else {
				Text prereq1 = new Text(prereqs[0]);
				prereq1.setFill(Color.web(BRIGHT[Database.colorOfTech(prereqs[0])]));
				Text prereq2 = new Text();
				prereq2.setFill(Color.web(BRIGHT[Database.colorOfTech(prereqs[1])]));
				if(prereqs[1] == null) {
					prereq2.setText(prereqs[2]);
					_root.getChildren().addAll(new Text("("), prereq1, new Text(" or "), prereq2, new Text(")"));
				} else if(prereqs[2] == null) {
					prereq2.setText(prereqs[1]);
					_root.getChildren().addAll(new Text("("), prereq1, new Text(" and "), prereq2, new Text(")"));
				}
			}
			
		}
		
	}
	
}
