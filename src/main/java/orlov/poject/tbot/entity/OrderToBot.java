package orlov.poject.tbot.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderToBot {
    private String nameAlgorithm;
    private String tournament;
    private String teamsName;
    private String coefficientsBeforeStart;
    private String coefficientsNow;
    private String setAndCountSet;
    private String foraCoef;
}
