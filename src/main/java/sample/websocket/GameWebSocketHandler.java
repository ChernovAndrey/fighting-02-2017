package sample.websocket;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import sample.game.SnapClient;
import services.UserService;
import support.Answer;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by andrey on 16.04.17.
 */


public class GameWebSocketHandler extends TextWebSocketHandler {
    private static final String SESSIONKEY = "user";
    private static final Logger log = Logger.getLogger(UserService.class);
    private @NotNull SocketService socketService;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();


    public GameWebSocketHandler(@NotNull SocketService socketService, @NotNull UserService userService) {
        this.socketService = socketService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession webSocketSession) {
        final String login = (String) webSocketSession.getAttributes().get(SESSIONKEY);
        if ((login == null)) {
            log.error("Only authenticated users allowed to play a game");
        }
        socketService.registerUser(login, webSocketSession);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage textMessage) {
        final String login = session.getAttributes().get(SESSIONKEY).toString();
        try {
            final ObjectMapper objectMapper = new ObjectMapper();
            final MessageReceive message = new MessageReceive(textMessage.getPayload());
            switch (message.getType()) {
                case "step":
                    final SnapClient snapClient = objectMapper.readValue(message.getContent(), SnapClient.class);
                    snapClient.setLogin(login);
                    socketService.transportToMechanics(snapClient);
                    break;
                case "ready":
                    final JSONObject json = new JSONObject(message.getContent());
                    final Long id = Long.parseLong(json.get("id").toString());
                    executorService.submit(() -> socketService.prepareGaming(id, login));
                    break;
                default:
                    log.error("This type is not supported");
                    socketService.sendMessageToUser(login, Answer.messageClient("This type is not supported"));
                    break;
            }
        } catch (Exception e) {
            log.error("Json error", e);
        }
    }


    @Override
    public void afterConnectionClosed(WebSocketSession webSocketSession, CloseStatus closeStatus) {

        final String login = (String) webSocketSession.getAttributes().get(SESSIONKEY);
        if (login == null) {
            log.error("null login");
            return;
        }
        socketService.removeUser(login);

    }


    @Override
    public void handleTransportError(WebSocketSession webSocketSession, Throwable throwable) {
        log.error("Transport Problem!!!!!");
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }
}
