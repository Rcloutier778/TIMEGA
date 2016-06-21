package sandbox_client;

import javafx.application.Platform;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;


/**
 * Created by Richard on 6/14/2016.
 * Combat Simulations.
 */


public class CombatSimTab {

    protected final Tab _root = new Tab("Combat");
    private Client _client;

    //Player units field
    private TextField pfighter;
    private TextField pdestroyer;
    private TextField pcruiser;
    private TextField pdread;
    private TextField pwar;

    //Enemy units field
    private TextField efighter;
    private TextField edestroyer;
    private TextField ecruiser;
    private TextField edread;
    private TextField ewar;

    private Button _start;
    private GridPane scenepane = new GridPane();
    private Text PlayerName;
    private ComboBox eOptions;
    private GridPane _pane;

        //make values cleared when you click on another tab
        public CombatSimTab(Client client) {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {


            _client = client;
            _root.setClosable(false);
            _root.setContent(scenepane);

            _pane = new GridPane();
            Text Results = new Text();
            Results.setText("\nEnter data to view results");
            Text ResultTitle = new Text();
            ResultTitle.setText("Result of the battle:");


            //make new textfields
            pfighter = new TextField();
            pdestroyer = new TextField();
            pcruiser = new TextField();
            pdread = new TextField();
            pwar = new TextField();
            efighter = new TextField();
            edestroyer = new TextField();
            ecruiser = new TextField();
            edread = new TextField();
            ewar = new TextField();

            //Set the prompt text
            pfighter.setPromptText("Fighters");
            pdestroyer.setPromptText("Destroyers");
            pcruiser.setPromptText("Cruisers");
            pdread.setPromptText("Dreadnaughts");
            pwar.setPromptText("Warsuns");
            efighter.setPromptText("Fighters");
            edestroyer.setPromptText("Destroyers");
            ecruiser.setPromptText("Cruisers");
            edread.setPromptText("Dreadnaughts");
            ewar.setPromptText("Warsuns");

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
            eOptions = new ComboBox();

            //Add things to grid
            _pane.add(pfighter, 1, 2);
            _pane.add(pdestroyer, 1, 3);
            _pane.add(pcruiser, 1, 4);
            _pane.add(pdread, 1, 5);
            _pane.add(pwar, 1, 6);
            _pane.add(efighter, 2, 2);
            _pane.add(edestroyer, 2, 3);
            _pane.add(ecruiser, 2, 4);
            _pane.add(edread, 2, 5);
            _pane.add(ewar, 2, 6);
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

    //enemy name
    private String enemy;
    //Number of each unit
    private int _pfighter;
    private int _pdestroyer;
    private int _pcruiser;
    private int _pdread;
    private int _pwar;
    private int _efighter;
    private int _edestroyer;
    private int _ecruiser;
    private int _edread;
    private int _ewar;

    /**
     * Reads the values entered into the textfield and assignes them to the unit numbers
     * @return true if no errors, false if errors
     */
    public boolean setUnits() {
        try{
            _pfighter =  Integer.parseInt(pfighter.getText().toString());
            _pdestroyer =  Integer.parseInt(pdestroyer.getText().toString());
            _pcruiser =  Integer.parseInt(pcruiser.getText().toString());
            _pdread =  Integer.parseInt(pdread.getText().toString());
            _pwar =  Integer.parseInt(pwar.getText().toString());
            _efighter =  Integer.parseInt(efighter.getText().toString());
            _edestroyer =  Integer.parseInt(edestroyer.getText().toString());
            _ecruiser =  Integer.parseInt(ecruiser.getText().toString());
            _edread =  Integer.parseInt(edread.getText().toString());
            _ewar =  Integer.parseInt(ewar.getText().toString());
        } catch (NumberFormatException e) {
            System.out.println("Error in setUnits");
            return false;
        }
        return true;
    }


    //Damage values of each unit
    private int _pfighterDam;
    private int _pdestroyerDam;
    private int _pcruiserDam;
    private int _pdreadDam;
    private int _pwarDam;
    private int _efighterDam;
    private int _edestroyerDam;
    private int _ecruiserDam;
    private int _edreadDam;
    private int _ewarDam;

    /**
     * Calculates damage values for units based on tech that the player and enemy have researched
     */
    public void DamageVal () {

        String player = _client.getName();

        if(Database.hasTech(player, "Hylar V Assult Laser")){
            _pdestroyerDam = 8;
            _pcruiserDam = 6;
        }
        else{
            _pdestroyerDam = 9;
            _pcruiserDam = 7;
        }
        if(Database.hasTech(enemy, "Hylar V Assult Laser")){
            _edestroyerDam = 8;
            _ecruiserDam = 6;
        }
        else{
            _edestroyerDam = 9;
            _ecruiserDam = 7;
        }
        if(Database.hasTech(player, "Ion Cannons")){
            _pdreadDam = 4;
        }
        else{
            _pdreadDam = 6;
        }
        if(Database.hasTech(enemy, "Ion Cannons")){
            _edreadDam = 4;
        }
        else{
            _edreadDam = 6;
        }
        if(Database.hasTech(player, "Cybernetics")){
            if(Database.hasTech(player, "Advanced Fighters")){
                _pfighterDam = 7;
            }
            else {
                _pfighterDam = 8;
            }
        }
        else{
            _pfighterDam = 9;
        }
        if(Database.hasTech(enemy, "Cybernetics")){
            if(Database.hasTech(enemy, "Advanced Fighters")){
                _efighterDam = 7;
            }
            else{
                _efighterDam = 8;
            }
        }
        else{
            _efighterDam = 9;
        }
        if(Database.raceOf(player).equals("The Sardakk N'Orr")){
            _pfighterDam -= 1;
            _pdestroyerDam -= 1;
            _pcruiserDam -= 1;
            _pdreadDam -= 1;
            _pwarDam = 2;
        }
        else{
            _pwarDam = 3;
        }
        if(Database.raceOf(enemy).equals("The Sardakk N'Orr")){
            _efighterDam -= 1;
            _edestroyerDam -= 1;
            _ecruiserDam -= 1;
            _edreadDam -= 1;
            _ewarDam = 2;
        }
        else{
            _ewarDam = 3;
        }
    }

    public int DiceRoller(){
        int roll = (int) Math.round((Math.random() * 10));
        return roll;
    }

    public int ppre; //Player pre combat hits
    public int epre; //Enemy pre combat hits

    /**
     * Calculates precombat (ADT, AFB, assault cannons)
     * Does not calculate deep space cannons
     */
    public void PreCombat () {
        DamageVal();
        //Player ADT
        if (_efighter > 3) {
            int pADT = 0;
            if (Database.hasTech(_client.getName(), "Automated Defence Turrets")) {
                for (int i = 0; i < (int) (_efighter / 4); i++) {
                    if (DiceRoller() >= (_pfighterDam - 1 - (int) (_efighter / 5))) {
                        pADT += 1;
                    }
                }
            } else {
                for (int i = 0; i < (int) (_efighter / 4); i++) {
                    if (DiceRoller() >= _pfighterDam) {
                        pADT +=+ 1;
                    }
                }
            }
            _efighter = _efighter - pADT;
        }
        //Enemy ADT
        if (_pfighter > 3) {
            int eADT = 0;
            if (Database.hasTech(_client.getName(), "Automated Defence Turrets")) {
                for (int i = 0; i < (int) (_pfighter / 4); i++) {
                    if (DiceRoller() >= (_efighterDam - 1 - (int) (_pfighter / 5))) {
                        eADT += 1;
                    }
                }
            } else {
                for (int i = 0; i < (int) (_pfighter / 4); i++) {
                    if (DiceRoller() >= _efighterDam) {
                        eADT += 1;
                    }
                }
            }
            _pfighter = _pfighter - eADT;
        }
        //Player Assault Cannons
        if(Database.hasTech(_client.getName(), "Assault Cannon")) {
            if (_pcruiser == 1) {
                if (DiceRoller() >= (_pcruiserDam - 1)) {
                    ppre = 1;
                }
            }
            else if (_pcruiser >= 2) {
                if (DiceRoller() >= (_pcruiserDam - 1)) {
                    ppre = 1;
                }
                if (DiceRoller() >= (_pcruiserDam - 1)) {
                    ppre += 1;
                }
            }
        }
        //Enemy Assault Cannons
        else{ppre = 0;}
        if(Database.hasTech(enemy, "Assault Cannon")) {
            if (_ecruiser == 1) {
                if (DiceRoller() >= (_ecruiserDam - 1)) {
                    epre = 1;
                }
            }
            else if (_ecruiser >= 2) {
                if (DiceRoller() >= (_ecruiserDam - 1)) {
                    epre = 1;
                }
                if (DiceRoller() >= (_ecruiserDam - 1)) {
                    epre += 1;
                }
            }
        }
        else{epre = 0;}
        //Applying player assault cannon hits to enemy
        while(ppre > 0){
            if(_efighter > 0){
                ppre -= 1;
                _efighter -= 1;
            }
            else if(_edestroyer > 0){
                ppre -= 1;
                _edestroyer -= 1;
            }
            else if(_ecruiser > 0){
                ppre -= 1;
                _ecruiser -= 1;
            }
            else if(_edread > 0){
                ppre -= 1;
                _edread -= 1;
            }
            else if(_ewar > 0){
                ppre -= 1;
                _ewar -= 1;
            }
            else{break;}
        }
        //Applying enemy assault cannon hits to player
        while(epre > 0){
            if(_pfighter > 0){
                epre -= 1;
                _pfighter -= 1;
            }
            else if(_pdestroyer > 0){
                epre -= 1;
                _pdestroyer -= 1;
            }
            else if(_pcruiser > 0){
                epre -= 1;
                _pcruiser -= 1;
            }
            else if(_pdread > 0){
                epre -= 1;
                _pdread -= 1;
            }
            else if(_pwar > 0){
                epre -= 1;
                _pwar -= 1;
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
     * Will take hits in the following order:
     * Dreadnaught(sustain) > WarSun(sustain) > Fighter > Destroyer > Cruiser > Dreadnaught > WarSun
     * Will not sustain hits to warsun or dreadnaught if there are enemy cruisers, will let other units take the hits
     * if possible.
     * Dreadnaught sustains first in order to prevent a targeted/direct hit on warsun. (look at me being in the meta)
     * Assumes that all fighters will be destroyed before destroying carriers/dreadnaughts/warsuns.
     */
    //todo make the simulator run 100 times and get the average losses on either side and win/loss ratio
    public String CombatSim() {
        enemy = new String();
        try {
            enemy = eOptions.getValue().toString(); //gets the value selected by the combobox
        } catch (NullPointerException e) {
            return "Enemy name field fucked";
        }
        if(setUnits() == false){
            return "Error, setUnits returned false";
        }
        else {
            System.out.println(Integer.parseInt(pfighter.getText().toString()));
            //Pre-Combat
            PreCombat();
            if (ppre > 0 && (_pwar + _pdread + _pcruiser + _pdestroyer + _pfighter) > 0) {
                return "You won in pre-combat with the remaining units: \n"
                        .concat("Fighters: ").concat(Integer.toString(_pfighter)).concat("\n")
                        .concat("Destroyers: ").concat(Integer.toString(_pdestroyer)).concat("\n")
                        .concat("Cruisers: ").concat(Integer.toString(_pcruiser)).concat("\n")
                        .concat("Dreadnaughts: ").concat(Integer.toString(_pdread)).concat("\n")
                        .concat("Warsuns: ").concat(Integer.toString(_pwar));
            }
            if (epre > 0 && (_ewar + _edread + _ecruiser + _edestroyer + _efighter) > 0) {
                return "You lost during pre-combat and the enemy has the following units: \n"
                        .concat("Fighters: ").concat(Integer.toString(_efighter)).concat("\n")
                        .concat("Destroyers: ").concat(Integer.toString(_edestroyer)).concat("\n")
                        .concat("Cruisers: ").concat(Integer.toString(_ecruiser)).concat("\n")
                        .concat("Dreadnaughts: ").concat(Integer.toString(_edread)).concat("\n")
                        .concat("Warsuns: ").concat(Integer.toString(_ewar));
            }
            pdreadsus = _pdread;
            pwarsus = _pwar;
            edreadsus = _edread;
            ewarsus = _ewar;
            return Combat();
        }
    }

    //Normal Combat (recursive rounds)
    public String Combat(){
        //Tally number of hits player makes
        System.out.println("Combat");
        int phits = 0;
        int pcr = 0;
        for(int i = 0; i < _pfighter; i++){
            if(DiceRoller() >= _pfighterDam){
                phits += 1;
            }
        }
        for(int i = 0; i < _pdestroyer; i++){
            if(DiceRoller() >= _pdestroyerDam){
                phits += 1;
            }
        }
        for(int i = 0; i < _pcruiser; i++){
            if(DiceRoller() >= _pcruiserDam){
                pcr += 1;   //use pcr for cruiser
            }
        }
        for(int i = 0; i < _pdread; i++){
            if(DiceRoller() >= _pdreadDam){
                phits += 1;
            }
        }
        for(int i = 0; i < (_pwar*3); i++){
            if(DiceRoller() >= _pwarDam){
                phits += 1;
            }
        }
        //Tally number of hits enemy makes
        int ehits = 0;
        int ecr = 0;
        for(int i = 0; i < _efighter; i++){
            if(DiceRoller() >= _efighterDam){
                ehits += 1;
            }
        }
        for(int i = 0; i < _edestroyer; i++){
            if(DiceRoller() >= _edestroyerDam){
                ehits += 1;
            }
        }
        for(int i = 0; i < _ecruiser; i++){
            if(DiceRoller() >= _ecruiserDam){
                ecr += 1;   //use ecr for cruiser
            }
        }
        for(int i = 0; i < _edread; i++){
            if(DiceRoller() >= _edreadDam){
                ehits += 1;
            }
        }
        for(int i = 0; i < (_ewar*3); i++){
            if(DiceRoller() >= _ewarDam){
                ehits += 1;
            }
        }
        //Inflict player cruiser damage on enemy
        while(pcr > 0){
            if(_efighter > 0){
                _efighter -= 1;
                pcr -= 1;
            }
            else if(_edestroyer > 0){
                pcr -= 1;
                _edestroyer -= 1;
            }
            else if(_ecruiser > 0){
                pcr -= 1;
                _ecruiser -= 1;
            }
            else if(_edread > 0){
                pcr -= 1;
                _edread -= 1;
            }
            else if(_ewar > 0){
                pcr -= 1;
                _ewar -= 1;
            }
            else{break;}
        }
        //Inflict enemy cruiser damage on player
        while(ecr > 0){
            if(_pfighter > 0){
                ecr -= 1;
                _pfighter -= 1;
            }
            else if(_pdestroyer > 0){
                ecr -= 1;
                _pdestroyer -= 1;
            }
            else if(_pcruiser > 0){
                ecr -= 1;
                _pcruiser -= 1;
            }
            else if(_pdread > 0){
                ecr -= 1;
                _pdread -= 1;
            }
            else if(_pwar > 0){
                ecr -= 1;
                _pwar -= 1;
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
            else if(_efighter > 0){
                _efighter -= 1;
                phits -= 1;
            }
            else if(_edestroyer > 0){
                phits -= 1;
                _edestroyer -= 1;
            }
            else if(_ecruiser > 0){
                phits -= 1;
                _ecruiser -= 1;
            }
            else if(_edread > 0){
                phits -= 1;
                _edread -= 1;
            }
            else if(_ewar > 0){
                phits -= 1;
                _ewar -= 1;
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
            else if(_pfighter > 0){
                ehits -= 1;
                _pfighter -= 1;
            }
            else if(_pdestroyer > 0){
                ehits -= 1;
                _pdestroyer -= 1;
            }
            else if(_pcruiser > 0){
                ehits -= 1;
                _pcruiser -= 1;
            }
            else if(_pdread > 0){
                ehits -= 1;
                _pdread -= 1;
            }
            else if(_pwar > 0){
                ehits -= 1;
                _pwar -= 1;
            }
            else{break;}
        }

        if(((_pwar + _pdread + _pcruiser + _pdestroyer + _pfighter) <= 0) && ((_ewar + _edread + _ecruiser + _edestroyer + _efighter) <= 0)){
            return "Stalemate";
        }
        else if((_ewar + _edread + _ecruiser + _edestroyer + _efighter) <= 0){
            return "You won and have the following leftover units: \n"
                    .concat("Fighters: ").concat(Integer.toString(_pfighter)).concat("\n")
                    .concat("Destroyers: ").concat(Integer.toString(_pdestroyer)).concat("\n")
                    .concat("Cruisers: ").concat(Integer.toString(_pcruiser)).concat("\n")
                    .concat("Dreadnaughts: ").concat(Integer.toString(_pdread)).concat("\n")
                    .concat("Warsuns: ").concat(Integer.toString(_pwar));
        }
        else if((_pwar + _pdread + _pcruiser + _pdestroyer + _pfighter) <= 0){
            return "You lost and the enemy has the following leftover units: \n"
                    .concat("Fighters: ").concat(Integer.toString(_efighter)).concat("\n")
                    .concat("Destroyers: ").concat(Integer.toString(_edestroyer)).concat("\n")
                    .concat("Cruisers: ").concat(Integer.toString(_ecruiser)).concat("\n")
                    .concat("Dreadnaughts: ").concat(Integer.toString(_edread)).concat("\n")
                    .concat("Warsuns: ").concat(Integer.toString(_ewar));
        }
        if(Database.hasTech(_client.getName(), "Duranium Armor")){
            if(_pwar > pwarsus){
                pwarsus += 1;
            }
            else if(_pdread > pdreadsus){
                pdreadsus += 1;
            }
        }
        if(Database.hasTech(enemy, "Duranium Armor")){
            if(_ewar > ewarsus){
                ewarsus += 1;
            }
            else if(_edread > edreadsus){
                edreadsus += 1;
            }
        }
        return Combat();
    }
}










































