package com.egovoryn.exchanger.DTOs;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import java.math.BigDecimal;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class ExchangeRequest extends DataTransferObject {
    private final String baseCurrencyCode;
    private final String targetCurrencyCode;
    private final BigDecimal amount;

    public ExchangeRequest(String baseCurrency, String targetCurrency, BigDecimal amount) {
        this.baseCurrencyCode = baseCurrency;
        this.targetCurrencyCode = targetCurrency;
        this.amount = amount;
    }
    @Override
    public String toString() {
        return "ExchangeRates{" +
                ", baseCurrency=" + baseCurrencyCode +
                ", targetCurrency=" + targetCurrencyCode +
                ", amount=" + amount +
                '}';
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getBaseCurrencyCode() {
        return baseCurrencyCode;
    }

    public String getTargetCurrencyCode() {
        return targetCurrencyCode;
    }
}

