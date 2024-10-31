package com.sakan.property;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
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
    private Date listingDate;
    private String streetAddress;
    private String city;
    private String state;
    private String country;
    private String postalCode;
    private List<String> imagesUrls;
}
