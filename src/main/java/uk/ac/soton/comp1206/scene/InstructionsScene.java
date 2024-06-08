package uk.ac.soton.comp1206.scene;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.PieceBoard;
import uk.ac.soton.comp1206.game.GamePiece;
import uk.ac.soton.comp1206.media.Multimedia;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import java.io.InputStream;

/**
 * Holds the instructions of how the game is played
 */
public class InstructionsScene extends BaseScene{

  private static final Logger logger = LogManager.getLogger(InstructionsScene.class);

  private boolean instructionsLoaded = false;
  private ImageView imageView;

  /**
   * Constructs the instructions scene
   * @param gameWindow the instruction's window
   */
  public InstructionsScene(GameWindow gameWindow){
    super(gameWindow);
    logger.info("Creating the instructions scene");
  }

  @Override
  public void initialise() {
    getScene().setOnKeyPressed(this::handleKeyPress);
    Multimedia.playBackgroundMusic("menu.mp3");
  }

  /**
   * Handle key press events
   * @param event the key event
   */
  private void handleKeyPress(KeyEvent event) {
    Platform.runLater(() -> {
      if (event.getCode() == KeyCode.ESCAPE) {
        gameWindow.startMenu();
        Multimedia.stopBackground();
        gameWindow.cleanup();
      }
    });
  }

  /**
   * Creates and displays the instructions panel
   */
  @Override
  public void build() {
    logger.info("Building " + this.getClass().getName());
    root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());

    if (!instructionsLoaded) {
      VBox instructionsPane = createInstructionsPane();
      HBox pieceDiagram = createPieceDiagram();
      root.getChildren().addAll(instructionsPane, pieceDiagram);

      instructionsLoaded = true;
    }
  }

  /**
   * Creates the instructions pane containing the image
   * @return VBox containing the instructions
   */
  private VBox createInstructionsPane() {
    VBox instructionsPane = new VBox();
    instructionsPane.setAlignment(Pos.TOP_CENTER);
    instructionsPane.getStyleClass().add("menu-background");
    imageView = createInstructionsImageView();
    instructionsPane.getChildren().add(imageView);
    return instructionsPane;
  }

  /**
   * Creates the image view of the instructions
   * @return ImageView of the instructions
   */
  private ImageView createInstructionsImageView() {
    InputStream inputStream = getClass().getResourceAsStream("/images/Instructions.png");
    Image instructionsImage = new Image(inputStream);
    ImageView imageView = new ImageView(instructionsImage);
    imageView.setFitWidth(550);
    imageView.setPreserveRatio(true);
    logger.info("Created the instruction's image");
    return imageView;
  }

  /**
   * Creates the piece diagram using a HBox and a VBox
   * @return HBox containing the piece diagram
   */
  private HBox createPieceDiagram() {
    HBox pieceDiagram = new HBox();
    pieceDiagram.setAlignment(Pos.BOTTOM_CENTER);
    pieceDiagram.setSpacing(20);

    int totalPieces = 15;
    int piecesPerRow = 3;

    for (int i = 0; i < totalPieces; i += piecesPerRow) {
      VBox vbox = new VBox();
      vbox.setAlignment(Pos.BOTTOM_CENTER);
      vbox.setSpacing(5);

      for (int j = 0; j < piecesPerRow; j++) {
        PieceBoard piece = new PieceBoard((double) gameWindow.getWidth() / 15, (double) gameWindow.getHeight() / 10);
        int pieceIndex = i + j;
        if (pieceIndex < totalPieces) {
          Label label = new Label(GamePiece.createPiece(pieceIndex).toString());
          label.setStyle("-fx-text-fill: white;");
          piece.showPiece(GamePiece.createPiece(pieceIndex));
          vbox.getChildren().addAll(piece, label);
        }
      }

      pieceDiagram.getChildren().add(vbox);
    }
    logger.info("Created the piece diagram");
    return pieceDiagram;
  }
}
