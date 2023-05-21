package com.egovoryn.exchanger.servlets;

import com.egovoryn.exchanger.DTOs.ExchangeRequest;
import com.egovoryn.exchanger.services.ExchangeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;

@WebServlet(name = "ExchangeResult", value = "/exchange")
public class ExchangeServlet extends EntityServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
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

        ExchangeRequest exchangeRequest = new ExchangeRequest(from, to, new BigDecimal(amount));
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            PrintWriter out = response.getWriter();
            out.println(objectMapper.writeValueAsString(ExchangeService.convert(exchangeRequest)));
        } catch (IOException e) {
            if (e.getMessage().equals("The currency pair is not in the database")) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                errorResponse(response, e.getMessage());
            } else if (e.getMessage().equals("This calculation is not feasible")) {
                response.setStatus(HttpServletResponse.SC_CONFLICT);
                errorResponse(response, e.getMessage());
            }
            throw new RuntimeException(e);
        }
    }

    private String checkParameters(String from, String to, String amount) {
        if (from == null || from.isEmpty()) {
            return "Parameter «from» is empty";
        } else if (to == null || to.isEmpty()) {
            return "Parameter «to» is empty";
        } else if (amount == null || amount.isEmpty()) {
            return "Parameter «amount» is empty";
        }
        return "OK";
    }
}
