package de.felk.twitchbot.reaction;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;

public abstract class ReactionConditioned implements Reaction {

	private HashSet<String> channels, senders;
	private boolean mustBeMod;

	public ReactionConditioned(String channel, String sender, boolean mustBeMod) {
		this(new HashSet<String>(Arrays.asList(new String[] { channel })), new HashSet<String>(Arrays.asList(new String[] { sender })), mustBeMod);
	}

	public ReactionConditioned(HashSet<String> channels, String sender, boolean mustBeMod) {
		this(channels, new HashSet<String>(Arrays.asList(new String[] { sender })), mustBeMod);
	}

	public ReactionConditioned(String channel, HashSet<String> senders, boolean mustBeMod) {
		this(new HashSet<String>(Arrays.asList(new String[] { channel })), senders, mustBeMod);
	}

	public ReactionConditioned(HashSet<String> channels, HashSet<String> senders, boolean mustBeMod) {
		this.channels = channels;
		this.senders = senders;
		this.mustBeMod = mustBeMod;
	}

	public abstract ReactionResult executeAccepted(String channel, String sender, boolean isSenderMod, String message, Date time);

	@Override
	public ReactionResult execute(String channel, String sender, boolean isSenderMod, String message, Date time) {
		if (mustBeMod && !isSenderMod) {
			return null;
		}
		if (channels != null && !channels.contains(channel)) {
			return null;
		}
		if (senders != null && !senders.contains(sender)) {
			return null;
		}
		return executeAccepted(channel, sender, isSenderMod, message, time);
	}
}
