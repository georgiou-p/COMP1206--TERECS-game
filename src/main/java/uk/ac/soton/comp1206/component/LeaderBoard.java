package uk.ac.soton.comp1206.component;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * Stores the leaderboard list for the multiplayer game
 */
public class LeaderBoard extends ScoresList{

  private static final Logger logger = LogManager.getLogger(LeaderBoard.class);

  /**
   * ObservableList to store the scores of the players
   */
  private final ObservableList<Pair<String, Integer>> playerScores;

  private final StringProperty title = new SimpleStringProperty();


  /**
   * Constructs a LeaderBoard with the given player scores
   * @param playerScores the name and score of the players
   */
  public LeaderBoard(ObservableList<Pair<String, Integer>> playerScores){
    this.playerScores = playerScores;
    scores.set(playerScores);
  }

  /**
   * Retrieves the player scores
   *
   * @return ObservableList containing player scores
   */
  public ObservableValue<? extends ObservableList<Pair<String, Integer>>> getPlayerScores() {
    return (ObservableValue<? extends ObservableList<Pair<String, Integer>>>) playerScores;
  }


}
