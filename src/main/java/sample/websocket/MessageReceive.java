package sample.websocket;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.json.JSONObject;

/**
 * Created by andrey on 21.05.17.
 */
public class MessageReceive extends Message {
    @JsonCreator
    public MessageReceive(String message) {
        final JSONObject json = new JSONObject(message);
        type = json.get("type").toString();
        json.remove("type");
        content = json.toString();
    }
}
