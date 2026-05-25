package com.restaurant.api.constant;

public enum EmailVerificationStatus {
    PENDING,   // Email sent, PIN not yet verified
    VERIFIED,  // PIN verified successfully
    USED,      // Final action (e.g. account creation) consumed the verification
    EXPIRED    // 10-minute window elapsed
}
