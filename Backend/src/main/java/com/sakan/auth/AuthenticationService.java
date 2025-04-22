package com.sakan.auth;

import com.sakan.config.JwtService;
import com.sakan.token.Token;
import com.sakan.token.TokenRepository;
import com.sakan.token.TokenType;
import com.sakan.user.Role;
import com.sakan.user.User;
import com.sakan.user.UserRepository;
import com.sakan.validation.EmailValidator;
import com.sakan.validation.PasswordValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final EmailValidator emailValidator;
    private final PasswordValidator passwordValidator;

    public AuthenticationResponse signUp(SignUpRequest signUpRequest, Role role) {
        if (!emailValidator.isValid(signUpRequest.getEmail()))
            throw new IllegalArgumentException("Invalid Email");

        if (userRepository.findByEmail(signUpRequest.getEmail()).isPresent())
            throw new IllegalArgumentException("User Already Exists");

        if (!passwordValidator.isValid(signUpRequest.getPassword()))
            throw new IllegalArgumentException("Invalid Password");

        var user = User.builder()
                .firstName(signUpRequest.getFirstName())
                .lastName(signUpRequest.getLastName())
                .email(signUpRequest.getEmail())
                .password(passwordEncoder.encode(signUpRequest.getPassword()))
                .phoneNumber(signUpRequest.getPhoneNumber())
                .role(role)
                .build();
        var savedUser = userRepository.save(user);
        var jwtToken = jwtService.generateToken(user);
        saveUserToken(savedUser, jwtToken);
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }

    public AuthenticationResponse signIn(SignInRequest signInRequest) {
        var user = userRepository.findByEmail(signInRequest.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            signInRequest.getEmail(),
                            signInRequest.getPassword()
                    )
            );
        } catch (Exception e) {
            throw new IllegalArgumentException("Incorrect password");
        }

        var jwtToken = jwtService.generateToken(user);

        revokeAllUserTokens(user);
        saveUserToken(user, jwtToken);

        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }

    private void revokeAllUserTokens(User user) {
        var validUserTokens = tokenRepository.findAllValidTokensByUser(user.getId());

        if (!validUserTokens.isEmpty()) {
            validUserTokens.forEach(token -> {
                token.setExpired(true);
                token.setRevoked(true);
            });
            tokenRepository.saveAll(validUserTokens);
        }
    }

    private void saveUserToken(User user, String jwtToken) {
        var token = Token.builder()
                .user(user)
                .token(jwtToken)
                .tokenType(TokenType.BEARER)
                .expired(false)
                .revoked(false)
                .build();
        tokenRepository.save(token);
    }
}
