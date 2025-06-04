package de.ait.finbot.repository;

import de.ait.finbot.model.Category;
import de.ait.finbot.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    public List<Category> findAll();
    public Optional<Category> findById(Long id);
    public Category save(Category category);
    public List<Category> findAllByUser_Id(Long userId);
    public List<Category> findCustomCategoryByUser_Id(Long chatId);
   // public Category save(List<Category> category);
   public Category findCategoryById(Long categoryID);


}
