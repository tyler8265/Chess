package chess;

public interface Validation {
  boolean isLegalMove(int fileBefore, int rankBefore, int fileAfter, int rankAfter);
}
