package de.ait.finbot.service;

import de.ait.finbot.model.User;

import java.util.List;

public interface UserService {
    public User addUser(User user);
    public List<User> getAllUsers();
    public User getUserById(Long id);
    public User getUserByChatId(Long id);
}
