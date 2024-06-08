package uk.ac.soton.comp1206.scene;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.media.Multimedia;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

/**
 * Where the players will connect to channels and play the multiplayer game
 */
public class LobbyScene extends BaseScene {

  private final Logger logger = LogManager.getLogger(LobbyScene.class);


  /**
   * Available channels array
   */
  private String[] channels;

  /**
   * VBox to place the available channels
   */
  private VBox channelListVBox;

  /**
   * Where the name of the new channel will be written
   */
  private TextField hostGameName;

  /**
   * Area to see chat messages
   */
  private TextArea chatTextArea;
  /**
   * Area to write chat messages
    */
  private TextField chatTextField;

  /**
   * Button to start the game
   */
  private Button startGameButton;

  /**
   * Button to leave the channel
    */
  private Button leaveChannelButton;

  /**
   * Pane for the messages able to scroll
   */
  private ScrollPane chatScrollPane;

  /**
   * Flag for when the game is started
   */
  private boolean gameStarted = false;

  /**
   * The users currently in a channel
   */
  private String[] users;

  /**
   * Create a new scene, passing in the GameWindow the scene will be displayed in
   *
   * @param gameWindow the game window
   */
  public LobbyScene(GameWindow gameWindow) {
    super(gameWindow);
  }

  @Override
  public void initialise() {
    getScene().setOnKeyPressed(this::handleKeyPress);
    Multimedia.playBackgroundMusic("menu.mp3");

    startChannelRequestTimer();
    gameWindow.getCommunicator().addListener(this::receiveCommunication);


  }

  @Override
  public void build() {
    logger.info("Building " + this.getClass().getName());

    root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());

    var mainPane = new HBox();
    mainPane.setMaxWidth(gameWindow.getWidth());
    mainPane.setMaxHeight(gameWindow.getHeight());
    mainPane.getStyleClass().add("menu-background");
    mainPane.setPadding(new Insets(20));

    root.getChildren().add(mainPane);

    // Left side containing existing channels and host new game section
    var leftPane = new VBox();
    leftPane.setAlignment(Pos.CENTER_LEFT);
    leftPane.setSpacing(20);

    Label channelLabel = new Label("Existing channels");
    channelLabel.getStyleClass().add("title");
    channelListVBox = new VBox();
    channelListVBox.setAlignment(Pos.CENTER_LEFT);
    channelListVBox.setSpacing(10);
    leftPane.getChildren().addAll(channelLabel, channelListVBox);

    //Hosting a new channel
    var hostNewGame = new VBox();
    hostGameName = new TextField();
    hostGameName.getStyleClass().add("TextField");

    Label createGameLabel = new Label("Host game");
    createGameLabel.getStyleClass().add("title");
    hostNewGame.getChildren().add(createGameLabel);

    var submitHosting = new Button();
    submitHosting.setText("Submit");
    submitHosting.getStyleClass().add("channelList");

    hostNewGame.getChildren().add(hostGameName);
    hostNewGame.getChildren().add(submitHosting);

    hostGameName.setMaxWidth(140);

    hostGameName.setOnKeyPressed(this::submitChannelOnEnter);
    submitHosting.setOnAction(this::submitChannelOnMouse);

    hostNewGame.setAlignment(Pos.CENTER_LEFT);
    hostNewGame.setSpacing(10);
    hostNewGame.setMinWidth(300);
    leftPane.getChildren().addAll(hostNewGame);
    mainPane.getChildren().add(leftPane);

    // Right side containing chat area and buttons
    var rightPane = new VBox();
    rightPane.setAlignment(Pos.TOP_RIGHT);
    rightPane.setSpacing(10);
    rightPane.setPadding(new Insets(20, 20, 0, 100));

    // Add chat box
    chatTextArea = new TextArea();
    chatTextArea.setEditable(false);
    chatTextArea.setWrapText(true);
    chatTextArea.setPrefWidth(300);
    chatTextArea.setPrefHeight(400);
    chatTextArea.getStyleClass().add("text-area");
    chatTextField = new TextField();
    chatTextField.setPromptText("Type '/nick \"nickname\"' to change your nickname");
    chatTextField.setOnKeyPressed(this::handleChatKeyPress);

    chatScrollPane = new ScrollPane(chatTextArea);
    chatScrollPane.setFitToWidth(true);
    chatScrollPane.setFitToHeight(true);
    chatScrollPane.setVisible(false);
    chatScrollPane.getStyleClass().add("scroller");
    rightPane.getStyleClass().add("scroller");

    rightPane.getChildren().addAll(chatScrollPane, chatTextField);

    // Add start game button
    startGameButton = new Button("Start Game");
    startGameButton.getStyleClass().add("channelList");
    startGameButton.setOnAction(this::startGame);
    rightPane.getChildren().add(startGameButton);

    // Add leave channel button
    leaveChannelButton = new Button("Leave");
    leaveChannelButton.getStyleClass().add("channelList");
    leaveChannelButton.setOnAction(this::leaveChannel);
    rightPane.getChildren().add(leaveChannelButton);

    chatTextArea.setVisible(false);
    chatTextField.setVisible(false);
    startGameButton.setVisible(false);
    leaveChannelButton.setVisible(false);

    // Add rightPane to the mainPane
    mainPane.getChildren().add(rightPane);
  }

  /**
   * When the user press ESC the menu scene appears
   * @param event escape button
   */
  private void handleKeyPress(KeyEvent event) {
    if (event.getCode() == KeyCode.ESCAPE) {
      // Stop any playing background music
      Multimedia.stopBackground();
      gameWindow.cleanup();
      //leave from the channel
      gameWindow.getCommunicator().send("PART");
      // Go back to the menu scene
      gameWindow.startMenu();

    }
  }

  /**
   * What the user presses enter the message will be sent
   * @param event enter
   */
  private void handleChatKeyPress(KeyEvent event) {
    if (event.getCode() == KeyCode.ENTER) {
      sendMessage();
    }
  }

  /**
   * Message is sent
   */
  private void sendMessage() {
    String message = chatTextField.getText().trim();
    if (!message.isEmpty()) {
      if (message.startsWith("/nick")) {
        gameWindow.getCommunicator().send("NICK "+ message.substring(6));
        gameWindow.getCommunicator().send("USERS");
      }

      gameWindow.getCommunicator().send("MSG " + message);
      chatTextField.clear();
    }
  }

  /**
   * Start the game
   * @param event pressing the start button
   */
  private void startGame(ActionEvent event) {
    gameWindow.getCommunicator().send("START");
    gameWindow.startMultiplayerScene();
    Multimedia.stopBackground();
    gameStarted = true;
  }

  /**
   * The user leaves the channel
   * @param event pressing th leave button
   */
  private void leaveChannel(ActionEvent event) {
    chatTextArea.clear();
    gameWindow.getCommunicator().send("PART");
  }

  /**
   * Requests new channels from the server every 5 seconds
   */
  public void startChannelRequestTimer(){
    ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    // Schedule a task to request channels every 5 seconds
    scheduler.scheduleAtFixedRate(() -> {
      if (!gameStarted) {
        logger.info("Requesting the current channels");
        // Send LIST command to the server to request current channels
        gameWindow.getCommunicator().send("LIST");
      }
    }, 0, 5, TimeUnit.SECONDS);
  }

  /**
   * Handles the received messages from the server
   * @param message message from the server
   */
  public void receiveCommunication(String message){
    Platform.runLater(() -> {
      if (message.contains("CHANNELS")){
        channels = message.substring(9).split("\n");
        presentChannels();
      } else if (message.contains("ERROR")) {
        String errorMessage = message.substring(6);
        showError(errorMessage);
      }else if (message.contains("MSG")){
        String[] parts = message.split(" ", 2);
        if (parts.length == 2) {
          String chatMessage = parts[1];
          Platform.runLater(() -> chatTextArea.appendText(chatMessage + "\n"));
          Multimedia.playSound("message.wav");
        }
      } else if (message.contains("HOST")) {
        startGameButton.setVisible(true);
      } else if (message.contains("PARTED")) {
        setConnectedToChannel(false);
      } else if (message.contains("START")) {
        gameWindow.startMultiplayerScene();
      } else if (message.contains("NICK")) {
        logger.info("Nickname is set to: " + message.split(" ")[1]);
      }else if (message.contains("JOIN")){
        setConnectedToChannel(true);
      } else if (message.contains("USERS")) {
        checkPlayers(message);
      }

    });
  }

  /**
   * Updates the chat with new or departing players
   */
  public void checkPlayers(String message){
    String[] newUsers = message.substring(6).split("\n");
    // Check if any users have joined the channel
    if (users != null) {
      for (String newUser : newUsers) {
        boolean userJoined = true;
        for (String existingUser : users) {
          if (newUser.equals(existingUser)) {
            userJoined = false;
            break;
          }
        }
        // If the user is new, append a message indicating their arrival
        if (userJoined) {
          chatTextArea.appendText( newUser + " joined\n");
        }
      }
      // Check if any users have left the channel
      for (String existingUser : users) {
        boolean userLeft = true;
        for (String newUser : newUsers) {
          if (existingUser.equals(newUser)) {
            userLeft = false;
            break;
          }
        }
        // If the user has left, append a message indicating their departure
        if (userLeft) {
          chatTextArea.appendText(existingUser + " left\n");
        }
      }
    }
    // Update the list of users in the channel
    users = newUsers;
  }

  /**
   * Sets the visibility of the chat by determining if the user is connected or not
    * @param isConnected if the user is in a channel
   */
  public void setConnectedToChannel(boolean isConnected){
    chatTextArea.setVisible(isConnected);
    chatTextField.setVisible(isConnected);
    leaveChannelButton.setVisible(isConnected);
    chatScrollPane.setVisible(isConnected);
    Multimedia.playSound("pling.wav");
    if (!isConnected) {
      chatTextArea.clear();
      startGameButton.setVisible(false);
    }
  }

  /**
   * Presenting channels and allowing user to join them
   */
  public void presentChannels(){
    logger.info("Presenting channels in lobby");

    Platform.runLater(() -> {
      channelListVBox.getChildren().clear(); // Clear existing channels

      if (channels != null) {
        for (String channel : channels) {
          var channelBox = new HBox();
          channelBox.setAlignment(Pos.CENTER_LEFT);
          channelBox.setSpacing(10);

          var channelLabel = new Label();
          channelLabel.setText(channel);
          channelLabel.getStyleClass().add("title");

          // Add join button only when there are channels available
          var joinButton = new Button();
          joinButton.setText("Join");
          joinButton.getStyleClass().add("channelList");
          joinButton.setOnAction((e) -> gameWindow.getCommunicator().send("JOIN " + channel));
          channelBox.getChildren().addAll(channelLabel, joinButton);

          channelListVBox.getChildren().add(channelBox);
        }
      }
    });

  }

  /**
   * Allows the user to create a channel
    */
  private void createChannel(){
    if (hostGameName.getText() != null){
      gameWindow.getCommunicator().send("CREATE " + hostGameName.getText());
    }
  }

  /**
   * The user can crete the channel by pressing the button
   * @param event pressing the button
   */
  private void submitChannelOnMouse(ActionEvent event){
    createChannel();
  }

  /**
   * The user can create a channel by pressing  enter
   * @param event presses enter
   */
  private void submitChannelOnEnter(KeyEvent event) {
    if (event.getCode().equals(KeyCode.ENTER)) {
      createChannel();
    }
  }

  /**
   * Display the error message to the user
   * @param errorMessage error message received
   */
  private void showError(String errorMessage) {
    Alert alert = new Alert(Alert.AlertType.ERROR);
    alert.setTitle("Error");
    alert.setHeaderText("Error occurred");
    alert.setContentText(errorMessage);
    alert.showAndWait();
  }


}
