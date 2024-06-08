package uk.ac.soton.comp1206.event;

import java.util.HashSet;
import uk.ac.soton.comp1206.component.GameBlockCoordinate;

/**
 * Takes a set of GameBlock coordinates and adds it the Game class
 */
public interface LineClearedListener {

  /**
   * Handles the event when the line is cleared
   * @param lineCleared th line cleared
   */
  void lineCleared(HashSet<GameBlockCoordinate> lineCleared);
}
