package com.example.bankcards.service;

import com.example.bankcards.dto.CreateCardRequestDto;
import com.example.bankcards.entity.Card;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CardService {
    Card createCard(CreateCardRequestDto requestDto);

    Page<Card> getCardsByUserId(Long userId, Pageable pageable);
}