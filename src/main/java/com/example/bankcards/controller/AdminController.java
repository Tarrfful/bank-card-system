package com.example.bankcards.controller;

import com.example.bankcards.dto.CardResponseDto;
import com.example.bankcards.dto.CreateCardRequestDto;
import com.example.bankcards.dto.UpdateCardStatusRequestDto;
import com.example.bankcards.dto.UserResponseDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.service.CardService;
import com.example.bankcards.service.UserService;
import com.example.bankcards.util.CardMapper;
import com.example.bankcards.util.UserMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin")
@Tag(name = "Admin Controller", description = "Endpoints for administrative operations (Requires ADMIN role)")
public class AdminController {

    private final CardService cardService;
    private final CardMapper cardMapper;
    private final UserService userService;
    private final UserMapper userMapper;

    public AdminController(CardService cardService, CardMapper cardMapper, UserService userService, UserMapper userMapper) {
        this.cardService = cardService;
        this.cardMapper = cardMapper;
        this.userService = userService;
        this.userMapper = userMapper;
    }

    @Operation(summary = "Create a new bank card", description = "Creates a new bank card for a specified user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Card created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "User to own the card not found"),
            @ApiResponse(responseCode = "409", description = "Card with this number already exists")
    })
    @PostMapping("/cards")
    public ResponseEntity<CardResponseDto> createCard(@Valid @RequestBody CreateCardRequestDto requestDto) {
        Card newCard = cardService.createCard(requestDto);

        CardResponseDto responseDto = cardMapper.toDto(newCard);

        return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
    }

    @Operation(summary = "Update status of any card", description = "Updates the status of a specific card (e.g., from ACTIVE to BLOCKED).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Status updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid status provided"),
            @ApiResponse(responseCode = "404", description = "Card not found")
    })
    @PatchMapping("/cards/{cardId}/status")
    public ResponseEntity<CardResponseDto> updateCardStatus(
            @PathVariable Long cardId,
            @Valid @RequestBody UpdateCardStatusRequestDto requestDto) {

        Card updatedCard = cardService.updateCardStatusByAdmin(cardId, requestDto.getNewStatus());
        CardResponseDto responseDto = cardMapper.toDto(updatedCard);

        return ResponseEntity.ok(responseDto);
    }

    @Operation(summary = "Get all cards with filtering", description = "Returns a paginated list of all cards in the system. Can be filtered by user ID and/or card status.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list")
    })
    @GetMapping("/cards")
    public ResponseEntity<Page<CardResponseDto>> getAllCards(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) CardStatus status,
            Pageable pageable) {

        Page<Card> cardPage = cardService.getAllCards(userId, status, pageable);
        Page<CardResponseDto> responseDtoPage = cardMapper.toDtoPage(cardPage);

        return ResponseEntity.ok(responseDtoPage);
    }

    @Operation(summary = "Delete a card", description = "Permanently deletes a card from the system.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Card deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Card not found")
    })
    @DeleteMapping("/cards/{cardId}")
    public ResponseEntity<Void> deleteCard(@PathVariable Long cardId) {
        cardService.deleteCard(cardId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get all users", description = "Returns a paginated list of all users in the system.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list")
    })
    @GetMapping("/users")
    public ResponseEntity<Page<UserResponseDto>> getAllUsers(Pageable pageable) {
        Page<User> userPage = userService.getAllUsers(pageable);
        Page<UserResponseDto> dtoPage = userPage.map(userMapper::toDto);
        return ResponseEntity.ok(dtoPage);
    }

    @Operation(summary = "Grant ADMIN role to a user", description = "Adds the ADMIN role to a user's permissions.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Role granted successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PatchMapping("/users/{userId}/grant-admin")
    public ResponseEntity<UserResponseDto> grantAdminRole(@PathVariable Long userId) {
        User updatedUser = userService.assignAdminRole(userId);
        return ResponseEntity.ok(userMapper.toDto(updatedUser));
    }

    @Operation(summary = "Revoke ADMIN role from a user", description = "Removes the ADMIN role from a user's permissions.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Role revoked successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PatchMapping("/users/{userId}/revoke-admin")
    public ResponseEntity<UserResponseDto> revokeAdminRole(@PathVariable Long userId) {
        User updatedUser = userService.removeAdminRole(userId);
        return ResponseEntity.ok(userMapper.toDto(updatedUser));
    }
}