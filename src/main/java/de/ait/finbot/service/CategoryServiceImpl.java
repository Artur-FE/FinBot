package de.ait.finbot.service;

import de.ait.finbot.mapper.CategoryMapper;
import de.ait.finbot.model.Category;
import de.ait.finbot.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

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
        return categoryRepository.findAllByUser_Id(userId);
    }
}
