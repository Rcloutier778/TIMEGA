package sandbox_client;

/**
 * Holds generic methods for the tabs so that the Client can handle them generically when appropriate
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
	
}
