package orlov.poject.tbot.entity;

import lombok.*;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class VolleyballGame {
    private String status;
    private String linkToGame;
    private String tournamentName;
    private String nameFirstTeam;
    private String nameSecondTeam;
    private String whoIsFavorite;
    private int countFirstTeam;
    private int countSecondTeam;
    private String gameSetNow;
    private LocalDateTime startGame;
    private Double victoryFirst;
    private Double victorySecond;
    private List<Odds> oddsFirstTeamList;
    private List<Odds> oddsSecondTeamList;
    private List<SetGame> setGameList;

    public VolleyballGame(String linkToGame, String tournamentName, String nameFirstTeam, String nameSecondTeam, LocalDateTime startGame, double victoryFirst, double victorySecond) {
        this.linkToGame = linkToGame;
        this.tournamentName = tournamentName;
        this.nameFirstTeam = nameFirstTeam;
        this.nameSecondTeam = nameSecondTeam;
        this.startGame = startGame;
        this.victoryFirst = victoryFirst;
        this.victorySecond = victorySecond;
    }

    public VolleyballGame(String tournamentName, String nameFirstTeam, String nameSecondTeam, double victoryFirst, double victorySecond) {
        this.tournamentName = tournamentName;
        this.nameFirstTeam = nameFirstTeam;
        this.nameSecondTeam = nameSecondTeam;
        this.victoryFirst = victoryFirst;
        this.victorySecond = victorySecond;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VolleyballGame that = (VolleyballGame) o;
        return Objects.equals(tournamentName, that.tournamentName) && Objects.equals(nameFirstTeam, that.nameFirstTeam) && Objects.equals(nameSecondTeam, that.nameSecondTeam);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tournamentName, nameFirstTeam, nameSecondTeam);
    }
}
