package com.example.demo.Servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Statement;

@WebServlet(name = "Currencies", value = "/currencies")
public class CurrenciesServlet extends EntityServlet {
    public void doGet(HttpServletRequest request, HttpServletResponse response) {
        super.doGet(response, "SELECT * FROM Currencies");
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String code = request.getParameter("code");
        String name = request.getParameter("name");
        String sign = request.getParameter("sign");

        String query =  "INSERT INTO Currencies (Code, FullName, Sign)" +
                "VALUES (\"" + code + "\", \"" + name +" \", \" " + sign + " \")";

        try {
            Statement statement = connection.createStatement();
            statement.executeUpdate(query);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        this.doGet(response, "SELECT * FROM Currencies WHERE Code=\""+code+"\"");
    }

    public void destroy() {
    }
}

//    CREATE TABLE "ExchangeRates" (
//        "ID"    INTEGER NOT NULL,
//        "BaseCurrencyId"        INTEGER UNIQUE,
//        "TargetCurrencyId"      INTEGER UNIQUE,
//        "Rate" DECIMAL(6),
//        PRIMARY KEY("ID" AUTOINCREMENT),
//        FOREIGN KEY("BaseCurrencyId") REFERENCES "Currencies"("ID"),
//        FOREIGN KEY("TargetCurrencyId") REFERENCES "Currencies"("ID")
//        );
//
//    CREATE TABLE "Currencies" (
//        "ID"    INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
//        "Code"  VARCHAR UNIQUE,
//        "FullName" VARCHAR,
//        "Sign"  VARCHAR);

//
//INSERT INTO Currencies (Code, FullName, Sign) VALUES ("AUD", "Australian dollar", "A$");
//INSERT INTO Currencies (Code, FullName, Sign) VALUES ("USD", "US Dollar", "$");
//INSERT INTO Currencies (Code, FullName, Sign) VALUES ("GBP", "Pound Sterling", "£");
//INSERT INTO Currencies (Code, FullName, Sign) VALUES ("JPY", "Yen", "¥");
//INSERT INTO Currencies (Code, FullName, Sign) VALUES ("RUB", "Russian Ruble", "₽");