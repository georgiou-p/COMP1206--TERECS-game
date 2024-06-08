package uk.ac.soton.comp1206.game;

import java.util.HashSet;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.component.GameBlockCoordinate;
import uk.ac.soton.comp1206.event.GameLoopListener;
import uk.ac.soton.comp1206.event.GameOverListener;
import uk.ac.soton.comp1206.event.LineClearedListener;
import uk.ac.soton.comp1206.event.NextPieceListener;
import uk.ac.soton.comp1206.media.Multimedia;

/**
 * The Game class handles the main logic, state and properties of the TetrECS game. Methods to manipulate the game state
 * and to handle actions made by the player should take place inside this class.
 */
public class Game {

    protected static final Logger logger = LogManager.getLogger(Game.class);

    /**
     * Number of rows
     */
    protected final int rows;

    /**
     * Number of columns
     */
    protected final int cols;

    /**
     * The grid model linked to the game
     */
    protected final Grid grid;

    /**
     * The current GamePiece in play
     */
    protected GamePiece currentPiece;

    /**
     * Score of the game
     */
    protected final IntegerProperty score;

    /**
     * Level of the game
     */
    protected final IntegerProperty level;

    /**
     * Lives remaining in the game
     */
    protected final IntegerProperty lives;

    /**
     * Multiplier for scoring
     */
    protected final IntegerProperty multiplier;

    /**
     * Listener for providing the next piece
     */
    protected NextPieceListener nextPieceListener;

    /**
     * Listener for when a line is cleared
     */
    protected LineClearedListener lineClearedListener;

    /**
     * Listener for repeating a game
     */
    protected GameLoopListener gameLoopListener;

    /**
     * Listener for when the game is over
     */
    protected GameOverListener gameOverListener;

    /**
     * The piece that is coming next
     */
    protected GamePiece followingPiece;

    /**
     * Responsible for looping the game
     */
    protected ScheduledFuture<?> loop;
    /**
     * Responsible for timing the looper
     */
    protected ScheduledExecutorService timer;


    /**
     * Create a new game with the specified rows and columns. Creates a corresponding grid model.
     * @param cols number of columns
     * @param rows number of rows
     */
    public Game(int cols, int rows) {
        this.cols = cols;
        this.rows = rows;
        //Create a new grid model to represent the game state
        this.grid = new Grid(cols,rows);

        this.score = new SimpleIntegerProperty(0);
        this.level = new SimpleIntegerProperty(0);
        this.lives = new SimpleIntegerProperty(3);
        this.multiplier = new SimpleIntegerProperty(1);
        this.timer = Executors.newSingleThreadScheduledExecutor();
    }

    /**
     * Start the game
     */
    public void start() {
        logger.info("Starting game");
        initialiseGame();
        followingPiece = spawnPiece();
        loop = timer.schedule(this::gameLoop, getTimerDelay(), TimeUnit.MILLISECONDS);
        gameLoopListener();
    }

    /**
     * Initialise a new game and set up anything that needs to be done at the start
     */
    public void initialiseGame() {
        logger.info("Initialising game");
        setFollowingPiece(spawnPiece());
        nextPiece();
    }

    /**
     * Handle what should happen when a particular block is clicked
     *
     * @param gameBlock the block that was clicked
     */
    public void blockClicked(GameBlock gameBlock) {
        int x = gameBlock.getX();
        int y = gameBlock.getY();

        boolean piecePlacedSuccessfully = grid.canPlayPiece(currentPiece, x, y);
        if (piecePlacedSuccessfully) {
            grid.playPiece(currentPiece, x, y);
            nextPiece();
            afterPiece();
            Multimedia.playSound("place.wav");
            loop.cancel(false);
            loop = timer.schedule(this::gameLoop, getTimerDelay(), TimeUnit.MILLISECONDS);
            gameLoopListener();
            logger.info("Timer was reset");
        }else {
            Multimedia.playSound("fail.wav");
        }
    }
    
    /**
     * Randomly spawns a GamePiece
     * @return returns the spawned GamePiece
     */
    public GamePiece spawnPiece(){
        logger.info("Spawning a new random piece");

        Random random = new Random();
        int randomPieceNumber = random.nextInt(GamePiece.PIECES);
        return GamePiece.createPiece(randomPieceNumber);

    }

    /**
     * Replaces the current piece with a new piece
     */
    public void nextPiece(){
        logger.info("Getting next random piece");

        setCurrentPiece(getFollowingPiece());
        setFollowingPiece(spawnPiece());
        if (nextPieceListener != null) {
            nextPieceListener.nextPiece(getCurrentPiece(), getFollowingPiece());
        }
    }

    /**
     * swaps the current piece with the following piece
     */
    public void swapCurrentPiece(){
        GamePiece temp = getCurrentPiece();
        setCurrentPiece(getFollowingPiece());
        setFollowingPiece(temp);
        Multimedia.playSound("transition.wav");
        logger.info("Swapped current piece with following piece");

    }

    /**
     * Listener for the next piece
     * @param listener listens to the next piece
     */
    public void setNextPieceListener(NextPieceListener listener){
        this.nextPieceListener = listener;
    }


    /**
     * Clears full lines
     */
    public void afterPiece() {
        logger.info("Handling actions after playing a piece");
        HashSet<GameBlockCoordinate> blocksToClear = new HashSet<>();
        int linesToClear = 0;
        boolean clearedLines = false; //track if lines were cleared
        //Search for horizontal lines
        for (int y = 0; y < rows; y++){
            boolean fullLine = true;
            for (int x = 0; x < cols; x++){
                if (grid.get(x,y) == 0){
                    fullLine = false;
                    break;
                }
            }
            if (fullLine){
                linesToClear++;
                clearedLines = true;
                for (int x = 0; x < cols; x++){
                    blocksToClear.add(new GameBlockCoordinate(x,y));
                }
            }
        }
        //Search for vertical lines
        for (int x =0; x< cols; x++) {
            boolean fullLine = true;
            for (int y = 0; y < rows; y++){
                if (grid.get(x,y) == 0){
                    fullLine = false;
                    break;
                }
            }
            if (fullLine){
                linesToClear++;
                clearedLines = true;
                for (int y = 0; y< rows; y++){
                    blocksToClear.add(new GameBlockCoordinate(x, y));
                }
            }
        }
        //Clear the marked blocks from the grid
        for(GameBlockCoordinate coordinate : blocksToClear){
            grid.set(coordinate.getX(), coordinate.getY(), 0);
        }
        //effects for clearing a line
        if (clearedLines){
            Multimedia.playSound("clear.wav");
            if (lineClearedListener != null){
                lineClearedListener.lineCleared(blocksToClear);
            }
        }
        //Calculate and update the score
        score(linesToClear, blocksToClear.size());
        //Update the multiplier
        multiplier(clearedLines);
    }

    /**
     * Calculates and updates the score level
     * @param lines number of lines cleared
     * @param blocks number of blocks
     */
    public void score(int lines, int blocks){
        int points = lines * blocks * multiplier.get() * 10;
        int newScore = getScore() + points;
        setScore(newScore);
        logger.info("Score added, Score: " + score.get());
        // Calculate the level
        int newLevel = newScore / 1000;
        if (newLevel > getLevel()) {
            setLevel(newLevel);
            Multimedia.playSound("level.wav");
            logger.info("Level up! Level: " + newLevel);
        }
    }

    /**
     * updates the multiplier
     * @param clearedLines if any lines were cleared
     */
    public void multiplier(boolean clearedLines){
        if (clearedLines){
            multiplier.set(multiplier.get()+1);
        } else {
            resetMultiplier();
        }
        logger.info("Updated the multiplier to: " + multiplier.get());
    }

    /**
     * rotates the current piece
     */
    public void rotateCurrentPiece(){
        if (currentPiece != null){
            currentPiece.rotate();
            Multimedia.playSound("rotate.wav");
            logger.info("Rotating current piece 90 degrees");
            if (nextPieceListener != null){
                nextPieceListener.nextPiece(getCurrentPiece(), getFollowingPiece());
            }
        }
    }

    /**
     * Calculates the time delay for the timer at a certain level
     * @return the time to delay
     */
    public int getTimerDelay(){
        return Math.max(2500, (12000-(500*getLevel())));
    }

    /**
     * Handles the looping of the game, in designated threads
      */
    public void gameLoop(){
        logger.info("Game loop fired");
        Platform.runLater(this::lostLive);
        Platform.runLater(this:: resetMultiplier);
        nextPiece();
        gameLoopListener();
        loop = timer.schedule(this::gameLoop, getTimerDelay(), TimeUnit.MILLISECONDS);
    }

    /**
     * Handles when the user loses a life
     */
    public void lostLive(){
        if (getLives() > 0){
            setLives(getLives()-1);
            Multimedia.playSound("lifelose.wav");
            logger.info("Lost a life");
        }else {
            logger.info("Game over");
            if (gameOverListener != null){
                Platform.runLater(()-> gameOverListener.gameOver());
            }
        }
    }

    /**
     * Sets the multiplier to 1
     */
    public void resetMultiplier(){
        setMultiplier(1);
    }

    /**
     * Stops the loop from running
     */
    public void endLoop(){
        this.loop.cancel(true);
        logger.info("Timer was shut down");
    }

    /**
     * Listener for the gameLoop
     */
    public void gameLoopListener(){
        if (gameLoopListener != null){
            gameLoopListener.gameLoop(getTimerDelay());
        }
    }

    /**
     * Sets the listener to the GameLoopListener
     * @param listener the listener to be passed
     */
    public void setOnGameLoop(GameLoopListener listener){
        gameLoopListener = listener;
    }

    /**
     * Sets the game over listener
     * @param listener the game over listener
     */
    public void setOnGameOver(GameOverListener listener){
        gameOverListener = listener;
    }

    /**
     * @return returns the following piece
     */
    public GamePiece getFollowingPiece(){
        return followingPiece;
    }

    /**
     * @return returns the current piece being played
     */
    public GamePiece getCurrentPiece() {
        return currentPiece;
    }

    /**
     * sets to the current piece the passed piece
     * @param currentPiece the piece being playing
     */
    public void setCurrentPiece(GamePiece currentPiece){
        this.currentPiece = currentPiece;
    }

    /**
     * sets to the followingPiece the passed piece
     * @param followingPiece the piece to be played next
     */
    public void setFollowingPiece(GamePiece followingPiece) {
        this.followingPiece = followingPiece;
    }

    /**
     * Sets the clearedLine listener
      * @param listener the listener to be set
     */
    public void setOnClearedLine(LineClearedListener listener){
        lineClearedListener = listener;
    }

    /**
     * Get the grid model inside this game representing the game state of the board
     * @return game grid model
     */
    public Grid getGrid() {
        return grid;
    }

    /**
     * Get the number of columns in this game
     * @return number of columns
     */
    public int getCols() {
        return cols;
    }

    /**
     * Get the number of rows in this game
     * @return number of rows
     */
    public int getRows() {
        return rows;
    }

    /**
     * Returns the score
     * @return score number
     */
    public int getScore(){
        return score.get();
    }

    /**
     * Get the score property
     * @return score property
     */
    public IntegerProperty scoreProperty(){
        return score;
    }

    /**
     * Sets the score
     * @param score score to set
     */
    public void setScore(int score) {
        this.score.set(score);
    }

    /**
     * Get the level
      * @return level number
     */
    public int getLevel() {
        return level.get();
    }

    /**
     * Get the property of the level
     * @return level property
     */
    public IntegerProperty levelProperty() {
        return level;
    }

    /**
     * Assign the level
     * @param level level lumber
     */
    public void setLevel(int level) {
        this.level.set(level);
    }

    /**
     * Get the number of lives
      * @return number of lives
     */
    public int getLives() {
        return lives.get();
    }

    /**
     * get the lives property
     * @return live property
     */
    public IntegerProperty livesProperty() {
        return lives;
    }

    /**
     * Assign the live number
     * @param lives lives left
     */
    public void setLives(int lives) {
        this.lives.set(lives);
    }

    /**
     * get the multiplayer
     * @return multiplayer number
     */
    public int getMultiplier() {
        return multiplier.get();
    }

    /**
     * get the multiplayer property
     * @return multiplayer property
     */
    public IntegerProperty multiplierProperty() {
        return multiplier;
    }

    /**
     * Assign the multiplayer number
     * @param multiplier multiplayer number
     */
    public void setMultiplier(int multiplier) {
        this.multiplier.set(multiplier);
    }


}
