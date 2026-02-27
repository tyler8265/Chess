package chess;

public class Queen implements Validation {
    public boolean isLegalMove(int fileBefore, int rankBefore, int fileAfter, int rankAfter) {
        return new Rook().isLegalMove(fileBefore, rankBefore, fileAfter, rankAfter)
            || new Bishop().isLegalMove(fileBefore, rankBefore, fileAfter, rankAfter);
    }
}
