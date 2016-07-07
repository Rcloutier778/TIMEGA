package sandbox_client;

import javafx.application.Platform;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;

import java.math.BigDecimal;


/**
 * Created by Richard on 6/14/2016.
 * Combat Simulations.
 */


//todo: Remember all techs are most updated on pdf, not website
/**
 * TODO:
 * 1) Flagships
 * 2) Updated race abilities (inc. Hacan)
 * 3) Xeno Psychology
 * 4) Personnel and SAS
 * 5) Custom targeting order
 */

public class CombatSimTab extends AbstractTab {

	public static final int FIGHTER = 0;
	public static final int DESTROYER = 1;
	public static final int CRUISER = 2;
	public static final int DREADNOUGHT = 3;
	public static final int WAR_SUN = 4;

	public static final String[] SHIP_NAMES = {"Fighters", "Destroyers", "Cruisers", "Dreadnoughts", "War Suns"};
	public static final int NUM_SHIPS = SHIP_NAMES.length;

    // Units fields
    private NumberTextField[][] _unitFields = new NumberTextField[2][NUM_SHIPS];

    // number of each unit
    private int[][] _unitCounts = new int[2][NUM_SHIPS];

    // damage values of each unit
    private int[][] _unitHitRate = {{9, 9, 7, 6, 3}, {9, 9, 7, 6, 3}};

    // dice rolled by each unit- factor this out too
    private int[][] _unitDice = {{1, 1, 1, 1, 3}, {1, 1, 1, 1, 3}};
    
    private static final int PLAYER = 0;
    private static final int ENEMY = 1;

    private Button _start;
    private GridPane _scenepane = new GridPane();
    private Text _playerName;
    private ComboBox<String> _eOptions;
    private GridPane _pane;

    //name of player(0) and enemy(1)
    public String[] _names = new String[2];

    //make values cleared when you click on another tab
    public CombatSimTab() {
        super(Client.SIMULATOR);
        _root.setClosable(false);
        _root.setContent(_scenepane);

        _pane = new GridPane();
        Text results = new Text();
        results.setText("Enter data to view results");
        Text resultTitle = new Text();
        resultTitle.setText("Result of the battle:");

        //make new textfields
        for(int k=0; k<NUM_SHIPS; k++){
            _unitFields[PLAYER][k] = new NumberTextField();
            _unitFields[ENEMY][k] = new NumberTextField();
        }

        //Set the prompt text
        for(int k=0; k<NUM_SHIPS; k++){
            _unitFields[PLAYER][k].setPromptText(SHIP_NAMES[k]);
            _unitFields[ENEMY][k].setPromptText(SHIP_NAMES[k]);
        }

        //start button
        _start = new Button("Start");
        _start.setOnAction(e ->
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        results.setText("\n".concat(combatSim()));
                    }
                }));
        GridPane.setHalignment(_start, HPos.CENTER);

        _eOptions = new ComboBox<String>();

        //Add things to grid
        for(int k=0; k<NUM_SHIPS; k++){
            _pane.add(_unitFields[PLAYER][k], 1, k + 2);
            _pane.add(_unitFields[ENEMY][k], 2, k + 2);
        }

        _pane.setAlignment(Pos.CENTER);

        GridPane ResultsPane = new GridPane();
        ResultsPane.add(resultTitle,1,1);
        ResultsPane.add(results,1,2);
        ResultsPane.setMinWidth(240);
        ResultsPane.setVgap(40);

        _pane.setVgap(40);
        _pane.setHgap(60);
        _playerName = new Text();
        _playerName.setText("Click start");

        _pane.add(_playerName, 1, 1);
        _pane.add(_eOptions, 2, 1);
        _pane.add(_start, 2, 7);
        _scenepane.add(ResultsPane, 2, 1);
        _scenepane.setAlignment(Pos.CENTER);
        _scenepane.add(_pane, 1, 1);

        _scenepane.setHgap(60);
    }

    /**
     * Waits until database is synced to get names
     */
    @Override
    public void localName(String name) {
        // Gets the name of the client (player)
        _playerName.setText(name);
        // Gets the name of the opposing players
        // drop down menu for enemies
        if (_eOptions.getItems().isEmpty()) {
            for (int i = 0; i < Database.numPlayers(); i++) {
                if(!name.equals(Database.getPlayer(i).name)){
                    _eOptions.getItems().add(Database.getPlayer(i).name);
                }
            }
            _eOptions.getItems().remove(_playerName.getText());
        }
    }



    /**
     * Reads the values entered into the textfield and assignes them to the unit numbers
     * @return true if no errors, false if errors
     */
    public boolean setUnits() {
        try{
            for(int k=0; k<NUM_SHIPS; k++){
                _unitCounts[PLAYER][k] = Integer.parseInt(_unitFields[PLAYER][k].getText());
                _unitCounts[ENEMY][k] = Integer.parseInt(_unitFields[ENEMY][k].getText());
            }
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    /**
     * Calculates damage values for units based on tech that the player and enemy have researched
     */
    public void damageVal () {
        _names[0] = _playerName.getText();
        for(int i = 0; i < 2; i++) {
            if (Database.hasTechLocal(_names[i], "Hylar V Assult Laser")) {
                _unitHitRate[i][DESTROYER] -= 1;
            }
            
            if (Database.hasTechLocal(_names[i], "Ion Cannons")) {
                _unitHitRate[i][DREADNOUGHT] -= 2;
            }
            
            if(Database.hasTechLocal(_names[i], "Cybernetics")) {
                _unitHitRate[i][FIGHTER] -= 1;
            }
            
            if(Database.hasTechLocal(_names[i], "Advanced Fighters")) {
                _unitHitRate[i][FIGHTER] -= 1;
            }
            
            if(Database.raceOf(_names[i]).equals("The Sardakk N'Orr")){
                for(int k=0; i<NUM_SHIPS; k++) {
                    _unitHitRate[i][k] -= 1;
                }
            }
            
            if(Database.raceOf(_names[i]).equals("The L1Z1X Mindnet")){
                _unitHitRate[i][DREADNOUGHT] -= 1;
            }
        }
    }

    public int diceRoller(){
        return (int) (Math.random() * 10) + 1;
    }

    public int[] totalUnits() {
        int[] ret = {0,0};
        for(int k=0; k<NUM_SHIPS; k++){
            ret[PLAYER] += _unitCounts[PLAYER][k];
            ret[ENEMY] += _unitCounts[ENEMY][k];
        }
        return ret;
    }
    /**
     * Calculates precombat (ADT, AFB, assault cannons)
     * Does not calculate deep space cannons
     */
    public void preCombat () {
        damageVal();
        int preFire[] = {0,0}; // player, enemy
        for(int i=0; i < 2; i++){
            // i is the current player
            // e is the enemy of the current player
            int e = 1 - i;
            
            //ADT
            if (_unitCounts[i][FIGHTER] > 3) {
                int ADT = 0;
                if (Database.hasTechLocal(_names[i], "ADT")) {
                    while(ADT < _unitCounts[i][DESTROYER] || ADT < _unitCounts[e][FIGHTER]) {
                        for (int k = 0; i < _unitCounts[e][FIGHTER] / 4; k++) {
                            if (diceRoller() >= (_unitHitRate[i][FIGHTER] - 1)) {
                                ADT += 1;
                            }
                        }
                    }
                } else {
                    for (int k = 0; i < _unitCounts[e][FIGHTER] / 4; k++) {
                        if (diceRoller() >= _unitHitRate[i][FIGHTER]) {
                            ADT +=+ 1;
                        }
                    }
                }
                _unitCounts[e][FIGHTER] = _unitCounts[e][FIGHTER] - ADT;

            }
            //Assault Cannons
            if (Database.hasTechLocal(_names[i], "Assault Cannon") && (totalUnits()[i] <= 3)) {
                for(int k = 0; k<_unitCounts[i][CRUISER]; k++){
                    if(diceRoller() >= _unitHitRate[i][CRUISER] - 3){
                        preFire[i] += 1;
                    }
                }
            }
        }
        //Applying Assault Cannons
        for(int i = 0; i<2; i++) {
            int e;
            if(i == 0){e=1;}
            else{e=0;}
            for (int k = 0; k < NUM_SHIPS; k++) {
                while (preFire[i] > 0) {
                    if (_unitCounts[e][k] > 0) {
                        _unitCounts[e][k] -= 1;
                        preFire[i] -= 1;
                        if (k == DREADNOUGHT) {
                            if (Database.hasTechLocal(_names[i], "Transfabrication")) {
                                _unitCounts[e][DESTROYER] += 1;
                                k -= 2;
                            }
                        }
                    } else {
                        break;
                    }
                }
            }
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
        //Tally number of hits each player makes
        int hits[] = {0,0};
        int cruiserHits[] = {0,0};
        for (int i = 0; i<2; i++) {
            for (int k = 0; k < NUM_SHIPS; k++) {
                for (int l = 0; l < _unitCounts[i][k]; l++) {
                    for (int die = 0; die < _unitDice[i][k]; die++) {
                        if (diceRoller() >= _unitHitRate[i][k]) {
                            if (k == CRUISER) {
                                cruiserHits[i]++;
                            } else {
                                hits[i]++;
                            }
                        }
                    }
                }
            }
            
            if(Database.hasTechLocal(_names[i],"Auxiliary Drones") && _unitCounts[i][DREADNOUGHT] > 0){
                if(diceRoller() >= (_unitHitRate[i][DREADNOUGHT]) + 3) {
                    hits[i] += 1;
                }
            }
        }

        //Inflict cruiser damage
        for(int i = 0; i < 2; i++){
            int e = 1 - i;
            for (int k = 0; k < NUM_SHIPS; k++) {
                while (cruiserHits[i] > 0) {
                    if (_unitCounts[e][k] > 0) {
                        _unitCounts[e][k]--;
                        cruiserHits[i]--;
                        if (k == DREADNOUGHT && Database.hasTechLocal(_names[i], "Transfabrication")) {
                            _unitCounts[e][DESTROYER]++;
                            k -= 2;
                        }
                    } else {
                        break;
                    }
                }
            }
        }

        //Inflict normal hits on enemy
        for(int i=0; i<2; i++){
            int e = 1 - i;
            while ((DREAD_SUS[e] > 0 || WAR_SUS[e] > 0) && hits[i] > 0) {
                if (DREAD_SUS[e] > 0) {
                    DREAD_SUS[e] -= 1;
                    hits[i] -= 1;
                } else if (WAR_SUS[e] > 0) {
                    WAR_SUS[e] -= 1;
                    hits[i] -= 1;
                }
                else{break;}
            }
            for (int k = 0; k < NUM_SHIPS; k++) {
                while (hits[i] > 0) {
                    if (_unitCounts[e][k] > 0) {
                        _unitCounts[e][k] -= 1;
                        hits[i] -= 1;
                        if (k == DREADNOUGHT && Database.hasTechLocal(_names[e], "Transfabrication")) {
                            _unitCounts[e][DESTROYER] += 1;
                            k -= 2;
                        }
                    } else {
                        break;
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
                if (_unitCounts[i][WAR_SUN] > WAR_SUS[i]) {
                    WAR_SUS[i] += 1;
                } else if (_unitCounts[i][DREADNOUGHT] > DREAD_SUS[i]) {
                    DREAD_SUS[i] += 1;
                }
            }
            if(Database.hasTechLocal(_names[i],"Hyper Metabolism")){
                _unitHitRate[i][DESTROYER] -= 1;
            }
        }
        return combat();
    }



    /**
     * Will take hits in the following order:
     * Dreadnaught(sustain) > WarSun(sustain) > Fighter > Destroyer > Cruiser > Dreadnaught > WarSun
     * Will not sustain hits to warsun or dreadnaught if there are enemy cruisers, will let other units take the hits
     * if possible.
     * Dreadnaught sustains first in order to prevent a targeted/direct hit on warsun. (look at me being in the meta)
     * Assumes that all fighters will be destroyed before destroying carriers/dreadnaughts/warsuns.
     */
    public String combatSim() {
        //Average remaining units, wins, losses
        Float[] avgUrem = {(float) 0,(float) 0,(float) 0,(float) 0,(float) 0,(float) 0,(float) 0,(float) 0,(float) 0,(float) 0,(float) 0,(float) 0,(float) 0};
        try {
            _names[0] = _playerName.getText();
            _names[1] = _eOptions.getValue(); // gets the value selected by the combobox
        } catch (NullPointerException e) {
            return "Enemy name field is null";
        }
        for(int i = 0; i<1000; i++){
            if(!setUnits()){
                return "Error, setUnits returned false";
            }
            else {
                //Pre-Combat
                preCombat();
                //Set sustains
                DREAD_SUS[PLAYER] = _unitCounts[PLAYER][DREADNOUGHT];
                DREAD_SUS[ENEMY] = _unitCounts[ENEMY][DREADNOUGHT];
                WAR_SUS[PLAYER] = _unitCounts[PLAYER][WAR_SUN];
                WAR_SUS[ENEMY] = _unitCounts[ENEMY][WAR_SUN];
                
                int res = combat();
                if(res == 1){
                    avgUrem[10]++;
                }
                else if(res == 2){
                    avgUrem[11]++;
                }
                else if(res == 3){
                    avgUrem[12]++;
                }
                else{
                    return "Error";
                }

                //Add number of ships remaining to average
                for(int l = 0; l < NUM_SHIPS; l++){
                    avgUrem[l] += _unitCounts[PLAYER][l];
                    avgUrem[l+5] += _unitCounts[ENEMY][l];
                }

                int remdread[] = {_unitCounts[0][DREADNOUGHT], _unitCounts[1][DREADNOUGHT]};
                int remdest[] = {_unitCounts[0][DESTROYER], _unitCounts[1][DESTROYER]};
                setUnits();
                if(Database.hasTechLocal(_names[0], "Transfabrication") && (_unitCounts[0][DREADNOUGHT] > remdread[0]) && (remdest[0] > 0)){
                    avgUrem[3]++;
                }
                if(Database.hasTechLocal(_names[1], "Transfabrication") && (_unitCounts[1][DREADNOUGHT] > remdread[1]) && (remdest[1] > 0)){
                    avgUrem[6]++;
                }
            }
        }
        //Compute average
        for(int i = 0; i < 10; i++){
            avgUrem[i] /= 1000;
        }
        avgUrem[10] /= 10;
        avgUrem[11] /= 10;
        avgUrem[12] /= 10;
        String[] prefix = {"Victory = ", "Defeat = ", "Stalemate = "};
        String results = "Out of 1000 trials, the results were: \n";
        for(int i=0; i<3; i++){
            results = results.concat(prefix[i]) + (new BigDecimal(Float.toString(avgUrem[i+10])).setScale(1, BigDecimal.ROUND_HALF_EVEN).toString()) + "%\n";
        }
        results = results.concat("You had the following average remaining units:\n");
        for(int i = 0; i<5; i++){
            results = results.concat(SHIP_NAMES[i] + ": ").concat((new BigDecimal(Float.toString(avgUrem[i])).setScale(2, BigDecimal.ROUND_HALF_EVEN).toString())).concat("\n");
        }
        results = results.concat("The enemy had the following average remaining units: \n");
        for(int i = 0; i<5; i++){
            results = results.concat(SHIP_NAMES[i] + ": ").concat((new BigDecimal(Float.toString(avgUrem[i+5])).setScale(2, BigDecimal.ROUND_HALF_EVEN).toString())).concat("\n");
        }
        return results;
    }



}


