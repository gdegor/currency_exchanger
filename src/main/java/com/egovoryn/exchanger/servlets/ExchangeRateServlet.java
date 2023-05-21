package com.egovoryn.exchanger.servlets;

import com.egovoryn.exchanger.repositories.ExchangeRatesRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(name = "ExchangeRate", value = "/exchangeRate/*")
public class ExchangeRateServlet extends EntityServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        String[] fromTo = splitAndCheckPair(request, response);
        if (fromTo[0] == null || fromTo[1] == null) return;
        Integer idPair = ExchangeRatesRepository.findPairIdByCodes(fromTo[0], fromTo[1]);
        if (idPair == -1) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            errorResponse(response, "The exchange rate for the pair was not found");
        } else {
            String query = "SELECT * FROM ExchangeRates WHERE ID="+idPair;
            super.doGet(response, query);
        }
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!req.getMethod().equals("PATCH")) {
            super.service(req, resp);
        } else {
            this.doPatch(req, resp);
        }
    }

    protected void doPatch(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String rate = request.getParameter("rate");
        String path = request.getReader().readLine();
        System.out.println(rate);
        System.out.println(path);
//        if (rate == null || rate.isEmpty()) {
//            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
//            errorResponse(response, "Parameter «rate» is empty");
//            return;
//        }
//
//        String[] fromTo = splitAndCheckPair(request, response);
//        if (fromTo[0] == null || fromTo[1] == null) return;
//        Integer idPair = ExchangeRatesRepository.findPairIdByCodes(fromTo[0], fromTo[1]);
//
//        if (idPair == -1) {
//            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
//            errorResponse(response, "The currency pair is not in the database");
//        } else {
//            String query = "UPDATE ExchangeRates SET Rate = " + rate + " WHERE ID = " + idPair;
//            try {
//                CurrencyRepository.update(query);
//            } catch (SQLException e) {
//                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
//                errorResponse(response, "Database is locked");
//                return;
//            }
//            super.doGet(response, "SELECT * FROM ExchangeRates WHERE ID = " + idPair);
//        }
    }

    private String[] splitAndCheckPair(HttpServletRequest request, HttpServletResponse response) {
        String pairCurrency;
        String[] result = new String[2];
        try {
            pairCurrency = request.getPathInfo().replace("/","");;
            if (pairCurrency.length() != 6) throw new Exception();
            result[0] = pairCurrency.substring(0,3);
            result[1] = pairCurrency.substring(3,6);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            errorResponse(response, "Pair currency codes are missing or incorrect in the address");
        }
        return result;
    }
}
