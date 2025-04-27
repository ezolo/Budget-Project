//Importing packages for application
package budgetapp.connection;

import java.sql.*;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;

//This section establishes connection with our MySQL databases!
public class DatabaseConnection
{
    public static Connection getConnection() throws SQLException
    {
        String dbURL = "jdbc:mysql://localhost:3306/budget_management";
        String username = "root";
        String password = "Filipio2008$";
        Connection connection = DriverManager.getConnection(dbURL, username, password);
        return connection;
    }

}
