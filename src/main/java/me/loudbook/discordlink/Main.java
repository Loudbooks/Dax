package me.loudbook.discordlink;

import com.github.steveice10.mc.auth.exception.request.RequestException;
import me.loudbook.discordlink.backend.Config;
import me.loudbook.discordlink.backend.Constants;
import me.loudbook.discordlink.discord.Discord;
import me.loudbook.discordlink.minecraft.Minecraft;

import java.io.IOException;
import java.util.Properties;

public class Main {
    public static void main(String[] args) throws RequestException {
        Discord discord = new Discord();
        Config config = new Config();
        Minecraft minecraft = new Minecraft();

        config.validateConfig();

        Constants.getInstance().setDiscord(discord);
        Constants.getInstance().setConfig(config);
        Constants.getInstance().setMinecraft(minecraft);

        Properties prop = Constants.getInstance().getConfig().getProperties();

        String msa_token = prop.getProperty("msa-token");

        try {
            Constants.getInstance().getMinecraft().connect(msa_token);
        } catch (RequestException | IOException e) {
            throw new RequestException("Failed to connect to Minecraft!");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        Constants.getInstance().getDiscord().connect(prop.getProperty("discord-token"));
        if (Boolean.parseBoolean(prop.get("use-webhook").toString())){
            discord.createWebhookClient(prop.getProperty("guild-webhook-url"), Minecraft.MessageType.PUBLIC);
            discord.createWebhookClient(prop.getProperty("officer-webhook-url"), Minecraft.MessageType.OFFICER);
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down...");
            Constants.getInstance().getMinecraft().getClient().disconnect("Shutting down...");
        }));
    }
}
