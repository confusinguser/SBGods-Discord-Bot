package sbgods.discord;

import java.util.ArrayList;
import java.util.Arrays;

import javax.security.auth.login.LoginException;

import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import sbgods.SBGods;
import sbgods.discord.commands.Command;
import sbgods.discord.commands.HelpCommand;
import sbgods.discord.commands.SkillCommand;
import sbgods.discord.commands.SlayerCommand;

public class DiscordBot {

	public final String commandPrefix = "-";
	public final String leaderboard_channel_id = "673619910324387885";

	private JDA jda;

	public SlayerCommand slayerCommand;
	public SkillCommand skillCommand;
	public HelpCommand helpCommand;
	public ArrayList<Command> commands;

	private Message lastLeaderboardMessage;

	public DiscordBot(SBGods main) throws LoginException {
		String token = "NjY0OTAwNzM0NTk2NDE1NDg4.Xj2D3Q.pYjtn9QFrmQ-aZnj5Mhg9i8uuUM";

		slayerCommand = new SlayerCommand(main, this);
		skillCommand = new SkillCommand(main, this);
		helpCommand = new HelpCommand(main, this);

		commands = new ArrayList<Command>(Arrays.asList(slayerCommand, skillCommand, helpCommand));

		jda = new JDABuilder(AccountType.BOT)
				.setToken(token)
				.setStatus(OnlineStatus.ONLINE)
				.setActivity(Activity.playing("Type -help for help!"))
				.addEventListeners(commands)
				.build();
	}

	public boolean isValidCommand(String command) {
		for (Command validCommand : commands) {
			String validCommandString = validCommand.name;
			if (command.equalsIgnoreCase(validCommandString)) {
				return true;
			}
		}
		return false;
	}

	public JDA getJDA() {
		return jda;
	}

	public boolean isLeaderboardChannel(MessageReceivedEvent e) {
		if (e.getChannel().getId().contentEquals(leaderboard_channel_id)) {
			return true;
		} else {
			return false;
		}
	}

	public void setLastLeaderboardMessage(Message contentRaw) {
		lastLeaderboardMessage = contentRaw;
	}

	public Message getLastLeaderboardMessage() {
		return lastLeaderboardMessage;
	}
}