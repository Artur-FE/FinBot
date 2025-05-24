package de.ait.finbot.config;

import de.ait.finbot.mapper.ExpenseMapper;
import de.ait.finbot.mapper.UserMapper;
import de.ait.finbot.model.Category;
import de.ait.finbot.model.Expense;
import de.ait.finbot.model.StatusMessage;
import de.ait.finbot.model.User;
import de.ait.finbot.repository.CategoryRepository;
import de.ait.finbot.service.CategoryService;
import de.ait.finbot.service.ExpenseService;
import de.ait.finbot.service.UserServiceImpl;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@Component
public class TelegramBotHandler implements SpringLongPollingBot, LongPollingSingleThreadUpdateConsumer {
    private final CategoryService categoryService;
    private final ExpenseService expenseService;
    private final TelegramClient telegramClient;
    private final UserServiceImpl userService;
    private final UserMapper userMapper;
    private final ExpenseMapper expenseMapper;
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


    public TelegramBotHandler(CategoryService categoryService, ExpenseService expenseService, UserServiceImpl userService, UserMapper userMapper, ExpenseMapper expenseMapper, @Value("${bot.token}") String token) {
        this.categoryService = categoryService;
        this.expenseService = expenseService;
        this.userService = userService;
        this.userMapper = userMapper;
        this.expenseMapper = expenseMapper;
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
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            if (messageText.equals("/start")) {
                startCommandReceived(chatId, update.getMessage().getChat().getFirstName());
            }
            else if (messageText.equals("/info")) {
                sendMessage(chatId, INFO);
            }
            else if (messageText.equals("/category")) {
                getAllCategoryForUser(chatId);
            } else if (messageText.equals("/my_expenses")) {
                User user = userService.getUserByChatId(chatId);
                System.out.println("Получен юсер " + user + user.getId() + user.getUserName());

                String collect1 = expenseService.findAllExpenseByUser_Id(user.getId())
                        .stream()
                        .map(expense -> "<b>" + expense.getNote() + "</b>" + ": " + expense.getAmount() + ". <b>Дата:</b> " + expense.getCreatedAt().getDayOfMonth() + " " + expense.getCreatedAt().getMonth() + " " + expense.getCreatedAt().getYear())
                        .collect(Collectors.joining("\n"));


                String collect = expenseService.findAllExpenseByUser_Id(user.getId())
                        .stream()
                        .map(expense -> expense.getNote())
                        .collect(Collectors.joining("\n"));
                System.out.println(user.getId());
                BigDecimal reduce = expenseService.findAllExpenseByUser_Id(user.getId())
                        .stream()
                        .map(expense -> expense.getAmount())
                        .filter(Objects::nonNull)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                System.out.println(reduce);
                String textToSend = "Список затрат: " + "\n" + collect1 + "\n" + "сумма затрат " + reduce;
                SendMessage sendMessage = new SendMessage(String.valueOf(chatId), textToSend);
                sendMessage.setParseMode("HTML");
                try {
                    telegramClient.execute(sendMessage);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
                //sendMessage(chatId, "Список затрат: " + "\n" + collect1 + "\n" + "сумма затрат " + reduce ;

            } else if (messageText.equals("/add_expense")) {
                sendMessage(chatId, "Введите сумму и примечание, чтобы я " +
                        "мог определить в какую категорию сохранить трату\n" +
                        "Например: 250 еда или 500 одежда");
                statusMessageMap.put(chatId, StatusMessage.WAITING_EXPENSE);
            }
            else if (StatusMessage.WAITING_EXPENSE.equals(statusMessageMap.get(chatId))) {
                Expense expense = expenseService.addExpense(expenseMapper.chatIdAndNoteToExpense(chatId, messageText));
                String expenseAmount = String.valueOf(expense.getAmount());
                String expenseName = expense.getNote();
                String nameCategory = expense.getCategory().getName();
                //Expense expenseByNote = expenseService.findExpenseByNote(expenseName);
               // String expenseCategory = expenseByNote.getCategory().getName();
                sendMessage(chatId, "Расход добавлен" + "\n" + "Сумма: "
                        + expenseAmount + "\n"+
                        "Название: " + expenseName + "\n"
                        + "Категория: " + nameCategory);

              System.out.println("Расход добавлен успешно " + expenseMapper.chatIdAndNoteToExpense(chatId, messageText));
                System.out.println(statusMessageMap.remove(chatId));

            }
            else if (messageText.equals("/add_category")) {
                sendMessage(chatId, "Введите название категории");
                statusMessageMap.put(chatId, StatusMessage.WAITING_CATEGORY);
            }
            else if (StatusMessage.WAITING_CATEGORY.equals(statusMessageMap.get(chatId))) {
                categoryService.addCategory(chatId, messageText);
                sendMessage(chatId, "Категория " + messageText + " добавлена");
                System.out.println(chatId + " Категория " + messageText + " добавлена");
                statusMessageMap.remove(chatId);
            }

            else {
                sendMessage(chatId, "Извините, пока не могу обработать данную команду");
                // log.error("chatId " + chatId + " ошибка");

            }
        }
    }

    private void startCommandReceived(long chatId, String name) {
        String answer = "\uD83D\uDC4B Привет " + name + ", приятно познакомиться";
        sendMessage(chatId, answer);
        sendMessage(chatId, INFO);
        if (userService.getUserByChatId(chatId).getUserName() == null) {
            User user = userService.addUser(userMapper.chatIdAndNameToUser(chatId, name));
            System.out.println(user + " успешно добавлен в бд");

        } else {
            System.out.println("Пользователь уже был в базе");
        }
        log.info("ответ успешен" + chatId);
    }

    private void getAllCategoryForUser(Long chatId) {
        sendMessage(chatId, categoryService.getAllCategoryForUser(chatId));
    }

    private void sendMessage(long chatId, String textToSend) {
        SendMessage sendMessage = new SendMessage(String.valueOf(chatId), textToSend);
        try {
            telegramClient.execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}

