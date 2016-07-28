package sandbox_client;

/**
 * Reads information from the ships.xml file and sends the information back to the Database.
 *
 * @author dmayans
 */

import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import java.io.IOException;

public class ShipSAXHandler extends DefaultHandler {

	// Called at the beginning of the program to read the personnel.xml file
	public static void generateShips() {
		String s = Main.PATH_TO_ASSETS + "ships.xml";
		try {
			SAXParser p = SAXParserFactory.newInstance().newSAXParser();
			p.parse(s, new ShipSAXHandler());
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private String _string;
	private String _name;
	private String _race;
	private int _hitRate;
	private int _dice;
	private int _movement;
	private int _capacity;
	private String _ability;
	
	@Override
	public void startDocument() {
		_string = "";
		_name = "";
		_race = "";
		_ability = "";
	}

	@Override
	public void endElement(String uri, String localName, String qName) {
		if(qName.equals("name")) {
			_name = new String(_string);
		} else if(qName.equals("race")) {
			_race = new String(_string);
		} else if(qName.equals("hitrate")) {
			_hitRate = Integer.parseInt(_string);
		} else if(qName.equals("dice")) {
			_dice = Integer.parseInt(_string);
		} else if(qName.equals("movement")) {
			_movement = Integer.parseInt(_string);
		} else if(qName.equals("capacity")) {
			_capacity = Integer.parseInt(_string);
		} else if(qName.equals("ability")) {
			_ability = new String(_string);
		} else if(qName.equals("ship")) {
			Database.addShip(_name, _hitRate, _dice);
			_name = "";
			_hitRate = -1;
			_dice = -1;
		} else if(qName.equals("flagship")) {
			Database.addFlagship(_name, _race, _hitRate, _dice, _movement, _capacity, _ability);
		}
		_string = "";
	}

	@Override
	public void characters(char[] ch, int start, int length) {
		_string += new String(ch, start, length).trim();
	}

	@Override
	public void endDocument() {

	}

}