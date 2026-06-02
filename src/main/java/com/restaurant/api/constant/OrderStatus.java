package com.restaurant.api.constant;

public enum OrderStatus {
    PENDING,
    CONFIRMED,
    PREPARING,
    READY,
    SERVED,     // food delivered; customer still seated, table stays occupied
    COMPLETED,  // order closed/paid; table is freed
    CANCELLED
}
