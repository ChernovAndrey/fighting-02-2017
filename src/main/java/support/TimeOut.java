package support;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static sample.controllers.UserController.URIRequest.login;

/**
 * Created by andrey on 10.05.17.
 */
//пока что не используется!
@Service
public class TimeOut {
    private volatile Map<String, Date> lastVisit = new ConcurrentHashMap<>();

    public void setVisit(ArrayList<String> logins) {
        logins.forEach(this::setVisit);
    }

    private ExecutorService checkExecutor = Executors.newSingleThreadExecutor();

    public  void startCheck(){
        while (true){
          //  checkExecutor.submit(this::checkAndSupportConnect);
        }
    }
    public void setVisit(String login) {
        lastVisit.put(login, new Date());
    }

    public Date getVisit(String login) {
        return lastVisit.get(login);
    }

}
