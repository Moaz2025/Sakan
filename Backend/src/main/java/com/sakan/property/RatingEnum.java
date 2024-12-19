package com.sakan.property;

public enum RatingEnum {
    ONE(1),
    TWO(2),
    THREE(3),
    FOUR(4),
    FIVE(5);

    private int value;

    RatingEnum(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static int rate(int value) {
        for (RatingEnum rating : values()) {
            if (rating.getValue() == value) {
                return value;
            }
        }
        throw new IllegalArgumentException("Invalid Rating value: " + value);
    }
}