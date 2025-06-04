package de.ait.finbot.config;

import de.ait.finbot.model.Category;
import lombok.Getter;
import lombok.ToString;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;


@Component
@Getter
@ToString
public class CategoryMap {
    private final Map<Long, Category> categoryMap = new HashMap<>();

    public void put (Long chatId, Category category) {
        categoryMap.put(chatId, category);
    }

    public Category get (Long chatId) {
        return categoryMap.get(chatId);
    }

    public Category remove (Long chatId) {
        return categoryMap.remove(chatId);
    }

}
