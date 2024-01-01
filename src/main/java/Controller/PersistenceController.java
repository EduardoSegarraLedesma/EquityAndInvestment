package Controller;

import Model.ComparePurchase;
import Model.Purchase;
import Model.Sell;
import WebScraping.WebScrapping;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@RestController
public class PersistenceController {
    WebScrapping aux = new WebScrapping();

    private static PersistenceController instance = null;

    private PersistenceController() {
    }

    public static PersistenceController getInstance() {
        if (instance == null)
            instance = new PersistenceController();
        return instance;
    }

    @GetMapping("/setUser/{userId}")
    public ResponseEntity<String> setUser(@PathVariable String userId) {
        Float userBalance = getUserBalance(userId);
        if (userBalance <= -1F) {
            try {
                createUser(userId);
                return new ResponseEntity<>("0 $", HttpStatus.OK);
            } catch (SQLException e) {
                return new ResponseEntity<>("Database Error, please try later", HttpStatus.BAD_REQUEST);
            }
        } else {
            return new ResponseEntity<>(userBalance + " $", HttpStatus.OK);
        }
    }

    @GetMapping("/addBalance/{id}/{money}")
    public ResponseEntity<String> addBalance(@PathVariable String id, @PathVariable Float money) {
        try {
            Float newBalance = getUserBalance(id) + money;
            updateBalance(id, newBalance);
            return new ResponseEntity<>(newBalance + " $", HttpStatus.OK);
        } catch (SQLException e) {
            return new ResponseEntity<>("Database Error, please try later", HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/companies")
    public ResponseEntity<String> getCompanies() {
        return new ResponseEntity<>(getCompaniesList().toString(), HttpStatus.OK);
    }


    @GetMapping("/purchases/{id}")
    public ResponseEntity<List<ComparePurchase>> getPurchases(@PathVariable String id) {
        try {
            Connection connection = InvestmentDB().getConnection();
            Statement statement = connection.createStatement();
            List<ComparePurchase> list = new ArrayList<>();
            ResultSet result = statement.executeQuery(
                    "SELECT Symbol, Quantity, Price, TransactionDate FROM Purchase" +
                            " WHERE Id = '" + id + "';");
            while (result.next()) {
                list.add(new ComparePurchase(result.getString(1),
                        result.getInt(2),
                        result.getFloat(3),
                        aux.searchForCompanyStockPriceFloatWithSymbol(result.getString(1)),
                        result.getString(4)));
            }
            return new ResponseEntity<>(list, HttpStatus.OK);
        } catch (SQLException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }


    @GetMapping("/buyStock/{purchase}")
    public ResponseEntity<String> buyStock(@PathVariable Purchase purchase) {
        try {
            Float stockPrice = aux.searchForCompanyStockPriceFloatWithSymbol(purchase.getSymbol());
            updateBalanceWithPurchase(purchase, stockPrice);
            createPurchase(purchase, stockPrice);
            return new ResponseEntity<>(getUserBalance(purchase.getId())+ "$", HttpStatus.OK);
        } catch (SQLException e) {
            return new ResponseEntity<>("Database Error, please try later", HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/sellStock/{sell}")
    public ResponseEntity<String> sellStock(@PathVariable Sell sell) {
        try {
            Float stockPrice = aux.searchForCompanyStockPriceFloatWithSymbol(sell.getSymbol());
            updateBalanceWithSell(sell, stockPrice);
            return new ResponseEntity<>(getUserBalance(sell.getId())+ "$", HttpStatus.OK);
        } catch (SQLException e) {
            return new ResponseEntity<>("Database Error, please try later", HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/deleteUser/{userId}")
    public ResponseEntity<String> deleteUser(@PathVariable String userId) {
        try {
            delete("Purchase", userId);
            delete("Users", userId);
            return new ResponseEntity<>("done", HttpStatus.OK);
        } catch (SQLException e) {
            return new ResponseEntity<>("Database Error, please try later", HttpStatus.BAD_REQUEST);
        }
    }

    // ----------------- SUPPORT FUNCTIONS ----------------- //

    private DataSource InvestmentDB() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        dataSource.setUrl("jdbc:sqlserver://southeast-asia-s3rv3r.database.windows.net:1433;database=PFM_System_Investment");
        dataSource.setUsername("TongjiStudent");
        dataSource.setPassword("Tongji_Root");
        return dataSource;
    }

    private void createUser(String userId) throws SQLException {
        Connection connection = InvestmentDB().getConnection();
        Statement statement = connection.createStatement();
        statement.execute("INSERT INTO Users VALUES ("
                + "'" + userId + "',"
                + "" + 0 + ");");
    }

    private void createPurchase(Purchase purchase, Float price) throws SQLException {
        Connection connection = InvestmentDB().getConnection();
        Statement statement = connection.createStatement();
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        statement.execute("INSERT INTO Users VALUES ("
                + "'" + purchase.getId() + "',"
                + "'" + purchase.getSymbol() + "',"
                + "" + price + ","
                + "" + purchase.getQuantity() + ","
                + "'" + now.format(formatter) + "');");
    }

    private Float getUserBalance(String userId) {
        try {
            Connection connection = InvestmentDB().getConnection();
            Statement statement = connection.createStatement();
            ResultSet result = statement.executeQuery(
                    "SELECT Balance FROM Users WHERE "
                            + "Id= '" + userId + "';");
            result.next();
            return result.getFloat("Balance");
        } catch (SQLException e) {
            return -1F;
        }
    }

    private void updateBalanceWithPurchase(Purchase purchase, Float price) throws SQLException {
        Float newBalance = getUserBalance(purchase.getId()) - (purchase.getQuantity() * price);
        updateBalance(purchase.getId(), newBalance);
    }

    private void updateBalanceWithSell(Sell sell, Float price) throws SQLException {
        Float newBalance = getUserBalance(sell.getId()) + (sell.getQuantity() * price);
        updateBalance(sell.getId(), newBalance);
    }

    private void updateBalance(String userId, Float newBalance) throws SQLException {
        Connection connection = InvestmentDB().getConnection();
        Statement statement = connection.createStatement();
        statement.executeUpdate("UPDATE Users SET Balance = '" + newBalance + "' WHERE Id ='" + userId + "';");
        connection.close();
    }

    private List<Map<String, String>> getCompaniesList() {
        WebScrapping aux = new WebScrapping();
        return aux.getCompanyData();
    }

    private void delete(String table, String userID) throws SQLException {
        Connection connection = InvestmentDB().getConnection();
        Statement statement = connection.createStatement();
        statement.execute("DELETE FROM " + table + " WHERE Id = '" + userID + "';");
    }

}
