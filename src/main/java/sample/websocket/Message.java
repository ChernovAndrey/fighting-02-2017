package sample.websocket;

/**
 * Created by andrey on 27.04.17.
 */
public class Message {
    String type;
    protected String content;

    public String getType() {
        return type;
    }

    public String getContent() {
        return content;
    }

    public Message() {
    }
}
