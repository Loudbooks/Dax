package me.loudbook.discordlink.discord;

import com.github.steveice10.mc.protocol.packet.ingame.serverbound.ServerboundChatPacket;
import com.github.steveice10.packetlib.Session;
import me.loudbook.discordlink.backend.Constants;
import me.loudbook.discordlink.minecraft.Minecraft;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class DiscordListener extends ListenerAdapter {
    /**
     * @param e Message received event.
     */
    @Override
    public void onMessageReceived(MessageReceivedEvent e) {
        Discord discord = Constants.getInstance().getDiscord();
        if (e.getAuthor().isBot()) return;
        if (e.getChannelType() == ChannelType.TEXT) {
            String message = e.getMessage().getContentDisplay();
            Member member = discord.getGuild().getMember(e.getAuthor());
            String author;
            if (member != null && member.getNickname() != null) {
                author = member.getNickname();
            } else {
                author = e.getAuthor().getName();
            }
            Minecraft minecraft = Constants.getInstance().getMinecraft();
            Session client = minecraft.getClient();
            if (client == null) return;
            if (!(message.chars().count() > 255)) {
                if (e.getTextChannel().getId().equals(discord.getMainChannel().getId())) {
                    client.send(new ServerboundChatPacket("/gc " + author + ": " + message));
                    discord.getMessageIds().add(e.getMessage().getId());
                } else if (e.getTextChannel().getId().equals(discord.getOfficerChannel().getId())){
                    client.send(new ServerboundChatPacket("/go " + author + ": " + message));
                    discord.getMessageIds().add(e.getMessage().getId());
                }
            } else {
                e.getMessage().reply("Message is too long, canceling send.").queue();
            }
        }
    }
}
