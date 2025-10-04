package com.example.bankcards.controller;

import com.example.bankcards.dto.CardResponseDto;
import com.example.bankcards.dto.CreateCardRequestDto;
import com.example.bankcards.dto.UpdateCardStatusRequestDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.service.CardService;
import com.example.bankcards.util.CardMapper;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    private final CardService cardService;
    private final CardMapper cardMapper;

    public AdminController(CardService cardService, CardMapper cardMapper) {
        this.cardService = cardService;
        this.cardMapper = cardMapper;
    }

    @PostMapping("/cards")
    public ResponseEntity<CardResponseDto> createCard(@Valid @RequestBody CreateCardRequestDto requestDto) {
        Card newCard = cardService.createCard(requestDto);

        CardResponseDto responseDto = cardMapper.toDto(newCard);

        return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
    }

    @PatchMapping("/cards/{cardId}/status")
    public ResponseEntity<CardResponseDto> updateCardStatus(
            @PathVariable Long cardId,
            @Valid @RequestBody UpdateCardStatusRequestDto requestDto) {

        Card updatedCard = cardService.updateCardStatusByAdmin(cardId, requestDto.getNewStatus());
        CardResponseDto responseDto = cardMapper.toDto(updatedCard);

        return ResponseEntity.ok(responseDto);
    }

    @GetMapping("/cards")
    public ResponseEntity<Page<CardResponseDto>> getAllCards(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) CardStatus status,
            Pageable pageable) {

        Page<Card> cardPage = cardService.getAllCards(userId, status, pageable);
        Page<CardResponseDto> responseDtoPage = cardMapper.toDtoPage(cardPage);

        return ResponseEntity.ok(responseDtoPage);
    }

    @DeleteMapping("/cards/{cardId}")
    public ResponseEntity<Void> deleteCard(@PathVariable Long cardId) {
        cardService.deleteCard(cardId);
        return ResponseEntity.noContent().build();
    }
}