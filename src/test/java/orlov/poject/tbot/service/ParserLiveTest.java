package orlov.poject.tbot.service;

import io.github.bonigarcia.wdm.WebDriverManager;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import orlov.poject.tbot.entity.Odds;
import orlov.poject.tbot.entity.OrderToBot;
import orlov.poject.tbot.entity.SetGame;
import orlov.poject.tbot.entity.VolleyballGame;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


@Slf4j
public class ParserLiveTest implements Runnable {
    private final String mainLiveUrl = "https://melbet.com/ru/live/volleyball/";
    private final String xpathByElementWithMainInfo = ".//li[starts-with(@class,'fl i1')]";
    private final VolleyballGame volleyballGame;
    private final ManagerService managerService;
    private boolean isWork;
    private boolean isContainsGameInLive;
    private String coefficientUrl;
    boolean isStartGame;
    private WebDriver driver;
    private WebDriverWait driverWait;
    private int setNow;
    private boolean isEndGame;

    //todo
    private String kayByFirstAlgorithm = "";
    private String kayBySecondAlgorithm = "";
    private double victoryFirstTeamBeforeStartGame;
    private double victorySecondTeamBeforeStartGame;
    private boolean checkFirstAlgorithm;
    private boolean checkSecondAlgorithm;
    private boolean checkSecondAlgorithmTwo;
    private int differentCount;
    private boolean checkChangeSet;
    private int setBySets;
    private Odds oddsBySignal;

    public ParserLiveTest(VolleyballGame volleyballGame, ManagerService managerService) {
        this.volleyballGame = volleyballGame;
        this.managerService = managerService;
    }


    @Override
    public void run() {
        log.info("START GAME :{}", volleyballGame);
        try {

            victoryFirstTeamBeforeStartGame = volleyballGame.getVictoryFirst();
            victorySecondTeamBeforeStartGame = volleyballGame.getVictorySecond();

            while (!isEndGame) {
                try {
                    driver = getDriver();
                    driver.get(mainLiveUrl);
                    disableAjax(driver);
                    isStartGame = waitStartGame();
                    if (isStartGame) {
                        volleyballGame.setStatus("start");
                    } else {
                        log.warn("GAME NOT FOUND FOR 5 MINUTES");
                        driver.manage().deleteAllCookies();
                        driver.quit();
                        isEndGame = true;
                        return;
                    }


                    while (!isEndGame && isStartGame) {
                        boolean continueGame = continueGame();

                        if (continueGame) {

                            driver.get(volleyballGame.getLinkToGame());
                            disableAjax(driver);
                            clickByCountElement();
                            disableAjax(driver);


                            setNow = getSetNow();
                            if (setNow == -1)
                                continue;

                            coefficientUrl = getCoefficientUrlBySet();
                            if (Objects.isNull(coefficientUrl))
                                continue;

                            driver.get(coefficientUrl);
                            disableAjax(driver);
                            clickByCountElement();
                            disableAjax(driver);
                            getGamesInfo();

                        } else {
                            log.warn("GAME NOT FOUND FOR 5 MINUTES");
                            driver.manage().deleteAllCookies();
                            driver.quit();
                            isEndGame = true;
                            return;
                        }

                    }


                } catch (Exception mainExc) {
                    driver.quit();
                    log.error("Error in mainExc: {}", mainExc.getMessage());
                    mainExc.printStackTrace();
                }
            }
        } catch (Exception ex) {
            log.error("Error in run(): {}", ex.getMessage());
        } finally {
            driver.quit();
        }

    }

    public boolean continueGame() {
        long startFind = System.currentTimeMillis();
        boolean result = false;
        while (!result && System.currentTimeMillis() - startFind < 1000 * 60 * 10) {
            try {
                result = findVolleyballGameOnLivePage();
            } catch (Exception ex) {
                log.error("Error in check start game");
            }
            if (!result) {
                driver.get(mainLiveUrl);
                disableAjax(driver);
            }
            log.info("CONTINUE GAME: {}", result);
        }
        log.info("result waitStartGame: {}", result && System.currentTimeMillis() - startFind < 1000 * 60 * 10);
        return result && System.currentTimeMillis() - startFind < 1000 * 60 * 10;
    }

    public boolean waitStartGame() {
        long startFind = System.currentTimeMillis();
        boolean result = false;
        while (!result && System.currentTimeMillis() - startFind < 1000 * 60 * 60) {
            try {
                result = findVolleyballGameOnLivePage();
            } catch (Exception ex) {
                log.error("Error in check start game");
            }
            if (!result) {
                driver.get(mainLiveUrl);
                disableAjax(driver);
            }
            log.info("WAIT GAME: {}", result);
        }
        log.info("result waitStartGame: {}", result && System.currentTimeMillis() - startFind < 1000 * 60 * 60);
        return result && System.currentTimeMillis() - startFind < 1000 * 60 * 60;
    }

    private void getGamesInfo() {
        try {
            WebElement teamInfoLive = driverWait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(".//div[@class='withDKWZV']")));
            // get count of teams
            List<WebElement> elementsCountOfTeams = driverWait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(".//div[@class='teams clear']"))).findElement(By.xpath(".//div[@class='countCon']")).findElements(By.xpath(".//div[@class='teamScore']"));

            int countFirstTeam = -1;
            int countSecondTeam = -1;
            if (elementsCountOfTeams.size() == 2) {
                try {

                    countFirstTeam = Integer.parseInt(elementsCountOfTeams.get(0).getText().trim());
                    countSecondTeam = Integer.parseInt(elementsCountOfTeams.get(1).getText().trim());
                } catch (Exception e) {
                    log.error("Error in getGamesInfo() with parse Count Teams");
                }
            }
            log.info("countFirstTeam: {}", countFirstTeam);
            log.info("countSecondTeam: {}", countSecondTeam);

            String gameSetNow = teamInfoLive.findElement(By.xpath(".//div[@class='date']")).getText();
            setNow = Integer.parseInt(gameSetNow.replaceAll("\\D", ""));
            log.info("setNow: {}", setNow);
            WebElement tableElement = teamInfoLive.findElement(By.xpath(".//div[@class='con']")).findElement(By.cssSelector("table"));
            List<String> countSet = tableElement.findElement(By.xpath(".//tr[@class='game-th']"))
                    .findElements(By.xpath(".//td[@class='prop']"))
                    .stream()
                    .map(e -> e.getText().trim())
                    .collect(Collectors.toList());
            List<WebElement> countInSet = tableElement.findElements(By.xpath(".//td[@class='num']"));

            List<SetGame> setGameList = new ArrayList<>();
            for (int i = 0; i < countSet.size(); i++) {
                SetGame setGame = new SetGame();
                String cSet = countSet.get(i);
                int pointInSetFirstTeam = Integer.parseInt(countInSet.get(i).getText().trim());
                int pointInSetSecondTeam = Integer.parseInt(countInSet.get(i + countSet.size()).getText().trim());
                setGame.setCountSet(Integer.parseInt(cSet.replaceAll("\\D", "")));
                setGame.setPointFirstTeam(pointInSetFirstTeam);
                setGame.setPointSecondTeam(pointInSetSecondTeam);
                setGameList.add(setGame);
            }


            boolean isBlockBet = checkBlockBet();
            if (!isBlockBet) {
                WebElement cofsBlockElement = driverWait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[@class='blockContent kofsContent']")));
                List<WebElement> elementsOfCoefByTeams = null;
                if (cofsBlockElement.findElements(By.xpath(".//div[@id='group_1']")).size() > 0) {
                    elementsOfCoefByTeams = cofsBlockElement.findElement(By.xpath(".//div[@id='group_1']")).findElement(By.xpath(".//div[@class='bets table']")).findElements(By.xpath(".//span[@class='kof ']"));
                }

                double newCoefTeamFirst = 0.0;
                double newCoefTeamSecond = 0.0;

                if (elementsOfCoefByTeams != null) {
                    String isBlockFirst = cofsBlockElement.findElement(By.xpath(".//div[@id='group_1']//span[starts-with(@class, 'betBut')]")).getAttribute("class");
                    if (!isBlockFirst.equals("betBut lock")) {
                        newCoefTeamFirst = Double.parseDouble(elementsOfCoefByTeams.get(0).getText().trim());
                        newCoefTeamSecond = Double.parseDouble(elementsOfCoefByTeams.get(1).getText().trim());
                    }

                } else {
                    log.warn("NOT HAS coef on teams");
                    newCoefTeamFirst = volleyballGame.getVictoryFirst();
                    newCoefTeamSecond = volleyballGame.getVictorySecond();
                }

                List<WebElement> elementsOfCoefInSetByTeams = null;
                cofsBlockElement = driverWait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[@class='blockContent kofsContent']")));
                if (cofsBlockElement.findElements(By.xpath(".//div[@id='group_2']")).size() > 0) {
                    elementsOfCoefInSetByTeams = cofsBlockElement.findElement(By.xpath(".//div[@id='group_2']")).findElement(By.xpath(".//div[@class='bets table']")).findElements(By.xpath(".//div[@class='cell']"));
                }

                List<Odds> oddsFirstTeam = new ArrayList<>();
                List<Odds> oddsSecondTeam = new ArrayList<>();
                if (elementsOfCoefInSetByTeams != null) {
                    for (int i = 0; i < elementsOfCoefInSetByTeams.size(); i++) {
                        if (i % 2 == 0) {

                            String isBlock = elementsOfCoefInSetByTeams.get(i).findElement(By.xpath(".//span[starts-with(@class, 'betBut')]")).getAttribute("class");
                            if (!isBlock.equals("betBut lock")) {
                                String testPoint = elementsOfCoefInSetByTeams.get(i).findElement(By.xpath(".//span[@class='name']")).getText().trim();
                                double testCoef = Double.parseDouble(elementsOfCoefInSetByTeams.get(i).findElement(By.xpath(".//span[@class='kof ']")).getText().trim());
                                double point = Double.parseDouble(testPoint.substring(testPoint.indexOf("(") + 1, testPoint.indexOf(")")));
                                Odds odds = new Odds();
                                odds.setPoint(point);
                                odds.setCoefficient(testCoef);
                                oddsFirstTeam.add(odds);
//                                        log.info("FIRST TEAM: {} | {}", testPoint, testCoef);
                            }
                        } else {
                            String isBlock = elementsOfCoefInSetByTeams.get(i).findElement(By.xpath(".//span[starts-with(@class, 'betBut')]")).getAttribute("class");

                            if (!isBlock.equals("betBut lock")) {
                                String testPoint = elementsOfCoefInSetByTeams.get(i).findElement(By.xpath(".//span[@class='name']")).getText().trim();
                                double testCoef = Double.parseDouble(elementsOfCoefInSetByTeams.get(i).findElement(By.xpath(".//span[@class='kof ']")).getText().trim());
                                double point = Double.parseDouble(testPoint.substring(testPoint.indexOf("(") + 1, testPoint.indexOf(")")));
                                Odds odds = new Odds();
                                odds.setPoint(point);
                                odds.setCoefficient(testCoef);
                                oddsSecondTeam.add(odds);
                                log.info("SECOND TEAM: {} | {}", testPoint, testCoef);
                            }
                        }
                    }
                } else {
                    log.warn("NOT HAS coef in SET on teams");
                }


                volleyballGame.setSetGameList(setGameList);
                volleyballGame.setCountFirstTeam(countFirstTeam);
                volleyballGame.setCountSecondTeam(countSecondTeam);
                volleyballGame.setGameSetNow(gameSetNow);
                volleyballGame.setVictoryFirst(newCoefTeamFirst);
                volleyballGame.setVictorySecond(newCoefTeamSecond);
                volleyballGame.setOddsFirstTeamList(oddsFirstTeam);
                volleyballGame.setOddsSecondTeamList(oddsSecondTeam);


                // check by signal
                String gameSetAtMoment = volleyballGame.getGameSetNow();
                SetGame setGameAtMoment = volleyballGame.getSetGameList().get(volleyballGame.getSetGameList().size() - 1);

                OrderToBot orderToBot = new OrderToBot();
                orderToBot.setTournament(volleyballGame.getTournamentName());
                orderToBot.setCoefficientsBeforeStart(victoryFirstTeamBeforeStartGame + " : " + victorySecondTeamBeforeStartGame);
                orderToBot.setCoefficientsNow(volleyballGame.getVictoryFirst() + " : " + volleyballGame.getVictorySecond());
                orderToBot.setTeamsName(volleyballGame.getNameFirstTeam() + " : " + volleyballGame.getNameSecondTeam());
                orderToBot.setSetAndCountSet(gameSetAtMoment + " = " + setGameAtMoment.getPointFirstTeam() + " : " + setGameAtMoment.getPointSecondTeam());


                if (volleyballGame.getWhoIsFavorite().equals("first")) {
                    int differentCountNow = volleyballGame.getCountFirstTeam() - volleyballGame.getCountSecondTeam();
                    if (differentCountNow < differentCount) {
                        differentCount = differentCountNow;
                        checkSecondAlgorithm = true;
                    } else if (differentCountNow > differentCount) {
                        differentCount = differentCountNow;
                        checkSecondAlgorithm = false;
                    }
                } else {
                    int differentCountNow = volleyballGame.getCountSecondTeam() - volleyballGame.getCountFirstTeam();
                    if (differentCountNow < differentCount) {
                        differentCount = differentCountNow;
                        checkSecondAlgorithm = true;
                    } else if (differentCountNow > differentCount) {
                        differentCount = differentCountNow;
                        checkSecondAlgorithm = false;
                    }
                }


                checkSignalByFirstAlgorithm(volleyballGame, orderToBot);

                if (checkSecondAlgorithm)
                    checkSignalBySecondAlgorithm(volleyballGame, orderToBot);

                log.warn("volleyballGame is: {}", volleyballGame);


            }

        } catch (Exception e) {
            log.error("Error in getGamesInfo():{} ", e.getMessage());
        }
    }

    private void checkSignalByFirstAlgorithm(VolleyballGame volleyballGame, OrderToBot orderToBot) {
        checkFirstAlgorithm = !kayByFirstAlgorithm.equals(String.valueOf(setNow));
        log.info("checkFirstAlgorithm = {} , !kayByFirstAlgorithm.equals(String.valueOf(setNow)): {}, setNow: {}", checkFirstAlgorithm, !kayByFirstAlgorithm.equals(String.valueOf(setNow)), setNow);
        if (volleyballGame.getWhoIsFavorite().equals("first")) {
            volleyballGame.getOddsFirstTeamList().forEach(odds -> {
                boolean point = odds.getPoint().equals(-4.5);
                boolean coef = odds.getCoefficient() >= 1.6;
                if (point && coef && checkFirstAlgorithm) {

                    oddsBySignal = odds;
                    orderToBot.setNameAlgorithm("Алгоритм № 1");
                    orderToBot.setForaCoef("фора: " + oddsBySignal.getPoint() + " коэф: " + oddsBySignal.getCoefficient());
                    managerService.sendSignal(orderToBot);
                    kayByFirstAlgorithm = String.valueOf(setNow);
                    log.warn("SIGNAAAAAAAAAAAAAAAAAAAAAAL: {} | {}", odds.getPoint(), odds.getCoefficient());

                } else {
                    log.warn("NOT SIGNAL: {} |{}", odds.getPoint(), odds.getCoefficient());
                }
            });

        } else if (volleyballGame.getWhoIsFavorite().equals("second")) {
            volleyballGame.getOddsSecondTeamList().forEach(odds -> {
                boolean point = odds.getPoint().equals(-4.5);
                boolean coef = odds.getCoefficient() >= 1.6;
                if (point && coef && checkFirstAlgorithm) {
                    oddsBySignal = odds;
                    orderToBot.setNameAlgorithm("Алгоритм № 1");
                    orderToBot.setForaCoef("фора: " + oddsBySignal.getPoint() + " коэф: " + oddsBySignal.getCoefficient());
                    managerService.sendSignal(orderToBot);
                    kayByFirstAlgorithm = String.valueOf(setNow);
                    log.warn("SIGNAAAAAAAAAAAAAAAAAAAAAAL: {} | {}", odds.getPoint(), odds.getCoefficient());
                } else {
                    log.warn("NOT SIGNAL: {} |{}", odds.getPoint(), odds.getCoefficient());
                }
            });
        }
    }

    private void checkSignalBySecondAlgorithm(VolleyballGame volleyballGame, OrderToBot orderToBot) {

        checkSecondAlgorithmTwo = !kayBySecondAlgorithm.equals(String.valueOf(setNow));
        log.info("checkFirstAlgorithm = {} , !kayByFirstAlgorithm.equals(String.valueOf(setNow)): {}, setNow: {}", checkFirstAlgorithm, !kayByFirstAlgorithm.equals(String.valueOf(setNow)), setNow);

        if (volleyballGame.getWhoIsFavorite().equals("first")) {
            volleyballGame.getOddsFirstTeamList().forEach(odds -> {
                boolean point = odds.getPoint().equals(-3.5);
                boolean coef = odds.getCoefficient() >= 1.6;

                log.info("Result check Signal second: {}, {}, {}, {}", point, coef, checkSecondAlgorithmTwo, setNow == volleyballGame.getCountFirstTeam() + volleyballGame.getCountSecondTeam() + 1);
                if (point && coef && checkSecondAlgorithmTwo && setNow == volleyballGame.getCountFirstTeam() + volleyballGame.getCountSecondTeam() + 1) {
                    oddsBySignal = odds;
                    orderToBot.setNameAlgorithm("Алгоритм № 2");
                    orderToBot.setForaCoef("фора: " + oddsBySignal.getPoint() + " коэф: " + oddsBySignal.getCoefficient());
                    managerService.sendSignal(orderToBot);
                    kayBySecondAlgorithm = String.valueOf(setNow);
                    log.warn("SIGNAAAAAAAAAAAAAAAAAAAAAAL: {} | {}", odds.getPoint(), odds.getCoefficient());
                } else {
                    log.warn("NOT SIGNAL: {} |{}", odds.getPoint(), odds.getCoefficient());
                }
            });

        } else if (volleyballGame.getWhoIsFavorite().equals("second")) {
            volleyballGame.getOddsSecondTeamList().forEach(odds -> {
                boolean point = odds.getPoint().equals(-3.5);
                boolean coef = odds.getCoefficient() >= 1.6;
                log.info("Result check Signal second: {}, {}, {}, {}", point, coef, checkSecondAlgorithmTwo, setNow == volleyballGame.getCountFirstTeam() + volleyballGame.getCountSecondTeam() + 1);

                if (point && coef && checkSecondAlgorithmTwo && setNow == volleyballGame.getCountFirstTeam() + volleyballGame.getCountSecondTeam() + 1) {
                    kayBySecondAlgorithm = String.valueOf(setNow);
                    oddsBySignal = odds;
                    orderToBot.setNameAlgorithm("Алгоритм № 2");
                    orderToBot.setForaCoef("фора: " + oddsBySignal.getPoint() + " коэф: " + oddsBySignal.getCoefficient());
                    managerService.sendSignal(orderToBot);
                    kayBySecondAlgorithm = String.valueOf(setNow);
                    log.warn("SIGNAAAAAAAAAAAAAAAAAAAAAAL: {} | {}", odds.getPoint(), odds.getCoefficient());
                } else {
                    log.warn("NOT SIGNAL: {} |{}", odds.getPoint(), odds.getCoefficient());
                }
            });
        }
    }


    private boolean checkBlockBet() {
        WebElement cofsBlockElement = driverWait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[@class='blockContent kofsContent']")));
        String spanClass = cofsBlockElement.findElements(By.xpath(".//div[starts-with(@id, 'group_')]")).get(0).findElement(By.xpath(".//span[starts-with(@class,'betBut')]")).getAttribute("class");
        String spanClassTwo = cofsBlockElement.findElements(By.xpath(".//div[starts-with(@id, 'group_')]")).get(1).findElement(By.xpath(".//span[starts-with(@class,'betBut')]")).getAttribute("class");
        if (spanClass.equals("betBut lock") && spanClassTwo.equals("betBut lock")) {
            return true;
        } else {
            return false;
        }
    }


    private int getSetNow() {
        int set = -1;
        String textSetNow = "";
        try {
            textSetNow = driverWait.until(ExpectedConditions.presenceOfNestedElementLocatedBy(By.xpath("//div[@class='tablo liveTablo voleyball']"), By.xpath(".//div[@class='date']"))).getText().replaceAll("\\D", "").trim();
        } catch (Exception ex) {
            log.error("Has not element for getSetNow()");
        }
        System.out.println("text: " + textSetNow);
        if (!textSetNow.isEmpty())
            set = Integer.parseInt(textSetNow);
        return set;
    }

    private boolean clickByCountElement() {

        boolean isSelected;
        WebElement countElement = null;
        try {
            countElement = driverWait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(xpathByElementWithMainInfo)));
        } catch (Exception e) {
            log.error("Error clickByCountElement(): {}", e.getStackTrace());
        }
        if (Objects.nonNull(countElement)) {
            String countElementClass = countElement.getAttribute("class");
            if (!countElementClass.contains("active")) {
                driverWait.until(ExpectedConditions.elementToBeClickable(By.xpath(xpathByElementWithMainInfo))).click();
                log.info("!countElementClass.contains('active')");
            } else {

            }

            isSelected = true;

        } else {
            isSelected = false;
        }

        return isSelected;
    }

    private String getCoefficientUrlBySet() {
        String resultUrl = null;
        try {

            log.warn("setNOW: {}", setNow);

            List<WebElement> elementsWithTournamentName = null;
            try {
                //todo
                elementsWithTournamentName = driverWait.until(ExpectedConditions.presenceOfElementLocated(
                        By.xpath(".//a[@id='sport_6']//following::ul")))
                        .findElements(By.xpath("//*[contains(text(),'" + volleyballGame.getTournamentName() + "')]"));
            } catch (Exception ex) {
                log.error("Hasn`t Tournament");
                ex.printStackTrace();
            }
            log.info("elementsWithTournamentName.size(): {}", elementsWithTournamentName.size());
            List<WebElement> gamesInTournament = null;

            if (Objects.nonNull(elementsWithTournamentName)) {
                for (WebElement ewtn : elementsWithTournamentName) {
                    String tournamentName = ewtn.getText();
                    if (tournamentName.equals(volleyballGame.getTournamentName())) {
//                    System.out.println("OJ");
                        gamesInTournament = ewtn.findElements(By.xpath("./../..//ul[@class='sub2']/li"));
                    }
                }
            } else {
                log.warn("IS NULL elementsWithTournamentName: {}", elementsWithTournamentName);
            }


            List<WebElement> setsElement = null;
            //todo
            if (Objects.nonNull(gamesInTournament)) {
                for (WebElement elementGame : gamesInTournament) {
                    String teamsName = elementGame.findElement(By.xpath("./a[@class='link']/span[@class='name']")).getText().trim();
                    System.out.println(teamsName + " | " + volleyballGame.getNameFirstTeam() + " : " + volleyballGame.getNameSecondTeam());

                    if (teamsName.contains(volleyballGame.getNameFirstTeam()) && teamsName.contains(volleyballGame.getNameSecondTeam())) {
                        setsElement = elementGame.findElement(By.xpath("./a[@class='link']/span[@class='name']")).findElements(By.xpath("./../../ul/li//span[@class='dop']"));
                    }
                }
            } else {
                log.warn("IS NULL gamesInTournament: {}", gamesInTournament);
            }

            if (Objects.nonNull(setsElement)) {
                for (WebElement elementSet : setsElement) {
                    String s = elementSet.getText().trim();
                    log.warn(s);
                    if (elementSet.getText().trim().startsWith(String.valueOf(setNow).concat("-й Сет"))) {
                        resultUrl = driverWait.until(ExpectedConditions.presenceOfNestedElementLocatedBy(elementSet, By.xpath("./.."))).getAttribute("href");
                    }
                }
            } else {
                log.warn("IS NULL setsElement: {}", setsElement);
            }
            log.info("resultUrl: {}", resultUrl);


        } catch (Exception ex) {
            log.error("Error in resultUrl bott: {}", ex.getMessage());
        }
        return resultUrl;

    }


    private boolean findVolleyballGameOnLivePage() {
        List<WebElement> elementsGameFromLivePage = driverWait.until(
                ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector("div[class^='kofsTable']")));

        String tournamentNameOnLivePage = "";
        String firstTeamName = "";
        String secondTeamName = "";
        String href = "";

        List<VolleyballGame> vgList = new ArrayList<>();
        boolean result = false;

        for (WebElement el : elementsGameFromLivePage) {
            try {
                if (el.getAttribute("class").contains("kofsTableLigaName")) {
                    tournamentNameOnLivePage = el.getText().trim();
                    log.info("tournamentNameOnLivePage: {}", tournamentNameOnLivePage);
                } else if (el.getAttribute("class").contains("kofsTableLineNums")) {

                    List<String> teamsName = el.findElement(
                            By.xpath(".//span[@class='teams fl']"))
                            .findElements(By.xpath(".//span[@class='team']"))
                            .stream()
                            .map(WebElement::getText)
                            .collect(Collectors.toList());

                    if (teamsName.size() > 0) {
                        firstTeamName = teamsName.get(0).trim();
                        secondTeamName = teamsName.get(1).trim();
                    }
//                    log.info("firstTeamName: {}, secondTeamName: {}", firstTeamName, secondTeamName);
                    href = el.findElement(By.xpath(".//a[@class='nameLink fl clear']")).getAttribute("href");

                    VolleyballGame vg = new VolleyballGame();
                    vg.setTournamentName(tournamentNameOnLivePage);
                    vg.setNameFirstTeam(firstTeamName);
                    vg.setNameSecondTeam(secondTeamName);
                    vg.setLinkToGame(href);
                    vgList.add(vg);
                }
            } catch (Exception ex) {
                log.error("Error in findVolleyballGameOnLivePage(): {}", ex.getMessage());
                isContainsGameInLive = false;
            }
        }


        if (vgList.contains(volleyballGame)) {
            result = true;
            VolleyballGame vg = vgList.get(vgList.indexOf(volleyballGame));
            volleyballGame.setLinkToGame(vg.getLinkToGame());
        }
        return result;
    }

    private WebDriver getDriver() {
        log.info("Getting WebDriver");
        WebDriverManager.chromedriver().setup();
        ChromeOptions chromeOptions = new ChromeOptions();
//        chromeOptions.addArguments("--headless");
//        chromeOptions.addArguments("window-size=1800x900");
        chromeOptions.addArguments("--start-maximized");
//        chromeOptions.addArguments("--no-sandbox");
        WebDriver driver = new ChromeDriver(chromeOptions);
        disableAjax(driver);
        driverWait = (WebDriverWait) new WebDriverWait(driver, 5, 50).ignoring(NoSuchElementException.class);
        return driver;
    }


    private void disableAjax(WebDriver driver) {
        ((JavascriptExecutor) driver).executeScript(
                "var p=window.XMLHttpRequest.prototype; p.open=p.send=p.setRequestHeader=function(){};");
    }


}
