package fm.apps.kirtsim.nonogramcheat;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;

import cellsAndDescriptions.PuzzleArraysController;
import db.PuzzleImage;
import solver.BackTracker;
import solver.LineSolver;
import solver.MyByteQueue;
import solver.Puzzle;

public class SolverFragment extends Fragment {
    public static final String TAG = "SolverFragment";

    private SolverFragmentListener mListener;
    private SolveTask solveTask;
    private PuzzleImage puzzleResult;
    private byte[] resultCells;

    interface SolverFragmentListener {
        void onSolvingFinished(int status, byte[] resultCells);
    }

    public static SolverFragment newInstance(Bundle data) {
        SolverFragment fragment = new SolverFragment();
        fragment.setArguments(data);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        Bundle arguments = getArguments();
        this.resultCells = arguments.getByteArray(PuzzleView.PUZZLE_CELLS_PARAM);
        solveTask = new SolveTask(
                arguments.getByteArray(PuzzleView.H_DESCRIPTIONS_PARAM),
                arguments.getByteArray(PuzzleView.V_DESCRIPTIONS_PARAM),
                resultCells);
        solveTask.execute();

    }

    public PuzzleImage getPuzzleImage() {
        if (puzzleResult != null)
            return this.puzzleResult;
        return new PuzzleImage();
    }

    public byte[] getResultCells() {
        return resultCells;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof SolverFragmentListener)
            mListener = (SolverFragmentListener) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (solveTask != null) {
            solveTask.forceStop();
        }
    }

    private static class SolveResults {
        private int status;
        private byte[] result;
        PuzzleImage puzzle;

        SolveResults(int status, byte[] result, PuzzleImage puzzle) {
            this.status = status;
            this.result = result;
            this.puzzle = puzzle;
        }

        int getStatus() {
            return status;
        }

        byte[] getResults() {
            return result;
        }
    }



    private class SolveTask extends AsyncTask<Void, Void, SolveResults> {
        BackTracker backTracker;
        LineSolver lineSolver;
        Puzzle puzzleInfo;
        final byte[] hDescriptions;
        final byte[] vDescriptions;
        final byte[] cells;
        int solveStatus;
        long totalTime;

        SolveTask(byte[] hDescriptions, byte[] vDescriptions, byte[] cells) {
            super();
            this.hDescriptions = hDescriptions;
            this.vDescriptions = vDescriptions;
            this.cells = cells;
            this.solveStatus = Puzzle.UNCHANGED;
        }

        void forceStop() {
            this.cancel(true);
            if (backTracker != null)
                backTracker.stopSolving();
        }

        @Override
        protected SolveResults doInBackground(Void... params) {
            byte[] result = performSolving();
            PuzzleImage puzzle = createPuzzleImageFromResults();
            SolveResults results = new SolveResults(solveStatus, result, puzzle);
            waitForListener();
            cleanMemory();
            return results;
        }

        private byte[] performSolving() {
            byte[] resultCells;
            try {
                createPuzzle();
                MyByteQueue queue = createAndFillQueueWithInQueueArray(puzzleInfo);
                lineSolver = new LineSolver(puzzleInfo);
                final long startTime = System.nanoTime();
                lineSolver.solveLines(puzzleInfo.lines, puzzleInfo.numOfCellsToPaintPerLine, puzzleInfo.inQueue, queue);
                solveStatus = lineSolver.getSolvingStatus();
                if ((solveStatus == Puzzle.UNCHANGED || solveStatus == Puzzle.PAINTED) && !isCancelled()) {
                    backTracker = new BackTracker(puzzleInfo, lineSolver);
                    solveStatus = backTracker.backTrack();
                }
                totalTime = System.nanoTime() - startTime;
                resultCells = PuzzleArraysController.convertLinesIntoCells(
                        lineSolver.getLines(), puzzleInfo.rowCount);
            } catch (Exception e) {
                Log.e(TAG, "EXCEEEEEPTION!!", e);
                resultCells = cells;
            }
            return resultCells;
        }

        private PuzzleImage createPuzzleImageFromResults() {
            PuzzleImage puzzle = new PuzzleImage(puzzleInfo.rowCount, puzzleInfo.colCount, solveStatus == Puzzle.SOLVED,
                    System.currentTimeMillis(), "");
            if (backTracker != null) {
                puzzle.setPuzzleStatistics(totalTime, lineSolver.getTotalSolvingTime(),
                        backTracker.getBackTrackIterations(), lineSolver.getTotalLinesProcessed(), backTracker.getMaxStackLoad());
            } else
                puzzle.setPuzzleStatistics(totalTime, lineSolver.getTotalSolvingTime(),
                        0, lineSolver.getTotalLinesProcessed(), 0);
            return puzzle;
        }

        private void waitForListener() {
            if (!isCancelled()) {
                int repeat = 10;
                while (mListener == null && repeat > 0) {
                    SystemClock.sleep(500);
                    repeat--;
                }
            }
        }

        private void cleanMemory() {
            backTracker = null;
            System.gc();
        }

        private void createPuzzle() {
            puzzleInfo = new Puzzle();
            puzzleInfo.rowCount = cells[0];
            puzzleInfo.colCount = cells[1] - 1;
            createLinesAndNumberOfCellsToPaint(puzzleInfo);
            puzzleInfo.descriptions = convertDescriptionsForSolving();
            puzzleInfo.descriptionSums = createDescriptionsSums(puzzleInfo.descriptions);
            createAndFillQueueWithInQueueArray(puzzleInfo);
        }

        private void createLinesAndNumberOfCellsToPaint(Puzzle puzzle) {
            final byte[][] cells2D = PuzzleArraysController.convertCellsFromSaveFormBackToOriginal(cells);
            puzzle.lines = PuzzleArraysController.convertCellsIntoLines(cells2D);
            puzzle.numOfCellsToPaintPerLine = createNumberOfCellsToPaintArray(cells2D);
            puzzle.linesToSolve = puzzle.lines.length;
        }

        private byte[][] convertDescriptionsForSolving() {
            byte[][] vDescs = PuzzleArraysController.convertCellsFromSaveFormBackToOriginal(vDescriptions);
            byte[][] hDescs = PuzzleArraysController.convertCellsFromSaveFormBackToOriginal(hDescriptions);
            return PuzzleArraysController.convertToDescriptionsForSolving(hDescs, vDescs);
        }

        private byte[][] createDescriptionsSums(byte[][] descriptions) {
            return PuzzleArraysController.createDescriptionSums(descriptions);
        }

        private MyByteQueue createAndFillQueueWithInQueueArray(Puzzle puzzle) {
            MyByteQueue queue = new MyByteQueue(puzzle.lines.length);
            puzzle.inQueue = new boolean[puzzle.lines.length];
            for (int i = 0; i < puzzle.inQueue.length; i++) {
                queue.add((byte) i);
                puzzle.inQueue[i] = true;
            }
            return queue;
        }

        private byte[] createNumberOfCellsToPaintArray(byte[][] cells) {
            final int colCount = cells[0].length;
            final int rows = cells.length;
            final int cols = colCount - 1;
            final byte[] cellsToPaint = new byte[cells.length + cols];
            for (int r = 0; r < rows; r++) {
                cellsToPaint[r] = (byte) (cols - cells[r][0]);
                int linesCol = 0;
                for (int c = 1; c < colCount; c++, linesCol++)
                    if (cells[r][c] > 0)
                        cellsToPaint[rows + linesCol]--;
            }
            for (int c = rows; c < cellsToPaint.length; c++)
                cellsToPaint[c] += rows;
            return cellsToPaint;
        }

        @Override
        protected void onPostExecute(SolveResults results) {
            puzzleResult = results.puzzle;
            resultCells = results.getResults();
            if (mListener != null)
                mListener.onSolvingFinished(results.getStatus(), results.getResults());
        }
    }


}
