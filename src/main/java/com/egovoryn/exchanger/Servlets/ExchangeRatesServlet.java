package com.egovoryn.exchanger.Servlets;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.sql.SQLException;
import java.sql.Statement;

@WebServlet(name = "ExchangeRates", value = "/exchangeRates")
public class ExchangeRatesServlet extends EntityServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        super.doGet(response, "SELECT * FROM ExchangeRates");
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) {
        String baseCurrencyCode = request.getParameter("baseCurrencyCode");
        String targetCurrencyCode = request.getParameter("targetCurrencyCode");
        String rate = request.getParameter("rate");

        String error = checkParameters(baseCurrencyCode, targetCurrencyCode, rate);
        if (!error.equals("OK")) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            errorResponse(response, error);
            return;
        }

        try {
            String query = "INSERT INTO ExchangeRates (BaseCurrencyId, TargetCurrencyId, Rate)" +
                           "VALUES (\"" + findCurIdByCode(baseCurrencyCode) + "\", \"" +
                           findCurIdByCode(targetCurrencyCode) + "\", \"" + rate + "\")";
            Statement statement = connection.createStatement();
            statement.executeUpdate(query);
            statement.close();
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_CONFLICT);
            errorResponse(response, "Currency pair with this code already exists "+
                                          "or there are no currencies with these codes in the database");
            return;
        }

        this.doGet(response, "SELECT * FROM ExchangeRates ORDER BY ID DESC LIMIT 1");
    }

    private String checkParameters(String baseCurrencyCode, String targetCurrencyCode, String rate) {
        if (baseCurrencyCode == null || baseCurrencyCode.isEmpty()) {
            return "baseCurrencyCode is empty";
        } else if (targetCurrencyCode == null || targetCurrencyCode.isEmpty()) {
            return "targetCurrencyCode is empty";
        } else if (rate == null || rate.isEmpty()) {
            return "rate is empty";
        }
        return "OK";
    }
}
