package orlov.poject.tbot.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import orlov.poject.tbot.bot.Manager;
import orlov.poject.tbot.entity.OrderToBot;
import orlov.poject.tbot.entity.VolleyballGame;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
@PropertySource("classpath:map.properties")
public class ManagerService {

    private Map<String, String> cache = new ConcurrentHashMap<>();

    @Autowired
    private Manager manager;

    @Value("${owner}")
    private String owner;

    @PostConstruct
    private void init(){
        cache.put(owner, "ok");
//        OrderToBot orderToBot = new OrderToBot();
//        orderToBot.setTournament("Вы авторизированы");
//        sendSignal(orderToBot);
    }

    public void sendSignal(OrderToBot orderToBot) {
//        log.warn("sendSignal() START");
        for (Map.Entry<String, String> es : cache.entrySet()) {

            if (es.getValue().equals("ok")) {
                String chatId = es.getKey();
                log.warn("Send signal to chatId: {}", chatId);
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(chatId);
                String message = orderToBot.getNameAlgorithm() + "\n" +
                        "турнир: " + orderToBot.getTournament() + "\n" +
                        "команды: " + orderToBot.getTeamsName() + "\n" +
                        "коэф до матча: " + orderToBot.getCoefficientsBeforeStart() + "\n" +
                        "последние коэф: " + orderToBot.getCoefficientsNow() + "\n" +
                        orderToBot.getSetAndCountSet() + "\n" +
                        orderToBot.getForaCoef();
                sendMessage.setText(message);

                try {
                    manager.execute(sendMessage);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    public SendMessage handlerUpdate(Update update) {
        SendMessage sendMessage = new SendMessage();
        String chatId = String.valueOf(update.getMessage().getChatId());
        String incomingMessage;
        String newMessage = "";
        String stateChatId;


        if (update.hasMessage() && update.getMessage().hasText()) {

            incomingMessage = update.getMessage().getText();
            sendMessage.setChatId(chatId);

            boolean hasInCache = cache.containsKey(chatId);

            if (!hasInCache) {
                cache.put(chatId, "start");
                stateChatId = "start";
            } else {
                stateChatId = cache.get(chatId);
            }


            if (stateChatId.equals("start")) {

                if (incomingMessage.equals("Включить оповещение")) {
                    stateChatId = "auth";
                    cache.put(chatId, stateChatId);
                    sendMessage.setText("Введите секретное слово");
                } else {
                    sendMessage.setText("Нажмите кнопку 'Включить оповещение'");
                }

                sendMessage.setReplyMarkup(startMarkup());


            } else if (stateChatId.equals("auth")) {
                if (incomingMessage.equalsIgnoreCase("auth")) {
                    newMessage = "Оповещение включено";
                    stateChatId = "ok";
                    cache.put(chatId, stateChatId);
                    sendMessage.setText(newMessage);
                    sendMessage.setReplyMarkup(okMarkup());
                } else if (incomingMessage.equals("Включить оповещение")) {
                    newMessage = "Введите секретное слово";
                    sendMessage.setText(newMessage);
                } else {
                    newMessage = "Вы ввели не верное слово";
                    sendMessage.setText(newMessage);
                }

            } else if (stateChatId.equals("ok")) {
                if (incomingMessage.equals("Отключить оповещение")) {
                    cache.remove(chatId);
                    newMessage = "Оповещание отключено";
                    sendMessage.setText(newMessage);
                    sendMessage.setReplyMarkup(startMarkup());
                } else {
                    newMessage = "Я не понимаю что вы от меня хотите";
                    sendMessage.setText(newMessage);
                }
            }


        } else {
            newMessage = "Неправельный шаг.";
            sendMessage.setText(newMessage);
        }

        return sendMessage;


    }

    public SendMessage buttonInline(Update update) {
        SendMessage sendMessage = new SendMessage();
        Long chatId = update.getMessage().getChatId();
        sendMessage.setChatId(chatId.toString());
        sendMessage.setText("Okey");

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

        InlineKeyboardButton buttonYes = new InlineKeyboardButton();
        buttonYes.setText("Да");
        InlineKeyboardButton buttonNo = new InlineKeyboardButton();
        buttonNo.setText("Нет");
        InlineKeyboardButton buttonThink = new InlineKeyboardButton();
        buttonThink.setText("Думаю");
        InlineKeyboardButton buttonKnow = new InlineKeyboardButton();
        buttonKnow.setText("Знаю");

        buttonYes.setCallbackData("ok");
        buttonNo.setCallbackData("no");
        buttonThink.setCallbackData("think");
        buttonKnow.setCallbackData("know");

        List<InlineKeyboardButton> buttonListRowFirst = new ArrayList<>();
        buttonListRowFirst.add(buttonYes);
        buttonListRowFirst.add(buttonNo);
        List<InlineKeyboardButton> buttonListRowSecond = new ArrayList<>();
        buttonListRowSecond.add(buttonThink);
        buttonListRowSecond.add(buttonKnow);

        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        rowList.add(buttonListRowFirst);
        rowList.add(buttonListRowSecond);
        inlineKeyboardMarkup.setKeyboard(rowList);
        sendMessage.setReplyMarkup(inlineKeyboardMarkup);

        return sendMessage;
    }

    public ReplyKeyboardMarkup startMarkup() {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);
        List<KeyboardRow> buttons = new ArrayList<>();
        KeyboardRow row1 = new KeyboardRow();
        row1.add(new KeyboardButton("Включить оповещение"));
//        row1.add(new KeyboardButton("Отключить оповещение"));
        buttons.add(row1);
        replyKeyboardMarkup.setKeyboard(buttons);
        return replyKeyboardMarkup;
    }

    public ReplyKeyboardMarkup okMarkup() {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);
        List<KeyboardRow> buttons = new ArrayList<>();
        KeyboardRow row1 = new KeyboardRow();
//        row1.add(new KeyboardButton("Включить оповещение"));
        row1.add(new KeyboardButton("Отключить оповещение"));
        buttons.add(row1);
        replyKeyboardMarkup.setKeyboard(buttons);
        return replyKeyboardMarkup;
    }


}
