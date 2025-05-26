package de.ait.finbot.repository;

import de.ait.finbot.model.Expense;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    public List<Expense> findAll();
    public Optional<Expense> findById(Long id);
    public List<Expense> findAllByUser_Id(Long userId);
    public Expense save(Expense expense);
    public boolean removeById(Long id);
    public boolean removeByNote(String note);
    public Expense findExpenseByNote(String note);

}
