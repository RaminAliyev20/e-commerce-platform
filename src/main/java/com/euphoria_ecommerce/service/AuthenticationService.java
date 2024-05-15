package com.euphoria_ecommerce.service;

import com.euphoria_ecommerce.dto.LoginRequest;
import com.euphoria_ecommerce.dto.RegisterRequest;
import com.euphoria_ecommerce.exception.UserAlreadyExistsException;
import com.euphoria_ecommerce.model.User;
import com.euphoria_ecommerce.repository.UserRepository;
import com.euphoria_ecommerce.security.AuthenticationResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthenticationService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthenticationService(UserRepository repository, PasswordEncoder passwordEncoder, JwtService jwtService, AuthenticationManager authenticationManager) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    public AuthenticationResponse register(RegisterRequest request) {
        User user = new User();
        Optional<User> user1 = repository.findByEmail(request.email());
        if (user1.isPresent())
            throw new UserAlreadyExistsException("User already exists with Email: " + request.email());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(request.role());
        user = repository.save(user);
        String token = jwtService.generateToken(user);
        return new AuthenticationResponse(token);
    }

    public boolean emailExists(String email) {
        Optional<User> user = repository.findByEmail(email);
        return user.isPresent();
    }

    public AuthenticationResponse authenticate(LoginRequest request) {
        if (!emailExists(request.email()))
            throw new UsernameNotFoundException("User not exist");

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                )
        );
        User user = repository.findByEmail(request.email()).orElseThrow();
        String token = jwtService.generateToken(user);
        return new AuthenticationResponse(token);
    }
}
