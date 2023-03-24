import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.*;

public class Main {
    public static void main(String[] args) throws SQLException {
        BigDecimal one = new BigDecimal(1);
        BigDecimal amountDecimal = BigDecimal.valueOf(10);

        System.out.println(one.divide(new BigDecimal(77), 6, RoundingMode.HALF_UP));




//        Connection connection = null;
//        try {
//            URL resource = Main.class.getClassLoader().getResource("currency.db");
//            String path = null;
//            try {
//                path = new File(resource.toURI()).getAbsolutePath();
//            } catch (URISyntaxException e) {
//                throw new RuntimeException(e);
//            }
//
//            Class.forName("org.sqlite.JDBC");
//            connection = DriverManager.getConnection(String.format("jdbc:sqlite:%s", path));
//
//            System.out.println("Connected\n-------------");
//        } catch (ClassNotFoundException | SQLException e) {
//            throw new RuntimeException(e);
//        }
////
//        Statement statement = connection.createStatement();
//
//
//        statement.executeUpdate("DELETE FROM Currencies WHERE ID=\"10\"");
//
////        statement.executeUpdate("INSERT INTO Currencies (Code, FullName, Sign) " +
////                "VALUES (\"TES2\", \"Rfdsfe\", \"X\")");
//        System.out.println(statement.execute("SELECT * FROM Currencies"));
//
//        ResultSet rs = statement.executeQuery("SELECT * FROM Currencies");
//        while (rs.next()) {
//            Object obj = rs.getObject("Code");
//            Object obj1 = rs.getObject("ID");
//            System.out.println(obj1+" - "+obj);
//        }
//
//        connection.close();
////        System.out.println("add!");
    }
}
