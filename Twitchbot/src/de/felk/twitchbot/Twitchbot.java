package de.felk.twitchbot;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;

import org.jibble.pircbot.IrcException;
import org.jibble.pircbot.PircBot;
import org.jibble.pircbot.User;

import de.felk.twitchbot.reaction.Reaction;
import de.felk.twitchbot.reaction.ReactionResult;

public abstract class Twitchbot extends PircBot {

	private String oauth;
	private boolean quitted = false;
	private boolean outputEnabled = true;

	private String ownChannel;
	private HashSet<String> ops = new HashSet<String>();
	private List<Reaction> reactions = new ArrayList<>();
	private List<String> defaultChannels = new ArrayList<>();
	
	protected final HashSet<String> ANY = null;
	protected final String CONSOLE = "console";
	
	protected String ip = "irc.twitch.tv";
	protected int port = 6667;

	public Twitchbot(String name, String oauth) {

		ownChannel = '#' + name.toLowerCase();
		init();

		this.oauth = oauth;
		this.setName(name);
		this.setVerbose(false);
		this.setMessageDelay(5100);

		addOp(CONSOLE); // don't remove this or you can't use certain commands from the console

		try {
			connect();
		} catch (IOException | IrcException e) {
			System.err.println("Could not connect!");
			e.printStackTrace();
			System.exit(-1);
		}

		// read from System.in and treat as chat input from "console"
		Thread cmdThread = new Thread(new Runnable() {
			public void run() {
				@SuppressWarnings("resource")
				Scanner in = new Scanner(System.in);
				String line = "";
				while (in.hasNextLine()) {
					line = in.nextLine();
					onMessage(CONSOLE, CONSOLE, CONSOLE, CONSOLE, line);
				}
				// CTRL+D
				quit("Console ended inputstream with CTRL+D");
			}
		});
		cmdThread.setDaemon(true);
		cmdThread.start();

	}
	
	public boolean isOutputEnabled() {
		return outputEnabled;
	}

	public void setOutputEnabled(boolean outputEnabled) {
		this.outputEnabled = outputEnabled;
	}

	public boolean addReaction(Reaction reaction) {
		return reactions.add(reaction);
	}

	public boolean addDefaultChannel(String channel) {
		return defaultChannels.add(channel);
	}

	public boolean addOp(String op) {
		return ops.add(op);
	}

	public boolean removeOp(String op) {
		return ops.remove(op);
	}

	public HashSet<String> getOps() {
		return ops;
	}

	public String getOwnChannel() {
		return ownChannel;
	}

	protected abstract void init();

	private void connect() throws IOException, IrcException {
		this.connect(ip, port, oauth);
		out("Connected!");
		joinChannels();
	}

	private void joinChannels() {
		for (String channel : defaultChannels) {
			joinChannel(channel);
			out("Joined default channel: " + channel);
		}
	}

	protected void quit(String reason) {
		if (quitted) {
			return; // already done
		}
		quitted = true;
		disconnect();
		dispose();
		out("BYE! Quitreason: " + reason);
	}

	@Override
	protected void onDisconnect() {
		// only reconnect if the connection was manually closed
		if (!quitted) {
			out("Connection lost! Trying to reconnect...");
			for (int i = 0; i < 10 && !isConnected(); i++) {
				try {
					out("Reconnecting (attempt " + (i + 1) + ") ...");
					reconnect();
				} catch (IOException | IrcException e1) {
					e1.printStackTrace();
					out("Connection attempt failed. Retrying in 10 seconds...");
					try {
						Thread.sleep(10000); // wait 10 seconds before next attempt
					} catch (InterruptedException e2) {
						e2.printStackTrace();
					}
				}
			}
			if (isConnected()) {
				out("Reconnecting successful!");
				joinChannels();
			} else {
				quit("Reconnecting was unsuccessful");
			}
		}
	}

	@Override
	public synchronized void onMessage(String channel, String sender, String login, String hostname, String message) {
		onMessageTime(channel, sender, login, hostname, message, new Date());
	}
		
	public synchronized void onMessageTime(String channel, String sender, String login, String hostname, String message, Date time) {

		boolean senderIsChannelMod = false;
		for (User user : getUsers(channel)) {
			if (sender.equalsIgnoreCase(user.getNick())) {
				if (user.isOp()) {
					senderIsChannelMod = true;
				}
			}
		}

		ReactionResult result;
		for (Reaction reaction : reactions) {
			result = reaction.execute(channel, sender, senderIsChannelMod, message, time);
			if (result != null) {
				if (result.getChannel().equalsIgnoreCase(CONSOLE)) {
					out(result.getText());
				} else if (isOutputEnabled()) {
					sendMessage(result.getChannel(), result.getText());
				}
			}
		}

	}

	public static void out(String str) {
		System.out.println(new SimpleDateFormat("dd-MM-yyyy HH:mm").format(new Date()) + " - " + str);
	}

}
