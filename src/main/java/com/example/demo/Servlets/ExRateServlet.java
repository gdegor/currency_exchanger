package com.example.demo.Servlets;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Statement;

@WebServlet(name = "ExchangeRate", value = "/exchangeRate/*")
public class ExRateServlet extends EntityServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String pairCurrency = request.getRequestURI().split("/")[2];
        String from = pairCurrency.substring(0,3);
        String to = pairCurrency.substring(3,6);

        String query = "SELECT * FROM ExchangeRates ex " +
                        "JOIN Currencies cur ON cur.ID = ex.BaseCurrencyId " +
                        "JOIN Currencies cur1 ON cur1.ID = ex.TargetCurrencyId " +
                        "WHERE cur.Code = \"" + from + "\" AND cur1.Code = \"" + to + "\" ";

        super.doGet(response, query);
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!req.getMethod().equals("PATCH")) {
            super.service(req, resp);
        }
        this.doPatch(req, resp);
    }

    protected void doPatch(HttpServletRequest request, HttpServletResponse response) {
        String rate = request.getParameter("rate");

        String pairCurrency = request.getRequestURI().split("/")[2];
        String fromCode = pairCurrency.substring(0,3);
        String toCode = pairCurrency.substring(3,6);

        Integer fromId = null;
        Integer toId = null;
        try {
            fromId = findCurIdByCode(fromCode);
            toId = findCurIdByCode(toCode);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        String query = "UPDATE ExchangeRates SET Rate = "+rate+" WHERE BaseCurrencyId = "+fromId+" AND TargetCurrencyId = "+toId;

        try {
            Statement statement = connection.createStatement();
            statement.executeUpdate(query);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        super.doGet(response, "SELECT * FROM ExchangeRates WHERE BaseCurrencyId = "+fromId+" AND TargetCurrencyId = "+toId);
    }
}
