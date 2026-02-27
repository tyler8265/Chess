package chess;

public class Knight implements Validation {
    public boolean isLegalMove(int fileBefore, int rankBefore, int fileAfter, int rankAfter) {
        int fileDiff = Math.abs(fileAfter - fileBefore);
        int rankDiff = Math.abs(rankAfter - rankBefore);

        if (!((fileDiff == 1 && rankDiff == 2) || (fileDiff == 2 && rankDiff == 1))) return false;

        ReturnPiece piece = Chess.board[rankBefore][fileBefore];
        ReturnPiece target = Chess.board[rankAfter][fileAfter];

        if (target != null) {
            boolean pieceIsWhite = piece.pieceType.toString().startsWith("W");
            boolean targetIsWhite = target.pieceType.toString().startsWith("W");
            if (pieceIsWhite == targetIsWhite) return false;
        }

        return true;
    }
}
