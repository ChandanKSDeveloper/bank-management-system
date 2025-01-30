package main.java;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;

public class Account {
    private final Connection connection;
    private final String table_name = DBConnection.getProperty("ACCOUNT_TABLE");

    private String accountNumber;
    public Account(Connection connection){
        this.connection = connection;
    }

    public static boolean doesAccountExist(Connection connection, String accountNumber){
        String query = "SELECT 1 FROM " + DBConnection.getProperty("ACCOUNT_TABLE") + " WHERE account_number = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, accountNumber);
            ResultSet resultSet = stmt.executeQuery();
            return resultSet.next(); // If any record is found, account exists
        } catch (SQLException e) {
            System.err.println("Error checking account existence: " + e.getMessage());
            return false;
        }
    }
    public void openAccount(String aadharId) throws SQLException {  // aadhar id == customer_id

        double balance = 0.0;
        boolean isvalid = true;
        while(isvalid){
            System.out.println("Do you want to deposit amount ? ");
            System.out.print("Press [Y] for yes or [N] for no : ");
            char choice = InputHandler.getCharInput();
            System.out.println();

            switch (choice) {
                case 'y' :
                    //ask for amount
                    System.out.print("Enter the amount that you want to deposit : ");
                    try{
                        balance = InputHandler.getDoubleInput();
                    } catch (NumberFormatException e) {
                        System.out.println("Not a valid number : " + e.getMessage() );
                    }
                    isvalid = false;
                    break;
                case 'n' :
                    isvalid = false;
                    break;
                default:
                    System.out.print("Invalid choice try again : -");
                    isvalid = false;
                    break;
            }
        }

        String query = "INSERT INTO " + table_name + " (customer_id, account_number, balance, created_at) VALUES (?, ?, ?, NOW())";

        try(PreparedStatement preparedStatement = connection.prepareStatement(query)){
            preparedStatement.setString(1, aadharId);
            accountNumber = generateUniqueAccountNumber();
            preparedStatement.setString(2, accountNumber);
            preparedStatement.setDouble(3, balance);


            int rowAffected = preparedStatement.executeUpdate();
            if (rowAffected > 0) {
                connection.commit();
                System.out.println("Account created successfully. Account Number: " + accountNumber);
            } else {
                System.out.println("Failed to create an account.");
            }


        } catch (SQLException e){
            connection.rollback();
            System.err.println("Database error while creating an account: " + e.getMessage());
        }
    }

    private String generateUniqueAccountNumber() throws SQLException {
        Random random = new Random();
        while(true){
            String accountNumber = String.valueOf(10000000 + random.nextInt(90000000));
            String query = "SELECT COUNT(*) FROM " + table_name + " WHERE account_number = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, accountNumber);
                ResultSet resultSet = preparedStatement.executeQuery();
                if (resultSet.next() && resultSet.getInt(1) == 0) {
                    return accountNumber;
                }
            }
        }
    }

}
