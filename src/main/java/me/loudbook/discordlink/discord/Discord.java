package me.loudbook.discordlink.discord;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.WebhookClientBuilder;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import lombok.Getter;
import lombok.Setter;
import me.loudbook.discordlink.backend.Config;
import me.loudbook.discordlink.backend.Constants;
import me.loudbook.discordlink.minecraft.Minecraft;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.json.JSONObject;

import javax.net.ssl.HttpsURLConnection;
import javax.security.auth.login.LoginException;
import java.awt.*;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
@Getter
public class Discord {
    private JDA jda;
    @Setter
    private Message latestPublicMessage;
    @Setter
    private Message latestOfficerMessage;
    @Setter
    private Minecraft.MessageType latestMessageType;
    private WebhookClient guildWebhookClient;
    private WebhookClient officerWebhookClient;
    private boolean connected = false;

    private Config config;
    /**
     * @param token The Discord bot token.
     */
    public void connect(String token){
        try {
            this.jda = JDABuilder.createDefault(token)
                    .enableIntents(GatewayIntent.GUILD_MEMBERS)
                    .setChunkingFilter(ChunkingFilter.ALL)
                    .addEventListeners(new DiscordListener())
                    .setMemberCachePolicy(MemberCachePolicy.ALL)
                    .setStatus(OnlineStatus.DO_NOT_DISTURB)
                    .build();
            jda.awaitReady();
            connected = true;
            System.out.println("[Dax] Connected to Discord!");
        } catch (LoginException | InterruptedException e) {
            e.printStackTrace();
        }
        this.config = new Config();
        Discord discord = Constants.getInstance().getDiscord();
        if (discord.getGuild() == null) {
            System.out.println("[ERROR] Discord server is invalid in config.properties");
            System.exit(1);
        }
        if (discord.getMainChannel() == null) {
            System.out.println("[ERROR] Main channel ID is invalid in config.properties");
            System.exit(1);
        }
        if (discord.getOfficerChannel() == null) {
            System.out.println("[ERROR] Officer Channel is invalid in config.properties");
            System.exit(1);
        }
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Dax has started successfully!");
        eb.setDescription("""
                Thank you for using Dax!

                Dax was created by Loudbook#0001, please contact them if you have any questions.
                
                GitHub Repository: https://github.com/Loudbooks/Dax
                You can support Dax at my PayPal: https://paypal.me/Loudbook""");
        eb.setThumbnail("https://lh3.googleusercontent.com/hr8NNPfbCgV7mClA5povGuLpf3E9xZ7uP8aJI1Fr58SvO8tNdQHVIVrHcspa1KFYlVBcjn16mYGQT3QLOCRrqcNTIpgeazaHp7y_8BA037N8J-QP7L1m7MDrYNbZPulTQBMwnG5MK_z_14Adzfg8G1zKijNLjSal7jKSMBImga1Mj-g9eUK8jc2hv889vbBdLUe0z2ukveLKO-ZHZ3_gVdjtlaAAUrTNgGM___NIpgWDai_pzULlwWgl5ob6QNEKLV8M8MEf5g9Lz9UPyIJUjKtTe6okrr6EY-oLl5TWKsN3BxYuPw3hrZpzvvJIX4qsn5ClGmu1OL8V5JRBGNZ_ofAhhjA7EeElfZj18XRYaDOZFcv61pQIiliB7MCHupXacDwymH6bn0m2mbXRrGrSwW9lUluwJUq6eRDMfdOmOThLhZfb8ihScFFeMtrMhVhrfGGHOAOzW5-CnMFEU6DplCSZ72ysxZ0fgTceEV6X7GCsk4hNdtIstbeAEmTGmRjqUD2BvACPWeIUALAtx1I0UbH0rNGTi4B-Rlzg5lvoMkVLN632IeGflRFKxOILVbHcaBWxhrfhxdT1nli91Jz-94jrrgFxUEAKW84-rSsgo6CkA5V83ya7YrbLymM_1uAFRiPV0ajC88edP4aX6p2KA1j97QFmijXmyH_OEJST6JccJsowPclGZW0EvmWlcxm3O5DFBVipo09gGnmmFX4DTPeGmGZxNcvOaE71GqoMoA-yY1gC8UAdqqYXAfqIFrwo0f4sYNtOYYsgnBG8d4Ek0dcKn9VDo_BS1ymtBXtrp3horQzrRdeihi4t2Oz6hTmt8MFZYAUge-Gcq_oqAeJLE10rvjXUCKlQAifU5tVp=w1200-h600-no?authuser=1.png");
        getMainChannel().sendMessageEmbeds(eb.build()).queue();
    }

    /**
     * @param url The webhook URL.
     */
    public void createWebhookClient(String url, Minecraft.MessageType type){
        WebhookClientBuilder builder = new WebhookClientBuilder(url); // or id, token
        builder.setThreadFactory((job) -> {
            Thread thread = new Thread(job);
            thread.setName(type.name());
            thread.setDaemon(true);
            return thread;
        });
        builder.setWait(true);
        if (type == Minecraft.MessageType.PUBLIC) {
            this.guildWebhookClient = builder.build();
        } else {
            this.officerWebhookClient = builder.build();
        }
    }

    public TextChannel getMainChannel(){
        return this.jda.getTextChannelById(config.getProperties().getProperty("main-channel-id"));
    }

    public TextChannel getOfficerChannel(){
        return this.jda.getTextChannelById(config.getProperties().getProperty("officer-channel-id"));
    }

    public Guild getGuild(){
        return this.jda.getGuildById(config.getProperties().getProperty("discord-server-id"));
    }

    /**
     * @param message The message to send.
     * @param author The author of the message.
     * @param avatar The avatar of the author.
     * @throws IOException If the webhook URL is invalid, or various other http errors.
     */
    public void sendWebhook(String message, String author, String avatar, Minecraft.MessageType type) throws IOException {
        WebhookMessageBuilder builder = new WebhookMessageBuilder();
        builder.setUsername(author);
        builder.setAvatarUrl(avatar);
        builder.setContent(message);
        switch (type) {
            case PUBLIC -> this.getGuildWebhookClient().send(builder.build());
            case OFFICER -> this.getOfficerWebhookClient().send(builder.build());
        }
    }

    /**
     * @param message The message to send.
     * @param author The author of the message.
     * @param avatar The avatar of the author.
     */
    public void sendEmbed(String message, String author, String avatar, Minecraft.MessageType type){
        EmbedBuilder eb = new EmbedBuilder();
        if (author.contains("left")){
            eb.setColor(0xFF0000);
        } else if (author.contains("joined")){
            eb.setColor(0x00FF00);
        } else {
            eb.setColor(Color.getColor("#" + this.config.getProperties().getProperty("embed-color")));
        }
        eb.setFooter("Dax");
        eb.setDescription(message);
        eb.setAuthor(author, "https://plancke.io/hypixel/player/stats/" + avatar, "https://minotar.net/helm/" + avatar);
        TextChannel channel = getMainChannel();

        if (type == Minecraft.MessageType.OFFICER) channel = getOfficerChannel();

        channel.sendMessageEmbeds(eb.build()).queue();
    }

    /**
     * @param message The message to send.
     * @param channel The channel to send the message to.
     */
    public void sendMessage(String message, TextChannel channel){
        channel.sendMessage(message).queue();
    }
}
