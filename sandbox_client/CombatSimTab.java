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
 * 2) Updated race abilities (inc. Hacan)
 * 3) Display tooltip on hover
 * 4) Xeno Psychology
 * 5) SAS
 * 6) Mercs
 * 7)
/**
 * Personnel
 * 2) Advisor   --Checkbox
 * 5) Explorer  --Implement carriers first
 */

/**
 Recommendations:
 - leave the results text in permanently (on the right half of the screen), and just change the output numbers when the simulation is run
 - run the simulator immediately after someone finishes editing the box instead of having to hit the button (the simulation is fast enough anyway)
 - allow the user to change each ATTACKER's loss policy; while I agree that yours should be the default, that customization is something that I know that Dan and I would be disappointed to miss out on

 Further work:
 - flagships are starting to roll in, I'll let you know when the database can handle it
 - home system/adjacent combat bonus
 - updated racial abilities (those are starting to roll in too, might want to take a peek)
 - add in SASs (don't do this yet, wait until they appear in the rulebook)
 - make sure the simulator is appropriately handling the current personnel and technology (it should be pretty stable right now, only changing stuff with SASs in a bit)

 Some comments:
 - way too much noise. there's going to be a lot of nit-picky modifiers, don't try to cram them into the main screen. I would either make a popup or sidebar appear with all of the tiny things (like striker fleets movement, nebula, home system combat bonus, barony effect, a lot of racial abilities, even flagships if relevant).

 - the user doesn't care how many trials were run (unless you also want to report the standard deviation, which is miles more useful, but no one is going to care about that either because it is presumably very small)

 - too much vertical padding between unit boxes, drop it a lot

 - the text on the right goes off the screen

 - "targeting order" is cut off

 - the start button is not intuitively placed- it should be after you input all of the information, not before

 - it's very hard to parse the output text. put some line breaks between different sections

 - I don't care how you handle bad input in the targeting order (either parse it as best you can, ignore it and use the default, or don't run the battle), but you have to give feedback. The user should know exactly what he did that is causing a problem (even if you do decide to parse it as best you can, you should still inform the user)

 - the user needs a reminder about which character corresponds to which ship (I might recommend underlining the relevant character in the ship name)

 - the grid layout doesn't work really well with the boxes and ship names. Try aligning the ship names to the right?

 - the output text should not start above everything to its left if it doesn't end below everything to its left
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
    private String[] _names = new String[2];

    //Striker Fleets Movement
    private NumberTextField _strikerField = new NumberTextField();

    //Deep space
    private NumberTextField _deepField = new NumberTextField();

    //Targeting order Field
    private TextField[] _targetOrderField = {new TextField(),new TextField()};

    //Targeting order
    private int[][] _targetOrder = new int[2][7];

    //Extras pane
    private GridPane _extraPane = new GridPane();

    //Results pane
    private GridPane ResultsPane = new GridPane();

    //Checkbox for all racial abilities in play
    private CheckBox[][] _raceButton = new CheckBox[2][2];

    //Number field for all racial abilities in play
    private NumberTextField[][] _raceField = new NumberTextField[2][2];

    //Contains the T/F and # value associated with a racial ability
    //Key = String, Values = int/boolean
    private HashMap[] _raceEffects = new HashMap[2];

    //Button to show extras pane
    private Button _extraButton = new Button(">>");

    //If cultist effect has been applied to a ship or not
    private boolean[][] _cultist;

    //Hit rates at the beginning of combat
    private int[][] _startHitRate;

    private int _rowOffset = 5;

    //make values cleared when you click on another tab
    public CombatSimTab() {
        super(Client.SIMULATOR);
        _root.setClosable(false);
        GridPane _scenepane = new GridPane();
        _root.setContent(_scenepane);

        GridPane _pane = new GridPane();
        Text results = new Text();
        results.setText("Enter data to view results");
        Text resultTitle = new Text();
        resultTitle.setText("Result of the battle:");

        Label[] _unitLabels = new Label[7];

        for(int k=0; k<Database.NUM_SHIPS; k++){
            //Make new Number Text Fields and labels
            _unitFields[ATTACKER][k] = new NumberTextField();
            _unitFields[DEFENDER][k] = new NumberTextField();
            _unitLabels[k] = new Label();

            //Set the prompt text
            _unitFields[ATTACKER][k].setPromptText(Database.nameOfShip(k) + "s");
            _unitFields[DEFENDER][k].setPromptText(Database.nameOfShip(k) + "s");

            //Set the label names
            String[] _targetOrderNames = {" (f):", " (d):", " (c):", " (n):", " (w):", ":"};
            _unitLabels[k].setText(Database.nameOfShip(k) + "s" + _targetOrderNames[k]);
            _unitLabels[k].setAlignment(Pos.BASELINE_RIGHT);

            //Set max width
            _unitFields[ATTACKER][k].setMaxWidth(90);
            _unitFields[DEFENDER][k].setMaxWidth(90);

            //Add things to the grid
            _pane.add(_unitLabels[k], 1, k + 2);
            _pane.add(_unitFields[ATTACKER][k], 2, k + 2);
            _pane.add(_unitFields[DEFENDER][k], 3, k + 2);
        }

        _extraPane.add(new Label("Attacker Extras"),2,1);
        _extraPane.add(new Label("Defender Extras"),3,1);

        //Striker Fleets
        _extraPane.add(new Label("Striker Fleets"),1,2);
        _extraPane.getChildren().get(2).setOnMouseEntered(e-> Tooltip.install(_extraPane.getChildren().get(2), new Tooltip("Number of extra movement \nfor Striker Fleets")));
        _strikerField.setPromptText("Striker Fleets");
        _strikerField.setMaxWidth(90);
        _extraPane.add(_strikerField,2,2);

        //Nebula
        _extraPane.add(new Label("In Nebula"), 1, 3);
        _extraPane.getChildren().get(4).setOnMouseEntered(e -> Tooltip.install(_extraPane.getChildren().get(4), new Tooltip("If defender is in a nebula")));
        _defNeb.setAlignment(Pos.CENTER);
        _extraPane.add(_defNeb, 3, 3);

        //Deep Space Cannon
        _extraPane.add(new Label("Deep Space"),1,4);
        _deepField.setPromptText("Adj. Systems");
        _extraPane.getChildren().get(6).setOnMouseEntered(e -> Tooltip.install(_extraPane.getChildren().get(6), new Tooltip("Number of adjacent systems \nwith at least 1 dreadnought")));
        _deepField.setMaxWidth(90);
        _extraPane.add(_deepField, 2, 4);

        _extraPane.setHgap(20);
        _extraPane.setVgap(20);
        GridPane.setHalignment(_defNeb, HPos.CENTER);

        //Targeting order Label
        _unitLabels[6] = new Label();
        _unitLabels[6].setText("Targeting Order:");
        _unitLabels[6].setTooltip(new Tooltip("Use capital letters for sustains"));
        _pane.add(_unitLabels[6],1,8);

        //Targeting order Fields
        for(int i=0; i<2; i++){
            _targetOrderField[i].setMaxWidth(90);
            _targetOrderField[i].setPromptText("Target Order");
            _pane.add(_targetOrderField[i],i+2,8);
        }


        GridPane[] _namePane = {new GridPane(), new GridPane()};

        //Attacker combobox
        _namePane[ATTACKER].add(new Text("Attacker"),1,1);
        _namePane[ATTACKER].add(_eOptions[ATTACKER],1,2);

        //Defender combobox
        _namePane[DEFENDER].add(new Text("Defender"), 1, 1);
        _namePane[DEFENDER].add(_eOptions[DEFENDER],1,2);

        //start button
        Button _start = new Button("Start");
        _start.setOnAction(e ->
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        results.setText("\n".concat(combatSim()));
                    }
                }));
        _pane.add(_start, 2, 9);

        GridPane.setHalignment(_start, HPos.CENTER);

        _pane.setAlignment(Pos.CENTER);

        //Results pane
        ResultsPane.add(resultTitle, 1, 1);
        ResultsPane.add(results,1,2);
        ResultsPane.setMinWidth(300);
        ResultsPane.setVgap(10);

        //Button to show extra pane
        _extraPane.setVisible(false);
        _extraButton.setOnAction(e -> movePane());
        _pane.add(_extraButton, 3, 9);
        _scenepane.add(_extraPane, 2, 1);
        
        //Pane gap
        _pane.setVgap(20);
        _pane.setHgap(60);

        //ATTACKER and enemey names
        _pane.add(_namePane[ATTACKER], 2, 1);
        _pane.add(_namePane[DEFENDER], 3, 1);

        _scenepane.add(ResultsPane, 2, 1);
        _scenepane.add(_pane, 1, 1);
        _scenepane.setPadding(new Insets(0,180,60,0));
        _scenepane.setHgap(60);
        _scenepane.setAlignment(Pos.CENTER);
    }

    //Slides Extras pane on top of results pane
    public void movePane(){
        if(_extraButton.getText().equals(">>")){
            _extraButton.setText("<<");
        }else{
            _extraButton.setText(">>");
        }
        _extraPane.setVisible(!_extraPane.isVisible());
        ResultsPane.setVisible(!ResultsPane.isVisible());
    }

    /**
     * Waits until database is synced to get names
     */
    @Override
    public void localName(String name) {

        Label[] _raceLabels = new Label[2];
        //Barony Effect
        if (Database.allRaces().contains(("The Barony of Letnev"))) {
            _raceLabels[0] = new Label("Barony:");
            _raceLabels[0].setTooltip(new Tooltip("Number of attacker/defender \ndreadnoughts on board = 5?"));
            _extraPane.add(_raceLabels[0], 1, _rowOffset);
            for (int i = 0; i < 2; i++) {
                _raceButton[i][0] = new CheckBox();
                _raceButton[i][0].setAlignment(Pos.CENTER);
                _extraPane.add(_raceButton[i][0], i + 2, _rowOffset);
                GridPane.setHalignment(_raceButton[i][0], HPos.CENTER);
            }
            _rowOffset++;
        }

        //Ember
        if(Database.allRaces().contains("The Embers of Muaat")){
            _raceLabels[1] = new Label("Embers:");
            _raceLabels[1].setTooltip(new Tooltip("Number of Embers attacker passes through"));
            _extraPane.add(_raceLabels[1], 1, _rowOffset);
            _extraPane.add(new Label("Embers:"), 1, _rowOffset);
            _raceField[DEFENDER][0] = new NumberTextField();
            _raceField[DEFENDER][0].setMaxWidth(80);
            _extraPane.add(_raceField[DEFENDER][0],3,_rowOffset);
            _raceField[DEFENDER][0].setPromptText("# of Embers");
            _rowOffset++;
        }

        // Gets the name of the players
        if (_eOptions[0].getItems().isEmpty()) {
            for (int i = 0; i < Database.numPlayers(); i++) {
                _eOptions[ATTACKER].getItems().add(Database.getPlayer(i).name);
                _eOptions[DEFENDER].getItems().add(Database.getPlayer(i).name);
            }
        }
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
                        }
                    }
                }
            }else{
                //Default targeting order
                _targetOrder[i] = new int[]{5, 6, 0, 1, 2, 3, 4};
            }

        }
        _deepField.setText((_deepField.getNumber() <0) ? "0":_deepField.getText());

        //Race Effects
        _raceEffects[ATTACKER] = new HashMap();
        _raceEffects[DEFENDER] = new HashMap();
        if(Database.allRaces().contains("The Embers of Muaat")){
            if(_raceField[DEFENDER][0].getNumber() <0){
                _raceField[DEFENDER][0].setText("0");
            }
            _raceEffects[DEFENDER].put("Embers",_raceField[DEFENDER][0].getNumber());
        }
        if(Database.allRaces().contains("The Barony of Letnev")){
            _raceEffects[ATTACKER].put("Letnev",_raceButton[ATTACKER][0].isSelected());
            _raceEffects[DEFENDER].put("Letnev",_raceButton[DEFENDER][0].isSelected());
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
                if(Database.raceOf(_names[i]).equals("The Naalu Collective")){
                    _unitHitRate[i][Database.FIGHTER]--;
                }
            }
            
            if(Database.raceOf(_names[i]).equals("The Sardakk N'Orr")){
                for(int k=0; k<Database.NUM_SHIPS; k++) {
                    _unitHitRate[i][k]--;
                }
            }
            
            if(Database.raceOf(_names[i]).equals("The L1Z1X Mindnet")){
                _unitHitRate[i][Database.DREADNOUGHT]--;
            }

            if(Database.raceOf(_names[i]).equals("The Naalu Collective")){
                _unitHitRate[i][Database.FIGHTER]--;
            }

            if(Database.localHasPerson(_names[i],"Mechanic")){
                _unitDice[i][Database.SAS]++;
            }

            if(Database.localHasPerson(_names[i],"Tactician")){
                for(int k=0; k<Database.NUM_SHIPS-1; k++){
                    _unitHitRate[i][k] -= (_unitCounts[1-i][k] == 0) ? 1:0;
                }
            }


        }
        if(_defNeb.isSelected()){
            for(int i=0; i<Database.NUM_SHIPS; i++){
                _unitHitRate[DEFENDER][i]--;
            }
        }
    }


    public void otherVal(boolean cultist){
        for(int i=0; i<2; i++){
            int e = 1-i;
            //Cultist
            if(Database.localHasPerson(_names[i], "Cultist")){
                for(int k=0; k<Database.NUM_SHIPS-1; k++){
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
                    for(int k=0; k<Database.NUM_SHIPS-1; k++) {
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
        for(int k=0; k<Database.NUM_SHIPS-1; k++){
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

        //Deep Space Cannon
        //todo implement carrier field
        preFire[ATTACKER] += _deepField.getNumber();
        for(int i=0; i<_targetOrder[DEFENDER].length; i++){
            if(i == Database.DREADNOUGHT && _unitCounts[DEFENDER][i] >0){
                _unitCounts[DEFENDER][i]--;
                break;
            }
            else if(i==Database.WAR_SUN && _unitCounts[DEFENDER][i] >0){
                _unitCounts[DEFENDER][i]--;
                break;
            }
        }

        //Embers
        if(_raceEffects[DEFENDER].containsKey("Muaat")) {
            while (_unitCounts[ATTACKER][Database.DESTROYER] > 0 && Database.raceOf(_names[DEFENDER]).equals("The Embers of Muaat") && (int) _raceEffects[DEFENDER].get("Muaat") > 0) {
                _unitCounts[ATTACKER][Database.DESTROYER]--;
                _raceEffects[DEFENDER].put("Muaat", (int) _raceEffects[DEFENDER].get("Muaat") - 1);
            }
        }

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
                            if (diceRoller() >= (_unitHitRate[i][Database.DESTROYER] - 1)) {
                                ADT++;
                            }
                        }
                    }
                } else { //AFB
                    for (int k = 0; k < ((_unitCounts[i][Database.DESTROYER] + _unitCounts[i][Database.SAS]) * _unitCounts[e][Database.FIGHTER] )/ 4; k++) {
                        if (diceRoller() >= _unitHitRate[i][Database.DESTROYER]) {
                            ADT++;
                        }
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
        }
        //todo if adding more pre combat, make sure to account for SAS Shields Holding
        //Applying Assault Cannons
        for(int i = 0; i<2; i++) {
            int e = 1 - i;
            int s = _unitCounts[e][Database.SAS];
            for (int k = 0; k < _targetOrder[i].length; k++) {  //iterates over the targeting order
                while (preFire[i] > 0) {
                    //Skip over sustains. Cruiser punches through
                    if (_targetOrder[e][k] >4) {
                        break;
                    } else if (_unitCounts[e][_targetOrder[e][k]] > 0) {   //if DEFENDER unit count of current target in targeting order > 0
                        if (s > 0) { //SAS Shields Holding
                            s--;
                        } else {
                            _unitCounts[e][_targetOrder[e][k]]--;
                            preFire[i]--;
                            if (k == Database.DREADNOUGHT && Database.hasTechLocal(_names[e], "Transfabrication")) {
                                _unitCounts[e][Database.DESTROYER]++;
                                k = 0;
                            }
                            if(_raceEffects[e].containsKey("Letnev")) {
                                _raceEffects[e].put("Letnev", ((boolean) _raceEffects[e].get("Letnev") && k != Database.DREADNOUGHT));
                            }
                        }
                    } else{break;}
                }
            }
        }

        //Set sustains
        for(int k=0;k<2;k++){
            DREAD_SUS[k] = _unitCounts[k][Database.DREADNOUGHT];
            WAR_SUS[k] = _unitCounts[k][Database.WAR_SUN];
            if(_raceEffects[k].containsKey("Letnev")) {
                if (!(boolean) _raceEffects[k].get("Letnev") && DREAD_SUS[k] > 0 && Database.raceOf(_names[k]).equals("The Barony of Letnev")) {
                    DREAD_SUS[k]++;
                }
            }
        }

        //Striker Fleets
        if(Database.hasTechLocal(_names[ATTACKER], "Striker Fleets")){
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
    public int combat(){
        otherVal(false);
        //Tally number of hits each ATTACKER makes
        int hits[] = {0,0};
        int cruiserHits[] = {0,0};
        for (int i = 0; i<2; i++) {
            for (int k = 0; k < Database.NUM_SHIPS; k++) {
                for (int l = 0; l < _unitCounts[i][k]; l++) {
                    for (int die = 0; die < _unitDice[i][k]; die++) {
                        if (diceRoller() >= _unitHitRate[i][k]) {
                            if (k == Database.CRUISER) {
                                cruiserHits[i]++;
                            } else {
                                hits[i]++;
                            }
                        }
                    }
                }
                //Barony Effect: #Dread >=5
                if(_raceEffects[i].containsKey("Letnev")) {
                    hits[i] += (k == Database.DREADNOUGHT && Database.raceOf(_names[i]).equals("The Barony of Letnev") && (boolean) _raceEffects[i].get("Letnev") && _unitCounts[i][k] > 0 && diceRoller() >= _unitHitRate[i][k]) ? 1 : 0;
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
                        if(_raceEffects[e].containsKey("Letnev")) {
                            _raceEffects[e].put("Letnev", ((boolean) _raceEffects[e].get("Letnev") && k != Database.DREADNOUGHT));
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
                            if(_raceEffects[e].containsKey("Letnev")) {
                                _raceEffects[e].put("Letnev", ((boolean) _raceEffects[e].get("Letnev") && k != Database.DREADNOUGHT));
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
                if (_unitCounts[i][Database.WAR_SUN] > WAR_SUS[i]) {
                    WAR_SUS[i]++;
                } else if (_unitCounts[i][Database.DREADNOUGHT] > DREAD_SUS[i]) {
                    DREAD_SUS[i]++;
                }
            }
            if(Database.hasTechLocal(_names[i],"Hyper Metabolism")){
                _unitHitRate[i][Database.DESTROYER]--;
            }
        }
        return combat();
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
        Float[] avgUrem = {(float) 0,(float) 0,(float) 0,(float) 0,(float) 0,(float) 0,(float) 0,(float) 0,(float) 0,(float) 0,(float) 0,(float) 0,(float) 0};

        _names[ATTACKER] = _eOptions[ATTACKER].getValue();
        _names[DEFENDER] = _eOptions[DEFENDER].getValue(); // gets the value selected by the combobox

        if(_names[ATTACKER] == null){
            return "Choose an attacker";
        }
        if(_names[DEFENDER] == null){
            return "Choose a defender";
        }
        if(!setUnits()){
            return "Invalid targeting order. \nMake sure to include sustains (capital letters).";
        }

        for(int i = 0; i<1000; i++) {
            setUnits();

            _unitHitRate[ATTACKER] = Database.getBaseHitRates() ;
            _unitHitRate[DEFENDER] = Database.getBaseHitRates();

            _cultist = new boolean[][]{{false,false,false,false,false},{false,false,false,false,false}};

            //Pre-Combat
            preCombat();

            _startHitRate = _unitHitRate;
            otherVal(true);

            int res = combat();
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
            for (int l = 0; l < Database.NUM_SHIPS; l++) {
                avgUrem[l] += _unitCounts[ATTACKER][l];
                avgUrem[l + 5] += _unitCounts[DEFENDER][l];
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
        String[] prefix = {"Attacker Victory = ", "Defender Victory = ", "Stalemate = "};
        String results = "\nOut of 1000 trials, the results were: \n";
        for(int i=0; i<3; i++){
            results = results.concat(prefix[i]) + (new BigDecimal(Float.toString(avgUrem[i+10])).setScale(1, BigDecimal.ROUND_HALF_EVEN).toString()) + "%\n";
        }
        results = results.concat("\n" + _names[ATTACKER] + " had the following average remaining units:\n");
        for(int i = 0; i<5; i++){
            results = results.concat(Database.nameOfShip(i) + ": ").concat((new BigDecimal(Float.toString(avgUrem[i])).setScale(2, BigDecimal.ROUND_HALF_EVEN).toString())).concat("\n");
        }
        results = results.concat("\n" + _names[DEFENDER] + " had the following average remaining units: \n");
        for(int i = 0; i<5; i++){
            results = results.concat(Database.nameOfShip(i) + ": ").concat((new BigDecimal(Float.toString(avgUrem[i+5])).setScale(2, BigDecimal.ROUND_HALF_EVEN).toString())).concat("\n");
        }
        return results;
    }



}


