package orlov.poject.tbot.service;

import io.github.bonigarcia.wdm.WebDriverManager;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import orlov.poject.tbot.entity.VolleyballGame;
import orlov.poject.tbot.validator.ValidatorDate;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class StartProcess {
    private static List<VolleyballGame> volleyballGameList = new ArrayList<>();

    public static void main(String[] args) {
//        ParseService parser = new ParseService();
//        parser.startLineProcess();
//        parser.startLiveProcess();

        ManagerService managerService = new ManagerService();

        findVolleyballGameOnLivePage();

        volleyballGameList.forEach( v -> {
                log.warn("vg: {}", v);
        });

        volleyballGameList.stream().limit(1).forEach(vg -> {

//        VolleyballGame vg = new VolleyballGame();
//        vg.setStatus("wait");
//        vg.setTournamentName("Belarus. Potato Cup");
//        vg.setNameFirstTeam("Reservoir Dogs (4x4)");
//        vg.setNameSecondTeam("Wild Ducks (4x4)");
//        vg.setVictoryFirst(1.12);
//        vg.setVictorySecond(4.12);
//        vg.setWhoIsFavorite("first");
//        vg.setStartGame(LocalDateTime.of(2020,12,27,21,0));


        new Thread(new ParserLiveTest(vg, managerService)).start();
        });



    }


    private static void findVolleyballGameOnLivePage() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions chromeOptions = new ChromeOptions();
//                chromeOptions.addArguments("--headless");
//                chromeOptions.addArguments("window-size=1800x900");
        chromeOptions.addArguments("--start-maximized");
//                chromeOptions.addArguments("--no-sandbox");
        WebDriver driver = new ChromeDriver(chromeOptions);
        WebDriverWait waitDriver = new WebDriverWait(driver, 2, 50);//.ignoring(NoSuchElementException.class);
        driver.get("https://melbet.com/ru/live/volleyball/");
        ((JavascriptExecutor) driver).executeScript(
                "var p=window.XMLHttpRequest.prototype; p.open=p.send=p.setRequestHeader=function(){};");


        List<WebElement> futureGames = waitDriver.until(ExpectedConditions.presenceOfNestedElementsLocatedBy(By.cssSelector("div#live_bets_on_main"), By.cssSelector("div[class^='kofsTable']")));
        ((JavascriptExecutor) driver).executeScript(
                "var p=window.XMLHttpRequest.prototype; p.open=p.send=p.setRequestHeader=function(){};");

        String tournamentName = "";
        for (WebElement e : futureGames) {

            ((JavascriptExecutor) driver).executeScript(
                    "var p=window.XMLHttpRequest.prototype; p.open=p.send=p.setRequestHeader=function(){};");

            try {


                if (e.getAttribute("class").contains("kofsTableLigaName")) {
                    tournamentName = e.getText().trim();
                } else if (e.getAttribute("class").contains("kofsTableLineNums ")) {

                    String linkToGame = e.findElement(By.xpath(".//a[@class='nameLink fl clear']")).getAttribute("href");
                    String firstTeamName = "";
                    String secondTeamName = "";
                    List<String> teamsName = e.findElement(
                            By.xpath(".//span[@class='teams fl']"))
                            .findElements(By.xpath(".//span[@class='team']"))
                            .stream()
                            .map(WebElement::getText)
                            .collect(Collectors.toList());

                    String time = e.findElement(By.xpath(".//span[@class='dateCon fl']"))
                            .findElement(By.xpath(".//span[@class='time']")).getText().trim();

                    String date = e.findElement(By.xpath(".//span[@class='dateCon fl']"))
                            .findElement(By.xpath(".//span[@class='date']")).getText().trim();

                    if (teamsName.size() > 0) {
                        firstTeamName = teamsName.get(0).trim();
                        secondTeamName = teamsName.get(1).trim();
                    }



                    double victoryFirst = Double.parseDouble(e.findElement(By.xpath(".//div[@class='kofs clear']//span[1]")).getText().equals("-") ? "0.0" : e.findElement(By.xpath(".//div[@class='kofs clear']//span[1]")).getText().trim());
                    double victorySecond = Double.parseDouble(e.findElement(By.xpath(".//div[@class='kofs clear']//span[3]")).getText().equals("-") ? "0.0" : e.findElement(By.xpath(".//div[@class='kofs clear']//span[3]")).getText().trim());



                    VolleyballGame volleyballGame = new VolleyballGame();
                    volleyballGame.setLinkToGame(linkToGame);
                    volleyballGame.setTournamentName(tournamentName);
                    volleyballGame.setNameFirstTeam(firstTeamName);
                    volleyballGame.setNameSecondTeam(secondTeamName);
                    volleyballGame.setVictoryFirst(victoryFirst);
                    volleyballGame.setVictorySecond(victorySecond);

                    String favorite = "";
                    if (volleyballGame.getVictoryFirst() < volleyballGame.getVictorySecond()) {
                        favorite = "first";
                    } else {
                        favorite = "second";
                    }
                    volleyballGame.setWhoIsFavorite(favorite);

                    if (volleyballGame.getVictoryFirst() <= 2.16 || volleyballGame.getVictorySecond() <= 2.16) {
                        boolean contains = volleyballGameList.contains(volleyballGame);

                        if (contains) {

                            int idxHasVG = volleyballGameList.indexOf(volleyballGame);
                            VolleyballGame old = volleyballGameList.get(idxHasVG);

                            if (old.getStatus().equals("wait")) {
                                if (!old.getStartGame().equals(volleyballGame.getStartGame())) {
                                    old.setStartGame(volleyballGame.getStartGame());
                                }

                                if (!old.getVictoryFirst().equals(volleyballGame.getVictoryFirst()) || !old.getVictorySecond().equals(volleyballGame.getVictorySecond())) {

                                    if (!old.getVictoryFirst().equals(volleyballGame.getVictoryFirst())) {
                                        old.setVictoryFirst(volleyballGame.getVictoryFirst());
                                    } else if (!old.getVictorySecond().equals(volleyballGame.getVictorySecond())) {
                                        old.setVictorySecond(volleyballGame.getVictorySecond());
                                    }

                                    if (!old.getWhoIsFavorite().equals(volleyballGame.getWhoIsFavorite())) {
                                        if (volleyballGame.getVictoryFirst() < volleyballGame.getVictorySecond()) {
                                            favorite = "first";
                                        } else {
                                            favorite = "second";
                                        }
                                        old.setWhoIsFavorite(favorite);
                                    }

                                }
                            }

                        } else {
                            volleyballGame.setStatus("wait");
                            volleyballGameList.add(volleyballGame);
                        }
                    }
                }

            } catch (Exception exception) {
                driver.quit();
                log.error("ERROR ------------------ in the BL line when iterate element");
                exception.printStackTrace();
            }
        }
        driver.quit();
    }

    private static void disableAjax(WebDriver driver) {
        ((JavascriptExecutor) driver).executeScript(
                "var p=window.XMLHttpRequest.prototype; p.open=p.send=p.setRequestHeader=function(){};");
    }


}

