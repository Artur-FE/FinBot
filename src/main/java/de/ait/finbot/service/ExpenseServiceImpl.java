package de.ait.finbot.service;

import de.ait.finbot.mapper.ExpenseMapper;
import de.ait.finbot.model.Expense;
import de.ait.finbot.model.User;
import de.ait.finbot.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.Period;
import java.time.format.DateTimeFormatter;
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
    public Expense findExpenseById(Long chatId, Long expenseId) {
        User user = userService.getUserByChatId(chatId);
       return findAllExpenseByUser_Id(user.getId())
                .stream()
                .filter(expense -> expense.getId().equals(expenseId))
                .findFirst()
                .orElseThrow();
    }

    @Override
    public List<Expense> findAllExpenseByUser_Id(Long userId) {
        return expenseRepository.findAllByUser_Id(userId)
                .stream()
                .filter(expense -> expense.isActive())
                .toList();
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
    public List<Expense> findAllExpenseByNoteIgnoreCase(Long chatId, String name) {
        User user = userService.getUserByChatId(chatId);
        List<Expense> allExpenseByUserId = findAllExpenseByUser_Id(user.getId())
                .stream()
                .filter(expense -> expense.getNote().equals(name))
                .toList();
        return allExpenseByUserId;

    }

    @Override
    public String findAllExpenseByChatId(Long chatId) {
        User user = userService.getUserByChatId(chatId);
        System.out.println("Получен юсер " + user + user.getId() + user.getUserName());

        String collect1 = findAllExpenseByUser_Id(user.getId())
                .stream()
                .sorted(Comparator.comparing(Expense::getId))
                .map(expense -> expenseMapper.expenseToExpenseString(expense))
                .collect(Collectors.joining("\n"));

        System.out.println(user.getId());
        BigDecimal reduce = findAllExpenseByUser_Id(user.getId())
                .stream()
                .map(expense -> expense.getAmount())
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        System.out.println(reduce);
        String textToSend = "<b>Список расходов за весь период:</b> " + "\n" + "\n" + collect1 + "\n" + "\n" + "Сумма расходов: " + reduce;
        return textToSend;
    }

    @Override
    public String findExpenseFor7DayByChatId(Long chatId) {
        User user = userService.getUserByChatId(chatId);
        LocalDateTime todayMinus7Days = LocalDateTime.now().minusDays(6);
        String stringTodayMinus7Days = todayMinus7Days.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        LocalDateTime today = LocalDateTime.now();
        String stringToday = today.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));


        String collect1 = findAllExpenseByUser_Id(user.getId())
                .stream()
                .filter(expense -> Period.between(expense.getCreatedAt().toLocalDate(), today.toLocalDate()).getYears() == 0)
                .filter(expense -> Period.between(expense.getCreatedAt().toLocalDate(), today.toLocalDate()).getMonths() == 0)
                .filter(expense -> Period.between(expense.getCreatedAt().toLocalDate(), today.toLocalDate()).getDays() < 7)
                .sorted(Comparator.comparing(Expense::getId))
                .map(expense -> expenseMapper.expenseToExpenseString(expense))
                .collect(Collectors.joining("\n"));

        System.out.println(user.getId());
        BigDecimal reduce = findAllExpenseByUser_Id(user.getId())
                .stream()
                .filter(expense -> Period.between(expense.getCreatedAt().toLocalDate(), today.toLocalDate()).getYears() == 0)
                .filter(expense -> Period.between(expense.getCreatedAt().toLocalDate(), today.toLocalDate()).getMonths() == 0)
                .filter(expense -> Period.between(expense.getCreatedAt().toLocalDate(), today.toLocalDate()).getDays() < 7)
                .map(expense -> expense.getAmount())
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        String textToSend = "<b>Список расходов за последние 7 дней</b>, "  + stringTodayMinus7Days + " - " +
                stringToday +  "\n" + "\n" + collect1 +
                "\n" + "\n" +"<b>Cумма расходов:</b> " + reduce;
        return textToSend;
    }

    @Override
    public String findExpenseForToDayByChatId(Long chatId) {
        User user = userService.getUserByChatId(chatId);
        System.out.println("Получен юсер " + user + user.getId() + user.getUserName());

        String collect1 = findAllExpenseByUser_Id(user.getId())
                .stream()
                .filter(expense -> Period.between(LocalDateTime.now().toLocalDate(), expense.getCreatedAt().toLocalDate()).getYears() == 0)
                .filter(expense -> Period.between(LocalDateTime.now().toLocalDate(), expense.getCreatedAt().toLocalDate()).getMonths() == 0)
                .filter(expense -> Period.between(LocalDateTime.now().toLocalDate(), expense.getCreatedAt().toLocalDate()).getDays() == 0)
                .sorted(Comparator.comparing(Expense::getId))
                .map(expense -> expenseMapper.expenseToExpenseString(expense))
                .collect(Collectors.joining("\n"));

        System.out.println(user.getId());
        BigDecimal reduce = findAllExpenseByUser_Id(user.getId())
                .stream()
                .filter(expense -> Period.between(LocalDateTime.now().toLocalDate(), expense.getCreatedAt().toLocalDate()).getYears() == 0)
                .filter(expense -> Period.between(LocalDateTime.now().toLocalDate(), expense.getCreatedAt().toLocalDate()).getMonths() == 0)
                .filter(expense -> Period.between(LocalDateTime.now().toLocalDate(), expense.getCreatedAt().toLocalDate()).getDays() == 0)
                .map(expense -> expense.getAmount())
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        System.out.println(reduce);
        String textToSend = "<b>Список расходов за сегодня:</b> " + "\n" + "\n" + collect1 + "\n" + "\n" +"<b>Cумма расходов:</b> " + reduce;
        return textToSend;
    }

    @Override
    public Expense removeExpenseById(Long chatId, Long expenseId) {
        User user = userService.getUserByChatId(chatId);
        boolean isExpenseByIdAvailableToUser = findAllExpenseByUser_Id(user.getId())
                .stream()
                .anyMatch(expense -> expense.getId().equals(expenseId));
        Expense expense = expenseRepository.findById(expenseId).get();
        try {
            if (isExpenseByIdAvailableToUser && expense.isActive()){
                expense.setActive(false);
                expenseRepository.save(expense);
                return expense;
            } else {
                    throw new RuntimeException();
            }
        } catch (RuntimeException e) {
            throw new RuntimeException();
        }

    }

    @Override
    public boolean removeAllExpenseByUser(Long chatId) {
        System.out.println("Вызов removeAllExpenseByUser");
        User user = userService.getUserByChatId(chatId);

        boolean b = expenseRepository.findAllByUser_Id(user.getId())
                .stream()
                .peek(expense -> expense.setActive(false))
                .peek(expense -> expenseRepository.save(expense))
                .noneMatch(expense -> expense.isActive());
        System.out.println(b);

        return b;

    }


    @Override
    public Expense removeExpenseByNote(String note) {
        return null;
    }
}
