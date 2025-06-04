package de.ait.finbot.config;

import lombok.Getter;
import lombok.ToString;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Getter
@Component
@ToString
public class StatusMessageMap {
    private final Map<Long, StatusMessage> statusMessageMap = new HashMap<>();

    public void put (Long chatId, StatusMessage statusMessage) {
        statusMessageMap.put(chatId, statusMessage);
    }

    public StatusMessage get (Long chatId) {
        return statusMessageMap.get(chatId);
    }

    public StatusMessage remove (Long chatId) {
        return statusMessageMap.remove(chatId);
    }
}
