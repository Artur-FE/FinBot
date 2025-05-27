package de.ait.finbot.service;

import de.ait.finbot.mapper.CategoryMapper;
import de.ait.finbot.model.Category;
import de.ait.finbot.model.User;
import de.ait.finbot.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final UserService userService;

    @Override
    public boolean init() {
//        List<Category> categories = new ArrayList<>();
//        categories.add(new Category("Продукты", null));
//        categories.add(new Category("Транспорт", null));
//        categories.add(new Category("Коммунальные услуги", null));
//        categories.add(new Category("Развлечения", null));
//        categories.add(new Category("Другое", null));
//        log.info(categories.toString());
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

    @Override
    public Category deleteCategory() {
        return null;
    }

    @Override
    public List<Category> getAllCategory() {
        return categoryRepository.findAll();
    }

    @Override
    public List<Category> getCategoryByUserId(Long userId) {
//        return categoryRepository.findAllByUser_Id(userId);
        List<Category> resultCategory = categoryRepository.findAllByUser_Id(null);
        List<Category> categoryByUserId = categoryRepository.findAllByUser_Id(userId);
        resultCategory.addAll(categoryByUserId);
        return resultCategory;

    }

    @Override
    public String getAllCategoryForUser(Long chatId) {
        User userByChatId = userService.getUserByChatId(chatId);
        userByChatId.getId();

//        List<Category> resultCategory = getCategoryByUserId(null);
//        List<Category> categoryByUserId = getCategoryByUserId(userByChatId.getId());
//        resultCategory.addAll(categoryByUserId);
        List<Category> resultCategory = getCategoryByUserId(userByChatId.getId());
        String category = resultCategory.stream()
                // .map(Category::getName)
                .map(category1 -> "<b>ID: </b>" + category1.getId() + ". <b>Имя: </b>" + category1.getName())
                .collect(Collectors.joining("\n"));
        return category;
    }

    @Override
    public Category getCategoryById(Long categoryID) {

        return categoryRepository.findCategoryById(categoryID);
    }
}
