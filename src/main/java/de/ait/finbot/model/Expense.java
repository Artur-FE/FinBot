package de.ait.finbot.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString

@Entity
@Table(name = "expense")
public class Expense {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;
    @Column(name = "amount")
    private BigDecimal amount;
    @Column(name = "note")
    private String note;
    @Column(name = "createdAt")
    private LocalDateTime createdAt;
    @Column(name="isActive")
    private boolean isActive;
}

