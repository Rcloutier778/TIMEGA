package sandbox_client;

import javafx.application.Platform;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
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

/**
 * Current problems with simulator tab:

 - there is no distinction between attacker and defender (important in some cases)

 Recommendations:
 - leave the results text in permanently (on the right half of the screen), and just change the output numbers when the simulation is run
 - run the simulator immediately after someone finishes editing the box instead of having to hit the button (the simulation is fast enough anyway)
 - allow the user to change each player's loss policy; while I agree that yours should be the default, that customization is something that I know that Dan and I would be disappointed to miss out on

 Further work:
 - flagships are starting to roll in, I'll let you know when the database can handle it
 - home system/adjacent combat bonus
 - updated racial abilities (those are starting to roll in too, might want to take a peek)
 - add in SASs (don't do this yet, wait until they appear in the rulebook)
 - make sure the simulator is appropriately handling the current personnel and technology (it should be pretty stable right now, only changing stuff with SASs in a bit)

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
    
    private static final int PLAYER = 0;
    private static final int ENEMY = 1;

    private ComboBox<String>[] _eOptions = new ComboBox[]{new ComboBox<String>(), new ComboBox<String>()};

    //name of player(0) and enemy(1)
    public String[] _names = new String[2];

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

        Label[] _unitLabels = new Label[5];

        for(int k=0; k<Database.NUM_SHIPS; k++){
            //Make new Number Text Fields and labels
            _unitFields[PLAYER][k] = new NumberTextField();
            _unitFields[ENEMY][k] = new NumberTextField();
            _unitLabels[k] = new Label();

            //Set the prompt text
            _unitFields[PLAYER][k].setPromptText(Database.nameOfShip(k) + "s");
            _unitFields[ENEMY][k].setPromptText(Database.nameOfShip(k) + "s");

            //Set the label names
            _unitLabels[k].setText(Database.nameOfShip(k) + "s:");

            _unitFields[PLAYER][k].setMaxWidth(90);
            _unitFields[ENEMY][k].setMaxWidth(90);

            //Add things to the grid
            _pane.add(_unitLabels[k], 1, k + 2);
            _pane.add(_unitFields[PLAYER][k], 2, k + 2);
            _pane.add(_unitFields[ENEMY][k], 3, k + 2);
        }


        GridPane[] _namePane = {new GridPane(), new GridPane()};

        _namePane[PLAYER].add(new Text("Attacker"),1,1);
        _namePane[PLAYER].add(_eOptions[PLAYER],1,2);

        _namePane[ENEMY].add(new Text("Defender"),1,1);
        _namePane[ENEMY].add(_eOptions[ENEMY],1,2);

        //start button
        Button _start = new Button("Start");
        _start.setOnAction(e ->
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        results.setText("\n".concat(combatSim()));
                    }
                }));
        GridPane.setHalignment(_start, HPos.CENTER);


        _pane.setAlignment(Pos.CENTER);

        GridPane ResultsPane = new GridPane();
        ResultsPane.add(resultTitle,1,1);
        ResultsPane.add(results,1,2);
        ResultsPane.setMinWidth(300);
        ResultsPane.setVgap(40);

        _pane.setVgap(40);
        _pane.setHgap(60);

        //Player and enemey names
        _pane.add(_namePane[PLAYER], 2, 1);
        _pane.add(_namePane[ENEMY], 3, 1);

        _pane.add(_start, 1, 1);
        _scenepane.add(ResultsPane, 2, 1);
        _scenepane.add(_pane, 1, 1);
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
                _eOptions[PLAYER].getItems().add(Database.getPlayer(i).name);
                _eOptions[ENEMY].getItems().add(Database.getPlayer(i).name);
            }
        }
    }



    /**
     * Reads the values entered into the textfield and assignes them to the unit numbers.
     * If the field is not filled, assigns 0.
     */
    public void setUnits() {
        for(int i=0; i<_names.length; i++) {
            for (int k = 0; k < Database.NUM_SHIPS; k++) {
                _unitCounts[i][k] = (_unitFields[i][k].getNumber() < 0) ? 0 : _unitFields[i][k].getNumber();
                _unitFields[i][k].setText((_unitCounts[i][k] == 0) ? "0" : Integer.toString(_unitCounts[i][k]));
            }
        }
    }

    /**
     * Calculates damage values for units based on tech that the player and enemy have researched
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
            
            if(Database.raceOf(_names[i]).equals("The Sardakk N'Orr")){
                for(int k=0; k<Database.NUM_SHIPS; k++) {
                    _unitHitRate[i][k]--;
                }
            }
            
            if(Database.raceOf(_names[i]).equals("The L1Z1X Mindnet")){
                _unitHitRate[i][Database.DREADNOUGHT]--;
            }
        }
    }

    public int diceRoller(){
        return (int) (Math.random() * 10) + 1;
    }

    public int[] totalUnits() {
        int[] ret = {0,0};
        for(int k=0; k<Database.NUM_SHIPS; k++){
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
        for(int i=0; i < 2; i++){   //iterates through player and enemy
            // i is the current player
            // e is the enemy of the current player
            int e = 1 - i;
            
            //ADT
            if (_unitCounts[i][Database.FIGHTER] > 3) { //if ADT/AFB can be done
                int ADT = 0;
                if (Database.hasTechLocal(_names[i], "ADT")) {  //ADT
                    while(ADT < _unitCounts[i][Database.DESTROYER] || ADT < _unitCounts[e][Database.FIGHTER]) {
                        for (int k = 0; k < _unitCounts[e][Database.FIGHTER] / 4; k++) {
                            if (diceRoller() >= (_unitHitRate[i][Database.FIGHTER] - 1)) {
                                ADT++;
                            }
                        }
                    }
                } else { //AFB
                    for (int k = 0; k < _unitCounts[e][Database.FIGHTER] / 4; k++) {
                        if (diceRoller() >= _unitHitRate[i][Database.FIGHTER]) {
                            ADT +=+ 1;
                        }
                    }
                }
                _unitCounts[e][Database.FIGHTER] = _unitCounts[e][Database.FIGHTER] - ADT;

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
        //Applying Assault Cannons
        for(int i = 0; i<2; i++) {
            int e = 1 - i;
            for (int k = 0; k < Database.NUM_SHIPS; k++) {
                while (preFire[i] > 0) {
                    if (_unitCounts[e][k] > 0) {
                        _unitCounts[e][k]--;
                        preFire[i]--;
                        if (k == Database.DREADNOUGHT && Database.hasTechLocal(_names[e], "Transfabrication")) {
                            _unitCounts[e][Database.DESTROYER]++;
                            k -= 2;
                        }
                    } else {break;}
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
            for (int k = 0; k < Database.NUM_SHIPS; k++) {
                while (cruiserHits[i] > 0) {
                    if (_unitCounts[e][k] > 0) {
                        _unitCounts[e][k]--;
                        cruiserHits[i]--;
                        if (k == Database.DREADNOUGHT && Database.hasTechLocal(_names[e], "Transfabrication")) {
                            _unitCounts[e][Database.DESTROYER]++;
                            k -= 2;
                        }
                    } else {break;}
                }
            }
        }

        //Inflict normal hits
        for(int i=0; i<2; i++){
            int e = 1 - i;
            while ((DREAD_SUS[e] > 0 || WAR_SUS[e] > 0) && hits[i] > 0) {
                if (DREAD_SUS[e] > 0) {
                    DREAD_SUS[e]--;
                    hits[i]--;
                } else if (WAR_SUS[e] > 0) {
                    WAR_SUS[e]--;
                    hits[i]--;
                }
                else{break;}
            }
            for (int k = 0; k < Database.NUM_SHIPS; k++) {
                while (hits[i] > 0) {
                    if (_unitCounts[e][k] > 0) {
                        _unitCounts[e][k]--;
                        hits[i]--;
                        if (k == Database.DREADNOUGHT && Database.hasTechLocal(_names[e], "Transfabrication")) {
                            _unitCounts[e][Database.DESTROYER]++;
                            k -= 2;
                        }
                    } else {break;}
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
     * Will not sustain hits to warsun or dreadnaught if there are enemy cruisers, will let other units take the hits
     * if possible.
     * Dreadnaught sustains first in order to prevent a targeted/direct hit on warsun. (look at me being in the meta)
     * Assumes that all fighters will be destroyed before destroying carriers/dreadnaughts/warsuns.
     */
    public String combatSim() {
        //Average remaining units, wins, losses
        Float[] avgUrem = {(float) 0,(float) 0,(float) 0,(float) 0,(float) 0,(float) 0,(float) 0,(float) 0,(float) 0,(float) 0,(float) 0,(float) 0,(float) 0};

        _names[PLAYER] = _eOptions[PLAYER].getValue();
        _names[ENEMY] = _eOptions[ENEMY].getValue(); // gets the value selected by the combobox

        if(_names[PLAYER] == null){
            return "Choose the attacker";
        }
        if(_names[ENEMY] == null){
            return "Choose the defender";
        }

        for(int i = 0; i<1000; i++) {
            setUnits();
            //Pre-Combat
            preCombat();
            //Set sustains
            DREAD_SUS[PLAYER] = _unitCounts[PLAYER][Database.DREADNOUGHT];
            DREAD_SUS[ENEMY] = _unitCounts[ENEMY][Database.DREADNOUGHT];
            WAR_SUS[PLAYER] = _unitCounts[PLAYER][Database.WAR_SUN];
            WAR_SUS[ENEMY] = _unitCounts[ENEMY][Database.WAR_SUN];

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
                avgUrem[l] += _unitCounts[PLAYER][l];
                avgUrem[l + 5] += _unitCounts[ENEMY][l];
            }

            int remdread[] = {_unitCounts[PLAYER][Database.DREADNOUGHT], _unitCounts[ENEMY][Database.DREADNOUGHT]};
            int remdest[] = {_unitCounts[PLAYER][Database.DESTROYER], _unitCounts[ENEMY][Database.DESTROYER]};
            setUnits();
            if (Database.hasTechLocal(_names[PLAYER], "Transfabrication") && (_unitCounts[PLAYER][Database.DREADNOUGHT] > remdread[0]) && (remdest[0] > 0)) {
                avgUrem[3]++;
            }
            if (Database.hasTechLocal(_names[ENEMY], "Transfabrication") && (_unitCounts[ENEMY][Database.DREADNOUGHT] > remdread[1]) && (remdest[1] > 0)) {
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
        String results = "\n \nOut of 1000 trials, the results were: \n";
        for(int i=0; i<3; i++){
            results = results.concat(prefix[i]) + (new BigDecimal(Float.toString(avgUrem[i+10])).setScale(1, BigDecimal.ROUND_HALF_EVEN).toString()) + "%\n";
        }
        results = results.concat(_names[PLAYER] + " had the following average remaining units:\n");
        for(int i = 0; i<5; i++){
            results = results.concat(Database.nameOfShip(i) + ": ").concat((new BigDecimal(Float.toString(avgUrem[i])).setScale(2, BigDecimal.ROUND_HALF_EVEN).toString())).concat("\n");
        }
        results = results.concat(_names[ENEMY] + " had the following average remaining units: \n");
        for(int i = 0; i<5; i++){
            results = results.concat(Database.nameOfShip(i) + ": ").concat((new BigDecimal(Float.toString(avgUrem[i+5])).setScale(2, BigDecimal.ROUND_HALF_EVEN).toString())).concat("\n");
        }
        return results;
    }



}


