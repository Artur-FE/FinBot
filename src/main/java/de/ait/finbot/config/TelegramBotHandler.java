package de.ait.finbot.config;

import de.ait.finbot.mapper.UserMapper;
import de.ait.finbot.model.Category;
import de.ait.finbot.model.StatusMessage;
import de.ait.finbot.model.User;
import de.ait.finbot.repository.CategoryRepository;
import de.ait.finbot.service.CategoryService;
import de.ait.finbot.service.UserServiceImpl;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Slf4j
@Component
public class TelegramBotHandler implements SpringLongPollingBot, LongPollingSingleThreadUpdateConsumer {
    private final CategoryService categoryService;
    private final TelegramClient telegramClient;
    private final UserServiceImpl userService;
    private final UserMapper userMapper;
    private final String token;
    private final Map<Long, StatusMessage> statusMessageMap = new HashMap<>();
    public final String INFO = "Я — твой помощник по учёту расходов.\n" +
            "С помощью меня ты сможешь быстро записывать траты, следить за своими расходами и управлять своими финансами прямо здесь, в Telegram.\n" +
            "\n" +
            "Вот что я умею:\n" +
            "➕ Добавить расходы /add_expense\n" +
            "\uD83D\uDCCB Посмотреть список трат /my_expenses\n" +
            "⚙\uFE0F Настроить категории и валюту /settings\n" +
            "\n" +
            "Начнём? Выбери действие из меню ниже ⬇\uFE0F";

    public TelegramBotHandler(CategoryService categoryService, UserServiceImpl userService, UserMapper userMapper, @Value("${bot.token}") String token) {
        this.categoryService = categoryService;
        this.userService = userService;
        this.userMapper = userMapper;
        this.token = token;
        telegramClient = new OkHttpTelegramClient(getBotToken());
        System.out.println(telegramClient);
        List<BotCommand> botCommandList = new ArrayList<>();
        botCommandList.add(new BotCommand("/start", "start"));
        botCommandList.add(new BotCommand("/info", "получить описание бота"));
        botCommandList.add(new BotCommand("/my_expenses", "мои расходы"));
        botCommandList.add(new BotCommand("/add_expense", "добавить расход"));
        botCommandList.add(new BotCommand("/category", "категории расходов"));
        botCommandList.add(new BotCommand("/add_category", "добавить категорию расходов"));
        botCommandList.add(new BotCommand("/settings", "настройки"));
        try {
            telegramClient.execute(new SetMyCommands(botCommandList, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            log.error("Ошибка в создании листа с меню");
        }
        System.out.println("конструктор отработал");

        categoryService.init();
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
                case "/category":
                    User userByChatId = userService.getUserByChatId(chatId);
                    userByChatId.getId();
//                    List<Category> categoryByUserId = categoryService.getCategoryByUserId(1L);
//                    Category category = categoryByUserId.get(0);
//                    System.out.println(category.getName());
                    List<Category> resultCategory = categoryService.getCategoryByUserId(null);
                    List<Category> categoryByUserId = categoryService.getCategoryByUserId(userByChatId.getId());
                    resultCategory.addAll(categoryByUserId);
                    String collect = resultCategory.stream()
                            .map(Category::getName)
                            .collect(Collectors.joining("\n"));

//                    String category = categoryService.getCategoryByUserId(userByChatId.getId())
//                            .stream()
//                                   .map(Category::getName)
//                            .collect(Collectors.joining("\n"));
//                    String collect = categoryService.getCategoryByUserId(null)
//                            .stream()
//                            .map(Category::getName)
//                            .collect(Collectors.joining("\n"));
                    sendMessage(chatId, collect);
                    break;
                case "/add_category":
                        sendMessage(chatId, "Введите название категории");
                        statusMessageMap.put(chatId, StatusMessage.WAITING_CATEGORY);
                    break;
                default: sendMessage(chatId, "Извините, пока не могу обработать данную команду");
                    //log.error("chatId " + String.valueOf(chatId) + " ошибка");

                if(statusMessageMap.getOrDefault(chatId, StatusMessage.WAITING_CATEGORY).equals(StatusMessage.WAITING_CATEGORY)){
                    categoryService.addCategory(chatId, messageText);
                    sendMessage(chatId, "Категория " + messageText + " добавлена");
                    System.out.println(chatId +  " Категория " + messageText + " добавлена");
                    statusMessageMap.remove(chatId);
                }
            }
        }
    }

    private void startCommandReceived(long chatId, String name){
        String answer = "\uD83D\uDC4B Привет " + name + ", приятно познакомиться";
        sendMessage(chatId, answer);
        sendMessage(chatId, INFO);
        if(userService.getUserByChatId(chatId).getUserName() == null) {
            User user = userService.addUser(userMapper.chatIdAndNameToUser(chatId, name));
            System.out.println(user + " успешно добавлен в бд");

        } else {
            System.out.println("Пользователь уже был в базе");
        }
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

