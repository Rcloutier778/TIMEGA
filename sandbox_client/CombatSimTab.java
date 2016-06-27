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
            super(Client.SIMULATOR);
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
            if (Database.hasTech(_client.getName(), "ADT")) {
                while(pADT < _pdestroyer || pADT < _efighter) {
                    for (int i = 0; i < _efighter / 4; i++) {
                        if (DiceRoller() >= (_pfighterDam - 1)) {
                            pADT += 1;
                        }
                    }
                }
            } else {
                for (int i = 0; i < _efighter / 4; i++) {
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
            if (Database.hasTech(enemy, "ADT")) {
                while(eADT < _edestroyer || eADT < _pfighter) {
                    for (int i = 0; i < _pfighter / 4; i++) {
                        if (DiceRoller() >= (_efighterDam - 1)) {
                            eADT += 1;
                        }
                    }
                }
            } else {
                for (int i = 0; i < _pfighter / 4; i++) {
                    if (DiceRoller() >= _efighterDam) {
                        eADT += 1;
                    }
                }
            }
            _pfighter = _pfighter - eADT;
        }

        //Player Assault Cannons
        ppre = 0;
        if(Database.hasTech(_client.getName(), "Assault Cannon") && (_pwar + _pdread + _pcruiser + _pdestroyer + _pfighter) <= 3) {
            for(int i = 0; i<_pcruiser; i++){
                if(DiceRoller() >= _pcruiserDam - 3){
                    ppre += 1;
                }
            }
        }
        else{ppre = 0;}

        //Enemy Assault Cannons
        epre = 0;
        if(Database.hasTech(enemy, "Assault Cannon")&& (_ewar + _edread + _ecruiser + _edestroyer + _efighter) <= 3) {
            for(int i = 0; i<_ecruiser; i++){
                if(DiceRoller() >= _ecruiserDam - 3){
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
                if(Database.hasTech(enemy, "Transfabrication")){
                    _edestroyer += 1;
                }
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
                if(Database.hasTech(_client.getName(), "Transfabrication")){
                    _pdestroyer += 1;
                }
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
     * Normal combat (recursive)
     * @return 1 = Win, 2 = Loss, 3 = Stalemate
     */
    public int Combat(int HM){
        //Tally number of hits player makes
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
        if(Database.hasTech(_client.getName(),"Auxiliary Drones") && _pdread >0){
            if(DiceRoller() >= 7){
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
        if(Database.hasTech(enemy,"Auxiliary Drones") && _edread >0){
            if(DiceRoller() >= 7){
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
                if(Database.hasTech(enemy, "Transfabrication")){
                    _edestroyer += 1;
                }
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
                if(Database.hasTech(_client.getName(), "Transfabrication")){
                    _pdestroyer += 1;
                }
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
                if(Database.hasTech(enemy, "Transfabrication")){
                    _edestroyer += 1;
                }
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
                if(Database.hasTech(_client.getName(), "Transfabrication")){
                    _pdestroyer += 1;
                }
            }
            else if(_pwar > 0){
                ehits -= 1;
                _pwar -= 1;
            }
            else{break;}
        }

        //Stalemate
        if(((_pwar + _pdread + _pcruiser + _pdestroyer + _pfighter) <= 0) && ((_ewar + _edread + _ecruiser + _edestroyer + _efighter) <= 0)){
            return 3;
        }
        //win
        else if((_ewar + _edread + _ecruiser + _edestroyer + _efighter) <= 0){
            return 1;
        }
        //lose
        else if((_pwar + _pdread + _pcruiser + _pdestroyer + _pfighter) <= 0){
            return 2;
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
        if(Database.hasTech(_client.getName(),"Hyper Metabolism")){
            _pdestroyerDam -= 1;
        }
        if(Database.hasTech(enemy, "Hyper Metabolism")){
            _edestroyerDam -= 1;
        }
        return Combat(HM+=1);
    }

    //Average remaining units
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
                pdreadsus = _pdread;
                pwarsus = _pwar;
                edreadsus = _edread;
                ewarsus = _ewar;
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
                avgPfighter += _pfighter;
                avgPdestroyer += _pdestroyer;
                avgPcruiser += _pcruiser;
                avgPdread += _pdread;
                avgPwar += _pwar;
                avgEfighter += _efighter;
                avgEdestroyer += _edestroyer;
                avgEcruiser += _ecruiser;
                avgEdread += _edread;
                avgEwar += _ewar;
                int premdread = _pdread;
                int premdest = _pdestroyer;
                int eremdread = _edread;
                int eremdest = _edestroyer;
                setUnits();
                if(_pdread > premdread && premdest > 0){
                    avgPdread += 1;
                }
                if(_edread > eremdread && eremdest > 0){
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














































