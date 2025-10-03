package com.example.bankcards.service;

import com.example.bankcards.dto.CreateCardRequestDto;
import com.example.bankcards.entity.Card;

public interface CardService {
    Card createCard(CreateCardRequestDto requestDto);
}