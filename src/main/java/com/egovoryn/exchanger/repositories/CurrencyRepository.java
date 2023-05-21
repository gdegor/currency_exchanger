package com.egovoryn.exchanger.repositories;

import com.egovoryn.exchanger.DTOs.Currency;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class CurrencyRepository extends EntityRepository {
    public Currency findCurDataByID(Integer id) throws SQLException {
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT * FROM Currencies WHERE ID=" + id.toString());

        Currency res = new Currency(resultSet.getInt(1), resultSet.getString(2),
                resultSet.getString(3), resultSet.getString(4));

        resultSet.close();
        statement.close();
        return res;
    }

    public Integer findCurIdByCode(String code) {
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
