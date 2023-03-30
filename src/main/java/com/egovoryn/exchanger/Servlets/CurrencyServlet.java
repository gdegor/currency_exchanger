package com.egovoryn.exchanger.Servlets;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(name = "Currency", value = "/currency/*")
public class CurrencyServlet extends EntityServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        String code;
        try {
            code = request.getPathInfo().replace("/","");;
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            errorResponse(response, "No currency code in the address");
            return;
        }

        if (findCurIdByCode(code) == -1) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            errorResponse(response, "Currency not found");
        } else {
            super.doGet(response, "SELECT * FROM Currencies WHERE Code=\"" + code + "\"");
        }
    }
}
