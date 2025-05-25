package de.ait.finbot.config;

import de.ait.finbot.mapper.ExpenseMapper;
import de.ait.finbot.mapper.UserMapper;
import de.ait.finbot.model.*;
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
import org.telegram.telegrambots.meta.api.objects.inlinequery.InlineQuery;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
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
    private final KeyBoard keyBoard;
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


    public TelegramBotHandler(CategoryService categoryService, ExpenseService expenseService, UserServiceImpl userService, UserMapper userMapper, ExpenseMapper expenseMapper, @Value("${bot.token}") String token, KeyBoard keyBoard) {
        this.categoryService = categoryService;
        this.expenseService = expenseService;
        this.userService = userService;
        this.userMapper = userMapper;
        this.expenseMapper = expenseMapper;
        this.token = token;
        this.keyBoard = keyBoard;
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
            else if (messageText.equals("/info") || messageText.equals("Информация о боте") ) {
                sendMessage(chatId, INFO);
            }
            else if (messageText.equals("/category")) {
                getAllCategoryForUser(chatId);
            }
            else if (messageText.equals("/my_expenses") || messageText.equals("Мои расходы")) {
                sendMessage(chatId, "Выберите из меню ниже период, за который необходимо вывести расходы ⬇\uFE0F", true);

            } else if (messageText.equals("Мои расходы за сегодня")) {
                getAllExpensesForToDayForUser(chatId);
            } else if (messageText.equals("Мои расходы за последние 7 дней")) {
                getAllExpensesFor7DayForUser(chatId);
            } else if (messageText.equals("Все мои расходы")) {
                getAllExpensesForUser(chatId);
            } else if (messageText.equals("/add_expense") || messageText.equals("Добавить расход")) {
                addExpense(chatId);
            }
            else if (StatusMessage.WAITING_EXPENSE.equals(statusMessageMap.get(chatId))) {
                putExpense(chatId, messageText);
            }
            else if (messageText.equals("/add_category")) {
                addCategory(chatId);
            }
            else if (StatusMessage.WAITING_CATEGORY.equals(statusMessageMap.get(chatId))) {
                putCategory(chatId, messageText);
            }
            else {
                sendMessage(chatId, "Извините, пока не могу обработать данную команду");
                // log.error("chatId " + chatId + " ошибка");
            }
        }
    }

    private void getAllExpensesFor7DayForUser(long chatId) {
        sendMessage(chatId, expenseService.findExpenseFor7DayByChatId(chatId), true);
    }
    private void getAllExpensesForUser(long chatId) {
        sendMessage(chatId, expenseService.findAllExpenseByChatId(chatId), true);
    }

    private void getAllExpensesForToDayForUser(long chatId) {
        sendMessage(chatId, expenseService.findExpenseForToDayByChatId(chatId), true);
    }




    private void putCategory(long chatId, String messageText) {
        categoryService.addCategory(chatId, messageText);
        sendMessage(chatId, "Категория " + messageText + " добавлена");
        System.out.println(chatId + " Категория " + messageText + " добавлена");
        statusMessageMap.remove(chatId);
    }

    private void addCategory(long chatId) {
        sendMessage(chatId, "Введите название категории");
        statusMessageMap.put(chatId, StatusMessage.WAITING_CATEGORY);
    }

    private void putExpense(long chatId, String messageText) {
        Expense expense = expenseService.addExpense(expenseMapper.chatIdAndNoteToExpense(chatId, messageText));
        String expenseAmount = String.valueOf(expense.getAmount());
        String expenseName = expense.getNote();
        String nameCategory = expense.getCategory().getName();
        sendMessage(chatId, "Расход добавлен" + "\n" + "Сумма: "
                + expenseAmount + "\n"+
                "Название: " + expenseName + "\n"
                + "Категория: " + nameCategory);

        System.out.println("Расход добавлен успешно " + expenseMapper.chatIdAndNoteToExpense(chatId, messageText));
        System.out.println(statusMessageMap.remove(chatId));

    }




    private void addExpense(long chatId) {
        sendMessage(chatId, "Введите сумму и примечание, чтобы я " +
                "мог определить в какую категорию сохранить трату\n" +
                "Например: 250 еда или 500 одежда");
        statusMessageMap.put(chatId, StatusMessage.WAITING_EXPENSE);
    }

    private void getAllCategoryForUser(Long chatId) {
        sendMessage(chatId, categoryService.getAllCategoryForUser(chatId));
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


    private void sendMessage(long chatId, String textToSend) {
        SendMessage sendMessage = new SendMessage(String.valueOf(chatId), textToSend);
        sendMessage.setReplyMarkup(keyBoard.startKeyboard());
        try {
            telegramClient.execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }


    private void sendMessage(long chatId, String textToSend, boolean setParseModeHtml) {
        SendMessage sendMessage = new SendMessage(String.valueOf(chatId), textToSend);
            sendMessage.setParseMode("HTML");
        sendMessage.setReplyMarkup(keyBoard.startExpenseKeyboard());
        try {
            telegramClient.execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}

