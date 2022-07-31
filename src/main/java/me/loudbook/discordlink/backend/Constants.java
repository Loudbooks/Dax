package me.loudbook.discordlink.backend;

import lombok.Getter;
import lombok.Setter;
import me.loudbook.discordlink.discord.Discord;
import me.loudbook.discordlink.minecraft.Minecraft;

@Getter
@Setter
public class Constants {
    private static Constants instance;


    private Discord discord;
    private Config config;
    private Minecraft minecraft;

    private Constants() {
    }

    public static Constants getInstance() {
        if (instance == null) {
            instance = new Constants();
        }
        return instance;
    }
}