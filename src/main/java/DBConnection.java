package main.java;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public final class DBConnection {
//    private static final String PROPERTIES_FILE = "main/resources/dbconfig.properties";
    private static volatile Connection connection;
    private static final Properties props = new Properties();

    private DBConnection() {

        try{
            try (InputStream input = DBConnection.class.getClassLoader().getResourceAsStream("dbconfig.properties")) {
                if (input == null) {
                    throw new IOException("Properties file not found in the classpath");
                }
                props.load(input);
            }


            String dbname = props.getProperty("DBNAME");
            String url = "jdbc:mysql://localhost:3306/" + dbname;
            String username = props.getProperty("USERNAME");
            String password = props.getProperty("PASSWORD");

            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("Driver Loaded Successfully");

            connection = DriverManager.getConnection(url, username, password);

        } catch (ClassNotFoundException  e) {
            System.err.println("Failed to load driver: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("Error connecting to the database: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("Error reading properties file: " + e.getMessage());
        }
    }

    public static Connection getInstance(){
        if(connection == null){
            synchronized (DBConnection.class){
                if(connection == null){
                    new DBConnection();
                }

            }
        }
        return connection;
    }

    public static void closeConnection(){
        if(connection != null){
            try{
                connection.close();
                System.out.println("Database connection closed successfully");
            } catch (SQLException e) {
                System.err.println("Error closing the connection: " + e.getMessage());
            }
        }
    }

    public static String getProperty(String key){
        return props.getProperty(key);
    }

}
