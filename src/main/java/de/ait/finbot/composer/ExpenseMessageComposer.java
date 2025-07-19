package de.ait.finbot.composer;


import de.ait.finbot.config.*;
import de.ait.finbot.mapper.ExpenseMapper;
import de.ait.finbot.model.Category;
import de.ait.finbot.model.Expense;
import de.ait.finbot.model.MessageObj;
import de.ait.finbot.model.User;
import de.ait.finbot.service.CategoryService;
import de.ait.finbot.service.ExpenseService;
import de.ait.finbot.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

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
    private final UserService userService;

    public MessageObj addExpense(long chatId) {
        statusMessageMap.put(chatId, StatusMessage.WAITING_EXPENSE);
        log.info("addExpense, chatId - {}", chatId);
        return new MessageObj(chatId, "Введите сумму и примечание, чтобы я " +
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
            return new MessageObj(chatId, "Расход не удален." +
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

    public MessageObj editExpenseById(long chatId, String idExpenseString) {
        Long idExpense = null;
        try {
            idExpense = Long.valueOf(idExpenseString);
            User user = userService.getUserByChatId(chatId);
            Expense expense = expenseService.findExpenseById(chatId, idExpense);
            expenseMap.put(chatId, expense);
            statusMessageMap.put(chatId, StatusMessage.WAITING_WHAT_EXPENSE_TO_EDIT);
            log.info("editExpenseById: Найден расход: {}, chatId - {}", expense, chatId);
            return new MessageObj(chatId, "Найден расход: \n" +
                    expenseMapper.expenseToExpenseStringAllField(expense),
                    keyBoard.editExpenseByIdKeyboard(), true);


        } catch (NumberFormatException e) {
            log.error("editExpenseById: Ошибка NumberFormatException! Передан некорректный ID - {} для chatId - {}", idExpenseString, chatId);
            return new MessageObj(chatId, "Передан некорректный ID. " + idExpenseString +
                    "Вводите только цифры, например 15. Попробуйте еще раз!",
                    keyBoard.backToStartAndExpenseMenuKeyboard(), true);
        } catch (Exception e) {
            log.error("editExpenseById: Ошибка Exception! Расход с ID - {} для chatId - {} не найден", idExpenseString, chatId);
            return new MessageObj(chatId, "Расход с ID: <b>" + idExpenseString + " </b> не найден. " +
                    "Проверьте правильность введения и повторите попытку",
                    keyBoard.backToStartAndExpenseMenuKeyboard(), true);
        }
    }

    public MessageObj waitingIDForExpenseToEdit(long chatId, String messageText) {
        if (StatusMessage.WAITING_ID_TO_EDIT.equals(statusMessageMap.get(chatId))) {
            log.info("waitingIDForExpenseToEdit: блок if, chatId - {}, messageText - {}", chatId, messageText);
            return editExpenseById(chatId, messageText);

        } else {
            log.info("waitingIDForExpenseToEdit: блок else, chatId - {}, messageText - {}",
                    chatId, messageText);
            statusMessageMap.put(chatId, StatusMessage.WAITING_ID_TO_EDIT);
            return new MessageObj(chatId, "Введите ID расхода для редактирования",
                    keyBoard.backToStartAndExpenseMenuKeyboard(), true);

        }

    }

    public MessageObj editNameExpenseById(long chatId) {
        log.info("editNameExpenseById: chatId - {}", chatId);
        statusMessageMap.put(chatId, StatusMessage.PUT_NEW_NAME_EXPENSE);
        return new MessageObj(chatId, "Введите новое название расхода",
                keyBoard.backToStartAndExpenseMenuKeyboard(), true);
    }

    public MessageObj putNewNameExpenseById(long chatId, String newNameExpense) {
        Expense expense = expenseMap.get(chatId);
        expense.setNote(newNameExpense);
        expenseService.addExpense(expense);
        expenseMap.remove(chatId);
        statusMessageMap.remove(chatId);
        log.info("putNewNameExpenseById: Имя расхода успешно изменено - {}, chatId - {}", newNameExpense, chatId);
        return new MessageObj(chatId, "Имя расхода успешно изменено!"
                + "\n" + expenseMapper.expenseToExpenseStringAllField(expense),
                keyBoard.startKeyboard(), true);

    }

    public MessageObj editAmountExpenseById(long chatId) {
        statusMessageMap.put(chatId, StatusMessage.PUT_NEW_AMOUNT_EXPENSE);
        log.info("editAmountExpenseById: chatId - {}", chatId);
        return new MessageObj(chatId, "Введите новую сумму расхода. " +
                "Допустимы только цифры и точка или запятая. Например 120 или 76.58",
                keyBoard.backToStartAndExpenseMenuKeyboard(), true);

    }

    public MessageObj putNewAmountExpenseById(long chatId, String newAmountExpense) {
        Expense expense = expenseMap.get(chatId);
        try {
            BigDecimal newAmountBigdecimal = new BigDecimal(newAmountExpense);
            expense.setAmount(newAmountBigdecimal);
            expenseService.addExpense(expense);
            expenseMap.remove(chatId);
            statusMessageMap.remove(chatId);
            return new MessageObj(chatId, "Сумма расхода успешно изменена!" + "\n" +
                    expenseMapper.expenseToExpenseStringAllField(expense),
                    keyBoard.startKeyboard(), true);

        } catch (NumberFormatException e) {
            log.error("putNewAmountExpenseById: Ошибка - {}. Невозможно преобразовать - {} в BigDecimal",
                    e.getMessage(), newAmountExpense);
            return new MessageObj(chatId, "Введен некорретный расход. Проверьте правильность написания, " +
                    "допустимы только цифры и точка или запятая. Например 120 или 76.58. " +
                    "Пожалуйста, повторите попытку", keyBoard.backToStartAndExpenseMenuKeyboard(), true);
        }

    }

    public MessageObj editDateExpenseById(long chatId) {
        statusMessageMap.put(chatId, StatusMessage.PUT_NEW_DATE_EXPENSE);
        log.info("editDateExpenseById: chatId - {}", chatId);
        return new MessageObj(chatId, "Введите новую дату расхода. " +
                "Формат день.месяц.год, например 27.05.2025 или 27052025",
                keyBoard.backToStartAndExpenseMenuKeyboard(), true);
    }

    public MessageObj putNewDateExpenseById(long chatId, String newDate) {
        Expense expense = expenseMap.get(chatId);
        try {
            String date = newDate.replaceAll("[^0-9]", "");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMyyyy");
            LocalDateTime localDateTime = LocalDate.parse(date, formatter).atStartOfDay();
            expense.setCreatedAt(localDateTime);
            expenseService.addExpense(expense);
            expenseMap.remove(chatId);
            statusMessageMap.remove(chatId);
            log.info("putNewDateExpenseById: Дата расхода успешно изменена на - {}, chatId -  {}",
                    localDateTime, chatId);
            return new MessageObj(chatId, "Дата расхода успешно изменена!" + "\n" +
                    expenseMapper.expenseToExpenseStringAllField(expense),
                    keyBoard.backToStartAndExpenseMenuKeyboard(), true);

        } catch (Exception e) {
            log.error("putNewDateExpenseById: Ошибка - {}. Новая дата некорректна {}", e.getMessage(), newDate);
            return new MessageObj(chatId, "Ошибка. Новая дата некорректная дата  " + newDate +
                    ". Пожалуйста, повторите попытку", keyBoard.backToStartAndExpenseMenuKeyboard(), true);
        }
    }

    public MessageObj editCategoryExpenseById(long chatId) {
        statusMessageMap.put(chatId, StatusMessage.PUT_NEW_CATEGORY_EXPENSE);
        log.info("editCategoryExpenseById: chatId - {}", chatId);
        return new MessageObj(chatId, "Введите ID категории, которую хотите присвоить вашему расходу. " +
                "\nНиже представлен список Ваших категорий с указанием ID" +
                "\n" + categoryService.getAllCategoryForUser(chatId),
                keyBoard.backToStartAndExpenseMenuKeyboard(), true);
    }

    public MessageObj putNewCategoryExpenseById(long chatId, String categoryId) {
        Expense expense = null;
        // Long userId = 0L;
        try {
            expense = expenseMap.get(chatId);
            Category category = categoryService.getCategoryById(Long.valueOf(categoryId));
            expense.setCategory(category);
            expenseService.addExpense(expense);
            expenseMap.remove(chatId);
            statusMessageMap.remove(chatId);
            log.info("putNewCategoryExpenseById: Категория изменена успешно на - {}", expense.getCategory());
            return new MessageObj(chatId, "Категория изменена успешно!\n" +
                    expenseMapper.expenseToExpenseStringAllField(expense),
                    keyBoard.backToStartAndExpenseMenuKeyboard(), true);

        } catch (NumberFormatException e) {
            log.error("putNewCategoryExpenseById: Ошибка! ID - {} невозможно преобразовать в Long", categoryId);
            return new MessageObj(chatId, "Ошибка! Введен некорретный ID <b>" + categoryId +
                    "</b>. Допустимы только цифры. Проверьте правильность написания",
                    keyBoard.backToStartAndExpenseMenuKeyboard(), true);
        } catch (Exception e) {
            log.error("putNewCategoryExpenseById: Ошибка! ID - {} не найден для chatId - {}", categoryId, chatId);
            return new MessageObj(chatId, "Введенная категория с ID \" + categoryId +\n" +
                    "                    \" не найдена. Введите ID категории из Вашего списка категорий ниже. " +
                    "\nНиже представлен список Ваших категорий с указанием ID" +
                    "\n" + categoryService.getAllCategoryForUser(chatId),
                    keyBoard.backToStartAndExpenseMenuKeyboard(), true);
        }
    }

    public MessageObj waitingNameForExpenseToEdit(long chatId, String messageText) {
        if (StatusMessage.WAITING_NAME_TO_EDIT.equals(statusMessageMap.get(chatId))) {
            log.info("waitingNameForExpenseToEdit: блок if, chatId - {}, messageText - {}", chatId, messageText);
            return findExpenseByName(chatId, messageText);
        } else {
            statusMessageMap.put(chatId, StatusMessage.WAITING_NAME_TO_EDIT);
            log.info("waitingNameForExpenseToEdit: блок else, chatId - {}, messageText - {}", chatId, messageText);
            return new MessageObj(chatId, "Введите имя расхода для редактирования. " +
                    "Вы получите список из расходов по введенному имени с указанием ID для дальнейшего редактирования",
                    keyBoard.backToStartAndExpenseMenuKeyboard(), true);
        }
    }

    public MessageObj findExpenseByName(long chatId, String nameExpense) {
        log.info("findExpenseByName: chatId - {}, nameExpense - {}", chatId, nameExpense);
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
            statusMessageMap.put(chatId, StatusMessage.WAITING_WHAT_EXPENSE_TO_EDIT);
            expenseMap.put(chatId, expense);
            return new MessageObj(chatId, "Найден 1 расход с именем <b>" + nameExpense + "</b>\n" +
                    "Выберите из меню ниже действия для дальнейшего редактирования \n\n" +
                    expenseMapper.expenseToExpenseStringAllField(expense), keyBoard.editExpenseByIdKeyboard(), true);
        } else if (!allExpenseByChatId.isBlank()) {
            statusMessageMap.put(chatId, StatusMessage.WAITING_ID_TO_EDIT);
            return new MessageObj(chatId, "Найдено " + listExpense.size() + " расходов с именем " + nameExpense + "\n" +
                    "Введите ID расхода для дальнейшего редактирования \n\n" +
                    allExpenseByChatId, keyBoard.backToStartAndExpenseMenuKeyboard(), true);
        } else {
            return new MessageObj(chatId, "По имени расхода <b>" + nameExpense + "</b> нет результатов. \n" +
                    "Проверьте правильность введения имени расхода и повторите попытку", keyBoard.backToStartAndExpenseMenuKeyboard(), true);
        }
    }

    public MessageObj waitingNameForExpenseToDelete(long chatId, String messageText) {
        if (StatusMessage.WAITING_NAME_TO_DELETE.equals(statusMessageMap.get(chatId))) {
            log.info("waitingNameForExpenseToDelete: блок if, chatId - {}, messageText - {}",
                    chatId, messageText);
           return deleteExpenseByName(chatId, messageText);
        } else {
            log.info("waitingNameForExpenseToDelete: блок else, chatId - {}, messageText - {}",
                    chatId, messageText);
            statusMessageMap.put(chatId, StatusMessage.WAITING_NAME_TO_DELETE);
            return new MessageObj(chatId, "Введите имя расхода для удаления. " +
                    "Вы получите список из расходов по введенному имени с указанием ID " +
                    "для дальнейшего удаления",
                    keyBoard.backToStartAndExpenseMenuKeyboard(), true);
        }
    }

    public MessageObj deleteExpenseByName(long chatId, String nameExpense) {
        log.info("deleteExpenseByName: chatId - {}, nameExpense - {}", chatId, nameExpense);
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
            statusMessageMap.put(chatId, StatusMessage.WAITING_WHAT_EXPENSE_TO_EDIT);
            expenseMap.put(chatId, expense);
            return new MessageObj(chatId, "Найден 1 расход с именем <b>" + nameExpense + "</b>\n" +
                    "Подтвердите в меню ниже процедуру удаления расхода \n\n" +
                    expenseMapper.expenseToExpenseStringAllField(expense), keyBoard.deleteExpenseKeyboard(), true);
        } else if (!allExpenseByChatId.isBlank()) {
            statusMessageMap.put(chatId, StatusMessage.WAITING_ID_TO_DELETE);
            return new MessageObj(chatId, "Найдено " + listExpense.size() + " расходов с именем " + nameExpense + "\n" +
                    "Введите ID расхода для дальнейшего удаления \n\n" +
                    allExpenseByChatId, keyBoard.backToStartAndExpenseMenuKeyboard(), true);
        } else {
            log.error("Ошибка! Расход с именем - {} для chatId - {} не найден", nameExpense, chatId);
            return new MessageObj(chatId, "По имени расхода <b>" + nameExpense + "</b> нет результатов. \n" +
                    "Проверьте правильность введения имени расхода и повторите попытку", keyBoard.editExpenseKeyboard(), true);
        }
    }

    public MessageObj searchExpense(long chatId) {
        statusMessageMap.put(chatId, StatusMessage.WAITING_WHAT_EXPENSE_TO_EDIT);
        log.info("searchExpense: chatId - {}", chatId);
        return new MessageObj(chatId, "Как ты хочешь найти расход, по имени расхода или ID? Выбери в меню ниже",
                keyBoard.searchExpenseKeyboard(), true);

    }

    public MessageObj deleteAllExpenseByUser(long chatId, String messageText) {
        log.info("deleteAllExpenseByUser: chatId- {}, messageText - {}", chatId, messageText);
        if (messageText.equals(IncomingMessage.DELETE_ALL_EXPENSES.getDescription())) {
            return new MessageObj(chatId, "Вы уверенны, что хотите удалить все свои расходы? Действие невозможно восстановить",
                    keyBoard.deleteAllExpenseMenuKeyboard(), true);
        } else if (messageText.equals(IncomingMessage.SURE_DELETE_ALL_EXPENSES.getDescription())) {
            try {
                expenseService.removeAllExpenseByUser(chatId);
                return new MessageObj(chatId, "Удаление успешно! \n" +
                                expenseService.findAllExpenseByChatId(chatId),
                        keyBoard.startKeyboard(), true);
            } catch (Exception e) {
                return new MessageObj(chatId, "Ошибка! Что-то пошло не так! Попробуйте вернуться в главное меню и повторить попытку",
                        keyBoard.backToStartAndExpenseMenuKeyboard(), true);
            }
        }
        else
            return new MessageObj(chatId, "Ошибка! Удаление невозможно",
                    keyBoard.backToStartAndExpenseMenuKeyboard(), true);
    }


}
