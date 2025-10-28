package com.cardpayment;

import com.cardpayment.util.Masking;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MaskingTest {

    @Test
    void maskLast4_formatsCorrectly() {
        assertEquals("**** **** 1234", Masking.maskLast4("1234"));
    }

    @Test
    void maskLast4_handlesEmptyOrNull() {
        assertEquals("**** **** null", Masking.maskLast4(null));
    }
}
