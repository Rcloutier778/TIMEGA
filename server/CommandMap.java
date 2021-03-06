package server;

/**
 * Maps command line input to the appropriate server-level instructions
 * 
 * @author dmayans
 */

import sandbox_client.Protocol;

import java.io.File;
import java.util.HashMap;

public class CommandMap {

	// maps string command to instructions
	private HashMap<String, Command> _map = new HashMap<String, Command>();

	// maps tab name to whether it's currently enabled
	private HashMap<String, Boolean> _enabled = new HashMap<String, Boolean>();

	private Main _main;

	public CommandMap(Main main) {
		// populate map
		_main = main;
		for (String tab : ServerDatabase.TABS.keySet()) {
			_enabled.put(tab, false);
		}
		_map.put("enable", new EnableCommand());
		_map.put("disable", new DisableCommand());
		_map.put("clear", new ClearCommand());
		_map.put("chown", new ChownCommand());
		_map.put("reload", new ReloadCommand());
		_map.put("sync", new Command() {
			@Override
			public void run(String[] args) {
				_main.broadcast(Protocol.END_ROUND);
			}
		});

		// sorry so much is hardcoded >.<
		_map.put("research", new ResearchCommand());
		_map.put("forget", new ForgetCommand());
		_map.put("hire", new HireCommand());
		_map.put("release", new ReleaseCommand());
		_map.put("resolutions", new ResolutionCommand());
		_map.put("resresult", new ResolutionWinCommand());
		_map.put("help", new HelpCommand());
		//_map.put("vote", new VoteCommand());
		_map.put("vote", new Command() {
			@Override
			public void run(String[] args) {
				_main.broadcast(Protocol.VOTE);
			}
		});
		_map.put("totalvotes", new TotalVotesCommand());

	}

	// given a command line input, execute relevant instructions
	public void parse(String command) {
		String[] args = command.split(" ");
		if (_map.containsKey(args[0])) {
			_map.get(args[0]).run(args);
		} else {
			Main.writeColortext("no command \"" + args[0] + "\" found", Main.ERROR);
		}
	}

	public boolean getEnabled(String tab) {
		return _enabled.get(tab);
	}

	private class HelpCommand implements Command{
		public void run(String[] args){
			if(args.length == 1){
				for(String s : _map.keySet()){
					Main.writeColortext(s + "\n", Main.SERVEROUT);
				}
				return;
			}
			if(args.length == 2){
				if(_map.containsKey(args[1])){
					if(args[1].equals("enable")){
						Main.writeColortext("Enables the desired tab or all tabs", Main.SERVEROUT);
					}else if(args[1].equals("disable")){
						Main.writeColortext("Disables the desired tab or all tabs", Main.SERVEROUT);
					}else if(args[1].equals("clear")){
						Main.writeColortext("Clears the terminal", Main.SERVEROUT);
					}else if(args[1].equals("chown")){
						Main.writeColortext("Captures a planet for a player", Main.SERVEROUT);
					}else if(args[1].equals("reload")){
						Main.writeColortext("Reloads game data from a save file", Main.SERVEROUT);
					}else if(args[1].equals("sync")){
						Main.writeColortext("Ends the round, syncs the client and server databses", Main.SERVEROUT);
					}else if(args[1].equals("research")){
						Main.writeColortext("Researches a tech for a player", Main.SERVEROUT);
					}else if(args[1].equals("forget")){
						Main.writeColortext("Removes a tech from a player", Main.SERVEROUT);
					}else if(args[1].equals("hire")){
						Main.writeColortext("Hires a personnel for a player", Main.SERVEROUT);
					}else if(args[1].equals("release")){
						Main.writeColortext("Fires a personnel for a player", Main.SERVEROUT);
					}else if(args[1].equals("resolutions")){
						Main.writeColortext("Sets the resolutions to be voted on.\nIf repealing, Repeal must be entered first with the law to be repealed entered after the second resolution.", Main.SERVEROUT);
					}else if(args[1].equals("resresult")) {
						Main.writeColortext("Sets the result of the current resolution. ", Main.SERVEROUT);
						//todo finish up council tab before doing repeals
					}else if(args[1].equals("vote")){
						Main.writeColortext("Tallies the votes for the current resolutions", Main.SERVEROUT);
					}else if(args[1].equals("totalvotes")){
						Main.writeColortext("Returns the total votes for/against for a given resolution and the turn order", Main.SERVEROUT);
					}
				}else {
					Main.writeColortext("No command \"" + args[1] + "\" found", Main.ERROR);
				}
			}

		}
	}

	// enable a single tab
	private class EnableCommand implements Command {
		public void run(String[] args) {
			if (args.length < 2) {
				Main.writeColortext("usage: enable <tab>", Main.ERROR);
				return;
			}

			String tab = args[1];

			if (tab.equals("all")) {
				this.enableAll();
			} else if (!ServerDatabase.TABS.containsKey(tab)) {
				Main.writeColortext("no tab " + tab, Main.ERROR);
			} else if (_enabled.get(tab)) {
				Main.writeColortext(tab + " tab is already enabled", Main.ERROR);
			} else {
				_main.broadcast(Protocol.ENABLE, ServerDatabase.TABS.get(tab) + "\n");
				Main.writeColortext(tab + " tab enabled", Main.SERVEROUT);
				_enabled.put(tab, true);
			}
		}

		// or, if "all" is given instead, enable all of them
		private void enableAll() {
			boolean any = false;
			for (String tab : ServerDatabase.TABS.keySet()) {
				if (_enabled.get(tab)) {
					// do nothing
				} else {
					any = true;
					_enabled.put(tab, true);
					_main.broadcast(Protocol.ENABLE, ServerDatabase.TABS.get(tab) + "\n");
					Main.writeColortext(tab + " tab enabled", Main.SERVEROUT);
				}
			}

			if (!any) {
				Main.writeColortext("all tabs already enabled", Main.ERROR);
			}
		}
	}

	//Reload from txt file
	private class ReloadCommand implements Command {
		public void run(String[] args) {
			if (args.length < 2) {
				Main.writeColortext("usage: reload <file name>", Main.ERROR);
				return;
			}

			if (new File(System.getProperty("user.dir") + "/" + args[1] + ".txt").exists()) {
				_main.broadcastReload(args[1] + ".txt");
			} else {
				Main.writeColortext("no file " + args[1], Main.ERROR);
				return;
			}
		}
	}

	// disable a single tab
	private class DisableCommand implements Command {

		public void run(String[] args) {
			if (args.length < 2) {
				Main.writeColortext("usage: disable <tab>", Main.ERROR);
				return;
			}

			String tab = args[1];

			if (tab.equals("all")) {
				this.disableAll();
			} else if (!ServerDatabase.TABS.containsKey(tab)) {
				Main.writeColortext("no tab " + tab, Main.ERROR);
			} else if (_enabled.get(tab)) {
				_main.broadcast(Protocol.DISABLE, ServerDatabase.TABS.get(tab) + "\n");
				Main.writeColortext(tab + " tab disabled", Main.SERVEROUT);
				_enabled.put(tab, false);
			} else {
				Main.writeColortext(tab + " tab is already disabled", Main.ERROR);
			}
		}

		// or if "all" is given, disable all of them
		private void disableAll() {
			boolean none = true;
			for (String tab : ServerDatabase.TABS.keySet()) {
				if (_enabled.get(tab)) {
					none = false;
					_enabled.put(tab, false);
					_main.broadcast(Protocol.DISABLE, ServerDatabase.TABS.get(tab) + "\n");
					Main.writeColortext(tab + " tab disabled", Main.SERVEROUT);
				}
			}

			if (none) {
				Main.writeColortext("all tabs already disabled", Main.ERROR);
			}
		}

	}

	// clear terminal
	private class ClearCommand implements Command {
		public void run(String[] args) {
			// definitely not hacky.
			System.out.print("\033[H\033[2J");
		}
	}

	// used to simplify all of the upcoming private classes, I promise
	private abstract class BasicCommand implements Command {

		protected abstract boolean validTarget(String t);

		protected abstract void broadcast(String target, String name, String clientName, int color);

		private String _usage;

		public BasicCommand(String usage) {
			_usage = usage;
		}

		public void run(String[] args) {
			if (args.length < 3) {
				Main.writeColortext("usage:  " + _usage, Main.ERROR);
				return;
			}

			String target = args[2];
			for (int i = 3; i < args.length; i++) {
				target += " " + args[i];
			}

			if (!ServerDatabase.hasName(args[1])) {
				Main.writeColortext("player " + args[1] + " not found", Main.ERROR);
			} else if (!this.validTarget(target)) {
				Main.writeColortext("target \"" + target + "\" not found", Main.ERROR);
			} else {
				this.broadcast(target, args[1], "[stdin] " + args[1] + " ", Main.SERVEROUT);
			}
		}

	}

	// change planet owner
	private class ChownCommand extends BasicCommand {

		public ChownCommand() {
			super("chown <player name> <planet name>");
		}

		@Override
		protected boolean validTarget(String t) {
			return ServerDatabase.PLANETS.containsKey(t);
		}

		@Override
		protected void broadcast(String target, String name, String clientName, int color) {
			_main.broadcastChown(target, name, clientName, color);
		}

	}

	private class ResearchCommand extends BasicCommand {

		public ResearchCommand() {
			super("research <player name> <tech name>");
		}

		@Override
		protected boolean validTarget(String t) {
			return ServerDatabase.TECH_SET.contains(t);
		}

		@Override
		protected void broadcast(String target, String name, String clientName, int color) {
			_main.broadcastTech(name, target, clientName, color);
		}
	}

	private class ForgetCommand extends BasicCommand {

		public ForgetCommand() {
			super("forget <player name> <tech name>");
		}

		@Override
		protected boolean validTarget(String t) {
			return ServerDatabase.TECH_SET.contains(t);
		}

		@Override
		protected void broadcast(String target, String name, String clientName, int color) {
			_main.broadcastForget(name, target, clientName, color);
		}
	}

	private class HireCommand extends BasicCommand {

		public HireCommand() {
			super("hire <player name> <personnel>");
		}

		@Override
		protected boolean validTarget(String t) {
			return ServerDatabase.PERSONNEL_SET.contains(t);
		}

		@Override
		protected void broadcast(String target, String name, String clientName, int color) {
			_main.broadcastHire(name, target, clientName, color);
		}
	}

	private class ReleaseCommand extends BasicCommand {

		public ReleaseCommand() {
			super("release <player name> <personnel>");
		}

		@Override
		protected boolean validTarget(String t) {
			return ServerDatabase.PERSONNEL_SET.contains(t);
		}

		@Override
		protected void broadcast(String target, String name, String clientName, int color) {
			_main.broadcastRelease(name, target, clientName, color);
		}
	}


	// added in at the last second
	private class ResolutionCommand implements Command {

		public void run(String[] args) {
			if (args.length < 3 || args.length > 5) {
				Main.writeColortext("usage: resolution <agenda_1> <agenda_2> <repeal_1> <repeal_2>", Main.ERROR);
				return;
			}

			String res1 = args[1].replace("_", " ");
			String res2 = args[2].replace("_", " ");
			String res3 = "";
			String res4 = "";
			if(args.length > 3){
				res3 = args[3].replace("_"," ");
			}
			if(args.length > 4){
				res4 = args[4].replace("_"," ");
			}
			if (!ServerDatabase.RESOLUTION_SET.contains(res1)) {
				Main.writeColortext("resolution \"" + res1 + "\" not found", Main.ERROR);
			} else if (!ServerDatabase.RESOLUTION_SET.contains(res2)) {
				Main.writeColortext("resolution \"" + res2 + "\" not found", Main.ERROR);
			} else if (!ServerDatabase.RESOLUTION_SET.contains(res3) && args.length>3) {
				Main.writeColortext("resolution \"" + res3 + "\" not found", Main.ERROR);
			}else if (!ServerDatabase.RESOLUTION_SET.contains(res4) && args.length>4) {
				Main.writeColortext("resolution \"" + res4 + "\" not found", Main.ERROR);
			}else{
				_main.broadcastResolution(res1, res2, res3, res4);
			}
		}

	}

	private class ResolutionWinCommand implements Command {
		//todo what if second resolution is repeal?
		public void run(String[] args) {
			if (args.length < 3) {
				Main.writeColortext("usage: resresult <result_1> <result_2>", Main.ERROR);
				return;
			}
			String[] result = new String[2];
			result[0] = args[1];
			result[1] = args[2];
			if (!(args[1].equals("for") || args[1].equals("against")) ||!(args[2].equals("for") || args[2].equals("against")) ) {
				Main.writeColortext("usage: resresult <for/against> <for/against>", Main.ERROR);
			}else{
				_main.broadcastResolutionResult(result);
			}
		}

	}

	private class TotalVotesCommand implements Command{
		public void run(String[] args){
			if(args.length != 2){
				Main.writeColortext("usage: totalvotes <resolution>", Main.ERROR);
				return;
			}
			String res = args[1].replace("_"," ");
			if(!ServerDatabase.VOTES_BY_RESOLUTION.containsKey(res)){
				Main.writeColortext("No such resolution: " + res, Main.ERROR);
			}else{
				ServerDatabase.VOTES_BY_RESOLUTION_LOCK.lock();
				Main.writeColortext("For: " + ServerDatabase.VOTES_BY_RESOLUTION.get(res)[0] +
						" Against: " + ServerDatabase.VOTES_BY_RESOLUTION.get(res)[1],Main.SERVEROUT);
				ServerDatabase.VOTES_BY_RESOLUTION_LOCK.unlock();
			}
		}
	}
}
























