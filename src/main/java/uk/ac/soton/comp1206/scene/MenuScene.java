package uk.ac.soton.comp1206.scene;

import java.util.Objects;
import javafx.animation.ScaleTransition;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.media.Multimedia;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

/**
 * The main menu of the game. Provides a gateway to the rest of the game.
 */
public class MenuScene extends BaseScene {

    private static final Logger logger = LogManager.getLogger(MenuScene.class);

    /**
     * Create a new menu scene
     * @param gameWindow the Game Window this will be displayed in
     */
    public MenuScene(GameWindow gameWindow) {
        super(gameWindow);
        SettingsScene.loadSettings();
        logger.info("Creating Menu Scene");
    }

    /**
     * Build the menu layout
     */
    @Override
    public void build() {
        logger.info("Building " + this.getClass().getName());

        root = new GamePane(gameWindow.getWidth(),gameWindow.getHeight());

        var menuPane = new VBox();
        menuPane.setMaxWidth(gameWindow.getWidth());
        menuPane.setMaxHeight(gameWindow.getHeight());
        menuPane.getStyleClass().add("menu-background");
        root.getChildren().add(menuPane);

        var mainPane = new BorderPane();
        menuPane.getChildren().add(mainPane);

        //Add title image
        ImageView titleImageView = createTitleImageView();
        titleImageView.setTranslateY(50); //Adjust the vertical position of the image
        menuPane.getChildren().add(titleImageView);
        menuPane.setAlignment(Pos.TOP_CENTER);

        //Buttons and style
        var singlePlayer = new Button("Single Player");
        var instructions = new Button("Instructions");
        var multiplayer = new Button("Multiplayer");
        var settings = new Button("Settings");
        singlePlayer.getStyleClass().add("menuItem");
        instructions.getStyleClass().add("menuItem");
        multiplayer.getStyleClass().add("menuItem");
        settings.getStyleClass().add("menuItem");

        //VBox to store the buttons
        var vbox = new VBox(singlePlayer, instructions, multiplayer, settings);
        vbox.setAlignment(Pos.CENTER);
        vbox.setSpacing(20);
        vbox.setTranslateY(150);

        menuPane.getChildren().add(vbox);


        //Bind the button action to the startGame method in the menu
        singlePlayer.setOnAction(this::startGame);
        instructions.setOnAction(this::showInstructions);
        multiplayer.setOnAction(this::showMultiplayer);
        settings.setOnAction(this::showSettings);
    }

    /**
     * Initialise the menu
     */
    @Override
    public void initialise() {
        Multimedia.playBackgroundMusic("menu.mp3");
    }

    /**
     * Handle when the Start Game button is pressed
     * @param event event
     */
    private void startGame(ActionEvent event) {
        Multimedia.stopBackground();
        Multimedia.playSound("transition.wav");
        gameWindow.startChallenge();
    }

    /**
     * Displays the instructions scene
     * @param event pressing th button
     */
    private void showInstructions(ActionEvent event){
        gameWindow.startInstructions();
        Multimedia.stopBackground();
        Multimedia.playSound("transition.wav");
    }


    /**
     * Displays th multiplayer scene
     * @param event pressing the button
     */
    private void showMultiplayer(ActionEvent event){
        gameWindow.startLobbyScene();
        Multimedia.stopBackground();
        Multimedia.playSound("transition.wav");
    }

    /**
     * Displays the Settings scene
     * @param event pressing the button
     */
    private void showSettings(ActionEvent event) {
        Multimedia.stopBackground();
        Multimedia.playSound("transition.wav");
        gameWindow.startSettingsScene();
    }

    /**
     * creates the image view
     * @return returns the image
     */
    private ImageView createTitleImageView() {
        Image titleImage = new Image(
            Objects.requireNonNull(getClass().getResourceAsStream("/images/TetrECS.png")));
        ImageView titleImageView = new ImageView(titleImage);
        titleImageView.setFitWidth(700);
        titleImageView.setPreserveRatio(true); // Preserve aspect ratio

        // Create a ScaleTransition
        ScaleTransition scaleTransition = new ScaleTransition(Duration.seconds(2), titleImageView);
        scaleTransition.setFromX(1.0); // Start scale factor X
        scaleTransition.setFromY(1.0); // Start scale factor Y
        scaleTransition.setToX(1.2); // End scale factor X
        scaleTransition.setToY(1.2); // End scale factor Y
        scaleTransition.setAutoReverse(true); // Reverse the transition
        scaleTransition.setCycleCount(ScaleTransition.INDEFINITE); // Repeat indefinitely
        scaleTransition.play(); // Start the animation

        return titleImageView;
    }
}
