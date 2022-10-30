package me.loudbook.discordlink.minecraft;

import com.github.steveice10.mc.auth.exception.request.RequestException;
import com.github.steveice10.mc.protocol.packet.ingame.clientbound.ClientboundChatPacket;
import com.github.steveice10.mc.protocol.packet.ingame.clientbound.ClientboundLoginPacket;
import com.github.steveice10.mc.protocol.packet.ingame.serverbound.ServerboundChatPacket;
import com.github.steveice10.packetlib.Session;
import com.github.steveice10.packetlib.event.session.DisconnectedEvent;
import com.github.steveice10.packetlib.event.session.SessionAdapter;
import com.github.steveice10.packetlib.packet.Packet;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.loudbook.discordlink.backend.Config;
import me.loudbook.discordlink.backend.Constants;
import me.loudbook.discordlink.discord.Discord;
import net.dv8tion.jda.api.entities.TextChannel;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;

import java.io.IOException;
import java.util.ArrayList;

public class MinecraftListener extends SessionAdapter {
    /**
     * @param client Minecraft account client
     * @param packet Packet received
     */
    @Override
    public void packetReceived(Session client, Packet packet) {
        Discord discord = Constants.getInstance().getDiscord();
        Config config = Constants.getInstance().getConfig();
        if (packet instanceof ClientboundLoginPacket) {
            for (int i = 0; i < 16; i++) {
                client.send(new ServerboundChatPacket("/"));
            }
        } else if (packet instanceof ClientboundChatPacket) {
            Component message = ((ClientboundChatPacket) packet).getMessage().asComponent();
            TextChannel textChannel = discord.getMainChannel();
            assert textChannel != null;
            String gson = GsonComponentSerializer.gson().serialize(message);
            JsonObject jsonObject = (JsonObject) new JsonParser().parse(gson);
            ArrayList<String> messagesID = discord.getMessageIds();
            if (jsonObject.get("text").getAsString().equals("You cannot say the same message twice!") || jsonObject.get("text").getAsString().contains("You can only chat every")) {
                try {
                    textChannel.retrieveMessageById(messagesID.get(0)).queue((messageFail) -> messageFail.addReaction("❌").queue());
                    messagesID.remove(0);
                } catch (NullPointerException e) {
                    TextChannel officerChannel = discord.getOfficerChannel();
                    officerChannel.retrieveMessageById(messagesID.get(0)).queue((messageFail) -> messageFail.addReaction("❌").queue());
                    messagesID.remove(0);
                }
            }
            String str = null;
            String author = null;
            String authorSub = null;
            String authorFormatted = null;
            try {
                str = jsonObject.getAsJsonArray("extra").get(1).getAsJsonObject().get("text").getAsString().replace("*", "\\*");
                authorSub = jsonObject.getAsJsonArray("extra").get(0).getAsJsonObject().get("text").getAsString();
                author = authorSub.substring(10);
            } catch (Exception ignored) {
                //This means it's a chat message we don't care about!
            }
            if (str == null)return;
            if (author != null) {
                authorFormatted = author
                        .replace("§3", "")
                        .replace(":", "")
                        .replaceFirst("\\[[^\\]]+\\]", "")
                        .trim().replace("§f", "")
                        .replace("§f", "")
                        .replace("§0", "")
                        .replace("§1", "")
                        .replace("§2", "")
                        .replace("§3", "")
                        .replace("§5", "")
                        .replace("§6", "")
                        .replace("§7", "")
                        .replace("§9", "")
                        .replace("§a", "")
                        .replace("§b", "")
                        .replace("§c", "")
                        .replace("§d", "")
                        .replace("§e", "")
                        .replace("§f", "").trim();
            }
            String authorjoinLeave = jsonObject.getAsJsonArray("extra").get(0).getAsJsonObject().get("text").getAsString().trim();
            if (str.contains("joined.")){
                String joinMessage = authorjoinLeave + " joined!";
                discord.sendEmbed("", joinMessage, authorjoinLeave);
                return;
            } else if (str.contains("left.")) {
                String leftMessage = authorjoinLeave + " left!";
                discord.sendEmbed("", leftMessage, authorjoinLeave);
                return;
            }
            if (authorFormatted == null) return;
            String authorLink = authorFormatted.replaceFirst("\\[[^\\]]+\\]", "").trim();

            Minecraft.MessageType type = Minecraft.MessageType.PUBLIC;

            if (authorSub.contains("Officer >")){
                type = Minecraft.MessageType.OFFICER;
            }
            if (authorSub.contains(config.getProperties().getProperty("ign"))){
                return;
            }
            if (!authorSub.contains("Guild >")){
                if (!authorSub.contains("Officer >")){
                    return;
                }
            }
            if (type == Minecraft.MessageType.PUBLIC) {
                if (Boolean.parseBoolean(config.getProperties().getProperty("use-webhook"))) {
                    try {
                        discord.sendWebhook(str, authorFormatted, "https://minotar.net/helm/" + authorFormatted);
                    } catch (IOException e) {
                        e.printStackTrace();
                        System.out.println("Failed to send message to webhook. Is your URL valid?");
                    }
                } else {
                    discord.sendEmbed(str, authorFormatted, authorLink);
                }
            } else {
                discord.sendMessage("**" + authorFormatted.replace(">", "").trim() + "**: " + str, discord.getOfficerChannel());
            }
        }

    }
    @Override
    public void disconnected(DisconnectedEvent event) {
        System.out.println("Disconnected: " + event.getReason());
        if (event.getCause() != null) {
            event.getCause().printStackTrace();
        }
        try {
            Constants.getInstance().getMinecraft().connect(Constants.getInstance().getMinecraft().getUsername(), Constants.getInstance().getMinecraft().getPassword(), Constants.getInstance().getMinecraft().getMsaToken());
        } catch (RequestException e) {
            e.printStackTrace();
        }
    }
}
