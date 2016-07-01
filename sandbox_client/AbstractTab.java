package sandbox_client;

/**
 * Holds common functionality for the tabs so that the Client can handle them when appropriate
 * 
 * @author dmayans
 */

import javafx.scene.control.Tab;

public abstract class AbstractTab {

	protected final Tab _root = new Tab();
	
	public AbstractTab(int index) {
		_root.setClosable(false);
		_root.setDisable(true);
		_root.setText(Client.TAB_NAMES[index]);
	}
	
	public void addNames() {}; // this method will be called as soon as the client is aware of the list of other players
	public void localName(String name) {}; // this method will be called after addNames, as soon as the server has validated the local player's name
	
}
