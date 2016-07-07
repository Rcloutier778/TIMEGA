package sandbox_client;

/**
 * Modified TextField that only accepts numbers (used to prevent ParseExceptions)
 * 
 * @author dmayans
 */

import javafx.scene.control.TextField;

public class NumberTextField extends TextField {
	
	@Override
	public void replaceText(int start, int end, String text) {
		if(validate(text))
			super.replaceText(start, end, text);
	}

	@Override
	public void replaceSelection(String text) {
		if(validate(text))
			super.replaceSelection(text);
	}
	
	private boolean validate(String text) {
		return text.matches("[0-9]*");
	}
	
	public int getNumber() {
		if(this.getText() == null || this.getText().equals("")) {
			return -1;
		}
		return Integer.parseInt(this.getText());
	}
	
}
