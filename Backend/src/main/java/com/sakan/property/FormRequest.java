package com.sakan.property;

import lombok.Data;

@Data
public class FormRequest {
    private String name;
    private String email;
    private String subject;
    private String message;
}
