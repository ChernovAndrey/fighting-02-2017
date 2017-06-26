package objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.json.JSONObject;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.function.Function;
import java.util.function.Predicate;


/**
 * Created by andrey on 21.02.2017.
 */

public class User implements Mutual {
    private Integer id;
    private String login;
    private String newlogin;
    private String password;
    private String newpassword;

    public User() {

    }

    @JsonCreator
    public User(
            @JsonProperty("id") Integer id,
            @JsonProperty("login") String login,
            @JsonProperty("newlogin") String newlogin,
            @JsonProperty("password") String password,
            @JsonProperty("newpassword") String newpassword) {
        this.id = id;
        this.login = login;
        this.newlogin = newlogin;
        this.password = password;
        this.newpassword = newpassword;
    }


    public Integer getId() {
        return id;
    }

    public String getLogin() {
        return login;
    }


    public String getNewlogin() {
        return newlogin;
    }

    PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private Function<String, String> hash = password -> passwordEncoder.encode(password);

    public Predicate<String> comparePass = passwordDB -> passwordEncoder.matches(password, passwordDB);

    public String getHashPassword() {
        return hash.apply(password);
    }

    public String getNewHashPassword() {
        return hash.apply(newpassword);
    }

    public String getPassword() {
        return password;
    }

    public String getNewpassword() {
        return newpassword;
    }


    public void setLogin(String login) {
        this.login = login;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setNewpassword(String newpassword) {
        this.newpassword = newpassword;
    }

    public void setNewlogin(String newlogin) {
        this.newlogin = newlogin;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public JSONObject getJson() {
        final JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", id);
        jsonObject.put("login", login);
        return jsonObject;

    }
}