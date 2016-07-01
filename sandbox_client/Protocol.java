package sandbox_client;

/**
 * Constants used for communication between the client and server. Usage is given for each.
 * 
 * @author dmayans
 */

public class Protocol {
	
	public static final int EOM = 0; // depricated
	public static final int HELLO = 1; // usage: HELLO
	public static final int WELCOME = 2; // usage: WELCOME
	public static final int NAME = 3; // usage: NAME <name>
	public static final int VALID = 4; // usage: VALID
	public static final int INVALID = 5; // usage: INVALID
	public static final int NEW_PLAYER = 6; // depricated (usage: NEW_PLAYER <name> <race> <r> <g> <b>)
	
	public static final int MAP = 10; // usage: MAP <name> <data as ints>
	
	public static final int ENABLE = 11; // usage: ENABLE <tab>
	public static final int DISABLE = 12; // usage: DISABLE <tab>

	public static final int PLANET_CHOWN = 30; // usage: PLANET_CHOWN <planet name> <new owner>
	public static final int NEW_SDOCK = 31; // usage: NEW_SDOCK <planet name>
	public static final int REMOVE_SDOCK = 32; // usage: REMOVE_SDOCK <planet name>
	
	public static final int END_ROUND = 35; // usage: END_ROUND (triggers a lot of responses)
	public static final int SEND_TECH = 36; // usage: SEND_TECH <player> <tech>
	public static final int REMOVE_TECH = 37; // usage: REMOVE_TECH <player> <tech>
	public static final int SEND_PERSON = 38; // usage: SEND_PERSON <player> <personnel>
	public static final int REMOVE_PERSON = 39; // usage: REMOVE_PERSON <player> <personnel>
	public static final int ADVANCE = 40; // usage: ADVANCE <name> <color> (TODO remove color)
	public static final int ROUND_OK = 41; // usage: ROUND_OK (indicates that all responses have been received)
	public static final int SEND_RESOLUTION = 42; // usage: SEND_RESOLUTION <resolution 1 name> <resolution 2 name>

}
