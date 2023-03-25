package com.example.demo.Servlets;

import com.example.demo.DTOs.Currency;
import com.example.demo.DTOs.DataTransferObject;
import com.example.demo.DTOs.ErrorResponse;
import com.example.demo.DTOs.ExchangeRates;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.*;
import org.sqlite.SQLiteConfig;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

abstract public class EntityServlet extends HttpServlet {
    private static final String DB_URL = "jdbc:sqlite:";
    private static final String DRIVER = "org.sqlite.JDBC";
    private static final String DATABASE = "currency.db";
    protected Connection connection;
    public EntityServlet() {
        connection = makeConnection();
    }

    private Connection makeConnection() {
        try {
            if (connection == null) {
                Class.forName(DRIVER);
                URL resource = CurrenciesServlet.class.getClassLoader().getResource(DATABASE);
                assert resource != null;
                String path = DB_URL + new File(resource.toURI()).getAbsolutePath();
                SQLiteConfig config = new SQLiteConfig();
                config.enforceForeignKeys(true);
                connection = DriverManager.getConnection(path, config.toProperties());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return connection;
    }

    protected void doGet(HttpServletResponse response, String query) {
        PrintWriter out;
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);

            response.setContentType("application/json; charset=UTF-8");
            List<? extends DataTransferObject> currencies;

            if (Objects.equals(query.split(" ")[3], "ExchangeRates")) {
                List<ExchangeRates> tmp = new ArrayList<>();
                while (resultSet.next()) {
                    tmp.add(new ExchangeRates(resultSet.getInt(1),
                                              findCurDataByID(resultSet.getInt(2)),
                                              findCurDataByID(resultSet.getInt(3)),
                                              resultSet.getBigDecimal(4)));
                }
                currencies = tmp;
            } else {
                List<Currency> tmp = new ArrayList<>();
                while (resultSet.next()) {
                    tmp.add(new Currency(resultSet.getInt(1), resultSet.getString(2),
                                         resultSet.getString(3), resultSet.getString(4)));
                }
                currencies = tmp;
            }
            ObjectMapper objectMapper = new ObjectMapper();
            out = response.getWriter();
            out.println(objectMapper.writeValueAsString(currencies));

            out.close();
            resultSet.close();
            statement.close();
        } catch (SQLException | IOException e) {
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

    protected Currency findCurDataByID(Integer id) throws SQLException {
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT * FROM Currencies WHERE ID="+id.toString());

        Currency res = new Currency(resultSet.getInt(1), resultSet.getString(2),
                resultSet.getString(3), resultSet.getString(4));

        resultSet.close();
        statement.close();
        return res;
    }

    protected Integer findCurIdByCode(String code) {
        try {
            int res = -1;
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT ID FROM Currencies WHERE Code=\""+code+"\"");
            if (resultSet.next()) {
                res = resultSet.getInt(1);
            }
            resultSet.close();
            statement.close();
            return res;
        } catch (Exception e) {
            return -1;
        }
    }
}
