package com.example.demo.Servlets;

import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

import java.sql.*;

@WebServlet(name = "ExchangeRates", value = "/exchangeRates")
public class ExchangeRatesServlet extends EntityServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        super.doGet(response, "SELECT * FROM ExchangeRates");
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) {
        String baseCurrencyCode = request.getParameter("baseCurrencyCode");
        String targetCurrencyCode = request.getParameter("targetCurrencyCode");
        String rate = request.getParameter("rate");

//        System.out.println();

        try {
            String query = "INSERT INTO ExchangeRates (BaseCurrencyId, TargetCurrencyId, Rate)" +
                    "VALUES (\"" + findCurIdByCode(baseCurrencyCode) + "\", \"" +
                    findCurIdByCode(targetCurrencyCode) +" \", \" " + rate + " \")";
            Statement statement = connection.createStatement();
            statement.executeUpdate(query);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        this.doGet(response, "SELECT * FROM ExchangeRates ORDER BY ID DESC LIMIT 1");
    }
}
