package uk.ac.soton.comp1206.scene;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.component.GameBlockCoordinate;
import uk.ac.soton.comp1206.component.GameBoard;
import uk.ac.soton.comp1206.component.PieceBoard;
import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.game.GamePiece;
import uk.ac.soton.comp1206.media.Multimedia;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

/**
 * The Single Player challenge scene. Holds the UI for the single player challenge mode in the game.
 */
public class ChallengeScene extends BaseScene {

    protected static final Logger logger = LogManager.getLogger(MenuScene.class);

    /**
     * The game played
     */
    protected Game game;

    /**
     * Labels for the in-game information
      */
   protected Label scoreLabel;
    /**
     * Labels for the in-game information
     */
    protected Label levelLabel;
    /**
     * Labels for the in-game information
     */
    protected Label multiplierLabel;
    /**
     * Labels for the in-game information
     */
    protected Label livesLabel;
    /**
     * Labels for the in-game information
     */
    protected Label highScoreLabel;
    /**
     * Property binding the high score
     */
    protected IntegerProperty highScoreProperty = new SimpleIntegerProperty();

    /**
     * The piece currently played
     */
    protected PieceBoard currentPiece;
    /**
     * The piece to be played next
      */
    protected PieceBoard nextPiece;

    /**
     * Projects the players staus
     */
    protected VBox infoBox;

    /**
     * The x and y coordinates
     */
    protected int x= 0, y= 0;

    /**
     * The board played on
     */
    protected GameBoard board;

    /**
     * The time to play a piece
     */
    protected Rectangle timer;

    /**
     * Labels for the in-game information
     */
    protected Label titleLabel;

    /**
     * Create a new Single Player challenge scene
     * @param gameWindow the Game Window
     */
    public ChallengeScene(GameWindow gameWindow) {
        super(gameWindow);
        logger.info("Creating Challenge Scene");
    }

    /**
     * Build the Challenge window
     */
    @Override
    public void build() {
        logger.info("Building " + this.getClass().getName());

        setupGame();

        root = new GamePane(gameWindow.getWidth(),gameWindow.getHeight());

        var challengePane = new StackPane();
        challengePane.setMaxWidth(gameWindow.getWidth());
        challengePane.setMaxHeight(gameWindow.getHeight());
        challengePane.getStyleClass().add("menu-background");
        root.getChildren().add(challengePane);
        var mainPane = new BorderPane();
        challengePane.getChildren().add(mainPane);

        // Add title at the top center
         titleLabel = new Label("THE GAME");
        titleLabel.getStyleClass().add("menuItem");
        titleLabel.setPadding(new Insets(10, 0, 0, 0));
        mainPane.setTop(titleLabel);
        BorderPane.setAlignment(titleLabel, Pos.CENTER);

        board = new GameBoard(game.getGrid(),
            (double) gameWindow.getWidth() /2, (double) gameWindow.getWidth() /2);
        mainPane.setCenter(board);

        //Handle block on game board grid being clicked

        //Create UI elements for displaying game properties
        infoBox = new VBox();
        scoreLabel = new Label("Score: " + game.getScore());
        levelLabel =new Label("Level: " + game.getLevel());
        multiplierLabel = new Label("Multiplier: " + game.getMultiplier());
        livesLabel = new Label("Lives: " + game.getLives());
        highScoreLabel = new Label();
        //Bind labels to game properties
        scoreLabel.textProperty().bind(game.scoreProperty().asString("Score: %d"));
        levelLabel.textProperty().bind(game.levelProperty().asString("Level: %d"));
        multiplierLabel.textProperty().bind(game.multiplierProperty().asString("Multiplier: %d"));
        livesLabel.textProperty().bind(game.livesProperty().asString("Lives: %d"));
        highScoreLabel.textProperty().bind(highScoreProperty.asString("High Score: %d"));
        //Styling the font
        scoreLabel.getStyleClass().add("score");
        levelLabel.getStyleClass().add("level");
        multiplierLabel.getStyleClass().add("score");
        livesLabel.getStyleClass().add("lives");
        highScoreLabel.getStyleClass().add("heading");

        //Shows the current piece
        VBox pieces = new VBox();
        currentPiece = new PieceBoard(100, 100);
        pieces.setAlignment(Pos.CENTER);
        //Shows the next piece
        nextPiece = new PieceBoard(75,75);
        pieces.getChildren().addAll(currentPiece, nextPiece);
        pieces.setSpacing(40);
        infoBox.getChildren().addAll(scoreLabel, levelLabel, multiplierLabel, livesLabel, highScoreLabel, pieces); //adds the pieces in the info vBox

        //Sets the listeners
        board.setOnBlockClick(this::blockClicked);
        board.setOnRightClick(this:: rotateCurrentPiece);
        currentPiece.setOnMouseClicked(event -> this.rotateCurrentPiece());
        nextPiece.setOnMouseClicked(event -> this.swapPieces());

        //Structures the info
        infoBox.setAlignment(Pos.TOP_RIGHT);
        infoBox.setSpacing(10);
        infoBox.setPadding(new Insets(20, 40, 0, 0)); // Insets: top, right, bottom, left
        mainPane.setRight(infoBox);

        //timer
        timer = new Rectangle();
        timer.setHeight(30.0);
        timer.setWidth(gameWindow.getWidth());
        timer.setFill(Color.RED);
        mainPane.setBottom(timer);

    }

    /**
     * Handle when a block is clicked
     * @param gameBlock the Game Block that was clocked
     */
    protected void blockClicked(GameBlock gameBlock) {
        game.blockClicked(gameBlock);
    }

    /**
     * Swaps the current piece with the following piece
     */
    public void swapPieces(){
        game.swapCurrentPiece();
        currentPiece.showPiece(game.getCurrentPiece());
        nextPiece.showPiece(game.getFollowingPiece());
    }

    /**
     * Rotates the current piece and displays it
      */
    public void rotateCurrentPiece(){
        game.rotateCurrentPiece();
    }

    /**
     * Set up the game object and model
     */
    public void setupGame() {
        logger.info("Starting a new challenge");
        //Start new game
        game = new Game(5, 5);
        //Sets the listeners
        game.setNextPieceListener(this ::nextPiece);
        game.setOnClearedLine(this::fadeLine);
        game.setOnGameLoop(this::gameLoop);
        game.setOnGameOver(() -> {
            game.endLoop();
            Multimedia.stopBackground();
            gameWindow.startScoresScene(game);
        });
    }

    /**
     * Initialise the scene and start the game
     */
    @Override
    public void initialise() {
        logger.info("Initialising Challenge");
        game.start();

        getHighScore("Scores.txt");
        // Bind the scoreLabel to the highScoreProperty
        scoreLabel.textProperty().addListener((observable, oldValue, newValue) -> {
            int currentScore = game.getScore();
            if (currentScore > highScoreProperty.get()) {
                updateHighScore(currentScore);
            }
        });
        //Display the current and following piece when the scene is initialised
        currentPiece.showPiece(game.getCurrentPiece());
        nextPiece.showPiece(game.getFollowingPiece());
        currentPiece.addDot();
        nextPiece.addDot();
        scene.setOnKeyPressed(this:: handleKeyboardInput);

        Multimedia.playBackgroundMusic("game_start.wav");
    }

    /**
     * handles updating the current and following pieces
     * @param current current piece being played
     * @param following following piece to be played
     */
    public void nextPiece(GamePiece current, GamePiece following){
        logger.info("Current piece:" + current.toString());
        currentPiece.showPiece(current);
        nextPiece.showPiece(following);
    }

    /**
     * handles the fading of the cleared lines
     * @param set The coordinates of the block cleared
     */
    public void fadeLine(HashSet<GameBlockCoordinate> set){
        board.fadeOut(set);
    }

    /**
     * handles the keyboard controls
     * @param event the keyboard key that was pressed
     */
    public void handleKeyboardInput(KeyEvent event){
        KeyCode keyCode = event.getCode();
        switch (keyCode){
            case W:
            case UP:
                if (y > 0){
                    y--;
                   board.hover(board.getBlock(x,y));
                }
                break;
            case A:
            case LEFT:
                if (x>0){
                    x--;
                    board.hover(board.getBlock(x,y));
                }
                break;
            case S:
            case DOWN:
                if (y < game.getRows() - 1) {
                    y++;
                    board.hover(board.getBlock(x,y));
                }
                break;
            case D:
            case RIGHT:
                if (x < game.getCols() - 1) {
                    x++;
                    board.hover(board.getBlock(x,y));
                }
                break;
            case E:
            case C:
            case CLOSE_BRACKET:
                rotateCurrentPiece();
                break;
            case Q:
            case Z:
            case OPEN_BRACKET:
                //rotating left
                for (int i =0; i < 3; i ++){
                    rotateCurrentPiece();
                }
                break;
            case X:
            case ENTER:
                blockClicked(board.getBlock(x, y));
                break;
            case R:
            case SPACE:
                swapPieces();
                break;
            case ESCAPE:
                showExitConfirmationDialog();
                break;
        }
    }

    /**
     * Gives an option to the user to continue playing the game if the esc key was pressed accidentally
     */
    protected void showExitConfirmationDialog() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Exit Confirmation");
        alert.setHeaderText("Do you want to exit the game?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                logger.info("User ended the challenge");
                game.endLoop();
                Multimedia.stopBackground();
                gameWindow.startMenu();
            }
        });
    }

    /**
     * The timer animation
     * @param timerDelay Delay that the timer is supposed to last
     */
    public void gameLoop(int timerDelay){
        Timeline timeline = new Timeline(
            new KeyFrame(Duration.ZERO, new KeyValue(timer.fillProperty(), Color.GREENYELLOW)),
            new KeyFrame(Duration.ZERO, new KeyValue(timer.widthProperty(), gameWindow.getWidth())),
            new KeyFrame(new Duration((double) timerDelay * 0.5D), new KeyValue(timer.fillProperty(), Color.ORANGE)),
            new KeyFrame(new Duration((double) timerDelay * 0.75D), new KeyValue(timer.fillProperty(), Color.RED)),
            new KeyFrame(new Duration(timerDelay), new KeyValue(timer.widthProperty(), 0))
        );
        timeline.play();
    }

    /**
     * Responsible for retrieving the high score from the local file and updating the new scores
     * @param filename file with the local scores
     */
    public void getHighScore(String filename) {
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(filename));
            String score = bufferedReader.readLine();
            String[] scoring = score.split(":");
            int currentHighScore = Integer.parseInt(scoring[1]);

          updateHighScore(Math.max(game.getScore(), currentHighScore));
        } catch (IOException e) {
            logger.info("File not found");
        }
    }

    /**
     * Presents the high score during the gameplay
     * @param newHighScore the high score
     */
    public void updateHighScore(int newHighScore){
        highScoreProperty.set(newHighScore);
    }

}
