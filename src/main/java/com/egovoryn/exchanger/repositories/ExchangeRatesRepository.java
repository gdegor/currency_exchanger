package com.egovoryn.exchanger.repositories;

import com.egovoryn.exchanger.DTOs.ExchangeRates;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class ExchangeRatesRepository extends EntityRepository {
    public ExchangeRatesRepository(String query) {
        super(query);
    }

    public ExchangeRates create() {
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);
            return new ExchangeRates(resultSet.getInt(1),
                                     CurrencyRepository.findCurDataByID(resultSet.getInt(2)),
                                     CurrencyRepository.findCurDataByID(resultSet.getInt(3)),
                                     resultSet.getBigDecimal(4));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static Integer findPairIdByCodes(String codeFrom, String codeTo) {
        try {
            String query =  "SELECT * FROM ExchangeRates ex " +
                            "JOIN Currencies cur ON cur.ID = ex.BaseCurrencyId " +
                            "JOIN Currencies cur1 ON cur1.ID = ex.TargetCurrencyId " +
                            "WHERE cur.Code = \"" + codeFrom + "\" AND cur1.Code = \"" + codeTo + "\" ";
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);
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
