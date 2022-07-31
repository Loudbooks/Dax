package me.loudbook.discordlink.discord;

import lombok.Getter;
import me.loudbook.discordlink.backend.Config;
import me.loudbook.discordlink.backend.Constants;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Guild;
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
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class Discord {
    @Getter
    private JDA jda;
    @Getter
    private final ArrayList<String> messageIds;

    private Config config;
    public Discord(){
        this.messageIds = new ArrayList<>();
    }

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

    public TextChannel getMainChannel(){
        return this.jda.getTextChannelById(config.getProperties().getProperty("main-channel-id"));
    }

    public TextChannel getOfficerChannel(){
        return this.jda.getTextChannelById(config.getProperties().getProperty("officer-channel-id"));
    }

    public Guild getGuild(){
        return this.jda.getGuildById(config.getProperties().getProperty("discord-server-id"));
    }

    public void sendWebhook(String message, String author, String avatar, String webhookUrl) throws IOException {
        JSONObject json = new JSONObject();
        json.put("content", message);
        json.put("username", author);
        json.put("avatar_url", avatar);
        json.put("tts", false);
        URL url = new URL(webhookUrl);
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        connection.addRequestProperty("Content-Type", "application/json");
        connection.addRequestProperty("User-Agent", "Hypixel Guild Discord bridge by Loudbook");
        connection.setDoOutput(true);
        connection.setRequestMethod("POST");
        OutputStream stream = connection.getOutputStream();
        stream.write(json.toString().getBytes(StandardCharsets.UTF_8));
        stream.flush();
        stream.close();
        connection.getInputStream().close();
        connection.disconnect();
    }

    public void sendEmbed(String message, String author, String avatar){
        EmbedBuilder eb = new EmbedBuilder();
        if (author.contains("left")){
            eb.setColor(0xFF0000);
        } else if (author.contains("joined")){
            eb.setColor(0x00FF00);
        } else {
            eb.setColor(Color.getColor("#" + this.config.getProperties().getProperty("embed-color")));
        }
        eb.setFooter("Dax Guild Bridge by Loudbook#0001", "https://lh3.googleusercontent.com/-msDr4kjOaL3VvyCgOg5DlRchCCrtjDNY_WfCWq60QG9vqDCJH5FDqiAcPTQ666pFrlcT-D-96B5f6t9baj3s-fIjfvLifC3y94wb3IPEMDOU1vM_N-ALUe2WrXc7-5-8-Ipz-9EOkDGOuDyFTtjNSChTPEF0HW_8ljPcaKBcl44SucJACm_McU6qWa2xVxupc4C5pORdO3c2EVxqwc1EWlDJqrFZYwsvsUeO8gxMkRTR6_0HMNbIe_MIGxLcEDR4YpleW9nmfAUP4ETPfEO5YhDMoDe7lJSUO_h3vepFE5mW2Jqn_4B078_DFFRMtXKd2urbLVXGJTaGcNduhe-GFJ7gTJGWzmBrK93XShxeCsaKVGAqiRrWeT3IVRwDGnd0Mf0v6sYgr28xShsaLRTE4cPoPsB_1yoKpsz2SdeZY4xw5CymZ0v8CAmVJpr8EwBjSTkWixdzwkl1JiJygY7bAAw7umZNghcs-lP894yahRLaa51ARimM3TXhq6EJU5cIQYQff2_ZFE4rvlUjT8tdB8VMltNU6foSsuv3ENt4FF6WY84-pbZtFYnnivmNj14GAkInTvhYNfZiQ0wChkapy_XHDvq8a87h-h_hDSTbM4QQsUdF8P5qbMvyBJYNX7vS0x_FWpo5c6T61A8X2wIJNFeZzW1CJQV3GaQEW8fvQP1M-1Joxhw6mX0rGI6_q93yRlyIPu6Loc22Egr1cg71WLnj2AxP8Ko5U3G2XL32EKx1z9ZGAmr4I9KSS0nvOnBX-ynW-zZUBzh9-ZiownSouw_yoRgLw4iNnVhRsG-r2KgSAh4n2yoye58u-R_xtJe9zgAGkKZ15M3-417loN7r70lq9WY2W8FuNepWQiJ=w287-h218-no?authuser=1.png");
        eb.setDescription(message);
        eb.setAuthor(author, "https://plancke.io/hypixel/player/stats/" + avatar, "https://minotar.net/helm/" + avatar);
        TextChannel channel = getMainChannel();
        channel.sendMessageEmbeds(eb.build()).queue();
    }

    public void sendMessage(String message, TextChannel channel){
        channel.sendMessage(message).queue();
    }
}
