package com.example.demo.Servlets;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Statement;

@WebServlet(name = "Currency", value = "/currency/*")
public class CurrencyServlet extends EntityServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        String code = request.getRequestURI().split("/")[2];
        super.doGet(response, "SELECT * FROM Currencies WHERE Code=\""+code+"\"");
    }
}
