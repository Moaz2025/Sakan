package com.sakan.user;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/users")
@CrossOrigin(origins = "http://localhost:3000")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PatchMapping("/change-password")
    public ResponseEntity changePassword(
            @RequestBody ChangePasswordRequest changePasswordRequest,
            Principal connectedUser
    ) throws IllegalArgumentException {
        String response = userService.changePassword(changePasswordRequest, connectedUser);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/user-data")
    public ResponseEntity getUserData(Principal principal) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.getUserData(principal));
    }
}