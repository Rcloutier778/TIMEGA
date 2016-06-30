package sandbox_client;

/**
 * Reads information from the personnel.xml file and sends the information back to the Database.
 *
 * @author dmayans
 */

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;

public class PersonnelSAXHandler extends DefaultHandler {

	// Called at the beginning of the program to read the personnel.xml file
	public static void generatePersonnel() {
		String s = Main.PATH_TO_ASSETS + "personnel.xml";
		try {
			SAXParser p = SAXParserFactory.newInstance().newSAXParser();
			p.parse(s, new PersonnelSAXHandler());
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private String _string;
	private String _color;
	private String _name;
	private String _effect;
	private String _tier;

	@Override
	public void startDocument() {
		_string = "";
	}

	@Override
	public void startElement(String namespaceURI, String localName, String qName, Attributes atts) {
		if(qName=="person") {
			_color = atts.getValue("color");
			_tier = atts.getValue("tier");
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName) {
		if(qName.equals("name")) {
			_name = _string;
		} else if(qName.equals("effect")) {
			_effect = _string;
		} else if(qName.equals("person")) {
			Database.addPersonnelToDatabase(_name, _color, _effect, _tier);
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