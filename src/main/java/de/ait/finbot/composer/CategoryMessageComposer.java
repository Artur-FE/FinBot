package de.ait.finbot.composer;

import de.ait.finbot.config.KeyBoard;
import de.ait.finbot.model.MessageObj;
import de.ait.finbot.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


@Component
@Slf4j
@RequiredArgsConstructor
public class CategoryMessageComposer {
    private final CategoryService categoryService;
    private final KeyBoard keyBoard;
//    @Lazy
//    private final TelegramBotHandler telegramBotHandler;

    public MessageObj getAllCategoryForUser(Long chatId) {
        return new MessageObj(chatId, "<b>Список твоих категорий:</b> \n\n"
                + categoryService.getAllCategoryForUser(chatId),
                keyBoard.categoryMenuKeyboard(), true);
    }
}
