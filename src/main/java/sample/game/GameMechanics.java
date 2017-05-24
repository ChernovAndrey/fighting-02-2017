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
    private volatile ScheduledFuture<?> future;
    private ExecutorService executorMessage=Executors.newFixedThreadPool(2);
    private ExecutorService executorSnapClient=Executors.newFixedThreadPool(2);
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
            executorMessage.submit(()-> socketService.sendMessageToUser(login, answer.messageClient("Waiting")));
            waiters.add(login);
        } else {
            final Players players = new Players(login, waiters.poll());
            playingNow.put(id.get(), players);
           executorMessage.submit(()->startGame(players.getLogins()));
        }
        future=executorScheduled.scheduleAtFixedRate(()->{
                if (socketService.isConnected(login)) executorMessage.submit(()->socketService.sendMessageToUser(login,answer.messageClient("pulse")));
                else future.cancel(true);//Так можно делать?
            }, 15, 15, TimeUnit.SECONDS);
    }

    public void startGame(ArrayList<String> logins) {
        logins.forEach(item ->socketService.sendMessageToUser(item, answer.messageClient(id.get(), logins)));
       // setTimeout(logins.get(0),id.get());
       // setTimeout(logins.get(1),id.get());
        id.getAndIncrement();
    }

    public void setTimeout(String login,Long id){
     //   didStep.put(login,false);
      /*  executorScheduled.schedule(()->{
            if(didStep.get(login)==false) {
                socketService.sendMessageToUser(login,answer.messageClient("Timeout"));
                final Players players=playingNow.get(id);
                if(players!=null) {
                    socketService.cutDownConnection(login,CloseStatus.GOING_AWAY);
                    if(players.getLogins().get(0).equals(login)){
                        if(didStep.get(players.getLogins().get(1))==true)
                            socketService.sendMessageToUser(players.getLogins().get(1), answer.messageClient("you win, your opponent go away"));
                    }
                    else{
                        if(didStep.get(players.getLogins().get(0))==true)
                            socketService.sendMessageToUser(players.getLogins().get(1), answer.messageClient("you win, your opponent go away"));
                    }
                    socketService.cutDownConnection(players.getLogins().get(1),CloseStatus.NORMAL);
                    didStep.remove(login);
                    playingNow.remove(id);
                }
            }
        }, 31, TimeUnit.SECONDS);*/
    }

    public void gmStep(Players players) {
       // System.out.println(players.getSnaps().get(0).getLogin());
       /*final CompletableFuture<SnapServer> snapServer= CompletableFuture.supplyAsync(()->{
           System.out.println(players.getSnaps().get(0).getLogin());
           final SnapServer snap=new SnapServer(players.getSnaps());
           System.out.println(snap.getJson().toString());
           players.getLogins().forEach(item -> socketService.sendMessageToUser(item, snap.getJson()));
           return snap;
       },executorSnapClient);*/
        final SnapServer snap=new SnapServer(players.getSnaps());
        players.getLogins().forEach(item -> socketService.sendMessageToUser(item, snap.getJson()));
        if (players.getSnaps().get(0).hp.equals(0) || (players.getSnaps().get(1).hp.equals(0))) {
            playingNow.remove(players.getSnaps().get(0).getId());
            executorMessage.submit(()->endGame(players.getSnaps()));
        } else {
            players.cleanSnaps();
        }
    }

    public void addSnap(SnapClient snap) {
        final Players players = playingNow.get(snap.getId());
     //   didStep.replace(snap.getLogin(),true);
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
    public String getLoginOpponent(String login){
        //плохое решение, но пока не придумал ничего лучше
        for (Players value : playingNow.values()) {
            if(value.getLogins().get(0).equals(login)) return value.getLogins().get(1);
            if(value.getLogins().get(0).equals(login)) return value.getLogins().get(0);
        }
        return null;
    }
}


