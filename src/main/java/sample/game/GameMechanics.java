package sample.game;

import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.CloseStatus;
import sample.controllers.UserController;
import sample.websocket.SocketService;
import support.Answer;
import support.TimeOut;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by andrey on 25.04.17.
 */
@Service
public class GameMechanics {
    @Autowired
    private SocketService socketService;
    @Autowired
    private GameService gameService;
    ScheduledFuture<?> future;
    ScheduledExecutorService executorScheduled = Executors.newScheduledThreadPool(1);
    static final Logger log = Logger.getLogger(UserController.class);
    private volatile @NotNull Map<String,Boolean> didStep= new ConcurrentHashMap<>();
    Answer answer = new Answer();
    private AtomicLong id = new AtomicLong(1);
    private volatile @NotNull ConcurrentLinkedQueue<String> waiters = new ConcurrentLinkedQueue<>();
    private volatile @NotNull Map<Long, Players> playingNow = new ConcurrentHashMap<>();
    public void addWaiters(String login){
        if (waiters.isEmpty()) {
            System.out.println("Waiting");
            socketService.sendMessageToUser(login, answer.messageClient("Waiting"));
            waiters.add(login);
        } else {
            final Players players = new Players(login, waiters.poll());
            playingNow.put(id.get(), players);
            startGame(players.getLogins());
        }
        future=executorScheduled.scheduleAtFixedRate(()->{
                if (socketService.isConnected(login)) socketService.sendMessageToUser(login,answer.messageClient("pulse"));
                else future.cancel(false);
            }, 0, 15, TimeUnit.SECONDS);
    }

    public void startGame(ArrayList<String> logins) {
        logins.forEach(item -> socketService.sendMessageToUser(item, answer.messageClient(id.get(), logins)));
        setTimeout(logins.get(0),id.get());
        setTimeout(logins.get(1),id.get());
        id.getAndIncrement();
    }

    public void setTimeout(String login,Long id){
        didStep.put(login,false);
        executorScheduled.schedule(()->{
            if(didStep.get(login)==false) {
                final Players players=playingNow.get(id);
                if(players!=null) {
                    socketService.cutDownConnection(login,CloseStatus.GOING_AWAY);
                    if(players.getLogins().get(0).equals(login)){
                        socketService.sendMessageToUser(players.getLogins().get(1), answer.messageClient("you win, your opponent go away"));
                    }
                    else{
                        socketService.sendMessageToUser(players.getLogins().get(1), answer.messageClient("you win, your opponent go away"));
                        socketService.cutDownConnection(players.getLogins().get(1),CloseStatus.NORMAL);
                    }
                    didStep.remove(login);
                    playingNow.remove(id);
                }
            }
        }, 31, TimeUnit.SECONDS);
    }

    public void gmStep(Players players) {
        final SnapServer snapServer = new SnapServer(players);
        players.getLogins().forEach(item -> socketService.sendMessageToUser(item, snapServer.getJson()));
        socketService.sendMessageToUser(players.getSnaps().get(0).getLogin(), snapServer.getJson());
        socketService.sendMessageToUser(players.getSnaps().get(1).getLogin(), snapServer.getJson());
        if (players.getSnaps().get(0).hp.equals(0) || (players.getSnaps().get(1).hp.equals(0))) {
            playingNow.remove(players.getSnaps().get(0).getId());
            endGame(players.getSnaps());
        } else {
            players.cleanSnaps();
        }
    }

    public void addSnap(SnapClient snap) {
        final Players players = playingNow.get(snap.getId());
        didStep.replace(snap.getLogin(),true);
        if (players.setAndGetSize(snap) == 2) {
            gmStep(players);
        }
    }

    public void endGame(ArrayList<SnapClient> snaps) {
        snaps.forEach(item -> {
            if (item.hp <= 0)
                socketService.sendMessageToUser(item.getLogin(), answer.messageClient("Game over. You lose."));
            else
                socketService.sendMessageToUser(item.getLogin(), answer.messageClient("Game over. Congratulation! You win."));
            socketService.cutDownConnection(item.getLogin(), CloseStatus.NORMAL);
        });
    }
}


