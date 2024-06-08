package uk.ac.soton.comp1206.event;

/**
 * Listener for looping the game when a life is lost
 */
public interface GameLoopListener {

  /**
   * Handles the event when a life is lost
   * @param delay The delay the timer is supposed to last
   */
  void gameLoop(int delay);

}
