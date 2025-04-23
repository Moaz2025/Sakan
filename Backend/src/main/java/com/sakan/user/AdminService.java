package com.sakan.user;

import com.sakan.auth.AuthenticationService;
import com.sakan.auth.SignUpRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import java.security.Principal;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final AuthenticationService authenticationService;

    public void assignNewAdmin(
            SignUpRequest request,
            Principal principal
    ) {
        var user = (User) ((UsernamePasswordAuthenticationToken) principal).getPrincipal();
        if (!user.getRole().equals(Role.ADMIN)) {
            throw new IllegalArgumentException("You are not an admin");
        }

        authenticationService.signUp(request, Role.ADMIN);
    }
}
