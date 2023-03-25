package com.example.demo.Servlets;

import com.example.demo.DTOs.Exchange;
import com.example.demo.DTOs.ExchangeRates;
import com.fasterxml.jackson.core.JsonProcessingException;
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
    private BigDecimal amountDecimal;
    PrintWriter out;
    ResultSet resultSet;
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json; charset=UTF-8");

        String amount = request.getParameter("amount");
        String from = request.getParameter("from");
        String to = request.getParameter("to");

        String error = checkParameters(from, to, amount);
        if (!error.equals("OK")) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            errorResponse(response, error);
            return;
        }

        Integer fromId = findCurIdByCode(from);
        Integer toId = findCurIdByCode(to);
        Integer usdId = findCurIdByCode("USD");

        if (fromId == -1 || toId == -1) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            errorResponse(response, "The currency pair is not in the database");
            return;
        }

        List<String> queries = createQueries(fromId, toId, usdId);
        out = response.getWriter();
        amountDecimal = new BigDecimal(amount).setScale(6, RoundingMode.HALF_UP);

        try {
            Statement statement = connection.createStatement();
            resultSet = statement.executeQuery(queries.get(0));
            if (resultSet.next()) { // is Direct?
                directExchange();
            } else {
                resultSet = statement.executeQuery(queries.get(1));
                if (resultSet.next()) { // is Back?
                    backExchange();
                } else {
                    resultSet = statement.executeQuery(queries.get(2));
                    if (resultSet.next()) {  // is USD-transform?
                        Statement statementTmp = connection.createStatement();
                        ResultSet rsTmp = statementTmp.executeQuery(queries.get(3));
                        if (!exchangeViaDollar(rsTmp)) {
                            response.setStatus(HttpServletResponse.SC_CONFLICT);
                            errorResponse(response, "This calculation is not feasible");
                        }
                    } else {
                        response.setStatus(HttpServletResponse.SC_CONFLICT);
                        errorResponse(response, "This calculation is not feasible");
                    }
                }
            }
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_CONFLICT);
            errorResponse(response, "This calculation is not feasible");
        }
    }

    private void directExchange() throws SQLException, JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        ExchangeRates exRateDirect = new ExchangeRates(resultSet.getInt(1),
                findCurDataByID(resultSet.getInt(2)),
                findCurDataByID(resultSet.getInt(3)),
                resultSet.getBigDecimal(4));

        BigDecimal convertedAmount = amountDecimal.multiply(exRateDirect.getRate()).setScale(6, RoundingMode.HALF_UP);
        out.println(objectMapper.writeValueAsString(new Exchange(exRateDirect, amountDecimal, convertedAmount)));
    }

    private void backExchange() throws SQLException, JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        ExchangeRates exchangeRateBack = new ExchangeRates(resultSet.getInt(1),
                findCurDataByID(resultSet.getInt(2)),
                findCurDataByID(resultSet.getInt(3)),
                resultSet.getBigDecimal(4));

        BigDecimal one = new BigDecimal(1);

        BigDecimal backRate = one.divide(exchangeRateBack.getRate(), 6, RoundingMode.HALF_UP);
        BigDecimal convertedAmount = amountDecimal.multiply(backRate).setScale(6, RoundingMode.HALF_UP);
        Exchange ex = new Exchange(exchangeRateBack, amountDecimal, convertedAmount);
        ex.setRate(backRate);
        ex.swapDirect();
        out.println(objectMapper.writeValueAsString(ex));
    }

    private boolean exchangeViaDollar(ResultSet rs2) throws SQLException, JsonProcessingException {
        List<ExchangeRates> exchangeRateUSD = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();
        exchangeRateUSD.add(new ExchangeRates(resultSet.getInt(1),
                                              findCurDataByID(resultSet.getInt(2)),
                                              findCurDataByID(resultSet.getInt(3)),
                                              resultSet.getBigDecimal(4)));

        exchangeRateUSD.add(new ExchangeRates(rs2.getInt(1),
                                              findCurDataByID(rs2.getInt(2)),
                                              findCurDataByID(rs2.getInt(3)),
                                              rs2.getBigDecimal(4)));

        BigDecimal fromUSD = exchangeRateUSD.get(0).getRate();
        BigDecimal toUSD = exchangeRateUSD.get(1).getRate();

        if (fromUSD == null || toUSD == null) return false;

        BigDecimal calcRate = toUSD.divide(fromUSD, 6, RoundingMode.HALF_UP);

        BigDecimal convertedAmount = amountDecimal.multiply(calcRate).setScale(6, RoundingMode.HALF_UP);

        Exchange ex = new Exchange(new ExchangeRates(-1, exchangeRateUSD.get(0).getTargetCurrency(),
                                                     exchangeRateUSD.get(1).getTargetCurrency(), calcRate),
                                   amountDecimal, convertedAmount);
        ex.setRate(calcRate);
        out.println(objectMapper.writeValueAsString(ex));
        return true;
    }

    private List<String> createQueries(Integer fromId, Integer toId, Integer usdId) {
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

    private String checkParameters(String from, String to, String amount) {
        if (from.isEmpty()) {
            return "Parameter «from» is empty";
        } else if (to.isEmpty()) {
            return "Parameter «to» is empty";
        } else if (amount.isEmpty()) {
            return "Parameter «amount» is empty";
        }
        return "OK";
    }
}
