package de.ait.finbot.mapper;

import de.ait.finbot.model.User;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class UserMapper {

    public User chatIdAndNameToUser(Long chatId, String name){
        User user = new User();
        user.setUserName(name);
        user.setChatId(chatId);
        user.setCreatedAt(LocalDateTime.now());
        return user;
    }
}
