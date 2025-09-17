// Written by Ayub Mohamoud, moha1660

package chess_codepack_F23;

import java.util.Scanner;
public class Game {

    public static void main(String[] args) {
        Scanner myScanner = new Scanner(System.in);
        Board myBoard = new Board();
        Fen.load("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR", myBoard);
        boolean isWhiteTurn = true; // Initialize with White's turn

        while (true) { // Use a loop, breaks out when the game is over
            System.out.println(myBoard);
            System.out.println("It is currently " + (isWhiteTurn ? "White's" : "Black's") + " turn to play.");
            System.out.println("What is your move? (format: [start row] [start col] [end row] [end col]): ");


            // Read the input as a single line
            String userInput = myScanner.nextLine();

            // Split the input string into an array of integers
            String[] inputArray = userInput.split(" ");

            // Make sure there are exactly four integers in the input
            if (inputArray.length == 4) {
                int startRow = Integer.parseInt(inputArray[0]);
                int startCol = Integer.parseInt(inputArray[1]);
                int endRow = Integer.parseInt(inputArray[2]);
                int endCol = Integer.parseInt(inputArray[3]);


                // Check if the piece at the starting position belongs to the current player
                Piece startPiece = myBoard.getPiece(startRow, startCol);
                if (startPiece != null && startPiece.getIsBlack() == isWhiteTurn) {
                    System.out.println("You can only move your own pieces.");
                } else if (myBoard.movePiece(startRow, startCol, endRow, endCol)) {
                    Piece promotePiece = myBoard.getPiece(endRow, endCol);
                    promotePiece.promotePawn(endRow, promotePiece.getIsBlack());
                    isWhiteTurn = !isWhiteTurn;
                } else {
                    System.out.println("Illegal move. Please try again.");
                }
            } else {
                System.out.println("Invalid input. Please provide four integers separated by spaces.");
            }

            if (myBoard.isGameOver()) {
                System.out.println((isWhiteTurn ? "Black" : "White") + " has won the game!");
                break;
            }
        }
    }
}