package orlov.poject.tbot.service;

import io.github.bonigarcia.wdm.WebDriverManager;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import orlov.poject.tbot.entity.VolleyballGame;
import orlov.poject.tbot.validator.ValidatorDate;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Slf4j
public class ParseBKLine implements Runnable {

    private final String lineUrlVolleyball = "https://melbet.com/ru/line/volleyball/";
    private final CopyOnWriteArrayList<VolleyballGame> volleyballGameList;

    public ParseBKLine(CopyOnWriteArrayList<VolleyballGame> volleyballGameList) {
        this.volleyballGameList = volleyballGameList;
    }

    @Override
    public void run() {
        WebDriver driver = null;

        boolean isWork = true;
        while (isWork) {
            try {

                WebDriverManager.chromedriver().setup();
                ChromeOptions chromeOptions = new ChromeOptions();
                chromeOptions.addArguments("--headless");
                chromeOptions.addArguments("window-size=1800x900");
                chromeOptions.addArguments("--start-maximized");
                chromeOptions.addArguments("--no-sandbox");
                driver = new ChromeDriver(chromeOptions);
                WebDriverWait waitDriver = new WebDriverWait(driver, 2, 50);//.ignoring(NoSuchElementException.class);
                driver.get(lineUrlVolleyball);
                ((JavascriptExecutor) driver).executeScript(
                        "var p=window.XMLHttpRequest.prototype; p.open=p.send=p.setRequestHeader=function(){};");


//                WebElement timeZoneElement = waitDriver.until(ExpectedConditions.elementToBeClickable(By.xpath("//div[@class='timeBut arr timeButTop']")));
//                timeZoneElement.click();
//                ((JavascriptExecutor) driver).executeScript(
//                        "var p=window.XMLHttpRequest.prototype; p.open=p.send=p.setRequestHeader=function(){};");
//                for (int i = 0; i < 5; i++) {
//
//                    waitDriver.until(ExpectedConditions.elementToBeClickable(By.xpath("//div[@class='jspVerticalBar']//a[@class='jspArrow jspArrowDown']"))).click();
//                    ((JavascriptExecutor) driver).executeScript(
//                            "var p=window.XMLHttpRequest.prototype; p.open=p.send=p.setRequestHeader=function(){};");
//                }
//                ((JavascriptExecutor) driver).executeScript(
//                        "var p=window.XMLHttpRequest.prototype; p.open=p.send=p.setRequestHeader=function(){};");
//                waitDriver.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[@class='jspContainer']//ul[@id='tz']//li[@data-value='2.00']"))).click();
//                ((JavascriptExecutor) driver).executeScript(
//                        "var p=window.XMLHttpRequest.prototype; p.open=p.send=p.setRequestHeader=function(){};");


                List<WebElement> futureGames = waitDriver.until(ExpectedConditions.presenceOfNestedElementsLocatedBy(By.cssSelector("div#line_bets_on_main"), By.cssSelector("div[class^='kofsTable']")));
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

                            LocalDateTime readyDateResult = null;
                            try {
                                readyDateResult = ValidatorDate.getDate(date, time);
                            } catch (ParseException parseException) {
                                parseException.printStackTrace();
                            }

                            double victoryFirst = Double.parseDouble(e.findElement(By.xpath(".//div[@class='kofs clear']//span[1]")).getText().equals("-") ? "0.0" : e.findElement(By.xpath(".//div[@class='kofs clear']//span[1]")).getText().trim());
                            double victorySecond = Double.parseDouble(e.findElement(By.xpath(".//div[@class='kofs clear']//span[3]")).getText().equals("-") ? "0.0" : e.findElement(By.xpath(".//div[@class='kofs clear']//span[3]")).getText().trim());


                            VolleyballGame volleyballGame = new VolleyballGame(linkToGame, tournamentName, firstTeamName, secondTeamName, readyDateResult, victoryFirst, victorySecond);


                            String favorite = "";
                            if (volleyballGame.getVictoryFirst() < volleyballGame.getVictorySecond()) {
                                favorite = "first";
                            } else {
                                favorite = "second";
                            }
                            volleyballGame.setWhoIsFavorite(favorite);

                            if (volleyballGame.getVictoryFirst() <= 1.16 || volleyballGame.getVictorySecond() <= 1.16) {
                                boolean contains = volleyballGameList.contains(volleyballGame);

                                if (contains) {

                                    int idxHasVG = volleyballGameList.indexOf(volleyballGame);
                                    VolleyballGame old = volleyballGameList.get(idxHasVG);

                                    if (old.getStatus().equals("wait")) {

                                        old.setStartGame(volleyballGame.getStartGame());


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
                            } else {
                                log.info("NOT CHECK game: {} | {} : {} | {} | {} | {}",
                                        volleyballGame.getTournamentName() , volleyballGame.getNameFirstTeam(), volleyballGame.getNameSecondTeam(), volleyballGame.getStartGame(), volleyballGame.getVictoryFirst(), volleyballGame.getVictorySecond());
                            }
                        }

                    } catch (Exception exception) {
                        driver.quit();
                        log.error("ERROR ------------------ in the BL line when iterate element");
                        exception.printStackTrace();
                    }
                }


                driver.quit();

                isWork = false;
                volleyballGameList.forEach(vg -> {
                    log.info("Check game: {} | {} : {} | {} | {} | {}",
                            vg.getTournamentName() , vg.getNameFirstTeam(), vg.getNameSecondTeam(), vg.getStartGame(), vg.getVictoryFirst(), vg.getVictorySecond());
                });

            } catch (Exception ex) {
                log.error("ERROR in check future game");
                driver.quit();
            }

        }

    }

}
