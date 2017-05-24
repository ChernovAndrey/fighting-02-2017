package sample.game;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by andrey on 25.04.17.
 */
public class SnapServer {
    private JSONObject result;
    private String first;
    private String second;
    private ArrayList<SnapClient> snapsClient=new ArrayList<>();
    public String getFirst() {
        return first;
    }

    public String getSecond() {
        return second;
    }


    SnapServer(ArrayList<SnapClient> snaps) {
        snapsClient=Damage.getInstance().getSnapsWithDamage(snaps);
    }
    public JSONObject getJson(){
        final ObjectMapper objectMapper=new ObjectMapper();
        final JSONObject resultJson = new JSONObject();
        try {
            resultJson.put("first",new JSONObject(objectMapper.writeValueAsString(snapsClient.get(0))));
            resultJson.put("second",new JSONObject(objectMapper.writeValueAsString(snapsClient.get(1))));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            System.out.println("json error snapclient");
        }
        return resultJson;
    }
}
