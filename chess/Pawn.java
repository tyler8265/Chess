package chess;

public class Pawn implements Validation {
    public boolean isLegalMove(int fileBefore, int rankBefore, int fileAfter, int rankAfter) {
        ReturnPiece piece = Chess.board[rankBefore][fileBefore];
        if (piece == null) return false;

        boolean isWhite = piece.pieceType == ReturnPiece.PieceType.WP;
        int direction = isWhite ? 1 : -1;
        int startRank = isWhite ? 1 : 6; // 0-indexed

        // Forward one square
        if (fileAfter == fileBefore && rankAfter == rankBefore + direction) {
            return Chess.board[rankAfter][fileAfter] == null;
        }

        // Forward two squares from starting rank
        if (fileAfter == fileBefore && rankAfter == rankBefore + 2 * direction && rankBefore == startRank) {
            return Chess.board[rankBefore + direction][fileBefore] == null
                && Chess.board[rankAfter][fileAfter] == null;
        }

        // Diagonal capture or en passant
        if (Math.abs(fileAfter - fileBefore) == 1 && rankAfter == rankBefore + direction) {
            ReturnPiece target = Chess.board[rankAfter][fileAfter];
            if (target != null) {
                boolean targetIsWhite = target.pieceType.toString().startsWith("W");
                return isWhite != targetIsWhite;
            }
            // En passant: destination is empty but en passant target pawn is on same rank
            if (Chess.enPassantFile == fileAfter && Chess.enPassantRank == rankBefore) {
                return true;
            }
        }

        return false;
    }
}
