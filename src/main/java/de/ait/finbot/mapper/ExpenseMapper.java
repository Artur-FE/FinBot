package de.ait.finbot.mapper;

import de.ait.finbot.model.Category;
import de.ait.finbot.model.Expense;
import de.ait.finbot.model.User;
import de.ait.finbot.repository.EntertainmentRepository;
import de.ait.finbot.repository.ProductRepository;
import de.ait.finbot.repository.PublicUtilitiesRepository;
import de.ait.finbot.repository.TransportRepository;
import de.ait.finbot.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExpenseMapper {
    private final UserService userService;
    private final ProductRepository productRepository;
    private final TransportRepository transportRepository;
    private final PublicUtilitiesRepository publicUtilitiesRepository;
    private final EntertainmentRepository entertainment;

    public Expense chatIdAndNoteToExpense(Long chatId, String note){

        String namesExpense = Arrays.stream(note.split("\\d+[.,]?")).
                toList().
                stream()
                .collect(Collectors.joining("")).trim();

        String amountString = Arrays.stream(note.split("[^0-9.,]"))
                .toList()
                .stream()
                .collect(Collectors.joining("")).trim();
        log.info(amountString);

        String[] namesExpenseSplit = namesExpense.split(" ");
        BigDecimal amount;
        if(amountString.isBlank()) {
            amount = new BigDecimal("0");
        } else {
            amount = new BigDecimal(amountString);
        }
        log.info(String.valueOf(amount));
        System.out.println("имя расхода" + namesExpense);
        System.out.println("величина расхода" + amount);


        User user = userService.getUserByChatId(chatId);
        Expense expense = new Expense();
        expense.setUser(user);
        expense.setAmount(amount);
        expense.setActive(true);
        if(namesExpense.isBlank()){
            expense.setNote("Без названия");
        } else {
            expense.setNote(namesExpense);
        }
        Category category = new Category();
        for (String s : namesExpenseSplit){
            if(productRepository.PRODUCTS.contains(s.toLowerCase())){
                category.setId(1L);
                category.setName("Продукты");
            } else if(transportRepository.TRANSPORT.contains(s.toLowerCase())){
                category.setId(2L);
                category.setName("Транспорт");
            } else if (publicUtilitiesRepository.PUBLICUTILITIES.contains(s.toLowerCase())) {
                category.setId(3L);
                category.setName("Коммунальные услуги");
            } else if (entertainment.ENTERTAINMENT.contains(s.toLowerCase())) {
                category.setId(4L);
                category.setName("Развлечения");
            } else {
                category.setId(5L);
                category.setName("Другое");
            }
        }

        expense.setCategory(category);
        expense.setCreatedAt(LocalDateTime.now());
       return expense;
    }

    public String expenseToExpenseString(Expense expense) {

        String expenseToList = "<b>ID:</b> " + expense.getId() + ". <b>" + expense.getNote() + "</b>" + ": " +
                expense.getAmount() + ". <b>Дата:</b> " +
                expense.getCreatedAt().getDayOfMonth() + "." +
                String.format("%02d", expense.getCreatedAt().getMonthValue()) + "." +
                expense.getCreatedAt().getYear();
        return expenseToList;
    }

    public String expenseToExpenseStringAllField(Expense expense) {

        String expenseToList = "<b>ID:</b> " + expense.getId() +
                "\n<b>Имя: </b>" + expense.getNote() +
                "\n<b>Сумма: </b>" + expense.getAmount() +
                "\n<b>Категория: </b>" + expense.getCategory().getName() +
                "\n<b>Дата:</b> " + expense.getCreatedAt().getDayOfMonth() + "." +
                String.format("%02d", expense.getCreatedAt().getMonthValue()) + "." +
                expense.getCreatedAt().getYear();
        return expenseToList;
    }

}
