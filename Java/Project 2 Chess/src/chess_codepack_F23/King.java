// Written by Ayub Mohamoud, moha1660

package chess_codepack_F23;

public class King {

    /**
     * Constructor.
     * @param row   The current row of the king.
     * @param col   The current column of the king.
     * @param isBlack   The color of the king.
     */
    public King(int row, int col, boolean isBlack) {
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
        if (board.verifyAdjacent(this.row, this.col, endRow, endCol)) {

            if (board.getPiece(endRow, endCol) == null) {
                // Case 1: Adjacent movement to empty square.
                return true;

            } else if ((board.getPiece(endRow, endCol) != null) && board.getPiece(endRow, endCol).getIsBlack() != this.isBlack) {
                // Case 2: Capturing a piece.
                // There is a piece of the opposite color to be captured.
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