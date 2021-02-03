package com.confusinguser.dynamicvcs.listeners;

import com.confusinguser.dynamicvcs.utils.VCUtil;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

public class VCListener extends ListenerAdapter {

    private void onGuildVoiceJoin(Member member, VoiceChannel vc) {
        List<Role> vcRoles = member.getGuild().getRolesByName("In " + vc.getName(), false);

        if (vcRoles.isEmpty()) member.getGuild().addRoleToMember(member, member.getGuild().createRole().setName("In " + vc.getName()).complete()).queue();
        else for (Role role : member.getGuild().getRolesByName("In " + vc.getName(), false)) {
            member.getGuild().addRoleToMember(member, role).queue();
        }

        if (vc.getMembers().size() == 1 && VCUtil.shouldAffectChannel(vc)) {
            String actualChannelName = VCUtil.actualChannelName(vc.getName());
            int currHighestChannelNum = VCUtil.getChannelNumber(vc.getName());
            if (currHighestChannelNum == -1) return;

            boolean oneChannelEmpty = false;
            for (VoiceChannel channel : vc.getGuild().getChannels().stream().filter(c -> c instanceof VoiceChannel).filter(c -> VCUtil.actualChannelName(c.getName()).equals(actualChannelName)).map(c -> (VoiceChannel) c).collect(Collectors.toSet())) {
                currHighestChannelNum = Math.max(VCUtil.getChannelNumber(channel.getName()), currHighestChannelNum);

                if (channel.getMembers().size() == 0) oneChannelEmpty = true;
            }

            Category parent = vc.getParent();
            if (!oneChannelEmpty && parent != null) {
                VoiceChannel newVC = vc.createCopy().setName(actualChannelName + " " + (currHighestChannelNum + 1)).complete();
                parent.modifyVoiceChannelPositions().selectPosition(newVC).moveTo(vc.getPosition() + 1).queue();
            }
        }
    }

    private void onGuildVoiceLeave(Member member, VoiceChannel vc) {
        for (Role role : member.getRoles()) {
            if (role.getName().equals("In " + vc.getName())) {
                member.getGuild().removeRoleFromMember(member, role).queue();
            }
        }

        if (vc.getMembers().isEmpty() && VCUtil.shouldAffectChannel(vc)) {
            String actualChannelName = VCUtil.actualChannelName(vc.getName());
            int currHighestChannelNum = VCUtil.getChannelNumber(vc.getName());
            if (currHighestChannelNum == -1) return;

            List<VoiceChannel> affectedVCs = vc.getGuild().getChannels().stream()
                    .filter(c -> c instanceof VoiceChannel)
                    .filter(c -> VCUtil.actualChannelName(c.getName()).equals(actualChannelName))
                    .map(c -> (VoiceChannel) c).collect(Collectors.toList());

            List<VoiceChannel> collect = affectedVCs.stream().filter(c -> c.getMembers().size() == 0).collect(Collectors.toList());
            for (int i = 0; i < collect.size(); i++) {
                if (i == 0) continue;
                VoiceChannel vcLoop = collect.get(i);
                vcLoop.delete().queue();
            }
        }
    }

    @Override
    public void onGuildVoiceJoin(@NotNull GuildVoiceJoinEvent event) {
        onGuildVoiceJoin(event.getMember(), event.getChannelJoined());
    }

    @Override
    public void onGuildVoiceMove(@NotNull GuildVoiceMoveEvent event) {
        onGuildVoiceJoin(event.getMember(), event.getChannelJoined());
        onGuildVoiceLeave(event.getMember(), event.getChannelLeft());
    }

    @Override
    public void onGuildVoiceLeave(@NotNull GuildVoiceLeaveEvent event) {
        onGuildVoiceLeave(event.getMember(), event.getChannelLeft());
    }
}
