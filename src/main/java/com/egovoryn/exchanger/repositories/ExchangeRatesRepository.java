package com.egovoryn.exchanger.repositories;

import com.egovoryn.exchanger.DTOs.ExchangeRates;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class ExchangeRatesRepository extends EntityRepository {
    private final String query;

    public ExchangeRatesRepository(String query) {
        this.query = query;
    }

    public boolean isValidateQuery() {
        try {
            Statement statement = connection.createStatement();
            return statement.executeQuery(query).next();
        } catch (SQLException e) {
            return false;
        }
    }

    public ExchangeRates create() {
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);

            CurrencyRepository curRepo = new CurrencyRepository();

            return new ExchangeRates(resultSet.getInt(1),
                                     curRepo.findCurDataByID(resultSet.getInt(2)),
                                     curRepo.findCurDataByID(resultSet.getInt(3)),
                                     resultSet.getBigDecimal(4));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
