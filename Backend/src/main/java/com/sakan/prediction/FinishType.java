package com.sakan.prediction;

import lombok.Getter;

@Getter
public enum FinishType {
    LUX("Lux"),
    SUPER_LUX("Super Lux"),
    EXTRA_SUPER_LUX("Extra Super Lux"),
    SEMI_FINISHED("Semi Finished"),
    WITHOUT_FINISH("Without Finish");

    private final String value;

    FinishType(String value) {
        this.value = value;
    }
}
