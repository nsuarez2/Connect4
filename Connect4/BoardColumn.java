package Connect4;

import java.util.Arrays;

/**
 * Created by Nico on 12/1/15.
 */
public class BoardColumn {

    private Color[] positions;
    private Status status;
    private int size;

    public BoardColumn(int maxSize) {
        positions = new Color[maxSize];
        Arrays.fill(positions, Color.OPEN_COLOR);
        status = (maxSize != 0)? Status.HAS_ROOM: Status.WAS_FULL;
        size = maxSize - 1;
    }

    // Returns the color of the column at the specifid row
    Color at(int row) {
        return positions[row];
    }


    // Tries to add a peice "colorToAdd" to this column
    // Returns the following:
    //
    // HAS_ROOM    - the column still has room
    // IS_NOW_FULL - the column just filled up
    // WAS_FULL    - the column was already full - a problem
    Status addPiece(Color colorToAdd) {
        if (isFull()) {
            System.out.println(
                    "Tried to add a peice when a column was already full?");
            return Status.WAS_FULL;
        }
        positions[size] = colorToAdd;
        size--;
        status = (size < 0)? Status.IS_NOW_FULL: Status.HAS_ROOM;
        return status;
    }

    int getSize() {
        return size;
    }

    public boolean isFull() {
        return (status == Status.WAS_FULL || status == Status.IS_NOW_FULL) ;
    }

}
