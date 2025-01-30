package main.java;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Transaction {
    public static void depositMoney(Connection connection, String accountNumber, double amount){
        if (amount <= 0) {
            System.out.print("Deposit amount must be greater than zero.");
            return;
        }

//        acc no - 66339319

//        System.out.println("account number" + accountNumber);

        // ask for pin
        // verify pin
        if(!Customer.verifyPinUsingAccNo(connection, accountNumber)){
            return;
        }
        String updateQuery = "UPDATE " + DBConnection.getProperty("ACCOUNT_TABLE") + " SET balance = balance + ? WHERE account_number = ?";

        try(PreparedStatement preparedStatement = connection.prepareStatement(updateQuery)
        ){
            preparedStatement.setDouble(1, amount);
            preparedStatement.setString(2, accountNumber);

            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Deposit successful! Amount: ₹" + amount);

                logTransaction(connection, accountNumber, "Deposit", amount, null);
            } else {
                System.out.println("Deposit failed. No account found.");
            }


        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
        }
    }

    public static void withdrawal(Connection connection, String accountNumber, double amount){
        if (amount <= 0) {
            System.out.print("Credit amount must be greater than zero.");
            return;
        }


        if(!Customer.verifyPinUsingAccNo(connection, accountNumber)){
            return;
        }
//        System.out.println("account number" + accountNumber);
        String updateQuery = "UPDATE " + DBConnection.getProperty("ACCOUNT_TABLE") + " SET balance = balance - ? WHERE account_number = ?";

        try(PreparedStatement preparedStatement = connection.prepareStatement(updateQuery)
        ){
            preparedStatement.setDouble(1, amount);
            preparedStatement.setString(2, accountNumber);

            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("withdrawal successful! Amount: ₹" + amount);
                logTransaction(connection, accountNumber, "WithDraw", amount, null);
            } else {
                System.out.println("withdrawal failed. No account found.");
            }


        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
        }
    }

    public static void transferMoney(Connection connection, String senderAccountNumber, double amount){
        if (amount <= 0) {
            System.out.print("amount must be greater than zero.");
            return;
        }

        //        singhchandan@gmail.com -> acc no. 94110467
//        test@mail.com -> acc no - 66339319


        System.out.print("Enter receiver's account number: ");
        String receiverAccountNumber = InputHandler.getLineInput();

        if(receiverAccountNumber.equals(senderAccountNumber)){
            System.out.println("Both account's are equal -> use Deposit Amount option.");
            return;
        }
        if(!Account.doesAccountExist(connection, receiverAccountNumber)){
            System.out.println("Receiver account not found. Transaction aborted.");
            return;
        }

        if(!Customer.verifyPinUsingAccNo(connection, senderAccountNumber)){
            return;
        }

        try{
            connection.setAutoCommit(false);

            // Deduct money from sender's account
            String deductQuery = "UPDATE " + DBConnection.getProperty("ACCOUNT_TABLE") +
                    " SET balance = balance - ? WHERE account_number = ? AND balance >= ?";

            try (PreparedStatement deductStmt = connection.prepareStatement(deductQuery)) {
                deductStmt.setDouble(1, amount);
                deductStmt.setString(2, senderAccountNumber);
                deductStmt.setDouble(3, amount);

                int senderUpdated = deductStmt.executeUpdate();
                if (senderUpdated == 0) {
                    System.out.println("Insufficient balance. Transaction aborted.");
                    connection.rollback(); // Rollback if sender has insufficient funds
                    return;
                }
            }

            // Add money to receiver's account
            String addQuery = "UPDATE " + DBConnection.getProperty("ACCOUNT_TABLE") +
                    " SET balance = balance + ? WHERE account_number = ?";

            try (PreparedStatement addStmt = connection.prepareStatement(addQuery)) {
                addStmt.setDouble(1, amount);
                addStmt.setString(2, receiverAccountNumber);

                int receiverUpdated = addStmt.executeUpdate();
                if (receiverUpdated == 0) {
                    System.out.println("Failed to credit receiver's account. Transaction aborted.");
                    connection.rollback(); // Rollback transaction if deposit fails
                    return;
                }
            }

            logTransaction(connection, senderAccountNumber, "Transfer", amount, receiverAccountNumber);
            logTransaction(connection, receiverAccountNumber, "Deposit", amount, senderAccountNumber);

            connection.commit();// Commit transaction if both updates succeed
            System.out.println("Transfer successful! Amount: ₹" + amount + " to " + receiverAccountNumber);
        } catch (SQLException e) {
            try{
                connection.rollback();
                System.err.println("Transaction failed! Rolling back. Error: " + e.getMessage());
            } catch (SQLException rollbackEx) {
                System.err.println("Rollback failed: " + rollbackEx.getMessage());
            }
        } finally {
            try {
                connection.setAutoCommit(true); // Reset auto-commit
            } catch (SQLException ex) {
                System.err.println("Error resetting auto-commit: " + ex.getMessage());
            }
        }


    }


    public static void logTransaction(Connection connection, String accountNumber, String type, double amount, String receiverAccount){
        String query = "INSERT INTO " + DBConnection.getProperty("TRANSACTION_TABLE") +
                " (account_number, transaction_type, amount, receiver_account_number) VALUES (?, ?, ?, ?)";

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, accountNumber);
            preparedStatement.setString(2, type);
            preparedStatement.setDouble(3, amount);
            preparedStatement.setString(4, receiverAccount);

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Failed to log transaction: " + e.getMessage());
        }

    }

    public static void showTransactionHistory(Connection connection, String accountNumber){
        String query = "SELECT transaction_type, amount, transaction_time, receiver_account_number " +
                "FROM " + DBConnection.getProperty("TRANSACTION_TABLE") +
                " WHERE account_number = ? ORDER BY transaction_time DESC";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, accountNumber);
            ResultSet rs = stmt.executeQuery();

            System.out.println("\n--- Transaction History ---");
            while (rs.next()) {
                String type = rs.getString("transaction_type");
                double amount = rs.getDouble("amount");
                String time = rs.getString("transaction_time");
                String receiver = rs.getString("receiver_account_number");

                if (type.equals("Transfer")) {
                    System.out.println(type + ": ₹" + amount + " to " + receiver + " on " + time);
                } else {
                    System.out.println(type + ": ₹" + amount + " on " + time);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching transaction history: " + e.getMessage());
        }
    }



}
