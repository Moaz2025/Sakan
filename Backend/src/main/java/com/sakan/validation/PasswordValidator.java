package com.sakan.validation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PasswordValidator implements IValidator{
    private static final String specialCharacters = "!@#$%^&*()-_=+[]{}|;:'\",.<>?/";

    @Override
    public boolean isValid(String password) {
        if (password.length() < 8)
            return false;
        return checkPasswordContainsCharacter(password) && checkPasswordContainsSpecialCharacters(password);
    }

    private boolean checkPasswordContainsSpecialCharacters(String password) {
        for (char c : password.toCharArray()) {
            if (specialCharacters.indexOf(c) == -1)
                return true;
        }
        return false;
    }

    private boolean checkPasswordContainsCharacter(String password) {
        for (char c : password.toCharArray()) {
            if (Character.isLetter(c))
                return true;
        }
        return false;
    }
}
