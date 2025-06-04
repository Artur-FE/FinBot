package de.ait.finbot.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;

@Component
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MessageObj {
    private long chatId;
    private String textToSend;
    private ReplyKeyboardMarkup keyBoard;
    private boolean setParseMode;
}
