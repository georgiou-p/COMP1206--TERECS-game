package uk.ac.soton.comp1206.component;

import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.game.GamePiece;

/**
 * PieceBoard class is responsible for creating and displaying boards of pieces
 */
public class PieceBoard extends GameBoard{
  private final Logger logger = LogManager.getLogger(PieceBoard.class);

  /**
   * Create a piece board for a piece
   * @param width width of the board
   * @param height height of the board
   */
  public PieceBoard(double width, double height){
    super(3, 3, width , height);
    build();
  }

  /**
   * Displays the next piece
   * @param gamePiece the piece played
   */
  public void showPiece(GamePiece gamePiece){
    for (int i = 0; i < 3; i ++){
      for (int j = 0; j < 3; j++) {
        this.grid.set(i, j, gamePiece.getBlocks()[i][j]);
      }
    }
    logger.info("Displayed a piece on the piece board");

  }

  /**
   * Adds the center dot in the pieces to be played
   */
  public void addDot(){
    logger.info("Adding the centre dot");
    BorderPane pane = new BorderPane();
    Circle circle = new Circle();
    circle.setFill(Color.rgb(255,255,255,0.5));
    double centreX = getWidth() /2;
    double centreY = getHeight() / 2;
    double radius = Math.min(getWidth(), getHeight()) /16;
    circle.setCenterX(centreX);
    circle.setCenterY(centreY);
    circle.setRadius(radius);
    pane.setCenter(circle);
    add(pane,1,1);
  }

  @Override
  public void hover(GameBlock gameBlock) {
    // Do nothing to prevent hovering effect
  }
}
