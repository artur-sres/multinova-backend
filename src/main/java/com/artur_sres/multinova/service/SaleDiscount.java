package com.artur_sres.multinova.service;

import com.artur_sres.multinova.domain.enums.DiscountType;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

final class SaleDiscount {
    private SaleDiscount() {}

    static BigDecimal amount(BigDecimal gross, DiscountType type, BigDecimal value) {
        if (type == null || value == null || value.signum() < 0 || value.stripTrailingZeros().scale() > 2
                || (type == DiscountType.NONE && value.signum() != 0)
                || (type == DiscountType.PERCENTAGE && value.compareTo(new BigDecimal("100")) > 0)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe um desconto válido, com até duas casas decimais e porcentagem de até 100%.");
        }
        BigDecimal amount = switch (type) {
            case NONE -> BigDecimal.ZERO;
            case AMOUNT -> value;
            case PERCENTAGE -> gross.multiply(value).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        };
        if (amount.compareTo(gross) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O desconto não pode superar o total do produto.");
        }
        return amount.setScale(2, RoundingMode.HALF_UP);
    }
}
