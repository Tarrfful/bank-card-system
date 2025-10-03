package com.example.bankcards.util;

import com.example.bankcards.dto.CardResponseDto;
import com.example.bankcards.entity.Card;
import org.springframework.stereotype.Component;

@Component
public class CardMapper {

    public CardResponseDto toDto(Card card) {
        if (card == null) {
            return null;
        }

        CardResponseDto dto = new CardResponseDto();

        dto.setId(card.getId());
        dto.setExpiryDate(card.getExpiryDate());
        dto.setStatus(card.getStatus());
        dto.setBalance(card.getBalance());

        if (card.getUser() != null) {
            dto.setUserId(card.getUser().getId());
            dto.setUsername(card.getUser().getUsername());
        }

        dto.setCardNumberMasked(maskCardNumber(card.getCardNumber()));

        return dto;
    }

    private String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) {
            return "****";
        }
        String lastFourDigits = cardNumber.substring(cardNumber.length() - 4);
        return "**** **** **** " + lastFourDigits;
    }
}