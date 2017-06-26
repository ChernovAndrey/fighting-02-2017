package sample.game;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by andrey on 24.04.17.
 */
public class SnapClient {
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Long id;
    String method;
    String block;
    String target;
    Integer hp;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    String login;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    Integer takenDamage;

    @JsonCreator
    public SnapClient(@JsonProperty("id") Long id,
                      @JsonProperty("method") String method,
                      @JsonProperty("block") String block,
                      @JsonProperty("target") String target,
                      @JsonProperty("hp") Integer hp) {
        this.id = id;
        this.method = method;
        this.block = block;
        this.target = target;
        this.hp = hp;
    }

    public void setTakenDamage(Integer takenDamage) {
        this.takenDamage = takenDamage;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getLogin() {
        return login;
    }

    public Long getId() {
        return id;
    }

    public String getMethod() {
        return method;
    }

    public String getBlock() {
        return block;
    }

    public String getTarget() {
        return target;
    }

    public Integer getHp() {
        return hp;
    }

    public Integer getTakenDamage() {
        return takenDamage;
    }
}
