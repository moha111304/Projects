import java.util.Random;

public class Minefield {
    private Cell[][] Minefield;
    private int rows;
    private int columns;
    private int flags;
    /**
    Global Section
    */
    public static final String ANSI_YELLOW_BRIGHT = "\u001B[33;1m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE_BRIGHT = "\u001b[34;1m";
    public static final String ANSI_BLUE = "\u001b[34m";
    public static final String ANSI_RED_BRIGHT = "\u001b[31;1m";
    public static final String ANSI_RED = "\u001b[31m";
    public static final String ANSI_GREEN = "\u001b[32m";
    public static final String ANSI_PURPLE = "\u001b[35m";
    public static final String ANSI_CYAN = "\u001b[36m";
    public static final String ANSI_WHITE_BACKGROUND = "\u001b[47m";
    public static final String ANSI_PURPLE_BACKGROUND = "\u001b[45m";
    public static final String ANSI_GREY_BACKGROUND = "\u001b[0m";

    /* 
     * Class Variable Section
     * 
    */

    /*Things to Note:
     * Please review ALL files given before attempting to write these functions.
     * Understand the Cell.java class to know what object our array contains and what methods you can utilize
     * Understand the StackGen.java class to know what type of stack you will be working with and methods you can utilize
     * Understand the QGen.java class to know what type of queue you will be working with and methods you can utilize
     */
    
    /**
     * Minefield
     * 
     * Build a 2-d Cell array representing your minefield.
     * Constructor
     * @param rows       Number of rows.
     * @param columns    Number of columns.
     * @param flags      Number of flags, should be equal to mines
     */
    public Minefield(int rows, int columns, int flags) {
        // Initialize instance variables with provided values
        this.rows = rows;
        this.columns = columns;
        this.flags = flags;

        // Create a 2D array to represent the minefield
        this.Minefield = new Cell[rows][columns];

        // Initialize each cell in the minefield with default values
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                // Each cell is initially unrevealed and marked with "-"
                Minefield[i][j] = new Cell(false, "0");
            }
        }

        createMines(0, 0, flags);
        evaluateField();
    }

    /**
     * evaluateField
     * 
     *
     * @function:
     * Evaluate entire array.
     * When a mine is found check the surrounding adjacent tiles. If another mine is found during this check, increment adjacent cells status by 1.
     * 
     */
    public void evaluateField() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                if (Minefield[i][j].getStatus().equals("M")) {
                    int[] array1 = {1, 1, 1, 0, -1, -1, -1, 0}; // Row change
                    int[] array2 = {-1, 0, 1, 1, 1, 0, -1, -1}; // Column change
                    for (int k = 0; k < 8; k++) {
                        int adjI = i + array1[k];
                        int adjJ = j + array2[k];
                        if ((adjI >= 0 && adjI < rows) && (adjJ >= 0 && adjJ < columns)) {
                            if (!Minefield[adjI][adjJ].getStatus().equals("M")) {
                                int add1 = Integer.parseInt(Minefield[adjI][adjJ].getStatus());
                                add1++;
                                String strAdd1 = Integer.toString(add1);
                                Minefield[adjI][adjJ].setStatus(strAdd1);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * createMines
     * 
     * Randomly generate coordinates for possible mine locations.
     * If the coordinate has not already been generated and is not equal to the starting cell set the cell to be a mine.
     * utilize rand.nextInt()
     * 
     * @param x       Start x, avoid placing on this square.
     * @param y        Start y, avoid placing on this square.
     * @param mines      Number of mines to place.
     */
    public void createMines(int x, int y, int mines) {
        // Create a Random object for generating random positions
        Random r = new Random();

        // Randomly select a position for the first mine
        int row = r.nextInt(rows);
        int column = r.nextInt(columns);
        int i = 0;

        // Place the specified number of mines in the minefield
        while (i < mines) {
            // Check if the randomly selected position is not the excluded position (x, y)
            if (x != row || y != column) {
                // Check if the cell at the selected position is empty ("-")
                if (Minefield[row][column].getStatus().equals("0")) {
                    // Set the status of the cell to indicate a mine ("M")
                    Minefield[row][column].setStatus("M");
                    i++;
                }
            }

            // Randomly select a new position for the next mine
            row = r.nextInt(rows);
            column = r.nextInt(columns);
        }
    }

    /**
     * guess
     *
     * Check if the guessed cell is inbounds (if not done in the Main class).
     * Either place a flag on the designated cell if the flag boolean is true or clear it.
     * If the cell has a 0 call the revealZeroes() method or if the cell has a mine end the game.
     * At the end reveal the cell to the user.
     *
     *
     * @param x       The x value the user entered.
     * @param y       The y value the user entered.
     * @param flag    A boolean value that allows the user to place a flag on the corresponding square.
     * @return boolean Return false if guess did not hit mine or if flag was placed, true if mine found.
     */
    public boolean guess(int x, int y, boolean flag) {
        // Check if the guessed cell is within the boundaries of the minefield
        if ((x >= 0 && x < rows) && (y >= 0 && y < columns)) {
            // If the user wants to place a flag
            if (flag && flags > 0) {
                // Set the flag on the designated cell
                Minefield[x][y].setStatus("F");
                // Decrement the available flags
                flags--;
                // Set the flag as revealed
                Minefield[x][y].setRevealed(true);
                // Return false because the mine is not hit
                return false;
            } else { // If the user is not placing a flag
                // Check if the cell contains a mine
                if (Minefield[x][y].getStatus().equals("M")) {
                    // Set the mine as revealed
                    Minefield[x][y].setRevealed(true);
                    // Return true because a mine is hit
                    return true;
                } else if (Minefield[x][y].getStatus().equals("0")) {
                    // If the cell contains 0, reveal connected 0's
                    revealZeroes(x, y);
                }
            }
            // Set the cell that is not a flag or mine as revealed
            Minefield[x][y].setRevealed(true);
            // Return false because the mine is not hit
            return false;
        }
        // Return true if a mine is found (out of bounds)
        return true;
    }

    /**
     * gameOver
     * 
     * Ways a game of Minesweeper ends:
     * 1. player guesses a cell with a mine: game over -> player loses
     * 2. player has revealed the last cell without revealing any mines -> player wins
     * 
     * @return boolean Return false if game is not over and squares have yet to be revealed, otheriwse return true.
     */
    public boolean gameOver() {
        int countRevealedMines = 0; // Variable to count the number of revealed mines
        int remainingMines = 0; // Variable to count the number of mines remaining;
        int countRevealedFlags = 0; // Variable to count the number of revealed flags

        // Loop through each cell in the minefield
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                // Check if the current cell contains a mine
                if (Minefield[i][j].getStatus().equals("M")) {
                    remainingMines++; // Increment the count of remaining mines

                    // Check if the mine is revealed
                    if (Minefield[i][j].getRevealed()) {
                        // Increment the count of revealed mines
                        countRevealedMines++;
                    }
                }

                // Check if the current cell contains a flag
                if (Minefield[i][j].getStatus().equals("F")) {
                    // Check if the flag is revealed
                    if (Minefield[i][j].getRevealed()) {
                        // Increment the count of revealed flags
                        countRevealedFlags++;
                    }
                }
            }
        }

        // Check if at least 2 mines have been revealed or 1 mine and 1 or more flags
        if (countRevealedMines >= 2 || (countRevealedMines == 1 && countRevealedFlags >= 1)) {
            System.out.println("You have lost!");
            return true;
        }

        // Check if there are no remaining unrevealed mines
        if (remainingMines == 0) {
            System.out.println("You have won, there are no more remaining mines!");
            return true;
        }

        return false;
    }

    /**
     * Reveal the cells that contain zeroes that surround the inputted cell.
     * Continue revealing 0-cells in every direction until no more 0-cells are found in any direction.
     * Utilize a STACK to accomplish this.
     *
     * This method should follow the psuedocode given in the lab writeup.
     * Why might a stack be useful here rather than a queue?
     *
     * @param x      The x value the user entered.
     * @param y      The y value the user entered.
     */
    public void revealZeroes(int x, int y) {
        // Create a stack to store points as integer arrays
        Stack1Gen<int[]> stack = new Stack1Gen<>();
        int[] array = {x, y};
        stack.push(array);

        // Process each point in the stack
        while (!stack.isEmpty()) {
            // Pop the top points from the stack
            int[] current = stack.pop();
            int row = current[0];
            int col = current[1];

            // Check if the cell is within bounds and unrevealed
            if (row >= 0 && row < rows && col >= 0 && col < columns && !Minefield[row][col].getRevealed()) {
                // Mark the current cell as revealed
                Minefield[row][col].setRevealed(true);

                // Check and add neighboring cells with '0' status to the stack
                if (Minefield[row][col].getStatus().equals("0")) {
                    stack.push(new int[]{row, col - 1});
                    stack.push(new int[]{row, col + 1});
                    stack.push(new int[]{row - 1, col});
                    stack.push(new int[]{row + 1, col});
                }
            }
        }
    }

    /**
     * revealStartingArea
     *
     * On the starting move only reveal the neighboring cells of the inital cell and continue revealing the surrounding concealed cells until a mine is found.
     * Utilize a QUEUE to accomplish this.
     * 
     * This method should follow the psuedocode given in the lab writeup.
     * Why might a queue be useful for this function?
     *
     * @param x     The x value the user entered.
     * @param y     The y value the user entered.
     */

    public void revealStartingArea(int x, int y) {
        // Create a stack to store points as integer arrays
        Q1Gen<int[]> queue = new Q1Gen<>();
        int[] array = {x, y};
        queue.add(array);

        while (queue.length() != 0) {
            int[] add  = queue.remove();
            int row = add[0];
            int col = add[1];

            Minefield[row][col].setRevealed(true);

            if (Minefield[row][col].getStatus().equals("M")) {
                return;
            } else {
                if (col - 1 >= 0) {
                    int[] array1 = {row, col - 1};
                    queue.add(array1);
                }

                if (col + 1 < Minefield[0].length) {
                    int[] array2 = {row, col + 1};
                    queue.add(array2);
                }

                if (row - 1 >= 0) {
                    int[] array3 = {row - 1, col};
                    queue.add(array3);
                }

                if (row + 1 < Minefield.length) {
                    int[] array4 = {row + 1, col};
                    queue.add(array4);

                }
            }
        }
    }

    /**
     * For both printing methods utilize the ANSI colour codes provided! 
     * 
     * 
     * 
     * 
     * 
     * debug
     *
     * @function This method should print the entire minefield, regardless if the user has guessed a square.
     * *This method should print out when debug mode has been selected. 
     */
    public void debug() {
        System.out.println("Debugged Minefield: ");

        StringBuilder str = new StringBuilder();
        str.append("  ");

        // Add column labels
        for (int i = 0; i < columns; i++) {
            str.append(i).append(" ");
        }
        str.append("\n");

        // Add minefield content
        for (int i = 0; i < rows; i++) {
            str.append(i).append(" ");
            for (int j = 0; j < columns; j++) {
                String status = Minefield[i][j].getStatus();
                switch (status) {
                    case "0":
                        str.append(ANSI_GREEN).append(status).append(ANSI_GREY_BACKGROUND).append(" ");
                        break;
                    case "1":
                        str.append(ANSI_BLUE_BRIGHT).append(status).append(ANSI_GREY_BACKGROUND).append(" ");
                        break;
                    case "2":
                        str.append(ANSI_RED).append(status).append(ANSI_GREY_BACKGROUND).append(" ");
                        break;
                    case "3":
                        str.append(ANSI_CYAN).append(status).append(ANSI_GREY_BACKGROUND).append(" ");
                        break;
                    case "4":
                        str.append(ANSI_PURPLE).append(status).append(ANSI_GREY_BACKGROUND).append(" ");
                        break;
                    case "5":
                        str.append(ANSI_GREEN).append(status).append(ANSI_GREY_BACKGROUND).append(" ");
                        break;
                    case "6":
                        str.append(ANSI_BLUE).append(status).append(ANSI_GREY_BACKGROUND).append(" ");
                        break;
                    case "7":
                        str.append(ANSI_YELLOW).append(status).append(ANSI_GREY_BACKGROUND).append(" ");
                        break;
                    case "8":
                        str.append(ANSI_CYAN).append(status).append(ANSI_GREY_BACKGROUND).append(" ");
                        break;
                    case "M":
                        str.append(ANSI_RED_BRIGHT).append(status).append(ANSI_GREY_BACKGROUND).append(" ");
                        break;
                    case "F":
                        str.append(ANSI_YELLOW_BRIGHT).append(status).append(ANSI_GREY_BACKGROUND).append(" ");
                        break;
                }
            }
            str.append("\n");
        }

        System.out.println(str);
    }

    /**
     * toString
     *
     * @return String The string that is returned only has the squares that has been revealed to the user or that the user has guessed.
     */
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("  ");

        // Add column labels
        for (int i = 0; i < columns; i++) {
            str.append(i).append(" ");
        }
        str.append("\n");

        // Add minefield content
        for (int i = 0; i < rows; i++) {
            str.append(i).append(" ");
            for (int j = 0; j < columns; j++) {
                if (Minefield[i][j].getRevealed()) {
                    String status = Minefield[i][j].getStatus();
                    switch (status) {
                        case "0":
                            str.append(ANSI_GREEN).append(status).append(ANSI_GREY_BACKGROUND).append(" ");
                            break;
                        case "1":
                            str.append(ANSI_BLUE_BRIGHT).append(status).append(ANSI_GREY_BACKGROUND).append(" ");
                            break;
                        case "2":
                            str.append(ANSI_RED).append(status).append(ANSI_GREY_BACKGROUND).append(" ");
                            break;
                        case "3":
                            str.append(ANSI_CYAN).append(status).append(ANSI_GREY_BACKGROUND).append(" ");
                            break;
                        case "4":
                            str.append(ANSI_PURPLE).append(status).append(ANSI_GREY_BACKGROUND).append(" ");
                            break;
                        case "5":
                            str.append(ANSI_GREEN).append(status).append(ANSI_GREY_BACKGROUND).append(" ");
                            break;
                        case "6":
                            str.append(ANSI_BLUE).append(status).append(ANSI_GREY_BACKGROUND).append(" ");
                            break;
                        case "7":
                            str.append(ANSI_YELLOW).append(status).append(ANSI_GREY_BACKGROUND).append(" ");
                            break;
                        case "8":
                            str.append(ANSI_CYAN).append(status).append(ANSI_GREY_BACKGROUND).append(" ");
                            break;
                        case "M":
                            str.append(ANSI_RED_BRIGHT).append(status).append(ANSI_GREY_BACKGROUND).append(" ");
                            break;
                        case "F":
                            str.append(ANSI_YELLOW_BRIGHT).append(status).append(ANSI_GREY_BACKGROUND).append(" ");
                            break;
                    }
                } else {
                    str.append("- ");
                }
            }
            str.append("\n");
        }

        return str.toString();
    }

    public int getFlags() {
        return flags;
    }
}

// Written by Ayub Mohamoud, moha1660