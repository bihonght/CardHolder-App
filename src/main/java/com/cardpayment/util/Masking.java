package com.cardpayment.util;

public final class Masking {
    private Masking(){}

    public static String maskLast4(String last4) {
        // **** **** 1234
        return "**** **** " + last4;
    }
}
