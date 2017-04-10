package solver;

import android.util.Log;

import java.util.Deque;
import java.util.LinkedList;

/**
 *
 * Created by kirtsim on 14/02/2017.
 */

public class BackTracker {
    private static final String TAG = "BackTracker";

    private Puzzle puzzle;
    private LineSolver          lineSolver;
    private final Deque<long[]> lineStack;
    private final Deque<byte[]>       numOfCellsToPaintStack;
    private final Deque<boolean[]>    isInQueueStack;
    private final Deque<MyByteQueue>  queuesStack;
    private final Deque<byte[]>       lastPixelStack;

    private long[] lines;
    private byte[] numOfCellsToPaint;
    private boolean[] isInQueue;
    private MyByteQueue queue;
    private byte[] lastPixel;

    private final StacksLoader  stacksLoader;
    private boolean continueSolving;

    private long totalSolvingTime;
    private int backTrackIterations;
    private int maxStackLoad;


//    public BackTracker(Puzzle puzzle) {
//        this(puzzle, new LineSolver(puzzle));
//    }

    public BackTracker(Puzzle puzzle, LineSolver lineSolver) {
        this.puzzle = puzzle;
        this.lineSolver = lineSolver;
        lineStack = new LinkedList<>();
        numOfCellsToPaintStack = new LinkedList<>();
        isInQueueStack = new LinkedList<>();
        queuesStack = new LinkedList<>();
        lastPixelStack = new LinkedList<>();
        stacksLoader = new StacksLoader(puzzle.lines.length);
    }

    public long getTotalSolvingTime() {
        return totalSolvingTime;
    }

    public int getBackTrackIterations() {
        return backTrackIterations;
    }

    public int getMaxStackLoad() {
        return maxStackLoad;
    }

    public int backTrack() {
        initiallyLoadStacks();
        continueSolving = !lineStack.isEmpty();
        return backTrackIter();
    }

    private int backTrackIter() {
        int status = Puzzle.UNCHANGED;
        final long start = System.nanoTime();
        while(continueSolving) {
            popFomStacksAndAssing();
            final int stackLoad = lineStack.size();
            if (stackLoad > maxStackLoad)
                maxStackLoad = stackLoad;
            status = lineSolver.solveLines(lines,
                    numOfCellsToPaint,
                    isInQueue,
                    queue);
            processStatusResult(status);
            backTrackIterations++;
        }
        this.totalSolvingTime = System.nanoTime() - start;
        return status;
    }

    private void initiallyLoadStacks() {
        lineStack.addFirst(puzzle.lines);
        numOfCellsToPaintStack.addFirst(puzzle.numOfCellsToPaintPerLine);
        isInQueueStack.addFirst(puzzle.inQueue);
        queuesStack.addFirst(addInitiallyLinesToQueue());
        lastPixelStack.addFirst(new byte[]{-1, -1});
    }

    private MyByteQueue addInitiallyLinesToQueue() {
        final boolean[] inQ = puzzle.inQueue;
        final int length = puzzle.lines.length;
        MyByteQueue newQueue = new MyByteQueue(length);
        for (int i = 0; i < length; i++) {
            newQueue.add((byte) i);
            inQ[i] = true;
        }
        return newQueue;
    }

    private void popFomStacksAndAssing() {
        lines = lineStack.pollFirst();
        numOfCellsToPaint = numOfCellsToPaintStack.pollFirst();
        isInQueue = isInQueueStack.pollFirst();
        queue = queuesStack.pollFirst();
        lastPixel = lastPixelStack.pollFirst();
    }

    private void processStatusResult(int status) {
        if (status == Puzzle.SOLVED) {
            continueSolving = false;
        } else if (status != Puzzle.CONFLICT) {
            byte[] pixelToPaint = choosePixelToPaint();
            paintAndLoadStacks(pixelToPaint);
            if (lineStack.isEmpty())
                continueSolving = false;
        }
    }

    private byte[] choosePixelToPaint() {
        final int lastRow = lastPixel[0];
        final int lastCol = lastPixel[1];
        final int rowCount = puzzle.rowCount;
        final int colCount = puzzle.colCount;
        byte[] nextPixel = {-1, -1};
        int nextCol = (lastCol + 1) % colCount;
        int nextRow = nextCol == 0 ? lastRow + 1 : lastRow;
        for (int i = nextRow; i < rowCount; i++) {
            if (numOfCellsToPaint[i] > 0) {
                long unknown = 0x3000000000000000L >>> (2 * nextCol);
                for (int j = nextCol; j < colCount; j++, unknown >>>= 2) {
                    if ((lines[i] & unknown) == unknown) {
                        nextPixel[0] = (byte)i;
                        nextPixel[1] = (byte)j;
                        return nextPixel;
                    }
                }
            }
            nextCol = 0;
        }
        return nextPixel;
    }

    private boolean paintAndLoadStacks(final byte[] pixelToPaint) {
        if (isPixelValid(pixelToPaint)) {
            stacksLoader.makeCopiesAndFillQueues();
            stacksLoader.paintPixel(pixelToPaint);
            stacksLoader.fillStacks();
            return true;
        }
        Log.d(TAG, "painting failed; current stack load: " + lineStack.size());
        return false;
    }

    private boolean isPixelValid(byte[] pixel) {
        if (pixel[0] < 0 || pixel[0] >= puzzle.rowCount ||
                pixel[1] < 0 || pixel[1] >= puzzle.colCount ) {
            System.out.println("Row or Col index out of bounds!!");
            return false;
        }
        return true;
    }

    public void stopSolving() {
        this.continueSolving = false;
        Log.d("BACKTRACK", "stop requested");
    }

    private class StacksLoader {
        private final int length;
        private long[] linesB;
        private long[] linesW;
        private byte[] numCellsB;
        private byte[] numCellsW;
        private boolean[] inQueueB;
        private boolean[] inQueueW;
        private MyByteQueue queueB;
        private MyByteQueue queueW;
        private byte[] newPixelPainted;

        StacksLoader(int length) {
            this.length = length;
        }

        void makeCopiesAndFillQueues() {
            initAllArraysAndQueues();
            final long[] currLines = lines;
            final byte[] numCells = numOfCellsToPaint;
            for (int i = 0; i < length; i++) {
                linesB[i]    = linesW[i]    = currLines[i];
                numCellsB[i] = numCellsW[i] = numCells[i];
            }
            fillQueues();
        }

        private void fillQueues() {
            final byte[] numCells = numOfCellsToPaint;
            int toAdd = queueB.capacity();
            for (int i = 0; i < length && toAdd > 0; i++) {
                if (numCells[i] > 0) {
                    queueB.add((byte) i);
                    queueW.add((byte) i);
                    inQueueB[i]  = inQueueW[i] = true;
                    toAdd--;
                }

            }
        }

        boolean paintPixel(final byte[] pixel) {
            this.newPixelPainted = pixel;
            final int row = pixel[0];
            final int col = pixel[1];

            final int colLine = puzzle.rowCount + col;
            linesB[row] &= (0xEFFFFFFFFFFFFFFFL >> (col * 2)); // paint row line
            linesW[row] &= (0xDFFFFFFFFFFFFFFFL >> (col * 2)); // paint row line
            linesB[colLine] &= (0xEFFFFFFFFFFFFFFFL >> (row * 2));      // paint col line
            linesW[colLine] &= (0xDFFFFFFFFFFFFFFFL >> (row * 2));      // paint col line
            decrementNumberOfPixelsToPaintAt(row, colLine);
            return true;
        }

        void fillStacks() {
            fillWhites();
            fillBlacks();
        }

        private void fillBlacks() {
            lineStack.addFirst(linesB);
            numOfCellsToPaintStack.addFirst(numCellsB);
            isInQueueStack.addFirst(inQueueB);
            queuesStack.addFirst(queueB);
            lastPixelStack.addFirst(newPixelPainted);
        }

        private void fillWhites() {
            lineStack.addFirst(linesW);
            numOfCellsToPaintStack.addFirst(numCellsW);
            isInQueueStack.addFirst(inQueueW);
            queuesStack.addFirst(queueW);
            lastPixelStack.addFirst(newPixelPainted);
        }

        private void decrementNumberOfPixelsToPaintAt(int rowLineIndex, int colLineIndex) {
            numCellsB[rowLineIndex] = --numCellsW[rowLineIndex];
            numCellsB[colLineIndex] = --numCellsW[colLineIndex];
        }


        private void initAllArraysAndQueues() {
            final int remainingLines = lineSolver.getRemainingLinesCount();
            linesB = new long[length];
            linesW = new long[length];
            numCellsB = new byte[length];
            numCellsW = new byte[length];
            inQueueB = new boolean[length];
            inQueueW = new boolean[length];
            queueB = new MyByteQueue(remainingLines);
            queueW = new MyByteQueue(remainingLines);
        }
    }
}
