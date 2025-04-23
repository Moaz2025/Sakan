package com.sakan.prediction;

import lombok.Getter;

@Getter
public enum View {
    OTHER("Other"),
    MAIN_STREET("Main Street"),
    GARDEN("Garden"),
    SIDE_STREET("Side Street"),
    CORNER("Corner"),
    SEA_VIEW("Sea View"),
    POOL("Pool"),
    BACK("Back"),
    NILE_VIEW("Nile View"),
    GOLF_COURSE("Golf Course");

    private final String value;

    View(String value) {
        this.value = value;
    }
}
