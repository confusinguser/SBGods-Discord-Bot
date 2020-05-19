package com.confusinguser.sbgods.discord.commands;

import com.confusinguser.sbgods.SBGods;
import com.confusinguser.sbgods.discord.DiscordBot;
import com.confusinguser.sbgods.entities.*;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.ArrayList;
import java.util.EventListener;

public class PlayerCommand extends Command implements EventListener {

    public PlayerCommand(SBGods main, DiscordBot discord) {
        this.main = main;
        this.discord = discord;
        this.name = "player";
        this.usage = this.name + " <IGN>";
        this.aliases = new String[]{"user"};
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent e) {
        if (e.getAuthor().isBot() || isNotTheCommand(e) || discord.shouldNotRun(e)) {
            return;
        }

        main.logger.info(e.getAuthor().getName() + " ran command: " + e.getMessage().getContentRaw());

        String[] args = e.getMessage().getContentRaw().split(" ");

        if (args.length <= 1) {
            e.getChannel().sendMessage("Invalid usage! Usage: `" + this.usage + "`").queue();
            return;
        }

        String messageId = e.getChannel().sendMessage("Loading (0/4)").complete().getId();
        e.getChannel().sendTyping().queue();

        Player player = main.getApiUtil().getPlayerFromUsername(args[1]);

        if(player.getDisplayName().equalsIgnoreCase(null)){
            e.getChannel().editMessageById(messageId,"Invalid player " + args[1]).queue();
            return;
        }

        e.getChannel().editMessageById(messageId,"Loading (1/4)").queue();

        TaxPayer taxPayer = new TaxPayer(null,null, null, null, main);

        if(DiscordServer.getDiscordServerFromEvent(e).getServerId() != DiscordServer.SBDGods.getServerId()){
            e.getChannel().sendTyping().queue();
            taxPayer = main.getApiUtil().getTaxPayer(player);
        }

        e.getChannel().sendTyping().queue();
        e.getChannel().editMessageById(messageId,"Loading (2/4)").queue();
        SlayerExp slayerExp = main.getApiUtil().getPlayerSlayerExp(player.getUUID());

        e.getChannel().sendTyping().queue();
        e.getChannel().editMessageById(messageId,"Loading (3/4)").queue();
        SkillLevels skillLevels = main.getApiUtil().getProfileSkillsAlternate(player.getUUID());;
        ArrayList<Pet> totalPets = new ArrayList<>();

        int index = 0;
        for(String profileId : player.getSkyblockProfiles()){
            index++;
            e.getChannel().sendTyping().queue();
            e.getChannel().editMessageById(messageId,"Loading (3/4) [Profile " + index + "/" + player.getSkyblockProfiles().size() + "]").queue();
            SkillLevels profSkillLevels = main.getApiUtil().getProfileSkills(profileId,player.getUUID());

            if(profSkillLevels.getAvgSkillLevel() > skillLevels.getAvgSkillLevel()){
                skillLevels = profSkillLevels;
            }

            e.getChannel().sendTyping().queue();
            e.getChannel().editMessageById(messageId,"Loading (3/4) [Profile " + index + ".5/" + player.getSkyblockProfiles().size() + "]").queue();
            ArrayList<Pet> pets = main.getApiUtil().getProfilePets(profileId, player.getUUID()); // Pets in profile
            totalPets.addAll(pets);
        }


        EmbedBuilder embedBuilder = new EmbedBuilder().setTitle(player.getDisplayName()).setColor(0xb8300b).setThumbnail("https://visage.surgeplay.com/bust/" + player.getUUID()).setFooter("SBGods");

        embedBuilder.addField("Discord",main.getDiscord().getJDA().getUserByTag(player.getDiscordTag()).getAsMention(),false);
        if(!taxPayer.getUuid().equals(null)) {
            embedBuilder.addField("Tax owed", Integer.toString(taxPayer.getOwes()), false);
        }
        embedBuilder.addField("Status", player.getIsOnline() ? "Online" : "Offline", false);
        embedBuilder.addField("Average skill level", String.valueOf(Math.round(skillLevels.getAvgSkillLevel()*100)/100) + (skillLevels.isApproximate() ? " (Approx)" : ""),true);
        embedBuilder.addField("Slayer EXP", String.valueOf(slayerExp.getTotalExp()),true);

        String petStr = "";

        for(Pet pet : totalPets){
            if(pet.isActive()){
                petStr += "\n" + main.getLangUtil().toLowerCaseButFirstLetter(pet.getTier().toString()) + " " + pet.getType() + " (" + pet.getLevel() + ")";
            }
        }

        embedBuilder.addField("Active pets (One per profile)",petStr,false);

        e.getChannel().deleteMessageById(messageId).queue();

        e.getChannel().sendMessage(embedBuilder.build()).queue();
    }
}