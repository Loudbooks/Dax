package me.loudbook.discordlink.minecraft;

import com.github.steveice10.mc.auth.exception.request.RequestException;
import com.github.steveice10.mc.auth.service.AuthenticationService;
import com.github.steveice10.mc.auth.service.MsaAuthenticationService;
import com.github.steveice10.mc.auth.service.SessionService;
import com.github.steveice10.mc.protocol.MinecraftConstants;
import com.github.steveice10.mc.protocol.MinecraftProtocol;
import com.github.steveice10.mc.protocol.packet.ingame.serverbound.ServerboundChatPacket;
import com.github.steveice10.packetlib.Session;
import com.github.steveice10.packetlib.tcp.TcpClientSession;
import lombok.Getter;
import lombok.Setter;

import java.net.Proxy;

public class Minecraft {
    @Getter
    private Session client;
    @Setter
    @Getter
    private String username;
    @Setter
    @Getter
    private String password;
    @Setter
    @Getter
    private String msaToken;
    public Minecraft(){
    }

    public void connect(String username, String password, String msaToken) throws RequestException {
        this.username = username;
        this.password = password;
        this.msaToken = msaToken;
        String HOST = "hypixel.net";
        int PORT = 25565;
        Proxy AUTH_PROXY = Proxy.NO_PROXY;
        AuthenticationService authService = new MsaAuthenticationService(msaToken);
        authService.setUsername(username);
        authService.setPassword(password);
        authService.setProxy(AUTH_PROXY);
        authService.login();
        MinecraftProtocol protocol;
        protocol = new MinecraftProtocol(authService.getSelectedProfile(), authService.getAccessToken());
        SessionService sessionService = new SessionService();
        sessionService.setProxy(AUTH_PROXY);
        this.client = new TcpClientSession(HOST, PORT, protocol, null);
        this.client.setFlag(MinecraftConstants.SESSION_SERVICE_KEY, sessionService);
        this.client.connect();
        //Send to limbo
        for (int i = 0; i < 16; i++) {
            this.client.send(new ServerboundChatPacket("/"));
        }
        //Add listener
        this.client.addListener(new MinecraftListener());
        System.out.println("[Dax] Connected to Minecraft!");
    }

    public enum MessageType {
        PUBLIC,
        OFFICER
    }
}
