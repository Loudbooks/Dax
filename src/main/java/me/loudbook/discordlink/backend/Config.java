package me.loudbook.discordlink.backend;

import lombok.Getter;
import me.loudbook.discordlink.discord.Discord;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;


public class Config {
    @Getter
    private final Properties properties;

    public Config() {
        String configFilePath = "./config.properties";
        FileInputStream propsInput = null;
        try {
            propsInput = new FileInputStream(configFilePath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        this.properties = new Properties();
        try {
            this.properties.load(propsInput);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void validateConfig(){
        if (this.properties.getProperty("discord-token") == null) {
            System.out.println("[ERROR] discord-token is not set in config.properties");
            System.exit(1);
        }
        if (this.properties.getProperty("msa-token") == null) {
            System.out.println("[ERROR] msa-token is not set in config.properties");
            System.exit(1);
        }
        if (this.properties.getProperty("main-channel-id") == null) {
            System.out.println("[ERROR] main-channel is not set in config.properties");
            System.exit(1);
        }
        if (this.properties.getProperty("officer-channel-id") == null) {
            System.out.println("[ERROR] officer-channel is not set in config.properties");
            System.exit(1);
        }
        if (this.properties.getProperty("use-webhook") == null) {
            System.out.println("[ERROR] use-webhook is not set in config.properties");
            System.exit(1);
        }
        if (Boolean.parseBoolean(this.properties.getProperty("use-webhook"))) {
            if (this.properties.getProperty("webhook-url") == null) {
                System.out.println("[ERROR] webhook-url is not set in and use-webhook is true.");
                System.exit(1);
            }
        } else if (this.properties.getProperty("embed-color") == null) {
            System.out.println("[ERROR] embed-color is not set and use-webhook is false.");
            System.exit(1);
        }
    }
}
