package com.example.bankcards.dto;

import com.example.bankcards.entity.CardStatus;
import java.math.BigDecimal;
import java.time.LocalDate;

public class CardResponseDto {

    private Long id;
    private String cardNumberMasked;
    private LocalDate expiryDate;
    private CardStatus status;
    private BigDecimal balance;
    private Long userId;
    private String username;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCardNumberMasked() { return cardNumberMasked; }
    public void setCardNumberMasked(String cardNumberMasked) { this.cardNumberMasked = cardNumberMasked; }
    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }
    public CardStatus getStatus() { return status; }
    public void setStatus(CardStatus status) { this.status = status; }
    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
}