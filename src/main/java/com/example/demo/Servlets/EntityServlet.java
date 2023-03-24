package com.example.demo.Servlets;

import com.example.demo.DTOs.Currency;
import com.example.demo.DTOs.DataTransferObject;
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
    protected Connection connection = getConnection();

    private static Connection getConnection() {
        try {
            Connection connection;
            Class.forName(DRIVER);
            URL resource = CurrenciesServlet.class.getClassLoader().getResource(DATABASE);
            assert resource != null;
            String path = DB_URL + new File(resource.toURI()).getAbsolutePath();
            SQLiteConfig config = new SQLiteConfig();
            config.enforceForeignKeys(true);
            connection = DriverManager.getConnection(path, config.toProperties());
            return connection;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected void doGet(HttpServletResponse response, String query) {
        PrintWriter out;
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);

//            response.setContentType("application/json; charset=UTF-8");
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
            response.setContentType("application/json; charset=UTF-8");
            out = response.getWriter();
            out.println(objectMapper.writeValueAsString(currencies));

            statement.close();
            resultSet.close();
        } catch (SQLException | IOException e) {
            response.setStatus(500);
            throw new RuntimeException(e);
        }
    }

    protected Currency findCurDataByID(Integer id) throws SQLException {
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT * FROM Currencies WHERE ID="+id.toString());

        return new Currency(resultSet.getInt(1), resultSet.getString(2),
                resultSet.getString(3), resultSet.getString(4));
    }

    protected Integer findCurIdByCode(String code) throws SQLException {
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT ID FROM Currencies WHERE Code=\""+code+"\"");

        return resultSet.getInt(1);
    }
}
