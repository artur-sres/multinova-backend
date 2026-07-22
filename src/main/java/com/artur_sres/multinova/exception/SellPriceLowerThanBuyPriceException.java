package com.artur_sres.multinova.exception;

public class SellPriceLowerThanBuyPriceException extends RuntimeException {
    public SellPriceLowerThanBuyPriceException(String message) {
        super(message);
    }
}
