package sandbox_client;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Polygon;

// canvas on which to draw the map
public class MapCanvas {
	
	private static int NUMRINGS = 4;
	
	private static void updateNumRings(int numRings) {
		Hexagon.RADIUS = 35; //80-(10*numRings);
		Hexagon.XOFFSET = 87; //110-(15*numRings);
		Hexagon.YOFFSET = 162; //225-(5*numRings);
		NUMRINGS = numRings;
	}
	
	private static boolean inBounds(int u, int v) {
		if(u < 0 || v < 0)
			return false;
		if(u > 2*NUMRINGS || v > 2*NUMRINGS)
			return false;
		return Math.abs(u - v) <= NUMRINGS;
	}
	
	
	
	
	
	protected final Pane _root = new Pane();
	private Client _client;
	
	private Hexagon[][] _systems;
	private Hexagon _selected;
	private BigHex _displayed = new BigHex();
	
	private Button[] _planets = new Button[]{new Button(), new Button(), new Button()};
	private CheckBox[] _spacedocks = new CheckBox[]{new CheckBox(), new CheckBox(), new CheckBox()};
	
	private ToggleButton _colorByPlayer = new ToggleButton("Color by Player");
	private ToggleButton _colorSD = new ToggleButton("Show Space Docks");
	
	public MapCanvas(Client client) {
		_client = client;
		
		_root.setPrefWidth(900);
		_root.setPrefHeight(600);
		_root.setOnMouseClicked(event -> {
			this.click((int)event.getX(), (int)event.getY());
		});
		_root.setStyle("-fx-background-color: #000000");
		
		for(int i=0; i<_planets.length; i++) {
			_planets[i].setVisible(false);
			_planets[i].setLayoutX(645);
			_planets[i].setLayoutY(220 + 40 * i);
			_planets[i].setText("Capture Mecatol Rex");
			_planets[i].setPrefWidth(150);
			_root.getChildren().add(_planets[i]);
			
			_spacedocks[i].setVisible(false);
			_spacedocks[i].setLayoutX(805);
			_spacedocks[i].setLayoutY(224 + 40 * i);
			_spacedocks[i].setText("SD");
			_spacedocks[i].setPrefWidth(50);
			_spacedocks[i].setStyle("-fx-text-fill:white;");
			_root.getChildren().add(_spacedocks[i]);
		}
			
		_colorByPlayer.setPrefWidth(210);
		_colorByPlayer.setLayoutX(645);
		_colorByPlayer.setLayoutY(450);
		_colorByPlayer.setOnAction(e -> this.paintOverlay());
		_root.getChildren().add(_colorByPlayer);
		
		_colorSD.setPrefWidth(210);
		_colorSD.setLayoutX(645);
		_colorSD.setLayoutY(490);
		_colorSD.setOnAction(e -> this.paintOverlay());
		_root.getChildren().add(_colorSD);
		
	}
	
	// Adds the given mapdata to the map and draws it on the left half of the board
	public void writeMap(int[] mapdata) {
		if(mapdata.length == 37) {
			updateNumRings(3);
		} else {
			updateNumRings(4);
		}
		
		_systems = new Hexagon[2*NUMRINGS+1][2*NUMRINGS+1];
		int i = 0;
		for(int u=0; u<=2*NUMRINGS; u++) {
			for(int v=0; v<=2*NUMRINGS; v++) {
				if(Math.abs(u-v)<=NUMRINGS) {
					Hexagon hex = new Hexagon(u, v, mapdata[i++]);
					_systems[u][v] = hex;
				}
			}
		}
		
		Platform.runLater(new Runnable() {
			public void run() {
				for(int u=0; u<=2*NUMRINGS; u++) {
					for(int v=0; v<=2*NUMRINGS; v++) {
						if(Math.abs(u-v) <= NUMRINGS) {
							
							Hexagon hex = _systems[u][v];
							_root.getChildren().add(hex._shape);
							_root.getChildren().addAll(hex.overlay());
							
						}
					}
				}
			}
		});
		
		_selected = _systems[0][0];
		
	}
	
	// change owner of a planet
	public void planetChown(String planetName, String newOwner) {
		// if either colorByPlayer or colorSD is selected, it should be repainted
		if(_colorByPlayer.isSelected() || _colorSD.isSelected()) {
			this.paintOverlay();
		}
		
		if(_selected == null) 
			return;

		// if the currently selected planet's owner has been changed
		int planet = Database.tile(_selected.id()).hasPlanet(planetName);
		if(planet >= 0) {
			Button b = _planets[planet];
			if(newOwner.equals(_client.getName())) {
				b.setText("Release " + planetName);
				b.setOnAction(new ButtonHandler(planetName, false));
			} else {
				b.setText("Capture " + planetName);
				b.setOnAction(new ButtonHandler(planetName, true));
			}
		}
	}
	
	// change space dock status
	public void notifySD(String planetName, boolean sdock) {
		if(_colorSD.isSelected()) {
			this.paintOverlay();
		}
		
		if(_selected == null) {
			return;
		}
		
		// if the currently selected tile's space dock status has changed
		int planet = Database.tile(_selected.id()).hasPlanet(planetName);
		if(planet >= 0) {
			CheckBox c = _spacedocks[planet];
			c.setSelected(sdock);
		}
	}
	
	// transform x and y to u and v and update _selected
	private void click(int x, int y) {		
		// if a tile was selected
		Point hex = identifyHexagon(x, y);
		if(inBounds(hex.u, hex.v)) {
			
			// unclick the last tile
			if(_selected != null) {
				_selected.unclick();
			}
			
			// click the new tile
			_selected = _systems[hex.u][hex.v];
			_selected.click();
			
			_displayed.draw(_selected.id());
			Tile currtile = Database.tile(_selected.id());
			
			// draw capture/release and spacedock buttons
			for(int i=0; i<3; i++) {
				String name = currtile.getPlanetName(i);
				if(name != null) {
					_planets[i].setVisible(true);
					_spacedocks[i].setVisible(true);
					
					if(Database.ownerOf(name).equals(_client.getName())) {
						_planets[i].setText("Release " + name);
						_planets[i].setOnAction(new ButtonHandler(name, false));
					} else {
						_planets[i].setText("Capture " + name);
						_planets[i].setOnAction(new ButtonHandler(name, true));
					}
					
					_spacedocks[i].setOnAction(new CheckBoxHandler(name, i));
					if(Database.hasSD(name)) {
						_spacedocks[i].setSelected(true);
					} else {
						_spacedocks[i].setSelected(false);
					}
					_spacedocks[i].setDisable(name.equals("Mecatol Rex") | name.equals("Tsion") | name.equals("Sumerian"));
					
				} else {
					_planets[i].setVisible(false);
					_spacedocks[i].setVisible(false);
				}
			}
			
		// if no tile was selected, don't do anything
		} else {
			//_selected = null;
			//_displayed.draw(-1);
			//for(int i=0; i<3; i++) {
			//	_planets[i].setVisible(false);
			//	_spacedocks[i].setVisible(false);
			//}
		}
	}
	
	// actually transform the x and y coordinates into four candidates, then find the closest one
	private Point identifyHexagon(int x, int y) {
		x -= Hexagon.XOFFSET;
		y -= Hexagon.YOFFSET;
		Point uv = this.transform(x, y);
		
		int[][] candidates = {{uv.u, uv.v},  {uv.u + 1, uv.v}, {uv.u, uv.v + 1}, {uv.u + 1, uv.v + 1}};
		double distance = Double.MAX_VALUE;
		Point clicked = new Point();
		
		for(int[] candidate : candidates) {
			double newdistance = this.distance(x, y, candidate[0], candidate[1]);
			if(newdistance < distance) {
				distance = newdistance;
				clicked.u = candidate[0];
				clicked.v = candidate[1];
			}
		}
		
		return clicked;
		
	}
	
	/* Change of basis from (x, y) => (u, v):
	 * 
	 * u = sqrt(3)*y
	 * v = (3/2)*x + (sqrt(3)/2)*y
	 * 
	 * P(uv => xy):
	 * 
	 * [ 0         3/2       ]
	 * [ sqrt(3)  -sqrt(3)/2 ]
	 * 
	 * then P^(-1) = P(xy => uv):
	 * 
	 * [ 1/3     sqrt(3)/3 ]
	 * [ 2/3     0         ]
	 */ 
	private Point transform(int x, int y) {
		double a = (1.0)/(3.0);
		double b = (2.0)/(3.0);
		double c = (Math.sqrt(3))/(3.0);
		double d = 0.0;
		
		Point output = new Point();
		
		output.u = (int) ((x*a + y*c)/Hexagon.RADIUS);
		output.v = (int) ((x*b + y*d)/Hexagon.RADIUS);
		
		return output;
	}
	
	// distance formula
	private double distance(int x, int y, int u, int v) {
		double a = 0.0;
		double b = Math.sqrt(3);
		double c = 3.0/2.0;
		double d = -Math.sqrt(3)/2.0;
		
		int x1 = x;
		int y1 = y;
		
		int x2 = (int) ((u*a + v*c)*Hexagon.RADIUS);
		int y2 = (int) ((u*b + v*d)*Hexagon.RADIUS);
		
		// yes, it technically returns the square of distance
		return (x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2);
	}
	
	private void paintOverlay() {
		boolean player = _colorByPlayer.isSelected();
		boolean sd = _colorSD.isSelected();
		for(int u=0; u<=2*NUMRINGS; u++) {
			for(int v=0; v<=2*NUMRINGS; v++) {
				if(Math.abs(u-v)<=NUMRINGS) {
					_systems[u][v].paintByPlayer(player);
					_systems[u][v].paintSD(sd);
				}
			}
		}
	}
	
	// small container for two integral coordinates
	private class Point {
		public int u;
		public int v;
	}
	
	// class that handles the large hexagon on the right
	private class BigHex {
		
		private int RADIUS = 100;
		
		private double[] _x;
		private double[] _y;
		private final Polygon _shape = new Polygon();
		
		public BigHex() {
			_x = new double[]{0.5*RADIUS, RADIUS, 0.5*RADIUS, -0.5*RADIUS, -RADIUS, -0.5*RADIUS};
			_y = new double[]{0.866*RADIUS, 0, -0.866*RADIUS, -0.866*RADIUS, 0, 0.866*RADIUS};
			for(int i=0; i<6; i++) {
				_x[i] += 750;
				_y[i] += 120;
				_shape.getPoints().addAll(new Double[]{_x[i], _y[i]});
			}
			
			Platform.runLater(new Runnable(){
				public void run() {
					_root.getChildren().add(_shape);
				}
			});

		}
		
		public void draw(int id) {
			if(id > 0) {
				_shape.setFill(new ImagePattern(new Image(Database.tile(id).getPath())));
			} else {
				_shape.setFill(null);
			}
		}
		
	}
	
	private class ButtonHandler implements EventHandler<ActionEvent> {
				
		private String _name;
		private boolean _isCapture;
		
		public ButtonHandler(String name, boolean isCapture) {
			_name = name;
			_isCapture = isCapture;
		}

		@Override
		public void handle(ActionEvent e) {
			if(_isCapture) {
				_client.write(Protocol.PLANET_CHOWN, _name + "\n" + _client.getName());
			} else {
				_client.write(Protocol.PLANET_CHOWN, _name + "\n" + "none");
			}
		}
		
	}
	
	private class CheckBoxHandler implements EventHandler<ActionEvent> {
		
		private String _name;
		private int _index;
		
		public CheckBoxHandler(String name, int index) {
			_name = name;
			_index = index;
		}
		
		@Override
		public void handle(ActionEvent e) {
			if(_spacedocks[_index].isSelected()) {
				_client.write(Protocol.NEW_SDOCK, _name);
			} else {
				_client.write(Protocol.REMOVE_SDOCK, _name);
			}
		}
		
	}
	
}