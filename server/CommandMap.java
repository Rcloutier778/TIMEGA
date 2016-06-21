package server;

/**
 * Maps command line input to the appropriate server-level instructions
 */

import sandbox_client.Protocol;

import java.util.HashMap;

public class CommandMap {

	// maps string command to instructions
	private HashMap<String,Command> _map = new HashMap<String,Command>();
	
	// maps tab name to whether it's currently enabled
	private HashMap<String,Boolean> _enabled = new HashMap<String,Boolean>();
	
	private Main _main;
	
	public CommandMap(Main main) {
		// populate map
		_main = main;
		for(String tab : ServerDatabase.TABS) {
			_enabled.put(tab, false);
		}
		_map.put("enable", new EnableCommand());
		_map.put("disable", new DisableCommand());
		_map.put("clear", new ClearCommand());
		_map.put("chown", new ChownCommand());
		
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
		
	}
	
	// given a command line input, execute relevant instructions
	public void parse(String command) {
		String[] args = command.split(" ");
		if(_map.containsKey(args[0])) {
			_map.get(args[0]).run(args);
		} else {
			Main.writeColortext("no command \"" + args[0] + "\" found", Main.ERROR);
		}
	}
	
	public boolean getEnabled(String tab) {
		return _enabled.get(tab);
	}
	
	// enable a single tab
	private class EnableCommand implements Command {
		public void run(String[] args) {
			if(args.length < 2) {
				Main.writeColortext("usage: enable <tab>", Main.ERROR);
				return;
			}
			
			String tab = args[1];
			boolean valid = false;
			for(String tabName : ServerDatabase.TABS) {
				valid = valid || tabName.equals(tab);
			}
			
			if(tab.equals("all")) {
				this.enableAll();
			} else if(!valid) {
				Main.writeColortext("no tab " + tab, Main.ERROR);
			} else if(_enabled.get(tab)) {
				Main.writeColortext(tab + " tab is already enabled", Main.ERROR);
			} else {
				_main.broadcast(ServerDatabase.PROTOCOL.get(tab + "_en"));
				Main.writeColortext(tab + " tab enabled", Main.SERVEROUT);
				_enabled.put(tab, true);
			}
		}
		
		// or, if "all" is given instead, enable all of them
		private void enableAll() {
			boolean any = false;
			for(String tab : ServerDatabase.TABS) {
				if(_enabled.get(tab)) {
					// do nothing
				} else {
					any = true;
					_enabled.put(tab, true);
					_main.broadcast(ServerDatabase.PROTOCOL.get(tab + "_en"));
					Main.writeColortext(tab + " tab enabled", Main.SERVEROUT);
				}
			}
			
			if(!any) {
				Main.writeColortext("all tabs already enabled", Main.ERROR);
			}
		}
	}
	
	// disable a single tab
	private class DisableCommand implements Command {

		public void run(String[] args) {
			if(args.length < 2) {
				Main.writeColortext("usage: disable <tab>", Main.ERROR);
				return;
			}
			
			String tab = args[1];
			boolean valid = false;
			for(String tabName : ServerDatabase.TABS) {
				valid = valid || tabName.equals(tab);
			}
			
			if(tab.equals("all")) {
				this.disableAll();
			} else if(!valid) {
				Main.writeColortext("no tab " + tab, Main.ERROR);
			} else if(_enabled.get(tab)) {
				_main.broadcast(ServerDatabase.PROTOCOL.get(tab + "_dis"));
				Main.writeColortext(tab + " tab disabled", Main.SERVEROUT);
				_enabled.put(tab, false);
			} else {
				Main.writeColortext(tab + " tab is already disabled", Main.ERROR);
			}
		}
		
		// or if "all" is given, disable all of them
		private void disableAll() {
			boolean none = true;
			for(String tab : ServerDatabase.TABS) {
				if(_enabled.get(tab)) {
					none = false;
					_enabled.put(tab, false);
					_main.broadcast(ServerDatabase.PROTOCOL.get(tab + "_dis"));
					Main.writeColortext(tab + " tab disabled", Main.SERVEROUT);
				}
			}
			
			if(none) {
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
			if(args.length < 3) {
				Main.writeColortext("usage:  " + _usage, Main.ERROR);
				return;
			} 
			
			String target = args[2];
			for(int i=3; i<args.length; i++) {
				target += " " + args[i];
			}
			
			if(!ServerDatabase.hasName(args[1])) {
				Main.writeColortext("player " + args[1] + " not found", Main.ERROR);
			} else if(!this.validTarget(target)) {
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
			if(args.length != 3) {
				Main.writeColortext("usage: resolution <agenda_1> <agenda_2>", Main.ERROR);
				return;
			} 
			
			String res1 = args[1].replace("_", " ");
			String res2 = args[2].replace("_", " ");
			
			if(!ServerDatabase.RESOLUTION_SET.contains(res1)) {
				Main.writeColortext("resolution \"" + res1 + "\" not found", Main.ERROR);
			} else if(!ServerDatabase.RESOLUTION_SET.contains(res2)) {
				Main.writeColortext("resolution \"" + res2 + "\" not found", Main.ERROR);
			} else {
				_main.broadcast(Protocol.SEND_RESOLUTION, res1 + "\n" + res2 + "\n");
			}
		}
		
	}
	
}
