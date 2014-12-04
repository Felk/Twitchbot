package de.felk.twitchbot.reaction;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class ReactionConditionedRegex extends ReactionConditioned {

	private Matcher regexMatcher;
	
	public ReactionConditionedRegex(String channel, String sender, boolean mustBeMod, Pattern regexPattern) {
		this(new HashSet<String>(Arrays.asList(new String[]{channel})), new HashSet<String>(Arrays.asList(new String[]{sender})), mustBeMod, regexPattern);
	}
	
	public ReactionConditionedRegex(HashSet<String> channels, String sender, boolean mustBeMod, Pattern regexPattern) {
		this(channels, new HashSet<String>(Arrays.asList(new String[]{sender})), mustBeMod, regexPattern);
	}
	
	public ReactionConditionedRegex(String channel, HashSet<String> senders, boolean mustBeMod, Pattern regexPattern) {
		this(new HashSet<String>(Arrays.asList(new String[]{channel})), senders, mustBeMod, regexPattern);
	}
	
	public ReactionConditionedRegex(HashSet<String> channels, HashSet<String> senders, boolean mustBeMod, Pattern regexPattern) {
		super(channels, senders, mustBeMod);
		this.regexMatcher = regexPattern.matcher("");
	}
	
	public abstract ReactionResult executeAccepted(String channel, String sender, boolean isSenderMod, String message, Date time, Matcher regexMatcher);
		
	@Override
	public ReactionResult executeAccepted(String channel, String sender, boolean isSenderMod, String message, Date time) {
		if (!regexMatcher.reset(message).find()) {
			return null;
		}
		return executeAccepted(channel, sender, isSenderMod, message, time, regexMatcher);
	}

}
