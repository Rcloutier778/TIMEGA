package sandbox_client;

/**
 * Displays the current resolutions being voted on. WIP.
 * 
 * @author dmayans
 */

import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;

public class CouncilTab extends AbstractTab {
	
	private Text _name1;
	private Label _pro1;
	private Label _con1;
	private Label _extra1;
	
	private Text _name2;
	private Label _pro2;
	private Label _con2;
	private Label _extra2;

	public CouncilTab(Client client) {
		super(Client.COUNCIL);
		
		Pane pane = new Pane();
		_root.setContent(pane);
		
		_name1 = new Text();
		_name1.setLayoutX(40);
		_name1.setLayoutY(40);
		_name1.setStyle("-fx-font-weight:bold");
		
		_pro1 = new Label();
		_pro1.setWrapText(true);
		_pro1.setMaxWidth(820);
		_pro1.setLayoutX(40);
		_pro1.setLayoutY(60);
		
		_con1 = new Label();
		_con1.setWrapText(true);
		_con1.setMaxWidth(820);
		_con1.setLayoutX(40);
		_con1.setLayoutY(100);
		
		_extra1 = new Label();
		_extra1.setWrapText(true);
		_extra1.setMaxWidth(820);
		_extra1.setLayoutX(40);
		_extra1.setLayoutY(140);
		_extra1.setStyle("-fx-text-fill:#aaa");
		
		_name2 = new Text();
		_name2.setLayoutX(40);
		_name2.setLayoutY(220);
		_name2.setStyle("-fx-font-weight:bold");
		
		_pro2 = new Label();
		_pro2.setWrapText(true);
		_pro2.setMaxWidth(820);
		_pro2.setLayoutX(40);
		_pro2.setLayoutY(240);
		
		_con2 = new Label();
		_con2.setWrapText(true);
		_con2.setMaxWidth(820);
		_con2.setLayoutX(40);
		_con2.setLayoutY(280);
		
		_extra2 = new Label();
		_extra2.setWrapText(true);
		_extra2.setLayoutX(40);
		_extra2.setMaxWidth(820);
		_extra2.setLayoutY(320);
		_extra2.setStyle("-fx-text-fill:#aaa");
		
		pane.getChildren().addAll(_name1, _pro1, _con1, _extra1, _name2, _pro2, _con2, _extra2);
		
	}
	
	public void resolved(String resolution1, String resolution2) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				_name1.setText(resolution1);
				String pro1 = "For: " + Database.getPro(resolution1);
				_pro1.setText(pro1);
				String con1 = "Against: " + Database.getCon(resolution1);
				_con1.setText(con1);
				_extra1.setText(Database.getExtra(resolution1));
						
				_name2.setText(resolution2);
				String pro2 = "For: " + Database.getPro(resolution2);
				_pro2.setText(pro2);
				String con2 = "Against: " + Database.getCon(resolution2);
				_con2.setText(con2);
				_extra2.setText(Database.getExtra(resolution2));
			}
		});
	}
	
}
