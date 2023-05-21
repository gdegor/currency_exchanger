package com.egovoryn.exchanger.services;

import com.egovoryn.exchanger.DTOs.ExchangeRates;
import com.egovoryn.exchanger.DTOs.ExchangeRequest;
import com.egovoryn.exchanger.DTOs.ExchangeResult;
import com.egovoryn.exchanger.repositories.CurrencyRepository;
import com.egovoryn.exchanger.repositories.ExchangeRatesRepository;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ExchangeService {
    private static BigDecimal amountDecimal;
    public static ExchangeResult convert(ExchangeRequest exchangeRequest) {
        String from = exchangeRequest.getBaseCurrencyCode();
        String to = exchangeRequest.getTargetCurrencyCode();

        CurrencyRepository currencyRepository = new CurrencyRepository();
        Integer fromId = currencyRepository.findCurIdByCode(from);
        Integer toId = currencyRepository.findCurIdByCode(to);
        Integer usdId = currencyRepository.findCurIdByCode("USD");

        if (fromId == -1 || toId == -1) {
            throw new RuntimeException("The currency pair is not in the database");
        }

        List<String> queries = createQueries(fromId, toId, usdId);
        amountDecimal = exchangeRequest.getAmount().setScale(6, RoundingMode.HALF_UP);

        try {
            ExchangeRatesRepository resultForDirectQuery = new ExchangeRatesRepository(queries.get(0));
            if (resultForDirectQuery.isValidateQuery()) { // is Direct?
                return directExchange(resultForDirectQuery);
            } else {
                ExchangeRatesRepository resultForBackQuery = new ExchangeRatesRepository(queries.get(1));
                if (resultForBackQuery.isValidateQuery()) { // is Back?
                    return backExchange(resultForBackQuery);
                } else {
                    ExchangeRatesRepository resultForUsdQuery = new ExchangeRatesRepository(queries.get(2));
                    if (resultForUsdQuery.isValidateQuery()) {  // is USD-transform?
                        ExchangeRatesRepository resultForUsdQuery2 = new ExchangeRatesRepository(queries.get(3));
                        ExchangeResult result = exchangeViaDollar(resultForUsdQuery, resultForUsdQuery2);
                        if (result == null) {
                            throw new RuntimeException("This calculation is not feasible");
                        }
                        return result;
                    } else {
                        throw new RuntimeException("This calculation is not feasible");
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("This calculation is not feasible");
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private static ExchangeResult directExchange(ExchangeRatesRepository resultForDirectQuery) throws SQLException, JsonProcessingException {
        ExchangeRates exRateDirect = resultForDirectQuery.create();
        BigDecimal convertedAmount = amountDecimal.multiply(exRateDirect.getRate()).setScale(6, RoundingMode.HALF_UP);
        return new ExchangeResult(exRateDirect, amountDecimal, convertedAmount);
    }

    private static ExchangeResult backExchange(ExchangeRatesRepository resultForBackQuery) throws SQLException, JsonProcessingException {
        ExchangeRates exchangeRateBack = resultForBackQuery.create();
        BigDecimal one = new BigDecimal(1);
        BigDecimal backRate = one.divide(exchangeRateBack.getRate(), 6, RoundingMode.HALF_UP);
        BigDecimal convertedAmount = amountDecimal.multiply(backRate).setScale(6, RoundingMode.HALF_UP);
        ExchangeResult ex = new ExchangeResult(exchangeRateBack, amountDecimal, convertedAmount);
        ex.setRate(backRate);
        ex.swapDirect();
        return ex;
    }

    private static ExchangeResult exchangeViaDollar(ExchangeRatesRepository repoOne, ExchangeRatesRepository repoTwo) throws SQLException, JsonProcessingException {
        List<ExchangeRates> exchangeRateUSD = new ArrayList<>();

        exchangeRateUSD.add(repoOne.create());
        exchangeRateUSD.add(repoTwo.create());

        BigDecimal fromUSD = exchangeRateUSD.get(0).getRate();
        BigDecimal toUSD = exchangeRateUSD.get(1).getRate();

        if (fromUSD == null || toUSD == null) return null;

        BigDecimal calcRate = toUSD.divide(fromUSD, 6, RoundingMode.HALF_UP);
        BigDecimal convertedAmount = amountDecimal.multiply(calcRate).setScale(6, RoundingMode.HALF_UP);

        ExchangeResult ex = new ExchangeResult(new ExchangeRates(-1, exchangeRateUSD.get(0).getTargetCurrency(),
                                               exchangeRateUSD.get(1).getTargetCurrency(), calcRate),
                                               amountDecimal, convertedAmount);
        ex.setRate(calcRate);

        return ex;
    }

    private static List<String> createQueries(Integer fromId, Integer toId, Integer usdId) {
        List<String> queries = new ArrayList<>();
        // DIRECT
        queries.add("SELECT * FROM ExchangeRates WHERE BaseCurrencyId = "+fromId+" AND TargetCurrencyId = "+toId);
        // BACK
        queries.add("SELECT * FROM ExchangeRates WHERE BaseCurrencyId = "+toId+" AND TargetCurrencyId = "+fromId);
        // USD (exRate for first and second currency)
        queries.add("SELECT * FROM ExchangeRates WHERE BaseCurrencyId = "+usdId+" AND TargetCurrencyId = "+fromId);
        queries.add("SELECT * FROM ExchangeRates WHERE BaseCurrencyId = "+usdId+" AND TargetCurrencyId = "+toId);

        return queries;
    }
}
