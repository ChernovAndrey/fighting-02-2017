package sample.game;

import org.jetbrains.annotations.NotNull;

/**
 * Created by andrey on 08.05.17.
 */
public class Players {
    private SnapClient fSnap;//first
    private SnapClient sSnap;//second
    private String fLogin;
    private String sLogin;

    Players(@NotNull SnapClient firstSnap, @NotNull SnapClient secondSnap) {
        this.fSnap = firstSnap;
        this.sSnap = secondSnap;
    }

    public SnapClient getFSnap() {
        System.out.println(fSnap);
        return fSnap;
    }


    public SnapClient getSSnap() {
        return sSnap;
    }

    Players(@NotNull String first, @NotNull String second) {
        this.fLogin = first;
        this.sLogin = second;
    }

    public boolean addSnap(SnapClient snap) {
        if (fSnap == null) {
            fSnap = snap;
            return false;
        }
        if (sSnap == null) {
            sSnap = snap;
            return true;
        }
        return false;
    }


    public String getFLogin() {
        return fLogin;
    }

    public String getSLogin() {
        return sLogin;
    }

    public boolean check() {
        return ((fSnap != null) && (sSnap != null));
    }

    public void clean() {
        this.sSnap = null;
        this.fSnap = null;
    }

}
