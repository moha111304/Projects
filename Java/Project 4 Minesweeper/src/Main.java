//Import Section

/*
 * Provided in this class is the necessary code to get started with your game's implementation
 * You will find a while loop that should take your minefield's gameOver() method as its conditional
 * Then you will prompt the user with input and manipulate the data as before in project 2
 * 
 * Things to Note:
 * 1. Think back to project 1 when we asked our user to give a shape. In this project we will be asking the user to provide a mode. Then create a minefield accordingly
 * 2. You must implement a way to check if we are playing in debug mode or not.
 * 3. When working inside your while loop think about what happens each turn. We get input, user our methods, check their return values. repeat.
 * 4. Once while loop is complete figure out how to determine if the user won or lost. Print appropriate statement.
 */

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        // Initialize variables for minefield dimensions and move count
        int x;
        int y;
        int count = 0;

        // Scanner to get user input
        Scanner myScanner = new Scanner(System.in);

        // Ask the user to choose the difficulty level
        System.out.println("What difficulty would you like to play? Easy, Medium, or Hard: ");
        String userInput = myScanner.nextLine();

        // Set minefield dimensions and number of mines based on user's difficulty choice
        if (userInput.equals("Easy")) {
            x = 5;
            y = 5;
        } else if (userInput.equals("Medium")) {
            x = 9;
            y = 12;
        } else if (userInput.equals("Hard")) {
            x = 20;
            y = 40;
        } else {
            // Exit the game if an invalid difficulty is chosen
            System.out.println("Invalid difficulty. Exiting the game.");
            myScanner.close();
            return;
        }

        // Create a Minefield object with the specified dimensions
        Minefield Sweeper = new Minefield(x, x, y);

        // Main game loop
        while (!Sweeper.gameOver()) {
            // Display the minefield in debug mode
            Sweeper.debug();
            // Display the minefield with revealed cells only
            System.out.println(Sweeper);

            // Prompt the user for input based on the move count
            if (count == 0) {
                System.out.println("Enter your starting coordinates: [x] [y]");
            } else {
                System.out.println("Enter a coordinate and if you wish to place a flag (Remaining: " + Sweeper.getFlags() + "): [x] [y] [true or false]");
            }

            // Get user's move as a string and split it into an array
            String userMove = myScanner.nextLine();
            String[] inputArray = userMove.split(" ");

            // Validate the number of input values based on the move count
            if (count == 0) {
                if (inputArray.length != 2) {
                    System.out.println("Invalid input. Please enter two values: [x] [y]");
                    continue;
                }
            } else {
                if (inputArray.length != 3) {
                    System.out.println("Invalid input. Please enter three values: [x] [y] [true or false]");
                    continue;
                }
            }

            // Parse user input into integers for row and column
            int row = Integer.parseInt(inputArray[0]);
            int col = Integer.parseInt(inputArray[1]);

            // Validate the coordinates to ensure they are within bounds
            if (row < 0 || row >= x || col < 0 || col >= x) {
                System.out.println("Invalid coordinates. Please enter valid coordinates.");
                continue;
            }

            // Process the user's move based on the move count
            if (count == 0) {
                Sweeper.revealStartingArea(row, col);
                count++;
            } else {
                // Parse the third input as a boolean for placing a flag
                String flag = inputArray[2];
                boolean bool = Boolean.parseBoolean(flag);
                Sweeper.guess(row, col, bool);
                count++;
            }
        }

        // Display the minefield and end the game
        System.out.println(Sweeper);
        System.out.println("Minesweeper Game Over");

        // Close the Scanner to prevent resource leaks
        myScanner.close();
    }
}

// Written by Ayub Mohamoud, moha1660