package sandbox_client;

import javafx.application.Platform;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;

import java.math.BigDecimal;
import java.util.HashMap;


/**
 * Created by Richard on 6/14/2016.
 * Combat Simulations.
 */


//todo: Remember all techs are most updated on pdf, not website
/**
 * TODO:
 * 1) Flagships
 * 2) Race abilities
 * 3) Mentak black market
 * 4) Xeno Psychology
 * 5) SAS
 * 6) Mercs
 * 7) Make Naalu shields its own method?
 * 8) Turn the start button into a next button, have extra pane appear over the units pane, then make a start button on extra pane
 /**
 * Personnel
 * 2) Advisor   --Checkbox
 * 5) Explorer  --Implement carriers first
 */

/**

 * Simulator:
 2) Carriers should appear in the simulator since they can absorb hits. The ships should be ordered "fighters, cruisers, SASs, carriers, dreadnoughts, flagships, war suns".
 4) You should definitely underline the letters of the ship name instead of putting it in parentheses

 Council Room
 1) This tab may need to be rewritten. When rewriting a tab, lay out the UI on paper first, then code in the UI, and then start adding in functionality. This will save you hours and me a headache.
 2) We don't care about all past resolutions, only the ones still in play (note that some stay in play for a single round and then disappear)
 4) Players should be able to cast votes from this tab as well (this is a big update, hence why it may need to be rewritten).

 Server
 3) the server should be track total votes, reset them whenever new resolutions are sent, and be able to report the votes for each resolution on command and the turn order on command. the turn order should be sent to each player.
 4) the server should also keep track of the total votes cast by each player (for statistics purposes)
 5) the server should save the resolutions in play, the resolutions under deliberation, the votes cast for the current resolutions by player, and the total votes cast be each player
 6) the server should be able to load the information in the above step



 Finally, the simulator should be able to handle this (exhaustive) list of all combat effects:

 Technology
 Xeno Psychology*

 Personnel
 Advisor*
 Explorer

 Agendas
 War Funding (against)

 Races
 Barony (2*)
 Embers (3)
 Emirates (fortune cards 7*, 11*, and 14*, can be owned by any player)
 Federation (4*, 5)
 Ghosts (5*)
 L1Z1X (3*)
 Mentak (3, also black market 1* and 6*)
 Naalu (3, 4*, you can assume that shields will always be taken if nothing can sustain damage)
 Sardakk (1*)
 Jol-Nar (1*)
 Winnu (2)
 Xxcha (incomplete)
 Yin (incomplete)
 Yssaril (1)
 Other
 Mercenaries (not in rulebook yet)
 Flagships (not in database yet)
 Everything marked with an asterisk will require more information than the database has. Everything else should be handled silently.
 Recommendations:
 - leave the results text in permanently (on the right half of the screen), and just change the output numbers when the simulation is run
 - run the simulator immediately after someone finishes editing the box instead of having to hit the button (the simulation is fast enough anyway)
 - allow the user to change each ATTACKER's loss policy; while I agree that yours should be the default, that customization is something that I know that Dan and I would be disappointed to miss out on

 Further work:
 - flagships are starting to roll in, I'll let you know when the database can handle it
 - add in SASs (don't do this yet, wait until they appear in the rulebook)
 - make sure the simulator is appropriately handling the current personnel and technology (it should be pretty stable right now, only changing stuff with SASs in a bit)

 Some comments:
 - the text on the right goes off the screen

 - "targeting order" is cut off

 - the grid layout doesn't work really well with the boxes and ship names. Try aligning the ship names to the right?

 */

public class CombatSimTab extends AbstractTab {

    // Units fields
    private NumberTextField[][] _unitFields = new NumberTextField[2][Database.NUM_SHIPS];

    // number of each unit
    private int[][] _unitCounts = new int[2][Database.NUM_SHIPS];

    // damage values of each unit
    private int[][] _unitHitRate = {Database.getBaseHitRates(), Database.getBaseHitRates()};

    // dice rolled by each unit
    private int[][] _unitDice = {Database.getBaseDiceRolled(), Database.getBaseDiceRolled()};

    private static final int ATTACKER = 0;
    private static final int DEFENDER = 1;

    @SuppressWarnings("unchecked")
    private ComboBox<String>[] _eOptions = new ComboBox[]{new ComboBox<String>(), new ComboBox<String>()};

    //If defender is in a nebula
    private CheckBox _defNeb = new CheckBox();

    //name of ATTACKER(0) and DEFENDER(1)
    private String[] _names = {"Attacker", "Defender"};

    //Striker Fleets Movement
    private NumberTextField _strikerField = new NumberTextField();

    //Targeting order Field
    private TextField[] _targetOrderField = {new TextField(),new TextField()};

    //Targeting order
    private int[][] _targetOrder = new int[2][7];

    //Extras pane
    private GridPane _extraPane = new GridPane();

    //Start button
    private Button _start = new Button("Start");

    //Number of racial abilities in the game (total, regardless of who is playing)
    private static final int _racialNumber = 12;

    //Checkbox for all racial abilities in play
    private CheckBox[][] _raceButton = new CheckBox[2][_racialNumber];

    //Number field for all racial abilities in play
    private NumberTextField[][] _raceNumberField = new NumberTextField[2][_racialNumber];

    //Text field for all racial abilities in play
    private TextField[][] _raceField = new TextField[2][_racialNumber];

    //Contains the T/F and # value associated with a racial ability
    //Key = String, Values = int. 0=false, 1=true, other==embers
    //Creuss, L1Z1X, Mentak
    private HashMap<String,Integer>[] _raceEffects = new HashMap[2];

    //Button to show extras pane
    private Button _extraButton = new Button("Next");

    //If cultist effect has been applied to a ship or not
    private boolean[][] _cultist;

    //Hit rates at the beginning of combat
    private int[][] _startHitRate;

    //Counter for the Jol-Nar combat penalty
    private int[] _jolCounter = {0,0,0,0,0,0};

    //boolean for Jol-Nar combat penalty
    private boolean[] _jolPenalty = {false, false, false, false, false, false};

    private GridPane _pane = new GridPane();

    //make values cleared when you click on another tab
    public CombatSimTab() {
        super(Client.SIMULATOR);
        _root.setClosable(false);
        GridPane _scenepane = new GridPane();
        _root.setContent(_scenepane);

        Text results = new Text();
        results.setText("Enter data to view results");
        Text resultTitle = new Text();
        resultTitle.setText("Result of the battle:");

        Label[] _unitLabels = new Label[6];
        String[] _targetOrderNames = {" (f):", " (d):", " (c):", " (n):", " (w):", ":"};

        for(int k=0; k<Database.NUM_SHIPS; k++){
            //Make new Number Text Fields and labels
            _unitFields[ATTACKER][k] = new NumberTextField();
            _unitFields[DEFENDER][k] = new NumberTextField();
            _unitLabels[k] = new Label();

            //Set the label names
            _unitLabels[k].setText(Database.nameOfShip(k) + "s" + _targetOrderNames[k]);

            //Set max width
            _unitFields[ATTACKER][k].setMaxWidth(90);
            _unitFields[DEFENDER][k].setMaxWidth(90);

            //Add things to the grid
            _pane.add(_unitLabels[k], 1, k + 2);
            GridPane.setHalignment(_unitLabels[k], HPos.RIGHT);
            _pane.add(_unitFields[ATTACKER][k], 2, k + 2);
            _pane.add(_unitFields[DEFENDER][k], 3, k + 2);
        }







        GridPane[] _namePane = {new GridPane(), new GridPane()};

        //Attacker combobox
        _namePane[ATTACKER].add(new Text("Attacker"),1,1);
        _namePane[ATTACKER].add(_eOptions[ATTACKER],1,2);

        //Defender combobox
        _namePane[DEFENDER].add(new Text("Defender"), 1, 1);
        _namePane[DEFENDER].add(_eOptions[DEFENDER],1,2);

        //start button
        _start.setOnAction(e ->
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        _pane.setVisible(true);
                        _extraPane.setVisible(false);
                        results.setText("\n".concat(combatSim()));
                    }
                }));

        GridPane.setHalignment(_start, HPos.CENTER);

        _pane.setAlignment(Pos.CENTER);

        //Results pane
        GridPane resultsPane = new GridPane();
        resultsPane.add(resultTitle, 1, 1);
        resultsPane.add(results, 1, 2);
        resultsPane.setMinWidth(300);
        resultsPane.setVgap(10);

        //Button to show extra pane
        _extraPane.setVisible(false);
        _extraButton.setOnAction(e-> {
            if(_eOptions[ATTACKER].getValue() == null || _eOptions[DEFENDER].getValue() == null){
                results.setText("Choose name of Attacker or Defender");
            }
            else {
                _names[ATTACKER] = _eOptions[ATTACKER].getValue();
                _names[DEFENDER] = _eOptions[DEFENDER].getValue(); // gets the value selected by the combobox
                setRaceEffects();
                _pane.setVisible(false);
                _extraPane.setVisible(true);
            }
        });
        _pane.add(_extraButton, 2, 10);

        //Pane gap
        _pane.setVgap(20);
        _pane.setHgap(60);

        //ATTACKER and enemey names
        _pane.add(_namePane[ATTACKER], 2, 1);
        _pane.add(_namePane[DEFENDER], 3, 1);

        _scenepane.add(resultsPane, 1, 1);
        _scenepane.add(_extraPane, 0, 1);
        _scenepane.add(_pane, 0, 1);
        _scenepane.setPadding(new Insets(0,180,60,0));
        _scenepane.setHgap(60);
        _scenepane.setAlignment(Pos.CENTER);
    }

    /**
     * Waits until database is synced to get names
     */
    @Override
    public void localName(String name) {
        // Gets the name of the players
        if (_eOptions[0].getItems().isEmpty()) {
            for (int i = 0; i < Database.numPlayers(); i++) {
                _eOptions[ATTACKER].getItems().add(Database.getPlayer(i).name);
                _eOptions[DEFENDER].getItems().add(Database.getPlayer(i).name);
            }
        }
    }

    /**
     * Sets up the race fields and buttons in extraPane
     */
    public void setRaceEffects(){
        _extraPane.getChildren().clear();

        _extraPane.add(new Label(_names[ATTACKER] + "\nExtras"),2,1);
        _extraPane.add(new Label(_names[DEFENDER] + "\nExtras"),3,1);
        _extraPane.setHgap(60);
        _extraPane.setVgap(20);

        //Striker Fleets
        _extraPane.add(new Label("Striker Fleets"), 1, 2);
        _extraPane.getChildren().get(2).setOnMouseEntered(e -> Tooltip.install(_extraPane.getChildren().get(2), new Tooltip("Number of extra movement \nfor Striker Fleets")));
        _strikerField.setMaxWidth(90);
        _extraPane.add(_strikerField,2,2);

        //Nebula
        _extraPane.add(new Label("In Nebula"), 1, 3);
        _extraPane.getChildren().get(4).setOnMouseEntered(e -> Tooltip.install(_extraPane.getChildren().get(4), new Tooltip("If defender is in a nebula")));
        _defNeb.setAlignment(Pos.CENTER);
        _extraPane.add(_defNeb, 3, 3);

        _extraPane.setHgap(20);
        _extraPane.setVgap(20);
        GridPane.setHalignment(_defNeb, HPos.CENTER);

        Label[] _raceLabels = new Label[_racialNumber];
        int _rowOffset = 4;

        //Creuss
        if (Database.raceOf(_names[DEFENDER]).equals("The Ghosts of Creuss")) {
            _raceLabels[0] = new Label("Wormhole");
            _raceLabels[0].setTooltip(new Tooltip("Are the Creuss fighting\nin a wormhole system?"));
            _extraPane.add(_raceLabels[0], 1, _rowOffset);
            _raceButton[DEFENDER][0] = new CheckBox();
            _extraPane.add(_raceButton[DEFENDER][0], 3, _rowOffset);
            GridPane.setHalignment(_raceButton[DEFENDER][0], HPos.CENTER);
            _rowOffset++;
        }

        //L1Z1X
        if (Database.raceOf(_names[ATTACKER]).equals("The L1Z1X Mindnet") || Database.raceOf(_names[DEFENDER]).equals("The L1Z1X Mindnet")){
            _raceLabels[1] = new Label("L1Z1X");
            _raceLabels[1].setTooltip(new Tooltip("Number of adjacent systems\nwith a friendly dreadnought"));
            _extraPane.add(_raceLabels[1], 1, _rowOffset);
            for (int i = 0; i < 2; i++) {
                if(Database.raceOf(_names[i]).equals("The L1Z1X Mindnet")) {
                    _raceNumberField[i][0] = new NumberTextField();
                    _raceNumberField[i][0].setMaxWidth(80);
                    _extraPane.add(_raceNumberField[i][0], i + 2, _rowOffset);
                }
            }
            _rowOffset++;
        }

        //Mentak
        if (Database.raceOf(_names[ATTACKER]).equals("The Mentak Coalition") || Database.raceOf(_names[DEFENDER]).equals("The Mentak Coalition")){
            _raceLabels[2] = new Label("Mentak");
            _raceLabels[2].setTooltip(new Tooltip("Pre-Combat ability.\nList in the same way\nas targeting order."));
            _extraPane.add(_raceLabels[2], 1, _rowOffset);
            for (int i = 0; i < 2; i++) {
                if(Database.raceOf(_names[i]).equals("The Mentak Coalition")) {
                    _raceField[i][0] = new TextField();
                    _raceField[i][0].setMaxWidth(80);
                    _extraPane.add(_raceField[i][0], i + 2, _rowOffset);
                }
            }
            _rowOffset++;
        }

        //Naalu
        if (Database.raceOf(_names[ATTACKER]).equals("The Naalu Collective") || Database.raceOf(_names[DEFENDER]).equals("The Naalu Collective")){
            _raceLabels[3] = new Label("Naalu");
            _raceLabels[3].setTooltip(new Tooltip("Number of shields"));
            _extraPane.add(_raceLabels[3], 1, _rowOffset);
            for (int i = 0; i < 2; i++) {
                if(Database.raceOf(_names[i]).equals("The Naalu Collective")) {
                    _raceNumberField[i][1] = new NumberTextField();
                    _raceNumberField[i][1].setMaxWidth(80);
                    _extraPane.add(_raceNumberField[i][1], i + 2, _rowOffset);
                }
            }
            _rowOffset++;
        }

        //Sardakk
        if (Database.raceOf(_names[ATTACKER]).equals("The Sardakk N'Orr") || Database.raceOf(_names[DEFENDER]).equals("The Sardakk N'Orr")){
            _raceLabels[4] = new Label("Sardakk");
            _raceLabels[4].setTooltip(new Tooltip("Exhaust a spacedock"));
            _extraPane.add(_raceLabels[4], 1, _rowOffset);
            for (int i = 0; i < 2; i++) {
                if(Database.raceOf(_names[i]).equals("The Sardakk N'Orr")) {
                    _raceButton[i][1] = new CheckBox();
                    _extraPane.add(_raceButton[i][1], i + 2, _rowOffset);
                    GridPane.setHalignment(_raceButton[i][1], HPos.CENTER);
                }
            }
            _rowOffset++;
        }

        //Jol-Nar
        if (Database.raceOf(_names[ATTACKER]).equals("The Universities of Jol-Nar") || Database.raceOf(_names[DEFENDER]).equals("The Universities of Jol-Nar")){
            _raceLabels[5] = new Label("Jol-Nar");
            _raceLabels[5].setTooltip(new Tooltip("Ships that the Jol-Nar\nhave a combat penalty on.\nList in the same way\nas targeting order.\nS for SAS"));
            _extraPane.add(_raceLabels[5], 1, _rowOffset);
            for (int i = 0; i < 2; i++) {
                if(Database.raceOf(_names[i]).equals("The Universities of Jol-Nar")) {
                    _raceField[i][1] = new TextField();
                    _raceField[i][1].setMaxWidth(80);
                    _extraPane.add(_raceField[i][1], i + 2, _rowOffset);
                }
            }
            _rowOffset++;
        }

        //Hacan
        if (Database.raceOf(_names[ATTACKER]).equals("The Emirates of Hacan") || Database.raceOf(_names[DEFENDER]).equals("The Emirates of Hacan")){
            _raceLabels[6] = new Label("Hacan");
            _raceLabels[6].setTooltip(new Tooltip("Fighters +1"));
            _extraPane.add(_raceLabels[6], 1, _rowOffset);

            _raceLabels[7] = new Label("Hacan (cont.)");
            _raceLabels[7].setTooltip(new Tooltip("AFB +1"));
            _extraPane.add(_raceLabels[7], 1, _rowOffset+1);

            _raceLabels[8] = new Label("Hacan (cont.)");
            _raceLabels[8].setTooltip(new Tooltip("War Suns +2"));
            _extraPane.add(_raceLabels[8], 1, _rowOffset+2);
            for(int i=0; i<2; i++) {
                if (Database.raceOf(_names[i]).equals("The Emirates of Hacan")) {
                    for (int k = 0; k < 3; k++) {
                        _raceButton[i][2] = new CheckBox();
                        _extraPane.add(_raceButton[i][k+2], i + 2, _rowOffset);
                        GridPane.setHalignment(_raceButton[i][k+2], HPos.CENTER);
                        _rowOffset++;
                    }
                }
            }
        }

        //Federation
        if (Database.raceOf(_names[ATTACKER]).equals("The Federation of Sol") || Database.raceOf(_names[DEFENDER]).equals("The Federation of Sol")){
            _raceLabels[9] = new Label("Sol");
            _raceLabels[9].setTooltip(new Tooltip("Number of Ground Forces"));
            _extraPane.add(_raceLabels[9], 1, _rowOffset);
            for (int i = 0; i < 2; i++) {
                if(Database.raceOf(_names[i]).equals("The Federation of Sol")) {
                    _raceNumberField[i][2] = new NumberTextField();
                    _raceNumberField[i][2].setMaxWidth(90);
                    _extraPane.add(_raceNumberField[i][2], i + 2, _rowOffset);
                }
            }
            _rowOffset++;
        }


        //Targeting order Label
        _raceLabels[11] = new Label();
        _raceLabels[11].setText("Targeting Order:");
        _raceLabels[11].setTooltip(new Tooltip("Use capital letters for sustains.\nDefault order is WNfdcnw"));
        _extraPane.add(_raceLabels[11], 1, _rowOffset);

        //Targeting order Fields
        for(int i=0; i<2; i++){
            _targetOrderField[i].setMaxWidth(90);
            _targetOrderField[i].setPromptText("Target Order");
            _extraPane.add(_targetOrderField[i],i+2,_rowOffset);
        }

        Button _backButton = new Button("Back");
        _backButton.setOnAction(e->
        {
            _extraPane.setVisible(false);
            _pane.setVisible(true);
        });

        _rowOffset++;

        _extraPane.add(_backButton,2,_rowOffset);
        _extraPane.add(_start, 3, _rowOffset);
    }

    /**
     * Reads the values entered into the textfield and assignes them to the unit numbers.
     * If the field is not filled, assigns 0.
     */
    public boolean setUnits() {
        for(int i=0; i<_names.length; i++) {
            for (int k = 0; k < Database.NUM_SHIPS; k++) {
                _unitCounts[i][k] = (_unitFields[i][k].getNumber() < 0) ? 0 : _unitFields[i][k].getNumber();
                _unitFields[i][k].setText((_unitCounts[i][k] == 0) ? "0" : Integer.toString(_unitCounts[i][k]));
            }
            if(!_targetOrderField[i].getText().isEmpty()){
                if(_targetOrderField[i].getText().length() != 7){
                    return false;
                }
                else {
                    String splitline[] = _targetOrderField[i].getText().split("");
                    //CANNOT SUBSTITUTE NUMBERS WITH K.
                    for (int k = 0; k < splitline.length; k++) {
                        switch (splitline[k]) {
                            case "f":
                                _targetOrder[i][k] = Database.FIGHTER;
                                break;
                            case "d":
                                _targetOrder[i][k] = Database.DESTROYER;
                                break;
                            case "c":
                                _targetOrder[i][k] = Database.CRUISER;
                                break;
                            case "n":
                                _targetOrder[i][k] = Database.DREADNOUGHT;
                                break;
                            case "w":
                                _targetOrder[i][k] = Database.WAR_SUN;
                                break;
                            case "D":
                                _targetOrder[i][k] = 5;
                                break;
                            case "W":
                                _targetOrder[i][k] = 6;
                                break;
                            default:
                                return false;
                        }
                    }
                }
            }else{
                //Default targeting order
                _targetOrder[i] = new int[]{5, 6, 0, 1, 2, 3, 4};
            }
        }

        //Race Effects
        _raceEffects[ATTACKER] = new HashMap<>();
        _raceEffects[DEFENDER] = new HashMap<>();

        for(int i=0; i<2; i++) {
            //Mentak
            if (Database.raceOf(_names[i]).equals("The Mentak Coalition")) {
                String mentakAbility = "";
                if (_raceField[i][0].getText().length() != 2) {
                    return false;
                } else {
                    String splitline[] = _raceField[i][0].getText().split("");
                    //CANNOT SUBSTITUTE NUMBERS WITH K.
                    for (int k = 0; k < splitline.length; k++) {
                        switch (splitline[k]) {
                            case "d":
                                mentakAbility = mentakAbility.concat(Integer.toString(Database.DESTROYER));
                                break;
                            case "c":
                                mentakAbility = mentakAbility.concat(Integer.toString(Database.CRUISER));
                                break;
                            default:
                                return false;
                        }
                    }
                }
                _raceEffects[i].put("Mentak", Integer.parseInt(mentakAbility));
            }

            //Naalu
            if(Database.raceOf(_names[i]).equals("The Naalu Collective")){
                _raceNumberField[i][1].setText((_raceNumberField[i][1].getNumber()<0 ? "0":_raceNumberField[i][1].getText()));
                _raceEffects[i].put("Naalu", _raceNumberField[i][1].getNumber());
            }

            //Jol-Nar
            if(Database.raceOf(_names[i]).equals("The Universities of Jol-Nar")){
                String splitline[] = _raceField[i][1].getText().split("");
                for (int k = 0; k < splitline.length; k++) {
                    switch (splitline[k]) {
                        case "f":
                            _jolPenalty[Database.FIGHTER] = true;
                            break;
                        case "d":
                            _jolPenalty[Database.DESTROYER] = true;
                            break;
                        case "c":
                            _jolPenalty[Database.CRUISER] = true;
                            break;
                        case "n":
                            _jolPenalty[Database.DREADNOUGHT] = true;
                            break;
                        case "w":
                            _jolPenalty[Database.WAR_SUN] = true;
                            break;
                        case "S":
                            _jolPenalty[Database.SAS] = true;
                            break;
                        default:
                            return false;
                    }
                }
            }

            //Federation
            if(Database.raceOf(_names[i]).equals("The Federation of Sol")){
                _raceEffects[i].put("Sol", _raceNumberField[i][2].getNumber() < 0 ? 0 : _raceNumberField[i][2].getNumber());
            }
        }

        return true;
    }

    /**
     * Calculates damage values for units based on tech that the ATTACKER and DEFENDER have researched
     */
    public void damageVal () {
        for(int i = 0; i < 2; i++) {
            if (Database.hasTechLocal(_names[i], "Hylar V Assault Laser")) {
                _unitHitRate[i][Database.DESTROYER]--;
            }

            if (Database.hasTechLocal(_names[i], "Ion Cannons")) {
                _unitHitRate[i][Database.DREADNOUGHT] -= 2;
            }

            if(Database.hasTechLocal(_names[i], "Cybernetics")) {
                _unitHitRate[i][Database.FIGHTER]--;
            }

            if(Database.hasTechLocal(_names[i], "Advanced Fighters")) {
                _unitHitRate[i][Database.FIGHTER]--;
            }

            if(Database.localHasPerson(_names[i], "Mechanic")){
                _unitDice[i][Database.SAS]++;
            }

            if(Database.localHasPerson(_names[i],"Tactician")){
                for(int k=0; k<Database.NUM_SHIPS-2; k++){
                    _unitHitRate[i][k] -= (_unitCounts[1-i][k] == 0) ? 1:0;
                }
            }

            if(Database.raceOf(_names[i]).equals("The Embers of Muaat")){
                _unitHitRate[i][Database.WAR_SUN] -= Database.getTechSpecs(_names[i],Database.RED);
            }

            if(Database.raceOf(_names[i]).equals("The Federation of Sol") && !Database.hasTechLocal(_names[i],"Hyper Metabolism")){
                _raceEffects[i].put("Sol",0);
            }

            if(Database.raceOf(_names[i]).equals("The L1Z1X Mindnet")){
                _raceNumberField[i][0].setText(_raceNumberField[i][0].getNumber()<0 ? "0":_raceNumberField[i][0].getText());
                for(int k=0; k<_raceNumberField[i][0].getNumber(); k++){
                    _unitHitRate[i][Database.DREADNOUGHT]--;
                }
            }

            if(Database.raceOf(_names[i]).equals("The Ghosts of Creuss") && _raceButton[DEFENDER][0].isSelected()){
                _unitHitRate[i][Database.DESTROYER]--;
                _unitHitRate[i][Database.CRUISER]--;
            }

            if(Database.raceOf(_names[i]).equals("The Naalu Collective")){
                _unitHitRate[i][Database.FIGHTER] -= (Database.empireStageOf(_names[i])-1 <0 ? 0: Database.empireStageOf(_names[i])-1);
            }


            if(Database.raceOf(_names[i]).equals("The Sardakk N'Orr") && _raceButton[i][1].isSelected()){
                for(int k=0; k<Database.NUM_SHIPS-1; k++) {
                    _unitHitRate[i][k]--;
                }
            }

            if(Database.raceOf(_names[i]).equals("The Universities of Jol-Nar")){
                for (int k = 0; k < _jolPenalty.length; k++) {
                    if(_jolPenalty[k]){
                        _unitHitRate[i][k]++;
                    }
                }
            }

            if(Database.raceOf(_names[i]).equals("The Emirates of Hacan")){
                if(_raceButton[i][2].isSelected()){
                    _unitHitRate[i][Database.FIGHTER]++;
                }
                if(_raceButton[i][4].isSelected()){
                    _unitHitRate[i][Database.WAR_SUN] += 2;
                }
            }

        }
        if(_defNeb.isSelected()){
            for(int i=0; i<Database.NUM_SHIPS-1; i++){
                _unitHitRate[DEFENDER][i]--;
            }
        }
    }


    public void otherVal(boolean cultist){
        for(int i=0; i<2; i++){
            int e = 1-i;
            //Cultist
            if(Database.localHasPerson(_names[i], "Cultist")){
                for(int k=0; k<Database.NUM_SHIPS-2; k++){
                    if(_unitCounts[i][k] >= 3 && !_cultist[i][k] && cultist){
                        _unitHitRate[i][k]--;
                        _cultist[i][k] = true;
                    } else if(_unitCounts[i][k] < 3 && _cultist[i][k]){
                        _unitHitRate[i][k]++;
                        _cultist[i][k] = false;
                    }
                }
            }

            //Champion
            if(Database.localHasPerson(_names[i], "Champion")){
                int bonus = totalUnits()[i] - totalUnits()[e] + _unitCounts[e][Database.FIGHTER];
                if(bonus>=0){
                    for(int k=0; k<Database.NUM_SHIPS-2; k++) {
                        _unitHitRate[i][k] = _startHitRate[i][k]-bonus;
                    }
                }
            }
        }
    }

    public int diceRoller(){
        return (int) (Math.random() * 10) + 1;
    }

    public int[] totalUnits() {
        int[] ret = {0,0};
        for(int k=0; k<Database.NUM_SHIPS-2; k++){
            ret[ATTACKER] += _unitCounts[ATTACKER][k];
            ret[DEFENDER] += _unitCounts[DEFENDER][k];
        }
        return ret;
    }
    /**
     * Calculates precombat (ADT, AFB, assault cannons)
     * Does not calculate deep space cannons
     */
    public void preCombat () {
        damageVal();
        int preFire[] = {0,0}; // ATTACKER, DEFENDER

        for(int i=0; i < 2; i++){   //iterates through ATTACKER and DEFENDER
            // i is the current ATTACKER
            // e is the DEFENDER of the current ATTACKER
            int e = 1 - i;
            //ADT
            if (_unitCounts[i][Database.FIGHTER] > 3) { //if ADT/AFB can be done
                int ADT = 0;
                if (Database.hasTechLocal(_names[i], "ADT")) {  //ADT
                    while(!((ADT < (_unitCounts[i][Database.DESTROYER] + _unitCounts[i][Database.SAS])) && (ADT < _unitCounts[e][Database.FIGHTER]))){
                        for (int k = 0; k < ((_unitCounts[i][Database.DESTROYER] + _unitCounts[i][Database.SAS]) * _unitCounts[e][Database.FIGHTER] )/ 4; k++) {
                            if(Database.raceOf(_names[i]).equals("The Emirates of Hacan")
                                    && _raceButton[i][3].isSelected()
                                    && diceRoller() >= (_unitHitRate[i][Database.DESTROYER] - 2)){
                                ADT++;
                            }
                            else if (diceRoller() >= (_unitHitRate[i][Database.DESTROYER] - 1)) {
                                ADT++;
                            }
                        }
                    }
                } else { //AFB
                    for (int k = 0; k < ((_unitCounts[i][Database.DESTROYER] + _unitCounts[i][Database.SAS]) * _unitCounts[e][Database.FIGHTER] )/ 4; k++) {
                        if(Database.raceOf(_names[i]).equals("The Emirates of Hacan")
                                && _raceButton[i][3].isSelected()
                                && diceRoller() >= (_unitHitRate[i][Database.DESTROYER] - 1)){
                            ADT++;
                        }
                        else if (diceRoller() >= _unitHitRate[i][Database.DESTROYER]) {
                            ADT++;
                        }
                    }
                }
                //Naalu Shields
                if(Database.raceOf(_names[e]).equals("The Naalu Collective")){
                    while(ADT >0 && _raceEffects[e].get("Naalu") >0){
                        ADT--;
                        _raceEffects[e].put("Naalu", _raceEffects[e].get("Naalu")-1);
                    }
                }

                //Jol-Nar combat counting
                if(Database.raceOf(_names[i]).equals("The Universities of Jol-Nar")){
                    _jolCounter[Database.DESTROYER] += ADT;
                    if(_jolCounter[Database.DESTROYER] >5 && _jolPenalty[Database.DESTROYER]){
                        _unitHitRate[i][Database.DESTROYER]--;
                        _jolPenalty[Database.DESTROYER] = false;
                    }
                }
                _unitCounts[e][Database.FIGHTER] -= ADT;
            }

            //Assault Cannons
            if (Database.hasTechLocal(_names[i], "Assault Cannon") && (totalUnits()[i] <= 3)) {
                for(int k = 0; k<_unitCounts[i][Database.CRUISER]; k++){
                    if(diceRoller() >= _unitHitRate[i][Database.CRUISER] - 3){
                        preFire[i]++;
                    }
                }
            }

            //Mentak Racial Ability
            if(Database.raceOf(_names[i]).equals("The Mentak Coalition")){
                String[] splitline = Integer.toString(_raceEffects[i].get("Mentak")).split("");
                int stolenGoods = 0;
                for(int k=0; k<splitline.length; k++){
                    if(diceRoller() >= _unitHitRate[i][Integer.parseInt(splitline[k])] && _unitCounts[i][Integer.parseInt(splitline[k])] > 0){
                        preFire[i]++;
                        stolenGoods++;
                    }
                }
                _raceEffects[i].put("Mentak Goods",stolenGoods);
            }
        }

        //todo if adding more pre combat, make sure to account for SAS Shields Holding
        //Applying Assault Cannons
        for(int i = 0; i<2; i++) {
            int e = 1 - i;
            int s = _unitCounts[e][Database.SAS];

            //Naalu Shields
            if(Database.raceOf(_names[e]).equals("The Naalu Collective")){
                while(preFire[i] >0 && _raceEffects[e].get("Naalu") >0){
                    preFire[i]--;
                    _raceEffects[e].put("Naalu", _raceEffects[e].get("Naalu")-1);
                }
            }

            //SAS Shields holding
            while(preFire[i] >0 && s >0){
                preFire[i]--;
                s--;
            }

            for (int k = 0; k < _targetOrder[i].length; k++) {  //iterates over the targeting order
                while (preFire[i] > 0) {
                    //Skip over sustains. Cruiser punches through
                    if (_targetOrder[e][k] >4) {
                        break;
                    } else if (_unitCounts[e][_targetOrder[e][k]] > 0) {   //if DEFENDER unit count of current target in targeting order > 0
                        //Jol-Nar combat counting
                        if(Database.raceOf(_names[i]).equals("The Universities of Jol-Nar")){
                            _jolCounter[Database.CRUISER] += preFire[i];
                            if(_jolCounter[Database.CRUISER] >5 && _jolPenalty[Database.CRUISER]){
                                _unitHitRate[i][Database.CRUISER]--;
                                _jolPenalty[Database.CRUISER] = false;
                            }
                        }
                        _unitCounts[e][_targetOrder[e][k]]--;
                        preFire[i]--;
                        if (k == Database.DREADNOUGHT && Database.hasTechLocal(_names[e], "Transfabrication")) {
                            _unitCounts[e][Database.DESTROYER]++;
                            k = 0;
                        }
                    } else{break;}
                }
            }
        }

        //Set sustains
        for(int k=0;k<2;k++){
            DREAD_SUS[k] = _unitCounts[k][Database.DREADNOUGHT];
            WAR_SUS[k] = _unitCounts[k][Database.WAR_SUN];
            if(Database.raceOf(_names[k]).equals("The Barony of Letnev")) {
                DREAD_SUS[k] *= 2;
            }
        }

        //Striker Fleets
        if(Database.hasTechLocal(_names[ATTACKER], "Striker Fleets") && _unitCounts[ATTACKER][Database.FIGHTER] < 6
                && (_unitCounts[ATTACKER][Database.DREADNOUGHT] + _unitCounts[ATTACKER][Database.WAR_SUN]) <2){
            _unitHitRate[ATTACKER][Database.DESTROYER] -= ((_strikerField.getNumber() < 0) ? 0 : _strikerField.getNumber());
            _unitHitRate[ATTACKER][Database.CRUISER] -= ((_strikerField.getNumber() < 0) ? 0 : _strikerField.getNumber());
            _strikerField.setText((_strikerField.getNumber() < 0) ? "0" : Integer.toString(_strikerField.getNumber()));
        }
    }

    //Sustains
    public int[] DREAD_SUS = {0,0};
    public int[] WAR_SUS = {0,0};

    /**
     * Normal combat (recursive)
     * @return 1 = Win, 2 = Loss, 3 = Stalemate
     */
    public int combat(int roundNumber){
        otherVal(false);
        //Tally number of hits each ATTACKER makes
        int hits[] = {0,0};
        int cruiserHits[] = {0,0};
        for (int i = 0; i<2; i++) {
            int e = 1-i;
            for (int k = 0; k < Database.NUM_SHIPS; k++) {
                for (int l = 0; l < _unitCounts[i][k]; l++) {
                    for (int die = 0; die < _unitDice[i][k]; die++) {
                        if (diceRoller() >= _unitHitRate[i][k]) {
                            //Naalu Shields
                            if(Database.raceOf(_names[e]).equals("The Naalu Collective") && _raceEffects[e].get("Naalu") >0){
                                _raceEffects[e].put("Naalu", _raceEffects[e].get("Naalu")-1);
                            }
                            else{
                                //Jol-Nar
                                if(Database.raceOf(_names[i]).equals("The Universities of Jol-Nar")){
                                    _jolCounter[k]++;
                                    if(_jolCounter[k] >5 && _jolPenalty[k]){
                                        _unitHitRate[i][k]--;
                                        _jolPenalty[k] = false;
                                    }
                                }
                                if (k == Database.CRUISER) {
                                    cruiserHits[i]++;
                                }
                                else {
                                    hits[i]++;
                                }
                            }
                        }
                    }
                }
            }
            //Federation of Sol
            if (roundNumber==1 && Database.raceOf(_names[i]).equals("The Federation of Sol")) {
                for(int k=0; k<_raceEffects[i].get("Sol");k++){
                    if(diceRoller() >= 8){
                        hits[i]++;
                    }
                }
            }
            if(Database.hasTechLocal(_names[i],"Auxiliary Drones") && _unitCounts[i][Database.DREADNOUGHT] > 0){
                if(diceRoller() >= (_unitHitRate[i][Database.DREADNOUGHT]) + 3) {
                    hits[i]++;
                }
            }
        }


        //Inflict cruiser damage
        for(int i = 0; i < 2; i++){
            int e = 1 - i;
            for (int k = 0; k < _targetOrder[i].length; k++) {
                while (cruiserHits[i] > 0) {
                    //Skip over sustains. Cruiser punches through
                    if(_targetOrder[e][k]>4){
                        break;
                    } else if (_unitCounts[e][_targetOrder[e][k]] > 0) {   //if DEFENDER unit count of current target in targeting order > 0
                        _unitCounts[e][_targetOrder[e][k]]--;
                        cruiserHits[i]--;
                        if (k == Database.DREADNOUGHT && Database.hasTechLocal(_names[e], "Transfabrication")) {
                            _unitCounts[e][Database.DESTROYER]++;
                            k = 0;
                        }
                    } else {break;}
                }
            }
        }

        //Inflict normal hits
        for(int i=0; i<2; i++) {
            int e = 1 - i;
            for (int k = 0; k < _targetOrder[i].length; k++) {
                //Sustains
                if (_targetOrder[e][k] > 4) {
                    while ((DREAD_SUS[e] > 0 || WAR_SUS[e] > 0) && hits[i] > 0) {
                        if (DREAD_SUS[e] > 0) {
                            DREAD_SUS[e]--;
                            hits[i]--;
                        } else if (WAR_SUS[e] > 0) {
                            WAR_SUS[e]--;
                            hits[i]--;
                        } else {
                            break;
                        }
                    }
                }
                else {
                    while (hits[i] > 0) {
                        if (_unitCounts[e][_targetOrder[e][k]] > 0) {   //if DEFENDER unit count of current target in targeting order > 0
                            _unitCounts[e][_targetOrder[e][k]]--;
                            hits[i]--;
                            if (k == Database.DREADNOUGHT && Database.hasTechLocal(_names[e], "Transfabrication")) {
                                _unitCounts[e][Database.DESTROYER]++;
                                k = 0;
                            }
                        } else {break;}
                    }
                }
            }
        }

        //Stalemate
        if ((totalUnits()[0] <= 0) && (totalUnits()[1] <= 0)) {
            return 3;
        }
        //win
        else if (totalUnits()[1] <= 0) {
            return 1;
        }
        //lose
        else if (totalUnits()[0] <= 0) {
            return 2;
        }

        for(int i=0; i<2; i++) {
            if (Database.hasTechLocal(_names[i], "Duranium Armor")) {
                for(int k : _targetOrder[i]){
                    if(k==5 && _unitCounts[i][Database.DREADNOUGHT] > DREAD_SUS[i]){
                        DREAD_SUS[i]++;
                    }
                    else if(k==6 && _unitCounts[i][Database.WAR_SUN] > WAR_SUS[i]){
                        WAR_SUS[i]++;
                    }
                }
            }
            if(Database.hasTechLocal(_names[i],"Hyper Metabolism")){
                _unitHitRate[i][Database.DESTROYER]--;
            }
        }
        return combat(roundNumber+1);
    }



    /**
     * Will take hits in the following order:
     * Dreadnaught(sustain) > WarSun(sustain) > Fighter > Destroyer > Cruiser > Dreadnaught > WarSun
     * Will not sustain hits to warsun or dreadnaught if there are DEFENDER cruisers, will let other units take the hits
     * if possible.
     * Dreadnaught sustains first in order to prevent a targeted/direct hit on warsun. (look at me being in the meta)
     * Assumes that all fighters will be destroyed before destroying carriers/dreadnaughts/warsuns.
     */
    public String combatSim() {
        //Average remaining units, wins, losses
        Float[] avgUrem = {(float) 0,(float) 0,(float) 0,(float) 0,(float) 0,(float) 0,(float) 0,(float) 0,(float) 0,(float) 0,(float) 0,(float) 0,(float) 0, (float) 0};



        if(_names[ATTACKER] == null){
            return "Choose an attacker";
        }
        if(_names[DEFENDER] == null){
            return "Choose a defender";
        }
        if(!setUnits()){
            if(Database.raceOf(_names[ATTACKER]).equals("The Mentak Coalition") || Database.raceOf(_names[DEFENDER]).equals("The Mentak Coalition")){
                return "Invalid racial ability order.\nRefer to racial ability";
            }
            return "Invalid targeting order.\nMake sure to include sustains (capital letters).\n";
        }

        for(int i = 0; i<1000; i++) {
            _unitHitRate[ATTACKER] = Database.getBaseHitRates();
            _unitHitRate[DEFENDER] = Database.getBaseHitRates();

            _unitDice[ATTACKER] = Database.getBaseDiceRolled();
            _unitDice[DEFENDER] = Database.getBaseDiceRolled();


            _jolCounter = new int[]{0,0,0,0,0,0};
            _jolPenalty = new boolean[]{false, false, false, false, false, false};

            setUnits();

            _cultist = new boolean[][]{{false,false,false,false,false},{false,false,false,false,false}};

            //Pre-Combat
            preCombat();

            _startHitRate = _unitHitRate;
            otherVal(true);

            int res = combat(1);
            if (res == 1) {
                avgUrem[10]++;
            } else if (res == 2) {
                avgUrem[11]++;
            } else if (res == 3) {
                avgUrem[12]++;
            } else {
                return "Error";
            }



            //Add number of ships remaining to average
            for (int l = 0; l < Database.NUM_SHIPS-1; l++) {
                avgUrem[l] += _unitCounts[ATTACKER][l];
                avgUrem[l + 5] += _unitCounts[DEFENDER][l];
            }

            //Add number of stolen goods to average if mentak
            for(int k=0; k<2; k++){
                if(Database.raceOf(_names[k]).equals("The Mentak Coalition")){
                    avgUrem[13] += _raceEffects[k].get("Mentak Goods");
                }
            }



            int remdread[] = {_unitCounts[ATTACKER][Database.DREADNOUGHT], _unitCounts[DEFENDER][Database.DREADNOUGHT]};
            int remdest[] = {_unitCounts[ATTACKER][Database.DESTROYER], _unitCounts[DEFENDER][Database.DESTROYER]};
            setUnits();
            if (Database.hasTechLocal(_names[ATTACKER], "Transfabrication") && (_unitCounts[ATTACKER][Database.DREADNOUGHT] > remdread[0]) && (remdest[0] > 0)) {
                avgUrem[3]++;
            }
            if (Database.hasTechLocal(_names[DEFENDER], "Transfabrication") && (_unitCounts[DEFENDER][Database.DREADNOUGHT] > remdread[1]) && (remdest[1] > 0)) {
                avgUrem[6]++;
            }
        }

        //Compute average
        for(int i = 0; i < 10; i++){
            avgUrem[i] /= 1000;
        }
        avgUrem[10] /= 10;
        avgUrem[11] /= 10;
        avgUrem[12] /= 10;
        avgUrem[13] /= 1000;

        String[] prefix = {"Attacker Victory = ", "Defender Victory = ", "Stalemate = "};
        String results = "\nOut of 1000 trials, the results were: \n";
        for(int i=0; i<3; i++){
            results = results.concat(prefix[i]) + (new BigDecimal(Float.toString(avgUrem[i+10])).setScale(1, BigDecimal.ROUND_HALF_EVEN).toString()) + "%\n";
        }
        results = results.concat("\n" + _names[ATTACKER] + " had the following average remaining units:\n");
        for(int i = 0; i<5; i++){
            results = results.concat(Database.nameOfShip(i) + ": ").concat((new BigDecimal(Float.toString(avgUrem[i])).setScale(2, BigDecimal.ROUND_HALF_EVEN).toString())).concat("\n");
        }
        if(Database.raceOf(_names[ATTACKER]).equals("The Mentak Coalition")){
            results = results.concat("Stolen Goods obtained: " + avgUrem[13]);
        }
        results = results.concat("\n" + _names[DEFENDER] + " had the following average remaining units: \n");
        for(int i = 0; i<5; i++){
            results = results.concat(Database.nameOfShip(i) + ": ").concat((new BigDecimal(Float.toString(avgUrem[i+5])).setScale(2, BigDecimal.ROUND_HALF_EVEN).toString())).concat("\n");
        }
        if(Database.raceOf(_names[DEFENDER]).equals("The Mentak Coalition")){
            results = results.concat("Stolen Goods obtained: " + avgUrem[13]);
        }
        return results;
    }



}


