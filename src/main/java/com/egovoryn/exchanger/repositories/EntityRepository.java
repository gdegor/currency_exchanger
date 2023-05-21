package com.egovoryn.exchanger.repositories;

import org.sqlite.SQLiteConfig;

import java.io.File;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

abstract class EntityRepository {
    private static final String DB_URL = "jdbc:sqlite:";
    private static final String DRIVER = "org.sqlite.JDBC";
    private static final String DATABASE = "currency.db";
    protected static Connection connection = makeConnection();

    protected String query;

    public EntityRepository(String query) {
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

    public static void update(String query) throws SQLException {
        Statement statement = connection.createStatement();
        statement.executeUpdate(query);
        statement.close();
    }

    private static Connection makeConnection() {
        try {
            if (connection == null) {
                Class.forName(DRIVER);
                URL resource = CurrencyRepository.class.getClassLoader().getResource(DATABASE);
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
}
