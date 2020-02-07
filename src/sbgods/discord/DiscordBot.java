package sbgods.discord;

import javax.security.auth.login.LoginException;

import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import sbgods.SBGods;
import sbgods.discord.commands.HelpCommand;
import sbgods.discord.commands.SkillCommand;
import sbgods.discord.commands.SlayerCommand;

public class DiscordBot {

	public String commandPrefix = "-";
	public String leaderboard_channel_id = "673619910324387885";
	private Message lastLeaderboardMessage;
	private JDA jda;

	public DiscordBot(SBGods main) throws LoginException {
		String token = "NjY0OTAwNzM0NTk2NDE1NDg4.Xj2D3Q.pYjtn9QFrmQ-aZnj5Mhg9i8uuUM";

		jda = new JDABuilder(AccountType.BOT)
				.setToken(token)
				.setStatus(OnlineStatus.ONLINE)
				.setActivity(Activity.playing("Type -help for help!"))
				.addEventListeners(new SlayerCommand(main, this), new SkillCommand(main, this), new HelpCommand(main, this))
				.build();
	}

	public boolean isValidCommand(String command) {
		String[] validCommands = {"slayer", "skills", "help"};
		for (String validCommand : validCommands) {
			if (command.equalsIgnoreCase(validCommand)) {
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