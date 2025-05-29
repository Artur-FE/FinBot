package de.ait.finbot.model;

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
        row.add("Найти расход");
        row.add("Редактировать расходы");
        keyboardRowList.add(row);
        row = new KeyboardRow();
        row.add("Список категорий");
        row.add("Информация о боте");
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
        row.add("Расходы за сегодня");
        row.add("Расходы за 7 дней");
        keyboardRowList.add(row);
        row = new KeyboardRow();
        row.add("Все мои расходы");
        row.add("Добавить расход");
        keyboardRowList.add(row);
        row = new KeyboardRow();
        row.add("Редактировать расходы");
        row.add("Главное меню");
        keyboardRowList.add(row);
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup(keyboardRowList);
        replyKeyboardMarkup.setResizeKeyboard(true);
        return replyKeyboardMarkup;
    }

    public ReplyKeyboardMarkup editExpenseKeyboard(){
        List<KeyboardRow> keyboardRowList = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        row.add("Редактировать по имени");
        row.add("Редактировать по ID");
        keyboardRowList.add(row);
        row = new KeyboardRow();
        row.add("Удалить по имени");
        row.add("Удалить по ID");
        keyboardRowList.add(row);
        row = new KeyboardRow();
        row.add("Удалить все расходы");
        row.add("Вернуться в главное меню");
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

    public ReplyKeyboardMarkup deleteExpenseKeyboard(){
        List<KeyboardRow> keyboardRowList = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        row.add("Подтверждаю удаление расхода");
        keyboardRowList.add(row);
        row = new KeyboardRow();
        row.add("Отменить и выйти в главное меню");
        keyboardRowList.add(row);
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup(keyboardRowList);
        replyKeyboardMarkup.setResizeKeyboard(true);
        return replyKeyboardMarkup;
    }

    public ReplyKeyboardMarkup backToStartAndExpenseMenuKeyboard(){
        List<KeyboardRow> keyboardRowList = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        row.add("Мои расходы");
        keyboardRowList.add(row);
        row = new KeyboardRow();
        row.add("Вернуться в главное меню");
        keyboardRowList.add(row);
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup(keyboardRowList);
        replyKeyboardMarkup.setResizeKeyboard(true);
        return replyKeyboardMarkup;
    }

    public ReplyKeyboardMarkup searchExpenseKeyboard(){
        List<KeyboardRow> keyboardRowList = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        row.add("Найти по имени");
        row.add("Найти по ID");
        keyboardRowList.add(row);
        row = new KeyboardRow();
        row.add("Вернуться в главное меню");
        keyboardRowList.add(row);
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup(keyboardRowList);
        replyKeyboardMarkup.setResizeKeyboard(true);
        return replyKeyboardMarkup;
    }

    public ReplyKeyboardMarkup deleteAllExpenseMenuKeyboard(){
        List<KeyboardRow> keyboardRowList = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        row.add("Уверен! Удалить все расходы!");
        keyboardRowList.add(row);
        row = new KeyboardRow();
        row.add("Отменить и выйти в главное меню");
        keyboardRowList.add(row);
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup(keyboardRowList);
        replyKeyboardMarkup.setResizeKeyboard(true);
        return replyKeyboardMarkup;
    }
}
