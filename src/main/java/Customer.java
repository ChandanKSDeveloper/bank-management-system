package main.java;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;

public class Customer {
    private static Connection connection;
    private final String table_name = DBConnection.getProperty("CUSTOMER_TABLE");
    public String aadhar_number;
    private final Account account; // made it final
    private String email;
    private String accNo;

    public Customer(Connection connection) {
        Customer.connection = connection;
        this.account = new Account(connection);

    }

    public void register() throws SQLException {
        connection.setAutoCommit(false);
        try{
            System.out.println("Register a New Customer");


            String name;
            while (true) {
                System.out.print("Enter customer name: ");
                name = InputHandler.getLineInput().trim();
                if (!name.isEmpty()) break;
                System.out.println("Name cannot be empty. Please try again.");
            }

            String email;
            while (true) {
                System.out.print("Enter your email: ");
                email = InputHandler.getLineInput().trim();
                if (isValidEmail(email)) break;
                System.out.println("Invalid email format. Please try again.");
            }


            while (true) {
                System.out.print("Enter your Aadhaar number (12 digits): ");
                try{
                    aadhar_number = InputHandler.getLineInput();
                    if (isValidAadhaarNumber(aadhar_number)) {
                        break;
                    } else {
                        System.out.println("Aadhaar number must be exactly 12 digits. Please try again.");
                    }
                } catch (Exception e){
                    System.out.println("Invalid input. Try again.");
                }
            }


            String phone_number;
            while (true) {
                System.out.print("Enter your contact number (10 digits): ");
                phone_number = InputHandler.getLineInput().trim();
                if (isValidPhoneNumber(phone_number)) break;
                System.out.println("Phone number must be 10 digits. Please try again.");
            }

            Date dob;
            while (true) {
                System.out.print("Enter your DOB (YYYY-MM-DD): ");
                try {
                    dob = Date.valueOf(InputHandler.getLineInput().trim());
                    break;
                } catch (IllegalArgumentException e) {
                    System.out.println("Invalid date format. Please try again.");
                }
            }

            String security_pin;
            while(true){
                System.out.print("Set a 4-digit security pin: ");
                try{
                    security_pin = InputHandler.getLineInput();
                    if(security_pin.length()  == 4 ) break;
                    System.out.println("Security pin cannot be 0 or less then 4 digit");
                } catch (NumberFormatException e){
                    System.out.println("Invalid input. Please enter a 4-digit number.");
                }
            }


            if(customerExists(aadhar_number, email)){
                System.out.println("A customer with this Aadhaar or email already exists.");
                return;
            }

            System.out.println();
            String query = "INSERT INTO " + table_name +  " (aadhar_id, name, email, phone_number, date_of_birth, security_pin) VALUES(?, ?, ?, ?, ?, ? )";
            try(PreparedStatement preparedStatement = connection.prepareStatement(query)){
                preparedStatement.setString(1,aadhar_number);
                preparedStatement.setString(2,name);
                preparedStatement.setString(3,email);
                preparedStatement.setString(4,phone_number);
                preparedStatement.setDate(5,dob);
                preparedStatement.setString(6,encrypt(security_pin));



                int rowAffected = preparedStatement.executeUpdate();
                if(rowAffected > 0){
//                    System.out.println("new customer added");
                    account.openAccount(aadhar_number);
                    connection.commit();
                } else {
                    System.out.println("Failed to register customer");
                }
                new Bank(connection).handleLogin();
            }
        } catch (SQLException e){
            connection.rollback();
            System.out.println("ERROR while registering customer : " + e.getMessage());
        }
    }

    public boolean login(){
        System.out.println("+------------------------------------------------+");
        System.out.println("|                     LOGIN                      |");
        System.out.println("+------------------------------------------------+");

        email = InputHandler.getValidatedEmail();
        String security_pin = InputHandler.getValidatedSecurityPin();

        String query = "SELECT security_pin FROM " + table_name + " WHERE email = ?";

        try(PreparedStatement preparedStatement = connection.prepareStatement(query)){
            preparedStatement.setString(1, email);
            ResultSet resultSet = preparedStatement.executeQuery();

            if(resultSet.next()){
                String hashedPin = resultSet.getString("security_pin");
                if(hashedPin.equals(encrypt(security_pin))){
                    System.out.println("Login Successful!");
                    return true;
                }
            }
        } catch (SQLException e) {
            System.err.println("Database error during login: " + e.getMessage());
        }

        System.out.println("Invalid email or security pin.");
        return false;
    }

    public void viewAccountDetails(){
        System.out.println("+------------------------------------------------+");
        System.out.println("|             ACCOUNT DETAILS                    |");
        System.out.println("+------------------------------------------------+");

        String fetchAadharQuery = "SELECT aadhar_id FROM customer WHERE email = ?";
        String query = "SELECT a.account_number, a.balance, c.name, c.email, a.customer_id FROM " + DBConnection.getProperty("ACCOUNT_TABLE") + " a INNER JOIN " + DBConnection.getProperty("CUSTOMER_TABLE") + " c ON c.aadhar_id = a.customer_id WHERE c.aadhar_id = ?" ;

        try(PreparedStatement fetchAadharStmt = connection.prepareStatement(fetchAadharQuery)){
            fetchAadharStmt.setString(1,email);

            ResultSet resultSet = fetchAadharStmt.executeQuery();
            if(resultSet.next()){
                String aadharId = resultSet.getString("aadhar_id");
                try(PreparedStatement preparedStatement = connection.prepareStatement(query)){
                    preparedStatement.setString(1, aadharId);
                    ResultSet accountResult  = preparedStatement.executeQuery();


                    if (accountResult .next()) {
                        System.out.println("Name: " + accountResult .getString("name"));
                        System.out.println("Email: " + accountResult .getString("email"));
                        System.out.println("Account Number: " + accountResult .getString("account_number"));
                        accNo = accountResult.getString("account_number");
                        System.out.println("Aadhaar Number/ Customer ID: " + accountResult .getString("customer_id"));
                        System.out.println("Balance: ₹" + accountResult .getDouble("balance"));
                    } else {
                        System.out.println("No account found for this user.");
                    }

                }
            } else {
                System.out.println("Customer not found.");
            }
        } catch (SQLException e){
            System.err.println("Error fetching Aadhaar id : " + e.getMessage());
        }

    }

    public String getAccountNumber(){
        if(accNo == null){
            String query = "SELECT a.account_number FROM " + DBConnection.getProperty("ACCOUNT_TABLE") + " a " +
                    "INNER JOIN " + DBConnection.getProperty("CUSTOMER_TABLE") + " c " +
                    "ON c.aadhar_id = a.customer_id WHERE c.email = ?";

            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, email);
                ResultSet resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    accNo = resultSet.getString("account_number");
                }
            } catch (SQLException e) {
                System.err.println("Error fetching account number: " + e.getMessage());
            }

        }
        return this.accNo;
    }


    public boolean customerExists(String aadharId, String email) {
        String query = "SELECT COUNT(*) FROM " + table_name + " WHERE aadhar_id = ? OR email = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, aadharId);
            preparedStatement.setString(2, email);

            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Database error while checking customer existence: " + e.getMessage());
        }
        return false;
    }

    // aadhar id / customer id
    private boolean isValidAadhaarNumber(String aadhar_number){
       return aadhar_number != null && aadhar_number.matches("\\d{12}");
    }

    // validation methods
    public boolean isValidEmail(String email) {
        // Simple regex for email validation
        return email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }

    // phone number validation
    private boolean isValidPhoneNumber(String phoneNumber) {
        return phoneNumber.matches("\\d{10}"); // Check for exactly 10 digits
    }

    private static String encrypt(String data){
        try{
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = messageDigest.digest(data.getBytes());
            StringBuilder hexString = new StringBuilder();
            for(byte b : hashedBytes){
                hexString.append(String.format("%02x",b));
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }


    public static boolean verifyPinUsingAccNo(Connection connection, String accountNumber){
        String security_pin = InputHandler.getValidatedSecurityPin();

        String query = "SELECT c.security_pin FROM " + DBConnection.getProperty("CUSTOMER_TABLE") + " c " +
                "INNER JOIN " + DBConnection.getProperty("ACCOUNT_TABLE") + " a ON c.aadhar_id = a.customer_id " +
                "WHERE a.account_number = ?";

        try(PreparedStatement preparedStatement = connection.prepareStatement(query)){
            preparedStatement.setString(1, accountNumber);
            ResultSet resultSet = preparedStatement.executeQuery();

            if(resultSet.next()){
                String hashedPin = resultSet.getString("security_pin");
                if(hashedPin.equals(encrypt(security_pin))){
                    return true;
                }
            }
        } catch (SQLException e) {
            System.err.println("Database error while verifying PIN:: " + e.getMessage());
        }
        System.out.println("Incorrect PIN. Transaction denied.");
        return false;
    }
}
