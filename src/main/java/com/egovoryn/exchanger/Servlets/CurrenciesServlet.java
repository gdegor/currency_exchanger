package com.egovoryn.exchanger.Servlets;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.sql.SQLException;
import java.sql.Statement;

@WebServlet(name = "Currencies", value = "/currencies")
public class CurrenciesServlet extends EntityServlet {
    public void doGet(HttpServletRequest request, HttpServletResponse response) {
        super.doGet(response, "SELECT * FROM Currencies");
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) {
        String code = request.getParameter("code");
        String name = request.getParameter("name");
        String sign = request.getParameter("sign");

        String error = checkParameters(code, name, sign);
        if (!error.equals("OK")) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            errorResponse(response, error);
            return;
        }

        String query =  "INSERT INTO Currencies (Code, FullName, Sign)" +
                        "VALUES (\"" + code + "\", \"" + name + "\", \"" + sign + "\")";

        try {
            Statement statement = connection.createStatement();
            statement.executeUpdate(query);
            statement.close();
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_CONFLICT);
            errorResponse(response, "Currency with this code already exists");
            return;
        }

        this.doGet(response, "SELECT * FROM Currencies WHERE Code=\"" + code + "\"");
    }

    private String checkParameters(String code, String name, String sign) {
        if (code == null || code.length() != 3) {
            return "Parameter «code» is wrong";
        } else if (name == null || name.isEmpty()) {
            return "Parameter «name» is wrong";
        } else if (sign == null || sign.isEmpty()) {
            return "Parameter «sign» is wrong";
        }
        return "OK";
    }
}