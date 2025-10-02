package com.example.bankcards.repository;

import com.example.bankcards.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void whenFindByUsername_thenReturnUser() {
        User testUser = new User();
        testUser.setUsername("testuser");
        testUser.setPassword("password");
        userRepository.save(testUser);

        Optional<User> foundUserOptional = userRepository.findByUsername("testuser");

        assertThat(foundUserOptional).isPresent();
        assertThat(foundUserOptional.get().getUsername()).isEqualTo("testuser");
    }
}