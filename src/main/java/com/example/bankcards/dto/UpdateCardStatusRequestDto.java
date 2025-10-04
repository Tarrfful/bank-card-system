package com.example.bankcards.dto;

import com.example.bankcards.entity.CardStatus;
import jakarta.validation.constraints.NotNull;

public class UpdateCardStatusRequestDto {

    @NotNull(message = "New status cannot be null")
    private CardStatus newStatus;

    public CardStatus getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(CardStatus newStatus) {
        this.newStatus = newStatus;
    }
}