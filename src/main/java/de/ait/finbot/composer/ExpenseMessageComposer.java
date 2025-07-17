package de.ait.finbot.composer;


import de.ait.finbot.config.*;
import de.ait.finbot.mapper.ExpenseMapper;
import de.ait.finbot.model.Expense;
import de.ait.finbot.model.MessageObj;
import de.ait.finbot.service.CategoryService;
import de.ait.finbot.service.ExpenseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class ExpenseMessageComposer {
    private final CategoryService categoryService;
    private final KeyBoard keyBoard;
    private final StatusMessageMap statusMessageMap;
    private final CategoryMap categoryMap;
    private final ExpenseService expenseService;
    private final ExpenseMapper expenseMapper;
    private final ExpenseMap expenseMap;

    public MessageObj addExpense(long chatId) {
        statusMessageMap.put(chatId, StatusMessage.WAITING_EXPENSE);
        log.info("addExpense, chatId - {}", chatId);
        return new MessageObj (chatId, "Введите сумму и примечание, чтобы я " +
                "мог определить в какую категорию сохранить трату\n" +
                "Например: 250 еда или 500 одежда",
                keyBoard.startExpenseKeyboard(), true);
    }

    public MessageObj putExpense(long chatId, String messageText) {
        Expense expense = expenseService.addExpense(expenseMapper.chatIdAndNoteToExpense(chatId, messageText));
        String expenseAmount = String.valueOf(expense.getAmount());
        String expenseName = expense.getNote();
        String nameCategory = expense.getCategory().getName();
        log.info("Расход добавлен - {}, chatId - {}", expense, chatId);
        statusMessageMap.remove(chatId);
        return new MessageObj(chatId, "Расход добавлен" + "\n" + "Сумма: "
                + expenseAmount + "\n" +
                "Название: " + expenseName + "\n"
                + "Категория: " + nameCategory, keyBoard.startExpenseKeyboard(), true);
    }

    public MessageObj getAllExpensesFor7DayForUser(long chatId) {
        log.info("Расходы за 7 дней для chatId - {}", chatId);
        return new MessageObj(chatId, expenseService.findExpenseFor7DayByChatId(chatId),
                keyBoard.startExpenseKeyboard(), true);
    }

    public MessageObj getAllExpensesForUser(long chatId) {
        log.info("Все расходы для chatId - {}", chatId);
        return new MessageObj(chatId, expenseService.findAllExpenseByChatId(chatId),
                keyBoard.startExpenseKeyboard(), true);
    }

    public MessageObj getAllExpensesForToDayForUser(long chatId) {
        log.info("Все расходы за сегодня для chatId - {}", chatId);
        return new MessageObj(chatId, expenseService.findExpenseForToDayByChatId(chatId),
                keyBoard.startExpenseKeyboard(), true);
    }

    public MessageObj deleteExpenseByChatId(long chatId) {
        try {
            Expense expense = expenseMap.get(chatId);
            expenseService.removeExpenseById(chatId, expense.getId());
            statusMessageMap.remove(chatId);
            expenseMap.remove(chatId);
            log.info("deleteExpenseByChatId: расход - {} для chatId - {} удален", expense, chatId);
            return new MessageObj(chatId, "Расход " + "<b>" + expense.getNote() + "</b>" + " c ID " + expense.getId() + "<b> успешно удален</b>", keyBoard.startKeyboard(), true);

        } catch (NumberFormatException e) {
            log.info("deleteExpenseByChatId: Ошибка - расход для chatId - {} не удален", chatId);
            return new MessageObj (chatId, "Расход не удален." +
                    "Вернитесь в главное меню и повторите попытку", keyBoard.backToStartAndExpenseMenuKeyboard(), true);

        }
    }

    public MessageObj deleteExpenseById(long chatId, String idExpenseString) {
        Long idExpense = 0L;
        try {
            idExpense = Long.valueOf(idExpenseString);
            log.info("deleteExpenseById: chatId - {}, idExpense - {}", chatId, idExpense);
            Expense expense = expenseService.removeExpenseById(chatId, idExpense);
            statusMessageMap.remove(chatId);
            return new MessageObj(chatId, "Расход " + "<b>" + expense.getNote() +
                    "</b>" + " c ID " + expense.getId() + "<b> успешно удален</b>",
                    keyBoard.startKeyboard(), true);

        } catch (NumberFormatException e) {
            log.info("deleteExpenseById: невозможно преобразовать ID - {} в цифры, chatId - {}", idExpenseString, chatId);
            return new MessageObj(chatId, "Передан некорректный ID " + idExpenseString +
                            ". Вводите только цифры, например 15. Проверьте правильность введения и повторите попытку",
                    keyBoard.backToStartAndExpenseMenuKeyboard(), true);
        } catch (Exception e) {
            log.info("deleteExpenseById: расход c ID - {} для chatId - {} не найден", idExpenseString, chatId);
            return new MessageObj(chatId, "Расход с ID " + idExpenseString +
                            " не найден. Попробуйте ввести другой ID!", keyBoard.backToStartAndExpenseMenuKeyboard(),
                    true);

        }
    }


    public MessageObj waitingIDForExpenseToDelete(long chatId, String messageText) {

        if (StatusMessage.WAITING_ID_TO_DELETE.equals(statusMessageMap.get(chatId))) {
            log.info("Удалить по id блок if, chatId - {}", chatId);
            return deleteExpenseById(chatId, messageText);
        } else {
            statusMessageMap.put(chatId, StatusMessage.WAITING_ID_TO_DELETE);
            log.info("Удалить по id блок else, chatId - {}", chatId);
            return new MessageObj(chatId, "Введите ID расхода для удаления (только цифры), например 24",
                    keyBoard.backToStartAndExpenseMenuKeyboard(), true);

        }
    }

}
