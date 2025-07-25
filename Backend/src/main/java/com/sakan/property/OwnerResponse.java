package com.sakan.property;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OwnerResponse {
    private String message;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
}
