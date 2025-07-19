package de.ait.finbot.config;

import de.ait.finbot.composer.CategoryMessageComposer;
import de.ait.finbot.composer.ExpenseMessageComposer;
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
    private final CategoryMessageComposer categoryMessageComposer;
    private final ExpenseMessageComposer expenseMessageComposer;
    private final StatusMessageMap statusMessageMap;
    private final ExpenseMap expenseMap;
    private final CategoryMap categoryMap;
    public String info;
    public TelegramBotHandler(CategoryService categoryService, ExpenseService expenseService, UserServiceImpl userService, UserMapper userMapper, ExpenseMapper expenseMapper, @Value("${bot.token}") String token, ExpenseMessageComposer expenseMessageComposer, @Value("${bot.info.message}") String info, KeyBoard keyBoard, CategoryMessageComposer categoryCommand, StatusMessageMap statusMessageMap, ExpenseMap expenseMap, CategoryMap categoryMap) {
        this.categoryService = categoryService;
        this.expenseService = expenseService;
        this.userService = userService;
        this.userMapper = userMapper;
        this.expenseMapper = expenseMapper;
        this.token = token;
        this.expenseMessageComposer = expenseMessageComposer;
        this.info = info;
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
                sendMessage(chatId, info, keyBoard.startKeyboard());
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
                log.error("Ошибка! Команда не распознана! chatId - {}", chatId);
            }
        }
//        log.info(String.valueOf(statusMessageMap));
//        log.info(String.valueOf(categoryMap));
//        log.info(String.valueOf(expenseMap));
    }

    private void editCategoryName(long chatId, String messageText) {
        log.info("editCategoryName: chatId - {}, messageText - {}", chatId, messageText);
       sendMessage(categoryMessageComposer.editCategoryName(chatId, messageText));
    }

    private void putCategoryName(long chatId, String messageText) {
        log.info("putCategoryName: chatId - {}, messageText - {}", chatId, messageText);
        sendMessage(categoryMessageComposer.putCategoryName(chatId, messageText));
        getAllCategoryForUser(chatId);
    }

    private void deleteCategory(long chatId, String messageText) {
        log.info("класс TelegramBotHandler, метод deleteCategory, chatId - {}, messageText - {}",
                chatId, messageText);
            sendMessage(categoryMessageComposer.deleteCategory(chatId, messageText));
    }

    private void deleteAllExpenseByUser(long chatId, String messageText) {
        sendMessage(expenseMessageComposer.deleteAllExpenseByUser(chatId, messageText));
    }

    private void searchExpense(long chatId) {
    sendMessage(expenseMessageComposer.searchExpense(chatId));
    }

    private void waitingNameForExpenseToDelete(long chatId, String messageText) {
       sendMessage(expenseMessageComposer.waitingNameForExpenseToDelete(chatId, messageText));
    }

    private void waitingNameForExpenseToEdit(long chatId, String messageText) {
       sendMessage(expenseMessageComposer.waitingNameForExpenseToEdit(chatId, messageText));
    }

    private void putNewCategoryExpenseById(long chatId, String categoryId) {
        sendMessage(expenseMessageComposer.putNewCategoryExpenseById(chatId, categoryId));
    }

    private void editCategoryExpenseById(long chatId) {
       sendMessage(expenseMessageComposer.editCategoryExpenseById(chatId));
    }

    private void putNewDateExpenseById(long chatId, String newDate) {
        sendMessage(expenseMessageComposer.putNewDateExpenseById(chatId, newDate));
    }

    private void editDateExpenseById(long chatId) {
       sendMessage(expenseMessageComposer.editDateExpenseById(chatId));
    }

    private void putNewAmountExpenseById(long chatId, String newAmountExpense) {
       sendMessage(expenseMessageComposer.putNewAmountExpenseById(chatId, newAmountExpense));
    }

    private void editAmountExpenseById(long chatId) {
     sendMessage(expenseMessageComposer.editAmountExpenseById(chatId));
    }

    private void putNewNameExpenseById(long chatId, String newNameExpense) {
        sendMessage(expenseMessageComposer.putNewNameExpenseById(chatId, newNameExpense));
    }

    private void editNameExpenseById(long chatId) {
        sendMessage(expenseMessageComposer.editNameExpenseById(chatId));
    }

    private void waitingIDForExpenseToEdit(long chatId, String messageText) {
        sendMessage(expenseMessageComposer.waitingIDForExpenseToEdit(chatId,messageText));
    }


    private void deleteExpenseByChatId(long chatId) {
        sendMessage(expenseMessageComposer.deleteExpenseByChatId(chatId));
    }


private void waitingIDForExpenseToDelete(long chatId, String messageText) {
        sendMessage(expenseMessageComposer.waitingIDForExpenseToDelete(chatId,messageText));
}

    private void getAllExpensesFor7DayForUser(long chatId) {
        sendMessage(expenseMessageComposer.getAllExpensesFor7DayForUser(chatId));
    }

    private void getAllExpensesForUser(long chatId) {
        sendMessage(expenseMessageComposer.getAllExpensesForUser(chatId));
    }

    private void getAllExpensesForToDayForUser(long chatId) {
        sendMessage(expenseMessageComposer.getAllExpensesForToDayForUser(chatId));
    }

    private void addCategory(long chatId, String messageText) {
        log.info("addCategory, chatId - {}, messageText - {}", chatId, messageText);
       sendMessage(categoryMessageComposer.addCategory(chatId, messageText));
    }


    private void putExpense(long chatId, String messageText){
        sendMessage(expenseMessageComposer.putExpense(chatId, messageText));
    }

    private void addExpense(long chatId) {
        sendMessage(expenseMessageComposer.addExpense(chatId));
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
            sendMessage(chatId, info, keyBoard.startKeyboard());
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

