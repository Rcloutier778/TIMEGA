package sandbox_client;

import javafx.application.Platform;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
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

	public static final String[] SHIP_NAMES = {"Fighters", "Destroyers", "Cruisers", "Dreadnoughts", "War Suns"};

    private Client _client;

    // Units fields
    private TextField[] _playerUnitFields = new TextField[NUM_SHIPS];
    private TextField[] _enemyUnitFields = new TextField[NUM_SHIPS];

    //enemy name
    private String enemy;

    //Number of each unit
    private int[] _playerUnitCounts = new int[NUM_SHIPS];
    private int[] _enemyUnitCounts = new int[NUM_SHIPS];

    //Damage values of each unit- TODO factor into xml
    private int[] _playerUnitHitRate = {9, 9, 7, 6, 3};
    private int[] _enemyUnitHitRate = {9, 9, 7, 6, 3};

    private Button _start;
    private GridPane scenepane = new GridPane();
    private Text PlayerName;
    private ComboBox<String> eOptions;
    private GridPane _pane;

    //make values cleared when you click on another tab
    public CombatSimTab(Client client) {
        super(Client.SIMULATOR);
        // TODO bad- at least make an "initialize()" method to call in here instead of defining everything
        _client = client;
        _root.setClosable(false);
        _root.setContent(scenepane);

        _pane = new GridPane();
        Text Results = new Text();
        Results.setText("\nEnter data to view results");
        Text ResultTitle = new Text();
        ResultTitle.setText("Result of the battle:");


        //make new textfields
        for(int i=0; i<NUM_SHIPS; i++) {
        	_playerUnitFields[i] = new TextField();
        	_enemyUnitFields[i] = new TextField();
        }

        //Set the prompt text
        for(int i=0; i<NUM_SHIPS; i++) {
        	_playerUnitFields[i].setPromptText(SHIP_NAMES[i]);
        	_enemyUnitFields[i].setPromptText(SHIP_NAMES[i]);
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
        for(int i=0; i<NUM_SHIPS; i++) {
        	_pane.add(_playerUnitFields[i], 1, i+2);
        	_pane.add(_enemyUnitFields[i], 2, i+2);
        }
        _pane.setAlignment(Pos.CENTER);

        Pane ResultsPane = new Pane();
        ResultsPane.getChildren().add(Results);

        PlayerName = new Text();
        PlayerName.setText("Click start");
        _pane.add(eOptions, 2, 1);
        _pane.add(PlayerName, 1, 1);
        scenepane.add(ResultTitle, 2, 1);
        scenepane.add(_start, 1, 3);
        scenepane.add(ResultsPane, 2, 2);
        scenepane.setAlignment(Pos.CENTER);
        scenepane.add(_pane, 1, 2);

    }

    /**
     * Waits until database is synced to get names
     */
    @Override
    public void localName(String name) {
        //Gets the name of the client (player)
        PlayerName.setText(name);
        //Gets the name of the opposing players
        //TODO after ensuring that everything works with multiple clients, get rid of client name in enemy list.
        //drop down menu for enemies
        if (eOptions.getItems().isEmpty()) {
            for (int i = 0; i < Database.numPlayers(); i++) {
                if(!name.equals(Database.getPlayer(i).name)){
                    eOptions.getItems().add(Database.getPlayer(i).name);
                }
            }
        }
    }



    /**
     * Reads the values entered into the textfield and assignes them to the unit numbers
     * @return true if no errors, false if errors
     */
    public boolean setUnits() {
        try{
        	for(int i=0; i<NUM_SHIPS; i++) {
        		_playerUnitCounts[i] = Integer.parseInt(_playerUnitFields[i].getText().toString());
        		_enemyUnitCounts[i] = Integer.parseInt(_enemyUnitFields[i].getText().toString());
        	}
        } catch (NumberFormatException e) {
            System.out.println("Error in setUnits");
            // TODO println doesn't actually work when run as a JAR
            return false;
        }
        return true;
    }

    /**
     * Calculates damage values for units based on tech that the player and enemy have researched
     */
    public void DamageVal () { //TODO stop it with capital method names

        String player = _client.getName();

        if(Database.hasTech(player, "Hylar V Assult Laser")){
        	_playerUnitHitRate[DESTROYER] -= 1;
        	_playerUnitHitRate[CRUISER] -= 1;
        } if(Database.hasTech(enemy, "Hylar V Assult Laser")){
        	_enemyUnitHitRate[DESTROYER] -= 1;
        	_enemyUnitHitRate[CRUISER] -= 1;
        }

        if(Database.hasTech(player, "Ion Cannons")){
            _playerUnitHitRate[DREADNOUGHT] -= 2;
        } if(Database.hasTech(enemy, "Ion Cannons")){
            _enemyUnitHitRate[DREADNOUGHT] -= 2;
        }

        if(Database.hasTech(player, "Cybernetics")) {
        	_playerUnitHitRate[FIGHTER] -= 1;
        } if(Database.hasTech(enemy, "Cybernetics")) {
        	_enemyUnitHitRate[FIGHTER] -= 1;
        }

        if(Database.hasTech(player, "Advanced Fighters")) {
            _playerUnitHitRate[FIGHTER] -= 1;
        } if(Database.hasTech(enemy, "Advanced Fighters")) {
        	_enemyUnitHitRate[FIGHTER] -= 1;
        }

        if(Database.raceOf(player).equals("The Sardakk N'Orr")){
        	for(int i=0; i<NUM_SHIPS; i++) {
        		_playerUnitHitRate[i] -= 1;
        	}
        } if(Database.raceOf(enemy).equals("The Sardakk N'Orr")){
        	for(int i=0; i<NUM_SHIPS; i++) {
        		_enemyUnitHitRate[i] -= 1;
        	}
        }
    }

    public int DiceRoller(){
        return (int) Math.round((Math.random() * 10));
    }

    public int[] totalUnits() {
        int[] ret = {_playerUnitCounts[WAR_SUN] + _playerUnitCounts[DREADNOUGHT] + _playerUnitCounts[CRUISER] + _playerUnitCounts[DESTROYER] + _playerUnitCounts[FIGHTER], _enemyUnitCounts[WAR_SUN] + _enemyUnitCounts[DREADNOUGHT] + _enemyUnitCounts[CRUISER] + _enemyUnitCounts[DESTROYER] + _enemyUnitCounts[FIGHTER]};
        return ret;
    }
    /**
     * Calculates precombat (ADT, AFB, assault cannons)
     * Does not calculate deep space cannons
     */
    public void PreCombat () {
        DamageVal();
        //Player ADT
        if (_enemyUnitCounts[FIGHTER] > 3) {
            int pADT = 0;
            if (Database.hasTech(_client.getName(), "ADT")) {
                while(pADT < _playerUnitCounts[DESTROYER] || pADT < _enemyUnitCounts[FIGHTER]) {
                    for (int i = 0; i < _enemyUnitCounts[FIGHTER] / 4; i++) {
                        if (DiceRoller() >= (_playerUnitHitRate[FIGHTER] - 1)) {
                            pADT += 1;
                        }
                    }
                }
            } else {
                for (int i = 0; i < _enemyUnitCounts[FIGHTER] / 4; i++) {
                    if (DiceRoller() >= _playerUnitHitRate[FIGHTER]) {
                        pADT +=+ 1;
                    }
                }
            }
            _enemyUnitCounts[FIGHTER] = _enemyUnitCounts[FIGHTER] - pADT;
        }
        //Enemy ADT
        if (_playerUnitCounts[FIGHTER] > 3) {
            int eADT = 0;
            if (Database.hasTech(enemy, "ADT")) {
                while(eADT < _enemyUnitCounts[DESTROYER] || eADT < _playerUnitCounts[FIGHTER]) {
                    for (int i = 0; i < _playerUnitCounts[FIGHTER] / 4; i++) {
                        if (DiceRoller() >= (_enemyUnitHitRate[FIGHTER] - 1)) {
                            eADT += 1;
                        }
                    }
                }
            } else {
                for (int i = 0; i < _playerUnitCounts[FIGHTER] / 4; i++) {
                    if (DiceRoller() >= _enemyUnitHitRate[FIGHTER]) {
                        eADT += 1;
                    }
                }
            }
            _playerUnitCounts[FIGHTER] = _playerUnitCounts[FIGHTER] - eADT;
        }

        //Player Assault Cannons
        int ppre = 0;
        if (Database.hasTech(_client.getName(), "Assault Cannon") && (totalUnits()[0] <= 3)) {
            for(int i = 0; i<_playerUnitCounts[CRUISER]; i++){
                if(DiceRoller() >= _playerUnitHitRate[CRUISER] - 3){
                    ppre += 1;
                }
            }
        }
        else{ppre = 0;}

        //Enemy Assault Cannons
        int epre = 0;
        if (Database.hasTech(enemy, "Assault Cannon") && (totalUnits()[1] <= 3)) {
            for(int i = 0; i<_enemyUnitCounts[CRUISER]; i++){
                if(DiceRoller() >= _enemyUnitHitRate[CRUISER] - 3){
                    epre += 1;
                }
            }
        }
        else{epre = 0;}
        //Applying player assault cannon hits to enemy
        for (int i = 0; i < 5; i++) {
            while (ppre > 0) {
                if (_enemyUnitCounts[i] > 0) {
                    _enemyUnitCounts[i] -= 1;
                    ppre -= 1;
                    if (i == 3) {
                        if (Database.hasTech(enemy, "Transfabrication")) {
                            _enemyUnitCounts[DESTROYER] += 1;
                            i -= 2;
                        }
                    }
                } else {
                    break;
                }
            }
        }

        //Applying enemy assault cannon hits to player
        for (int i = 0; i < 5; i++) {
            while (epre > 0) {
                if (_playerUnitCounts[i] > 0) {
                    _playerUnitCounts[i] -= 1;
                    epre -= 1;
                    if (i == 3) {
                        if (Database.hasTech(_client.getName(), "Transfabrication")) {
                            _playerUnitCounts[DESTROYER] += 1;
                            i -= 2;
                        }
                    }
                } else {
                    break;
                }
            }
        }
    }

    //Sustains
    public int pdreadsus;
    public int pwarsus;
    public int edreadsus;
    public int ewarsus;

    /**
     * Normal combat (recursive)
     * @return 1 = Win, 2 = Loss, 3 = Stalemate
     */
    public int Combat(int HM){ // TODO stop this too, method names do NOT get capital first letters unless they hella important
        //Tally number of hits player makes
        int phits = 0;
        int pcr = 0;
        // TODO = Now that everything's been moved to arrays, can we factor some of this redundant code out?
        for (int i = 0; i < 5; i++) {
            for (int k = 0; k < _playerUnitCounts[i]; k++) {
                if (i == 4) { //Warsuns 3 hits
                    for (int l = 0; l < 3; l++) {
                        if (DiceRoller() >= _playerUnitHitRate[i]) {
                            phits += 1;
                        }
                    }
                } else if (DiceRoller() >= _playerUnitHitRate[i]) {
                    if (i == 2) { //Cruisers
                        pcr += 1;
                    } else {
                        phits += 1;
                    }
                }
            }
        }
        if(Database.hasTech(_client.getName(),"Auxiliary Drones") && _playerUnitCounts[DREADNOUGHT]>0){
            if(DiceRoller() >= 7){
                phits += 1;
            }
        }
        //Tally number of hits enemy makes
        int ehits = 0;
        int ecr = 0;
        for (int i = 0; i < 5; i++) {
            for (int k = 0; k < _enemyUnitCounts[i]; k++) {
                if (i == 4) { //Warsuns 3 hits
                    for (int l = 0; l < 3; l++) {
                        if (DiceRoller() >= _enemyUnitHitRate[i]) {
                            ehits += 1;
                        }
                    }
                } else if (DiceRoller() >= _enemyUnitHitRate[i]) {
                    if (i == 2) { //Cruisers
                        ecr += 1;
                    } else {
                        ehits += 1;
                    }
                }
            }
        }
        if(Database.hasTech(enemy,"Auxiliary Drones") && _enemyUnitCounts[DREADNOUGHT]>0){
            if(DiceRoller() >= 7){
                ehits += 1;
            }
        }

        //Inflict player cruiser damage on enemy
        for (int i = 0; i < 5; i++) {
            while (pcr > 0) {
                if (_enemyUnitCounts[i] > 0) {
                    _enemyUnitCounts[i] -= 1;
                    pcr -= 1;
                    if (i == 3) {
                        if (Database.hasTech(enemy, "Transfabrication")) {
                            _enemyUnitCounts[DESTROYER] += 1;
                            i -= 2;
                        }
                    }
                } else {
                    break;
                }
            }
        }

        //Inflict enemy cruiser damage on player
        for (int i = 0; i < 5; i++) {
            while (ecr > 0) {
                if (_playerUnitCounts[i] > 0) {
                    _playerUnitCounts[i] -= 1;
                    ecr -= 1;
                    if (i == 3) {
                        if (Database.hasTech(_client.getName(), "Transfabrication")) {
                            _playerUnitCounts[DESTROYER] += 1;
                            i -= 2;
                        }
                    }
                } else {
                    break;
                }
            }
        }

        //Inflict normal hits on enemy
        while ((edreadsus > 0 || ewarsus > 0) && phits > 0) {
            if (edreadsus > 0) {
                edreadsus -= 1;
                phits -= 1;
            } else if (ewarsus > 0) {
                ewarsus -= 1;
                phits -= 1;
            }
            else{break;}
        }
        for (int i = 0; i < 5; i++) {
            while (phits > 0) {
                if (_enemyUnitCounts[i] > 0) {
                    _enemyUnitCounts[i] -= 1;
                    phits -= 1;
                    if (i == 3) {
                        if (Database.hasTech(enemy, "Transfabrication")) {
                            _enemyUnitCounts[DESTROYER] += 1;
                            i -= 2;
                        }
                    }
                } else {
                    break;
                }
            }
        }

        //Inflict normal hits on player
        while ((pdreadsus > 0 || pwarsus > 0) && ehits > 0) {
            if (pdreadsus > 0) {
                pdreadsus -= 1;
                ehits -= 1;
            } else if (pwarsus > 0) {
                pwarsus -= 1;
                ehits -= 1;
            }
            else{break;}
        }
        for (int i = 0; i < 5; i++) {
            while (ehits > 0) {
                if (_playerUnitCounts[i] > 0) {
                    _playerUnitCounts[i] -= 1;
                    ehits -= 1;
                    if (i == 3) {
                        if (Database.hasTech(_client.getName(), "Transfabrication")) {
                            _playerUnitCounts[DESTROYER] += 1;
                            i -= 2;
                        }
                    }
                } else {
                    break;
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

        if(Database.hasTech(_client.getName(), "Duranium Armor")){
            if(_playerUnitCounts[WAR_SUN] > pwarsus){
                pwarsus += 1;
            }
            else if(_playerUnitCounts[DREADNOUGHT]> pdreadsus){
                pdreadsus += 1;
            }
        } if(Database.hasTech(enemy, "Duranium Armor")){
            if(_enemyUnitCounts[WAR_SUN] > ewarsus){
                ewarsus += 1;
            }
            else if(_enemyUnitCounts[DREADNOUGHT]> edreadsus){
                edreadsus += 1;
            }
        }

        if(Database.hasTech(_client.getName(),"Hyper Metabolism")){
            _playerUnitHitRate[DESTROYER] -= 1;
        } if(Database.hasTech(enemy, "Hyper Metabolism")){
            _enemyUnitHitRate[DESTROYER] -= 1;
        }
        return Combat(HM+=1);
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
        addNames();
        avgUrem = new ArrayList<Float>(13);
        for (int i = 0; i < 13; i++) {
            avgUrem.add(new Float(0));
        }
        enemy = new String();
        try {
            enemy = eOptions.getValue().toString(); //gets the value selected by the combobox
        } catch (NullPointerException e) {
            System.out.println(eOptions.toString());
            System.out.println(eOptions.getItems().toString());
            //System.out.println(PlayerName.toString());
            System.out.println(_client.getName());

            return "Enemy name field is null";
        }
        for(int i = 0; i<1000; i++){
            if(setUnits() == false){
                return "Error, setUnits returned false";
            }
            else {
                //Pre-Combat
                PreCombat();
                pdreadsus = _playerUnitCounts[DREADNOUGHT];
                pwarsus = _playerUnitCounts[WAR_SUN];
                edreadsus = _enemyUnitCounts[DREADNOUGHT];
                ewarsus = _enemyUnitCounts[WAR_SUN];
                int res = Combat(0);
                if(res == 1){
                    avgUrem.set(10, (avgUrem.get(10) + new Float(1)));
                }
                else if(res == 2){
                    avgUrem.set(11, (avgUrem.get(11) + new Float(1)));
                }
                else if(res == 3){
                    avgUrem.set(12, (avgUrem.get(12) + new Float(1)));
                }
                else{
                    return "Error";
                }
                avgUrem.set(0, avgUrem.get(0) + new Float(_playerUnitCounts[FIGHTER]));
                avgUrem.set(1, avgUrem.get(1) + new Float(_playerUnitCounts[DESTROYER]));
                avgUrem.set(2, avgUrem.get(2) + new Float(_playerUnitCounts[CRUISER]));
                avgUrem.set(3, avgUrem.get(3) + new Float(_playerUnitCounts[DREADNOUGHT]));
                avgUrem.set(4, avgUrem.get(4) + new Float(_playerUnitCounts[WAR_SUN]));
                avgUrem.set(5, avgUrem.get(5) + new Float(_enemyUnitCounts[FIGHTER]));
                avgUrem.set(6, avgUrem.get(6) + new Float(_enemyUnitCounts[DESTROYER]));
                avgUrem.set(7, avgUrem.get(7) + new Float(_enemyUnitCounts[CRUISER]));
                avgUrem.set(8, avgUrem.get(8) + new Float(_enemyUnitCounts[DREADNOUGHT]));
                avgUrem.set(9, avgUrem.get(9) + new Float(_enemyUnitCounts[WAR_SUN]));

                int premdread = _playerUnitCounts[DREADNOUGHT];
                int premdest = _playerUnitCounts[DESTROYER];
                int eremdread = _enemyUnitCounts[DREADNOUGHT];
                int eremdest = _enemyUnitCounts[DESTROYER];
                setUnits();
                if(_playerUnitCounts[DREADNOUGHT]> premdread && premdest > 0){
                    avgUrem.set(3, avgUrem.get(3) + new Float(1));
                }
                if(_enemyUnitCounts[DREADNOUGHT]> eremdread && eremdest > 0){
                    avgUrem.set(8, avgUrem.get(8) + new Float(1));
                }
            }
        }
        avgUrem.set(0, avgUrem.get(0) / 1000);
        avgUrem.set(1, avgUrem.get(1) / 1000);
        avgUrem.set(2, avgUrem.get(2) / 1000);
        avgUrem.set(3, avgUrem.get(3) / 1000);
        avgUrem.set(4, avgUrem.get(4) / 1000);
        avgUrem.set(5, avgUrem.get(5) / 1000);
        avgUrem.set(6, avgUrem.get(6) / 1000);
        avgUrem.set(7, avgUrem.get(7) / 1000);
        avgUrem.set(8, avgUrem.get(8) / 1000);
        avgUrem.set(9, avgUrem.get(9) / 1000);
        avgUrem.set(10, avgUrem.get(10) / 10);
        avgUrem.set(11, avgUrem.get(11) / 10);
        avgUrem.set(12, avgUrem.get(12) / 10);

        return "Out of 1000 trials, the results were: \n" +
                "Victory = " + (new BigDecimal(Float.toString(avgUrem.get(10))).setScale(1, BigDecimal.ROUND_HALF_EVEN).toString()) + "%\n" +
                "Defeat = " + new BigDecimal(Float.toString(avgUrem.get(11))).setScale(1, BigDecimal.ROUND_HALF_EVEN).toString() + "%\n" +
                "Stalemate = " + new BigDecimal(Float.toString(avgUrem.get(12))).setScale(1, BigDecimal.ROUND_HALF_EVEN).toString() + "%\n" +
                "You had the following average remaining units: \n"
                        .concat("Fighters: ").concat((new BigDecimal(Float.toString(avgUrem.get(0))).setScale(2, BigDecimal.ROUND_HALF_EVEN).toString())).concat("\n")
                        .concat("Destroyers: ").concat(new BigDecimal(Float.toString(avgUrem.get(1))).setScale(2, BigDecimal.ROUND_HALF_EVEN).toString()).concat("\n")
                        .concat("Cruisers: ").concat(new BigDecimal(Float.toString(avgUrem.get(2))).setScale(2, BigDecimal.ROUND_HALF_EVEN).toString()).concat("\n")
                        .concat("Dreadnaughts: ").concat(new BigDecimal(Float.toString(avgUrem.get(3))).setScale(2, BigDecimal.ROUND_HALF_EVEN).toString()).concat("\n")
                        .concat("Warsuns: ").concat(new BigDecimal(Float.toString(avgUrem.get(4))).setScale(2, BigDecimal.ROUND_HALF_EVEN).toString()) + "\n" +
                "The enemy had the following average remaining units: \n"
                        .concat("Fighters: ").concat((new BigDecimal(Float.toString(avgUrem.get(5))).setScale(2, BigDecimal.ROUND_HALF_EVEN).toString())).concat("\n")
                        .concat("Destroyers: ").concat(new BigDecimal(Float.toString(avgUrem.get(6))).setScale(2, BigDecimal.ROUND_HALF_EVEN).toString()).concat("\n")
                        .concat("Cruisers: ").concat(new BigDecimal(Float.toString(avgUrem.get(7))).setScale(2, BigDecimal.ROUND_HALF_EVEN).toString()).concat("\n")
                        .concat("Dreadnaughts: ").concat(new BigDecimal(Float.toString(avgUrem.get(8))).setScale(2, BigDecimal.ROUND_HALF_EVEN).toString()).concat("\n")
                        .concat("Warsuns: ").concat(new BigDecimal(Float.toString(avgUrem.get(9))).setScale(2, BigDecimal.ROUND_HALF_EVEN).toString());
    }



}














































