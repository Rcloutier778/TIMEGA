package server;

/**
 * Simple interface to translate string arguments into computer instructions
 * 
 * @author dmayans
 */

public interface Command {
	
	public void run(String[] args);

}
