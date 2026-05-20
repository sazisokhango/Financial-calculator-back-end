package com.psybergate.financialcalculator.service;

import com.psybergate.financialcalculator.dto.RegisterRequest;
import com.psybergate.financialcalculator.dto.UserResponse;
import com.psybergate.financialcalculator.entity.User;
import com.psybergate.financialcalculator.exception.EmailAlreadyRegisteredException;
import com.psybergate.financialcalculator.exception.UserNotFoundException;
import com.psybergate.financialcalculator.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

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

        return toResponse(saved);
    }

    public List<UserResponse> findAll() {
        return userRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public UserResponse findById(Long id) {
        return userRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    private UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .build();
    }
}
