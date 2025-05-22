package de.ait.finbot.repository;

import de.ait.finbot.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    public List<User> findAll();
    public Optional<User> findById(Long chatId);
    public Optional<User> findByChatId(Long chatId);
    public User save(User user);


}
