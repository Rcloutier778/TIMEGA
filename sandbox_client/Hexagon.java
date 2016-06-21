package sandbox_client;

import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;

public class Hexagon {
		
	// useful fields to draw hexagons
	protected static double RADIUS = 40;
	protected static double XOFFSET = 60;
	protected static double YOFFSET = 205;
	
	// usefule fields to draw circles and squares
	protected static double CIRCLE_RADIUS = 12;
	private static int NO_HINTS = 0;
	private static int TOP_LEFT = 1;
	private static int BOTTOM_RIGHT = 2;
	protected static double SIDE_LENGTH = 8;
	
	protected final Polygon _shape = new Polygon();
	
	private double _xCenter, _yCenter;
	private double[] _x, _y;
	private int _id;
	
	private Overlay[] _overlay;
			
	public Hexagon(int u, int v, int id) {
		_id = id;
		
		_xCenter = (3*v*RADIUS)/2;
		_yCenter = (Math.sqrt(3)*u*RADIUS)-(Math.sqrt(3)*v*RADIUS)/2;
		_xCenter += XOFFSET;
		_yCenter += YOFFSET;
		
		this.initPoints();
		
		for(int i=0; i<6; i++) {
			_shape.getPoints().addAll(new Double[]{_x[i], _y[i]});
		}
		
		if(_id > 0) {
			_shape.setFill(new ImagePattern(new Image(Database.tile(_id).getPath())));
		} else {
			// do nothing
		}
		
		this.initOverlay();
						
		_shape.setStrokeWidth(4);
	}
	
	// sets up the six vertices of the hexagon
	private void initPoints() {
		_x = new double[]{0.5*RADIUS, RADIUS, 0.5*RADIUS, -0.5*RADIUS, -RADIUS, -0.5*RADIUS};
		_y = new double[]{0.866*RADIUS, 0, -0.866*RADIUS, -0.866*RADIUS, 0, 0.866*RADIUS};
		for(int i=0; i<6; i++) {
			_x[i] += _xCenter;
			_y[i] += _yCenter;
		}
		
	}
	
	// sets up the overlay
	private void initOverlay() {
		Tile t = Database.tile(_id);
		int numPlanets = t.numPlanets();
		_overlay = new Overlay[numPlanets];
		
		for(int i=0; i < numPlanets; i++) {
			_overlay[i] = new Overlay();
			_overlay[i].name = t.getPlanetName(i);
			_overlay[i].circle = new Circle(CIRCLE_RADIUS);
			_overlay[i].circle.setVisible(false);
			_overlay[i].square = new Rectangle(SIDE_LENGTH, SIDE_LENGTH);
			_overlay[i].square.setStroke(Color.BLACK);
			_overlay[i].square.setVisible(false);
			_overlay[i].border = new Rectangle(SIDE_LENGTH+2, SIDE_LENGTH+2);
			_overlay[i].border.setStroke(Color.WHITE);
			_overlay[i].border.setFill(Color.TRANSPARENT);
			_overlay[i].border.setVisible(false);
			int hints = NO_HINTS;
			if(numPlanets == 2) {
				if(i == 0) {
					hints = TOP_LEFT;
				} else {
					hints = BOTTOM_RIGHT;
				}
			} else if(numPlanets == 3) {
				
			}
			_overlay[i].center(hints);
		}
	}
	
	// draws a border around the hexagon
	public void click() {
		_shape.setStroke(Color.RED);
		_shape.toFront();
		for(int i=0; i < _overlay.length; i++) {
			_overlay[i].circle.toFront();
			_overlay[i].border.toFront();
			_overlay[i].square.toFront();
		}
	}
	
	// clears the border
	public void unclick() {
		_shape.setStroke(null);
	}
	
	// accessors
	public int id() {
		return _id;
	}
	
	public Shape[] overlay() {
		Shape[] output = new Shape[3*_overlay.length];
		for(int i=0; i < _overlay.length; i++) {
			output[3*i] = _overlay[i].circle;
			output[3*i+1] = _overlay[i].square;
			output[3*i+2] = _overlay[i].border;
		}
		return output;
	}
	
	// if b is true, then it colors the contained planets by their owner
	// if b is false, it clears them
	public void paintByPlayer(boolean b) {
		for(int i=0; i < _overlay.length; i++) {
			_overlay[i].circle.setVisible(b);
			if(b) {
				_overlay[i].colorCircle();
			}
		}
	}
	
	// if b is true, then it draws space docks on the contained planets
	// if b is false, it clears them
	public void paintSD(boolean b) {
		for(int i=0; i < _overlay.length; i++) {
			_overlay[i].square.setVisible(b);
			_overlay[i].border.setVisible(b);
			if(b) {
				_overlay[i].colorSquare();
			}
		}
	}
	
	private class Overlay {
		
		public String name;
		public Circle circle;
		public Rectangle square;
		public Rectangle border;
		
		// lots of magic numbers, sorry future david :(
		public void center(int hints) {
			double x = _xCenter;
			double y = _yCenter;
						
			if(hints == TOP_LEFT) {
				x -= 12;
				y -= 12;
			} else if(hints == BOTTOM_RIGHT) {
				x += 12;
				y += 12;
			}
			
			hints = Database.getHints(name);
			
			if(hints == Database.SINGLE_PLANET) {
				y -= 5;
			} else if(hints == Database.TOP_LEFT) {
				y += 5;
				x -= 1;
			} else if(hints == Database.BOTTOM_RIGHT) {
				y -= 11;
				x += 3;
			} else if(name.equals("Mecatol Rex")) {
				circle.setRadius(24);
			} else if(name.equals("Lisis II")) {
				x -= 2;
				y += 2;
				circle.setRadius(8);
			} else if(name.equals("Ragh")) {
				y -= 12;
				circle.setRadius(11);
			} else if(name.equals("Arretze")) {
				y -= 15;
				x -= 4;
			} else if(name.equals("Hercant")) {
				y += 7;
				x -= 12;
			} else if(name.equals("Kamdorn")) {
				x += 16;
			} else if(name.equals("Industrex")) {
				y -= 3;
				circle.setRadius(16);
			} else if(name.equals("Hercalor")) {
				x += 4;
			} else if(name.equals("Tiamat")) {
				x -= 4;
			} else if(name.equals("Cormund")) {
				x += 1;
				y -= 3;
			} else if(name.equals("Hope's End")) {
				circle.setRadius(16);
			} else if(name.equals("Lodor")) {
				x -= 12;
				y -= 12;
			} else if(name.equals("Quann")) {
				x -= 12;
				y -= 12;
			} else if(name.equals("Sumerian")) {
				circle.setRadius(5);
			} else if(name.equals("Tsion")) {
				circle.setRadius(5);
			} else if(name.equals("Ashtroth")) {
				y -= 17;
			} else if(name.equals("Abaddon")) {
				y += 8;
				x -= 14;
			} else if(name.equals("Loki")) {
				x += 14;
				y += 6;
			} else if(name.equals("Rigel I")) {
				y -= 17;
			} else if(name.equals("Rigel III")) {
				y += 8;
				x -= 14;
			} else if(name.equals("Rigel II")) {
				x += 14;
				y += 6;
			}
			
			circle.setCenterX(x);
			circle.setCenterY(y);
			
			square.setLayoutX(x+3);
			square.setLayoutY(y-13);
			
			border.setLayoutX(x+2);
			border.setLayoutY(y-14);
		}
		
		public void colorCircle() {
			circle.setFill(Database.colorOfPlanet(name));
		}
		
		public void colorSquare() {
			if(!Database.hasSD(name)) {
				square.setVisible(false);
				border.setVisible(false);
			} else {
				square.setFill(Database.colorOfPlanet(name));
			}
		}
		
	}
	
}

