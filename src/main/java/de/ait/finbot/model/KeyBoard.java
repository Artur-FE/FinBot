package de.ait.finbot.model;

import de.ait.finbot.config.IncomingMessage;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

@Component
public class KeyBoard {

    public ReplyKeyboardMarkup startKeyboard(){
        List<KeyboardRow> keyboardRowList = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        row.add(IncomingMessage.MY_EXPENSES.getDescription());
        row.add(IncomingMessage.ADD_EXPENSE.getDescription());
        keyboardRowList.add(row);
        row = new KeyboardRow();
        row.add(IncomingMessage.FIND_EXPENSE.getDescription());
        row.add(IncomingMessage.EDIT_EXPENSES.getDescription());
        keyboardRowList.add(row);
        row = new KeyboardRow();
        row.add(IncomingMessage.CATEGORY_LIST.getDescription());
        row.add(IncomingMessage.BOT_INFO.getDescription());
        keyboardRowList.add(row);
        row = new KeyboardRow();
        keyboardRowList.add(row);
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup(keyboardRowList);
        replyKeyboardMarkup.setResizeKeyboard(true);
        return replyKeyboardMarkup;
    }

    public ReplyKeyboardMarkup startExpenseKeyboard(){
        List<KeyboardRow> keyboardRowList = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        row.add(IncomingMessage.EXPENSES_TODAY.getDescription());
        row.add(IncomingMessage.EXPENSES_IN_7_DAYS.getDescription());
        keyboardRowList.add(row);
        row = new KeyboardRow();
        row.add(IncomingMessage.ALL_MY_EXPENSES.getDescription());
        row.add(IncomingMessage.ADD_EXPENSE.getDescription());
        keyboardRowList.add(row);
        row = new KeyboardRow();
        row.add(IncomingMessage.EDIT_EXPENSES.getDescription());
        row.add(IncomingMessage.MAIN_MENU.getDescription());
        keyboardRowList.add(row);
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup(keyboardRowList);
        replyKeyboardMarkup.setResizeKeyboard(true);
        return replyKeyboardMarkup;
    }

    public ReplyKeyboardMarkup editExpenseKeyboard(){
        List<KeyboardRow> keyboardRowList = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        row.add(IncomingMessage.EDIT_BY_NAME.getDescription());
        row.add(IncomingMessage.EDIT_BY_ID.getDescription());
        keyboardRowList.add(row);
        row = new KeyboardRow();
        row.add(IncomingMessage.DELETE_BY_NAME.getDescription());
        row.add(IncomingMessage.DELETE_BY_ID.getDescription());
        keyboardRowList.add(row);
        row = new KeyboardRow();
        row.add(IncomingMessage.DELETE_ALL_EXPENSES.getDescription());
        row.add(IncomingMessage.BACK_TO_MAIN_MENU.getDescription());
        keyboardRowList.add(row);
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup(keyboardRowList);
        replyKeyboardMarkup.setResizeKeyboard(true);
        return replyKeyboardMarkup;
    }

    public ReplyKeyboardMarkup editExpenseByIdKeyboard(){
        List<KeyboardRow> keyboardRowList = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        row.add(IncomingMessage.EDIT_NAME.getDescription());
        row.add(IncomingMessage.EDIT_AMOUNT.getDescription());
        keyboardRowList.add(row);
        row = new KeyboardRow();
        row.add(IncomingMessage.EDIT_CATEGORY.getDescription());
        row.add(IncomingMessage.EDIT_DATE.getDescription());
        keyboardRowList.add(row);
        row = new KeyboardRow();
        row.add(IncomingMessage.DELETE_EXPENSE.getDescription());
        row.add(IncomingMessage.TO_MAIN_MENU.getDescription());
        keyboardRowList.add(row);
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup(keyboardRowList);
        replyKeyboardMarkup.setResizeKeyboard(true);
        return replyKeyboardMarkup;
    }

    public ReplyKeyboardMarkup deleteExpenseKeyboard(){
        List<KeyboardRow> keyboardRowList = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        row.add(IncomingMessage.ACCEPT_DELETE_EXPENSE.getDescription());
        keyboardRowList.add(row);
        row = new KeyboardRow();
        row.add(IncomingMessage.CANCEL_AND_EXIT_TO_THE_MAIN_MENU.getDescription());
        keyboardRowList.add(row);
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup(keyboardRowList);
        replyKeyboardMarkup.setResizeKeyboard(true);
        return replyKeyboardMarkup;
    }

    public ReplyKeyboardMarkup backToStartAndExpenseMenuKeyboard(){
        List<KeyboardRow> keyboardRowList = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        row.add(IncomingMessage.MY_EXPENSES.getDescription());
        keyboardRowList.add(row);
        row = new KeyboardRow();
        row.add(IncomingMessage.BACK_TO_MAIN_MENU.getDescription());
        keyboardRowList.add(row);
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup(keyboardRowList);
        replyKeyboardMarkup.setResizeKeyboard(true);
        return replyKeyboardMarkup;
    }

    public ReplyKeyboardMarkup backToStartAndCategoryMenuKeyboard(){
        List<KeyboardRow> keyboardRowList = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        row.add(IncomingMessage.CATEGORY_LIST.getDescription());
        keyboardRowList.add(row);
        row = new KeyboardRow();
        row.add(IncomingMessage.BACK_TO_MAIN_MENU.getDescription());
        keyboardRowList.add(row);
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup(keyboardRowList);
        replyKeyboardMarkup.setResizeKeyboard(true);
        return replyKeyboardMarkup;
    }

    public ReplyKeyboardMarkup searchExpenseKeyboard(){
        List<KeyboardRow> keyboardRowList = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        row.add(IncomingMessage.FIND_BY_NAME.getDescription());
        row.add(IncomingMessage.FIND_BY_ID.getDescription());
        keyboardRowList.add(row);
        row = new KeyboardRow();
        row.add(IncomingMessage.BACK_TO_MAIN_MENU.getDescription());
        keyboardRowList.add(row);
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup(keyboardRowList);
        replyKeyboardMarkup.setResizeKeyboard(true);
        return replyKeyboardMarkup;
    }

    public ReplyKeyboardMarkup deleteAllExpenseMenuKeyboard(){
        List<KeyboardRow> keyboardRowList = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        row.add(IncomingMessage.SURE_DELETE_ALL_EXPENSES.getDescription());
        keyboardRowList.add(row);
        row = new KeyboardRow();
        row.add(IncomingMessage.CANCEL_AND_EXIT_TO_THE_MAIN_MENU.getDescription());
        keyboardRowList.add(row);
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup(keyboardRowList);
        replyKeyboardMarkup.setResizeKeyboard(true);
        return replyKeyboardMarkup;
    }

    public ReplyKeyboardMarkup categoryMenuKeyboard(){
        List<KeyboardRow> keyboardRowList = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        row.add(IncomingMessage.ADD_CATEGORY.getDescription());
        row.add(IncomingMessage.EDIT_CATEGORIES.getDescription());
        keyboardRowList.add(row);
        row = new KeyboardRow();
        row.add(IncomingMessage.DELETE_CATEGORY.getDescription());
        row.add(IncomingMessage.BACK_TO_MAIN_MENU.getDescription());
        keyboardRowList.add(row);
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup(keyboardRowList);
        replyKeyboardMarkup.setResizeKeyboard(true);
        return replyKeyboardMarkup;
    }
}
