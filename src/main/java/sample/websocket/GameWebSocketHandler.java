package sample.websocket;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import sample.game.SnapClient;
import services.UserService;
import support.Answer;

/**
 * Created by andrey on 16.04.17.
 */


public class GameWebSocketHandler extends TextWebSocketHandler {
    private static final String SESSIONKEY = "user";
    private static final Logger log = Logger.getLogger(UserService.class);


    private Answer answer = new Answer();

    private @NotNull SocketService socketService;

    @ExceptionHandler(Exception.class)
    public void handleException(WebSocketSession webSocketSession, Exception e) {
        log.error(e.getMessage());
        afterConnectionClosed(webSocketSession, CloseStatus.SERVER_ERROR);
    }

    public GameWebSocketHandler(@NotNull SocketService socketService, @NotNull UserService userService) {
        this.socketService = socketService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession webSocketSession) {
        System.out.println("Connect");
        final String login = (String) webSocketSession.getAttributes().get(SESSIONKEY);
        if ((login == null)) {
            System.out.println("Only authenticated users allowed to play a game");
            log.error("Only authenticated users allowed to play a game");
            socketService.sendMessageToUser(login, answer.messageClient("Only authenticated users allowed to play a game"));
        }
        socketService.registerUser(login, webSocketSession);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage textMessage) {
        final String login = session.getAttributes().get(SESSIONKEY).toString();
        System.out.println("textmessage");
        try {
            final ObjectMapper objectMapper = new ObjectMapper();
            final MessageReceive message =new MessageReceive(textMessage.getPayload());
            switch (message.getType()) {
                case "step":
                    final SnapClient snapClient = objectMapper.readValue(message.getContent(), SnapClient.class);
                    snapClient.setLogin(login);
                    socketService.transportToMechanics(snapClient);
                    break;
                case "startStep":
                        final JSONObject json=new JSONObject();
                        final Long id= (Long) json.get("id");
                        if(id==null) throw new Exception();
                        socketService.transportTotimeOut(login,id);
                        break;
                default:
                    System.out.println("This type is not supported");
                    log.error("This type is not supported");
                    socketService.sendMessageToUser(login, answer.messageClient("This type is not supported"));
                    break;
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            log.error("Json error");
        }
    }


    @Override
    public void afterConnectionClosed(WebSocketSession webSocketSession, CloseStatus closeStatus) {
        System.out.println("closeConnect");
        final String login = (String) webSocketSession.getAttributes().get(SESSIONKEY);
        if (login == null) {
            System.out.println("null login");
            log.error("null login");
        }
        socketService.removeUser(login);
    }


    @Override
    public void handleTransportError(WebSocketSession webSocketSession, Throwable throwable) {
        System.out.println("Transport Problem!!!!!");
        log.error("Transport Problem!!!!!");
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }
}
