package com.example.demo.Servlets;

import com.example.demo.DTOs.Currency;
import com.example.demo.DTOs.Exchange;
import com.example.demo.DTOs.ExchangeRates;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

@WebServlet(name = "Exchange", value = "/exchange")
public class ExchangeServlet extends EntityServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String from = request.getParameter("from");
        String to = request.getParameter("to");
        String amount = request.getParameter("amount");

        Integer fromId;
        Integer toId;
        Integer usdId;

        try {
            fromId = findCurIdByCode(from);
            toId = findCurIdByCode(to);
            usdId = findCurIdByCode("USD");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
//
//        List<String> queries = new ArrayList<>();
//        queries.add()


        String queryDirect = "SELECT * FROM ExchangeRates WHERE BaseCurrencyId = "+fromId+" AND TargetCurrencyId = "+toId;
        ExchangeRates exchangeRateDirect = null;
        try {
            Statement statementDirect = connection.createStatement();
            ResultSet resultSet = statementDirect.executeQuery(queryDirect);
            if (resultSet.next()) {
                exchangeRateDirect = new ExchangeRates(resultSet.getInt(1),
                        findCurDataByID(resultSet.getInt(2)),
                        findCurDataByID(resultSet.getInt(3)),
                        resultSet.getBigDecimal(4));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        String queryBack = "SELECT * FROM ExchangeRates WHERE BaseCurrencyId = "+toId+" AND TargetCurrencyId = "+fromId;
        ExchangeRates exchangeRateBack = null;
        try {
            Statement statementBack = connection.createStatement();
            ResultSet resultSet = statementBack.executeQuery(queryBack);
            if (resultSet.next()) {
                exchangeRateBack = new ExchangeRates(resultSet.getInt(1),
                        findCurDataByID(resultSet.getInt(2)),
                        findCurDataByID(resultSet.getInt(3)),
                        resultSet.getBigDecimal(4));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        String queryUSD = "SELECT * FROM ExchangeRates WHERE BaseCurrencyId = "+usdId+" AND TargetCurrencyId = "+fromId;
        String queryUSD2 = "SELECT * FROM ExchangeRates WHERE BaseCurrencyId = "+usdId+" AND TargetCurrencyId = "+toId;

        List<ExchangeRates> exchangeRateUSD = new ArrayList<>();
        try {
            Statement statementUSD = connection.createStatement();
            ResultSet resultSet = statementUSD.executeQuery(queryUSD);
            if (resultSet.next()) {
                exchangeRateUSD.add(new ExchangeRates(resultSet.getInt(1),
                        findCurDataByID(resultSet.getInt(2)),
                        findCurDataByID(resultSet.getInt(3)),
                        resultSet.getBigDecimal(4)));
                ResultSet resultSet2 = statementUSD.executeQuery(queryUSD2);
                exchangeRateUSD.add(new ExchangeRates(resultSet2.getInt(1),
                        findCurDataByID(resultSet2.getInt(2)),
                        findCurDataByID(resultSet2.getInt(3)),
                        resultSet2.getBigDecimal(4)));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        PrintWriter out;
        ObjectMapper objectMapper = new ObjectMapper();

        if (exchangeRateDirect != null) {
            response.setContentType("application/json; charset=UTF-8");
            out = response.getWriter();
            BigDecimal amountDecimal = new BigDecimal(amount).setScale(6, RoundingMode.HALF_UP);
            BigDecimal convertedAmount = amountDecimal.multiply(exchangeRateDirect.getRate()).setScale(6, RoundingMode.HALF_UP);
            out.println(objectMapper.writeValueAsString(new Exchange(exchangeRateDirect, amountDecimal, convertedAmount)));
        } else if (exchangeRateBack != null) {
            response.setContentType("application/json; charset=UTF-8");
            out = response.getWriter();
            BigDecimal one = new BigDecimal(1);
            BigDecimal amountDecimal = new BigDecimal(amount).setScale(6, RoundingMode.HALF_UP);

            BigDecimal backRate = one.divide(exchangeRateBack.getRate(), 6, RoundingMode.HALF_UP);
            BigDecimal convertedAmount = amountDecimal.multiply(backRate).setScale(6, RoundingMode.HALF_UP);
            Exchange ex = new Exchange(exchangeRateBack, amountDecimal, convertedAmount);
            ex.setRate(backRate);
            out.println(objectMapper.writeValueAsString(ex));
        } else if (!exchangeRateUSD.isEmpty()) {
            response.setContentType("application/json; charset=UTF-8");
            out = response.getWriter();

            BigDecimal fromUSD = exchangeRateUSD.get(0).getRate();
            BigDecimal toUSD = exchangeRateUSD.get(1).getRate();

            BigDecimal calcRate = toUSD.divide(fromUSD, 6, RoundingMode.HALF_UP);
            BigDecimal amountDecimal = new BigDecimal(amount).setScale(6, RoundingMode.HALF_UP);

            BigDecimal convertedAmount = amountDecimal.multiply(calcRate).setScale(6, RoundingMode.HALF_UP);

            Exchange ex = new Exchange(new ExchangeRates(-1, exchangeRateUSD.get(0).getTargetCurrency(),
                                                         exchangeRateUSD.get(1).getTargetCurrency(), calcRate),
                                                         amountDecimal, convertedAmount);
            ex.setRate(calcRate);
            out.println(objectMapper.writeValueAsString(ex));
        }
    }
}
