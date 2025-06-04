package de.ait.finbot.service;

import de.ait.finbot.mapper.CategoryMapper;
import de.ait.finbot.model.Category;
import de.ait.finbot.model.Expense;
import de.ait.finbot.model.User;
import de.ait.finbot.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final UserService userService;
    private final ExpenseService expenseService;

    @Override
    public boolean init() {
        if (categoryRepository.findAll().isEmpty()) {
            categoryRepository.save(new Category("Продукты", null));
            categoryRepository.save(new Category("Транспорт", null));
            categoryRepository.save(new Category("Коммунальные услуги", null));
            categoryRepository.save(new Category("Развлечения", null));
            categoryRepository.save(new Category("Другое", null));
            log.info(categoryRepository.findAll().toString());
            return true;
        } else {
            log.info("таблица уже создана" + categoryRepository.findAll().toString());
            return false;
        }
    }

    @Override
    public Category addCategory(Long chatId, String name) {
        Category category = categoryMapper.StringNameToCategory(chatId, name);
        log.info(category.toString());
        categoryRepository.save(category);
        return category;
    }

    public Category editNameCategory(Category category, String newName) {
        category.setName(newName);
        categoryRepository.save(category);
        log.info(category.toString());
        return category;
    }

    @Override
    public Category deleteCategoryById(Long categoryId) {
        Category categoryById = categoryRepository.findCategoryById(categoryId);
        categoryById.setIsActive(false);
        categoryRepository.save(categoryById);
        return categoryById;
    }

    @Override
    public List<Category> getAllCategory() {
        return categoryRepository.findAll();
    }

    @Override
    public List<Category> getCategoryByUserId(Long userId) {
        List<Category> resultCategory = categoryRepository.findAllByUser_Id(null);
        List<Category> categoryByUserId = categoryRepository.findAllByUser_Id(userId)
                .stream()
                .filter(Category::getIsActive)
                .toList();
        resultCategory.addAll(categoryByUserId);
        return resultCategory;

    }

    @Override
    public String getAllCategoryForUser(Long chatId) {
        User userByChatId = userService.getUserByChatId(chatId);
        userByChatId.getId();
        List<Category> resultCategory = getCategoryByUserId(userByChatId.getId());
        String category = resultCategory.stream()
                .filter(Category::getIsActive)
                .sorted(Comparator.comparing(Category::getId))
                .map(category1 -> "<b>ID: </b>" + category1.getId() + ". <b>Имя: </b>" + category1.getName())
                .collect(Collectors.joining("\n"));
        return category;
    }

    @Override
    public Category getCategoryById(Long categoryID) {

        return categoryRepository.findCategoryById(categoryID);
    }

    @Override
    public String getAllCategoryToDeleteForUser(Long chatId) {
        User userByChatId = userService.getUserByChatId(chatId);
        Long userId = userByChatId.getId();
        List<Category> categoryByUserId = categoryRepository.findAllByUser_Id(userId)
                .stream()
                .filter(Category::getIsActive)
                .toList();
        if (categoryByUserId.isEmpty()) {
            throw new RuntimeException();
        }
        List<Long> allExpenseByUserId = expenseService.findAllExpenseByUser_Id(userId)
                .stream()
                .map(expense -> expense.getCategory().getId())
                .toList();
        log.info(String.valueOf(allExpenseByUserId));
        List<Category> resultIdToDelete = categoryByUserId
                .stream()
                .filter(categoryByUser -> !allExpenseByUserId.contains(categoryByUser.getId()))
                .toList();

        log.info(String.valueOf(resultIdToDelete));
        String category = resultIdToDelete.stream()
                .sorted(Comparator.comparing(Category::getId))
                .map(category1 -> "<b>ID: </b>" + category1.getId() + ". <b>Имя: </b>" + category1.getName())
                .collect(Collectors.joining("\n"));
        return category;

    }

    @Override
    public String getAllCategoryToEditNameForUser(Long chatId) {
        User userByChatId = userService.getUserByChatId(chatId);
        Long userId = userByChatId.getId();
        List<Category> categoryByUserId = categoryRepository.findAllByUser_Id(userId)
                .stream()
                .filter(Category::getIsActive)
                .toList();

        if (categoryByUserId.isEmpty()) {
            throw new RuntimeException();
        }

        String category = categoryByUserId.stream()
                .sorted(Comparator.comparing(Category::getId))
                .filter(Category::getIsActive)
                .map(category1 -> "<b>ID: </b>" + category1.getId() + ". <b>Имя: </b>" + category1.getName())
                .collect(Collectors.joining("\n"));
        return category;
    }

    @Override
    public boolean checkCategoryToDeleteForUser(Long chatId, String categoryId) {
        try {
            Long longId = Long.valueOf(categoryId);
            User userByChatId = userService.getUserByChatId(chatId);
            Long userId = userByChatId.getId();
            List<Category> categoryByUserId = categoryRepository.findAllByUser_Id(userId)
                    .stream()
                    .filter(Category::getIsActive)
                    .toList();
            if (categoryByUserId.isEmpty()) {
                throw new RuntimeException();
            }
            List<Long> allExpenseByUserId = expenseService.findAllExpenseByUser_Id(userId)
                    .stream()
                    .map(expense -> expense.getCategory().getId())
                    .toList();
            log.info(String.valueOf(allExpenseByUserId));
            List<Long> resultIdToDelete = categoryByUserId
                    .stream()
                    .filter(Category::getIsActive)
                    .filter(categoryByUser -> !allExpenseByUserId.contains(categoryByUser.getId()))
                    .map(Category::getId)
                    .toList();
            System.out.println("resultIdToDelete.contains(longId) + " + resultIdToDelete.contains(longId));
            return resultIdToDelete.contains(longId);
        } catch (NumberFormatException e) {
            throw new NumberFormatException();
        }
    }

    @Override
    public List<Long> getCustomCategoryByUser_Id(Long chatId) {
        User userByChatId = userService.getUserByChatId(chatId);
        Long userId = userByChatId.getId();
        List<Long> customCategoryByUser_Id = categoryRepository.findCustomCategoryByUser_Id(userId)
                .stream()
                .filter(Category::getIsActive)
                .map(Category::getId)
                .toList();
        System.out.println(customCategoryByUser_Id);
        return customCategoryByUser_Id;
    }
}
