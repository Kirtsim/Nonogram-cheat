package gridPainting;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;


import java.util.List;

/**
 *
 * Created by kirtsim on 20/01/2017.
 */

public abstract class GridPainter {
//    private static final String TAG = "GridPainter";

    Paint paint;
    private Paint flagPaint;

    private int xViewStart, yViewStart, xViewEnd, yViewEnd;
    private int xViewScroll, yViewScroll;
    private int xOffset, yOffset;
    private int backgroundColor;
    private static final int LINE_COLOR = Color.parseColor("#3E2723");
    private static final int FLAG_COLOR = Color.parseColor("#D7CCC8");
    private boolean includeFlags;
    private int cellSizeMIN;
    int cellSize;

    private int rowCount, colCount;

    private int xLastColToDraw,  yLastRowToDraw;
    private int xFirstColToDraw, yFirstRowToDraw;
    private int firstVisCol, lastVisCol;
    private int firstVisRow, lastVisRow;
    private int highlightColor;
    private List<Integer> highLightedRows;
    private List<Integer> highLightedCols;

    private int selectionColor;
    private Point selectionCellTopLeft, selectionCellBottomRight;
    private BlockLinesPainter blockLinesPainter;
    private LineVisibilityCalculator rowVisibilityEvaluator;
    private LineVisibilityCalculator colVisibilityEvaluator;
    byte[][] cells;

    private final BlockLinesPainter.LinePaintConsumer columnLinesDrawing = (c, x) ->
            c.drawLine(x, yFirstRowToDraw, x, yLastRowToDraw, paint);
    private final BlockLinesPainter.LinePaintConsumer rowLinesDrawing = (c, y) ->
            c.drawLine(xFirstColToDraw, y, xLastColToDraw, y, paint);



    GridPainter() {
        defaultInit();
    }

    private void defaultInit() {
        initializeMainPaint();
        initializePaintForFlags();
        backgroundColor = Color.parseColor("#D7CCC8");
        initializeLineVisibilityEvaluators();
        blockLinesPainter = new BlockLinesPainter();
        selectionCellBottomRight = new Point(-1, -1);
        selectionCellTopLeft = new Point(-1, -1);
        cells = new byte[0][0];
    }

    private void initializeMainPaint() {
        paint = new Paint();
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(5);
        paint.setColor(Color.BLACK);
    }

    private void initializePaintForFlags() {
        flagPaint = new Paint();
        flagPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        flagPaint.setStrokeCap(Paint.Cap.ROUND);
        flagPaint.setStrokeWidth(5);
        flagPaint.setColor(FLAG_COLOR);
        flagPaint = new Paint();
    }

    private void initializeLineVisibilityEvaluators() {
        LineVisibilityCalculator.Builder builder = new LineVisibilityCalculator.Builder();
        colVisibilityEvaluator = builder.linesAreRows(false)
                .firstVisibleCoordinateAssigner(x -> xFirstColToDraw = x)
                .lastVisibleCoordinateAssigner(x -> xLastColToDraw = x)
                .firstVisibleLineAssigner(line -> firstVisCol = line)
                .lastVisibleLineAssigner(line -> lastVisCol = line)
                .build();
        rowVisibilityEvaluator = builder.linesAreRows(true)
                .firstVisibleCoordinateAssigner(y -> yFirstRowToDraw = y)
                .lastVisibleCoordinateAssigner(y -> yLastRowToDraw = y)
                .firstVisibleLineAssigner(line -> firstVisRow = line)
                .lastVisibleLineAssigner(line -> lastVisRow = line)
                .build();
    }

    public byte[][] getCells() {
        return this.cells;
    }

    public void setCells(byte[][] cells) {
        this.cells = cells;
    }

    public int getColCount() {
        return this.colCount;
    }

    public int getRowCount() {
        return this.rowCount;
    }

    public int getXOffset() {
        return xOffset;
    }

    public int getYOffset() {
        return this.yOffset;
    }

    public int getCellSize() {
        return this.cellSize;
    }

    @SuppressWarnings("SuspiciousNameCombination")
    public Point getRowSelectionRange() {
        return new Point(selectionCellTopLeft.x, selectionCellBottomRight.x);
    }

    @SuppressWarnings("SuspiciousNameCombination")
    public Point getColSelectionRange() {
        return new Point(selectionCellTopLeft.y, selectionCellBottomRight.y);
    }

    public void setMinimumCellSize(int cellSize) {
        cellSizeMIN = cellSize;
    }

    public void setCellSize(int cellSize) {
        final int old = this.cellSize;
        cellSize = Math.max(cellSize, cellSizeMIN);
        this.cellSize = Math.min(cellSize, (Math.min(xViewEnd - xViewStart, yViewEnd - yViewStart) / 5));
        processCellSizeChange(old - this.cellSize);
        adjustNewLineWidth();
        flagPaint.setTextSize(this.cellSize * 0.7f);
    }

    protected void processCellSizeChange(int change) {
        this.xOffset += (change * colCount) / 2;
        this.yOffset += (change * rowCount) / 2;
    }

    private void adjustNewLineWidth() {
        paint.setStrokeWidth(Math.max(cellSize * 0.05f, 4f));
    }

    public void alignToLeftOf(int x) {
        this.xOffset = x - (colCount * cellSize);
    }

    public void alignToTopOf(int y) {
        this.yOffset = y - (rowCount * cellSize);
    }

    public void setStartingXYCoordinates(int xStart, int yStart) {
        this.xViewStart = xStart;
        this.yViewStart = yStart;
    }

    public void setStopXYCoordinates(int xStop, int yStop) {
        this.xViewEnd = xStop;
        this.yViewEnd = yStop;
    }

    public void setXYOffsets(int xOffset, int yOffset) {
        this.xOffset = xOffset;
        this.yOffset = yOffset;
    }

    public void setBackgroundColor(int color) {
        this.backgroundColor = color;
    }

    public void setFlagColor(int color) {
        this.flagPaint.setColor(color);
    }

    public void setDisplayFlags(boolean display) {
        this.includeFlags = display;
    }

    public void setSize(int rows, int cols) {
        this.rowCount = rows;
        this.colCount = cols;
    }

    public void setHighlightColor(int color) {
        this.highlightColor = color;
    }

    public void setHighlightedRows(List<Integer> rows) {
        this.highLightedRows = rows;
    }

    public void setHighLightedCols(List<Integer> cols) {
        this.highLightedCols = cols;
    }

    public void resetSelectionArea() {
        selectionCellBottomRight.set(-1, -1);
        selectionCellTopLeft.set(-1, -1);
    }

    public boolean setSelectionArea(float x1, float y1, float x2, float y2, int color) {
        this.selectionColor = color;
        calculateRowColumn(Math.min(x1, x2), Math.min(y1, y2), selectionCellTopLeft);
        calculateRowColumn(Math.max(x1, x2), Math.max(y1, y2), selectionCellBottomRight);
        if (!areSelectionCellsValid()) {
            resetSelectionArea();
            return false;
        }
        return true;
    }

    private boolean areSelectionCellsValid() {
        return selectionCellBottomRight.x < rowCount && selectionCellBottomRight.x > -1 &&
                selectionCellBottomRight.y <= colCount && selectionCellBottomRight.y > 0 &&
                selectionCellTopLeft.x < rowCount && selectionCellTopLeft.x > -1 &&
                selectionCellTopLeft.y <= colCount && selectionCellTopLeft.y > 0;
    }

    public void drawToCanvas(Canvas canvas) {
        if (cells.length > 0 && cellSize > 0) {
            drawBackground(canvas);
            highlightLines(canvas);
            paint.setColor(LINE_COLOR);
            drawLines(canvas, yFirstRowToDraw, yLastRowToDraw, rowLinesDrawing);
            drawLines(canvas, xFirstColToDraw, xLastColToDraw, columnLinesDrawing);
            paintCells(canvas);
            paintBlockLines(canvas);
            drawSelectionArea(canvas);
        }
    }

    private void highlightLines(Canvas canvas) {
        paint.setColor(highlightColor);
        if (highLightedCols != null && highLightedCols.size() > 0) {
            final int count = highLightedCols.size();
            for (int i = 0; i < count; i++)
                highlightColumn(highLightedCols.get(i), canvas);
        }
        if (highLightedRows != null && highLightedRows.size() > 0) {
            final int count = highLightedRows.size();
            for (int i = 0; i < count; i++)
                highlightRow(highLightedRows.get(i), canvas);
        }
    }

    private void drawSelectionArea(Canvas canvas) {
        if (selectionCellTopLeft.x != -1) {
            final int left  = xOffset + cellSize * (selectionCellTopLeft.y - 1);
            final int top   = yOffset + cellSize * selectionCellTopLeft.x;
            final int right = xOffset + cellSize * selectionCellBottomRight.y;
            final int bottom = yOffset + cellSize * (selectionCellBottomRight.x + 1);
            paint.setColor(selectionColor);
            paint.setAlpha(150);
            canvas.drawRect(left, top, right, bottom, paint);
        }
    }

    private void highlightRow(int row, Canvas canvas) {
        if (row >= firstVisRow && row <= lastVisRow) {
            final int topY = (row - firstVisRow) * cellSize + yFirstRowToDraw;
            canvas.drawRect(xFirstColToDraw, topY, xLastColToDraw, topY + cellSize, paint);
        }
    }

    private void highlightColumn(int index, Canvas canvas) {
        if (index >= firstVisCol && index <= lastVisCol) {
            final int leftX = (index - firstVisCol) * cellSize + xFirstColToDraw;
            canvas.drawRect(leftX, yFirstRowToDraw, leftX + cellSize, yLastRowToDraw, paint);
        }
    }

    public void recalculateForDrawing(int scrollX, int scrollY) {
        if (cellSize > 0) {
            xViewScroll = scrollX;
            yViewScroll = scrollY;
            assignVisibilitiesOfLines();
            if (hasCellsMatrixSizeChanged())
                resizeCellMatrix();
        }
    }

    private void assignVisibilitiesOfLines() {
        rowVisibilityEvaluator.offset(yOffset);
        rowVisibilityEvaluator.viewScroll(yViewScroll);
        rowVisibilityEvaluator.viewBounds(yViewStart, yViewEnd);
        rowVisibilityEvaluator.lineCount(rowCount);
        rowVisibilityEvaluator.cellSize(cellSize);
        rowVisibilityEvaluator.computeAndUpdate();

        colVisibilityEvaluator.offset(xOffset);
        colVisibilityEvaluator.viewScroll(xViewScroll);
        colVisibilityEvaluator.viewBounds(xViewStart, xViewEnd);
        colVisibilityEvaluator.lineCount(colCount);
        colVisibilityEvaluator.cellSize(cellSize);
        colVisibilityEvaluator.computeAndUpdate();
    }

    private boolean hasCellsMatrixSizeChanged() {
        if (cells.length > 0)
            return (rowCount != cells.length || colCount + 1 != cells[0].length);
        else if (rowCount > 0 && colCount > 0)
            return true;
        return false;
    }

    private void resizeCellMatrix() {
        final byte[][] original = cells;
        cells = new byte[rowCount][colCount + 1];

        final int rowCycles = Math.min(original.length, cells.length);
        final int colStartOld = rowCycles == 0 ? 0 : original[0].length - 1;
        final int colStop = rowCycles == 0 ? 0: chooseStopColumnForResizing(original);

        for (int r = 0; r < rowCycles; r++) {
            int cellsToFind = original[r][0];
            int colOldIndex = colStartOld;
            for (int c = cells[0].length - 1; c >= colStop && cellsToFind > 0; c--) {
                final byte cell = original[r][colOldIndex];
                if (considerCellValueWhenResizingCellMatrix(r, c, cell)) {
                    cells[r][c] = cell;
                    cellsToFind--;
                }
                colOldIndex--;
            }
            cells[r][0] = (byte) (original[r][0] - cellsToFind);
        }
    }

    private void drawBackground(Canvas canvas) {
        paint.setColor(backgroundColor);
        paint.setAlpha(255);
        canvas.drawRect(xFirstColToDraw, yFirstRowToDraw, xLastColToDraw, yLastRowToDraw, paint);
    }

    private void drawLines(final Canvas canvas, int startPosition, final int upperBound,
                           BlockLinesPainter.LinePaintConsumer lineDrawing) {
        int currentPosition = startPosition;
        while (currentPosition <= upperBound) {
            lineDrawing.accept(canvas, currentPosition);
            currentPosition += cellSize;
        }
    }

    private int chooseStopColumnForResizing(final byte[][] originalDescs) {
        if (cells[0].length > originalDescs[0].length)
            return cells[0].length - originalDescs[0].length;
        return 1;
    }

    protected abstract boolean considerCellValueWhenResizingCellMatrix(int row, int col, int value);

    private void paintCells(Canvas canvas) {
        paint.setColor(LINE_COLOR);
        int yTop = yFirstRowToDraw;
        for (int r = firstVisRow; r <= lastVisRow; r++) {
            int xLeft = xFirstColToDraw;
            for (int c = firstVisCol; c <= lastVisCol; c++) {
                paintCell(canvas, xLeft, yTop, r, c);
                xLeft += cellSize;
            }
            yTop += cellSize;
        }
    }

    private void paintBlockLines(Canvas canvas) {
        paint.setColor(LINE_COLOR);
        final float originalStrokeW = paint.getStrokeWidth();
        paint.setStrokeWidth(originalStrokeW + (originalStrokeW * 0.7f));
        adjustBlockLinesPainterBeforePainting(canvas);
        paintColumnBlockLines(canvas, getFirstVisibleBlockStart(xOffset, xFirstColToDraw));
        paintRowBlockLines(canvas, getFirstVisibleBlockStart(yOffset, yFirstRowToDraw));
        paint.setStrokeWidth(originalStrokeW);
    }

    private void adjustBlockLinesPainterBeforePainting(Canvas canvas) {
        blockLinesPainter.drawTo(canvas)
                .blockSize(5 * cellSize)
                .includeFlags(includeFlags);
    }

    private void paintColumnBlockLines(Canvas canvas, final int startX) {
        blockLinesPainter.startAt(startX)
                .dontPaintBeyond(xLastColToDraw)
                .drawLastLineAt(xOffset + (colCount * cellSize))
                .flagValueStartsWith((startX - xOffset) / cellSize)
                .positionFlagAt(Math.min(yViewEnd - yViewScroll, yOffset + rowCount * cellSize))
                .paint(columnLinesDrawing, (flag, x, fPos) -> canvas.drawText(String.valueOf(flag), x, fPos, flagPaint));
    }

    private void paintRowBlockLines(Canvas canvas, final int startY) {
        blockLinesPainter.startAt(startY)
                .dontPaintBeyond(yLastRowToDraw)
                .drawLastLineAt(yOffset + (rowCount * cellSize))
                .flagValueStartsWith((startY - yOffset) / cellSize)
                .positionFlagAt(Math.min(xViewEnd - xViewScroll, xOffset + colCount * cellSize) - (int)flagPaint.getTextSize())
                .paint(rowLinesDrawing, (flag, y, fPos) -> canvas.drawText(String.valueOf(flag), fPos, y, flagPaint));
    }


    private int getFirstVisibleBlockStart(int offset, int firstVisible) {
        if (offset < firstVisible) {
            final int blockLength = cellSize * 5;
            final int distance = firstVisible - offset;
            if (distance % blockLength > 0)
                return offset + (((distance / blockLength) + 1) * blockLength); // xOffset + distance + blockLength;
        }
        return firstVisible;
    }

    public boolean onTouch(float x, float y, final int value, Point coords) {
        calculateRowColumn(x, y, coords);
        return areIndexesWithinBounds(coords.x, coords.y) && onValueChangeAt(coords.x, coords.y, value);
    }

    public boolean isTouched(float x, float y) {
        return areCoordinatesWithingDrawnArea(x, y);
    }

    public void applySelectionValue(int value) {
        if (selectionCellTopLeft.x == -1)
            return;
        for (int r = selectionCellTopLeft.x; r <= selectionCellBottomRight.x; r++) {
            for (int c = selectionCellTopLeft.y; c <= selectionCellBottomRight.y; c++)
                onValueChangeAt(r, c, value);
        }
    }

    protected abstract void paintCell(Canvas canvas, int xLeft, int yTop, int row, int col);
    protected abstract boolean onValueChangeAt(int row, int col, int value);

    private Point calculateRowColumn(float x, float y, Point toReturn) {
        toReturn.x = toReturn.y = -1;
        if (areCoordinatesWithingDrawnArea(x, y)) {
            int columnsRel = (int) Math.ceil((x - xFirstColToDraw) / cellSize);
            int rowsRel = (int) Math.ceil((y - yFirstRowToDraw) / cellSize);
            toReturn.y = columnsRel + ((xFirstColToDraw - xOffset) / cellSize);
            toReturn.x = rowsRel + ((yFirstRowToDraw - yOffset) / cellSize) - 1;
        }
        return toReturn;
    }

    private boolean areCoordinatesWithingDrawnArea(float x, float y) {
        return x > xFirstColToDraw  &&
                x < xLastColToDraw  &&
                y > yFirstRowToDraw && y < yLastRowToDraw;
    }

    private boolean areIndexesWithinBounds(int row, int col) {
        return (row < cells.length && row > -1) && (col < cells[0].length && col > -1);
    }
}
