package com.egovoryn.exchanger.DTOs;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import java.math.BigDecimal;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class ExchangeResult {
    private Currency baseCurrency;
    private Currency targetCurrency;
    private BigDecimal rate;
    private final BigDecimal amount;
    private final BigDecimal convertedAmount;



    public ExchangeResult(ExchangeRates exRates, BigDecimal amount, BigDecimal convertedAmount) {
        this.baseCurrency = exRates.getBaseCurrency();
        this.targetCurrency = exRates.getTargetCurrency();
        this.rate = exRates.getRate();

        this.amount = amount;
        this.convertedAmount = convertedAmount;
    }

    public void setRate(BigDecimal rate) {
        this.rate = rate;
    }

    public void swapDirect() {
        Currency tmp = baseCurrency;
        baseCurrency = targetCurrency;
        targetCurrency = tmp;
    }

    @Override
    public String toString() {
        return "ExchangeResult{" +
                "baseCurrency=" + baseCurrency +
                ", targetCurrency=" + targetCurrency +
                ", rate=" + rate +
                ", amount=" + amount +
                ", convertedAmount=" + convertedAmount +
                '}';
    }
}
