package com.example.demo.DTOs;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import java.math.BigDecimal;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class Exchange {
    private final Currency baseCurrency;
    private final Currency targetCurrency;
    private BigDecimal rate;
    private final BigDecimal amount;
    private final BigDecimal convertedAmount;



    public Exchange(ExchangeRates exRates, BigDecimal amount, BigDecimal convertedAmount) {
        this.baseCurrency = exRates.getBaseCurrency();
        this.targetCurrency = exRates.getTargetCurrency();
        this.rate = exRates.getRate();

        this.amount = amount;
        this.convertedAmount = convertedAmount;
    }

    public void setRate(BigDecimal rate) {
        this.rate = rate;
    }

    @Override
    public String toString() {
        return "Exchange{" +
                "baseCurrency=" + baseCurrency +
                ", targetCurrency=" + targetCurrency +
                ", rate=" + rate +
                ", amount=" + amount +
                ", convertedAmount=" + convertedAmount +
                '}';
    }
}
