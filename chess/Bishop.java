package chess;

public class Bishop implements Validation {
    public boolean isLegalMove(int fileBefore, int rankBefore, int fileAfter, int rankAfter) {
        int fileDiff = Math.abs(fileAfter - fileBefore);
        int rankDiff = Math.abs(rankAfter - rankBefore);

        if (fileDiff != rankDiff || fileDiff == 0) return false;

        ReturnPiece piece = Chess.board[rankBefore][fileBefore];
        ReturnPiece target = Chess.board[rankAfter][fileAfter];

        if (target != null) {
            boolean pieceIsWhite = piece.pieceType.toString().startsWith("W");
            boolean targetIsWhite = target.pieceType.toString().startsWith("W");
            if (pieceIsWhite == targetIsWhite) return false;
        }

        // Check path is clear
        int fileStep = fileAfter > fileBefore ? 1 : -1;
        int rankStep = rankAfter > rankBefore ? 1 : -1;
        int f = fileBefore + fileStep;
        int r = rankBefore + rankStep;
        while (f != fileAfter) {
            if (Chess.board[r][f] != null) return false;
            f += fileStep;
            r += rankStep;
        }

        return true;
    }
}
