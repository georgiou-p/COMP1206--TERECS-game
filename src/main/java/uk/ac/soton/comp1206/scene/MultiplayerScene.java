package uk.ac.soton.comp1206.scene;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.component.LeaderBoard;
import uk.ac.soton.comp1206.game.MultiplayerGame;
import uk.ac.soton.comp1206.media.Multimedia;
import uk.ac.soton.comp1206.ui.GameWindow;

/**
 * The multiplayer game scene
 */
public class MultiplayerScene extends ChallengeScene{

  private static final Logger logger = LogManager.getLogger(MultiplayerScene.class);

  private final Timer receivePieces = new Timer();

  private final ObservableList<Integer> pieceList = FXCollections.observableArrayList();
  private final SimpleListProperty<Integer> pieceProperty = new SimpleListProperty<>(pieceList);

  private final HashMap<String, Integer> scorestore = new HashMap<>();

  private final ObservableList<Pair<String, Integer>> scoreList = FXCollections.observableArrayList();
  private final SimpleListProperty<Pair<String, Integer>> scores = new SimpleListProperty<>(scoreList);
  private final List<String> deadPlayers = new ArrayList<>();
  private LeaderBoard leaderBoard;

  private final VBox leaderboardEntries = new VBox();

  private final VBox leaderboardInfo = new VBox();


  /**
   * Create a new Single Player challenge scene
   *
   * @param gameWindow the Game Window
   */
  public MultiplayerScene(GameWindow gameWindow) {
    super(gameWindow);
  }
  @Override
  public void setupGame(){
    logger.info("Starting the new multiplayer scene");
    receivePieces();
    gameWindow.getCommunicator().addListener((multiplayerListener)->Platform.runLater(()->{
      if(multiplayerListener.startsWith("PIECE")){
        listPiece(multiplayerListener);
      }
      if (multiplayerListener.startsWith("SCORES")){
        manageScores(multiplayerListener);
     }
    }));
    super.game = new MultiplayerGame(5,5);
    ((MultiplayerGame)super.game).pieceProperty().bind(pieceProperty);


    //Set the listeners
    game.setNextPieceListener(this ::nextPiece);
    game.setOnClearedLine(this::fadeLine);
    super.game.setOnGameLoop(this::gameLoop);
    game.setOnGameOver(() -> {
      game.endLoop();
      Multimedia.stopBackground();
      scoreList.clear();
      scorestore.forEach((playerName, playerScore) -> scoreList.add(new Pair<>(playerName, playerScore)));
      scores.setAll(scoreList);
      leaderBoard = new LeaderBoard(scores);

      gameWindow.startScoresScene(game, leaderBoard);
    });
    sendScores();
  }

  /**
   * Sends messages to the communicator in order to receive the new pieces to be played
   */
  private void receivePieces(){
    TimerTask task = new TimerTask() {
      public void run() {
        Platform.runLater(() ->{
          gameWindow.getCommunicator().send("SCORES");
          gameWindow.getCommunicator().send("PIECE");
          gameWindow.getCommunicator().send("PIECE");
          gameWindow.getCommunicator().send("PIECE");
          gameWindow.getCommunicator().send("PIECE");
          gameWindow.getCommunicator().send("PIECE");
        });
      }};
    receivePieces.schedule(task, 0);
  }

  /**
   * Places the pieces received into piece list
   * @param message the signal received from the communicator
   */
  private void listPiece(String message){
    message = message.replace("PIECE","");
    message = message.trim();
    pieceList.add(Integer.valueOf(message));

    updatePieceBoards();
  }

  @Override
  protected void blockClicked(GameBlock gameBlock){
    super.game.blockClicked(gameBlock);
    StringBuilder block = new StringBuilder("BOARD ");
    for (int i = 0; i < board.getRowCount(); i++) {
      for (int j = 0; j < board.getColumnCount(); j++) {
        block.append(board.getBlock(i, j).getValue()).append(" ");
      }
    }
    gameWindow.getCommunicator().send(block.toString());
    gameWindow.getCommunicator().send("PIECE");
  }

  @Override
  public void build(){
    super.build();

    //Remove unnecessary info
    infoBox.getChildren().remove(highScoreLabel);
    infoBox.getChildren().remove(multiplierLabel);
    infoBox.getChildren().remove(levelLabel);
    titleLabel.setText("THE MULTIPLAYER");

    // Add leaderboard entries VBox to leaderboardInfo VBox
    leaderboardInfo.getChildren().addAll(new Label("Leaderboard"), leaderboardEntries);
    VBox.setVgrow(leaderboardEntries, Priority.ALWAYS);

    leaderboardInfo.getStyleClass().add("leaderboard-info");

    // Add leaderboardInfo VBox to infoBox VBox
    infoBox.getChildren().add(leaderboardInfo);


  }

  @Override
  public void initialise(){
    logger.info("Initialising challenge");
    super.game.start();

    scene.setOnKeyPressed(this::handleKeyboardInput);

    currentPiece.addDot();
    nextPiece.addDot();

    Multimedia.playBackgroundMusic("game_start.wav");
  }

  /**
   * Updates the boards displaying the pieces in the multiplayer game
   */
  private void updatePieceBoards(){
    // Show the current piece
    if (game.getCurrentPiece() != null) {
      currentPiece.showPiece(game.getCurrentPiece());
    }
    // Show the next piece
    if (game.getFollowingPiece() != null) {
      nextPiece.showPiece(game.getFollowingPiece());
    }
  }

  /**
   * Sends the scores to the server
   */
  private void sendScores(){
    super.game.scoreProperty().addListener(((observableValue, number, t1) -> gameWindow.getCommunicator().send("SCORE " + t1)));
    super.game.livesProperty().addListener(((observableValue, number, t1) -> {
      if (t1.intValue() >= 0){
        gameWindow.getCommunicator().send("LIVES " + t1);
        gameWindow.getCommunicator().send("PIECE");
      }
    }));
  }

  @Override
  protected void showExitConfirmationDialog(){
    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
    alert.setTitle("Exit Confirmation");
    alert.setHeaderText("Do you want to exit the multiplayer?");
    alert.setContentText("By exiting the multiplayer you will be guided in the menu scene and thrown out of the channel");

    alert.showAndWait().ifPresent(response -> {
      if (response == ButtonType.OK) {
        logger.info("User left the multiplayer challenge");
        game.endLoop();
        Multimedia.stopBackground();
        gameWindow.startMenu();
        gameWindow.getCommunicator().send("PART");
        pieceList.clear();
        pieceProperty.clear();
      }
    });
  }

  /**
   * Responsible for managing the scores once they are received from the server
   * @param message the message from the server
   */
  private void manageScores(String message){
    message = message.replace("SCORES", "");
    String[] player = message.split("\n");
    for (String user: player){
      String[] score = user.split(":", 3);
      if (score[2].equals("DEAD")) {
        deadPlayers.add(score[0]);
      } else {
        if (Integer.parseInt(score[2]) < 0) {
          gameWindow.getCommunicator().send("DIE" + score[1]);
        }
        if (scorestore.containsKey(score[0])){
          scorestore.replace(score[0], Integer.parseInt(score[1]));
        } else {
          scorestore.put(score[0], Integer.parseInt(score[1]));
        }
      }
      updateLeaderboard();
    }
  }

  /**
   * Updates the in-game leaderboard to show the live status of players
   */
  private void updateLeaderboard(){
    leaderboardEntries.getChildren().clear();
    // Iterate over scorestore HashMap to add leaderboard entries
    scorestore.entrySet().stream()
        .sorted((entry1, entry2) -> Integer.compare(entry2.getValue(), entry1.getValue())) // Sort by score in descending order
        .forEach(entry -> {
          String playerName = entry.getKey();
          int playerScore = entry.getValue();


          String labelText = playerName + ": " + playerScore;
          Label entryLabel = new Label(labelText);
          if (deadPlayers.contains(playerName)) {
            entryLabel.getStyleClass().add("deadscore");
            logger.info("Crossing out the dead player");
          }
          leaderboardEntries.getChildren().add(entryLabel);

        });
  }



}
