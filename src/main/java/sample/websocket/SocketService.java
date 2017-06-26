package sample.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.log4j.Logger;
import org.eclipse.jetty.websocket.api.WebSocketException;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import sample.controllers.UserController;
import sample.game.GameService;
import sample.game.SnapClient;
import support.Answer;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by andrey on 16.04.17.
 */
@Service
public class SocketService {
    static final Logger log = Logger.getLogger(UserController.class);
    private Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private Map<Long, String> ready = new ConcurrentHashMap<>();

    @Autowired
    private GameService gameService;

    public void registerUser(@NotNull String login, @NotNull WebSocketSession webSocketSession) {
        sessions.put(login, webSocketSession);
        sendMessageToUser(login, Answer.messageClient("Connect"));
        gameService.addWaiters(login);
    }

    public void transportToMechanics(SnapClient snapClient) {
        if (isConnected(snapClient.getLogin())) gameService.addSnap(snapClient);
    }

    public boolean isConnected(@NotNull String login) {
        return sessions.containsKey(login) && sessions.get(login).isOpen();
    }

    public void removeUser(@NotNull String login) {
        sessions.remove(login);
    }

    public void cutDownConnection(@NotNull String login, @NotNull CloseStatus closeStatus) {
        final WebSocketSession webSocketSession = sessions.get(login);
        if (webSocketSession != null && webSocketSession.isOpen()) {
            try {
                sendMessageToUser(login, Answer.messageClient("Bye"));
                webSocketSession.close(closeStatus);
                sessions.remove(login);
            } catch (IOException e) {
                log.error("session did not close", e);
            }
        }
    }

    public void sendMessageToUser(@NotNull String first, @NotNull String second, @NotNull JSONObject json) {
        sendMessageToUser(first, json);
        sendMessageToUser(second, json);

    }

    public void sendMessageToUser(@NotNull String login, @NotNull JSONObject json) {
        final WebSocketSession webSocketSession = sessions.get(login);
        if (webSocketSession == null) {

            log.error("no game websocket for user " + login);
            cutDownConnection(login, CloseStatus.SESSION_NOT_RELIABLE);
        }
        if (!webSocketSession.isOpen()) {
            log.error("session is closed or not exsists");
            cutDownConnection(login, CloseStatus.SESSION_NOT_RELIABLE);
        }
        try {
            webSocketSession.sendMessage(new TextMessage(json.toString()));
        } catch (JsonProcessingException | WebSocketException e) {
            log.error("Unnable to send message", e);
            cutDownConnection(login, CloseStatus.SESSION_NOT_RELIABLE);

        } catch (Exception ignore) {
        }
    }

    public void prepareGaming(Long id, String login) {
        final String loginMap = ready.get(id);
        if (loginMap == null) {
            ready.put(id, login);
            return;
        }
        sendMessageToUser(loginMap, Answer.messageClient("go"));
        sendMessageToUser(login, Answer.messageClient("go"));
        ready.remove(id);
    }
}
