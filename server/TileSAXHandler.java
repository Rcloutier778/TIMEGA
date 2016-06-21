package server;

/**
 * Reads information from the planets.xml file and sends the information back to the database.
 */

import java.util.LinkedList;

import org.xml.sax.*;
import org.xml.sax.helpers.*;

public class TileSAXHandler extends DefaultHandler {
	
	// the reader does NOT guarantee that when it reads, that it has read all of the text available to it.
	// _string stores all the text it has read in a buffer (not efficient, but the reader almost always reads all of
	// the text it can, so _string is almost never rewritten).
	private String _string;
	
	// holds the tiles temporarily
	private LinkedList<Tile> _tiles;
	// points to the tile currently being read in
	private Tile _currTile;
	// int used to index planets as they're read
	private int _currPlanetIndex;
	
	// when doc is opened: initialize variables
	@Override
	public void startDocument() {
		_string = "";
		_tiles = new LinkedList<Tile>();
		_currTile = new Tile();
		_currTile.setTitle("Erase");
		_tiles.addFirst(_currTile);
		_currPlanetIndex = -1;
	}
	
	// on the start of an entry
	@Override
	public void startElement(String namespaceURI, String localName, String qName, Attributes atts) {
		// if it's a new tile, update the _currTile pointer and _currPlanetIndex int
		if(qName=="tile") {
			_currTile = new Tile();
			_currPlanetIndex = -1;
		} else if(qName=="planet") {
			// else if we have a new planet, then tell the tile and increment the index
			_currTile.addPlanet();
			_currPlanetIndex++;
		}
	}
	
	// on the end of an entry
	@Override
	public void endElement(String uri, String localName, String qName) {
		// if that entry has name 'title', then whatever we read is the title- tell the tile
		if(qName=="title") {
			_currTile.setTitle(new String(_string));
		// if that entry has name 'name', then whatever we read has a planet name- tell the planet
		} else if(qName=="name") {
			_currTile.setPlanetName(_currPlanetIndex, new String(_string));
		// if that entry has name 'tile', then whatever we read was the full description of the tile- add it to our list
		} else if(qName=="tile") {
			_tiles.addLast(_currTile);
		} 
		// in any case, _string should be cleared
		_string = "";
	}
	
	// SAXHandler reads more characters between brackets, what to do? just append to _string
	// _string is used then cleared at the end of each element, guaranteeing that all text is read
	@Override
	public void characters(char[] ch, int start, int length) {
		_string += new String(ch, start, length).trim();
	}
	
	// once the document is finished, give the database our list of tiles
	@Override
	public void endDocument() {
		Tile.placeTiles(_tiles);
	}
	
}
