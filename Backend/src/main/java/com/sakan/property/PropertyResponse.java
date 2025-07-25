package com.sakan.property;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PropertyResponse {
    private String message;
    private int id;
    private String title;
    private String description;
    private String saleStatus;
    private int price;
    private String propertyType;
    private float size;
    private int numberOfRooms;
    private int numberOfBathrooms;
    private int floorNumber;
    private String availabilityStatus;
    private int buildingYear;
    private Date listingDate;
    private int views;
    private String streetAddress;
    private String city;
    private String state;
    private String country;
    private String postalCode;
    private List<String> imagesUrls;
}
