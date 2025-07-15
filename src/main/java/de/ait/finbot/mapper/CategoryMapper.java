package de.ait.finbot.mapper;

import de.ait.finbot.model.Category;
import de.ait.finbot.model.User;
import de.ait.finbot.repository.UserRepository;
import de.ait.finbot.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class CategoryMapper {
    private final UserService userService;
    public Category StringNameToCategory(Long chatId, String name) {
        Category category = new Category();
        category.setName(name);
        category.setIsActive(true);
        category.setCreatedAt(LocalDateTime.now());
        User userById = userService.getUserByChatId(chatId);
        category.setUser(userById);
        log.info("класс CategoryMapper, метод StringNameCategory отработал, категория - {} добавлена юзеру с " +
              "chatId - {}", category, chatId);
        return category;
    }
}
