package uk.ac.soton.comp1206.scene;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.media.Multimedia;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

/**
 * Responsible for the music and sound effect volumes
 */
public class SettingsScene extends BaseScene{

  private static final Logger logger = LogManager.getLogger(MenuScene.class);

  /**
   * Sliders for the user to control the volume
   */
  private Slider musicSlider, effectsSlider;

  /**
   * The default volumes
   */
  public static double musicVolume = 50, effectVolume = 50;

  /**
   * Create a new scene, passing in the GameWindow the scene will be displayed in
   *
   * @param gameWindow the game window
   */
  public SettingsScene(GameWindow gameWindow) {
    super(gameWindow);
    logger.info("Creating Settings Scene");
  }

  @Override
  public void initialise() {
    getScene().setOnKeyPressed(this::handleKeyPress);
    Multimedia.playBackgroundMusic("menu.mp3");
  }

  /**
   * Handles when the esc key is pressed
   * @param event the esc key
   */
  private void handleKeyPress(KeyEvent event) {
    if (event.getCode() == KeyCode.ESCAPE) {
      //Save the settings
      writeSettings();
      // Stop any playing background music
      Multimedia.stopBackground();
      gameWindow.cleanup();
      // Go back to the menu scene
      gameWindow.startMenu();

    }
  }
  @Override
  public void build(){
    logger.info("Building " + this.getClass().getName());

    root = new GamePane(gameWindow.getWidth(),gameWindow.getHeight());

    var settingsPane = new StackPane();
    settingsPane.setMaxWidth(gameWindow.getWidth());
    settingsPane.setMaxHeight(gameWindow.getHeight());
    settingsPane.getStyleClass().add("menu-background");
    root.getChildren().add(settingsPane);
    var mainPane = new BorderPane();
    settingsPane.getChildren().add(mainPane);

    // Title
     var titleLabel = new Label("THE SETTINGS");
    titleLabel.getStyleClass().add("menuItem");
    titleLabel.setPadding(new Insets(10, 0, 0, 0));
    mainPane.setTop(titleLabel);
    BorderPane.setAlignment(titleLabel, Pos.CENTER);


    /* Center */
    var centerBox = new VBox(10);
    centerBox.setAlignment(Pos.CENTER);
    mainPane.setCenter(centerBox);

    var volumeBox = new HBox(100);
    volumeBox.setAlignment(Pos.CENTER);
    var volumeControl = new Text("Volume Control");
    volumeControl.getStyleClass().add("heading");

    // Music
    var musicBox = new VBox(10);
    musicBox.setAlignment(Pos.CENTER);
    var musicText = new Text("Music");
    musicText.getStyleClass().add("heading");
    musicSlider = new Slider(0, 100, musicVolume);
    musicSlider.setPrefSize(300, 20);
    musicSlider.setShowTickMarks(true);
    musicSlider.setMajorTickUnit(25);
    musicSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
      musicVolume = (int) musicSlider.getValue();
      Multimedia.backgroundPlayer.setVolume(musicVolume / 100);
    });
    musicBox.getChildren().addAll(musicText, musicSlider);

    // Audio
    var audioBox = new VBox(10);
    audioBox.setAlignment(Pos.CENTER);
    var audioText = new Text("Sound effects");
    audioText.getStyleClass().add("heading");
    effectsSlider = new Slider(0, 100, effectVolume);
    effectsSlider.setPrefSize(300, 20);
    effectsSlider.setShowTickMarks(true);
    effectsSlider.setMajorTickUnit(25);
    effectsSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
      effectVolume = (int) effectsSlider.getValue();
      Multimedia.soundPlayer.setVolume(effectVolume / 100);
    });
    audioBox.getChildren().addAll(audioText, effectsSlider);

    volumeBox.getChildren().addAll(musicBox, audioBox);

    centerBox.getChildren().addAll(volumeControl, volumeBox);

  }

  /**
   * Responsible for writing the settings in a file
   */
  public static void writeSettings() {
    try {
      if (new File("Settings.txt").createNewFile()) {
        logger.info("File created");
      }
    } catch (IOException e) {
      e.printStackTrace();
          }
    try {
      BufferedWriter settingsWriter = new BufferedWriter(new FileWriter("Settings.txt"));
      settingsWriter.write(musicVolume + " ");
      settingsWriter.write(effectVolume + " ");
      settingsWriter.close();
      logger.info("Settings written");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Responsible for loading the recommended user settings
   */
  public static void loadSettings() {
    logger.info("Loading settings");
    if (new File("Settings.txt").exists()) {
      try {
        FileInputStream reader = new FileInputStream("Settings.txt");
        BufferedReader br = new BufferedReader(new InputStreamReader(reader));
        try {
          String line = br.readLine();
          String[] parts = line.split(" ");
          musicVolume = Double.parseDouble(parts[0]);
          effectVolume = Double.parseDouble(parts[1]);
          br.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      } catch (FileNotFoundException e) {
        e.printStackTrace();
      }
    } else {
      writeSettings();
    }
  }


}
