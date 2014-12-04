package de.felk.twitchbot;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.felk.twitchbot.reaction.Reaction;
import de.felk.twitchbot.reaction.ReactionConditioned;
import de.felk.twitchbot.reaction.ReactionConditionedRegex;
import de.felk.twitchbot.reaction.ReactionResult;

public class ExampleBot extends Twitchbot {

	/*
	 * Make an instance of your custom Bot to start it.
	 */
	public static void main(String[] args) {
		if (args.length < 2) {
			System.out.println("Need arguments USERNAME OAUTH for twitch.tv authentification");
			System.exit(-1);
		}
		new ExampleBot(args[0], args[1]);
	}

	/**
	 * Create a new custom twitch bot!
	 * 
	 * @param name
	 *            Twitch.TV account name
	 * @param oauth
	 *            Twitch.TV oauth (authentification key). Get yours at http://twitchapps.com/tmi/
	 */
	public ExampleBot(String name, String oauth) {
		super(name, oauth);
		// init more stuff here when needed
	}

	/**
	 * Use this method to programm your custom bot! You can add channels to join, global OPs and behaviours.
	 * This is called very early, before any connection is established or stuff.
	 * For "after-init" stuff, use the constructor.
	 */
	@Override
	protected void init() {

		/* make this bot always join the channel #felkbot
		 * IRC channels always start with "#"!
		 * Only exception is the constant "CONSOLE", which represents the command line as user and channel
		 * the twitchbot will join these channels, also after a reconnect
		 */
		String mainChannel = "#felkbot";
		addDefaultChannel(mainChannel);

		// add "felkcraft" as an global OP
		addOp("felkcraft");

		/* ADD YOUR CUSTOM BOT BEHAVIOUR BY ADDING REACTIONS TO CHAT MESSAGES!
		 * addReaction( <interface> Reaction );
		 * Implement the interface on your own OR
		 * use one of the 2 handy abstract classes (see below).
		 */

		/* quit-command. You should always have something like this.
		 * should be executable from everywhere by OPs.
		 * "ReactionConditioned" can be used if you want to only "listen" to certain users or channels.
		 * Takes these parameters in constructor: HashSet/String channels, HashSet/String users, boolean mustBeMod (must the user be a mod of the current channel)
		 * Input the constant "ANY" (which is a null of type HashSet<String>) for ANY channel or user.
		 */
		Reaction quitCommand = new ReactionConditioned(ANY, getOps(), false) {
			public ReactionResult executeAccepted(String channel, String sender, boolean isSenderMod, String message, Date time) {
				// messages reaching this method are already filtered by user and channel
				if (message.startsWith("!quit")) {
					quit("The bot is shutting down now. blahblah");
				}
				// return null for no response/output
				return null;
			}
		};

		// simple greeting reaction in main channel by any user
		Reaction greetingReaction = new ReactionConditioned(mainChannel, ANY, false) {
			public ReactionResult executeAccepted(String channel, String sender, boolean isSenderMod, String message, Date time) {
				if (message.contains("hello")) {
					// return a ReactionResult(channel, text) object as a response
					return new ReactionResult(channel, "Hello, " + sender + "!");
				}
				return null;
			}
		};

		/* Now a reaction, that parses some user-input.
		 * You want every MOD (that's the "true" below) of your channel to be able to bet virtual money on either "blue" or "red" in your channel.
		 * "ReactionConditionedRegex" does the same as ReactionConditioned, but also checks against a regular expression!
		 */
		Reaction betReaction = new ReactionConditionedRegex(mainChannel, ANY, true, Pattern.compile("^!bet 0*([0-9]{1,9}) (blue|red)($|\\s)", Pattern.CASE_INSENSITIVE)) {
			public ReactionResult executeAccepted(String channel, String sender, boolean isSenderMod, String message, Date time, Matcher regexMatcher) {
				// you can do the same as before, plus retrieving the matched groups of the regular expression!
				int bet = Integer.parseInt(regexMatcher.group(1)); // must be parsable because of regex
				boolean onBlue = regexMatcher.group(2).equalsIgnoreCase("blue");
				// do something with the data
				addBetting(sender, bet, onBlue);
				// log to console
				return new ReactionResult(CONSOLE, "successfully parsed a bet! blahblah");
			}
		};

		addReaction(quitCommand);
		addReaction(greetingReaction);
		addReaction(betReaction);

		// furthermore, if you want to mute your bot in all channels (except the console),
		// you can call setOutputEnabled(true/false). This will mute all "ReactionResult"s

	}

	private void addBetting(String user, int bet, boolean onBlue) {
		// dummy method
	}

}
