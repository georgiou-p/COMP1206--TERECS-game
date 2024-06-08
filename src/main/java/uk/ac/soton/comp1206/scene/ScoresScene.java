package uk.ac.soton.comp1206.scene;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javafx.application.Platform;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.LeaderBoard;
import uk.ac.soton.comp1206.component.ScoresList;
import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.media.Multimedia;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;


/**
 * Holds the methods for the high scores when the game ends
 */
public class ScoresScene extends BaseScene{
  protected static final Logger logger = LogManager.getLogger(ScoresScene.class);

  /**
   * List property for the local scores
   */
  protected final ListProperty<Pair<String, Integer>> localScores = new SimpleListProperty<>(
      FXCollections.observableArrayList());
  /**
   * Observable list containing the online scores
   */
  protected final ObservableList<Pair<String, Integer>> remoteList= FXCollections.observableArrayList();
  /**
   * List property containing the online scores
   */
  protected final ListProperty<Pair<String, Integer>> remoteScores = new SimpleListProperty<>(FXCollections.observableArrayList());


  /**
   * Scorelist for presenting the scores
   */
  protected ScoresList scoresList;

  /**
   * Where the scores will be displayed in the UI
   */
  protected BorderPane mainPane;

  /**private LeaderBoard multiplayerList;
   * Leaderboard containing the multiplayer scores
   */
  private  LeaderBoard multiplayerList;

  /**
   * To check whether the player is playing a multiplayer game or a single game
   */
  private boolean offline =true;

  /**
   * Create a new scene, passing in the GameWindow the scene will be displayed in
   *
   * @param gameWindow the game window
   */
  public ScoresScene(GameWindow gameWindow, Game game) {
    super(gameWindow);
    this.game = game;
    logger.info("Creating new scores scene");
  }

  /**
   * Creates a new scene for the multiplayer scores
   * @param gameWindow the game window
   * @param game the game played
   * @param leaderBoard the multiplayer leaderboard
   */
  public ScoresScene(GameWindow gameWindow, Game game, LeaderBoard leaderBoard){
    super(gameWindow);
    this.game = game;
    this.multiplayerList = leaderBoard;
    this.offline =false;
    logger.info("Creating score scene for the multiplayer game");

  }

  @Override
  public void initialise() {
    logger.info("Initialising scores scene");
    Multimedia.playBackgroundMusic("menu.mp3");
    getScene().setOnKeyPressed(this::handleKeyPress);
    if (offline) {
      loadScores();
      revealHighScores();
    } else{
      loadMultiplayerScores();
      revealHighScores();
    }
    loadOnlineScores();
  }

  @Override
  public void build() {
    logger.info("Building " + this.getClass().getName());
    root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());

    var scoresPane = new StackPane();
    scoresPane.setMaxWidth(gameWindow.getWidth());
    scoresPane.setMaxHeight(gameWindow.getHeight());
    scoresPane.getStyleClass().add("menu-background");
    root.getChildren().add(scoresPane);

    mainPane = new BorderPane();
    scoresPane.getChildren().add(mainPane);

    //Top
    var top = new HBox();
    top.setAlignment(Pos.CENTER);
    BorderPane.setMargin(top, new Insets(10, 0, 0, 0));
    mainPane.setTop(top);

    Text gameOver = new Text("Game Over");
    gameOver.getStyleClass().add("bigtitle");
    top.getChildren().add(gameOver);

    //Load local scores
    if (offline) {
      setScoresList();
    }else {
      loadMultiplayerScores();
    }

    //Load online scores
    ScoresList onlineScoresList = new ScoresList();
    onlineScoresList.setTitle("Top 10 Online Scores");
    onlineScoresList.scoreProperty().bind(remoteScores);

    // Add onlineScoresList to the layout
    mainPane.setRight(onlineScoresList);
    BorderPane.setAlignment(onlineScoresList, Pos.CENTER_RIGHT);
    BorderPane.setMargin(onlineScoresList, new Insets(0, 20, 10, 20)); // Adjust margins as needed

  }

  /**
   * Reads the scores in the file and adds the new ones
   */
  public void loadScores(){
    ArrayList<Pair<String, Integer>> score = new ArrayList<>();
    File filename = new File("Scores.txt");

    if (!filename.exists()){
      ArrayList<Pair<String, Integer>> scores = new ArrayList<>();
      scores.add(new Pair<>("Player1", 450));
      scores.add(new Pair<>("Player2", 200));
      scores.add(new Pair<>("Player3", 1500));
      scores.add(new Pair<>("Player5", 1000));
      scores.add(new Pair<>("Player4", 500));
      scores.add(new Pair<>("Player6", 680));
      scores.add(new Pair<>("Player7", 0));
      scores.add(new Pair<>("Player8", 450));
      scores.add(new Pair<>("Player9", 200));
      scores.add(new Pair<>("Player10", 1500));
      writeScores(scores);
      checkHighScore();
    }

    try{ BufferedReader bufferedReader = new BufferedReader(new FileReader(filename));
        String line;
        while ((line = bufferedReader.readLine()) != null){
          String[] parts = line.split(":");
          if (parts.length ==2) {
            score.add(new Pair<>(parts[0], Integer.parseInt(parts[1])));
          }
        }
        //Sort the scores
        score.sort((s1, s2) -> Integer.compare(s2.getValue(), s1.getValue()));
        localScores.addAll(score);
    } catch (IOException e) {
      e.printStackTrace();
    }
    //Check for the high score
    checkHighScore();
  }

  /**
   * Writes the scores in the file
   * @param scores the list of scores
   */
  public void writeScores(List<Pair<String, Integer>> scores)  {
    File file = new File("Scores.txt");
    try(BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file))){
      for (Pair<String, Integer> score : scores){
        bufferedWriter.write(score.getKey() + ":" + score.getValue());
        bufferedWriter.newLine();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Checks for high scores and sets a name to the player
   */
  public void checkHighScore() {
    int gameScore = game.getScore();

    //Get the player's name
    TextInputDialog dialog = new TextInputDialog("RandomGuest");
    dialog.setTitle("Adding you in the scoreboard file");
    dialog.setContentText("Please enter your name:");
    Optional<String> result = dialog.showAndWait();

    if (result.isPresent()) {
      String name = result.get();

      // Add the new score to the local scores list
      localScores.add(0, new Pair<>(name, gameScore));

      // Sort the scores
      localScores.sort((s1, s2) -> Integer.compare(s2.getValue(), s1.getValue()));
      writeScores(new ArrayList<>(localScores));
      writeOnlineScore(name, gameScore);
      scoresList.updateScoresList();
    }

  }

  /**
   * Presents the top 10 scores
   */
  public void revealHighScores () {
      scoresList.updateScoresList();
    }

  /**
   * Sends and receives high scores from the server
   */
  public void loadOnlineScores(){
      gameWindow.getCommunicator().send("HISCORES");
      gameWindow.getCommunicator().addListener((scorelistener)-> Platform.runLater(()->this.receiveCommunication(scorelistener)));
    }

  /**
   * Handles the received messages from the server
   * @param communicate the message received
   */
  public void receiveCommunication(String communicate) {
    if (communicate.startsWith("HISCORES")) {
      communicate = communicate.replace("HISCORES", "");
      String[] highScores = communicate.split("\\n"); //splits the communicate array into substrings
      for (int i = 0; i < 10; i++) {
        String[] eachScore = highScores[i].split(":");
        remoteList.add(new Pair<>(eachScore[0], Integer.parseInt(eachScore[1])));
      }
      remoteScores.setAll(remoteList);
    } else if (communicate.startsWith("NEWSCORE")) {
      String[] parts = communicate.split(" ");
      if (parts.length == 2) {
        String[] scoreParts = parts[1].split(":");
        if (scoreParts.length == 2) {
          // Add the new high score to the remote scores list
          remoteScores.add(new Pair<>(scoreParts[0], Integer.parseInt(scoreParts[1])));
        }
      }
    }
  }

  /**
   * Writes the score in the server
   * @param name the player's name
   * @param score the player's score
   */
  public void writeOnlineScore(String name, int score){
    gameWindow.getCommunicator().send("HISCORE " + name + ":" + score);
  }

  /**
   * Handles when the esc key is pressed
   * @param event the esc key
   */
  protected void handleKeyPress(KeyEvent event) {
    if (event.getCode() == KeyCode.ESCAPE) {
      // Stop any playing background music
      Multimedia.stopBackground();
      gameWindow.cleanup();
      // Go back to the menu scene
      gameWindow.startMenu();

    }
  }

  /**
   * Presents the local scores
   */
  protected void setScoresList(){
    scoresList = new ScoresList();
    scoresList.setTitle("Top 10 Local Scores");
    scoresList.scoreProperty().bind(localScores);

    //Load Scores
    mainPane.setLeft(scoresList);
    BorderPane.setAlignment(scoresList,Pos.CENTER_LEFT);
    BorderPane.setMargin(scoresList, new Insets(0, 20, 10, 20)); // Add space on left and right

  }

  /**
   * Presents the multiplayer scores
   */
  public void loadMultiplayerScores(){
    Platform.runLater(()-> {
      scoresList = new ScoresList();
      scoresList.setTitle("Top Multiplayer Scores");
      scoresList.scoreProperty().bind(multiplayerList.getPlayerScores());

      //Load Scores
      mainPane.setLeft(scoresList);
      BorderPane.setAlignment(scoresList,Pos.CENTER_LEFT);
      BorderPane.setMargin(scoresList, new Insets(0, 20, 10, 20)); // Add space on left and right

      logger.info("Added lobby scores");

    });
  }



}
