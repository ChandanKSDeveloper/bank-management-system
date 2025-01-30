package main.java;

import java.util.Scanner;

public class InputHandler {
    private static final Scanner scanner = new Scanner(System.in);

    private InputHandler(){

    }

    public static String getLineInput(){
        return scanner.nextLine().trim();
    }

    public static int getIntInput(){
        while(true){
            try{
                return Integer.parseInt(getLineInput());
            } catch (NumberFormatException e){
                System.err.println("Invalid Input. Please Enter an Integer");
            }
        }
    }

    public static double getDoubleInput(){
        while(true){
            try{
                return Double.parseDouble(getLineInput());
            } catch (NumberFormatException e){
                System.err.println("Invalid Input. Please Enter a Decimal value");
            }
        }
    }

    public static char getCharInput(){
        while(true){
            try{
                return Character.toLowerCase(getLineInput().charAt(0));
            } catch (NumberFormatException e){
                System.err.println("Invalid Input. Please Enter a valid Character [y]/[n]");
            }
        }
    }

    public static String getValidatedEmail() {
        while (true) {
            System.out.print("Enter your email: ");
            String email = getLineInput().trim();
            if (email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$")) {
                return email;
            }
            System.out.println("Invalid email format. Please try again.");
        }
    }

    public static String getValidatedSecurityPin() {
        while (true) {
            System.out.print("Enter your 4-digit security pin: ");
            String pin = getLineInput().trim();
            if (pin.matches("\\d{4}")) {
                return pin;
            }
            System.out.println("Security pin must be exactly 4 digits.");
        }
    }



    public static void dispose(){
        try{
            scanner.close();
            System.out.println("Scanner closed successfully");

        } catch (Exception e) {
            System.err.println("Failed to close scanner");
        }
    }
}
