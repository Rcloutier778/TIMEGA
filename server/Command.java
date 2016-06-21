package server;

/**
 * Simple interface to translate string arguments into computer instructions
 */

public interface Command {
	
	public void run(String[] args);

}
