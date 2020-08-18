package com.confusinguser.sbgods.discord;

import com.confusinguser.sbgods.SBGods;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import org.jetbrains.annotations.NotNull;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class MessageListener extends ListenerAdapter {

    final SBGods main;
    final DiscordBot discord;

    public MessageListener(SBGods main, DiscordBot discord) {
        this.main = main;
        this.discord = discord;
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent e) {
        if (discord.shouldNotRun(e)) {
            return;
        }

        if(e.getChannel().getId().equals("736164363731337297") || e.getChannel().getId().equals("692303914304929802")){ //Splash leech channel
        //if(e.getChannel().getId().equals("744558150426034268")){ //Splash leech channel (test server)

            Runnable target = () -> {
                String message = null;
                try {
                    message = URLEncoder.encode("&2" + e.getMember().getEffectiveName() + ": &c" + e.getMessage().getContentDisplay(), StandardCharsets.UTF_8.toString());
                    SBGods.getInstance().getApiUtil().getNonHypixelResponse("http://soopymc.my.to/api/sbgDiscord/newLeechMessage.json?key=HoVoiuWfpdAjJhfTj0YN&timestamp=" + new Date().getTime() + "&message=" + message);
                } catch (UnsupportedEncodingException unsupportedEncodingException) {
                    unsupportedEncodingException.printStackTrace();
                }
            };
            new Thread(target).start();
        }

        if (e.getChannel().getName().contains("verif") && e.getMember() != null &&
                (!e.getMember().hasPermission(Permission.MANAGE_SERVER) || e.getAuthor().isBot())) {
            e.getMessage().delete().queueAfter(30, TimeUnit.SECONDS);
        } else if (e.getMessage().getMentions(Message.MentionType.USER).stream().anyMatch((mention) -> discord.getJDA().getSelfUser().getId().equals(mention.getId()))) {
            switch ((int) Math.ceil(new Random().nextDouble() * 4)) {
                case 1:
                    e.getChannel().sendMessage("\"All men can see these tactics whereby I conquer, but what none can see is the strategy out of which victory is evolved.\"\n       - Sun Tzu, The Art of War").queue();
                    break;
                case 2:
                    e.getChannel().sendMessage("\"The opportunity to secure ourselves against defeat lies in our own hands, but the opportunity of defeating the enemy is provided by the enemy himself.\"\n       - Sun Tzu, The Art of War").queue();
                    break;
                case 3:
                    e.getChannel().sendMessage("\"The supreme art of war is to subdue the enemy without fighting.\"\n       - Sun Tzu, The Art of War").queue();
                    break;
                case 4:
                    MessageAction messageAction = e.getChannel().sendMessage("Ever wanted to know how high the Eiffel Tower is? No, me neither, but anyway here is the wikipedia page for it (oh, did i mention it's in french?):\nLa tour Eiffel Écouter est une tour de fer puddlé de 324 mètres de hauteur (avec antennes)o 1 située à Paris, à l’extrémité nord-ouest du parc du Champ-de-Mars en bordure de la Seine dans le 7e arrondissement. Son adresse officielle est 5, avenue Anatole-France2.\n" +
                            "\n" +
                            "Construite par Gustave Eiffel et ses collaborateurs pour l’Exposition universelle de Paris de 1889, et initialement nommée « tour de 300 mètres », elle est devenue le symbole de la capitale française et un site touristique de premier plan : il s’agit du troisième site culturel français payant le plus visité en 2015, avec 5,9 millions de visiteurs en 20163. Depuis son ouverture au public, elle a accueilli plus de 300 millions de visiteurs4.\n" +
                            "\n" +
                            "D’une hauteur de 312 mètreso 1 à l’origine, la tour Eiffel est restée le monument le plus élevé du monde pendant quarante ans. Le second niveau du troisième étage, appelé parfois quatrième étage, situé à 279,11 mètres, est la plus haute plateforme d'observation accessible au public de l'Union européenne et la deuxième plus haute d'Europe, derrière la tour Ostankino à Moscou culminant à 337 mètres. La hauteur de la tour a été plusieurs fois augmentée par l’installation de nombreuses antennes. Utilisée dans le passé pour de nombreuses expériences scientifiques, elle sert aujourd’hui d’émetteur de programmes radiophoniques et télévisés.");

                    byte[] eiffelTower = main.getApiUtil().getEiffelTowerImage();
                    if (eiffelTower != null)
                        messageAction.addFile(eiffelTower, "eiffelTower.jpg");
                    messageAction.queue();
                    break;
            }
        }
    }
}
