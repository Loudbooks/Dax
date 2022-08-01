package me.loudbook.discordlink;

import com.github.steveice10.mc.auth.exception.request.RequestException;
import com.github.steveice10.mc.protocol.packet.ingame.serverbound.ServerboundChatPacket;
import me.loudbook.discordlink.backend.Config;
import me.loudbook.discordlink.backend.Constants;
import me.loudbook.discordlink.discord.Discord;
import me.loudbook.discordlink.minecraft.Minecraft;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        Discord discord = new Discord();
        Config config = new Config();
        Minecraft minecraft = new Minecraft();
        config.validateConfig();
        Constants.getInstance().setDiscord(discord);
        Constants.getInstance().setConfig(config);
        Constants.getInstance().setMinecraft(minecraft);

        Properties prop = Constants.getInstance().getConfig().getProperties();
        Constants.getInstance().getDiscord().connect(prop.getProperty("discord-token"));

        if (Boolean.parseBoolean(prop.get("use-webhook").toString())){
            discord.createWebhookClient(prop.getProperty("webhook-url"));
        }
        String username = prop.getProperty("username");
        String password = prop.getProperty("password");
        String msa_token = prop.getProperty("msa-token");
        try {
            Constants.getInstance().getMinecraft().connect(username, password, msa_token);
        } catch (RequestException e) {
            e.printStackTrace();
            System.out.println("[ERROR] Failed to connect to Minecraft! Is your username, password, and Microsoft token correct?");
        }
/*        Scanner input = new Scanner(System.in);
        String command = input.nextLine();
        if (command.startsWith("execute")){
            String commandFormatted = command.toLowerCase().replace("execute", "").trim();
            if (commandFormatted.contains("/")){
                minecraft.getClient().send(new ServerboundChatPacket(commandFormatted));
            } else {
                minecraft.getClient().send(new ServerboundChatPacket("/" + commandFormatted));
            }
            System.out.println("[Dax] Executed command: " + commandFormatted);
        } else if (command.toLowerCase().startsWith("joinguild")) {
            String guildName = command.toLowerCase().replace("joinguild", "").trim();
            minecraft.getClient().send(new ServerboundChatPacket("/guild join " + guildName));
            System.out.println("[Dax] Sent join command to: " + guildName);

        } else if (command.toLowerCase().startsWith("leaveguild")) {
            minecraft.getClient().send(new ServerboundChatPacket("/g leave"));
            Thread.sleep(120);
            minecraft.getClient().send(new ServerboundChatPacket("/g confirm"));
            System.out.println("[Dax] Left guild");
        } else {
            System.out.println("[ERROR] Unknown command!");
        }*/
    }
}
