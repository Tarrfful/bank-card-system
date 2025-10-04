package com.example.bankcards.service;

import com.example.bankcards.dto.CardTransferRequestDto;
import com.example.bankcards.dto.CreateCardRequestDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CardService {
    Card createCard(CreateCardRequestDto requestDto);

    Page<Card> getCardsByUserId(Long userId, Pageable pageable);

    Card blockCard(Long cardId, String username);

    Card updateCardStatusByAdmin(Long cardId, CardStatus newStatus);

    void transferBetweenCards(CardTransferRequestDto requestDto, String username);
}