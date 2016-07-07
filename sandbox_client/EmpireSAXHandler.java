package sandbox_client;

/**
 * Reads information from the empire.xml file and sends the information back to the Database.
 *
 * @author dmayans
 */

import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import java.io.IOException;

public class EmpireSAXHandler extends DefaultHandler {

	// Called at the beginning of the program to read the personnel.xml file
	public static void generateStages() {
		String s = Main.PATH_TO_ASSETS + "empire.xml";
		try {
			SAXParser p = SAXParserFactory.newInstance().newSAXParser();
			p.parse(s, new EmpireSAXHandler());
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
	private int _command;
	private int _fleet;
	private String _objectives;
	private String _rewards;

	@Override
	public void startDocument() {
		_string = "";
	}

	@Override
	public void endElement(String uri, String localName, String qName) {
		if(qName.equals("name")) {
			_name = _string;
		} else if(qName.equals("command")) {
			if(_string.length() <= 0) {
				_command = -1;
			} else {
				_command = Integer.parseInt(_string);
			}
		} else if(qName.equals("fleet")) {
			if(_string.length() <= 0) {
				_command = -1;
			} else {
				_fleet = Integer.parseInt(_string);
			}
		} else if(qName.equals("objective")) {
			if(_objectives == null) {
				_objectives = new String(_string);
			} else {
				_objectives += " OR " + new String(_string);
			}
		} else if(qName.equals("reward")) {
			_rewards = new String(_string);
		} else if(qName.equals("stage")) {
			Database.addEmpireStage(_name, _command, _fleet, _objectives, _rewards);
			_name = "";
			_command = -1;
			_fleet = -1;
			_objectives = null;
			_rewards = "";
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