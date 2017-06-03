package sample.game;

import objects.UsersData;
import org.apache.log4j.Logger;
import org.hibernate.stat.internal.ConcurrentEntityStatisticsImpl;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.CloseStatus;
import sample.controllers.UserController;
import sample.websocket.SocketService;
import services.UserService;
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
public class GameMechanicsSingleThread {
    @Autowired
    private UserService userService;
    @Autowired
    private SocketService socketService;
    @Autowired
    private GameService gameService;
    @Autowired
    private Damage damage;
    ScheduledExecutorService executorScheduled = Executors.newScheduledThreadPool(1);
    static final Logger log = Logger.getLogger(UserController.class);
    private AtomicLong id = new AtomicLong(1);
    private final @NotNull ConcurrentLinkedQueue<String> waiters = new ConcurrentLinkedQueue<>();
    private final @NotNull Map<Long, Players> playingNow = new ConcurrentHashMap<>();


    public void addWaiters(String login){
        if (waiters.isEmpty()) {
            socketService.sendMessageToUser(login, Answer.messageClient("Waiting"));
            waiters.add(login);
        } else {
            if(waiters.peek().equals(login))   socketService.sendMessageToUser(login, Answer.messageClient("Waiting"));
            else startGame(login,waiters.poll());
        }
        executorScheduled.scheduleAtFixedRate(()-> socketService.sendMessageToUser(login,Answer.messageClient("pulse")), 15, 15, TimeUnit.SECONDS);
    }

    public void startGame(String first,String second) {
        final Players players = new Players(first,second);
        playingNow.put(id.get(), players);
        socketService.sendMessageToUser(first,second,Answer.messageClient(first,second,id.get()));
        id.getAndIncrement();
    }

    public void gmStep(Players players, Long id) {
      final SnapServer snapServer = new SnapServer(players,id);
        damage.SetDamage(players.getFSnap(), players.getSSnap());
        socketService.sendMessageToUser(players.getFLogin(),players.getSLogin(),Answer.getJson(snapServer));
        if (players.getFSnap().hp.equals(0) || (players.getSSnap().hp.equals(0))) {
            playingNow.remove(players.getFSnap().getId());
            endGame(players.getFSnap(),players.getSSnap());
        } else {
            players.clean();
        }
    }

    public void addSnap(SnapClient snap) {
        final Players players = playingNow.get(snap.getId());
        if (players.addSnap(snap)) {
            gmStep(players,snap.getId());
        }
    }

    public void endGame(SnapClient first,SnapClient second) {
        final ArrayList<SnapClient> snaps=new ArrayList<>();
        snaps.add(first);
        snaps.add(second);
        snaps.forEach(item -> {
            if (item.hp <= 0)
                socketService.sendMessageToUser(item.getLogin(), Answer.messageClient("Game over. You lose."));
            else
                socketService.sendMessageToUser(item.getLogin(), Answer.messageClient("Game over. Congratulation! You win."));

            socketService.cutDownConnection(item.getLogin(), CloseStatus.NORMAL);
        });
        if(first.hp>0){
            userService.updateRating(first.getLogin(),second.getLogin());
            return;
        }
        if(second.hp>0){
            userService.updateRating(first.getLogin(),second.getLogin());
            return;
        }
        if((second.hp<=0)&&(first.hp<=0)){
            userService.updateRating(first.getLogin(),second.getLogin(),"draw");
            return;
        }
    }


    //в отдельном потоке
    public void checkConnect(){
        while (true) {
            try {
                playingNow.forEach((key, value) -> {
                    if (!(socketService.isConnected(value.getFLogin()))) {
                        if (socketService.isConnected(value.getSLogin())) {
                            socketService.sendMessageToUser(value.getSLogin(), Answer.messageClient("win"));
                            userService.updateRating(value.getSLogin(),value.getFLogin());
                            socketService.cutDownConnection(value.getSLogin(), CloseStatus.NORMAL);
                            playingNow.remove(key);
                        }
                    } else {
                        if (!(socketService.isConnected(value.getSLogin()))) {
                            if (socketService.isConnected(value.getFLogin())) {
                                socketService.sendMessageToUser(value.getFLogin(), Answer.messageClient("win"));
                                socketService.cutDownConnection(value.getFLogin(), CloseStatus.NORMAL);
                                userService.updateRating(value.getFLogin(),value.getSLogin());
                                playingNow.remove(key);
                            }
                        }
                    }
                });
            }catch (Exception e) {System.out.println(e.getMessage());}
        }
    }
}


