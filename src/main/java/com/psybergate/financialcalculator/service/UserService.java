package com.psybergate.financialcalculator.service;

import com.psybergate.financialcalculator.dto.RegisterRequest;
import com.psybergate.financialcalculator.dto.UserResponse;
import com.psybergate.financialcalculator.entity.User;
import com.psybergate.financialcalculator.exception.EmailAlreadyRegisteredException;
import com.psybergate.financialcalculator.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public UserResponse register(RegisterRequest request) {
        String email = request.getEmail().toLowerCase();

        if (userRepository.findByEmailIgnoreCase(email).isPresent()) {
            throw new EmailAlreadyRegisteredException("Email already registered");
        }

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(email)
                .build();

        User saved = userRepository.save(user);

        return UserResponse.builder()
                .id(saved.getId())
                .firstName(saved.getFirstName())
                .lastName(saved.getLastName())
                .email(saved.getEmail())
                .build();
    }
}
