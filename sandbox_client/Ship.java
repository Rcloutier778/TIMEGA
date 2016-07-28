package sandbox_client;

/** 
 * 
 * 
 * @author dmayans
 */

public class Ship {

	public String name;
	
	public int hitrate;
	public int dice;
	
	// only used for flagships
	public int movement;
	public int capacity;
	
	public String ability;
	
	public Ship() {}
	
	public Ship(Ship s) {
		this.name = new String(s.name);
		this.hitrate = s.hitrate;
		this.dice = s.dice;
		this.movement = s.movement;
		this.capacity = s.capacity;
		this.ability = new String(s.ability);
	}
	
}
