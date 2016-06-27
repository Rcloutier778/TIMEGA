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
            Platform.runLater(new Runnable() { // TODO bad- at least make an "initialize()" method to call in here instead of defining everything
                @Override
                public void run() {

                _client = client;
            // TODO - whitespace is misaligned
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

            //drop down menue for enemies
            eOptions = new ComboBox<String>();

            //Add things to grid
            for(int i=0; i<NUM_SHIPS; i++) {
            	_pane.add(_playerUnitFields[i], 1, i+2);
            	_pane.add(_enemyUnitFields[i], 2, i+2);
            }
            _pane.setAlignment(Pos.CENTER);

            Pane ResultsPane = new Pane();
            ResultsPane.getChildren().add(Results);


            scenepane.add(ResultTitle, 2, 1);
            scenepane.add(_pane, 1, 2);
            scenepane.add(_start, 1, 3);
            scenepane.add(ResultsPane, 2, 2);
            scenepane.setAlignment(Pos.CENTER);
                }
            });
        }

    /**
     * Waits until database is synced to get names
     */
    public void initialize(){
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                //Gets the name of the client (player)
                PlayerName = new Text();
                PlayerName.setText(_client.getName());
                _pane.add(PlayerName, 1, 1);
                //Gets the name of the opposing players
                //TODO after ensuring that everything works with multiple clients, get rid of client name in enemy list.
                for(int i=0; i<Database.numPlayers(); i++){
                    eOptions.getItems().add(Database.getPlayer(i).name);
                }
                _pane.add(eOptions, 2, 1);
            }
        });

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

    // TODO stop this, stop putting instance variables halfway through the class
    // TODO and if you are going to make an instance variable, keep it consistent. Since everywhere else in the code uses _varName, that's what I'd recommend switching to.
    public int ppre; //Player pre combat hits
    public int epre; //Enemy pre combat hits

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
        ppre = 0;
        if(Database.hasTech(_client.getName(), "Assault Cannon") && (_playerUnitCounts[WAR_SUN] + _playerUnitCounts[DREADNOUGHT]+ _playerUnitCounts[CRUISER] + _playerUnitCounts[DESTROYER] + _playerUnitCounts[FIGHTER]) <= 3) {
            for(int i = 0; i<_playerUnitCounts[CRUISER]; i++){
                if(DiceRoller() >= _playerUnitHitRate[CRUISER] - 3){
                    ppre += 1;
                }
            }
        }
        else{ppre = 0;}

        //Enemy Assault Cannons
        epre = 0;
        if(Database.hasTech(enemy, "Assault Cannon")&& (_enemyUnitCounts[WAR_SUN] + _enemyUnitCounts[DREADNOUGHT]+ _enemyUnitCounts[CRUISER] + _enemyUnitCounts[DESTROYER] + _enemyUnitCounts[FIGHTER]) <= 3) {
            for(int i = 0; i<_enemyUnitCounts[CRUISER]; i++){
                if(DiceRoller() >= _enemyUnitHitRate[CRUISER] - 3){
                    epre += 1;
                }
            }
        }
        else{epre = 0;}
        //Applying player assault cannon hits to enemy
        while(ppre > 0){
            if(_enemyUnitCounts[FIGHTER] > 0){
                ppre -= 1;
                _enemyUnitCounts[FIGHTER] -= 1;
            }
            else if(_enemyUnitCounts[DESTROYER] > 0){
                ppre -= 1;
                _enemyUnitCounts[DESTROYER] -= 1;
            }
            else if(_enemyUnitCounts[CRUISER] > 0){
                ppre -= 1;
                _enemyUnitCounts[CRUISER] -= 1;
            }
            else if(_enemyUnitCounts[DREADNOUGHT]> 0){
                ppre -= 1;
                _enemyUnitCounts[DREADNOUGHT]-= 1;
                if(Database.hasTech(enemy, "Transfabrication")){
                    _enemyUnitCounts[DESTROYER] += 1;
                }
            }
            else if(_enemyUnitCounts[WAR_SUN] > 0){
                ppre -= 1;
                _enemyUnitCounts[WAR_SUN] -= 1;
            }
            else{break;}
        }
        //Applying enemy assault cannon hits to player
        while(epre > 0){
            if(_playerUnitCounts[FIGHTER] > 0){
                epre -= 1;
                _playerUnitCounts[FIGHTER] -= 1;
            }
            else if(_playerUnitCounts[DESTROYER] > 0){
                epre -= 1;
                _playerUnitCounts[DESTROYER] -= 1;
            }
            else if(_playerUnitCounts[CRUISER] > 0){
                epre -= 1;
                _playerUnitCounts[CRUISER] -= 1;
            }
            else if(_playerUnitCounts[DREADNOUGHT]> 0){
                epre -= 1;
                _playerUnitCounts[DREADNOUGHT]-= 1;
                if(Database.hasTech(_client.getName(), "Transfabrication")){
                    _playerUnitCounts[DESTROYER] += 1;
                }
            }
            else if(_playerUnitCounts[WAR_SUN] > 0){
                epre -= 1;
                _playerUnitCounts[WAR_SUN] -= 1;
            }
            else{break;}
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
        for(int i = 0; i < _playerUnitCounts[FIGHTER]; i++){
            if(DiceRoller() >= _playerUnitHitRate[FIGHTER]){
                phits += 1;
            }
        }
        for(int i = 0; i < _playerUnitCounts[DESTROYER]; i++){
            if(DiceRoller() >= _playerUnitHitRate[DESTROYER]){
                phits += 1;
            }
        }
        for(int i = 0; i < _playerUnitCounts[CRUISER]; i++){
            if(DiceRoller() >= _playerUnitHitRate[CRUISER]){
                pcr += 1;   //use pcr for cruiser
            }
        }
        for(int i = 0; i < _playerUnitCounts[DREADNOUGHT]; i++){
            if(DiceRoller() >= _playerUnitHitRate[DREADNOUGHT]){
                phits += 1;
            }
        }
        for(int i = 0; i < (_playerUnitCounts[WAR_SUN]*3); i++){
            if(DiceRoller() >= _playerUnitHitRate[WAR_SUN]){
                phits += 1;
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
        for(int i = 0; i < _enemyUnitCounts[FIGHTER]; i++){
            if(DiceRoller() >= _enemyUnitHitRate[FIGHTER]){
                ehits += 1;
            }
        }
        for(int i = 0; i < _enemyUnitCounts[DESTROYER]; i++){
            if(DiceRoller() >= _enemyUnitHitRate[DESTROYER]){
                ehits += 1;
            }
        }
        for(int i = 0; i < _enemyUnitCounts[CRUISER]; i++){
            if(DiceRoller() >= _enemyUnitHitRate[CRUISER]){
                ecr += 1;   //use ecr for cruiser
            }
        }
        for(int i = 0; i < _enemyUnitCounts[DREADNOUGHT]; i++){
            if(DiceRoller() >= _enemyUnitHitRate[DREADNOUGHT]){
                ehits += 1;
            }
        }
        for(int i = 0; i < (_enemyUnitCounts[WAR_SUN]*3); i++){
            if(DiceRoller() >= _enemyUnitHitRate[WAR_SUN]){
                ehits += 1;
            }
        }
        if(Database.hasTech(enemy,"Auxiliary Drones") && _enemyUnitCounts[DREADNOUGHT]>0){
            if(DiceRoller() >= 7){
                ehits += 1;
            }
        }
        //Inflict player cruiser damage on enemy
        while(pcr > 0){
            if(_enemyUnitCounts[FIGHTER] > 0){
                _enemyUnitCounts[FIGHTER] -= 1;
                pcr -= 1;
            }
            else if(_enemyUnitCounts[DESTROYER] > 0){
                pcr -= 1;
                _enemyUnitCounts[DESTROYER] -= 1;
            }
            else if(_enemyUnitCounts[CRUISER] > 0){
                pcr -= 1;
                _enemyUnitCounts[CRUISER] -= 1;
            }
            else if(_enemyUnitCounts[DREADNOUGHT]> 0){
                pcr -= 1;
                _enemyUnitCounts[DREADNOUGHT]-= 1;
                if(Database.hasTech(enemy, "Transfabrication")){
                    _enemyUnitCounts[DESTROYER] += 1;
                }
            }
            else if(_enemyUnitCounts[WAR_SUN] > 0){
                pcr -= 1;
                _enemyUnitCounts[WAR_SUN] -= 1;
            }
            else{break;}
        }
        //Inflict enemy cruiser damage on player
        while(ecr > 0){
            if(_playerUnitCounts[FIGHTER] > 0){
                ecr -= 1;
                _playerUnitCounts[FIGHTER] -= 1;
            }
            else if(_playerUnitCounts[DESTROYER] > 0){
                ecr -= 1;
                _playerUnitCounts[DESTROYER] -= 1;
            }
            else if(_playerUnitCounts[CRUISER] > 0){
                ecr -= 1;
                _playerUnitCounts[CRUISER] -= 1;
            }
            else if(_playerUnitCounts[DREADNOUGHT]> 0){
                ecr -= 1;
                _playerUnitCounts[DREADNOUGHT]-= 1;
                if(Database.hasTech(_client.getName(), "Transfabrication")){
                    _playerUnitCounts[DESTROYER] += 1;
                }
            }
            else if(_playerUnitCounts[WAR_SUN] > 0){
                ecr -= 1;
                _playerUnitCounts[WAR_SUN] -= 1;
            }
            else{break;}
        }

        //Inflict normal hits on enemy
        while(phits>0){
            if(edreadsus > 0){
                edreadsus -= 1;
                phits -= 1;
            }
            else if(ewarsus > 0){
                ewarsus -= 1;
                phits -= 1;
            }
            else if(_enemyUnitCounts[FIGHTER] > 0){
                _enemyUnitCounts[FIGHTER] -= 1;
                phits -= 1;
            }
            else if(_enemyUnitCounts[DESTROYER] > 0){
                phits -= 1;
                _enemyUnitCounts[DESTROYER] -= 1;
            }
            else if(_enemyUnitCounts[CRUISER] > 0){
                phits -= 1;
                _enemyUnitCounts[CRUISER] -= 1;
            }
            else if(_enemyUnitCounts[DREADNOUGHT]> 0){
                phits -= 1;
                _enemyUnitCounts[DREADNOUGHT]-= 1;
                if(Database.hasTech(enemy, "Transfabrication")){
                    _enemyUnitCounts[DESTROYER] += 1;
                }
            }
            else if(_enemyUnitCounts[WAR_SUN] > 0){
                phits -= 1;
                _enemyUnitCounts[WAR_SUN] -= 1;
            }
            else{break;}
        }
        //Inflict normal hits on player
        while(ehits > 0){
            if(pdreadsus > 0){
                pdreadsus -=1;
                ehits -= 1;
            }
            else if(pwarsus > 0){
                pwarsus -= 1;
                ehits -= 1;
            }
            else if(_playerUnitCounts[FIGHTER] > 0){
                ehits -= 1;
                _playerUnitCounts[FIGHTER] -= 1;
            }
            else if(_playerUnitCounts[DESTROYER] > 0){
                ehits -= 1;
                _playerUnitCounts[DESTROYER] -= 1;
            }
            else if(_playerUnitCounts[CRUISER] > 0){
                ehits -= 1;
                _playerUnitCounts[CRUISER] -= 1;
            }
            else if(_playerUnitCounts[DREADNOUGHT]> 0){
                ehits -= 1;
                _playerUnitCounts[DREADNOUGHT]-= 1;
                if(Database.hasTech(_client.getName(), "Transfabrication")){
                    _playerUnitCounts[DESTROYER] += 1;
                }
            }
            else if(_playerUnitCounts[WAR_SUN] > 0){
                ehits -= 1;
                _playerUnitCounts[WAR_SUN] -= 1;
            }
            else{break;}
        }

        // TODO - and maybe keep a running count of the total number of units for each player
        //Stalemate
        if(((_playerUnitCounts[WAR_SUN] + _playerUnitCounts[DREADNOUGHT]+ _playerUnitCounts[CRUISER] + _playerUnitCounts[DESTROYER] + _playerUnitCounts[FIGHTER]) <= 0) && ((_enemyUnitCounts[WAR_SUN] + _enemyUnitCounts[DREADNOUGHT]+ _enemyUnitCounts[CRUISER] + _enemyUnitCounts[DESTROYER] + _enemyUnitCounts[FIGHTER]) <= 0)){
            return 3;
        }
        //win
        else if((_enemyUnitCounts[WAR_SUN] + _enemyUnitCounts[DREADNOUGHT]+ _enemyUnitCounts[CRUISER] + _enemyUnitCounts[DESTROYER] + _enemyUnitCounts[FIGHTER]) <= 0){
            return 1;
        }
        //lose
        else if((_playerUnitCounts[WAR_SUN] + _playerUnitCounts[DREADNOUGHT]+ _playerUnitCounts[CRUISER] + _playerUnitCounts[DESTROYER] + _playerUnitCounts[FIGHTER]) <= 0){
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

    //Average remaining units
    // TODO and turn this into an array too
    public float avgPfighter=0;
    public float avgPdestroyer=0;
    public float avgPcruiser=0;
    public float avgPdread=0;
    public float avgPwar=0;
    public float avgEfighter=0;
    public float avgEdestroyer=0;
    public float avgEcruiser=0;
    public float avgEdread=0;
    public float avgEwar=0;
    
    public float wins=0;
    public float losses=0;
    public float draw=0;

    /**
     * Will take hits in the following order:
     * Dreadnaught(sustain) > WarSun(sustain) > Fighter > Destroyer > Cruiser > Dreadnaught > WarSun
     * Will not sustain hits to warsun or dreadnaught if there are enemy cruisers, will let other units take the hits
     * if possible.
     * Dreadnaught sustains first in order to prevent a targeted/direct hit on warsun. (look at me being in the meta)
     * Assumes that all fighters will be destroyed before destroying carriers/dreadnaughts/warsuns.
     */
    public String CombatSim() {
        avgPfighter=0;
        avgPdestroyer=0;
        avgPcruiser=0;
        avgPdread=0;
        avgPwar=0;
        avgEfighter=0;
        avgEdestroyer=0;
        avgEcruiser=0;
        avgEdread=0;
        avgEwar=0;
        wins=0;
        losses=0;
        draw=0;
        enemy = new String();
        try {
            enemy = eOptions.getValue().toString(); //gets the value selected by the combobox
        } catch (NullPointerException e) {
            return "Enemy name field fucked";
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
                    wins += 1;
                }
                else if(res == 2){
                    losses += 1;
                }
                else if(res == 3){
                    draw += 1;
                }
                else{
                    return "Error";
                }
                avgPfighter += _playerUnitCounts[FIGHTER];
                avgPdestroyer += _playerUnitCounts[DESTROYER];
                avgPcruiser += _playerUnitCounts[CRUISER];
                avgPdread += _playerUnitCounts[DREADNOUGHT];
                avgPwar += _playerUnitCounts[WAR_SUN];
                avgEfighter += _enemyUnitCounts[FIGHTER];
                avgEdestroyer += _enemyUnitCounts[DESTROYER];
                avgEcruiser += _enemyUnitCounts[CRUISER];
                avgEdread += _enemyUnitCounts[DREADNOUGHT];
                avgEwar += _enemyUnitCounts[WAR_SUN];
                int premdread = _playerUnitCounts[DREADNOUGHT];
                int premdest = _playerUnitCounts[DESTROYER];
                int eremdread = _enemyUnitCounts[DREADNOUGHT];
                int eremdest = _enemyUnitCounts[DESTROYER];
                setUnits();
                if(_playerUnitCounts[DREADNOUGHT]> premdread && premdest > 0){
                    avgPdread += 1;
                }
                if(_enemyUnitCounts[DREADNOUGHT]> eremdread && eremdest > 0){
                    avgEdread += 1;
                }
            }
        }
        avgPfighter /= 1000;
        avgPdestroyer /= 1000;
        avgPcruiser /= 1000;
        avgPdread /= 1000;
        avgPwar /= 1000;
        avgEfighter /= 1000;
        avgEdestroyer /= 1000;
        avgEcruiser /= 1000;
        avgEdread /= 1000;
        avgEwar /= 1000;
        wins /= 10;
        losses /= 10;
        draw /=10;
        return "Out of 1000 trials, the results were: \n" +
                "Victory = " + (new BigDecimal(Float.toString(wins)).setScale(1).toString()) + "%\n" +
                "Defeat = " + new BigDecimal(Float.toString(losses)).setScale(1).toString() + "%\n" +
                "Stalemate = " + new BigDecimal(Float.toString(draw)).setScale(1).toString() + "%\n" +
                "You had the following average remaining units: \n"
                .concat("Fighters: ").concat((new BigDecimal(Float.toString(avgPfighter)).setScale(2).toString())).concat("\n")
                .concat("Destroyers: ").concat(new BigDecimal(Float.toString(avgPdestroyer)).setScale(2).toString()).concat("\n")
                .concat("Cruisers: ").concat(new BigDecimal(Float.toString(avgPcruiser)).setScale(2).toString()).concat("\n")
                .concat("Dreadnaughts: ").concat(new BigDecimal(Float.toString(avgPdread)).setScale(2).toString()).concat("\n")
                .concat("Warsuns: ").concat(new BigDecimal(Float.toString(avgPwar)).setScale(2).toString()) + "\n" +
                "The enemy had the following average remaining units: \n"
                        .concat("Fighters: ").concat((new BigDecimal(Float.toString(avgEfighter)).setScale(2).toString())).concat("\n")
                        .concat("Destroyers: ").concat(new BigDecimal(Float.toString(avgEdestroyer)).setScale(2).toString()).concat("\n")
                        .concat("Cruisers: ").concat(new BigDecimal(Float.toString(avgEcruiser)).setScale(2).toString()).concat("\n")
                        .concat("Dreadnaughts: ").concat(new BigDecimal(Float.toString(avgEdread)).setScale(2).toString()).concat("\n")
                        .concat("Warsuns: ").concat(new BigDecimal(Float.toString(avgEwar)).setScale(2).toString());
    }



}














































