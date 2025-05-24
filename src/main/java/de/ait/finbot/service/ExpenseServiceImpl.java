package de.ait.finbot.service;

import de.ait.finbot.model.Expense;
import de.ait.finbot.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExpenseServiceImpl implements ExpenseService{
    private final ExpenseRepository expenseRepository;

    @Override
    public List<Expense> getAllExpense() {
        return expenseRepository.findAll();
    }

    @Override
    public Expense findExpenseById(Long id) {
        return expenseRepository.findById(id).get();
    }

    @Override
    public List<Expense> findAllExpenseByUser_Id(Long userId) {
        return expenseRepository.findAllByUser_Id(userId);
    }

    @Override
    public Expense addExpense(Expense expense) {
        return expenseRepository.save(expense);
    }

    @Override
    public boolean removeExpenseById(Long id) {
        return expenseRepository.removeById(id);
    }

    @Override
    public Expense findExpenseByNote(String name) {
        return expenseRepository.findExpenseByNote(name);
    }
}
