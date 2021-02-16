package orlov.poject.tbot.bot;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import orlov.poject.tbot.service.ManagerService;


@Slf4j
@Component
@PropertySource("classpath:telegram.properties")
@Getter
@Setter
public class Manager extends TelegramLongPollingBot {
    @Value("${bot.name}")
    private String BOT_NAME;
    @Value("${bot.token}")
    private String TOKEN;

    private final ManagerService managerService;

    public Manager(@Lazy ManagerService managerService) {
        this.managerService = managerService;
    }


    @Override
    public String getBotUsername() {
        return BOT_NAME;
    }

    @Override
    public String getBotToken() {
        return TOKEN;
    }

    @Override
    public void onUpdateReceived(Update update) {
        SendMessage sendMessage = managerService.handlerUpdate(update);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
//            log.error("Problem with ---> execute(resultHandler);");
            e.printStackTrace();
        }
    }

}

