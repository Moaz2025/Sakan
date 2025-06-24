package com.sakan.prediction;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PredictionRequest {
    private String region;
    private double netHabitableSurface;
    private int rooms;
    private int bathrooms;
    private String finish_type;
    private String type;
    private String view;
    private int floor;
    private int building_year;
}

