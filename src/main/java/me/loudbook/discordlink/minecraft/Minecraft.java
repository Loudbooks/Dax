package me.loudbook.discordlink.minecraft;

import com.github.steveice10.mc.auth.exception.request.RequestException;
import com.github.steveice10.mc.auth.service.MsaAuthenticationService;
import com.github.steveice10.mc.auth.service.SessionService;
import com.github.steveice10.mc.auth.util.MSALApplicationOptions;
import com.github.steveice10.mc.protocol.MinecraftConstants;
import com.github.steveice10.mc.protocol.MinecraftProtocol;
import com.github.steveice10.mc.protocol.data.game.entity.player.Hand;
import com.github.steveice10.mc.protocol.data.game.entity.player.InteractAction;
import com.github.steveice10.mc.protocol.packet.ingame.serverbound.ServerboundChatPacket;
import com.github.steveice10.mc.protocol.packet.ingame.serverbound.player.ServerboundInteractPacket;
import com.github.steveice10.mc.protocol.packet.ingame.serverbound.player.ServerboundMovePlayerPosPacket;
import com.github.steveice10.packetlib.Session;
import com.github.steveice10.packetlib.tcp.TcpClientSession;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.net.Proxy;
import java.time.Instant;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.function.Consumer;

@Getter
public class Minecraft {
    private Session client;
    public String username;
    @Setter
    private String clientID;
    @Setter
    private MsaAuthenticationService auth;
    @Setter
    private Consumer<String> deviceCodeConsumer = System.out::println;

    /**
     * @param msaToken The Microsoft token created.
     * @throws RequestException Exception thrown if the account is invalid.
     */
    public void connect(String msaToken) throws RequestException, IOException, InterruptedException {
        this.clientID = msaToken;

        String HOST = "hypixel.net";
        int PORT = 25565;

        Proxy authProxy = Proxy.NO_PROXY;
        this.auth = new MsaAuthenticationService(clientID, new MSALApplicationOptions.Builder().offlineAccess(true).build());
        if (!this.auth.isLoggedIn()) {

            this.auth.setDeviceCodeConsumer((deviceCode) -> deviceCodeConsumer.accept(deviceCode.message()));

            this.auth.login();

            System.out.println("Logged in as " +
                    this.auth.getSelectedProfile().getName() + "(" +
                    this.auth.getSelectedProfile().getId() + ")");
        }

        MinecraftProtocol protocol;
        protocol = new MinecraftProtocol(auth.getSelectedProfile(), auth.getAccessToken());
        SessionService sessionService = new SessionService();
        sessionService.setProxy(authProxy);

        this.username = auth.getSelectedProfile().getName();

        this.client = new TcpClientSession(HOST, PORT, protocol, null);
        this.client.setFlag(MinecraftConstants.SESSION_SERVICE_KEY, sessionService);
        this.client.connect();

        //Add listener
        this.client.addListener(new MinecraftListener());
        System.out.println("[Dax] Connected to Minecraft!");
    }

    public enum MessageType {
        PUBLIC,
        OFFICER
    }
}
