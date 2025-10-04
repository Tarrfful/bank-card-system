package com.example.bankcards.service;

import com.example.bankcards.dto.CreateCardRequestDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.CardAlreadyExistsException;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

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
    public Page<Card> getCardsByUserId(Long userId, Pageable pageable){
        return cardRepository.findByUserId(userId, pageable);
    }
}