package sample.game;

/**
 * Created by andrey on 25.04.17.
 */
public class SnapServer {
    public SnapClient first;

    public SnapClient second;

    public Long id;


    public Long getId() {
        return id;
    }

    public SnapClient getFirst() {
        return first;
    }

    public SnapClient getSecond() {
        return second;
    }

    SnapServer(Players players, Long id) {
        this.id = id;
        first = players.getFSnap();
        second = players.getSSnap();
    }
}
