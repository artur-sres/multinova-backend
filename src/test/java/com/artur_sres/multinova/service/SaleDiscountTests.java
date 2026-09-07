package com.artur_sres.multinova.service;

import com.artur_sres.multinova.domain.enums.DiscountType;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class SaleDiscountTests {
    @Test void percentageRoundsOnceOnTheLineAndAmountIsNotMultipliedByQuantity() {
        assertEquals(new BigDecimal("2.50"), SaleDiscount.amount(new BigDecimal("25"), DiscountType.PERCENTAGE, new BigDecimal("10")));
        assertEquals(new BigDecimal("3.00"), SaleDiscount.amount(new BigDecimal("25"), DiscountType.AMOUNT, new BigDecimal("3")));
        assertEquals(new BigDecimal("0.02"), SaleDiscount.amount(new BigDecimal("0.15"), DiscountType.PERCENTAGE, new BigDecimal("10")));
        assertEquals(new BigDecimal("25.00"), SaleDiscount.amount(new BigDecimal("25"), DiscountType.PERCENTAGE, new BigDecimal("100")));
        assertEquals(new BigDecimal("0.00"), SaleDiscount.amount(BigDecimal.ZERO, DiscountType.NONE, BigDecimal.ZERO));
    }

    @Test void invalidDiscountsAreRejected() {
        for (String value : new String[]{"-1", "101", "1.001"}) {
            assertThrows(ResponseStatusException.class, () -> SaleDiscount.amount(new BigDecimal("25"), DiscountType.PERCENTAGE, new BigDecimal(value)));
        }
        assertThrows(ResponseStatusException.class, () -> SaleDiscount.amount(new BigDecimal("25"), DiscountType.AMOUNT, new BigDecimal("25.01")));
        assertThrows(ResponseStatusException.class, () -> SaleDiscount.amount(new BigDecimal("25"), DiscountType.NONE, BigDecimal.ONE));
        assertThrows(ResponseStatusException.class, () -> SaleDiscount.amount(new BigDecimal("25"), null, BigDecimal.ZERO));
    }
}
