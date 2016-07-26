package sandbox_client;

/**
 * Displays the current resolutions being voted on. WIP.
 * 
 * @author dmayans
 */
//todo make repeal work
//todo make revote work


import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;

public class CouncilTab extends AbstractTab {
	
	private Text _name1;
	private Label _pro1;
	private Label _con1;
	private Label _extra1;
	private Label _voteLabel1;
	private ComboBox<String> _vote1;
	private NumberTextField _numVotes1;
	private Button _send1;
	
	private Text _name2;
	private Label _pro2;
	private Label _con2;
	private Label _extra2;
	private Label _voteLabel2;
	private ComboBox<String> _vote2;
	private NumberTextField _numVotes2;
	private Button _send2;

	private Label _turnOrder;


	private Text _pastTitle;
	private Text _pastResText;

	public CouncilTab() {
		super(Client.COUNCIL);

		_pastTitle = new Text("");
		_pastResText = new Text("");
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

		//todo readjust ui
		//todo make Server file in sandbox count up the votes and send to server database, will need new protocol
		_voteLabel1 = new Label("Vote:");
		_voteLabel1.setLayoutX(40);
		_voteLabel1.setLayoutY(180);

		_vote1 = new ComboBox<>();
		_vote1.setLayoutX(80);
		_vote1.setLayoutY(180);
		_vote1.getItems().addAll("For","Against");

		_numVotes1 = new NumberTextField();
		_numVotes1.setPromptText("# of Votes");
		_numVotes1.setLayoutX(180);
		_numVotes1.setLayoutY(180);
		_numVotes1.setMaxWidth(80);

		_send1 = new Button("Send");
		_send1.setLayoutX(260);
		_send1.setLayoutY(180);
		_send1.setOnAction(e->{
			if(_vote1.getValue()!=null && _numVotes1.getNumber() >=0 ){
				Database.localVote(0, _vote1.getValue(), _numVotes1.getNumber());
				_vote1.setDisable(true);
				_numVotes1.setDisable(true);
				_send1.setDisable(true);

			}
		});


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

		_voteLabel2 = new Label("Vote:");
		_voteLabel2.setLayoutX(40);
		_voteLabel2.setLayoutY(360);

		_vote2 = new ComboBox<>();
		_vote2.setLayoutX(80);
		_vote2.setLayoutY(360);
		_vote2.getItems().addAll("For","Against");

		_numVotes2 = new NumberTextField();
		_numVotes2.setPromptText("# of Votes");
		_numVotes2.setLayoutX(180);
		_numVotes2.setLayoutY(360);
		_numVotes2.setMaxWidth(80);

		_send2 = new Button("Send");
		_send2.setLayoutX(260);
		_send2.setLayoutY(360);
		_send2.setOnAction(e->{
			if(_vote2.getValue()!=null && _numVotes2.getNumber() >=0 ){
				Database.localVote(1, _vote2.getValue(), _numVotes2.getNumber());
				_vote2.setDisable(true);
				_numVotes2.setDisable(true);
				_send2.setDisable(true);
			}
		});

		_pastTitle.setLayoutX(40);
		_pastTitle.setLayoutY(400);
		_pastTitle.setStyle("-fx-font-weight:bold");

		_pastResText.setLayoutX(40);
		_pastResText.setLayoutY(420);
		_pastResText.maxWidth(820);
		_pastResText.setWrappingWidth(820);

		_turnOrder = new Label();
		_turnOrder.setLayoutX(625);
		_turnOrder.setLayoutY(40);


		pane.getChildren().addAll(_name1, _pro1, _con1, _extra1, _name2, _pro2, _con2, _extra2, _pastTitle, _pastResText,
				_voteLabel1,_vote1,_numVotes1,_send1,_voteLabel2,_vote2,_numVotes2,_send2, _turnOrder);
		
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
				_vote1.setDisable(false);
				_numVotes1.setDisable(false);
				_send1.setDisable(false);
						
				_name2.setText(resolution2);
				String pro2 = "For: " + Database.getPro(resolution2);
				_pro2.setText(pro2);
				String con2 = "Against: " + Database.getCon(resolution2);
				_con2.setText(con2);
				_extra2.setText(Database.getExtra(resolution2));
				_vote2.setDisable(false);
				_numVotes2.setDisable(false);
				_send2.setDisable(false);
			}
		});
	}

	public void result() {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				_pastTitle.setText("Past Resolutions");
				String display = "";
				for(String resolution: Database.resolutionKeys()){
					if(Database.getRes(resolution).equals("for")){
						display += (resolution + ": " + Database.getPro(resolution) + "\n");
					}else if(Database.getRes(resolution).equals("against")){
						display += (resolution + ": " + Database.getCon(resolution) + "\n");
					}
				}
				System.out.println(Database.resolutionKeys());
				System.out.println(Database.getTurnOrder());
				_pastResText.setText(display);
				_turnOrder.setText(Database.getTurnOrder());
			}
		});
	}
}
