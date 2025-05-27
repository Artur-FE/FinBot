package de.ait.finbot.model;

import org.springframework.context.annotation.Bean;
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
        row.add("Мои расходы");
        row.add("Добавить расход");
        keyboardRowList.add(row);
        row = new KeyboardRow();
        row.add("Информация о боте");
        keyboardRowList.add(row);
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup(keyboardRowList);
        replyKeyboardMarkup.setResizeKeyboard(true);
        return replyKeyboardMarkup;
    }

    public ReplyKeyboardMarkup startExpenseKeyboard(){
        List<KeyboardRow> keyboardRowList = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        row.add("Мои расходы за сегодня");
        keyboardRowList.add(row);
        row = new KeyboardRow();
        row.add("Мои расходы за последние 7 дней");
        keyboardRowList.add(row);
        row = new KeyboardRow();
        row.add("Все мои расходы");
        row.add("Добавить расход");
        keyboardRowList.add(row);
        row = new KeyboardRow();
        row.add("Редактировать расходы");
        keyboardRowList.add(row);
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup(keyboardRowList);
        replyKeyboardMarkup.setResizeKeyboard(true);
        return replyKeyboardMarkup;
    }

    public ReplyKeyboardMarkup editExpenseKeyboard(){
        List<KeyboardRow> keyboardRowList = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        row.add("Редактировать расход по имени");
        keyboardRowList.add(row);
        row = new KeyboardRow();
        row.add("Редактировать расход по ID");
        keyboardRowList.add(row);
        row = new KeyboardRow();
        row.add("Удалить расход по имени");
        keyboardRowList.add(row);
        row = new KeyboardRow();
        row.add("Удалить по ID");
        row.add("Удалить все расходы");
        keyboardRowList.add(row);
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup(keyboardRowList);
        replyKeyboardMarkup.setResizeKeyboard(true);
        return replyKeyboardMarkup;
    }

    public ReplyKeyboardMarkup editExpenseByIdKeyboard(){
        List<KeyboardRow> keyboardRowList = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        row.add("Изменить название");
        row.add("Изменить сумму");
        keyboardRowList.add(row);
        row = new KeyboardRow();
        row.add("Изменить категорию");
        row.add("Изменить дату");
        keyboardRowList.add(row);
        row = new KeyboardRow();
        row.add("Удалить расход");
        row.add("В главное меню");
        keyboardRowList.add(row);
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup(keyboardRowList);
        replyKeyboardMarkup.setResizeKeyboard(true);
        return replyKeyboardMarkup;
    }
}
