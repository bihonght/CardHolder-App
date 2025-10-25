package com.cardpayment.model;

import java.time.OffsetDateTime;

public record CardRecord(
        String name,
        String last4,
        OffsetDateTime createdAt
) {}
