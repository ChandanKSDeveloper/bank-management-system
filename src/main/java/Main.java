package main.java;

import java.sql.Connection;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        Connection connection = DBConnection.getInstance();

        Bank bankUI = new Bank(connection);
        bankUI.start();

    }
}