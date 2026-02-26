package chess;

import java.util.ArrayList;

import chess.ReturnPlay.Message;

public class Chess {

        static Player currentPlayer;

        static ReturnPiece[][] board;
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
    boolean drawRequested = false;
    if(move == "resign") {
      if(currentPlayer == Player.white) {
        ReturnPlay resign = new ReturnPlay();
        resign.message = Message.RESIGN_BLACK_WINS;
        return resign;
      } else if (currentPlayer == Player.black) {
        ReturnPlay resign = new ReturnPlay();
        resign.message = Message.RESIGN_WHITE_WINS;
        return resign;
      }
    } else if(move.contains("draw?")) {
      drawRequested = true;
    }

    int fileBefore = move.charAt(0) - 'a';
    int rankBefore = move.charAt(1) - '1';
    int fileAfter =  move.charAt(3) - 'a';
    int rankAfter = move.charAt(4) - '1';

    ReturnPiece spotBefore = board[rankBefore][fileBefore];


    if(spotBefore == null) {
      ReturnPlay illegalMove = new ReturnPlay();
      illegalMove.message = Message.ILLEGAL_MOVE;
      return illegalMove;
    } else if(currentPlayer == Player.white && (spotBefore.pieceType.toString().endsWith("BP") || spotBefore.pieceType.toString().endsWith("BR") || spotBefore.pieceType.toString().endsWith("BN") ||
      spotBefore.pieceType.toString().endsWith("BB") || spotBefore.pieceType.toString().endsWith("BQ") || spotBefore.pieceType.toString().endsWith("BK"))) {
      ReturnPlay illegalMove = new ReturnPlay();
      illegalMove.message = Message.ILLEGAL_MOVE;
      return illegalMove;
    } else if(currentPlayer == Player.black && (spotBefore.pieceType.toString().endsWith("WP") || spotBefore.pieceType.toString().endsWith("WR") ||
      spotBefore.pieceType.toString().endsWith("WN") || spotBefore.pieceType.toString().endsWith("WB") ||
      spotBefore.pieceType.toString().endsWith("WQ") || spotBefore.pieceType.toString().endsWith("WK"))) {
      ReturnPlay illegalMove = new ReturnPlay();
      illegalMove.message = Message.ILLEGAL_MOVE;
      return illegalMove;
    } else if("idk".equals(move)) {
      //kuthab do this
    }
    board[rankAfter][fileAfter] = spotBefore;
    board[rankBefore][fileBefore] = null;
    ReturnPlay legalMove = new ReturnPlay();
    if(currentPlayer == Player.white) {
      currentPlayer = Player.black;
    } else if(currentPlayer == Player.black) {
      currentPlayer = Player.white;
    }
    if(drawRequested) {
      ReturnPlay draw = new ReturnPlay();
      draw.message = Message.DRAW;
      return draw;
    }

		return legalMove;
	}


	/**
	 * This method should reset the game, and start from scratch.
	 */
	public static void start() {
		//Initialize Board
    board = new ReturnPiece[8][8];
    for (ReturnPiece.PieceFile f : ReturnPiece.PieceFile.values()) {
      ReturnPiece wp = new ReturnPiece();
      wp.pieceType = ReturnPiece.PieceType.WP;
      wp.pieceFile = f;
      wp.pieceRank = 2;
      board[wp.pieceRank - 1][f.ordinal()] = wp;
    }
    for (ReturnPiece.PieceFile f : ReturnPiece.PieceFile.values()) {
      ReturnPiece bp = new ReturnPiece();
      bp.pieceType = ReturnPiece.PieceType.BP;
      bp.pieceFile = f;
      bp.pieceRank = 7;
      board[bp.pieceRank - 1][f.ordinal()] = bp;
    }

    ReturnPiece.PieceType[] whitePieces = {ReturnPiece.PieceType.WR, ReturnPiece.PieceType.WN, ReturnPiece.PieceType.WB,
      ReturnPiece.PieceType.WQ, ReturnPiece.PieceType.WK, ReturnPiece.PieceType.WB,
      ReturnPiece.PieceType.WN, ReturnPiece.PieceType.WR};

    ReturnPiece.PieceType[] blackPieces = {ReturnPiece.PieceType.BR, ReturnPiece.PieceType.BN, ReturnPiece.PieceType.BB,
      ReturnPiece.PieceType.BQ, ReturnPiece.PieceType.BK, ReturnPiece.PieceType.BB,
      ReturnPiece.PieceType.BN, ReturnPiece.PieceType.BR};

    ReturnPiece.PieceFile[] files = ReturnPiece.PieceFile.values();
    for (int i = 0; i < files.length; i++) {
      ReturnPiece whitePiece = new ReturnPiece();
      whitePiece.pieceType = whitePieces[i];
      whitePiece.pieceFile = files[i];
      whitePiece.pieceRank = 1;
      board[whitePiece.pieceRank - 1][whitePiece.pieceFile.ordinal()] = whitePiece;
    }

    for (int i = 0; i < files.length; i++) {
      ReturnPiece blackPiece = new ReturnPiece();
      blackPiece.pieceType = blackPieces[i];
      blackPiece.pieceFile = files[i];
      blackPiece.pieceRank = 8;
      board[blackPiece.pieceRank - 1][blackPiece.pieceFile.ordinal()] = blackPiece;
    }
    currentPlayer = Player.white;
  }
}
