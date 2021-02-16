package orlov.poject.tbot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import orlov.poject.tbot.service.ParseService;

import javax.annotation.PostConstruct;

@SpringBootApplication
public class TBotApplication {
    @Autowired
    private ParseService parseService;

    public static void main(String[] args) {
        SpringApplication.run(TBotApplication.class, args);
    }

    @PostConstruct
    public void initProcess() {
        Runnable r = () -> {
            parseService.startLineProcess();

        };

        Runnable r2 = () -> {
            parseService.startLiveProcess();
        };

        Runnable r3 = () -> {
            parseService.removeOverGame();
        };

        new Thread(r).start();
        new Thread(r2).start();
        new Thread(r3).start();

    }
}
