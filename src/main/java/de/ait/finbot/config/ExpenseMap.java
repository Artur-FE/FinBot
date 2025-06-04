package de.ait.finbot.config;

import de.ait.finbot.model.Expense;
import lombok.Getter;
import lombok.ToString;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Getter
@Component
@ToString
public class ExpenseMap {

    private final Map<Long, Expense> expenseMap = new HashMap<>();

    public void put (Long chatId, Expense expense) {
        expenseMap.put(chatId, expense);
    }

    public Expense get (Long chatId) {
        return expenseMap.get(chatId);
    }

    public Expense remove (Long chatId) {
        return expenseMap.remove(chatId);
    }
}
