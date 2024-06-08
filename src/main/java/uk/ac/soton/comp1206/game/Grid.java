package uk.ac.soton.comp1206.game;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * The Grid is a model which holds the state of a game board. It is made up of a set of Integer values arranged in a 2D
 * arrow, with rows and columns.
 * Each value inside the Grid is an IntegerProperty can be bound to enable modification and display of the contents of
 * the grid.
 * The Grid contains functions related to modifying the model, for example, placing a piece inside the grid.
 * The Grid should be linked to a GameBoard for its display.
 */
public class Grid {
    private static final Logger logger = LogManager.getLogger(Grid.class);

    /**
     * The number of columns in this grid
     */
    private final int cols;

    /**
     * The number of rows in this grid
     */
    private final int rows;

    /**
     * The grid is a 2D arrow with rows and columns of SimpleIntegerProperties.
     */
    private final SimpleIntegerProperty[][] grid;

    /**
     * Create a new Grid with the specified number of columns and rows and initialise them
     * @param cols number of columns
     * @param rows number of rows
     */
    public Grid(int cols, int rows) {
        this.cols = cols;
        this.rows = rows;

        //Create the grid itself
        grid = new SimpleIntegerProperty[cols][rows];

        //Add a SimpleIntegerProperty to every block in the grid
        for(var y = 0; y < rows; y++) {
            for(var x = 0; x < cols; x++) {
                grid[x][y] = new SimpleIntegerProperty(0);
            }
        }
    }

    /**
     * Returns whether a piece can be played at a given position
     * @param piece A given piece
     * @param x The x coordinate
     * @param y Yhe y coordinate
     * @return Whether a piece can be played
     */
    public boolean canPlayPiece(GamePiece piece, int x, int y) {
        logger.info(String.format("Checking if a piece can be played at position (%d, %d)", x, y));

        x -= 1;
        y -= 1;
        int[][] pieceBlocks = piece.getBlocks();
        //iterate through the blocks of the GamePiece
        for (int i = 0; i < pieceBlocks.length; i++) {
            for (int j = 0; j < pieceBlocks[i].length; j++) {
                int value = pieceBlocks[i][j];
                if (value == 0)
                    continue;
                int gridX = x + i;
                int gridY = y + j;
                if (gridX < 0 || gridX >= cols || gridY < 0 || gridY >= rows)
                    return false;
                if (get(gridX, gridY) != 0)
                    return false;
            }
        }
        return true;
    }

    /**
     * Writing to the grid the value of the piece
     * @param piece the GamePiece to play
     * @param x the x coordinate
     * @param y the y coordinate
     */
    public void playPiece(GamePiece piece, int x, int y){
        logger.info(String.format("Playing piece %s at position (%d, %d)", piece.toString(), x +1, y+1));

        x -= 1;
        y -= 1;
        int[][] pieceBlocks = piece.getBlocks();

        for(int i = 0; i < pieceBlocks.length; i++){
            for (int j = 0; j < pieceBlocks[i].length; j++){
                int value = pieceBlocks[i][j];
                if (value != 0){
                    //calculates the grid value to place the piece
                    int gridX = x + i;
                    int gridY = y + j;
                    set(gridX, gridY, piece.getValue());
                }
            }
        }
    }

    /**
     * Get the Integer property contained inside the grid at a given row and column index. Can be used for binding.
     * @param x column
     * @param y row
     * @return the IntegerProperty at the given x and y in this grid
     */
    public IntegerProperty getGridProperty(int x, int y) {
        return grid[x][y];
    }

    /**
     * Update the value at the given x and y index within the grid
     * @param x column
     * @param y row
     * @param value the new value
     */
    public void set(int x, int y, int value) {
        grid[x][y].set(value);
    }

    /**
     * Get the value represented at the given x and y index within the grid
     * @param x column
     * @param y row
     * @return the value
     */
    public int get(int x, int y) {
        try {
            //Get the value held in the property at the x and y index provided
            return grid[x][y].get();
        } catch (ArrayIndexOutOfBoundsException e) {
            //No such index
            return -1;
        }
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
     * cleans the grid
     */
    public void clean(){
        for (int x = 0; x < this.cols; x++){
            for ( int y =0; y < this.rows; y ++){
                this.grid[x][y].set(0);
            }
        }
    }

}
