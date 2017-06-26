package sample.game;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import support.GameData;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by andrey on 09.05.17.
 */
@Service
public class Damage {
    private static final Logger log = Logger.getLogger(Damage.class);
    public Integer baseDamage;
    public Double MethodHead;
    public Double MethodArm;
    public Double MethodLeg;
    public Double BlockHead;
    public Double BlockBody;
    private static Damage instance;

    public void resourseUp() {
        final InputStream inJson = GameData.class.getResourceAsStream("/InitialData.json");
        try {
            instance = new ObjectMapper().readValue(inJson, Damage.class);

        } catch (IOException e) {
            log.error("wrong json in resource file", e);
        }
    }

    public Double setKMethod(String method) {
        System.out.println(instance.MethodArm);
        Double kProb = 1.0;
        if (method.equals("arm")) kProb *= instance.MethodArm;
        if (method.equals("head")) kProb *= instance.MethodHead;
        if (method.equals("leg")) kProb *= instance.MethodLeg;
        return kProb;
    }

    public Double setKBlock(String target, String block, Double kProb) {
        if (target.equals(block)) {
            if (block.equals("body")) kProb *= instance.BlockBody;
            else kProb *= instance.BlockHead;
        } else kProb = kProb / 2.0;
        return kProb;
    }

    public @NotNull Integer setAndGetDamage(Double kProb) {
        return (int) Math.round((1.0 - kProb) * instance.baseDamage);
    }

    public void SetDamage(SnapClient first, SnapClient second) {
        calculate(first, second.block);
        calculate(second, first.block);
    }

    private void calculate(SnapClient snap, String block) {
        if (snap.method.equals("null")) {
            snap.setTakenDamage(0);
            return;
        }
        final Double kProb = setKBlock(snap.target, block, setKMethod(snap.method));
        if (Math.random() < kProb) {
            snap.setTakenDamage(0);
            return;
        }
        final Integer damage = setAndGetDamage(kProb);
        snap.hp = Math.max(snap.hp - damage, 0);
        snap.setTakenDamage(damage);
    }

}
