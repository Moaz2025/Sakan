package com.sakan.auth;

import com.sakan.user.UserRepository;
import com.sakan.validation.EmailValidator;
import com.sakan.validation.PasswordValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthenticationController {
    private final AuthenticationService authenticationService;
    private final UserRepository userRepository;
    private final EmailValidator emailValidator;
    private final PasswordValidator passwordValidator;

    @PostMapping("/signUp")
    public ResponseEntity<AuthenticationResponse> signUp(@RequestBody SignUpRequest signUpRequest) {
        if (!emailValidator.isValid(signUpRequest.getEmail()))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new AuthenticationResponse("Invalid Email"));

        if (!passwordValidator.isValid(signUpRequest.getPassword()))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new AuthenticationResponse("Invalid Password"));

        if (userRepository.findByEmail(signUpRequest.getEmail()).isPresent())
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new AuthenticationResponse("Email Already Exist"));

        return ResponseEntity.ok(authenticationService.signUp(signUpRequest));
    }

    @PostMapping("/signIn")
    public ResponseEntity<AuthenticationResponse> signIn(@RequestBody SignInRequest signInRequest) {
        return ResponseEntity.ok(authenticationService.signIn(signInRequest));
    }
}
