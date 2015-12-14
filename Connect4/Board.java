package Connect4;

import java.util.HashSet;

/**
 * Created by Nico on 12/1/15.
 */
public class Board {

    private BoardColumn[] columns;
    private HashSet<Integer> availableCols;
    private int numCols;
    private int numRows;

    public Board(int inNumRows, int inNumCols) {

        columns = new BoardColumn[inNumCols];
        availableCols = new HashSet<>();
        for (int i = 0; i < inNumCols; i++) {
            columns[i] = new BoardColumn(inNumRows);
            availableCols.add(i);
        }

        numCols = inNumCols;
        numRows = inNumRows;
    }

    // This will try to add a piece of "color" to the column "colNum"
    // After, will check for a winner and return the following:
    //
    // RED        - Red has won the game
    // YELLOW     - Yellow has won the game
    // FULL_COLOR - This column is now full, possible tie
    // OPEN_COLOR - There is no winner, nothing to be done
    // BAD_COLOR  - Something went wrong
    //
    Color addPiece(int colNum, Color color) {

        if (!availableCols.contains(colNum)) {
            System.out.println("This column should already be done?");
            System.exit(1);
            return Color.BAD_COLOR;
        }

        Status tryToAdd = columns[colNum].addPiece(color);
        Color result = Color.BAD_COLOR;

        switch (tryToAdd) {

            case IS_NOW_FULL:
                availableCols.remove(colNum);
                result = checkForWinner(color, colNum, topOf(colNum));
                if (result == Color.OPEN_COLOR) result = Color.FULL_COLOR;
                break;

            case HAS_ROOM:
                result = checkForWinner(color, colNum, topOf(colNum));
                break;

            case WAS_FULL:
                System.out.println(
                        "Tried to add a peice when a column was already full?");
                System.exit(1);

            default:
                System.out.println("I should never get here?");
                System.exit(2);
        }

        return result;
    }

    // Returns the top row available of the column
    public int topOf(int colNum) {
        return columns[colNum].getSize() + 1;
    }

    // Returns true if the board is empty
    public boolean isEmpty() {
        return availableCols.isEmpty();
    }


    // Checks to see if a player has won
    //
    // returns the following:
    //
    // RED        - Red has won the game
    // YELLOW     - Yellow has won the game
    // OPEN_COLOR - There is no winner
    //
    private Color checkForWinner(Color colorToCheck, int inX, int inY) {

        // count of the streak
        int count = 1;

        // Check flat (left and right)
        count += checkDirection(colorToCheck, 1, 0, inX, inY);
        count += checkDirection(colorToCheck, -1, 0, inX, inY);
        if (count >= 4) return colorToCheck;

        // Check vertical (up and down)
        count = 1;
        count += checkDirection(colorToCheck, 0, 1, inX, inY);
        count += checkDirection(colorToCheck, 0, -1, inX, inY);
        if (count >= 4) return colorToCheck;

        // Check diagonal down
        count = 1;
        count += checkDirection(colorToCheck, 1, -1, inX, inY);
        count += checkDirection(colorToCheck, -1, 1, inX, inY);
        if (count >= 4) return colorToCheck;

        // Check diagonal up
        count = 1;
        count += checkDirection(colorToCheck, 1, 1, inX, inY);
        count += checkDirection(colorToCheck, -1, -1, inX, inY);
        if (count >= 4) return colorToCheck;


        return Color.OPEN_COLOR;
    }

    // performs check in specified direction
    //
    // returns the count of the streak found in that direction
    private int checkDirection(Color colorToCheck, int changeInX, int changeInY,
                               int inX, int inY) {

        int count = 0;
        int x = inX + changeInX;
        int y = inY + changeInY;

        while (isValid(x, y) && count < 4 && columns[x].at(y) == colorToCheck) {
            count++;
            x += changeInX;
            y += changeInY;
        }

        return count;
    }


    // Returns true if x and y make a valid location that is also on the board
    public boolean isValid(int x, int y) {
        return (x >= 0 &&
                x < numCols &&
                y >= 0 &&
                y < numRows);
    }

}
