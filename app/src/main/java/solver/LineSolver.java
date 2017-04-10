package solver;

/**
 *
 * Created by kirtsim on 14/02/2017.
 */

public class LineSolver {
    private static final long [] WHITES = {
            0,                   0x1000000000000000L, // the first is omitted as it is in a line it is always white
            0x0400000000000000L, 0x0100000000000000L,
            0x0040000000000000L, 0x0010000000000000L,
            0x0004000000000000L, 0x0001000000000000L,
            0x0000400000000000L, 0x0000100000000000L,
            0x0000040000000000L, 0x0000010000000000L,
            0x0000004000000000L, 0x0000001000000000L,
            0x0000000400000000L, 0x0000000100000000L,
            0x0000000040000000L, 0x0000000010000000L,
            0x0000000004000000L, 0x0000000001000000L,
            0x0000000000400000L, 0x0000000000100000L,
            0x0000000000040000L, 0x0000000000010000L,
            0x0000000000004000L, 0x0000000000001000L,
            0x0000000000000400L, 0x0000000000000100L,
            0x0000000000000040L, 0x0000000000000010L,
            0x0000000000000004L,
    };

    public static final long WHITE_CELL = 0x4000000000000000L;
    //public static final long BLACK_CELL = 0x8000000000000000L;
    private static final long UNKNOWN_CELL11 = 0xC000000000000000L;

    private long totalSolvingTime;
    private long totalLinesProcessed;

    private final byte    [][] descriptions;
    private final byte    [][] descriptionSums;
    private long            [] lines;
    private byte            [] numOfCellsToPaint;
    private boolean         [] inQueue;

    private MyByteQueue indexesOfLinesToProcess;

    // helper attributes
    private int         linesRemaining;
    private final int   rowCount;
    private final int   colCount;
    private int         lineLength;
    private byte[]      description;
    private byte[]      descSum;
    private long        currentLine;
    private int         currLineIndex;
    private long        resultLine;
    private int      status;

    public LineSolver(Puzzle puzzle) {
        rowCount = puzzle.rowCount;
        colCount = puzzle.colCount;
        descriptions = puzzle.descriptions;
        descriptionSums = puzzle.descriptionSums;
        lines = puzzle.lines;
        linesRemaining = puzzle.linesToSolve;
        numOfCellsToPaint = puzzle.numOfCellsToPaintPerLine;
        inQueue = puzzle.inQueue;
        indexesOfLinesToProcess = new MyByteQueue(lines.length);
        status = Puzzle.UNCHANGED;
    }

    public long[] getLines() {
        return lines;
    }

    int getRemainingLinesCount() {
        return this.linesRemaining;
    }

    public long getTotalSolvingTime() {
        return totalSolvingTime;
    }

    public long getTotalLinesProcessed() {
        return totalLinesProcessed;
    }

    public int getSolvingStatus() {
        return status;
    }

    public int solveLines(long[] lines, byte[] numCells, boolean[] inQueue, MyByteQueue queue) {
        this.status = Puzzle.UNCHANGED;
        this.lines = lines;
        this.numOfCellsToPaint = numCells;
        this.inQueue = inQueue;
        this.linesRemaining = queue.getSize();
        this.indexesOfLinesToProcess = queue;
        solveLines();
        return status;
    }

    private void solveLines() {
        final long startTime = System.nanoTime();
        if (indexesOfLinesToProcess.isEmpty())
            fillLineQueue();
        while (setNextCurrentLine()) {
            totalLinesProcessed++;
            setHelperAttributes();
            if (description.length == 0 && !paintAllCellsWhite()) {
                this.status = Puzzle.CONFLICT;
                return;
            } else if (!fix(lineLength, description.length - 1)) {
                this.status = Puzzle.CONFLICT;
                return;
            }
            processLineChanges();
        }
        totalSolvingTime += System.nanoTime() - startTime;
        updateStatusToSolvedIfAllLinesAreSolved();
    }


    private void fillLineQueue() {
        if (indexesOfLinesToProcess == null || indexesOfLinesToProcess.isEmpty())
            indexesOfLinesToProcess = new MyByteQueue(linesRemaining);
        final int length = lines.length;
        for (int i = 0; i < length; i++)
            if (numOfCellsToPaint[i] > 0)
                indexesOfLinesToProcess.add((byte) i);
    }


    private boolean setNextCurrentLine() {
        if (!indexesOfLinesToProcess.isEmpty()) {
            currLineIndex = indexesOfLinesToProcess.pop();
//            if (numOfCellsToPaint[currLineIndex] > 0) {
                currentLine = lines[currLineIndex];
                inQueue[currLineIndex] = false;
                return true;
//            }
        }
        return false;
    }

    private void setHelperAttributes() {
        resultLine = WHITE_CELL;
        description = descriptions[currLineIndex];
        descSum = descriptionSums[currLineIndex];
        lineLength = getCurrentLineLength();
    }

    private boolean paintAllCellsWhite() {
        final long template = (UNKNOWN_CELL11 >> (2*lineLength)) & 0x5555555555555555L;
        resultLine = template & currentLine;
        return resultLine == template;
    }

    private void updateStatusToSolvedIfAllLinesAreSolved() {
        for (int i = 0; i < rowCount; i++) {
            if (numOfCellsToPaint[i] > 0)
                return;
        }
        this.status = Puzzle.SOLVED;
    }

    private void processLineChanges() {
        final long change = currentLine ^ resultLine;
        if (change != 0) {
            updateLinesWith(change);
            paintCurrentLine();
            decrementLinesRemainingIfNeeded(currLineIndex);
        }
    }

    private void updateLinesWith(long change) {
        final int indexOffset = getIndexOffset();
        final int length = this.lineLength;
        final int currentLineIndex = this.currLineIndex;
        change <<= 2;
        for (int i = 0; i < length; i++, change <<= 2) {
            long cellUpdate = change & UNKNOWN_CELL11;
            if (cellUpdate != 0) {
                numOfCellsToPaint[currentLineIndex]--;
                cellUpdate = adjustFirstCellToCurrentLineIndex(cellUpdate);
                final int updLineIndex = indexOffset + i;
                lines[updLineIndex] &= (~cellUpdate);
                numOfCellsToPaint[updLineIndex]--;
                addLineToQueueIfNeededAndIsNotThere(updLineIndex);
            }
        }
    }


    private void paintCurrentLine() {
        lines[currLineIndex] &= resultLine;
        this.status = Puzzle.PAINTED;
    }


    private void addLineToQueueIfNeededAndIsNotThere(int index) {
        if (!decrementLinesRemainingIfNeeded(index)) {
            if (!inQueue[index]) {
                inQueue[index] = true;
                indexesOfLinesToProcess.add((byte)index);
            }
        }
    }

    private boolean decrementLinesRemainingIfNeeded(int index) {
        if (numOfCellsToPaint[index] == 0) {
            linesRemaining--;
            return true;
        }
        return false;
    }

    private int getCurrentLineLength() {
        if (currLineIndex < rowCount)
            return colCount;
        return rowCount;
    }

    private int getIndexOffset() {
        if (currLineIndex < rowCount)
            return rowCount;
        return 0;
    }

    private long adjustFirstCellToCurrentLineIndex(long cell) {
        if (currLineIndex < rowCount)
            return (cell >>> (currLineIndex + 1) * 2);
        return (cell >>> (currLineIndex - rowCount + 1) * 2);
    }

    private boolean fix(int cellIndex, int descIndex) {
        if (cellIndex == 0 || cellIndex == -1) {
            return descIndex == -1;
        } else if (cellIndex < -1)
            return false;
        boolean hasFix = false;
        if (descIndex > -1)
            hasFix = fixBlack(cellIndex, descIndex);
        if (fixWhite(cellIndex, descIndex))
            hasFix = true;
        return hasFix;
    }

    private boolean fixBlack(int cellIndex, int descIndex) {
        final long blockTemplate = getBlockTemplate(cellIndex, descIndex);
        if (enoughSpaceLeft(cellIndex, descIndex)
                && canPlaceBlock(blockTemplate)) {
            if (fix(cellIndex - description[descIndex] - 1, descIndex - 1)) {
                resultLine |= blockTemplate;
                return true;
            }
        }
        return false;
    }

    private boolean fixWhite(int cellIndex, int descIndex) {
        if (canPlaceWhiteCellOnIndex(cellIndex)) {
            if (fix(cellIndex - 1, descIndex)) {
                resultLine |= WHITES[cellIndex];
                return true;
            }
        }
        return false;
    }

    private boolean canPlaceWhiteCellOnIndex(final int cellIndex) {
        return (currentLine & WHITES[cellIndex]) != 0;
    }

    private boolean canPlaceBlock(final long template) {
        return ((currentLine & template) == template);
    }

    private boolean enoughSpaceLeft(int cellIndex, int descIndex) {
        return (cellIndex - descSum[descIndex] > -1);
    }

    private long getBlockTemplate(int cellIndex, int descIndex) {
        final int whiteCellIndex = cellIndex - description[descIndex];
        long tmplt = 0xAAAAAAAAAAAAAAAAL >>> (whiteCellIndex * 2);
        tmplt &= (0xAAAAAAAAAAAAAAAAL << ((WHITES.length - cellIndex) * 2));
        // adding white cell at the end of the block
        tmplt ^= (UNKNOWN_CELL11 >>> (whiteCellIndex * 2));
        return tmplt; // apps: 00 00 01 10 10 10 00 00... for line with length 5
    }

//    public String getVisual() {
//        StringBuilder builder = new StringBuilder();
//        for (int i = 0; i < rowCount; i++) {
//            long line = lines[i] << 2;
//            for (int c = 1; c <= colCount; c++) {
//                if ((line & 0xC000000000000000L) == 0xC000000000000000L)
//                    builder.append("? ");  // unknown (11)
//                else if ((line & 0xC000000000000000L) == 0)
//                    builder.append(". ");  // unknown (00)
//                else if ((line & 0x4000000000000000L) == 0x4000000000000000L)
//                    builder.append("- ");  // paint white
//                else
//                    builder.append("X ");  // paint black
//                line <<= 2;
//            }
//            builder.append("\n");
//        }
//        return builder.toString();
//    }
}
