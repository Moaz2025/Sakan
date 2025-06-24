package com.sakan.property;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "property_location")
public class Location {
    @Id
    @GeneratedValue
    private Integer id;

    @OneToOne
    @JoinColumn(name = "property_id")
    private Property property;

    private String streetAddress;

    private String city;

    private String state;

    private String country;

    private String postalCode;
}
