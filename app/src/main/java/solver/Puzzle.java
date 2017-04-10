package solver;

/**
 *
 * Created by kirtsim on 14/02/2017.
 */

public class Puzzle {
    public static final int SOLVED = 0;
    public static final int PAINTED = 1;
    public static final int UNCHANGED = 2;
    public static final int CONFLICT = 3;

    public long    [] lines;
    public boolean [] inQueue;
    public byte    [] numOfCellsToPaintPerLine;

    public int rowCount;
    public int colCount;
    public int linesToSolve;
    public byte[][] descriptionSums;
    public byte[][] descriptions;
}
