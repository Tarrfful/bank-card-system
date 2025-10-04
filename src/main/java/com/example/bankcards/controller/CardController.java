package com.example.bankcards.controller;

import com.example.bankcards.dto.CardResponseDto;
import com.example.bankcards.dto.CardTransferRequestDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.CardService;
import com.example.bankcards.util.CardMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/cards")
@Tag(name = "Card Controller", description = "Endpoints for current user's card operations (Requires authentication)")
public class CardController {
    private final CardService cardService;
    private final CardMapper cardMapper;
    private final UserRepository userRepository;;

    public CardController(CardService cardService, CardMapper cardMapper, UserRepository userRepository) {
        this.cardService = cardService;
        this.cardMapper = cardMapper;
        this.userRepository = userRepository;
    }

    @Operation(summary = "Get my cards", description = "Returns a paginated list of cards owned by the currently authenticated user. Can be filtered by status.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list")
    })
    @GetMapping
    public ResponseEntity<Page<CardResponseDto>> getMyCards(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) CardStatus status,
            Pageable pageable) {

        String username = userDetails.getUsername();

        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Authenticated user not found in database"));

        Page<Card> cardPage = cardService.getCardsByUserId(currentUser.getId(), status, pageable);

        Page<CardResponseDto> responseDtoPage = cardMapper.toDtoPage(cardPage);

        return ResponseEntity.ok(responseDtoPage);
    }

    @Operation(summary = "Block my card", description = "Requests to block a specific card owned by the current user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Card blocked successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden - User does not own this card"),
            @ApiResponse(responseCode = "404", description = "Card not found")
    })
    @PatchMapping("/{cardId}/block")
    public ResponseEntity<CardResponseDto> blockMyCard(
            @PathVariable Long cardId,
            @AuthenticationPrincipal UserDetails userDetails) {

        Card blockedCard = cardService.blockCard(cardId, userDetails.getUsername());

        CardResponseDto responseDto = cardMapper.toDto(blockedCard);

        return ResponseEntity.ok(responseDto);
    }

    @Operation(summary = "Transfer money between my cards", description = "Performs a money transfer between two cards owned by the current user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transfer successful"),
            @ApiResponse(responseCode = "400", description = "Bad request (e.g., insufficient funds, card not active)"),
            @ApiResponse(responseCode = "403", description = "Forbidden - User does not own one of the cards"),
            @ApiResponse(responseCode = "404", description = "One of the cards not found")
    })
    @PostMapping("/transfer")
    public ResponseEntity<Void> transferMoney(
            @Valid @RequestBody CardTransferRequestDto requestDto,
            @AuthenticationPrincipal UserDetails userDetails) {

        cardService.transferBetweenCards(requestDto, userDetails.getUsername());

        return ResponseEntity.ok().build();
    }
}
