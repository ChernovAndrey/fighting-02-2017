package sample.game;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

/**
 * Created by andrey on 08.05.17.
 */
public class Players {
    private volatile ArrayList<SnapClient> snaps = new ArrayList<>();
    private volatile ArrayList<String> logins = new ArrayList<>();

    Players(@NotNull ArrayList<String> logins) {
        this.logins = logins;
    }

    Players(@NotNull String first, @NotNull String second) {
        this.logins.add(first);
        this.logins.add(second);
    }

    public int setAndGetSize(SnapClient snap) {
        snaps.add(snap);
        return snaps.size();
    }

    public ArrayList<String> getLogins() {
        return logins;
    }


    public ArrayList<SnapClient> getSnaps() {
        return snaps;
    }

    public void cleanSnaps() {
        snaps.clear();
    }
}
