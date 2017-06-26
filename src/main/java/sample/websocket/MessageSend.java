package sample.websocket;

import com.fasterxml.jackson.annotation.JsonAnyGetter;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by andrey on 21.05.17.
 */
public class MessageSend extends Message {
    private Map<String, Object> properties = new HashMap<>();

    public MessageSend(String type, String content) {
        this.type = type;
    }

    @JsonAnyGetter
    public Map<String, Object> properties() {
        return properties;
    }
}
