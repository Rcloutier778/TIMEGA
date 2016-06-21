package sandbox_client;

/**
 * Constants used for the protocols
 */

public class Protocol {
	
	public static final int EOM = 0;
	public static final int HELLO = 1;
	public static final int WELCOME = 2;
	public static final int NAME = 3;
	public static final int VALID = 4;
	public static final int INVALID = 5;
	public static final int NEW_PLAYER = 6;
	
	public static final int MAP = 10;
	public static final int EN_MAP = 11;
	public static final int DIS_MAP = 12;
	public static final int EN_PLANETS = 13;
	public static final int DIS_PLANETS = 14;
	public static final int EN_RESEARCH = 15;
	public static final int DIS_RESEARCH = 16;
	public static final int EN_PERSONNEL = 17;
	public static final int DIS_PERSONNEL = 18;
	public static final int EN_EMPIRE = 19;
	public static final int DIS_EMPIRE = 20;
	public static final int EN_STATUS = 21;
	public static final int DIS_STATUS = 22;
	public static final int EN_COUNCIL = 23;
	public static final int DIS_COUNCIL = 24;
	public static final int EN_COMBAT = 25;
	public static final int DIS_COMBAT = 26;


	public static final int PLANET_CHOWN = 30;
	public static final int NEW_SDOCK = 31;
	public static final int REMOVE_SDOCK = 32;
	
	public static final int END_ROUND = 35;
	public static final int SEND_TECH = 36;
	public static final int REMOVE_TECH = 37;
	public static final int SEND_PERSON = 38;
	public static final int REMOVE_PERSON = 39;
	public static final int ADVANCE = 40;
	public static final int ROUND_OK = 41;
	public static final int SEND_RESOLUTION = 42;

}
