package com.sakan.property;

import lombok.Data;

import java.util.Date;

@Data
public class FormResponse {
    private String email;
    private String subject;
    private String message;
    private Date date;
}
