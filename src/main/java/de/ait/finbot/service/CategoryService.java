package de.ait.finbot.service;

import de.ait.finbot.model.Category;

import java.util.List;

public interface CategoryService {
    public boolean init();
    public Category addCategory(Long chatId, String name);
    public Category deleteCategoryById(Long categoryId);
    public List<Category> getAllCategory();
    public List<Category> getCategoryByUserId(Long id);
    public String getAllCategoryForUser(Long chatId);
    public Category getCategoryById(Long categoryID);
    public String getAllCategoryToDeleteForUser(Long chatId);
    public String getAllCategoryToEditNameForUser(Long chatId);
    public boolean checkCategoryToDeleteForUser(Long chatId, String categoryId);
    public List<Long> getCustomCategoryByUser_Id(Long chatId);
    public Category editNameCategory(Category category, String newName);
}
