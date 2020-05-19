package com.confusinguser.sbgods.discord.commands;

import com.confusinguser.sbgods.SBGods;
import com.confusinguser.sbgods.discord.DiscordBot;
import com.confusinguser.sbgods.entities.Pet;
import com.confusinguser.sbgods.entities.Player;
import com.confusinguser.sbgods.entities.SkillLevels;
import com.confusinguser.sbgods.entities.SlayerExp;
import com.confusinguser.sbgods.utils.Constants;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.EventListener;
import java.util.HashMap;
import java.util.Map;

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

        String messageId = e.getChannel().sendMessage("Loading (0/2)").complete().getId();
        e.getChannel().sendTyping().queue();

        Player player = main.getApiUtil().getPlayerFromUsername(args[1]);

        if (player.getDisplayName() == null) {
            e.getChannel().editMessageById(messageId, "Invalid player " + args[1]).queue();
            return;
        }

        e.getChannel().sendTyping().queue();
        e.getChannel().editMessageById(messageId, "Loading (1/2)").queue();
        SkillLevels skillLevels = main.getApiUtil().getProfileSkillsAlternate(player.getUUID());
        ArrayList<Pet> totalPets = new ArrayList<>();


        SlayerExp slayerExp;
        long totalMoney = 0;

        Map<String, Integer> slayerOutput = new HashMap<>();
        for (String slayer_type : Constants.slayer_types) {
            slayerOutput.put(slayer_type, 0);
        }

        int index = 0;
        for (String profileId : player.getSkyblockProfiles()) {
            index++;
            e.getChannel().sendTyping().queue();
            e.getChannel().editMessageById(messageId, "Loading (1/2) [Profile " + (index - 1) + ".33/" + player.getSkyblockProfiles().size() + "]").queue();
            SkillLevels profSkillLevels = main.getApiUtil().getProfileSkills(profileId, player.getUUID());

            if (profSkillLevels.getAvgSkillLevel() > (skillLevels.isApproximate() ? skillLevels.getAvgSkillLevel() - 2 : skillLevels.getAvgSkillLevel())) { //if approx remove 2 levels in calc due to bug idk but i think this fix
                skillLevels = profSkillLevels;
            }

            e.getChannel().sendTyping().queue();
            e.getChannel().editMessageById(messageId, "Loading (1/2) [Profile " + (index - 1) + ".66/" + player.getSkyblockProfiles().size() + "]").queue();
            ArrayList<Pet> pets = main.getApiUtil().getProfilePets(profileId, player.getUUID()); // Pets in profile
            totalPets.addAll(pets);

            //Putting slayer here to make the loading animation smoother (can animate for every profile)


            e.getChannel().sendTyping().queue();
            e.getChannel().editMessageById(messageId, "Loading (1/2) [Profile " + index + "/" + player.getSkyblockProfiles().size() + "]").queue();

            String response = main.getApiUtil().getResponse(main.getApiUtil().BASE_URL + "skyblock/profile" + "?key=" + main.getNextApiKey() + "&profile=" + profileId, 600000);
            if (response == null) continue;

            JSONObject jsonObject = new JSONObject(response);

            JSONObject jsonObjectSlayer = new JSONObject();
            try {
                jsonObjectSlayer = jsonObject.getJSONObject("profile").getJSONObject("members").getJSONObject(player.getUUID()).getJSONObject("slayer_bosses");
            } catch (JSONException ignored) {
            }

            for (String slayer_type : Constants.slayer_types) {
                try {
                    slayerOutput.put(slayer_type, slayerOutput.get(slayer_type) + jsonObjectSlayer.getJSONObject(slayer_type).getInt("xp"));
                } catch (JSONException ignored) {
                }
            }

            try {
                totalMoney += jsonObject.getJSONObject("profile").getJSONObject("banking").getLong("balance");
            } catch (Exception ignore) {
            }

            for (String profMemberUuid : jsonObject.getJSONObject("profile").getJSONObject("members").keySet()) {
                try {
                    totalMoney += jsonObject.getJSONObject("profile").getJSONObject("members").getJSONObject(profMemberUuid).getLong("coin_purse");
                } catch (Exception ignore) {
                }
            }
        }

        slayerExp = new SlayerExp(slayerOutput);

        EmbedBuilder embedBuilder = new EmbedBuilder().setTitle(player.getDisplayName()).setColor(0xb8300b).setThumbnail("https://visage.surgeplay.com/bust/" + player.getUUID()).setFooter("SBGods");
        User discordUser = main.getDiscord().getJDA().getUserByTag(player.getDiscordTag());

        if (player.getDiscordTag() != null && discordUser != null)
            embedBuilder.addField("Discord", discordUser.getAsMention(), false);
        embedBuilder.addField("Status", player.isOnline() ? "Online" : "Offline", false);
        embedBuilder.addField("Guild", main.getApiUtil().getGuildFromUUID(player.getUUID()), false);
        embedBuilder.addField("Average skill level", ((double) Math.round(skillLevels.getAvgSkillLevel() * 100)) / 100 + (skillLevels.isApproximate() ? " (Approx)" : ""), true);
        embedBuilder.addField("Slayer EXP", main.getLangUtil().prettifyInt(slayerExp.getTotalExp()), true);
        embedBuilder.addField("Total money (All coops)", main.getLangUtil().prettifyLong(totalMoney), true);

        StringBuilder petStr = new StringBuilder();

        for (Pet pet : totalPets) {
            if (pet.isActive()) {
                petStr.append("\n").append(main.getLangUtil().toLowerCaseButFirstLetter(pet.getTier().toString())).append(" ").append(pet.getType()).append(" (").append(pet.getLevel()).append(")");
            }
        }

        embedBuilder.addField("Active pets (One per profile)", petStr.toString(), false);

        e.getChannel().deleteMessageById(messageId).queue();

        e.getChannel().sendMessage(embedBuilder.build()).queue();
    }
}