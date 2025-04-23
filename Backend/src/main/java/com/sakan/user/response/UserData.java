package com.sakan.user.response;

import com.sakan.user.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserData {
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private Role role;
}
