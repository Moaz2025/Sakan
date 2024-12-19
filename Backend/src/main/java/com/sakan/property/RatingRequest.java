package com.sakan.property;

import lombok.Data;

@Data
public class RatingRequest {
    private int propertyId;
    private int rate;
}
