package com.sakan.property;

import lombok.Data;

import java.util.Date;

@Data
public class PropertyRequest {
    private int userId;
    private String title;
    private String description;
    private SaleStatus saleStatus;
    private int price;
    private PropertyType propertyType;
    private float size;
    private int numberOfRooms;
    private int numberOfBathrooms;
    private int floorNumber;
    private AvailabilityStatus availabilityStatus;
    private int buildingYear;
    private Date listingDate;
}
