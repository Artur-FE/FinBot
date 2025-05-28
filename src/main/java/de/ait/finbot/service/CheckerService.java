package de.ait.finbot.service;

import de.ait.finbot.model.Expense;
import de.ait.finbot.model.User;

public interface CheckerService {

    public boolean isCategoryAvailableToUser(String categoryId, Expense expense);
    public boolean isExpenseAvailableToUser(String expenseId, User user);
}
