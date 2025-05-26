package de.ait.finbot.service;

import de.ait.finbot.model.Expense;

import java.util.List;
import java.util.Optional;

public interface ExpenseService {

    public List<Expense> getAllExpense();
    public Expense findExpenseById(Long id);
    public List<Expense> findAllExpenseByUser_Id(Long userId);
    public Expense addExpense(Expense expense);
    public Expense removeExpenseById(Long id);
    public Expense findExpenseByNote(String name);
    public String findAllExpenseByChatId(Long chatId);
    public String findExpenseFor7DayByChatId(Long chatId);
    public String findExpenseForToDayByChatId(Long chatId);
    public Expense removeExpenseByNote(String note);
}
