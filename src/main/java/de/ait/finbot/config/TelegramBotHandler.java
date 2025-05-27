package de.ait.finbot.config;

import de.ait.finbot.mapper.ExpenseMapper;
import de.ait.finbot.mapper.UserMapper;
import de.ait.finbot.model.*;
import de.ait.finbot.service.CategoryService;
import de.ait.finbot.service.ExpenseService;
import de.ait.finbot.service.UserServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;


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
    private final Map<Long, Expense> expenseMap = new HashMap<>();
    public final String INFO = "Я — твой помощник по учёту расходов.\n" +
            "С помощью меня ты сможешь быстро записывать траты, следить за своими расходами и управлять своими финансами прямо здесь, в Telegram.\n" +
            "\n" +
            "Вот что я умею:\n" +
            "➕ Добавить расходы /add_expense\n" +
            "\uD83D\uDCCB Посмотреть список расходов /my_expenses\n" +
            "\uD83D\uDCC1 Посмотреть список категорий расходов /category \n" +
            "➕ Добавить категорию расходов /add_category\"\n" +
            // "⚙\uFE0F Настроить категории и валюту /settings\n" +
            //       "\n" +
            "Начнём? Выбери действие ниже ⬇\uFE0F";


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
            } else if (messageText.equals("/info") || messageText.equals("Информация о боте")) {
                sendMessage(chatId, INFO, keyBoard.startKeyboard());
            } else if (messageText.equals("/category")) {
                getAllCategoryForUser(chatId);
            } else if (messageText.equals("/my_expenses") || messageText.equals("Мои расходы")) {
                sendMessage(chatId, "Выберите ниже период, за который необходимо вывести расходы ⬇\uFE0F",
                        keyBoard.startExpenseKeyboard(), true);

            } else if (messageText.equals("Мои расходы за сегодня")) {
                getAllExpensesForToDayForUser(chatId);
            } else if (messageText.equals("Мои расходы за последние 7 дней")) {
                getAllExpensesFor7DayForUser(chatId);
            } else if (messageText.equals("Все мои расходы")) {
                getAllExpensesForUser(chatId);
            } else if (messageText.equals("/add_expense") || messageText.equals("Добавить расход")) {
                addExpense(chatId);
            } else if (StatusMessage.WAITING_EXPENSE.equals(statusMessageMap.get(chatId))) {
                putExpense(chatId, messageText);
            } else if (messageText.equals("/add_category")) {
                addCategory(chatId);
            } else if (StatusMessage.WAITING_CATEGORY.equals(statusMessageMap.get(chatId))) {
                putCategory(chatId, messageText);
            } else if (messageText.equals("Редактировать расходы")) {
                sendMessage(chatId, "Выберите ниже необходимое действие ⬇\uFE0F", keyBoard.editExpenseKeyboard(), false);
            } else if (messageText.equals("Удалить по ID")) {
                waitingIDForExpenseToDelete(chatId);
            } else if (StatusMessage.WAITING_ID_TO_DELETE.equals(statusMessageMap.get(chatId))) {

                deleteExpenseById(chatId, messageText);
            } else if (messageText.equals("Редактировать расход по ID")) {
                waitingIDForExpenseToEdit(chatId);
            } else if (StatusMessage.WAITING_ID_TO_EDIT.equals(statusMessageMap.get(chatId))) {
                editExpenseById(chatId, messageText);
            } else if (StatusMessage.WAITING_WHAT_EXPENSE_TO_EDIT.equals(statusMessageMap.get(chatId))) {
                if (messageText.equals("Изменить название")) {
                    editNameExpenseById(chatId);
                } else if (messageText.equals("Изменить сумму")) {
                    editAmountExpenseById(chatId);
                } else if (messageText.equals("Изменить категорию")) {
                    editCategoryExpenseById(chatId);
                } else if (messageText.equals("Изменить дату")) {
                    editDateExpenseById(chatId);
                } else if (messageText.equals("Удалить расход")) {
                    deleteExpenseByChatId(chatId);
                } else {
                    sendMessage(chatId, "Извините, пока не могу обработать данную команду");
                }
            } else if (StatusMessage.PUT_NEW_NAME_EXPENSE.equals(statusMessageMap.get(chatId))) {
                putNewNameExpenseById(chatId, messageText);
            } else if (StatusMessage.PUT_NEW_AMOUNT_EXPENSE.equals(statusMessageMap.get(chatId))) {
                putNewAmountExpenseById(chatId, messageText);
            } else if (StatusMessage.PUT_NEW_DATE_EXPENSE.equals(statusMessageMap.get(chatId))) {
                putNewDateExpenseById(chatId, messageText);
            } else if (StatusMessage.PUT_NEW_CATEGORY_EXPENSE.equals(statusMessageMap.get(chatId))) {
                putNewCategoryExpenseById(chatId, messageText);
            } else {
                sendMessage(chatId, "Извините, пока не могу обработать данную команду");
                // log.error("chatId " + chatId + " ошибка");
            }
        }
        System.out.println(statusMessageMap);
    }

    private void putNewCategoryExpenseById(long chatId, String categoryId) {
        Expense expense = null;
        Long userId = 0L;
        try {
            expense = expenseMap.get(chatId);
            userId = expense.getUser().getId();
            List<Category> categoryByUserId = categoryService.getCategoryByUserId(userId);
            if (categoryByUserId.stream().anyMatch(category -> category.getId().equals(Long.valueOf(categoryId)))) {
                Category category = categoryService.getCategoryById(Long.valueOf(categoryId));
                expense.setCategory(category);
                expenseService.addExpense(expense);
                sendMessage(chatId, "Категория изменена успешно!\n" +
                                expenseMapper.expenseToExpenseStringAllField(expense),
                        keyBoard.startKeyboard(), true);
                expenseMap.remove(chatId);
                statusMessageMap.remove(chatId);
            } else {
                sendMessage(chatId, "Введенная категория с ID " + categoryId +
                        " не найдена. Выберите ID категории из Вашего списка категорий ниже", true);
                sendMessage(chatId, "Ниже представлен список Ваших категорий с указанием ID" +
                        "\n" + categoryService.getAllCategoryForUser(chatId), true);
            }
        } catch (NumberFormatException e) {
            sendMessage(chatId, "Ошибка! Введен некорретный ID <b>" + categoryId +
                    "</b>. Допустимы только цифры. Проверьте правильность написания", true);
            log.error(String.valueOf(expense), userId, categoryId);
        }  catch (Exception e) {
            sendMessage(chatId, "Что-то пошло не так");
            log.error(String.valueOf(expense), userId, categoryId);
        }

    }

    private void editCategoryExpenseById(long chatId) {
        sendMessage(chatId, "Введите ID категории, которую хотите присвоить вашему расходу");
        sendMessage(chatId, "Ниже представлен список Ваших категорий с указанием ID" +
                "\n" + categoryService.getAllCategoryForUser(chatId), true);
        statusMessageMap.put(chatId, StatusMessage.PUT_NEW_CATEGORY_EXPENSE);

    }

    private void putNewDateExpenseById(long chatId, String newDate) {
        Expense expense = expenseMap.get(chatId);
        try {
            String date = newDate.replaceAll("[^0-9]", "");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMyyyy");
            LocalDateTime localDateTime = LocalDate.parse(date, formatter).atStartOfDay();
            expense.setCreatedAt(localDateTime);
            expenseService.addExpense(expense);
            expenseMap.remove(chatId);
            sendMessage(chatId, "Дата расхода успешно изменена!" + "\n" +
                            expenseMapper.expenseToExpenseStringAllField(expense),
                    keyBoard.startKeyboard(), true);
            statusMessageMap.remove(chatId);
        } catch (Exception e) {
            log.error(e.getMessage() + "Ошибка. Новая дата некорректна " + newDate);
            sendMessage(chatId, "Ошибка. Новая дата некорректная дата  " + newDate +
                    ". Пожалуйста, повторите попытку");
        }
    }

    private void editDateExpenseById(long chatId) {
        sendMessage(chatId, "Введите новую дату расхода. " +
                "Формат день.месяц.год, например 27.05.2025 или 27052025");
//        statusMessageMap.remove(chatId);
        statusMessageMap.put(chatId, StatusMessage.PUT_NEW_DATE_EXPENSE);
    }

    private void putNewAmountExpenseById(long chatId, String newAmountExpense) {
        Expense expense = expenseMap.get(chatId);
        try {
            BigDecimal newAmountBigdecimal = new BigDecimal(newAmountExpense);
            expense.setAmount(newAmountBigdecimal);
            expenseService.addExpense(expense);
            expenseMap.remove(chatId);
            sendMessage(chatId, "Сумма расхода успешно изменена!" + "\n" +
                            expenseMapper.expenseToExpenseStringAllField(expense),
                    keyBoard.startKeyboard(), true);
            statusMessageMap.remove(chatId);
        } catch (NumberFormatException e) {
            log.error(e.getMessage() + "Ошибка. Новая сумма расхода отправленная пользователем не может " +
                    "быть преобразована в Bigdecimal " + newAmountExpense);
            sendMessage(chatId, "Введен некорретный расход. Проверьте правильность написания, " +
                    "допустимы только цифры и точка или запятая. Например 120 или 76.58. " +
                    "Пожалуйста, повторите попытку");
        }

    }

    private void editAmountExpenseById(long chatId) {
        sendMessage(chatId, "Введите новую сумму расхода. " +
                "Допустимы только цифры и точка или запятая. Например 120 или 76.58");
//        statusMessageMap.remove(chatId);
        statusMessageMap.put(chatId, StatusMessage.PUT_NEW_AMOUNT_EXPENSE);
    }

    private void putNewNameExpenseById(long chatId, String newNameExpense) {
        Expense expense = expenseMap.get(chatId);
        expense.setNote(newNameExpense);
        expenseService.addExpense(expense);
        expenseMap.remove(chatId);
        sendMessage(chatId, "Имя расхода успешно изменено!" + "\n" + expenseMapper.expenseToExpenseStringAllField(expense), keyBoard.startKeyboard(), true);
        statusMessageMap.remove(chatId);
    }

    private void editNameExpenseById(long chatId) {
        sendMessage(chatId, "Введите новое название расхода");
        statusMessageMap.remove(chatId);
        statusMessageMap.put(chatId, StatusMessage.PUT_NEW_NAME_EXPENSE);
    }

    private void editExpenseById(long chatId, String idExpenseString) {
        Long idExpense = null;
        try {
            idExpense = Long.valueOf(idExpenseString);
            User user = userService.getUserByChatId(chatId);

            if(expenseService.findAllExpenseByUser_Id(user.getId())
                    .stream().anyMatch(expense -> expense.getId().equals(Long.valueOf(idExpenseString))))
            {
                Expense expense = expenseService.findExpenseById(idExpense);
                expenseMap.put(chatId, expense);
                sendMessage(chatId, "Найден расход: \n" +
                                expenseMapper.expenseToExpenseStringAllField(expense),
                        keyBoard.editExpenseByIdKeyboard(), true);
                statusMessageMap.remove(chatId);
                statusMessageMap.put(chatId, StatusMessage.WAITING_WHAT_EXPENSE_TO_EDIT);
            }  else {
                sendMessage(chatId, "Расход с ID: <b>" + idExpenseString + " </b> не найден. Повторите попытку",
                                          keyBoard.editExpenseByIdKeyboard(), true);
            }

        } catch (NumberFormatException e) {
            sendMessage(chatId, "Передан некорректный ID. " + idExpenseString +
                            "Вводите только цифры, например 15. Попробуйте еще раз!",
                    true);
        } catch (Exception e) {
            sendMessage(chatId, "Расход с ID " + idExpenseString +
                            " не найден. Попробуйте ввести другой ID!",
                    true);
        }
    }

    private void waitingIDForExpenseToEdit(long chatId) {
        sendMessage(chatId, "Введите ID расхода для редактирования");
        statusMessageMap.put(chatId, StatusMessage.WAITING_ID_TO_EDIT);
    }

    private void deleteExpenseById(long chatId, String idExpenseString) {
        Long idExpense = 0L;
        try {
            idExpense = Long.valueOf(idExpenseString);
            System.out.println("idExpense - " + idExpense);
            Expense expense = expenseService.removeExpenseById(idExpense);
            sendMessage(chatId, "Расход " + "<b>" + expense.getNote() + "</b>" + " c ID " + expense.getId() + "<b> успешно удален</b>", keyBoard.startKeyboard(), true);
            statusMessageMap.remove(chatId);
        } catch (NumberFormatException e) {
            sendMessage(chatId, "Передан некорректный ID " + idExpenseString +
                            ". Вводите только цифры, например 15. Попробуйте еще раз!",
                    true);
        } catch (Exception e) {
            sendMessage(chatId, "Расход с ID " + idExpenseString +
                            " не найден. Попробуйте ввести другой ID!",
                    true);

        }
    }

    private void deleteExpenseByChatId(long chatId) {
        try {
            Expense expense = expenseMap.get(chatId);
            expenseService.removeExpenseById(expense.getId());
            sendMessage(chatId, "Расход " + "<b>" + expense.getNote() + "</b>" + " c ID " + expense.getId() + "<b> успешно удален</b>", keyBoard.startKeyboard(), true);
            statusMessageMap.remove(chatId);
            expenseMap.remove(chatId);
        } catch (NumberFormatException e) {
            sendMessage(chatId, "Расход не удален." +
                    " Проверьте правильность введения", true);

        }
    }

    private void waitingIDForExpenseToDelete(long chatId) {
        sendMessage(chatId, "Введите ID расхода для удаления (только цифры), например 24");
        statusMessageMap.put(chatId, StatusMessage.WAITING_ID_TO_DELETE);
    }

    private void getAllExpensesFor7DayForUser(long chatId) {
        sendMessage(chatId, expenseService.findExpenseFor7DayByChatId(chatId), keyBoard.startExpenseKeyboard(), true);
    }

    private void getAllExpensesForUser(long chatId) {
        sendMessage(chatId, expenseService.findAllExpenseByChatId(chatId), keyBoard.startExpenseKeyboard(), true);
    }

    private void getAllExpensesForToDayForUser(long chatId) {
        sendMessage(chatId, expenseService.findExpenseForToDayByChatId(chatId), keyBoard.startExpenseKeyboard(), true);
    }

    private void putCategory(long chatId, String messageText) {
        categoryService.addCategory(chatId, messageText);
        sendMessage(chatId, "Категория " + messageText + " добавлена");
        sendMessage(chatId, "Полный список Ваших категорий. \n" +
                categoryService.getAllCategoryForUser(chatId), true);
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
                + expenseAmount + "\n" +
                "Название: " + expenseName + "\n"
                + "Категория: " + nameCategory, keyBoard.startExpenseKeyboard());

        System.out.println("Расход добавлен успешно " + expenseMapper.chatIdAndNoteToExpense(chatId, messageText));
        System.out.println(statusMessageMap.remove(chatId));

    }

    private void addExpense(long chatId) {
        sendMessage(chatId, "Введите сумму и примечание, чтобы я " +
                "мог определить в какую категорию сохранить трату\n" +
                "Например: 250 еда или 500 одежда", keyBoard.startExpenseKeyboard());
        statusMessageMap.put(chatId, StatusMessage.WAITING_EXPENSE);
    }

    private void getAllCategoryForUser(Long chatId) {
        sendMessage(chatId, categoryService.getAllCategoryForUser(chatId), true);
    }


    private void startCommandReceived(long chatId, String name) {
        String answer = "\uD83D\uDC4B Привет " + name + ", приятно познакомиться";
        sendMessage(chatId, answer, keyBoard.startKeyboard());
        sendMessage(chatId, INFO, keyBoard.startKeyboard());
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
        log.info(statusMessageMap.toString());
        sendMessage.setReplyMarkup(new ReplyKeyboardRemove(true));

        try {
            telegramClient.execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
            log.error(e.getMessage());
        }
    }

    private void sendMessageStartKeyBoard(long chatId, String textToSend) {
        SendMessage sendMessage = new SendMessage(String.valueOf(chatId), textToSend);
        sendMessage.setReplyMarkup(keyBoard.startKeyboard());
        log.info(statusMessageMap.toString());
        try {
            telegramClient.execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
            log.error(e.getMessage());
        }
    }


    private void sendMessage(long chatId, String textToSend, boolean setParseModeHtml) {
        SendMessage sendMessage = new SendMessage(String.valueOf(chatId), textToSend);
        sendMessage.setParseMode("HTML");
        // sendMessage.setReplyMarkup(keyBoard.startExpenseKeyboard());
        log.info(statusMessageMap.toString());
        try {
            telegramClient.execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
            log.error(e.getMessage());
        }
    }

    private void sendMessage(long chatId, String textToSend, ReplyKeyboardMarkup replyKeyboardMarkup, boolean setParseMode) {
        SendMessage sendMessage = new SendMessage(String.valueOf(chatId), textToSend);
        sendMessage.setReplyMarkup(replyKeyboardMarkup);
        if (setParseMode) {
            sendMessage.setParseMode("HTML");
        }
        log.info(statusMessageMap.toString());
        try {
            telegramClient.execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
            log.error(e.getMessage());
        }
    }

    private void sendMessage(long chatId, String textToSend, ReplyKeyboardMarkup replyKeyboardMarkup) {
        SendMessage sendMessage = new SendMessage(String.valueOf(chatId), textToSend);
        sendMessage.setReplyMarkup(replyKeyboardMarkup);
        log.info(statusMessageMap.toString());
        try {
            telegramClient.execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
            log.error(e.getMessage());
        }
    }

}

