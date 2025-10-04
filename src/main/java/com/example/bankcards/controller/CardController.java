package com.example.bankcards.controller;

import com.example.bankcards.dto.CardResponseDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.CardService;
import com.example.bankcards.util.CardMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/cards")
public class CardController {
    private final CardService cardService;
    private final CardMapper cardMapper;
    private final UserRepository userRepository;;

    public CardController(CardService cardService, CardMapper cardMapper, UserRepository userRepository) {
        this.cardService = cardService;
        this.cardMapper = cardMapper;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<Page<CardResponseDto>> getMyCards(
            @AuthenticationPrincipal UserDetails userDetails,
            Pageable pageable) {

        String username = userDetails.getUsername();

        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Authenticated user not found in database"));

        Page<Card> cardPage = cardService.getCardsByUserId(currentUser.getId(), pageable);

        Page<CardResponseDto> responseDtoPage = cardMapper.toDtoPage(cardPage);

        return ResponseEntity.ok(responseDtoPage);
    }
}
