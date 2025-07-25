package com.sakan.property;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PropertyRequest {
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
    private String streetAddress;
    private String city;
    private String state;
    private String country;
    private String postalCode;
}
