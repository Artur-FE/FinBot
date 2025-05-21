package de.ait.finbot.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.ArrayList;
import java.util.List;


@Slf4j
@Component
public class TelegramBotHandler implements SpringLongPollingBot, LongPollingSingleThreadUpdateConsumer {
    private final TelegramClient telegramClient;
    private final String token;
    public final String INFO = "Я — твой помощник по учёту расходов.\n" +
            "С помощью меня ты сможешь быстро записывать траты, следить за своими расходами и управлять своими финансами прямо здесь, в Telegram.\n" +
            "\n" +
            "Вот что я умею:\n" +
            "➕ Добавить расходы /add_expense\n" +
            "\uD83D\uDCCB Посмотреть список трат /my_expenses\n" +
            "⚙\uFE0F Настроить категории и валюту /settings\n" +
            "\n" +
            "Начнём? Выбери действие из меню ниже ⬇\uFE0F";

    public TelegramBotHandler( @Value("${bot.token}") String token) {
        this.token = token;
        telegramClient = new OkHttpTelegramClient(getBotToken());
        System.out.println(telegramClient);
        List<BotCommand> botCommandList = new ArrayList<>();
        botCommandList.add(new BotCommand("/start", "start"));
        botCommandList.add(new BotCommand("/info", "получить описание бота"));
        botCommandList.add(new BotCommand("/my_expenses", "мои расходы"));
        botCommandList.add(new BotCommand("/add_expense", "добавить расход"));
        botCommandList.add(new BotCommand("/settings", "настройки"));
        try {
            telegramClient.execute(new SetMyCommands(botCommandList, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            log.error("Ошибка в создании листа с меню");
        }
        System.out.println("конструктор отработал");
    }


    @Override
    public String getBotToken() {
        System.out.println("токен прочитан");
         return token;
    }

    @Override
    public LongPollingUpdateConsumer getUpdatesConsumer() {

        System.out.println("LongPollingUpdateConsumer getUpdatesConsumer()");
        return this;
    }

    @Override
    public void consume(Update update) {
        System.out.println("public void consume(Update update) ");
        if(update.hasMessage() && update.getMessage().hasText()){
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            switch (messageText) {
                case "/start":
                    startCommandReceived(chatId, update.getMessage().getChat().getFirstName());
                    break;
                case "/info":
                    sendMessage(chatId, INFO);
                    break;
                default: sendMessage(chatId, "Извините, пока не могу обработать данную команду");
                log.error("chatId " + String.valueOf(chatId) + " ошибка");
            }
        }
    }

    private void startCommandReceived(long chatId, String name){
        String answer = "\uD83D\uDC4B Привет " + name + ", приятно познакомиться";
        sendMessage(chatId, answer);
        sendMessage(chatId, INFO);
        log.info("ответ успешен" + chatId);
    }

    private void sendMessage(long chatId, String textToSend){
        SendMessage sendMessage = new SendMessage(String.valueOf(chatId),textToSend);


        try{
            telegramClient.execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}

