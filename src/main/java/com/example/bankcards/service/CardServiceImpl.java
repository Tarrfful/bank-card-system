package com.example.bankcards.service;

import com.example.bankcards.dto.CardTransferRequestDto;
import com.example.bankcards.dto.CreateCardRequestDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.CardAlreadyExistsException;
import com.example.bankcards.exception.CardNotFoundException;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.repository.specifications.CardSpecifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class CardServiceImpl implements CardService {

    private final CardRepository cardRepository;
    private final UserRepository userRepository;

    public CardServiceImpl(CardRepository cardRepository, UserRepository userRepository) {
        this.cardRepository = cardRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public Card createCard(CreateCardRequestDto requestDto) {
        if (cardRepository.existsByCardNumber(requestDto.getCardNumber())) {
            throw new CardAlreadyExistsException("Card with number " + requestDto.getCardNumber() + " already exists.");
        }

        User user = userRepository.findById(requestDto.getUserId())
                .orElseThrow(() -> new UserNotFoundException("User with ID " + requestDto.getUserId() + " not found."));

        Card newCard = new Card();
        newCard.setCardNumber(requestDto.getCardNumber());
        newCard.setExpiryDate(requestDto.getExpiryDate());
        newCard.setUser(user);

        newCard.setBalance(BigDecimal.ZERO);
        newCard.setStatus(CardStatus.ACTIVE);

        return cardRepository.save(newCard);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Card> getCardsByUserId(Long userId, CardStatus status, Pageable pageable) {

        List<Specification<Card>> specs = new ArrayList<>();

        specs.add(CardSpecifications.hasUserId(userId));

        if (status != null) {
            specs.add(CardSpecifications.hasStatus(status));
        }

        Specification<Card> finalSpec = Specification.allOf(specs);

        return cardRepository.findAll(finalSpec, pageable);
    }

    @Override
    @Transactional
    public Card blockCard(Long cardId, String username) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException("Card with ID " + cardId + " not found."));

        if (!card.getUser().getUsername().equals(username)) {
            throw new AccessDeniedException("User does not have permission to modify this card.");
        }

        if (card.getStatus() == CardStatus.BLOCKED) {
            throw new IllegalStateException("Card with ID " + cardId + " is already blocked.");
        }

        card.setStatus(CardStatus.BLOCKED);
        return cardRepository.save(card);
    }

    @Override
    @Transactional
    public Card updateCardStatusByAdmin(Long cardId, CardStatus newStatus) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException("Card with ID " + cardId + " not found."));

        card.setStatus(newStatus);

        return cardRepository.save(card);
    }

    @Override
    @Transactional
    public void transferBetweenCards(CardTransferRequestDto requestDto, String username) {
        Long fromCardId = requestDto.getFromCardId();
        Long toCardId = requestDto.getToCardId();
        BigDecimal amount = requestDto.getAmount();

        if (fromCardId.equals(toCardId)) {
            throw new IllegalArgumentException("Source and destination cards cannot be the same.");
        }

        Card sourceCard = cardRepository.findById(fromCardId)
                .orElseThrow(() -> new CardNotFoundException("Source card with ID " + fromCardId + " not found."));
        Card destinationCard = cardRepository.findById(toCardId)
                .orElseThrow(() -> new CardNotFoundException("Destination card with ID " + toCardId + " not found."));

        if (!sourceCard.getUser().getUsername().equals(username) || !destinationCard.getUser().getUsername().equals(username)) {
            throw new AccessDeniedException("User does not have permission to use one of the cards.");
        }

        if (sourceCard.getStatus() != CardStatus.ACTIVE) {
            throw new IllegalStateException("Source card is not active.");
        }

        if (sourceCard.getBalance().compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient funds on the source card.");
        }

        sourceCard.setBalance(sourceCard.getBalance().subtract(amount));
        destinationCard.setBalance(destinationCard.getBalance().add(amount));

        cardRepository.save(sourceCard);
        cardRepository.save(destinationCard);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Card> getAllCards(Long userId, CardStatus status, Pageable pageable) {

        List<Specification<Card>> specs = new ArrayList<>();

        if (userId != null) {
            specs.add(CardSpecifications.hasUserId(userId));
        }
        if (status != null) {
            specs.add(CardSpecifications.hasStatus(status));
        }

        Specification<Card> finalSpec = Specification.allOf(specs);

        return cardRepository.findAll(finalSpec, pageable);
    }

    @Override
    @Transactional
    public void deleteCard(Long cardId) {
        if (!cardRepository.existsById(cardId)) {
            throw new CardNotFoundException("Card with ID " + cardId + " not found.");
        }
        cardRepository.deleteById(cardId);
    }
}