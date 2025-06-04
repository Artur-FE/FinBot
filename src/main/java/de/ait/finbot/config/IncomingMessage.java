package de.ait.finbot.config;

import lombok.Getter;

@Getter
public enum IncomingMessage {
    BACK_TO_MAIN_MENU("Вернуться в главное меню"),
    TO_MAIN_MENU("В главное меню"),
    MAIN_MENU("Главное меню"),
    CANCEL_AND_EXIT_TO_THE_MAIN_MENU("Отменить и выйти в главное меню"),
    CATEGORY_LIST("Мои категории"),
    MY_EXPENSES("Мои расходы"),
    EXPENSES_TODAY("Расходы за сегодня"),
    EXPENSES_IN_7_DAYS("Расходы за 7 дней"),
    ALL_MY_EXPENSES("Все мои расходы"),
    FIND_EXPENSE("Найти расход"),
    ADD_EXPENSE("Добавить расход"),
    ADD_CATEGORY("Добавить категорию"),
    EDIT_EXPENSES("Редактировать расходы"),
    DELETE_BY_ID("Удалить по ID"),
    DELETE_ALL_EXPENSES("Удалить все расходы"),
    SURE_DELETE_ALL_EXPENSES("Уверен! Удалить все расходы!"),
    EDIT_BY_ID("Редактировать по ID"),
    FIND_BY_ID("Найти по ID"),
    EDIT_BY_NAME("Редактировать по имени"),
    FIND_BY_NAME("Найти по имени"),
    DELETE_BY_NAME("Удалить по имени"),
    EDIT_NAME("Изменить название"),
    EDIT_AMOUNT("Изменить сумму"),
    EDIT_CATEGORY("Изменить категорию"),
    EDIT_DATE("Изменить дату"),
    DELETE_EXPENSE("Удалить расход"),
    ACCEPT_DELETE_EXPENSE("Подтверждаю удаление расхода"),
    EDIT_CATEGORIES("Редактировить категории"),
    DELETE_CATEGORY("Удалить категорию"),
    BOT_INFO("Информация о боте");

    private String description;
    IncomingMessage(String description) {
        this.description = description;
    }


}
