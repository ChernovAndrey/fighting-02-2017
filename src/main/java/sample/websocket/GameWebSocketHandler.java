package sample.websocket;


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

import javax.naming.AuthenticationException;
import java.io.IOException;
/**
 * Created by andrey on 16.04.17.
 */


public class GameWebSocketHandler extends TextWebSocketHandler {
    private static final String SESSIONKEY = "user";
    private static final Logger log = Logger.getLogger(UserService.class);


    //private @NotNull UserService userService;

    private @NotNull SocketService socketService;

    @ExceptionHandler(Exception.class)
    public void handleException(WebSocketSession webSocketSession, Exception e)  {
        log.error(e.getMessage());
        afterConnectionClosed(webSocketSession,CloseStatus.SERVER_ERROR);
    }
    public  GameWebSocketHandler(@NotNull SocketService socketService,@NotNull  UserService userService){
        this.socketService=socketService;
    //    this.userService=userService;
    }
    @Override
    public void afterConnectionEstablished(WebSocketSession webSocketSession) throws AuthenticationException, IOException {
        System.out.println("Connect");

        final String login = (String) webSocketSession.getAttributes().get(SESSIONKEY);
        if ((login == null)) {
            System.out.println("Only authenticated users allowed to play a game");
            throw new AuthenticationException("Only authenticated users allowed to play a game");
        }
        socketService.registerUser(login, webSocketSession);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage textMessage)  {
        final String login  = session.getAttributes().get(SESSIONKEY).toString();
        System.out.println("textmessage");
        try {
            final Message message=new Message(textMessage.getPayload());
            switch (message.getType().toLowerCase()){
                case "step":{
                    final SnapClient snapClient = new SnapClient(message.getContent());
                    snapClient.setLogin(login);
                    socketService.transportToMechanics(snapClient);
                }
                case "pulse":{
                    //later
                  //  content=Boolean.parseBoolean(json.get("flag").toString());
                }
                default:{
                    log.error("t");
                }
            }
        }
        catch (Exception e) {
            log.error("Json error");
        }

    }


    @Override
    public void afterConnectionClosed(WebSocketSession webSocketSession, CloseStatus closeStatus)  {

        final String login = (String) webSocketSession.getAttributes().get(SESSIONKEY);
        if (login == null) {
            System.out.println("null login");
            log.error("null login");
            return;
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
