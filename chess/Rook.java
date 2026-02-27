package chess;

public class Rook implements Validation {
    public boolean isLegalMove(int fileBefore, int rankBefore, int fileAfter, int rankAfter) {
        if (fileBefore != fileAfter && rankBefore != rankAfter) return false;
        if (fileBefore == fileAfter && rankBefore == rankAfter) return false;

        ReturnPiece piece = Chess.board[rankBefore][fileBefore];
        ReturnPiece target = Chess.board[rankAfter][fileAfter];

        // Can't capture own piece
        if (target != null) {
            boolean pieceIsWhite = piece.pieceType.toString().startsWith("W");
            boolean targetIsWhite = target.pieceType.toString().startsWith("W");
            if (pieceIsWhite == targetIsWhite) return false;
        }

        // Check path is clear
        if (fileBefore == fileAfter) {
            int minRank = Math.min(rankBefore, rankAfter);
            int maxRank = Math.max(rankBefore, rankAfter);
            for (int r = minRank + 1; r < maxRank; r++) {
                if (Chess.board[r][fileBefore] != null) return false;
            }
        } else {
            int minFile = Math.min(fileBefore, fileAfter);
            int maxFile = Math.max(fileBefore, fileAfter);
            for (int f = minFile + 1; f < maxFile; f++) {
                if (Chess.board[rankBefore][f] != null) return false;
            }
        }

        return true;
    }
}
