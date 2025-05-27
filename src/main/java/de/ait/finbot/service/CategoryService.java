package de.ait.finbot.service;

import de.ait.finbot.model.Category;

import java.util.List;

public interface CategoryService {
    public boolean init();
    public Category addCategory(Long chatId, String name);
    public Category deleteCategory();
    public List<Category> getAllCategory();
    public List<Category> getCategoryByUserId(Long id);
    public String getAllCategoryForUser(Long chatId);
    public Category getCategoryById(Long categoryID);
}
