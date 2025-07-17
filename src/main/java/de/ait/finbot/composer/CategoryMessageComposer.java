package de.ait.finbot.composer;

import de.ait.finbot.config.CategoryMap;
import de.ait.finbot.config.KeyBoard;
import de.ait.finbot.config.StatusMessage;
import de.ait.finbot.config.StatusMessageMap;
import de.ait.finbot.model.Category;
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
    private final StatusMessageMap statusMessageMap;
    private final CategoryMap categoryMap;
//    @Lazy
//    private final TelegramBotHandler telegramBotHandler;

    public MessageObj getAllCategoryForUser(Long chatId) {
        return new MessageObj(chatId, "<b>Список твоих категорий:</b> \n\n"
                + categoryService.getAllCategoryForUser(chatId),
                keyBoard.categoryMenuKeyboard(), true);
    }

    public MessageObj addCategory(long chatId, String messageText) {
        if (StatusMessage.WAITING_CATEGORY.equals(statusMessageMap.get(chatId))) {
            log.info("класс CategoryMessageComposer, метод addCategory, блок if, " +
                            "chatId - {}, messageText - {}, statusMessageMap.get(chatId) - {}",
                    chatId, messageText, statusMessageMap.get(chatId));
            return putCategory(chatId, messageText);
        } else {
            statusMessageMap.put(chatId, StatusMessage.WAITING_CATEGORY);
            log.info("класс CategoryMessageComposer, метод addCategory, блок else, " +
                            "chatId - {}, messageText - {}, statusMessageMap.get(chatId) - {}",
                    chatId, messageText, statusMessageMap.get(chatId));

            return new MessageObj(chatId, "Введите название категории",
                    keyBoard.backToStartAndCategoryMenuKeyboard(), true);
        }
    }

    private MessageObj putCategory(long chatId, String messageText) {
        categoryService.addCategory(chatId, messageText);
        log.info("класс CategoryMessageComposer, метод putCategory " +
                        "chatId - {}, messageText - {}, statusMessageMap.get(chatId) - {}",
                chatId, messageText, statusMessageMap.get(chatId));
        statusMessageMap.remove(chatId);
        log.info("класс CategoryMessageComposer, putCategory, после statusMessageMap.remove(chatId) " +
                "statusMessageMap.get(chatId) - {}", statusMessageMap.get(chatId));
        return new MessageObj(chatId, "Категория " + messageText + " добавлена! \nПолный список Ваших категорий. \n" +
                categoryService.getAllCategoryForUser(chatId), keyBoard.backToStartAndCategoryMenuKeyboard(), true);
    }

    public MessageObj deleteCategory(long chatId, String messageText) {

        if (StatusMessage.WAITING_ID_CATEGORY_TO_DELETE.equals(statusMessageMap.get(chatId))) {
            try {
                if (categoryService.checkCategoryToDeleteForUser(chatId, messageText)) {
                    log.info("класс CategoryMessageComposer, метод deleteCategory, блок if -> if, " +
                                    "chatId - {}, messageText - {}, statusMessageMap.get(chatId) - {}",
                            chatId, messageText, statusMessageMap.get(chatId));

                    Category category = categoryService.deleteCategoryById(Long.valueOf(messageText));
                    statusMessageMap.remove(chatId);
                    return new MessageObj(chatId, "Категория с ID " + category.getId() + " удалена! " +
                            "\nПолный список Ваших категорий. \n" +
                            categoryService.getAllCategoryForUser(chatId),
                            keyBoard.backToStartAndCategoryMenuKeyboard(), true);
                } else {
                    log.info("Категория не найдена! Класс CategoryMessageComposer, метод deleteCategory, блок if -> else, " +
                                    "chatId - {}, messageText - {}, statusMessageMap.get(chatId) - {}",
                            chatId, messageText, statusMessageMap.get(chatId));

                    return new MessageObj(chatId, "Введенная категория с ID: " + messageText
                            + " не является Вашей категорией. Повторите ввод",
                            keyBoard.backToStartAndCategoryMenuKeyboard(), true);
                }
            } catch (NumberFormatException e) {
                log.error("Ошибка! Введена не цифра для идентификации категории. " +
                                "Класс CategoryMessageComposer, метод deleteCategory, блок if -> catch, " +
                                "chatId - {}, messageText - {}  statusMessageMap.get(chatId) - {}",
                        chatId, messageText, statusMessageMap.get(chatId));

                return new MessageObj(chatId, "Ошибка. Вы ввели не цифру для идентификации категории. Повторите ввод.",
                        keyBoard.backToStartAndCategoryMenuKeyboard(), true);
            }
        } else {
            try {

                statusMessageMap.put(chatId, StatusMessage.WAITING_ID_CATEGORY_TO_DELETE);
                log.info("Выводим список категорий доступных для удаления. " +
                                "Класс CategoryMessageComposer, метод deleteCategory, блок else, " +
                                "chatId - {}, messageText - {}  statusMessageMap.get(chatId) - {}",
                        chatId, messageText, statusMessageMap.get(chatId));

                return new MessageObj(chatId, "Удалить можно только те категории, в которых нет привязанных расходов. " +
                        "Также нельзя удалить категории по умолчанию. \n" +
                        "Введите в следующем сообщении ID категории, которую хотите удалить. " +
                        "\nНиже список доступных  категорий:\n" +
                        categoryService.getAllCategoryToDeleteForUser(chatId),
                        keyBoard.backToStartAndCategoryMenuKeyboard(), true);

            } catch (RuntimeException e) {
                statusMessageMap.remove(chatId);
                log.error("Ошибка! Нет доступных категорий для удаления." +
                                "Класс CategoryMessageComposer, метод deleteCategory, блок else -> catch, " +
                                "chatId - {}, messageText - {}  statusMessageMap.get(chatId) - {}",
                        chatId, messageText, statusMessageMap.get(chatId));
                return new MessageObj(chatId, "Нет доступных категорий для удаления",
                        keyBoard.backToStartAndCategoryMenuKeyboard(), true);
            }
        }

    }

    public MessageObj editCategoryName(long chatId, String messageText) {
        if (StatusMessage.WAITING_ID_CATEGORY_TO_EDIT_NAME.equals(statusMessageMap.get(chatId))) {
            try {
                Long categoryId = Long.valueOf(messageText);
                if (categoryService.getCustomCategoryByUser_Id(chatId).contains(categoryId)) {
                    Category category = categoryService.getCategoryById(categoryId);
                    categoryMap.put(chatId, category);
                    statusMessageMap.put(chatId, StatusMessage.WAITING_NAME_CATEGORY_TO_EDIT_NAME);
                    log.info("Категория найдена - {}", category);
                    return new MessageObj(chatId, "Категория  с ID: <b>" + category.getId() + "</b> и именем: <b>"
                            + category.getName() + "</b> успешно найдена.\n" +
                            "Введи новое имя для этой категории",
                            keyBoard.backToStartAndCategoryMenuKeyboard(), true);
                } else {
                    log.info("Категория ID - {} не принадлежит user {} или является общей категорией", messageText, chatId);
                    return new MessageObj(chatId, "Введенная категория с ID: " + messageText +
                            " не является твоей категорией доступной для изменения. Повтори ввод",
                            keyBoard.backToStartAndCategoryMenuKeyboard(), true);
                }
            } catch (NumberFormatException e) {
                log.info("Невозможно преобразовать ввод пользователя в цифры. Сейчас передан ID - {}", messageText);
                return new MessageObj(chatId, "ID категории состоит только из цифр. Твой <b>ID: " + messageText +
                        "</b> не из цифр!\n " + "Повтори ввод ID",
                        keyBoard.backToStartAndCategoryMenuKeyboard(), true);
            }

        } else {
            try {
                statusMessageMap.put(chatId, StatusMessage.WAITING_ID_CATEGORY_TO_EDIT_NAME);
                log.info("Ожидание ввода от пользователя ID категории для редактирования");
                return new MessageObj(chatId, "Введи ID категории для редактирования. " +
                        "Ниже список доступных для редактирования категорий:\n" +
                        categoryService.getAllCategoryToEditNameForUser(chatId),
                        keyBoard.backToStartAndCategoryMenuKeyboard(), true);

            } catch (RuntimeException e) {
                log.info("Нет доступных категорий для редактирования у chatId - {}", chatId);
                return new MessageObj(chatId, "Нет доступных категорий для редактирования.",
                        keyBoard.backToStartAndCategoryMenuKeyboard(), true);
            }
        }
    }

    public MessageObj putCategoryName(long chatId, String messageText) {
        Category category = categoryMap.get(chatId);
        categoryService.editNameCategory(category, messageText);
        categoryMap.remove(chatId);
        statusMessageMap.remove(chatId);
        return new MessageObj(chatId, "Имя категории с <b>ID: " + category.getId() +
                "</b> успешно изменено на <b>" + category.getName() + "</b>", keyBoard.categoryMenuKeyboard(), true);
    }
}
