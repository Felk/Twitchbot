package de.felk.twitchbot.reaction;

import java.util.Date;

public interface Reaction {

	public ReactionResult execute(String channel, String sender, boolean isSenderMod, String message, Date time);
	
}
