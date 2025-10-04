package com.example.bankcards.service;

import com.example.bankcards.dto.CardTransferRequestDto;
import com.example.bankcards.dto.CreateCardRequestDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.CardAlreadyExistsException;
import com.example.bankcards.exception.CardNotFoundException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceImplTest {

    @Mock
    private CardRepository cardRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CardServiceImpl cardService;

    private User testUser;
    private Card sourceCard;
    private Card destinationCard;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");

        sourceCard = new Card();
        sourceCard.setId(10L);
        sourceCard.setUser(testUser);
        sourceCard.setStatus(CardStatus.ACTIVE);
        sourceCard.setBalance(new BigDecimal("1000.00"));

        destinationCard = new Card();
        destinationCard.setId(20L);
        destinationCard.setUser(testUser);
        destinationCard.setStatus(CardStatus.ACTIVE);
        destinationCard.setBalance(new BigDecimal("500.00"));
    }

    @Test
    void whenTransferSuccessful_thenBalancesShouldBeUpdated() {
        CardTransferRequestDto requestDto = new CardTransferRequestDto();
        requestDto.setFromCardId(10L);
        requestDto.setToCardId(20L);
        requestDto.setAmount(new BigDecimal("100.00"));

        when(cardRepository.findById(10L)).thenReturn(Optional.of(sourceCard));
        when(cardRepository.findById(20L)).thenReturn(Optional.of(destinationCard));

        cardService.transferBetweenCards(requestDto, "testuser");

        assertEquals(new BigDecimal("900.00"), sourceCard.getBalance());
        assertEquals(new BigDecimal("600.00"), destinationCard.getBalance());
        verify(cardRepository, times(2)).save(any(Card.class));
    }

    @Test
    void whenInsufficientFunds_thenThrowIllegalStateException() {
        CardTransferRequestDto requestDto = new CardTransferRequestDto();
        requestDto.setFromCardId(10L);
        requestDto.setToCardId(20L);
        requestDto.setAmount(new BigDecimal("2000.00"));

        when(cardRepository.findById(10L)).thenReturn(Optional.of(sourceCard));
        when(cardRepository.findById(20L)).thenReturn(Optional.of(destinationCard));

        assertThrows(IllegalStateException.class, () -> {
            cardService.transferBetweenCards(requestDto, "testuser");
        });

        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    void whenUserDoesNotOwnCard_thenThrowAccessDeniedException() {
        CardTransferRequestDto requestDto = new CardTransferRequestDto();
        requestDto.setFromCardId(10L);
        requestDto.setToCardId(20L);
        requestDto.setAmount(new BigDecimal("100.00"));

        when(cardRepository.findById(10L)).thenReturn(Optional.of(sourceCard));
        when(cardRepository.findById(20L)).thenReturn(Optional.of(destinationCard));

        assertThrows(AccessDeniedException.class, () -> {
            cardService.transferBetweenCards(requestDto, "anotherUser");
        });
    }

    @Test
    void whenSourceCardNotFound_thenThrowCardNotFoundException() {
        CardTransferRequestDto requestDto = new CardTransferRequestDto();
        requestDto.setFromCardId(99L);
        requestDto.setToCardId(20L);
        requestDto.setAmount(new BigDecimal("100.00"));

        when(cardRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(CardNotFoundException.class, () -> {
            cardService.transferBetweenCards(requestDto, "testuser");
        });
    }

    @Test
    void whenCreateCard_andUserExists_andCardNumberIsUnique_thenCardShouldBeSaved() {
        CreateCardRequestDto request = new CreateCardRequestDto();
        request.setCardNumber("1111222233334444");
        request.setUserId(1L);
        request.setExpiryDate(LocalDate.now().plusYears(1));

        when(cardRepository.existsByCardNumber(request.getCardNumber())).thenReturn(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        when(cardRepository.save(any(Card.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Card savedCard = cardService.createCard(request);

        assertNotNull(savedCard);
        assertEquals(CardStatus.ACTIVE, savedCard.getStatus());
        assertEquals(BigDecimal.ZERO, savedCard.getBalance());
        assertEquals("1111222233334444", savedCard.getCardNumber());
        verify(cardRepository, times(1)).save(any(Card.class));
    }

    @Test
    void whenCreateCard_andCardNumberExists_thenThrowCardAlreadyExistsException() {
        CreateCardRequestDto request = new CreateCardRequestDto();
        request.setCardNumber("1111222233334444");
        request.setUserId(1L);

        when(cardRepository.existsByCardNumber(request.getCardNumber())).thenReturn(true);

        assertThrows(CardAlreadyExistsException.class, () -> {
            cardService.createCard(request);
        });

        verify(userRepository, never()).findById(anyLong());
        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    void whenBlockCard_andUserIsOwner_thenStatusShouldBeBlocked() {
        when(cardRepository.findById(10L)).thenReturn(Optional.of(sourceCard));

        cardService.blockCard(10L, "testuser");

        assertEquals(CardStatus.BLOCKED, sourceCard.getStatus());
        verify(cardRepository, times(1)).save(sourceCard);
    }
}