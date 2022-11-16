package org.flan.discord;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;

import javax.security.auth.login.LoginException;

public class Main {
    private static JDA jda = null;
    private static final String BOT_TOKEN = "MTA0MTE5ODkyMzM0MTEwMzEzNA.GVl2DT.HZrL9Gb3_G1848kbmuNWwGLapEpKLqV_7oYybE";


    public static void main(String[] args) {
        try {
            jda = JDABuilder.createDefault(BOT_TOKEN, GatewayIntent.GUILD_MESSAGES)
                    .setRawEventsEnabled(true)
                    .addEventListeners(new DiscordMessageListener()) //追加部分
                    .setActivity(Activity.playing("設定したいステータスを入力してください"))
                    .build();
        } catch (LoginException e) {
            e.printStackTrace();
        }
    }

}