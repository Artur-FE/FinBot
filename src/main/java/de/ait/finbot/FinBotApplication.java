package de.ait.finbot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;

@SpringBootApplication
@PropertySource(value = "classpath:botMessages.properties", encoding = "UTF-8") // Указываем файл и его кодировку

public class FinBotApplication {

    public static void main(String[] args) {
        SpringApplication.run(FinBotApplication.class, args);
    }

}
