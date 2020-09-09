package com.confusinguser.sbgods.discord;

import com.confusinguser.sbgods.SBGods;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;
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
        Member member = e.getMember();
        if (discord.shouldNotRun(e) || member == null) return;

        if (e.getChannel().getId().equals("736164363731337297") || e.getChannel().getId().equals("692303914304929802")) { //Splash leech channel
            //if(e.getChannel().getId().equals("744558150426034268")){ //Splash leech channel (test server)

            Runnable target = () -> {
                String message;
                try {
                    message = URLEncoder.encode("&2" + member.getEffectiveName() + ": &c" + e.getMessage().getContentDisplay(), StandardCharsets.UTF_8.toString());
                    SBGods.getInstance().getApiUtil().getNonHypixelResponse("http://soopymc.my.to/api/sbgDiscord/newLeechMessage.json?key=HoVoiuWfpdAjJhfTj0YN&timestamp=" + new Date().getTime() + "&sbgods=true&message=" + message);
                } catch (UnsupportedEncodingException unsupportedEncodingException) {
                    unsupportedEncodingException.printStackTrace();
                }
            };
            new Thread(target).start();
        }
        if(e.getChannel().getId().equals("746069131022827560")){ //Splash leech channel
            //if(e.getChannel().getId().equals("744558150426034268")){ //Splash leech channel (test server)

            Runnable target = () -> {
                String message = null;
                try {
                    message = URLEncoder.encode("&2" + member.getEffectiveName() + ": &c" + e.getMessage().getContentDisplay(), StandardCharsets.UTF_8.toString());
                    SBGods.getInstance().getApiUtil().getNonHypixelResponse("http://soopymc.my.to/api/sbgDiscord/newLeechMessage.json?key=HoVoiuWfpdAjJhfTj0YN&timestamp=" + new Date().getTime() + "&sbgods=false&message=" + message);
                } catch (UnsupportedEncodingException unsupportedEncodingException) {
                    unsupportedEncodingException.printStackTrace();
                }
            };
            new Thread(target).start();
        }

        if (e.getChannel().getName().contains("verif") &&
                (!member.hasPermission(Permission.MANAGE_SERVER) || e.getAuthor().isBot())) {
            e.getMessage().delete().queueAfter(30, TimeUnit.SECONDS);
        }
    }
}
