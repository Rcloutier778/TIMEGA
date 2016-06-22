package server;

/**
 * Thread that waits for new clients to connect and then forwards them to the main thread.
 * 
 * @author dmayans
 */

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class ClientListener implements Runnable {
	
	// haven't seen a need to use any other port yet
	public static final int PORT = 4040;
	
	private ServerSocket _server;
	private Main _main;

	public ClientListener(Main main) {
		
		_main = main;
		
		// try to open up a server socket at the predefined port
		try {
			_server = new ServerSocket(PORT);
		} catch (IOException e) {
			// if there's an error, print it and quit
			Main.writeColortext("error establishing server at port " + Integer.toString(PORT), Main.ERROR);
			System.exit(0);
		}
		
		// otherwise, indicate that the server can accept clients
		Main.writeColortext("connection opened", Main.SERVEROUT);
	}
	
	// cancel the thread quietly
	public void cancel() {
		try {
			_server.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		Main.writeColortext("cancel request sent", Main.SERVEROUT);
	}
	
	// listen for new clients, give each its own thread, and pass the data back to the main thread
	@Override
	public void run() {
		
		// count is incremented and used to give each client a temporary human-readable name until it identifies itself
		int count = 1;
		
		while(true) {
			
			try {
				// try to connect a new client
				Socket socket = _server.accept();
				ClientThread client = new ClientThread(socket, count++, _main);
				// if the main thread is rejecting clients, close the client
				// addClient should return true if the client was added successfully, false otherwise
				if(!_main.addClient(client)) {
					client.close();
				} else {
					// start the client thread
					new Thread(client).start();
				}
				
			} catch (SocketException e) {
				// if the _server is closed (from this.cancel()), then quietly kill the server thread
				Main.writeColortext("server socket closed", Main.SERVEROUT);
				return;
				
			} catch (IOException e) {
				// uh oh
				e.printStackTrace();
			}
			
		}
		
	}

}
