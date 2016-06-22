package sandbox_client;

/**
 * Reads information from the personnel.xml file and sends the information back to the Database.
 * 
 * @author dmayans
 */

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.*;
import org.xml.sax.helpers.*;

public class ResolutionSAXHandler extends DefaultHandler {
	
	// Called at the beginning of the program to read the resolutions.xml file
	public static void generateResolutions() {
		String s = "TIMEGA/assets/resolutions.xml";
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
	
	private String _string;
	private String _name;
	private String _for;
	private String _against;
	private String _extra;
	
	@Override
	public void startDocument() {
		_string = "";
	}
	
	@Override
	public void startElement(String namespaceURI, String localName, String qName, Attributes atts) {

	}
	
	@Override
	public void endElement(String uri, String localName, String qName) {
		if(qName.equals("name")) {
			_name = _string;
		} else if(qName.equals("for")) {
			_for = _string;
		} else if(qName.equals("against")) {
			_against = _string;
		} else if(qName.equals("extra")) {
			_extra = _string;
		} else if(qName.equals("resolution")) {
			Database.addResolutionToDatabase(_name, _for, _against, _extra);
			_extra = null;
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
