package uk.ac.soton.comp1206.game;

import java.util.concurrent.TimeUnit;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Responsible for the game logic of the multiplayer
 */
public class MultiplayerGame extends Game {

  private static final Logger logger = LogManager.getLogger(MultiplayerGame.class);

  /**
   * The pieces to be played in the multiplayer game
   */
  public final ListProperty<Integer> pieces = new SimpleListProperty<>();

  /**
   * The specifi number of the piece in order to be created
   */
  private static int pieceNum = 0;

  /**
   * Create a new game with the specified rows and columns. Creates a corresponding grid model.
   *
   * @param cols number of columns
   * @param rows number of rows
   */
  public MultiplayerGame(int cols, int rows) {
    super(cols, rows);
  }

  @Override
  /**
   * Generates a GamePiece based on the order of the server
   */
  public GamePiece spawnPiece(){
    int pieceNo = pieces.get(pieceNum);
    GamePiece play = GamePiece.createPiece(pieceNo);
    pieceNum ++;
    return play;
  }

  @Override
  /**
   * Starts the multiplayer game
   */
  public void start(){
    logger.info("Starting game");
    loop = timer.schedule(this::gameLoop, getTimerDelay(), TimeUnit.MILLISECONDS);
    gameLoopListener();

    pieces.addListener((observableValue, integers, t1) -> {
      if (pieces.size() == 5){
        followingPiece = spawnPiece();
        nextPiece();
      }
    });
  }

  /**
   * Returns the piece list
   * @return the list with the pieces
   */
  public ListProperty<Integer> pieceProperty(){
    return pieces;
  }


}
