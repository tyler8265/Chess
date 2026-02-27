// Names: Tyler Bertrand and Kuthab Ibrahim

package chess;

import java.util.ArrayList;
import chess.ReturnPlay.Message;

public class Chess {

    static Player currentPlayer;
    static ReturnPiece[][] board;

    // Castling rights
    static boolean whiteKingMoved  = false;
    static boolean blackKingMoved  = false;
    static boolean whiteRookAMoved = false;
    static boolean whiteRookHMoved = false;
    static boolean blackRookAMoved = false;
    static boolean blackRookHMoved = false;

    // En passant: file and rank (0-indexed) of the pawn that just double-moved; -1 if none
    static int enPassantFile = -1;
    static int enPassantRank = -1;

    enum Player { white, black }

    /**
     * Plays the next move for whichever player has the turn.
     *
     * @param move String for next move, e.g. "a2 a3"
     *
     * @return A ReturnPlay instance that contains the result of the move.
     *         See the section "The Chess class" in the assignment description for details of
     *         the contents of the returned ReturnPlay instance.
     */
    public static ReturnPlay play(String move) {
        move = move.trim();
        boolean drawOfferThisTurn = move.contains("draw?");

        // Handle resign
        if (move.equals("resign")) {
            ReturnPlay result = new ReturnPlay();
            result.piecesOnBoard = getBoardAsList();
            result.message = currentPlayer == Player.white
                ? Message.RESIGN_BLACK_WINS : Message.RESIGN_WHITE_WINS;
            return result;
        }

        // Parse coordinates
        int fileBefore = move.charAt(0) - 'a';
        int rankBefore = move.charAt(1) - '1';
        int fileAfter  = move.charAt(3) - 'a';
        int rankAfter  = move.charAt(4) - '1';

        // Validate source square
        ReturnPiece spotBefore = board[rankBefore][fileBefore];
        if (spotBefore == null) return illegalMove();

        boolean isWhitePiece = spotBefore.pieceType.toString().startsWith("W");
        if (currentPlayer == Player.white && !isWhitePiece) return illegalMove();
        if (currentPlayer == Player.black &&  isWhitePiece) return illegalMove();

        // Check for castling (king moves 2 squares horizontally)
        boolean isCastling = false;
        boolean isKingSide = false;
        if ((spotBefore.pieceType == ReturnPiece.PieceType.WK
                || spotBefore.pieceType == ReturnPiece.PieceType.BK)
                && Math.abs(fileAfter - fileBefore) == 2 && rankAfter == rankBefore) {
            isKingSide = fileAfter > fileBefore;
            if (!validateCastling(currentPlayer == Player.white, isKingSide)) return illegalMove();
            isCastling = true;
        }

        // Check for en passant (pawn captures diagonally to empty square)
        boolean isEnPassant = false;
        int epCaptureRank = -1, epCaptureFile = -1;
        if ((spotBefore.pieceType == ReturnPiece.PieceType.WP
                || spotBefore.pieceType == ReturnPiece.PieceType.BP)
                && Math.abs(fileAfter - fileBefore) == 1
                && board[rankAfter][fileAfter] == null
                && enPassantFile == fileAfter && enPassantRank == rankBefore) {
            isEnPassant = true;
            epCaptureRank = enPassantRank;
            epCaptureFile = enPassantFile;
        }

        // Validate piece movement (skip for castling, already validated above)
        if (!isCastling) {
            Validation validator = getValidator(spotBefore.pieceType);
            if (validator == null || !validator.isLegalMove(fileBefore, rankBefore, fileAfter, rankAfter)) {
                return illegalMove();
            }
        }

        // Simulate move to verify own king is not left in check
        ReturnPiece savedTarget = board[rankAfter][fileAfter];
        ReturnPiece savedEnPassantCapture = null;

        board[rankAfter][fileAfter] = spotBefore;
        board[rankBefore][fileBefore] = null;
        if (isEnPassant) {
            savedEnPassantCapture = board[epCaptureRank][epCaptureFile];
            board[epCaptureRank][epCaptureFile] = null;
        }

        // Also move rook temporarily for castling
        ReturnPiece savedCastleRookSrc = null;
        ReturnPiece savedCastleRookDst = null;
        int simRookFrom = -1, simRookTo = -1;
        if (isCastling) {
            simRookFrom = isKingSide ? 7 : 0;
            simRookTo = isKingSide ? 5 : 3;
            savedCastleRookSrc = board[rankBefore][simRookFrom];
            savedCastleRookDst = board[rankBefore][simRookTo];
            board[rankBefore][simRookTo] = savedCastleRookSrc;
            board[rankBefore][simRookFrom] = null;
        }

        boolean ownKingInCheck = isInCheck(currentPlayer);

        // Undo simulation
        board[rankBefore][fileBefore] = spotBefore;
        board[rankAfter][fileAfter] = savedTarget;
        if (isEnPassant) {
            board[epCaptureRank][epCaptureFile] = savedEnPassantCapture;
        }
        if (isCastling) {
            board[rankBefore][simRookFrom] = savedCastleRookSrc;
            board[rankBefore][simRookTo] = savedCastleRookDst;
        }

        if (ownKingInCheck) return illegalMove();

        // --- Commit the move ---

        board[rankAfter][fileAfter] = spotBefore;
        board[rankBefore][fileBefore] = null;
        spotBefore.pieceFile = ReturnPiece.PieceFile.values()[fileAfter];
        spotBefore.pieceRank = rankAfter + 1;

        // Remove en passant captured pawn
        if (isEnPassant) {
            board[epCaptureRank][epCaptureFile] = null;
        }

        // Move rook for castling
        if (isCastling) {
            int rank = rankBefore;
            int rookFromFile = isKingSide ? 7 : 0;
            int rookToFile   = isKingSide ? 5 : 3;
            ReturnPiece rook = board[rank][rookFromFile];
            board[rank][rookToFile]   = rook;
            board[rank][rookFromFile] = null;
            rook.pieceFile = ReturnPiece.PieceFile.values()[rookToFile];
            if (currentPlayer == Player.white) {
                if (isKingSide) whiteRookHMoved = true; else whiteRookAMoved = true;
            } else {
                if (isKingSide) blackRookHMoved = true; else blackRookAMoved = true;
            }
        }

        // Update castling rights for moved piece (before promotion changes type)
        updateCastlingRights(spotBefore, fileBefore, rankBefore);

        // Update castling rights if a rook was captured at its starting square
        if (fileAfter == 0 && rankAfter == 0) whiteRookAMoved = true;
        if (fileAfter == 7 && rankAfter == 0) whiteRookHMoved = true;
        if (fileAfter == 0 && rankAfter == 7) blackRookAMoved = true;
        if (fileAfter == 7 && rankAfter == 7) blackRookHMoved = true;

        // Pawn promotion
        ReturnPiece.PieceType originalType = spotBefore.pieceType;
        if (spotBefore.pieceType == ReturnPiece.PieceType.WP && rankAfter == 7) {
            char promo = getPromoChar(move);
            spotBefore.pieceType = getPromotionType(true, promo);
        } else if (spotBefore.pieceType == ReturnPiece.PieceType.BP && rankAfter == 0) {
            char promo = getPromoChar(move);
            spotBefore.pieceType = getPromotionType(false, promo);
        }

        // Update en passant state for next turn
        if ((originalType == ReturnPiece.PieceType.WP || originalType == ReturnPiece.PieceType.BP)
                && Math.abs(rankAfter - rankBefore) == 2) {
            enPassantFile = fileAfter;
            enPassantRank = rankAfter;
        } else {
            enPassantFile = -1;
            enPassantRank = -1;
        }

        // Switch player
        Player movingPlayer = currentPlayer;
        currentPlayer = currentPlayer == Player.white ? Player.black : Player.white;

        // Build result
        ReturnPlay result = new ReturnPlay();
        result.piecesOnBoard = getBoardAsList();

        // Draw: the other player is obligated to accept
        if (drawOfferThisTurn) {
            result.message = Message.DRAW;
            return result;
        }

        // Check / checkmate detection
        if (isInCheck(currentPlayer)) {
            if (isCheckmate(currentPlayer)) {
                result.message = movingPlayer == Player.white
                    ? Message.CHECKMATE_WHITE_WINS : Message.CHECKMATE_BLACK_WINS;
                return result;
            }
            result.message = Message.CHECK;
        }

        return result;
    }

    /**
     * This method should reset the game, and start from scratch.
     */
    public static void start() {
        board = new ReturnPiece[8][8];
        whiteKingMoved  = false;
        blackKingMoved  = false;
        whiteRookAMoved = false;
        whiteRookHMoved = false;
        blackRookAMoved = false;
        blackRookHMoved = false;
        enPassantFile   = -1;
        enPassantRank   = -1;

        // White pawns on rank 2
        for (ReturnPiece.PieceFile f : ReturnPiece.PieceFile.values()) {
            ReturnPiece wp = new ReturnPiece();
            wp.pieceType = ReturnPiece.PieceType.WP;
            wp.pieceFile = f;
            wp.pieceRank = 2;
            board[1][f.ordinal()] = wp;
        }

        // Black pawns on rank 7
        for (ReturnPiece.PieceFile f : ReturnPiece.PieceFile.values()) {
            ReturnPiece bp = new ReturnPiece();
            bp.pieceType = ReturnPiece.PieceType.BP;
            bp.pieceFile = f;
            bp.pieceRank = 7;
            board[6][f.ordinal()] = bp;
        }

        ReturnPiece.PieceType[] whitePieces = {
            ReturnPiece.PieceType.WR, ReturnPiece.PieceType.WN, ReturnPiece.PieceType.WB,
            ReturnPiece.PieceType.WQ, ReturnPiece.PieceType.WK, ReturnPiece.PieceType.WB,
            ReturnPiece.PieceType.WN, ReturnPiece.PieceType.WR
        };
        ReturnPiece.PieceType[] blackPieces = {
            ReturnPiece.PieceType.BR, ReturnPiece.PieceType.BN, ReturnPiece.PieceType.BB,
            ReturnPiece.PieceType.BQ, ReturnPiece.PieceType.BK, ReturnPiece.PieceType.BB,
            ReturnPiece.PieceType.BN, ReturnPiece.PieceType.BR
        };

        ReturnPiece.PieceFile[] files = ReturnPiece.PieceFile.values();
        for (int i = 0; i < 8; i++) {
            ReturnPiece whitePiece = new ReturnPiece();
            whitePiece.pieceType = whitePieces[i];
            whitePiece.pieceFile = files[i];
            whitePiece.pieceRank = 1;
            board[0][i] = whitePiece;

            ReturnPiece blackPiece = new ReturnPiece();
            blackPiece.pieceType = blackPieces[i];
            blackPiece.pieceFile = files[i];
            blackPiece.pieceRank = 8;
            board[7][i] = blackPiece;
        }

        currentPlayer = Player.white;
    }

    public static boolean isInCheck(Player player) {
        int[] kingPos = getKingPosition(player);
        int kingRank = kingPos[0];
        int kingFile = kingPos[1];

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                ReturnPiece piece = board[i][j];
                if (piece == null) continue;
                boolean pieceIsWhite = piece.pieceType.toString().startsWith("W");
                boolean playerIsWhite = player == Player.white;
                if (pieceIsWhite == playerIsWhite) continue; // same team

                Validation validator = getValidator(piece.pieceType);
                if (validator != null && validator.isLegalMove(j, i, kingFile, kingRank)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static int[] getKingPosition(Player player) {
        int[] kingPosition = new int[2];
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (board[i][j] != null) {
                    if (player == Player.white && board[i][j].pieceType == ReturnPiece.PieceType.WK) {
                        kingPosition[0] = i;
                        kingPosition[1] = j;
                        return kingPosition;
                    } else if (player == Player.black && board[i][j].pieceType == ReturnPiece.PieceType.BK) {
                        kingPosition[0] = i;
                        kingPosition[1] = j;
                        return kingPosition;
                    }
                }
            }
        }
        return kingPosition;
    }

    private static boolean isCheckmate(Player player) {
        for (int fromRank = 0; fromRank < 8; fromRank++) {
            for (int fromFile = 0; fromFile < 8; fromFile++) {
                ReturnPiece piece = board[fromRank][fromFile];
                if (piece == null) continue;
                boolean pieceIsWhite = piece.pieceType.toString().startsWith("W");
                if (pieceIsWhite != (player == Player.white)) continue;

                Validation validator = getValidator(piece.pieceType);
                if (validator == null) continue;

                for (int toRank = 0; toRank < 8; toRank++) {
                    for (int toFile = 0; toFile < 8; toFile++) {
                        if (fromRank == toRank && fromFile == toFile) continue;
                        if (!validator.isLegalMove(fromFile, fromRank, toFile, toRank)) continue;

                        // Simulate move
                        ReturnPiece savedTarget = board[toRank][toFile];
                        board[toRank][toFile]     = piece;
                        board[fromRank][fromFile] = null;

                        // If en passant pawn capture, also remove captured pawn
                        ReturnPiece savedEpCapture = null;
                        boolean isEpEscape = false;
                        if ((piece.pieceType == ReturnPiece.PieceType.WP || piece.pieceType == ReturnPiece.PieceType.BP)
                            && Math.abs(toFile - fromFile) == 1
                            && savedTarget == null
                            && toFile == enPassantFile) {
                            isEpEscape = true;
                            savedEpCapture = board[fromRank][enPassantFile];
                            board[fromRank][enPassantFile] = null;
                        }

                        boolean stillInCheck = isInCheck(player);

                        board[fromRank][fromFile] = piece;
                        board[toRank][toFile]     = savedTarget;
                        if (isEpEscape) {
                            board[fromRank][enPassantFile] = savedEpCapture;
                        }

                        if (!stillInCheck) return false;
                    }
                }
            }
        }
        return true;
    }

    private static boolean validateCastling(boolean isWhite, boolean isKingSide) {
        if (isWhite) {
            if (whiteKingMoved) return false;
            if (isKingSide  && whiteRookHMoved) return false;
            if (!isKingSide && whiteRookAMoved) return false;
        } else {
            if (blackKingMoved) return false;
            if (isKingSide  && blackRookHMoved) return false;
            if (!isKingSide && blackRookAMoved) return false;
        }

        int rank = isWhite ? 0 : 7;
        Player player = isWhite ? Player.white : Player.black;

        // Squares between king and rook must be empty
        if (isKingSide) {
            if (board[rank][5] != null || board[rank][6] != null) return false;
        } else {
            if (board[rank][1] != null || board[rank][2] != null || board[rank][3] != null) return false;
        }

        // King must not currently be in check
        if (isInCheck(player)) return false;

        // King must not pass through an attacked square
        ReturnPiece king = board[rank][4];
        int passThroughFile = isKingSide ? 5 : 3;
        board[rank][passThroughFile] = king;
        board[rank][4] = null;
        boolean passesCheck = isInCheck(player);
        board[rank][4] = king;
        board[rank][passThroughFile] = null;
        if (passesCheck) return false;

        return true;
    }

    private static void updateCastlingRights(ReturnPiece piece, int fromFile, int fromRank) {
        switch (piece.pieceType) {
            case WK: whiteKingMoved = true; break;
            case BK: blackKingMoved = true; break;
            case WR:
                if (fromFile == 0 && fromRank == 0) whiteRookAMoved = true;
                if (fromFile == 7 && fromRank == 0) whiteRookHMoved = true;
                break;
            case BR:
                if (fromFile == 0 && fromRank == 7) blackRookAMoved = true;
                if (fromFile == 7 && fromRank == 7) blackRookHMoved = true;
                break;
            default: break;
        }
    }

    static Validation getValidator(ReturnPiece.PieceType type) {
        switch (type) {
            case WP: case BP: return new Pawn();
            case WR: case BR: return new Rook();
            case WN: case BN: return new Knight();
            case WB: case BB: return new Bishop();
            case WQ: case BQ: return new Queen();
            case WK: case BK: return new King();
            default: return null;
        }
    }

    private static char getPromoChar(String move) {
        // Promotion piece is at index 6: "e7 e8 Q" or "e7 e8 Q draw?"
        if (move.length() >= 7) {
            char c = move.charAt(6);
            if (c == 'R' || c == 'N' || c == 'B' || c == 'Q') return c;
        }
        return 'Q'; // default to queen
    }

    private static ReturnPiece.PieceType getPromotionType(boolean isWhite, char c) {
        switch (c) {
            case 'R': return isWhite ? ReturnPiece.PieceType.WR : ReturnPiece.PieceType.BR;
            case 'N': return isWhite ? ReturnPiece.PieceType.WN : ReturnPiece.PieceType.BN;
            case 'B': return isWhite ? ReturnPiece.PieceType.WB : ReturnPiece.PieceType.BB;
            default:  return isWhite ? ReturnPiece.PieceType.WQ : ReturnPiece.PieceType.BQ;
        }
    }

    static ArrayList<ReturnPiece> getBoardAsList() {
        ArrayList<ReturnPiece> pieces = new ArrayList<>();
        for (int r = 0; r < 8; r++) {
            for (int f = 0; f < 8; f++) {
                if (board[r][f] != null) pieces.add(board[r][f]);
            }
        }
        return pieces;
    }

    private static ReturnPlay illegalMove() {
        ReturnPlay result = new ReturnPlay();
        result.piecesOnBoard = getBoardAsList();
        result.message = Message.ILLEGAL_MOVE;
        return result;
    }
}
