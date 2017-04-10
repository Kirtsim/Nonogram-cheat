package fm.apps.kirtsim.nonogramcheat;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cellsAndDescriptions.PuzzleCentering;
import cellsAndDescriptions.PuzzleArraysController;
import gridPainting.CellsGridPainter;
import gridPainting.DescriptionGridPainter;
import gridPainting.GridPainter;

/**
 *
 * Created by kirtsim on 16/01/2017.
 */

public class PuzzleView extends View {
//    private static final String TAG = "PuzzleView";
    public static final String H_DESCRIPTIONS_PARAM = "PuzzleView.hDescriptions";
    public static final String V_DESCRIPTIONS_PARAM = "PuzzleView.vDescriptions";
    public static final String PUZZLE_CELLS_PARAM = "PuzzleView.puzzleCells";
    private static final String NEXT_DESCRIPTOR_PARAM = "PuzzleView.nextDescriptor";
    public static final String IS_PUZ_EDITABLE_PARAM = "PuzzleView.isPuzzleEditable";
    public static final String IS_MOVE_MODE_PARAM = "PuzzleView.isMoveMode";
    private static final String CENTER_FLAG_PARAM = "centerFlag.PV";

    public static final byte PAINT_MODE_BLACK_WHITE = 0;
    public static final byte PAINT_MODE_BLACK_ONLY = 1;
    public static final byte PAINT_MODE_WHITE_ONLY = 2;
    public static final byte PAINT_MODE_ERASE = 3;

    private GestureDetector gestureListener;
    private ScaleGestureDetector scaleListener;
    private DescriptionInputChecker validator;

    private DescriptionGridPainter hDescriptions;
    private DescriptionGridPainter vDescriptions;
    private CellsGridPainter puzzleCells;

    private GridPainter gridWithSelection;
    private boolean isScrolling;
    private boolean isScaling;
    private boolean isMoveMode;
    private byte paintMode;
    private byte centerFlag;

    private int nextDescriptor;
    private boolean isPuzzleEditable;
    private final int backgroundColor = Color.parseColor("#D7CCC8");
    private final int selectionColor = Color.parseColor("#5D4037");

    public PuzzleView(Context context) {
        super(context);
        initListeners();
        isPuzzleEditable = true;
    }

    public PuzzleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initListeners();
        isPuzzleEditable = true;
    }

    public void setCenterFlag(byte flag) {
        this.centerFlag = flag;
    }

    public void initPuzzle(byte[][] hDescs, byte[][] vDescs, byte[][] pCells) {
        if (!areAllGridPaintersInitialized())
            initGridPainters();
        setDescriptionsOrCellsArrays(hDescriptions, hDescs);
        setDescriptionsOrCellsArrays(vDescriptions, vDescs);
        setDescriptionsOrCellsArrays(puzzleCells, pCells);
    }

    public void setUpDefaultPuzzle(int rows, int columns) {
        if (!areAllGridPaintersInitialized())
            initGridPainters();
        puzzleCells.setSize(rows, columns);
        hDescriptions.setSize(rows, (columns + 1) / 2);
        vDescriptions.setSize((rows + 1) / 2, columns);
    }

    private void initListeners() {
        gestureListener = new GestureDetector(PuzzleView.this.getContext(), new GestureListener(this));
        scaleListener = new ScaleGestureDetector(PuzzleView.this.getContext(), new ScaleListener(this));
        validator = new DescriptionInputChecker(this);
    }

    private boolean areAllGridPaintersInitialized() {
        return hDescriptions != null && vDescriptions != null && puzzleCells != null;
    }

    private void initGridPainters() {
        hDescriptions = new DescriptionGridPainter(DescriptionGridPainter.HORIZONTAL);
        vDescriptions = new DescriptionGridPainter(DescriptionGridPainter.VERTICAL);
        puzzleCells = new CellsGridPainter();
        puzzleCells.setFlagColor(Color.parseColor("#3E2723"));
        puzzleCells.setDisplayFlags(true, true);
        hDescriptions.setBackgroundColor(Color.parseColor("#A1887F"));
        hDescriptions.setHighlightColor(Color.parseColor("#E57373"));
        vDescriptions.setBackgroundColor(Color.parseColor("#A1887F"));
        vDescriptions.setHighlightColor(Color.parseColor("#E57373"));
    }

    private void setDescriptionsOrCellsArrays(GridPainter painter, byte[][] cells) {
        if (cells.length > 0) {
            painter.setCells(cells);
            painter.setSize(cells.length, cells[0].length - 1);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        gestureListener = null;
        scaleListener = null;
        validator = null;
    }

    public int getIncorrectDescsCount() {
        return validator.highlightedRows.size() + validator.highlightedCols.size();
    }

    public Point getRowColCount() {
        if (puzzleCells != null)
            return new Point(puzzleCells.getRowCount(), puzzleCells.getColCount());
        return new Point(1, 1);
    }

//    public boolean areAllPuzzleCellsFilled() {
//        final byte[][] cells = puzzleCells.getCells();
//        if (cells.length > 0 && cells[0].length > 0) {
//            final int colCount = cells[0].length -1;
//            for (int r = 0; r < cells.length; r++)
//                if (cells[r][0] != colCount)
//                    return false;
//            return true;
//        }
//        return false;
//    }

    public void setNextDescriptorToAdd(int value) {
        this.nextDescriptor = value;
    }

    public void setIsEditable(boolean editable) {
        this.isPuzzleEditable = editable;
    }

    public boolean isInMoveMode() {
        return isMoveMode;
    }

    public boolean switchMoveMode() {
        isMoveMode = !isMoveMode;
        return isMoveMode;
    }

    public void setPaintMode(int mode) {
        if (mode < 4 && mode > -1) {
            paintMode = (byte) mode;
        }
    }


    @SuppressWarnings("unused")
    public int getPaintMode() {
        return this.paintMode;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w > 0 && h > 0) {
            setXandYBoundsForPuzzle(w, h);
            if (oldw == 0 || oldh == 0)
                applyNewDimensionsToPuzzle(w, h);
            processPuzzleChanges();
        }
    }

    private void setXandYBoundsForPuzzle(int width, int height) {
        hDescriptions.setStartingXYCoordinates(0, 0);
        vDescriptions.setStartingXYCoordinates(0, 0);
        puzzleCells.setStartingXYCoordinates(0, 0);
        hDescriptions.setStopXYCoordinates(width, height);
        vDescriptions.setStopXYCoordinates(width, height);
        puzzleCells.setStopXYCoordinates(width, height);
    }

    private void applyNewDimensionsToPuzzle(int width, int height) {
        final int minCellSize = decideMinimumCellSize(width, height);
        final int cellSize = decideCellSizeForPuzzle(width, height, minCellSize);
        assignCellSizeToPuzzle(cellSize, minCellSize);
        centerPuzzleOnScreen(width, height);
    }

    public void processPuzzleChanges() {
        if (getWidth() > 0 && getHeight() > 0) {
            final int scrollX = -getScrollX();
            final int scrollY = -getScrollY();
            hDescriptions.recalculateForDrawing(scrollX, scrollY);
            vDescriptions.recalculateForDrawing(scrollX, scrollY);
            puzzleCells.recalculateForDrawing(scrollX, scrollY);
        }
    }

    private int decideMinimumCellSize(int width, int height) {
        if (hasRowAndColCountBeenAssigned()) {
            final int cellSizeAccordingToWidth = width / (puzzleCells.getColCount() + hDescriptions.getColCount());
            final int cellSizeAccordingToHeight = height / (puzzleCells.getRowCount() + vDescriptions.getRowCount());
            return Math.min(cellSizeAccordingToHeight, cellSizeAccordingToWidth);
        }
        return 0;
    }

    private int decideCellSizeForPuzzle(int width, int height, int minCellSize) {
        if (hasRowAndColCountBeenAssigned()) {
            int cellSizeAccordingToWidth = minCellSize;
            int cellSizeAccordingToHeight = minCellSize;
            switch(centerFlag) {
                case PuzzleCentering.FLAG_CENTER_FIT_PUZZLE_CELLS:
                    cellSizeAccordingToWidth = Math.max(minCellSize, width/puzzleCells.getColCount());
                    cellSizeAccordingToHeight = Math.max(minCellSize, height/puzzleCells.getRowCount());
                    break;
                case PuzzleCentering.FLAG_CENTER_FIT_H_DESCRIPTIONS:
                    cellSizeAccordingToWidth = Math.max(minCellSize, width/hDescriptions.getColCount());
                    cellSizeAccordingToHeight = Math.max(minCellSize, height/vDescriptions.getRowCount());
                    break;
            }
            return Math.min(cellSizeAccordingToHeight, cellSizeAccordingToWidth);
        }
        return 0;
    }

    private void assignCellSizeToPuzzle(int cellSize, int minCellSize) {
        puzzleCells.setCellSize(cellSize);
        puzzleCells.setMinimumCellSize(minCellSize);
        hDescriptions.setCellSize(cellSize);
        hDescriptions.setMinimumCellSize(minCellSize);
        vDescriptions.setCellSize(cellSize);
        vDescriptions.setMinimumCellSize(minCellSize);
    }

    private void centerPuzzleOnScreen(int width, int height) {
        final int cellSize = puzzleCells.getCellSize();
        final int xOffset = decideXOffsetOfCells(width, cellSize);
        final int yOffset = decideYOffsetOfCells(height, cellSize);
        final int hDWidth = cellSize * hDescriptions.getColCount();
        final int vDHeight = cellSize * vDescriptions.getRowCount();
        puzzleCells.setXYOffsets(xOffset, yOffset);
        hDescriptions.setXYOffsets(xOffset - hDWidth, yOffset);
        vDescriptions.setXYOffsets(xOffset, yOffset - vDHeight);
    }

    private int decideXOffsetOfCells(int viewWidth, int cellSize) {
        switch (centerFlag) {
            case PuzzleCentering.FLAG_CENTER_FIT_PUZZLE_CELLS:
                return (viewWidth - puzzleCells.getColCount() * cellSize) / 2;
            case PuzzleCentering.FLAG_CENTER_FIT_H_DESCRIPTIONS:
                final int width = hDescriptions.getColCount() * cellSize;
                return (viewWidth - width) / 2 + (width - cellSize);
            default:
                return (viewWidth - (puzzleCells.getColCount() + hDescriptions.getColCount()) * cellSize) / 2;
        }
    }

    private int decideYOffsetOfCells(int viewHeight, int cellSize) {
        switch (centerFlag) {
            case PuzzleCentering.FLAG_CENTER_FIT_PUZZLE_CELLS:
                return (viewHeight - puzzleCells.getRowCount() * cellSize) / 2;
            case PuzzleCentering.FLAG_CENTER_FIT_H_DESCRIPTIONS:
                return vDescriptions.getRowCount() + cellSize;
            default:
                return (viewHeight - (puzzleCells.getRowCount() + vDescriptions.getRowCount()) * cellSize) / 2;
        }
    }

    private boolean hasRowAndColCountBeenAssigned() {
        return puzzleCells.getColCount() > 0 && puzzleCells.getRowCount() > 0;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(backgroundColor);
        highlightDescriptionErrors();
        hDescriptions.drawToCanvas(canvas);
        vDescriptions.drawToCanvas(canvas);
        puzzleCells.drawToCanvas(canvas);
    }

    private void highlightDescriptionErrors() {
        List<Integer> lines = validator.getHighlightedRows();
        if (lines.size() > 0)
            hDescriptions.setHighlightedRows(lines);
        lines = validator.getHighlightedColumns();
        if (lines.size() > 0)
            vDescriptions.setHighLightedCols(lines);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle outState = new Bundle();
        outState.putParcelable("superState", super.onSaveInstanceState());
        saveAllCellsIntoBundle(outState);
        outState.putInt(NEXT_DESCRIPTOR_PARAM, nextDescriptor);
        outState.putBoolean(IS_PUZ_EDITABLE_PARAM, isPuzzleEditable);
        outState.putBoolean(IS_MOVE_MODE_PARAM, isMoveMode);
        outState.putByte(CENTER_FLAG_PARAM, centerFlag);
        return outState;
    }

    public void saveAllCellsIntoBundle(Bundle outState) {
        byte[] horCells = PuzzleArraysController.convertCellsToFormForSaving(hDescriptions.getCells());
        byte[] verCells = PuzzleArraysController.convertCellsToFormForSaving(vDescriptions.getCells());
        byte[] puzCells = PuzzleArraysController.convertCellsToFormForSaving(puzzleCells.getCells());
        outState.putByteArray(H_DESCRIPTIONS_PARAM, horCells);
        outState.putByteArray(V_DESCRIPTIONS_PARAM, verCells);
        outState.putByteArray(PUZZLE_CELLS_PARAM, puzCells);
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle inState = (Bundle) state;
            initializeCellsFromSavedState(inState);
            isPuzzleEditable = inState.getBoolean(IS_PUZ_EDITABLE_PARAM, false);
            nextDescriptor = inState.getInt(NEXT_DESCRIPTOR_PARAM, 0);
            isMoveMode = inState.getBoolean(IS_MOVE_MODE_PARAM, false);
            centerFlag = inState.getByte(CENTER_FLAG_PARAM, PuzzleCentering.FLAG_CENTER_PUZZLE);
            state = inState.getParcelable("superState");
        }
        super.onRestoreInstanceState(state);
    }

    private void initializeCellsFromSavedState(Bundle savedState) {
        final byte[] hDescs = savedState.getByteArray(H_DESCRIPTIONS_PARAM);
        final byte[] vDescs = savedState.getByteArray(V_DESCRIPTIONS_PARAM);
        final byte[] cells = savedState.getByteArray(PUZZLE_CELLS_PARAM);
        if (hDescs != null && vDescs != null && cells != null) {
            byte[][] hDescs2D = PuzzleArraysController.convertCellsFromSaveFormBackToOriginal(hDescs);
            byte[][] vDescs2D = PuzzleArraysController.convertCellsFromSaveFormBackToOriginal(vDescs);
            byte[][] cells2D = PuzzleArraysController.convertCellsFromSaveFormBackToOriginal(cells);
            initPuzzle(hDescs2D, vDescs2D, cells2D);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final int count = event.getPointerCount();
        if (count == 2) {
            if (isScrolling)
                dismissSelection();
            return scaleListener.onTouchEvent(event);
        } else if (!isScaling && gestureListener.onTouchEvent(event))
            return true;
        final int action = event.getAction();
        if (action == MotionEvent.ACTION_UP) {
            if (isScrolling) {
                applySelection();
                return true;
            } else if (isScaling) {
                isScaling = false;
                return true;
            }
        }
        return false;
    }

    private void onSelection(float stX, float stY, float enX, float enY) {
        if (gridWithSelection != null) {
            if (gridWithSelection.setSelectionArea(stX, stY, enX, enY, selectionColor))
                invalidate();
        }
    }

    private void dismissSelection() {
        isScrolling = false;
        if (gridWithSelection != null) {
            gridWithSelection.resetSelectionArea();
            gridWithSelection = null;
        }
    }

    private void applySelection() {
        if (gridWithSelection != null) {
            if (gridWithSelection instanceof CellsGridPainter)
                gridWithSelection.applySelectionValue(getCellValueToPaint());
            else {
                gridWithSelection.applySelectionValue(paintMode != PAINT_MODE_ERASE ? nextDescriptor : 0);
                validateSelectionInDescriptions((DescriptionGridPainter) gridWithSelection);
            }
        }
        dismissSelection();
        invalidate();
    }

    private int getCellValueToPaint() {
        switch (paintMode) {
            case PAINT_MODE_BLACK_ONLY: return CellsGridPainter.BLACK_CELL;
            case PAINT_MODE_WHITE_ONLY: return CellsGridPainter.WHITE_CELL;
            case PAINT_MODE_ERASE: return CellsGridPainter.UNKNOWN_CELL;
            default: return -1;
        }
    }

    private void validateSelectionInDescriptions(DescriptionGridPainter descriptions) {
        if (descriptions.getOrientation() == DescriptionGridPainter.HORIZONTAL) {
            Point rowRange = hDescriptions.getRowSelectionRange();
            for (int r = rowRange.x > -1 ? rowRange.x : 0; r <= rowRange.y; r++)
                validator.checkRow(r);
        } else {
            Point colRange = vDescriptions.getColSelectionRange();
            for (int c = colRange.x > -1 ? colRange.x : 1; c <= colRange.y; c++)
                validator.checkColumn(c);
        }
    }


    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
    *                    G E S T U R E   L I S T E N E R                   *
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
    private static class GestureListener extends GestureDetector.SimpleOnGestureListener {
//        private static final String TAG = "GestureListener";
        WeakReference<PuzzleView> puzzleViewRef;
        private Point touchIndexes;


        GestureListener(PuzzleView view) {
            super();
            puzzleViewRef = new WeakReference<>(view);
            touchIndexes = new Point();
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            PuzzleView v = puzzleViewRef.get();
            if (v.isPuzzleEditable && !v.isMoveMode)
                processSingleTap(e);
            return true;
        }


        private void processSingleTap(MotionEvent e) {
            PuzzleView view = puzzleViewRef.get();
            final float x = e.getX() + view.getScrollX();
            final float y = e.getY() + view.getScrollY();
            if (checkIfPainted(x, y))
                view.invalidate();
        }

        private boolean checkIfPainted(float x, float y) {
            PuzzleView v = puzzleViewRef.get();
            final int descValue = v.paintMode != PAINT_MODE_ERASE ? v.nextDescriptor : 0;
            boolean painted = v.vDescriptions.onTouch(x, y, descValue, touchIndexes);
            if (painted)
                v.validator.checkColumn(touchIndexes.y);
            else if ((painted = v.hDescriptions.onTouch(x, y, descValue, touchIndexes))) {
                v.validator.checkRow(touchIndexes.x);
            } else
                painted = v.puzzleCells.onTouch(x, y, v.getCellValueToPaint(), touchIndexes);
            return painted;
        }



        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            PuzzleView v = puzzleViewRef.get();
            final int scrollX = -v.getScrollX();
            final int scrollY = -v.getScrollY();
            if (v.isMoveMode) {
                v.puzzleCells.recalculateForDrawing(scrollX, scrollY);
                v.hDescriptions.recalculateForDrawing(scrollX, scrollY);
                v.vDescriptions.recalculateForDrawing(scrollX, scrollY);
                v.scrollBy((int) distanceX, (int) distanceY);
            } else if (v.isPuzzleEditable) {
                if (!v.isScrolling) {
                    v.isScrolling = true;
                    initSelectedGrid(e1.getX() - scrollX, e1.getY() - scrollY);
                }
                v.onSelection(e1.getX() - scrollX, e1.getY() - scrollY, e2.getX() - scrollX, e2.getY() - scrollY);
            }
            return true;
        }

        private void initSelectedGrid(float x, float y) {
            PuzzleView v = puzzleViewRef.get();
            if (v.vDescriptions.isTouched(x, y))
                v.gridWithSelection = v.vDescriptions;
            else if (v.hDescriptions.isTouched(x, y))
                v.gridWithSelection = v.hDescriptions;
            else if (v.puzzleCells.isTouched(x, y))
                v.gridWithSelection = v.puzzleCells;
        }
    }



    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
    *                    S C A L E   L I S T E N E R                       *
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
    private static class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        WeakReference<PuzzleView> puzzleViewRef;

        ScaleListener(PuzzleView view) {
            super();
            this.puzzleViewRef = new WeakReference<>(view);
        }

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            final float scaleFactor = detector.getScaleFactor();
            final PuzzleView v = puzzleViewRef.get();
            v.isScaling = true;
            if (scaleFactor < 0.991f || scaleFactor > 1.009f) {
                int newCellSize = v.puzzleCells.getCellSize() + (scaleFactor > 1.0f ? 1 : -1);
                v.puzzleCells.setCellSize(newCellSize);
                v.hDescriptions.setCellSize(newCellSize);
                v.vDescriptions.setCellSize(newCellSize);
                if (v.puzzleCells.getCellSize() == newCellSize) {
                    v.hDescriptions.alignToLeftOf(v.puzzleCells.getXOffset());
                    v.vDescriptions.alignToTopOf(v.puzzleCells.getYOffset());
                    final int scrollX = -v.getScrollX();
                    final int scrollY = -v.getScrollY();

                    v.puzzleCells.recalculateForDrawing(scrollX, scrollY);
                    v.hDescriptions.recalculateForDrawing(scrollX, scrollY);
                    v.vDescriptions.recalculateForDrawing(scrollX, scrollY);
                    v.invalidate();
                }
            }
            return true;
        }
    }

    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
    *                      I N P U T   C H E C K E R                       *
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
    private static class DescriptionInputChecker {
        private WeakReference<PuzzleView> puzzleViewRef;
        private ArrayList<Integer> highlightedRows;
        private ArrayList<Integer> highlightedCols;

        private boolean isEnabled;
        private int toFindInColumn;


        DescriptionInputChecker(PuzzleView view) {
            this.puzzleViewRef = new WeakReference<>(view);
            this.highlightedCols = new ArrayList<>(1);
            this.highlightedRows = new ArrayList<>(1);
            isEnabled = true;
        }

        List<Integer> getHighlightedRows() {
            return this.highlightedRows;
        }

        List<Integer> getHighlightedColumns() {
            return this.highlightedCols;
        }

        void checkRow(int row) {
            if (isEnabled) {
                PuzzleView v = puzzleViewRef.get();
                final byte[][] descs = v.hDescriptions.getCells();
                if (row > -1 && row < descs.length) {
                    final int sum = sumRow(descs[row]);
                    if (sum + descs[row][0] - 1 > v.puzzleCells.getColCount())
                        addToBeHiglighted(row, highlightedRows);
                    else
                        removeFromHighlighted(row, highlightedRows);
                }
            }
        }

        private int sumRow(byte[] row) {
            final int length = row.length;
            int toFind = row[0];
            int sum = 0;
            for (int i = 1; i < length && toFind > 0; i++) {
                if (row[i] != 0) {
                    sum += row[i];
                    toFind--;
                }
            }
            return sum;
        }

        void checkColumn(int col) {
            if (isEnabled) {
                final PuzzleView v = puzzleViewRef.get();
                final byte[][] descs = v.vDescriptions.getCells();
                if (descs.length != 0 && descs[0].length > col && col > 0) {
                    final int sum = sumColumn(col, descs);
                    if (sum + toFindInColumn - 1 > v.puzzleCells.getRowCount())
                        addToBeHiglighted(col, highlightedCols);
                    else
                        removeFromHighlighted(col, highlightedCols);
                }
            }
        }

        private void addToBeHiglighted(int lineIndex, List<Integer> highlighted) {
            if (highlighted.size() == 0)
                highlighted.add(lineIndex);
            else if (lineIndex < highlighted.get(0))
                highlighted.add(0, lineIndex);
            else if (lineIndex > highlighted.get(highlighted.size() -1))
                highlighted.add(lineIndex);
            else {
                int index = Collections.binarySearch(highlighted, lineIndex, Integer::compare);
                if (index < 0)
                    highlighted.add((index*-1)-1, lineIndex);
            }
        }

        private void removeFromHighlighted(int line, List<Integer> highlighted) {
            final int index = Collections.binarySearch(highlighted, line, Integer::compare);
            if (index > -1)
                highlighted.remove(index);
        }

        private int sumColumn(final int col, byte[][] descs) {
            final int cellsCount = descs.length;
            toFindInColumn = 0;
            int sum = 0;
            //noinspection ForLoopReplaceableByForEach
            for (int r = 0; r < cellsCount; r++)
                if (descs[r][col] > 0) {
                    sum += descs[r][col];
                    toFindInColumn++;
                }
            return sum;
        }
    }
}
