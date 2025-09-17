// Written by Ayub Mohamoud, moha1660

package chess_codepack_F23;

public class Rook {

    /**
     * Constructor.
     * @param row   The current row of the rook.
     * @param col   The current column of the rook.
     * @param isBlack   The color of the rook.
     */
    public Rook(int row, int col, boolean isBlack) {
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
        if (board.verifyVertical(this.row, this.col, endRow, endCol) || board.verifyHorizontal(this.row, this.col, endRow, endCol)) {

            if (board.getPiece(endRow, endCol) == null) {
                // Case 1: Vertical or Horizontal movement to an empty square.
                return (endCol == this.col) || (endRow == this.row);

            } else if (board.getPiece(endRow, endCol) != null && board.getPiece(endRow, endCol).getIsBlack() != this.isBlack) {
                // Case 2: Capturing a piece.
                // There is a piece of the opposite color to be captured.
                return (endCol == this.col) || (endRow == this.row);
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