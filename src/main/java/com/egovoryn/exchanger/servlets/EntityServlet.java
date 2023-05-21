package com.egovoryn.exchanger.servlets;

import com.egovoryn.exchanger.DTOs.Currency;
import com.egovoryn.exchanger.DTOs.DataTransferObject;
import com.egovoryn.exchanger.DTOs.ErrorResponse;
import com.egovoryn.exchanger.DTOs.ExchangeRates;
import com.egovoryn.exchanger.repositories.CurrencyRepository;
import com.egovoryn.exchanger.repositories.ExchangeRatesRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

abstract public class EntityServlet extends HttpServlet {
    protected void doGet(HttpServletResponse response, String query) {
        try {
            response.setContentType("application/json; charset=UTF-8");
            List<? extends DataTransferObject> transferObjects;

            if (Objects.equals(query.split(" ")[3], "ExchangeRates")) {
                List<ExchangeRates> tmp = new ArrayList<>();
                ExchangeRatesRepository exRatesRepo = new ExchangeRatesRepository(query);
                while (exRatesRepo.isValidateQuery()) {
                    tmp.add(exRatesRepo.create());
                }
                transferObjects = tmp;
            } else {
                List<Currency> tmp = new ArrayList<>();
                CurrencyRepository curRepo = new CurrencyRepository(query);
                while (curRepo.isValidateQuery()) {
                    tmp.add(curRepo.create());
                }
                transferObjects = tmp;
            }
            ObjectMapper objectMapper = new ObjectMapper();
            PrintWriter out = response.getWriter();
            out.println(objectMapper.writeValueAsString(transferObjects));

            out.close();
        } catch (IOException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            errorResponse(response, "Database is locked");
        }
    }

    protected void errorResponse(HttpServletResponse response, String error) {
        try {
            response.setContentType("application/json; charset=UTF-8");
            ObjectMapper objectMapper = new ObjectMapper();
            PrintWriter out = response.getWriter();
            out.println(objectMapper.writeValueAsString(new ErrorResponse(error)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
