package com.sakan.user;

import com.sakan.user.response.UserData;
import com.sakan.validation.PasswordValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.Principal;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordValidator passwordValidator;

    public String changePassword(
            ChangePasswordRequest changePasswordRequest,
            Principal connectedUser
    ) throws IllegalArgumentException {
        var user = (User) ((UsernamePasswordAuthenticationToken) connectedUser).getPrincipal();

        // Check if the current password is correct
        if (!passwordEncoder.matches(changePasswordRequest.getCurrentPassword(), user.getPassword()))
            throw new IllegalArgumentException("Wrong Password");

        // Check if the new password matches the standard format
        if (!passwordValidator.isValid(changePasswordRequest.getNewPassword()))
            throw new IllegalArgumentException("Invalid Password Format");

        // Check if the new password equals the current password
        if (changePasswordRequest.getCurrentPassword().equals(changePasswordRequest.getNewPassword()))
            throw new IllegalArgumentException("New password is the same as the current password");

        // Check if the new password matches the confirmation password
        if (!changePasswordRequest.getNewPassword().equals(changePasswordRequest.getConfirmPassword()))
            throw new IllegalArgumentException("New Password does not match Confirm Password");

        // Update the password
        user.setPassword(passwordEncoder.encode(changePasswordRequest.getNewPassword()));

        // Save the user with new password in the database
        userRepository.save(user);

        return "Password Changed Successfully";
    }

    public UserData getUserData(Principal principal) {
        var user = (User) ((UsernamePasswordAuthenticationToken) principal).getPrincipal();

        return UserData
                .builder()
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole())
                .build();
    }
}