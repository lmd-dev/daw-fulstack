package database;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class MySQLDatabase {
    private final String host;
    private final int port;
    private final String databaseName;
    private final String user;
    private final String password;

    private Connection connection;

    private static boolean initialized = false;

    public MySQLDatabase(String host, int port, String databaseName, String user, String password) throws SQLException {
        this.host = host;
        this.port = port;
        this.databaseName = databaseName;
        this.user = user;
        this.password = password;

        this.connection = null;

        this.connect();
    }

    private static void initialize() {
        if (MySQLDatabase.initialized == false) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                MySQLDatabase.initialized = true;
            } catch (ClassNotFoundException exception) {
                System.err.println("Unable to load MySQL driver.");
            }
        }
    }

    private void connect() throws SQLException {
        MySQLDatabase.initialize();
        this.connection = DriverManager.getConnection(String.format("jdbc:mysql://%s:%d/%s?allowMultiQueries=true", this.host, this.port, this.databaseName), user, password);
    }

    public PreparedStatement prepareStatement(String sqlQuery) throws SQLException
    {
        return this.connection.prepareStatement(sqlQuery);
    }
}
