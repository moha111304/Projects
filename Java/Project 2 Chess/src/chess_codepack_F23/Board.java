// Written by Ayub Mohamoud, moha1660

package chess_codepack_F23;

public class Board {

    // Instance variables
    private Piece[][] board;

    public Board() {
        board = new Piece[8][8];
    }


    // Accessor Methods
    public Piece getPiece(int row, int col) {
        return board[row][col];
    }

    public void setPiece(int row, int col, Piece piece) {
        board[row][col] = piece;
    }

    // Game functionality methods
    // Checks if move is legal and then moves piece
    public boolean movePiece(int startRow, int startCol, int endRow, int endCol) {
        Piece startPiece = this.getPiece(startRow, startCol);
        if (startPiece != null && startPiece.isMoveLegal(this, endRow, endCol)) {
           this.setPiece(endRow, endCol, startPiece);
           this.setPiece(startRow, startCol, null);
           startPiece.setPosition(endRow, endCol);
           return true;
        }
        return false;
    }

    // Checks if any of the kings have been captured
    public boolean isGameOver() {
        boolean blackKingCaptured = true;
        boolean whiteKingCaptured = true;

        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                Piece piece = this.getPiece(i, j);
                if (piece != null) {
                    if (piece.getCharacter() == '\u265A' || piece.getCharacter() == '\u2654') {
                        if (piece.getIsBlack()) {
                            blackKingCaptured = false;
                        } else {
                            whiteKingCaptured = false;
                        }
                    }
                }
            }
        }
        return blackKingCaptured || whiteKingCaptured;
    }


        // Constructs a String that represents the Board object's 2D array.
        // Returns the fully constructed String.
        public String toString () {
            StringBuilder out = new StringBuilder();
            out.append(" ");
            for (int i = 0; i < 8; i++) {
                out.append(" ");
                out.append(i);
            }
            out.append('\n');
            for (int i = 0; i < board.length; i++) {
                out.append(i);
                out.append("|");
                for (int j = 0; j < board[0].length; j++) {
                    out.append(board[i][j] == null ? "\u2001|" : board[i][j] + "|");
                }
                out.append("\n");
            }
            return out.toString();
        }

        // Clears the board
        public void clear() {
            for (int i = 0; i < 8; i++) {
                for (int j = 0; j < 8; j++) {
                    board[i][j] = null;
                }
            }
        }

        // Movement helper functions

        public boolean verifySourceAndDestination( int startRow, int startCol, int endRow, int endCol, boolean isBlack)
        {   //Checks if  move is within bounds
            if (((startRow >= 0 && startRow <= 7) && (startCol >= 0 && startCol <= 7)) && ((endRow >= 0 && endRow <= 7) && (endCol >= 0 && endCol <= 7))) {
                // Gets piece at start
                Piece startPiece = this.getPiece(startRow, startCol);
                // Checks if the piece exists and is black, gets the endpiece
                if (startPiece != null && startPiece.getIsBlack() == isBlack) {
                    Piece endPiece = this.getPiece(endRow, endCol);
                    // Checks if that piece does not exist or isn't black
                    if (endPiece == null || endPiece.getIsBlack() != isBlack) {
                        return true;
                    }
                }
            }
            return false;
        }

        // Checks if move is adjacent
        public boolean verifyAdjacent ( int startRow, int startCol, int endRow, int endCol){
            return Math.abs(endRow - startRow) <= 1 && Math.abs(endCol - startCol) <= 1;
        }

        // Checks if move is horizontal
        public boolean verifyHorizontal ( int startRow, int startCol, int endRow, int endCol){

            if (startRow != endRow) {
                return false; // Not a horizontal move
            }

            if (startCol == endCol) {
                return true; // No horizontal movement
            }

            int colDir = Integer.compare(endCol, startCol); // Determine the direction of the column movement

            // Ensure that the loop stays within bounds
            for (int i = startCol + colDir; i != endCol; i += colDir) {
                // Check if the new value of 'i' is within the valid range
                if (i < 0 || i >= 8) {
                    return false; // Out of bounds
                }

                // Check if spaces are not empty
                if (board[startRow][i] != null) {
                    return false; // There is a non-empty space along the path
                }
            }

            return true; // It's a valid horizontal move
        }

        // Checks if move is vertical
        public boolean verifyVertical ( int startRow, int startCol, int endRow, int endCol){

            if (startCol != endCol) {
                return false; // Not a vertical move
            }

            if (startRow == endRow) {
                return true; // No vertical movement
            }

            int rowDir = Integer.compare(endRow, startRow); // Determine the direction of the row movement

            // Ensure that the loop stays within bounds
            for (int i = startRow + rowDir; i != endRow; i += rowDir) {
                // Check if the new value of 'i' is within the valid range
                if (i < 0 || i >= 8) {
                    return false; // Out of bounds
                }

                // Check if spaces are not empty
                if (board[i][endCol] != null) {
                    return false; // There is a non-empty space along the path
                }
            }

            return true; // It's a valid vertical move
        }

        // Checks if move is diagonal
        public boolean verifyDiagonal ( int startRow, int startCol, int endRow, int endCol){
            int rowDif = Math.abs(endRow - startRow);
            int colDif = Math.abs(endCol - startCol);

            if (rowDif != colDif) {
                return false; // Not a diagonal move
            }

            int rowDir = Integer.compare(endRow, startRow); // Determine the direction of the row movement
            int colDir = Integer.compare(endCol, startCol); // Determine the direction of the column movement

            for (int i = 1; i < rowDif; i++) {
                int row = startRow + i * rowDir;
                int col = startCol + i * colDir;

                // Check if spaces are not empty
                if (board[row][col] != null) {
                    return false; // There is a non-empty space along the diagonal
                }
            }

            return true; // It's a valid diagonal move
        }
}