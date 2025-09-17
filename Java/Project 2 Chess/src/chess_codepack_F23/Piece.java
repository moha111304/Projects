// Written by Ayub Mohamoud, moha1660

package chess_codepack_F23;

import java.util.Scanner;

public class Piece {
    private char character;
    private int row;
    private int col;
    private boolean isBlack;


    /**
     * Constructor.
     *
     * @param character The character representing the piece.
     * @param row       The row on the board the piece occupies.
     * @param col       The column on the board the piece occupies.
     * @param isBlack   The color of the piece.
     */
    public Piece(char character, int row, int col, boolean isBlack) {
        this.character = character;
        this.row = row;
        this.col = col;
        this.isBlack = isBlack;
    }

    // Accessor methods

    public int getRow() {
        return row;
    }

    public void setRow (int newRow) {
        row = newRow;
    }

    public int getCol() {
        return col;
    }

    public void setCol (int newCol) {
        col = newCol;
    }

    public char getCharacter() {
        return character;
    }

    public void setCharacter (char newCharacter) {
        character = newCharacter;
    }

    /**
     * Determines if moving this piece is legal.
     *
     * @param board  The current state of the board.
     * @param endRow The destination row of the move.
     * @param endCol The destination column of the move.
     * @return If the piece can legally move to the provided destination on the board.
     */
    public boolean isMoveLegal(Board board, int endRow, int endCol) {
        switch (this.character) {
            case '\u2659':
            case '\u265f':
                Pawn pawn = new Pawn(row, col, isBlack);
                return pawn.isMoveLegal(board, endRow, endCol);
            case '\u2656':
            case '\u265c':
                Rook rook = new Rook(row, col, isBlack);
                return rook.isMoveLegal(board, endRow, endCol);
            case '\u265e':
            case '\u2658':
                Knight knight = new Knight(row, col, isBlack);
                return knight.isMoveLegal(board, endRow, endCol);
            case '\u265d':
            case '\u2657':
                Bishop bishop = new Bishop(row, col, isBlack);
                return bishop.isMoveLegal(board, endRow, endCol);
            case '\u265b':
            case '\u2655':
                Queen queen = new Queen(row, col, isBlack);
                return queen.isMoveLegal(board, endRow, endCol);
            case '\u265a':
            case '\u2654':
                King king = new King(row, col, isBlack);
                return king.isMoveLegal(board, endRow, endCol);
            default:
                return false;
        }
    }

    /**
     * Sets the position of the piece.
     *
     * @param row The row to move the piece to.
     * @param col The column to move the piece to.
     */
    public void setPosition(int row, int col) {
        this.row = row;
        this.col = col;
    }

    /**
     * Return the color of the piece.
     *
     * @return The color of the piece.
     */
    public boolean getIsBlack() {
        return isBlack;
    }

    /**
     * Handle promotion of a pawn.
     *
     * @param row     Current row of the pawn
     * @param isBlack Color of the pawn
     */
    public void promotePawn(int row, boolean isBlack) {
        Scanner myScanner = new Scanner(System.in);
        if (this.getCharacter() == '\u265F' || this.getCharacter() == '\u2659') {

            if (row == 7 && isBlack) {
                System.out.println("Enter the piece to promote black pawn to: ");
                String inputBlack = myScanner.nextLine();
                // Promote a black pawn at the bottom row to another piece
                if (inputBlack.equals("Queen")) {
                    // Set as Queen piece
                    this.setCharacter('\u265B');

                } else if (inputBlack.equals("Rook")) {
                    // Set as Rook piece
                    this.setCharacter('\u265C');

                } else if (inputBlack.equals("Knight")) {
                    // Set as Knight piece
                    this.setCharacter('\u265E');

                } else if (inputBlack.equals("Bishop")) {
                    // Set as Bishop piece
                    this.setCharacter('\u265D');
                }

            } else if (row == 0 && !isBlack) {
                System.out.println("Enter the piece to promote white pawn to: ");
                String inputWhite = myScanner.nextLine();
                // Promote a white pawn at the top row to another piece
                if (inputWhite.equals("Queen")) {
                    // Set as Queen piece
                    this.setCharacter('\u2655');

                } else if (inputWhite.equals("Rook")) {
                    // Set as new Rook piece
                    this.setCharacter('\u2656');

                } else if (inputWhite.equals("Knight")) {
                    // Set as new Knight piece
                    this.setCharacter('\u2658');

                } else if (inputWhite.equals("Bishop")) {
                    // Set as new Bishop piece
                    this.setCharacter('\u2657');

                }
            }
        }
    }

    /**
     * Returns a string representation of the piece.
     * @return  A string representation of the piece.
     */
    public String toString() {
        return String.valueOf(character);
    }
}