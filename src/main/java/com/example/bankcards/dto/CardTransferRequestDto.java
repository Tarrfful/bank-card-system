package com.example.bankcards.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class CardTransferRequestDto {

    @NotNull(message = "Source card ID cannot be null")
    private Long fromCardId;

    @NotNull(message = "Destination card ID cannot be null")
    private Long toCardId;

    @NotNull(message = "Amount cannot be null")
    @DecimalMin(value = "0.01", message = "Transfer amount must be positive")
    private BigDecimal amount;

    public Long getFromCardId() { return fromCardId; }
    public void setFromCardId(Long fromCardId) { this.fromCardId = fromCardId; }
    public Long getToCardId() { return toCardId; }
    public void setToCardId(Long toCardId) { this.toCardId = toCardId; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
}