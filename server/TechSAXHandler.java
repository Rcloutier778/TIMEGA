package server;

/**
 * Reads information from the technology.xml file and sends the information back to the database.
 * 
 * @author dmayans
 */

import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.util.LinkedList;

public class TechSAXHandler extends DefaultHandler {
	
	// Called at the beginning of the program to read the technology.xml file
	public static void generateTech() {
		String s = sandbox_client.Main.PATH_TO_ASSETS + "technology.xml";
		try {
			SAXParser p = SAXParserFactory.newInstance().newSAXParser();
			p.parse(s, new TechSAXHandler());
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	// the reader does NOT guarantee that when it reads, that it has read all of the text available to it.
	// _string stores all the text it has read in a buffer (not efficient, but the reader almost always reads all of
	// the text it can, so _string is almost never rewritten).
	private String _string;
	
	// holds the techs temporarily
	private LinkedList<String> _tech;
	
	// when doc is opened: initialize variables
	@Override
	public void startDocument() {
		_string = "";
		_tech = new LinkedList<String>();
	}
	
	// on the end of an entry
	@Override
	public void endElement(String uri, String localName, String qName) {
		// if that entry has name 'name', then whatever we read is the name of a tech
		if(qName=="name") {
			_tech.addLast(_string);
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
	
	// once the document is finished, give the database our list of techs
	@Override
	public void endDocument() {
		ServerDatabase.placeTech(_tech);
	}
	
}
