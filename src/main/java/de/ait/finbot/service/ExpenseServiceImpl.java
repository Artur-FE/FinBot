package de.ait.finbot.service;

import de.ait.finbot.mapper.ExpenseMapper;
import de.ait.finbot.model.Expense;
import de.ait.finbot.model.User;
import de.ait.finbot.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExpenseServiceImpl implements ExpenseService{
    private final ExpenseRepository expenseRepository;
    private final UserService userService;
    private final ExpenseMapper expenseMapper;

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
    public Expense findExpenseByNote(String name) {
        return expenseRepository.findExpenseByNote(name);
    }

    @Override
    public String findAllExpenseByChatId(Long chatId) {
        User user = userService.getUserByChatId(chatId);
        System.out.println("Получен юсер " + user + user.getId() + user.getUserName());

        String collect1 = findAllExpenseByUser_Id(user.getId())
                .stream()
                .filter(expense -> expense.isActive())
                .sorted(Comparator.comparing(Expense::getId))
                .map(expense -> expenseMapper.expenseToExpenseString(expense))
                .collect(Collectors.joining("\n"));

        System.out.println(user.getId());
        BigDecimal reduce = findAllExpenseByUser_Id(user.getId())
                .stream()
                .filter(expense -> expense.isActive())
                .map(expense -> expense.getAmount())
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        System.out.println(reduce);
        String textToSend = "Список расходов за весь период: " + "\n" + "\n" + collect1 + "\n" + "\n" + "Сумма расходов: " + reduce;
        return textToSend;
    }

    @Override
    public String findExpenseFor7DayByChatId(Long chatId) {
        User user = userService.getUserByChatId(chatId);
        System.out.println("Получен юсер " + user + user.getId() + user.getUserName());

        String collect1 = findAllExpenseByUser_Id(user.getId())
                .stream()
                .filter(expense -> LocalDateTime.now().getDayOfYear() - expense.getCreatedAt().getDayOfYear() < 7)
                .filter(expense -> expense.isActive())
                .sorted(Comparator.comparing(Expense::getId))
              //  .map(expense -> "<b>" + expense.getNote() + "</b>" + ": " + expense.getAmount() + ". <b>Дата:</b> " + expense.getCreatedAt().getDayOfMonth() + "." + String.format("%02d", expense.getCreatedAt().getMonthValue()) + "." + expense.getCreatedAt().getYear())
                .map(expense -> expenseMapper.expenseToExpenseString(expense))
                .collect(Collectors.joining("\n"));

        System.out.println(user.getId());
        BigDecimal reduce = findAllExpenseByUser_Id(user.getId())
                .stream()
                .filter(expense -> LocalDateTime.now().getDayOfYear() - expense.getCreatedAt().getDayOfYear() < 7)
                .filter(expense -> expense.isActive())
                .map(expense -> expense.getAmount())
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        System.out.println(reduce);
        String textToSend = "<b>Список расходов за последние 7 дней:</b> " + "\n" + "\n" + collect1 + "\n" + "\n" +"<b>Cумма расходов:</b> " + reduce;
        return textToSend;
    }

    @Override
    public String findExpenseForToDayByChatId(Long chatId) {
        User user = userService.getUserByChatId(chatId);
        System.out.println("Получен юсер " + user + user.getId() + user.getUserName());

        String collect1 = findAllExpenseByUser_Id(user.getId())
                .stream()
                .filter(expense -> expense.getCreatedAt().getDayOfYear() == LocalDateTime.now().getDayOfYear())
                .filter(expense -> expense.isActive())
                .sorted(Comparator.comparing(Expense::getId))
                .map(expense -> expenseMapper.expenseToExpenseString(expense))
                .collect(Collectors.joining("\n"));

        System.out.println(user.getId());
        BigDecimal reduce = findAllExpenseByUser_Id(user.getId())
                .stream()
                .filter(expense -> expense.getCreatedAt().getDayOfYear() == LocalDateTime.now().getDayOfYear())
                .filter(expense -> expense.isActive())
                .map(expense -> expense.getAmount())
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        System.out.println(reduce);
        String textToSend = "<b>Список расходов за сегодня:</b> " + "\n" + "\n" + collect1 + "\n" + "\n" +"<b>Cумма расходов:</b> " + reduce;
        return textToSend;
    }

    @Override
    public Expense removeExpenseById(Long id) {
        Expense expense = expenseRepository.findById(id).get();
        try {
            if (!expense.isActive()) {
                throw new RuntimeException();
            } else {
                expense.setActive(false);
                expenseRepository.save(expense);
                return expense;
            }
        } catch (RuntimeException e) {
            throw new RuntimeException();
        }

    }

    @Override
    public Expense removeExpenseByNote(String note) {
        return null;
    }
}
