package com.confusinguser.sbgods.discord.commands;

import com.confusinguser.sbgods.SBGods;
import com.confusinguser.sbgods.discord.DiscordBot;
import com.confusinguser.sbgods.entities.DiscordServer;
import com.confusinguser.sbgods.entities.Player;
import com.confusinguser.sbgods.utils.ApiUtil;
import com.confusinguser.sbgods.utils.Util;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

public class VerifyCommand extends Command {

    public VerifyCommand(SBGods main, DiscordBot discord) {
        this.main = main;
        this.discord = discord;
        this.name = "verify";
        this.aliases = new String[]{"v"};
    }

    @Override
    public void handleCommand(MessageReceivedEvent e, @NotNull DiscordServer currentDiscordServer, @NotNull Member senderMember, String[] args) {
        if (!e.getChannel().getName().toLowerCase().contains("verify") && !e.getChannel().getName().toLowerCase().contains("bot")) {
            e.getChannel().sendMessage(main.getMessageByKey("command_cannot_be_used_in_channel")).queue();
            return;
        }

        if (args.length >= 2) {
            // Check if that is actual player ign
            Player player = ApiUtil.getPlayerFromUsername(args[1]);
            if (player == null || player.getDiscordTag() == null) {
                e.getChannel().sendMessage(e.getAuthor().getAsMention() + " could not find MC account " + args[1]).queue();
                return;
            }
            if (player.getDiscordTag().equalsIgnoreCase(e.getAuthor().getAsTag())) {
                switch (Util.verifyPlayer(senderMember, player.getDisplayName(), e.getGuild(), e.getChannel())) {
                    case 0:
                        e.getChannel().sendMessage(e.getAuthor().getAsMention() + " you are already verified to the account mc account " + player.getDisplayName()).queue();
                        return;
                    case 2:
                        e.getChannel().sendMessage("Bot is still loading the leaderboards! Try again in a few minutes").queue();
                        return;
                    default:
                        main.logger.fine("Added " + currentDiscordServer + " verified role to " + e.getAuthor().getAsTag());
                        return;
                }
            }

            // Send error message saying to link discord account with mc
            switch (currentDiscordServer) {
                case SBGods:
                    e.getChannel().sendMessage(e.getAuthor().getAsMention() + " you are already verified or you need to link your minecraft account with discord (see <#743066201806340137>)").queue();
                    break;
                case SBForceful:
                    e.getChannel().sendMessage(e.getAuthor().getAsMention() + " you are already verified or you need to link your minecraft account with discord (see <#711462220852101170>)").queue();
                    break;
                default:
                    e.getChannel().sendMessage(e.getAuthor().getAsMention() + " you are already verified or you need to link your minecraft account with discord (see welcome channel)").queue();
                    break;
            }
        } else {
            String mcName = ApiUtil.getMcNameFromDisc(e.getAuthor().getAsTag());
            if (mcName.isEmpty()) {
                e.getChannel().sendMessage("There was an error auto-detecting your minecraft ign. Please do -verify <IGN>").queue();
                return;
            }

            // Verify player with mc name mcName
            Util.verifyPlayer(senderMember, mcName, e.getGuild(), e.getChannel());
        }
    }
}
