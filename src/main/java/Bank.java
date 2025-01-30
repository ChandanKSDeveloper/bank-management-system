package main.java;

import java.sql.Connection;
import java.sql.SQLException;

/* this is a banking app -> allow user to register and setup new account,
    login then and allow access to bank facilites like transfer money credit and debit*/
public class Bank {
    private final Connection connection;
    private final Customer customer;
//    private final Account account;

    public Bank(Connection connection) {
        this.connection = connection;
        this.customer = new Customer(connection);
//        this.account = new Account(connection);
    }

    public void start() throws InterruptedException {
        header();
    }

    private void header() throws InterruptedException {
        System.out.println("+-------------------------------------------------------+");
        System.out.print(  "|               WELCOME TO BANKING SYSTEM               |\n");
        System.out.println("+-------------------------------------------------------+");
        Thread.sleep(500);

        int choice;
        while (true) {

            System.out.println("+-------------------------------------------------------+");
            System.out.println("| [1]           LOGIN                                   |");
            System.out.println("| [2]           CREATE NEW ACCOUNT                      |");
            System.out.println("| [3]           EXIT                                    |");
            System.out.println("+-------------------------------------------------------+");
            System.out.print("Enter a option : ");
            choice = InputHandler.getIntInput();
            switch (choice) {
                case 1 -> handleLogin();

                case 2 -> createNewAccount();

                case 3 -> {
                    DBConnection.closeConnection();
                    InputHandler.dispose();
                    return;
                }
                default -> System.out.println("Invalid option. Try again");


            }
        }

    }


    public void createNewAccount(){
        System.out.println("+------------------------------------------------+");
        System.out.println("|              CREATING NEW ACCOUNT              |");
        System.out.println("+------------------------------------------------+");
        System.out.println();
        try {
            connection.setAutoCommit(false);
            customer.register();

            connection.commit();


        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    public void handleLogin(){
        boolean isLoggedIn =  customer.login();

        if(isLoggedIn){
            displayBankingOptions(customer);
        } else {
            System.out.println("Login failed. Please try again.");
        }

    }

    private void displayBankingOptions(Customer customer){
        while(true){
            System.out.println("+-------------------------------------------------------+");
            System.out.println("|                       BANK MENU                       |");
            System.out.println("+-------------------------------------------------------+");
            System.out.println("| [1] View Account Details                              |");
            System.out.println("| [2| Deposit Money                                     |");
            System.out.println("| [3] Withdraw Money                                    |");
            System.out.println("| [4] Transfer Money                                    |");
            System.out.println("| [5] Statement                                         |");
            System.out.println("| [6] Logout                                            |");
            System.out.println("+-------------------------------------------------------+");
            System.out.print("Choose an option : ");
            int choice = InputHandler.getIntInput();
            switch (choice){
                case 1 -> customer.viewAccountDetails();
                case 2 -> {
                    System.out.print("Enter Deposit Amount : ");
                    double amount = InputHandler.getDoubleInput();
                    Transaction.depositMoney(connection ,customer.getAccountNumber(), amount);
                }
                case 3 -> {
                    System.out.print("Enter WithDraw Amount : ");
                    double amount = InputHandler.getDoubleInput();
                    Transaction.withdrawal(connection, customer.getAccountNumber(), amount);
                }
                case 4 -> {
                    System.out.print("Enter Amount to transfer: ");
                    double amount = InputHandler.getDoubleInput();
                    Transaction.transferMoney(connection, customer.getAccountNumber(), amount);
                }

                case 5 -> Transaction.showTransactionHistory(connection, customer.getAccountNumber());

                case 6 -> {
                    System.out.println("Log out successful");
//                    DBConnection.closeConnection();
                    return;
                }
                default -> System.out.println("Invalid option. Please try again.");
            }

        }
    }

}
