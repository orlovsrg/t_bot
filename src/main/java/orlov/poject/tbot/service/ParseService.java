package orlov.poject.tbot.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import orlov.poject.tbot.entity.VolleyballGame;

import java.time.LocalDateTime;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


@Slf4j
@Service
public class ParseService {
    @Autowired
    private ManagerService managerService;

    private CopyOnWriteArrayList<VolleyballGame> volleyballGameList = new CopyOnWriteArrayList<>();

    public void startLineProcess() {
//        log.info("Start startLineProcess()");
        ParseBKLine parseBKLine = new ParseBKLine(volleyballGameList);
        ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
        service.scheduleWithFixedDelay(parseBKLine, 0, 1, TimeUnit.HOURS);

    }

    public void startLiveProcess() {
        while (true) {
//            log.info("Check game in startLiveProcess() with size: {}", volleyballGameList.size());
            volleyballGameList.forEach(vg -> {
                if (vg.getStartGame().isBefore(LocalDateTime.now()) && vg.getStatus().equals("wait")) {
                    vg.setStatus("inWork");
                    new Thread(new ParseBKLive( vg, managerService)).start();
                }
            });
            try {
                Thread.sleep(1000 * 15);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    public void removeOverGame() {
        while (true) {
//            log.info("Check game in removeOverGame()");
            try {
                Thread.sleep(1000 * 60 * 60 * 24);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            volleyballGameList.removeIf(vg -> vg.getStatus().equals("over"));
//            log.info("End check game in removeOverGame()");
        }

    }


}
