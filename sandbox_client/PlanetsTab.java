package sandbox_client;

/**
 * Lists currently owned planets and their values. Allows players to track their resource and influence totals.
 *
 * @author dmayans
 */

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

public class PlanetsTab extends AbstractTab {

	private GridPane _pane = new GridPane();
	private Client _client;

	private HashSet<String> _resourcePlanets = new HashSet<String>();
	private HashSet<String> _influencePlanets = new HashSet<String>();

	private LinkedList<Text> _labels = new LinkedList<Text>();
	private ArrayList<RadioButton> _resourceButtons = new ArrayList<RadioButton>();
	private ArrayList<RadioButton> _influenceButtons = new ArrayList<RadioButton>();
	private LinkedList<Button> _resetButtons = new LinkedList<Button>();

	private FlowPane _HUD;
	private int _totalResources = 0;
	private Text _totalResourcesText;
	private int _totalInfluence = 0;
	private Text _totalInfluenceText;

	public PlanetsTab(Client c) {
		super(Client.PLANETS);

		_root.setContent(_pane);
		_client = c;

		_pane.setVgap(10);
		_pane.setHgap(15);
		_pane.setPadding(new Insets(20, 20, 20, 20));

		_HUD = new FlowPane();

		_totalResourcesText = new Text("Total Resources: 0");
		_totalResourcesText.setStyle("-fx-font-weight:bold;-fx-font-size: 14px");

		_totalInfluenceText = new Text("Total Influence: 0");
		_totalInfluenceText.setStyle("-fx-font-weight:bold;-fx-font-size: 14px");

		_HUD.getChildren().addAll(_totalResourcesText, new Text("            "), _totalInfluenceText);

	}

	public void planetChown(String newOwner, String oldOwner) {
		if(newOwner.equals(_client.getName()) || oldOwner.equals(_client.getName())) {
			Platform.runLater(new Runnable() {
				public void run() {
					PlanetsTab.this.updatePlanets();
				}
			});
		}
	}

	private synchronized void updatePlanets() {

		_labels.clear();
		_resourceButtons.clear();
		_influenceButtons.clear();
		_resetButtons.clear();

		_totalResources = 0;
		_totalInfluence = 0;

		for(String planet : Database.getLocalPlanets()) {
			Text t = new Text(planet);

			int resources = Database.resourcesOf(planet);
			Image resIcon = new Image(Database.iconPath(true, resources), 25, 31, false, false);
			RadioButton r = new RadioButton();
			r.setGraphic(new ImageView(resIcon));
			r.setOnAction(new RadioHandler(planet, true, false));

			int influence = Database.influenceOf(planet);
			Image infIcon = new Image(Database.iconPath(false, influence), 33, 33, false, false);
			RadioButton i = new RadioButton();
			i.setGraphic(new ImageView(infIcon));
			i.setOnAction(new RadioHandler(planet, false, true));

			Button n = new Button("Reset");
			n.setOnAction(new ResetHandler(planet, r, i));

			if(_resourcePlanets.contains(planet)) {
				r.setSelected(true);
				_totalResources += resources;
			} else if(_influencePlanets.contains(planet)) {
				i.setSelected(true);
				_totalInfluence += influence;
			}

			ToggleGroup g = new ToggleGroup();
			r.setToggleGroup(g);
			i.setToggleGroup(g);
			_labels.addLast(t);
			_resourceButtons.add(r);
			_influenceButtons.add(i);
			_resetButtons.addLast(n);
		}

		this.updateResources();
		this.updateInfluence();

		_pane.getChildren().clear();
		Iterator<Text> labelIter = _labels.iterator();
		Iterator<RadioButton> resourceIter = _resourceButtons.iterator();
		Iterator<RadioButton> influenceIter = _influenceButtons.iterator();
		Iterator<Button> nullIter = _resetButtons.iterator();

		_pane.add(_HUD, 0, 0, 12, 1);

		int i=0, size = _labels.size();
		int x=0, y=1;
		while(i++ < size) {
			Text text = labelIter.next();
			RadioButton res = resourceIter.next();
			RadioButton inf = influenceIter.next();
			Button nil = nullIter.next();
			Text spacer = new Text("    ");
			_pane.add(text, 5*x, y);
			_pane.add(res, 5*x+1, y);
			_pane.add(inf, 5*x+2, y);
			_pane.add(nil, 5*x+3, y);
			_pane.add(spacer, 5*x+4, y);
			if(y++ == 12) {
				x++;
				y = 1;
			}
		}

	}

	private void updateResources() {
		_totalResourcesText.setText("Total Resources: " + Integer.toString(_totalResources));
	}

	private void updateInfluence() {
		_totalInfluenceText.setText("Total Influence: " + Integer.toString(_totalInfluence));
	}

	private class RadioHandler implements EventHandler<ActionEvent> {

		private String _name;
		private boolean _resources;
		private boolean _influence;

		public RadioHandler(String name, boolean resources, boolean influence) {
			this._name = name;
			this._resources = resources;
			this._influence = influence;
		}

		@Override
		public void handle(ActionEvent e) {
			if(this._resources) {
				if(_resourcePlanets.add(this._name)) {
					_totalResources += Database.resourcesOf(this._name);
					updateResources();
				}

				if(_influencePlanets.remove(this._name)) {
					_totalInfluence -= Database.influenceOf(this._name);
					updateInfluence();
				}

			} else if(this._influence){
				if(_influencePlanets.add(this._name)) {
					_totalInfluence += Database.influenceOf(this._name);
					updateInfluence();
				}

				if(_resourcePlanets.remove(this._name)) {
					_totalResources -= Database.resourcesOf(this._name);
					updateResources();
				}
			}
		}

	}

	private class ResetHandler implements EventHandler<ActionEvent> {

		private String _name;
		private RadioButton _resources;
		private RadioButton _influence;

		public ResetHandler(String name, RadioButton resources, RadioButton influence) {
			this._name = name;
			this._resources = resources;
			this._influence = influence;
		}

		@Override
		public void handle(ActionEvent e) {
			if(_resourcePlanets.remove(this._name)) {
				_totalResources -= Database.resourcesOf(this._name);
				updateResources();
			}

			if(_influencePlanets.remove(this._name)) {
				_totalInfluence -= Database.influenceOf(this._name);
				updateInfluence();
			}

			this._resources.setSelected(false);
			this._influence.setSelected(false);

		}

	}

}