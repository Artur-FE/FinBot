package de.ait.finbot.service;

import de.ait.finbot.mapper.ExpenseMapper;
import de.ait.finbot.model.Category;
import de.ait.finbot.model.Expense;
import de.ait.finbot.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CheckerServiceImpl implements CheckerService {
    private final ExpenseMapper expenseMapper;
    private final CategoryService categoryService;
    private final ExpenseService expenseService;

    @Override
    public boolean isCategoryAvailableToUser(String categoryId, Expense expense) {
        try {
            Long userId = expense.getUser().getId();
            List<Category> categoryByUserId = categoryService.getCategoryByUserId(userId);
            return categoryByUserId.stream().anyMatch(category -> category.getId().equals(Long.valueOf(categoryId)));
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean isExpenseAvailableToUser(String expenseId, User user) {
        try {
            return expenseService.findAllExpenseByUser_Id(user.getId())
                    .stream().anyMatch(expense -> expense.getId().equals(Long.valueOf(expenseId)));
        } catch (Exception e) {
            return false;
        }
    }
}