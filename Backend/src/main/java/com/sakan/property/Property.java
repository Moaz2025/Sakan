package com.sakan.property;

import com.sakan.user.User;
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
@Table(name = "property")
public class Property {
    @Id
    @GeneratedValue
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "userId")
    private User user;

    private String title;

    @Enumerated(EnumType.STRING)
    private SaleStatus saleStatus;

    private int price;

    @Enumerated(EnumType.STRING)
    private PropertyType propertyType;

    private float size;

    private int numberOfRooms;

    private int numberOfBathrooms;

    private int floorNumber;

    @Enumerated(EnumType.STRING)
    private AvailabilityStatus availabilityStatus;

    private int buildingYear;

    private Date listingDate;
}
