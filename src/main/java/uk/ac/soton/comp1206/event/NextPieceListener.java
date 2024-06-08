package uk.ac.soton.comp1206.event;

import uk.ac.soton.comp1206.game.GamePiece;

/**
 * Listener for providing the next available piece
 */
public interface NextPieceListener {

  /**
   * Handles the event for providing the next piece
   * @param piece played now
   * @param following played after
   */
  void nextPiece(GamePiece piece, GamePiece following);

}
