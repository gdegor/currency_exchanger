package com.egovoryn.exchanger.servlets;

import com.egovoryn.exchanger.DTOs.Currency;
import com.egovoryn.exchanger.DTOs.DataTransferObject;
import com.egovoryn.exchanger.DTOs.ErrorResponse;
import com.egovoryn.exchanger.DTOs.ExchangeRates;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletResponse;
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
    protected Connection connection = makeConnection();

    private Connection makeConnection() {
        try {
            if (connection == null) {
                Class.forName(DRIVER);
                URL resource = this.getClass().getClassLoader().getResource(DATABASE);
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
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);

            response.setContentType("application/json; charset=UTF-8");
            List<? extends DataTransferObject> transferObjects;

            if (Objects.equals(query.split(" ")[3], "ExchangeRates")) {
                List<ExchangeRates> tmp = new ArrayList<>();
                while (resultSet.next()) {
                    tmp.add(new ExchangeRates(resultSet.getInt(1),
                                              findCurDataByID(resultSet.getInt(2)),
                                              findCurDataByID(resultSet.getInt(3)),
                                              resultSet.getBigDecimal(4)));
                }
                transferObjects = tmp;
            } else {
                List<Currency> tmp = new ArrayList<>();
                while (resultSet.next()) {
                    tmp.add(new Currency(resultSet.getInt(1), resultSet.getString(2),
                                         resultSet.getString(3), resultSet.getString(4)));
                }
                transferObjects = tmp;
            }
            ObjectMapper objectMapper = new ObjectMapper();
            PrintWriter out = response.getWriter();
            out.println(objectMapper.writeValueAsString(transferObjects));

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
        ResultSet resultSet = statement.executeQuery("SELECT * FROM Currencies WHERE ID=" + id.toString());

        Currency res = new Currency(resultSet.getInt(1), resultSet.getString(2),
                                    resultSet.getString(3), resultSet.getString(4));

        resultSet.close();
        statement.close();
        return res;
    }

    protected Integer findCurIdByCode(String code) {
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT ID FROM Currencies WHERE Code=\"" + code + "\"");
            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
            resultSet.close();
            statement.close();
            return -1;
        } catch (Exception e) {
            return -1;
        }
    }
}
