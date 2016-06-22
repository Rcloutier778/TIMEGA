package sandbox_client;

/**
 * Models a tile- the information about it (what groups it belongs in, planetary information), etc.
 * Also keeps track of its image pathname for Hexagons to paint.
 * 
 * @author dmayans
 */

import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.util.LinkedList;

public class Tile {
			
	// Called at the beginning of the program to read the planets.xml file
	public static void generateTiles() {
		String s = "TIMEGA/assets/planets.xml";
		try {
			SAXParser p = SAXParserFactory.newInstance().newSAXParser();
			p.parse(s, new SAXHandler());
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	// Called after reading the planets.xml file to place the tiles in their array
	public static void placeTiles(LinkedList<Tile> tiles) {
		for(Tile t : tiles) {
			Database.addTile(t);
		}
	}
	
	// General information about the tile
	private String _path;
	private String _title;
	
	private Planet[] _planets = {};
	
	// tile accessors
	public String getPath() {
		return "file:assets/Systems/" + _path;
	}
	
	public String getTitle() {
		return _title;
	}
	
	// tile mutators
	public void setPath(String path) {
		_path = path;
	}
	
	public void setTitle(String title) {
		_title = title;
	}
	
	// planet accessors
	public String getPlanetName(int index) {
		if(index >= _planets.length || index < 0) 
			return null;
		return _planets[index].name;
	}
	
	public int hasPlanet(String name) {
		for(int i = 0; i < _planets.length; i++) {
			if(_planets[i].name.equals(name)) {
				return i;
			}
		}
		return -1;
	}
	
	public int getPlanetResources(int index) {
		if(index >= _planets.length || index < 0) {
			return -1;
		}
		return _planets[index].resources;
	}
	
	public int getPlanetInfluence(int index) {
		if(index >= _planets.length || index < 0) 
			return -1;
		return _planets[index].influence;
	}
	
	public int numPlanets() {
		return _planets.length;
	}
	
	
	// planet mutators
	public void addPlanet() {
		int newlength = _planets.length + 1;
		Planet[] newplanets = new Planet[newlength];
		for(int i=0; i<_planets.length; i++) {
			newplanets[i] = _planets[i];
		}
		newplanets[_planets.length] = new Planet();
		_planets = newplanets;
	}
	
	public void setPlanetName(int index, String name) {
		Database.addPlanetName(name);
		_planets[index].name = name;
	}
	
	public void setPlanetResources(int index, String resources) {
		int r = Integer.parseInt(resources);
		Database.setResources(_planets[index].name, r);
		_planets[index].influence = r;
	}
	
	public void setPlanetInfluence(int index, String influence) {
		int i = Integer.parseInt(influence);
		Database.setInfluence(_planets[index].name, i);
		_planets[index].influence = i;
	}
	
	public void addTech(int index, int tech) {
		Database.addTech(_planets[index].name, tech);
	}
	
	
	private class Planet {
		
		public String name;
		public int resources;
		public int influence;
				
	}
	
}
