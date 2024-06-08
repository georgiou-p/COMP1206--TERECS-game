package uk.ac.soton.comp1206.component;

import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javafx.animation.AnimationTimer;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The Visual User Interface component representing a single block in the grid.
 *
 * Extends Canvas and is responsible for drawing itself.
 *
 * Displays an empty square (when the value is 0) or a coloured square depending on value.
 *
 * The GameBlock value should be bound to a corresponding block in the Grid model.
 */
public class GameBlock extends Canvas {

    private static final Logger logger = LogManager.getLogger(GameBlock.class);

    /**
     * The set of colours for different pieces
     */
    public static final Color[] COLOURS = {
            Color.TRANSPARENT,
            Color.DEEPPINK,
            Color.RED,
            Color.ORANGE,
            Color.YELLOW,
            Color.YELLOWGREEN,
            Color.LIME,
            Color.GREEN,
            Color.DARKGREEN,
            Color.DARKTURQUOISE,
            Color.DEEPSKYBLUE,
            Color.AQUA,
            Color.AQUAMARINE,
            Color.BLUE,
            Color.MEDIUMPURPLE,
            Color.PURPLE
    };

    /**
     * the game-board to play
     */
    private final GameBoard gameBoard;

    /**
     * blocks to represent the width
     */
    private final double width;
    /**
     * blocks to represent the height
     */
    private final double height;

    /**
     * The column this block exists as in the grid
     */
    private final int x;

    /**
     * The row this block exists as in the grid
     */
    private final int y;

    /**
     * The value of this block (0 = empty, otherwise specifies the colour to render as)
     */
    private final IntegerProperty value = new SimpleIntegerProperty(0);
    /**
     * Track whether a block is being hovered
      */
    private Boolean hover = false;

    /**
     * the hovering effect to be covered with an image
     */
    private Image spaceshipImage;

    /**
     * Executor service for asynchronous image loading
     */
    private static final ExecutorService executorService = Executors.newCachedThreadPool();


    /**
     * Create a new single Game Block
     * @param gameBoard the board this block belongs to
     * @param x the column the block exists in
     * @param y the row the block exists in
     * @param width the width of the canvas to render
     * @param height the height of the canvas to render
     */
    public GameBlock(GameBoard gameBoard, int x, int y, double width, double height) {
        this.gameBoard = gameBoard;
        this.width = width;
        this.height = height;
        this.x = x;
        this.y = y;

        //A canvas needs a fixed width and height
        setWidth(width);
        setHeight(height);

        // Load the spaceship image asynchronously
        loadSpaceshipImageAsync();

        //Do an initial paint
        paint();

        //When the value property is updated, call the internal updateValue method
        value.addListener(this::updateValue);
    }

    /**
     * loads the image of the spaceship when a block is hovered
     */
    private void loadSpaceshipImageAsync() {
        executorService.submit(() -> {
            spaceshipImage = new Image(getClass().getResourceAsStream("/images/rocket.png"));
            // Call paint method after the image is loaded
            paint();
        });
    }

    /**
     * When the value of this block is updated,
     * @param observable what was updated
     * @param oldValue the old value
     * @param newValue the new value
     */
    private void updateValue(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
        paint();
    }

    /**
     * Handle painting of the block canvas
     */
    public void paint() {
        //If the block is empty, paint as empty
        if(value.get() == 0) {
            paintEmpty();
        } else {
            //If the block is not empty, paint with the colour represented by the value
            paintColor(COLOURS[value.get()]);
        }
        //if a block is being hovered
        if (this.hover){
            paintHover();
        }
    }

    /**
     * Paint this canvas empty
     */
    private void paintEmpty() {
        var gc = getGraphicsContext2D();

        //Clear
        gc.clearRect(0,0,width,height);

        //Fill
        Color transparentCream = new Color(0.5,0.78,1,0.2);
        gc.setFill(transparentCream);
        gc.fillRect(0,0, width, height);

        //Border
        gc.setStroke(Color.BLACK);
        gc.strokeRect(0,0,width,height);
    }

    /**
     * Draws a hover effect on a block
     */
    public void paintHover(){
        GraphicsContext gc = getGraphicsContext2D();
        if (spaceshipImage != null) {
            // Fill the block with the spaceship image
            gc.setFill(new ImagePattern(spaceshipImage));
            gc.fillRect(0, 0, width, height);
        }
    }

    /**
     * Paint this canvas with the given colour
     * @param colour the colour to paint
     */
    private void paintColor(Paint colour) {
        var gc = getGraphicsContext2D();

        //Clear
        gc.clearRect(0,0,width,height);

        // Define gradient colors for shading
        Color darkColor = ((Color) colour).darker();
        Color lightColor = ((Color) colour).brighter();

        // Create a linear gradient from dark to light across the square
        gc.setFill(new LinearGradient(0, 0, width, height, false, CycleMethod.NO_CYCLE,
            new Stop(0, darkColor), new Stop(0.5, lightColor), new Stop(1, darkColor)));

        gc.fillRect(0, 0, width, height);

        //Border
        gc.setStroke(Color.BLACK);
        gc.strokeRect(0,0,width,height);
    }

    /**
     * Get the column of this block
     * @return column number
     */
    public int getX() {
        return x;
    }

    /**
     * Get the row of this block
     * @return row number
     */
    public int getY() {
        return y;
    }

    /**
     * Get the current value held by this block, representing its colour
     * @return value
     */
    public int getValue() {
        return this.value.get();
    }

    /**
     * Bind the value of this block to another property. Used to link the visual block to a corresponding block in the Grid.
     * @param input property to bind the value to
     */
    public void bind(ObservableValue<? extends Number> input) {
        value.bind(input);
    }

    /**
     * Updates the hover state of the block
     * @param hover checks if a block is being hovered or not
     */
   public void setHovering(boolean hover){
        this.hover = hover;
        paint();
   }

    /**
     * Fade out animation for the cleared lines
     */
    public void fadeOut() {
        AnimationTimer timer = new AnimationTimer() {
            double width = 0;

            @Override
            public void handle(long l) {
                GameBlock.this.paintEmpty();
                width += 10;
                if (width >= GameBlock.this.width) {
                    stop(); // Stop the animation when the explosion covers the entire block width
                    return;
                }
                var gc = getGraphicsContext2D();
                gc.setFill(Color.rgb(255, 255, 0, 0.5));
                gc.fillRect((GameBlock.this.width - width) / 2, 0, width, GameBlock.this.height);
            }
        };
        timer.start();
    }


}
