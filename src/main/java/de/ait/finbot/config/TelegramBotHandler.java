package de.ait.finbot.config;

import de.ait.finbot.composer.CategoryMessageComposer;
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
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
    private final CategoryMessageComposer categoryMessageComposer;
    //private final Map<Long, StatusMessage> statusMessageMap = new HashMap<>();
    private final StatusMessageMap statusMessageMap;
   // private final Map<Long, Expense> expenseMap = new HashMap<>();
    private final ExpenseMap expenseMap;
   // private final Map<Long, Category> categoryMap = new HashMap<>();
    private final CategoryMap categoryMap;
    public final String INFO = "Я — твой помощник по учёту расходов.\n" +
            "С помощью меня ты сможешь быстро записывать траты, следить за своими расходами и управлять своими финансами прямо здесь, в Telegram.\n" +
            "\n" +
            "Вот что ты можешь делать:\n" +
            "➕ Добавить расходы /add_expense\n" +
            "\uD83D\uDCCB Посмотреть список расходов /my_expenses\n" +
            "\u270F\uFE0F Редактировать расходы /edit_expense\n" +
            "\uD83D\uDD0D Поиск расходов /search_expense\n" +
            "\uD83D\uDCC1 Посмотреть список категорий расходов /category\n" +
            "➕ Добавить категорию расходов /add_category\n" +
            "\u270F\uFE0F Редактировать категории /edit_category\n" +
            "\n" +
            "Начнём? Выбери действие ниже ⬇\uFE0F";

    public TelegramBotHandler(CategoryService categoryService, ExpenseService expenseService, UserServiceImpl userService, UserMapper userMapper, ExpenseMapper expenseMapper, @Value("${bot.token}") String token, KeyBoard keyBoard, CategoryMessageComposer categoryCommand, StatusMessageMap statusMessageMap, ExpenseMap expenseMap, CategoryMap categoryMap) {
        this.categoryService = categoryService;
        this.expenseService = expenseService;
        this.userService = userService;
        this.userMapper = userMapper;
        this.expenseMapper = expenseMapper;
        this.token = token;
        this.keyBoard = keyBoard;
        this.categoryMessageComposer = categoryCommand;
        this.statusMessageMap = statusMessageMap;
        this.expenseMap = expenseMap;
        this.categoryMap = categoryMap;
        telegramClient = new OkHttpTelegramClient(getBotToken());
        System.out.println(telegramClient);
        List<BotCommand> botCommandList = new ArrayList<>();
        botCommandList.add(new BotCommand("/start", "главное меню"));
        botCommandList.add(new BotCommand("/info", "получить описание бота"));
        botCommandList.add(new BotCommand("/my_expenses", "мои расходы"));
        botCommandList.add(new BotCommand("/add_expense", "добавить расход"));
        botCommandList.add(new BotCommand("/category", "мои категории"));
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
        return this;
    }


    @Override
    public void consume(Update update) {
        System.out.println("public void consume(Update update) ");
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            if (messageText.equals("/start")
                    || messageText.equals(IncomingMessage.BACK_TO_MAIN_MENU.getDescription())
                    || messageText.equals(IncomingMessage.TO_MAIN_MENU.getDescription())
                    || messageText.equals(IncomingMessage.CANCEL_AND_EXIT_TO_THE_MAIN_MENU.getDescription())
                    || messageText.equals(IncomingMessage.MAIN_MENU.getDescription())) {
                statusMessageMap.remove(chatId);
                startCommandReceived(chatId, update.getMessage().getChat().getFirstName());
                sendMessage(chatId, update.getMessage().getChat().getFirstName() + ", ты в главном меню",
                        keyBoard.startKeyboard());
            } else if (messageText.equals("/info")
                    || messageText.equals(IncomingMessage.BOT_INFO.getDescription())) {
                statusMessageMap.remove(chatId);
                sendMessage(chatId, INFO, keyBoard.startKeyboard());
            } else if (messageText.equals("/category")
                    || messageText.equals(IncomingMessage.CATEGORY_LIST.getDescription())) {
                statusMessageMap.remove(chatId);
                categoryMap.remove(chatId);
            getAllCategoryForUser(chatId);
            } else if (messageText.equals("/my_expenses")
                    || messageText.equals(IncomingMessage.MY_EXPENSES.getDescription())) {
                statusMessageMap.remove(chatId);
                expenseMap.remove(chatId);
                sendMessage(chatId, "Выберите ниже период, за который необходимо вывести расходы ⬇\uFE0F",
                        keyBoard.startExpenseKeyboard(), true);
            } else if (messageText.equals(IncomingMessage.EXPENSES_TODAY.getDescription())) {
                getAllExpensesForToDayForUser(chatId);
            } else if (messageText.equals(IncomingMessage.EXPENSES_IN_7_DAYS.getDescription())) {
                getAllExpensesFor7DayForUser(chatId);
            } else if (messageText.equals(IncomingMessage.ALL_MY_EXPENSES.getDescription())) {
                getAllExpensesForUser(chatId);
            } else if (messageText.equals(IncomingMessage.FIND_EXPENSE.getDescription())) {
                searchExpense(chatId);
            } else if (messageText.equals("/add_expense")
                    || messageText.equals(IncomingMessage.ADD_EXPENSE.getDescription())) {
                addExpense(chatId);
            } else if (StatusMessage.WAITING_EXPENSE.equals(statusMessageMap.get(chatId))) {
                putExpense(chatId, messageText);
            } else if (messageText.equals("/add_category")
                    || messageText.equals(IncomingMessage.ADD_CATEGORY.getDescription())
                    || StatusMessage.WAITING_CATEGORY.equals(statusMessageMap.get(chatId))) {
                addCategory(chatId, messageText);
            } else if (messageText.equals(IncomingMessage.DELETE_CATEGORY.getDescription())
                    || StatusMessage.WAITING_ID_CATEGORY_TO_DELETE.equals(statusMessageMap.get(chatId))) {
                deleteCategory(chatId, messageText);
            } else if (messageText.equals(IncomingMessage.EDIT_EXPENSES.getDescription())) {
                sendMessage(chatId, "Выберите ниже необходимое действие ⬇\uFE0F",
                        keyBoard.editExpenseKeyboard(), false);
            } else if (messageText.equals(IncomingMessage.DELETE_BY_ID.getDescription())
                    || StatusMessage.WAITING_ID_TO_DELETE.equals(statusMessageMap.get(chatId))) {
                waitingIDForExpenseToDelete(chatId, messageText);
            } else if (messageText.equals(IncomingMessage.DELETE_ALL_EXPENSES.getDescription())
                    || messageText.equals(IncomingMessage.SURE_DELETE_ALL_EXPENSES.getDescription())) {
                deleteAllExpenseByUser(chatId, messageText);
            } else if (messageText.equals(IncomingMessage.EDIT_BY_ID.getDescription())
                    || messageText.equals(IncomingMessage.FIND_BY_ID.getDescription())
                    || StatusMessage.WAITING_ID_TO_EDIT.equals(statusMessageMap.get(chatId))) {
                waitingIDForExpenseToEdit(chatId, messageText);
            } else if (messageText.equals(IncomingMessage.EDIT_BY_NAME.getDescription())
                    || messageText.equals(IncomingMessage.FIND_BY_NAME.getDescription())
                    || StatusMessage.WAITING_NAME_TO_EDIT.equals(statusMessageMap.get(chatId))) {
                waitingNameForExpenseToEdit(chatId, messageText);
            } else if (messageText.equals(IncomingMessage.DELETE_BY_NAME.getDescription())
                    || StatusMessage.WAITING_NAME_TO_DELETE.equals(statusMessageMap.get(chatId))) {
                waitingNameForExpenseToDelete(chatId, messageText);
            } else if (StatusMessage.WAITING_WHAT_EXPENSE_TO_EDIT.equals(statusMessageMap.get(chatId))) {
                if (messageText.equals(IncomingMessage.EDIT_NAME.getDescription())) {
                    editNameExpenseById(chatId);
                } else if (messageText.equals(IncomingMessage.EDIT_AMOUNT.getDescription())) {
                    editAmountExpenseById(chatId);
                } else if (messageText.equals(IncomingMessage.EDIT_CATEGORY.getDescription())) {
                    editCategoryExpenseById(chatId);
                } else if (messageText.equals(IncomingMessage.EDIT_DATE.getDescription())) {
                    editDateExpenseById(chatId);
                } else if (messageText.equals(IncomingMessage.DELETE_EXPENSE.getDescription())
                        || messageText.equals(IncomingMessage.ACCEPT_DELETE_EXPENSE.getDescription())) {
                    deleteExpenseByChatId(chatId);
                } else {
                    sendMessage(chatId, "Извините, пока не могу обработать данную команду", keyBoard.startKeyboard());
                }
            } else if (StatusMessage.PUT_NEW_NAME_EXPENSE.equals(statusMessageMap.get(chatId))) {
                putNewNameExpenseById(chatId, messageText);
            } else if (StatusMessage.PUT_NEW_AMOUNT_EXPENSE.equals(statusMessageMap.get(chatId))) {
                putNewAmountExpenseById(chatId, messageText);
            } else if (StatusMessage.PUT_NEW_DATE_EXPENSE.equals(statusMessageMap.get(chatId))) {
                putNewDateExpenseById(chatId, messageText);
            } else if (StatusMessage.PUT_NEW_CATEGORY_EXPENSE.equals(statusMessageMap.get(chatId))) {
                putNewCategoryExpenseById(chatId, messageText);
            } else if (messageText.equals(IncomingMessage.EDIT_CATEGORY_NAME.getDescription())
                    || StatusMessage.WAITING_ID_CATEGORY_TO_EDIT_NAME.equals(statusMessageMap.get(chatId))) {
                editCategoryName(chatId, messageText);
            } else if (StatusMessage.WAITING_NAME_CATEGORY_TO_EDIT_NAME.equals(statusMessageMap.get(chatId))) {
                putCategoryName(chatId, messageText);
            }

            else {
                sendMessage(chatId, "Извините, пока не могу обработать данную команду", keyBoard.startKeyboard());
                // log.error("chatId " + chatId + " ошибка");
            }
        }
        System.out.println(statusMessageMap);
        System.out.println(categoryMap);
        System.out.println(expenseMap);
    }

    private void putCategoryName(long chatId, String messageText) {
        Category category = categoryMap.get(chatId);
        categoryService.editNameCategory(category, messageText);
        sendMessage(chatId, "Имя категории с <b>ID: " + category.getId() +
                "</b> успешно изменено на <b>" + category.getName() + "</b>", keyBoard.categoryMenuKeyboard(), true);
        categoryMap.remove(chatId);
        statusMessageMap.remove(chatId);
        getAllCategoryForUser(chatId);
    }

    private void editCategoryName(long chatId, String messageText) {
        if (StatusMessage.WAITING_ID_CATEGORY_TO_EDIT_NAME.equals(statusMessageMap.get(chatId))) {
            try {
                Long categoryId = Long.valueOf(messageText);
                if (categoryService.getCustomCategoryByUser_Id(chatId).contains(categoryId)) {
                    Category category = categoryService.getCategoryById(categoryId);
                    categoryMap.put(chatId, category);
                    sendMessage(chatId, "Категория  с ID: <b>" + category.getId() + "</b> и именем: <b>"
                                    + category.getName() + "</b> успешно найдена.\n" +
                                    "Введи новое имя для этой категории",
                            keyBoard.backToStartAndCategoryMenuKeyboard(), true);
                    statusMessageMap.put(chatId, StatusMessage.WAITING_NAME_CATEGORY_TO_EDIT_NAME);
                } else {
                    sendMessage(chatId, "Введенная категория с ID: " + messageText +
                            " не является твоей категорией. Повтори ввод");
                }
            } catch (NumberFormatException e) {
                sendMessage(chatId, "ID категории состоит только из цифр. Твой <b>ID: " + messageText +
                                "</b> не из цифр!\n " + "Повтори ввод ID",
                        keyBoard.backToStartAndCategoryMenuKeyboard(), true);
            }

        } else {
            try {
                sendMessage(chatId, "Введи ID категории для редактирования. " +
                                "Ниже список доступных для редактирования категорий:\n" +
                                categoryService.getAllCategoryToEditNameForUser(chatId),
                        keyBoard.backToStartAndCategoryMenuKeyboard(), true);
                statusMessageMap.put(chatId, StatusMessage.WAITING_ID_CATEGORY_TO_EDIT_NAME);
            } catch (RuntimeException e) {
                sendMessage(chatId, "Нет доступных категорий для редактирования.",
                        keyBoard.backToStartAndCategoryMenuKeyboard(), true);
            }
        }
    }

    private void deleteCategory(long chatId, String messageText) {
        if (StatusMessage.WAITING_ID_CATEGORY_TO_DELETE.equals(statusMessageMap.get(chatId))) {
            try {
                if (categoryService.checkCategoryToDeleteForUser(chatId, messageText)) {
                    Category category = categoryService.deleteCategoryById(Long.valueOf(messageText));
                    sendMessage(chatId, "Категория с ID " + category.getId() + " удалена",
                            keyBoard.backToStartAndCategoryMenuKeyboard());
                    sendMessage(chatId, "Полный список Ваших категорий. \n" +
                                    categoryService.getAllCategoryForUser(chatId),
                            keyBoard.backToStartAndCategoryMenuKeyboard(), true);
                    statusMessageMap.remove(chatId);
                } else {
                    sendMessage(chatId, "Введенная категория с ID: " + messageText + " не является Вашей категорией. Повторите ввод");
                }
            } catch (NumberFormatException e) {
                sendMessage(chatId, "Ошибка. Вы ввели не цифру для идентификации категории. Повторите ввод.");
            }
        } else {
            try {
                sendMessage(chatId, "Удалить можно только те категории, в которых нет привязанных расходов. " +
                        "Также нельзя удалить категории по умолчанию. \n" +
                        "Введите в следующем сообщении ID категории, которую хотите удалить.", keyBoard.backToStartAndCategoryMenuKeyboard(), true);
                categoryService.getAllCategoryToDeleteForUser(chatId);
                sendMessage(chatId, "Ниже список доступных  категорий:\n" +
                        categoryService.getAllCategoryToDeleteForUser(chatId), keyBoard.backToStartAndCategoryMenuKeyboard(), true);
                statusMessageMap.put(chatId, StatusMessage.WAITING_ID_CATEGORY_TO_DELETE);
            } catch (RuntimeException e) {
                sendMessage(chatId, "Нет доступных категорий для удаления",
                        keyBoard.backToStartAndCategoryMenuKeyboard(), true);
            }
        }

    }

    private void deleteAllExpenseByUser(long chatId, String messageText) {
        if (messageText.equals(IncomingMessage.DELETE_ALL_EXPENSES.getDescription())) {
            sendMessage(chatId, "Вы уверенны, что хотите удалить все свои расходы? Действие невозможно восстановить",
                    keyBoard.deleteAllExpenseMenuKeyboard(), true);
        } else if (messageText.equals(IncomingMessage.SURE_DELETE_ALL_EXPENSES.getDescription())) {
            try {
                expenseService.removeAllExpenseByUser(chatId);
                sendMessage(chatId, "Удаление успешно! \n" +
                                expenseService.findAllExpenseByChatId(chatId),
                        keyBoard.startKeyboard(), true);
            } catch (Exception e) {
                sendMessage(chatId, "Ошибка! Что-то пошло не так! Попробуйте вернуться в главное меню и повторить попытку",
                        keyBoard.backToStartAndExpenseMenuKeyboard(), true);
            }
        }
    }

    private void searchExpense(long chatId) {
        sendMessage(chatId, "Как ты хочешь найти расход, по имени расхода или ID? Выбери в меню ниже",
                keyBoard.searchExpenseKeyboard(), true);
        statusMessageMap.put(chatId, StatusMessage.WAITING_WHAT_EXPENSE_TO_EDIT);
    }

    private void deleteExpenseByName(long chatId, String nameExpense) {
        String allExpenseByChatId = expenseService.findAllExpenseByNoteIgnoreCase(chatId, nameExpense)
                .stream()
                .map(expense -> expenseMapper.expenseToExpenseString(expense))
                .collect(Collectors.joining("\n"));
        List<Expense> listExpense = expenseService.findAllExpenseByNoteIgnoreCase(chatId, nameExpense)
                .stream()
                .filter(expense -> expense.getNote().equalsIgnoreCase(nameExpense))
                .toList();

        if (listExpense.size() == 1) {
            Expense expense = listExpense.get(0);
            sendMessage(chatId, "Найден 1 расход с именем <b>" + nameExpense + "</b>\n" +
                    "Подтвердите в меню ниже процедуру удаления расхода \n\n" +
                    expenseMapper.expenseToExpenseStringAllField(expense), keyBoard.deleteExpenseKeyboard(), true);
            statusMessageMap.put(chatId, StatusMessage.WAITING_WHAT_EXPENSE_TO_EDIT);
            expenseMap.put(chatId, expense);
            System.out.println(expenseMap);
        } else if (!allExpenseByChatId.isBlank()) {
            sendMessage(chatId, "Найдено " + listExpense.size() + " расходов с именем " + nameExpense + "\n" +
                    "Введите ID расхода для дальнейшего удаления \n\n" +
                    allExpenseByChatId, keyBoard.backToStartAndExpenseMenuKeyboard(), true);
            statusMessageMap.put(chatId, StatusMessage.WAITING_ID_TO_DELETE);
        } else {
            sendMessage(chatId, "По имени расхода <b>" + nameExpense + "</b> нет результатов. \n" +
                    "Проверьте правильность введения имени расхода и повторите попытку", keyBoard.editExpenseKeyboard(), true);
        }
    }

    private void waitingNameForExpenseToDelete(long chatId, String messageText) {
        if (StatusMessage.WAITING_NAME_TO_DELETE.equals(statusMessageMap.get(chatId))) {
            deleteExpenseByName(chatId, messageText);
            System.out.println("Блок if удалить по имени");
        } else {
            sendMessage(chatId, "Введите имя расхода для удаления. " +
                    "Вы получите список из расходов по введенному имени с указанием ID для дальнейшего удаления", true);
            statusMessageMap.put(chatId, StatusMessage.WAITING_NAME_TO_DELETE);
            System.out.println("Блок else удалить по имени");
        }
    }

    private void findExpenseByName(long chatId, String nameExpense) {
        String allExpenseByChatId = expenseService.findAllExpenseByNoteIgnoreCase(chatId, nameExpense)
                .stream()
                .map(expense -> expenseMapper.expenseToExpenseString(expense))
                .collect(Collectors.joining("\n"));
        List<Expense> listExpense = expenseService.findAllExpenseByNoteIgnoreCase(chatId, nameExpense)
                .stream()
                .filter(expense -> expense.getNote().equalsIgnoreCase(nameExpense))
                .toList();

        if (listExpense.size() == 1) {
            Expense expense = listExpense.get(0);
            sendMessage(chatId, "Найден 1 расход с именем <b>" + nameExpense + "</b>\n" +
                    "Выберите из меню ниже действия для дальнейшего редактирования \n\n" +
                    expenseMapper.expenseToExpenseStringAllField(expense), keyBoard.editExpenseByIdKeyboard(), true);
            statusMessageMap.put(chatId, StatusMessage.WAITING_WHAT_EXPENSE_TO_EDIT);
            expenseMap.put(chatId, expense);
        } else if (!allExpenseByChatId.isBlank()) {
            sendMessage(chatId, "Найдено " + listExpense.size() + " расходов с именем " + nameExpense + "\n" +
                    "Введите ID расхода для дальнейшего редактирования \n\n" +
                    allExpenseByChatId, keyBoard.backToStartAndExpenseMenuKeyboard(), true);
            statusMessageMap.put(chatId, StatusMessage.WAITING_ID_TO_EDIT);
        } else {
            System.out.println(allExpenseByChatId);
            sendMessage(chatId, "По имени расхода <b>" + nameExpense + "</b> нет результатов. \n" +
                    "Проверьте правильность введения имени расхода и повторите попытку", keyBoard.backToStartAndExpenseMenuKeyboard(), true);
        }
    }

    private void waitingNameForExpenseToEdit(long chatId, String messageText) {
        if (StatusMessage.WAITING_NAME_TO_EDIT.equals(statusMessageMap.get(chatId))) {
            findExpenseByName(chatId, messageText);
            System.out.println("поиск по имени блок if");
        } else {
            sendMessage(chatId, "Введите имя расхода для редактирования. " +
                    "Вы получите список из расходов по введенному имени с указанием ID для дальнейшего редактирования", true);
            statusMessageMap.put(chatId, StatusMessage.WAITING_NAME_TO_EDIT);
            System.out.println("поиск по имени блок else");
        }
    }

    private void putNewCategoryExpenseById(long chatId, String categoryId) {
        Expense expense = null;
        // Long userId = 0L;
        try {
            expense = expenseMap.get(chatId);
            Category category = categoryService.getCategoryById(Long.valueOf(categoryId));
            expense.setCategory(category);
            expenseService.addExpense(expense);
            sendMessage(chatId, "Категория изменена успешно!\n" +
                            expenseMapper.expenseToExpenseStringAllField(expense),
                    keyBoard.backToStartAndExpenseMenuKeyboard(), true);
            expenseMap.remove(chatId);
            statusMessageMap.remove(chatId);
        } catch (NumberFormatException e) {
            sendMessage(chatId, "Ошибка! Введен некорретный ID <b>" + categoryId +
                    "</b>. Допустимы только цифры. Проверьте правильность написания", keyBoard.backToStartAndExpenseMenuKeyboard(), true);
            log.error(String.valueOf(expense), categoryId);
        } catch (Exception e) {
            sendMessage(chatId, "Введенная категория с ID " + categoryId +
                    " не найдена. Введите ID категории из Вашего списка категорий ниже", true);
            sendMessage(chatId, "Ниже представлен список Ваших категорий с указанием ID" +
                    "\n" + categoryService.getAllCategoryForUser(chatId), keyBoard.backToStartAndExpenseMenuKeyboard(), true);

            log.error(String.valueOf(expense), categoryId);
        }

    }

    private void editCategoryExpenseById(long chatId) {
        sendMessage(chatId, "Введите ID категории, которую хотите присвоить вашему расходу", keyBoard.backToStartAndExpenseMenuKeyboard());
        sendMessage(chatId, "Ниже представлен список Ваших категорий с указанием ID" +
                "\n" + categoryService.getAllCategoryForUser(chatId), keyBoard.backToStartAndExpenseMenuKeyboard(), true);
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
                    keyBoard.backToStartAndExpenseMenuKeyboard(), true);
            statusMessageMap.remove(chatId);
        } catch (Exception e) {
            log.error(e.getMessage() + "Ошибка. Новая дата некорректна " + newDate);
            sendMessage(chatId, "Ошибка. Новая дата некорректная дата  " + newDate +
                    ". Пожалуйста, повторите попытку", keyBoard.backToStartAndExpenseMenuKeyboard(), true);
        }
    }

    private void editDateExpenseById(long chatId) {
        sendMessage(chatId, "Введите новую дату расхода. " +
                "Формат день.месяц.год, например 27.05.2025 или 27052025");
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
                    "Пожалуйста, повторите попытку", keyBoard.backToStartAndExpenseMenuKeyboard(), true);
        }

    }

    private void editAmountExpenseById(long chatId) {
        sendMessage(chatId, "Введите новую сумму расхода. " +
                "Допустимы только цифры и точка или запятая. Например 120 или 76.58");
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
            Expense expense = expenseService.findExpenseById(chatId, idExpense);
            expenseMap.put(chatId, expense);
            sendMessage(chatId, "Найден расход: \n" +
                            expenseMapper.expenseToExpenseStringAllField(expense),
                    keyBoard.editExpenseByIdKeyboard(), true);
            statusMessageMap.remove(chatId);
            statusMessageMap.put(chatId, StatusMessage.WAITING_WHAT_EXPENSE_TO_EDIT);

        } catch (NumberFormatException e) {
            sendMessage(chatId, "Передан некорректный ID. " + idExpenseString +
                            "Вводите только цифры, например 15. Попробуйте еще раз!",
                    keyBoard.backToStartAndExpenseMenuKeyboard(), true);
        } catch (Exception e) {
            sendMessage(chatId, "Расход с ID: <b>" + idExpenseString + " </b> не найден. " +
                            "Проверьте правильность введения и повторите попытку",
                    keyBoard.backToStartAndExpenseMenuKeyboard(), true);
        }
    }

    private void waitingIDForExpenseToEdit(long chatId, String messageText) {
        if (StatusMessage.WAITING_ID_TO_EDIT.equals(statusMessageMap.get(chatId))) {
            editExpenseById(chatId, messageText);
            System.out.println("рефактор успешен блок if");
        } else {
            sendMessage(chatId, "Введите ID расхода для редактирования");
            statusMessageMap.put(chatId, StatusMessage.WAITING_ID_TO_EDIT);
            System.out.println("рефактор успешен блок else");
        }

    }

    private void deleteExpenseById(long chatId, String idExpenseString) {
        Long idExpense = 0L;
        try {
            idExpense = Long.valueOf(idExpenseString);
            System.out.println("idExpense - " + idExpense);
            Expense expense = expenseService.removeExpenseById(chatId, idExpense);
            sendMessage(chatId, "Расход " + "<b>" + expense.getNote() + "</b>" + " c ID " + expense.getId() + "<b> успешно удален</b>", keyBoard.startKeyboard(), true);
            statusMessageMap.remove(chatId);
        } catch (NumberFormatException e) {
            sendMessage(chatId, "Передан некорректный ID " + idExpenseString +
                            ". Вводите только цифры, например 15. Проверьте правильность введения и повторите попытку",
                    keyBoard.backToStartAndExpenseMenuKeyboard(), true);
        } catch (Exception e) {
            sendMessage(chatId, "Расход с ID " + idExpenseString +
                            " не найден. Попробуйте ввести другой ID!", keyBoard.backToStartAndExpenseMenuKeyboard(),
                    true);

        }
    }

    private void deleteExpenseByChatId(long chatId) {
        try {
            Expense expense = expenseMap.get(chatId);
            expenseService.removeExpenseById(chatId, expense.getId());
            sendMessage(chatId, "Расход " + "<b>" + expense.getNote() + "</b>" + " c ID " + expense.getId() + "<b> успешно удален</b>", keyBoard.startKeyboard(), true);
            statusMessageMap.remove(chatId);
            expenseMap.remove(chatId);
        } catch (NumberFormatException e) {
            sendMessage(chatId, "Расход не удален." +
                    "Вернитесь в главное меню и повторите попытку", keyBoard.backToStartAndExpenseMenuKeyboard(), true);

        }
    }

    private void waitingIDForExpenseToDelete(long chatId, String messageText) {

        if (StatusMessage.WAITING_ID_TO_DELETE.equals(statusMessageMap.get(chatId))) {
            deleteExpenseById(chatId, messageText);
            System.out.println("Удалить по id блок if");
        } else {
            sendMessage(chatId, "Введите ID расхода для удаления (только цифры), например 24");
            statusMessageMap.put(chatId, StatusMessage.WAITING_ID_TO_DELETE);
            System.out.println("Удалить по id блок else");
        }
    }

    private void getAllExpensesFor7DayForUser(long chatId) {
        sendMessage(chatId, expenseService.findExpenseFor7DayByChatId(chatId),
                keyBoard.startExpenseKeyboard(), true);
    }

    private void getAllExpensesForUser(long chatId) {
        sendMessage(chatId, expenseService.findAllExpenseByChatId(chatId), keyBoard.startExpenseKeyboard(), true);
    }

    private void getAllExpensesForToDayForUser(long chatId) {
        sendMessage(chatId, expenseService.findExpenseForToDayByChatId(chatId), keyBoard.startExpenseKeyboard(), true);
    }

    private void putCategory(long chatId, String messageText) {
        categoryService.addCategory(chatId, messageText);
        sendMessage(chatId, "Категория " + messageText + " добавлена", keyBoard.backToStartAndCategoryMenuKeyboard());
        sendMessage(chatId, "Полный список Ваших категорий. \n" +
                categoryService.getAllCategoryForUser(chatId), keyBoard.backToStartAndCategoryMenuKeyboard(), true);
        statusMessageMap.remove(chatId);
    }

    private void addCategory(long chatId, String messageText) {
        if (StatusMessage.WAITING_CATEGORY.equals(statusMessageMap.get(chatId))) {
            putCategory(chatId, messageText);
        } else {
            sendMessage(chatId, "Введите название категории", keyBoard.backToStartAndCategoryMenuKeyboard());
            statusMessageMap.put(chatId, StatusMessage.WAITING_CATEGORY);
        }
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
        sendMessage(categoryMessageComposer.getAllCategoryForUser(chatId));
    }

    private void startCommandReceived(long chatId, String name) {
        if (userService.getUserByChatId(chatId).getUserName() == null) {
            User user = userService.addUser(userMapper.chatIdAndNameToUser(chatId, name));
            System.out.println(user + " успешно добавлен в бд");
            sendMessage(chatId, "\uD83D\uDC4B Привет " + name + ", приятно познакомиться",
                    keyBoard.startKeyboard());
            sendMessage(chatId, INFO, keyBoard.startKeyboard());
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

    private void sendMessage(long chatId, String textToSend, boolean setParseModeHtml) {
        SendMessage sendMessage = new SendMessage(String.valueOf(chatId), textToSend);
        sendMessage.setParseMode("HTML");
        log.info(statusMessageMap.toString());
        try {
            telegramClient.execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
            log.error(e.getMessage());
        }
    }

    private void sendMessage(long chatId, String textToSend, ReplyKeyboardMarkup replyKeyboardMarkup,
                             boolean setParseMode) {
        SendMessage sendMessage = new SendMessage(String.valueOf(chatId), textToSend);
        sendMessage.setReplyMarkup(replyKeyboardMarkup);
        if (setParseMode) {
            sendMessage.setParseMode("HTML");
        }
        log.info(statusMessageMap.toString());
        try {
            telegramClient.execute(sendMessage);
        } catch (TelegramApiException e) {
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

    private void sendMessage(MessageObj messageObj) {
        SendMessage sendMessage = new SendMessage(String.valueOf(messageObj.getChatId()), messageObj.getTextToSend());
        sendMessage.setReplyMarkup(messageObj.getKeyBoard());
        if (messageObj.isSetParseMode()) {
            sendMessage.setParseMode("HTML");
        }
        log.info(statusMessageMap.toString());
        try {
            telegramClient.execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
        }
    }

}

