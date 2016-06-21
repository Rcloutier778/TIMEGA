package server;

/**
 * Models a tile and its planet names
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
		String s = System.getProperty("user.dir") +"/assets/planets.xml";
		try {
			SAXParser p = SAXParserFactory.newInstance().newSAXParser();
			p.parse(s, new TileSAXHandler());
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
			ServerDatabase.TILES.add(t);
		}
	}
	
	// General information about the tile
	private String _title;
	
	private String[] _planets = {};
	
	// tile accessors
	public String getTitle() {
		return _title;
	}
	
	// tile mutators
	public void setTitle(String title) {
		_title = title;
	}
	
	// planet accessors
	public String getPlanetName(int index) {
		if(index >= _planets.length || index < 0) 
			return null;
		return _planets[index];
	}

	
	
	// planet mutators
	public void addPlanet() {
		int newlength = _planets.length + 1;
		String[] newplanets = new String[newlength];
		for(int i=0; i<_planets.length; i++) {
			newplanets[i] = _planets[i];
		}
		_planets = newplanets;
	}
	
	public void setPlanetName(int index, String name) {
		_planets[index] = name;
		
		ServerDatabase.PLANETS.put(name, "none");
		ServerDatabase.SPACEDOCKS.put(name, false);
		
	}
	
}
