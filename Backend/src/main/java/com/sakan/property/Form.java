package com.sakan.property;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "forms")public class Form {
    @Id
    @GeneratedValue
    private Integer id;

    @Column
    private String name;
    private String email;
    private String subject;
    private String message;
    private Date date;
}
