package sandbox_client;

import javafx.application.Platform;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;

import java.math.BigDecimal;
import java.util.ArrayList;


/**
 * Created by Richard on 6/14/2016.
 * Combat Simulations.
 */


//todo: Remember all techs are most updated on pdf, not website
/**
 * 1) Update for hyper metabolism
 * 2) Flagships! :D :D :D (and home system combat bonus, but that'll be in the rulebook eventually...)
 * 3) Update for Xeno?
  */

public class CombatSimTab extends AbstractTab {

	// TODO: move to database, add xml
	public static final int FIGHTER = 0;
	public static final int DESTROYER = 1;
	public static final int CRUISER = 2;
	public static final int DREADNOUGHT = 3;
	public static final int WAR_SUN = 4;

	public static final int NUM_SHIPS = 5;
    public static final int NUM_MATCHED = 2;

	public static final String[] SHIP_NAMES = {"Fighters", "Destroyers", "Cruisers", "Dreadnoughts", "War Suns"};

    private Client _client;

    // Units fields
    private NumberTextField[][] _UnitFields = new NumberTextField[2][NUM_SHIPS];

    // number of each unit
    //player, enemy, player
    private int[][] _UnitCounts = new int[2][NUM_SHIPS];

    // damage values of each unit- TODO factor into xml
    private int[][] _UnitHitRate = {{9, 9, 7, 6, 3}, {9, 9, 7, 6, 3}};

    // dice rolled by each unit- factor this out too
    private int[] _playerUnitDice = {1, 1, 1, 1, 3};

    private Button _start;
    private GridPane scenepane = new GridPane();
    private Text PlayerName;
    private ComboBox<String> eOptions;
    private GridPane _pane;

    //name of player(0) and enemy(1)
    public String[] names = new String[2];

    //make values cleared when you click on another tab
    public CombatSimTab(Client client) {
        super(Client.SIMULATOR);
        // TODO bad- at least make an "initialize()" method to call in here instead of defining everything
        _client = client;
        _root.setClosable(false);
        _root.setContent(scenepane);

        _pane = new GridPane();
        Text Results = new Text();
        Results.setText("Enter data to view results");
        Text ResultTitle = new Text();
        ResultTitle.setText("Result of the battle:");

        //make new textfields
        for(int i=0; i<NUM_MATCHED; i++) {
            for(int k=0; k<NUM_SHIPS; k++){
                _UnitFields[i][k] = new NumberTextField();
            }
        }

        //Set the prompt text
        for(int i=0; i<NUM_MATCHED; i++) {
            for(int k=0; k<NUM_SHIPS; k++){
                _UnitFields[i][k].setPromptText(SHIP_NAMES[k]);
            }
        }

        //start button
        _start = new Button("Start");
        _start.setOnAction(e ->
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        Results.setText("\n".concat(CombatSim()));
                    }
                }));
        GridPane.setHalignment(_start, HPos.CENTER);

        eOptions = new ComboBox<String>();

        //Add things to grid
        for(int k=0; k<NUM_SHIPS; k++){
            _pane.add(_UnitFields[0][k], 1, k + 2);
            _pane.add(_UnitFields[1][k], 2, k + 2);
        }

        _pane.setAlignment(Pos.CENTER);

        GridPane ResultsPane = new GridPane();
        ResultsPane.add(ResultTitle,1,1);
        ResultsPane.add(Results,1,2);
        ResultsPane.setMinWidth(240);
        ResultsPane.setVgap(40);

        _pane.setVgap(40);
        _pane.setHgap(60);
        PlayerName = new Text();
        PlayerName.setText("Click start");

        _pane.add(PlayerName, 1, 1);
        _pane.add(eOptions, 2, 1);
        _pane.add(_start, 2, 7);
        scenepane.add(ResultsPane, 2, 1);
        scenepane.setAlignment(Pos.CENTER);
        scenepane.add(_pane, 1, 1);

        scenepane.setHgap(60);
    }

    /**
     * Waits until database is synced to get names
     */
    @Override
    public void localName(String name) {
        //Gets the name of the client (player)
        PlayerName.setText(name);
        //Gets the name of the opposing players
        //drop down menu for enemies
        if (eOptions.getItems().isEmpty()) {
            for (int i = 0; i < Database.numPlayers(); i++) {
                if(!name.equals(Database.getPlayer(i).name)){
                    eOptions.getItems().add(Database.getPlayer(i).name);
                }
            }
            eOptions.getItems().remove(PlayerName.getText());
        }
    }



    /**
     * Reads the values entered into the textfield and assignes them to the unit numbers
     * @return true if no errors, false if errors
     */
    public boolean setUnits() {
        try{
        	for(int i=0; i<NUM_MATCHED; i++) {
                for(int k=0; k<NUM_SHIPS; k++){
                    _UnitCounts[i][k] = Integer.parseInt(_UnitFields[i][k].getText().toString());
                }
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
        names[0] = PlayerName.getText();
        for(int i = 0; i < NUM_MATCHED; i++) {
            if (Database.hasTech(names[i], "Hylar V Assult Laser")) {
                _UnitHitRate[i][DESTROYER] -= 1;
            }
            if (Database.hasTech(names[i], "Ion Cannons")) {
                _UnitHitRate[i][DREADNOUGHT] -= 2;
            }
            if(Database.hasTech(names[i], "Cybernetics")) {
                _UnitHitRate[i][FIGHTER] -= 1;
            }
            if(Database.hasTech(names[i], "Advanced Fighters")) {
                _UnitHitRate[i][FIGHTER] -= 1;
            }
            if(Database.raceOf(names[i]).equals("The Sardakk N'Orr")){
                for(int k=0; i<NUM_SHIPS; k++) {
                    _UnitHitRate[i][k] -= 1;
                }
            }
            if(Database.raceOf(names[i]).equals("The L1Z1X Mindnet")){
                _UnitHitRate[i][DREADNOUGHT] -= 1;
            }
        }
    }

    public int diceRoller(){
        return (int) Math.round((Math.random() * 10));
    }

    public int[] totalUnits() {
        int[] ret = {0,0};
        for(int i=0; i<NUM_MATCHED; i++){
            for(int k=0; k<NUM_SHIPS; k++){
                ret[i] += _UnitCounts[i][k];
            }
        }
        return ret;
    }
    /**
     * Calculates precombat (ADT, AFB, assault cannons)
     * Does not calculate deep space cannons
     */
    public void preCombat () {
        damageVal();
        int preFire[] = {0,0}; //player, enemy
        for(int i=0; i<NUM_MATCHED; i++){
            //i is the current player
            //e is the enemy of the current player
            int e;
            if(i == 0){ //current player == player
                e=1;    //current enemy == enemy
            }
            else{ //current player == enemy
                e=0;    //current enemy == player
            }
            //ADT
            if (_UnitCounts[i][FIGHTER] > 3) {
                int ADT = 0;
                if (Database.hasTech(names[i], "ADT")) {
                    while(ADT < _UnitCounts[i][DESTROYER] || ADT < _UnitCounts[e][FIGHTER]) {
                        for (int k = 0; i < _UnitCounts[e][FIGHTER] / 4; k++) {
                            if (diceRoller() >= (_UnitHitRate[i][FIGHTER] - 1)) {
                                ADT += 1;
                            }
                        }
                    }
                } else {
                    for (int k = 0; i < _UnitCounts[e][FIGHTER] / 4; k++) {
                        if (diceRoller() >= _UnitHitRate[i][FIGHTER]) {
                            ADT +=+ 1;
                        }
                    }
                }
                _UnitCounts[e][FIGHTER] = _UnitCounts[e][FIGHTER] - ADT;

            }
            //Assault Cannons
            if (Database.hasTech(names[i], "Assault Cannon") && (totalUnits()[i] <= 3)) {
                for(int k = 0; k<_UnitCounts[i][CRUISER]; k++){
                    if(diceRoller() >= _UnitHitRate[i][CRUISER] - 3){
                        preFire[i] += 1;
                    }
                }
            }
        }
        //Applying Assault Cannons
        for(int i = 0; i<NUM_MATCHED; i++) {
            int e;
            if(i == 0){e=1;}
            else{e=0;}
            for (int k = 0; k < NUM_SHIPS; k++) {
                while (preFire[i] > 0) {
                    if (_UnitCounts[e][k] > 0) {
                        _UnitCounts[e][k] -= 1;
                        preFire[i] -= 1;
                        if (k == DREADNOUGHT) {
                            if (Database.hasTech(names[i], "Transfabrication")) {
                                _UnitCounts[e][DESTROYER] += 1;
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
        for (int i = 0; i<NUM_MATCHED; i++) {
            for (int k = 0; k < NUM_SHIPS; k++) {
                for (int l = 0; l < _UnitCounts[i][k]; l++) {
                    for (int die = 0; die < _playerUnitDice[k]; die++) {
                        if (diceRoller() >= _UnitHitRate[i][k]) {
                            if (k == CRUISER) {
                                cruiserHits[i]++;
                            } else {
                                hits[i]++;
                            }
                        }
                    }
                }
            }
            if(Database.hasTech(names[i],"Auxiliary Drones") && _UnitCounts[i][DREADNOUGHT]>0){
                if(diceRoller() >= _UnitHitRate[i][DREADNOUGHT]){
                    hits[i] += 1;
                }
            }
        }

        //Inflict cruiser damage
        for(int i = 0; i < NUM_MATCHED; i++){
            int e;
            if(i == 0){e=1;}
            else{e=0;}
            for (int k = 0; k < NUM_SHIPS; k++) {
                while (cruiserHits[i] > 0) {
                    if (_UnitCounts[e][k] > 0) {
                        _UnitCounts[e][k]--;
                        cruiserHits[i]--;
                        if (k == DREADNOUGHT && Database.hasTech(names[i], "Transfabrication")) {
                            _UnitCounts[e][DESTROYER]++;
                            k -= 2;
                        }
                    } else {
                        break;
                    }
                }
            }
        }

        //Inflict normal hits on enemy
        for(int i=0; i<NUM_MATCHED; i++){
            int e;
            if(i == 0){e=1;}
            else{e=0;}
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
                    if (_UnitCounts[e][k] > 0) {
                        _UnitCounts[e][k] -= 1;
                        hits[i] -= 1;
                        if (k == DREADNOUGHT && Database.hasTech(names[e], "Transfabrication")) {
                            _UnitCounts[e][DESTROYER] += 1;
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

        for(int i=0; i<NUM_MATCHED; i++) {
            if (Database.hasTech(names[i], "Duranium Armor")) {
                if (_UnitCounts[i][WAR_SUN] > WAR_SUS[i]) {
                    WAR_SUS[i] += 1;
                } else if (_UnitCounts[i][DREADNOUGHT] > DREAD_SUS[i]) {
                    DREAD_SUS[i] += 1;
                }
            }
            if(Database.hasTech(names[i],"Hyper Metabolism")){
                _UnitHitRate[i][DESTROYER] -= 1;
            }
        }
        return combat();
    }

    //Average remaining units, wins, losses
    public ArrayList<Float> avgUrem;

    /**
     * Will take hits in the following order:
     * Dreadnaught(sustain) > WarSun(sustain) > Fighter > Destroyer > Cruiser > Dreadnaught > WarSun
     * Will not sustain hits to warsun or dreadnaught if there are enemy cruisers, will let other units take the hits
     * if possible.
     * Dreadnaught sustains first in order to prevent a targeted/direct hit on warsun. (look at me being in the meta)
     * Assumes that all fighters will be destroyed before destroying carriers/dreadnaughts/warsuns.
     */
    public String CombatSim() {
        avgUrem = new ArrayList<Float>(13);
        for (int i = 0; i < 13; i++) {
            avgUrem.add(new Float(0));
        }
        String enemy;
        try {
            names[0] = PlayerName.getText();
            names[1] = eOptions.getValue(); //gets the value selected by the combobox
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
                for(int k=0; k<NUM_MATCHED; k++){
                    DREAD_SUS[k] = _UnitCounts[k][DREADNOUGHT];
                    WAR_SUS[k] = _UnitCounts[k][WAR_SUN];
                }
                int res = combat();
                if(res == 1){
                    avgUrem.set(10, (avgUrem.get(10) + 1));
                }
                else if(res == 2){
                    avgUrem.set(11, (avgUrem.get(11) + 1));
                }
                else if(res == 3){
                    avgUrem.set(12, (avgUrem.get(12) + 1));
                }
                else{
                    return "Error";
                }
                //Add number of ships remaining to average
                for(int l = 0; l < NUM_SHIPS; l++){
                    avgUrem.set(l, avgUrem.get(l) + _UnitCounts[0][l]);
                    avgUrem.set(l+5, avgUrem.get(l+5) + _UnitCounts[1][l]);
                }

                int remdread[] = {_UnitCounts[0][DREADNOUGHT], _UnitCounts[1][DREADNOUGHT]};
                int remdest[] = {_UnitCounts[0][DESTROYER], _UnitCounts[1][DESTROYER]};
                setUnits();
                if(Database.hasTech(names[0], "Transfabrication") && (_UnitCounts[0][DREADNOUGHT] > remdread[0]) && (remdest[0] > 0)){
                    avgUrem.set(3, avgUrem.get(3) + 1);
                }
                if(Database.hasTech(names[1], "Transfabrication") && (_UnitCounts[1][DREADNOUGHT] > remdread[1]) && (remdest[1] > 0)){
                    avgUrem.set(6, avgUrem.get(6) + 1);
                }
            }
        }
        //Compute average
        for(int i = 0; i < 10; i++){
            avgUrem.set(i, avgUrem.get(i) /1000);
        }
        avgUrem.set(10, avgUrem.get(10) / 10);
        avgUrem.set(11, avgUrem.get(11) / 10);
        avgUrem.set(12, avgUrem.get(12) / 10);

        String results =
                "Out of 1000 trials, the results were: \n" +
                "Victory = " + (new BigDecimal(Float.toString(avgUrem.get(10))).setScale(1, BigDecimal.ROUND_HALF_EVEN).toString()) + "%\n" +
                "Defeat = " + new BigDecimal(Float.toString(avgUrem.get(11))).setScale(1, BigDecimal.ROUND_HALF_EVEN).toString() + "%\n" +
                "Stalemate = " + new BigDecimal(Float.toString(avgUrem.get(12))).setScale(1, BigDecimal.ROUND_HALF_EVEN).toString() + "%\n";
        results = results.concat("You had the following average remaining units: \n");
        //Your average remaining
        for(int i = 0; i<5; i++){
            results = results.concat(SHIP_NAMES[i] + ": ").concat((new BigDecimal(Float.toString(avgUrem.get(i))).setScale(2, BigDecimal.ROUND_HALF_EVEN).toString())).concat("\n");
        }
        results = results.concat("The enemy had the following average remaining units: \n");
        //Enemy average remaining
        for(int i = 0; i<5; i++){
            results = results.concat(SHIP_NAMES[i] + ": ").concat((new BigDecimal(Float.toString(avgUrem.get(i+5))).setScale(2, BigDecimal.ROUND_HALF_EVEN).toString())).concat("\n");
        }
        return results;
    }



}


