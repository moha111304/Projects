// Written by Ayub Mohamoud, moha1660

package chess_codepack_F23;

public class Knight {

    /**
     * Constructor.
     * @param row   The current row of the knight.
     * @param col   The current column of the knight.
     * @param isBlack   The color of the knight.
     */
    public Knight(int row, int col, boolean isBlack) {
        this.row = row;
        this.col = col;
        this.isBlack = isBlack;
    }

    /**
     * Checks if a move to a destination square is legal.
     * @param board     The game board.
     * @param endRow    The row of the destination square.
     * @param endCol    The column of the destination square.
     * @return True if the move to the destination square is legal, false otherwise.
     */


    public boolean isMoveLegal(Board board, int endRow, int endCol) {
        int rowDif = Math.abs(endRow - this.row);
        int colDif = Math.abs(endCol - this.col);

        if ((rowDif == 2 && colDif == 1) || (rowDif == 1 && colDif == 2)) {
            // Only Case: Knight moves in an L-shape (2 squares in one direction and 1 square in a perpendicular direction).

            // The destination square is empty or contains an opponent's piece.
            if (board.getPiece(endRow, endCol) == null || (board.getPiece(endRow, endCol).getIsBlack() != this.isBlack)) {
                return true;
            }
        }

        // Default case: Illegal move.
        return false;
    }


    // Instance variables
    private int row;
    private int col;
    private boolean isBlack;
}