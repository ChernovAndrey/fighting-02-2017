package support;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import objects.Mutual;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import sample.game.SnapServer;

/**
 * Created by andrey on 06.04.17.
 */
public final class Answer {
    private Answer() {
    }

    private static final Logger log = Logger.getLogger(SnapServer.class);

    public static String withObject(String status, Mutual user) {
        final JSONObject answer = new JSONObject();
        answer.put("status", status);
        answer.put("user", user.getJson());
        return answer.toString();
    }

    public static String onlyStatus(String status) {
        final JSONObject answer = new JSONObject();
        answer.put("status", status);
        return answer.toString();
    }

    public static String forLeaders(String status, JSONArray leaders) {
        final JSONObject answer = new JSONObject();
        answer.put("leaders", leaders);
        answer.put("status", status);
        return answer.toString();
    }

    public static JSONObject messageClient(String answer) {
        final JSONObject result = new JSONObject();
        result.put("message", answer);
        return result;
    }


    public static JSONObject messageClient(String first, String second, Long id) {
        final JSONObject result = new JSONObject();
        result.put("key", id);
        result.put("first", first);
        result.put("second", second);
        return result;
    }

    public static JSONObject getJson(SnapServer snapServer) {
        final ObjectMapper objectMapper = new ObjectMapper();
        try {
            return new JSONObject(objectMapper.writeValueAsString(snapServer));
        } catch (JsonProcessingException e) {
            log.error("Json error", e);
        }
        return new JSONObject();
    }

}
