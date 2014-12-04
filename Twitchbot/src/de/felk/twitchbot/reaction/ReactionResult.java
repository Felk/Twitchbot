package de.felk.twitchbot.reaction;

public class ReactionResult {

	private String channel, text;
	
	public ReactionResult(String channel, String text) {
		this.channel = channel;
		this.text = text;
	}
	
	public String getChannel() {
		return channel;
	}
	
	public String getText() {
		return text;
	}
	
}
