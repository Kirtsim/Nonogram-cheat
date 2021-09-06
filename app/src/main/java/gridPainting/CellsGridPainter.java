package gridPainting;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

/**
 *
 * Created by kirtsim on 31/01/2017.
 */

public class CellsGridPainter extends GridPainter {
    public static final int BLACK_CELL = 2;
    public static final int WHITE_CELL = 1;
    public static final int UNKNOWN_CELL = 0;
    private static final int PAINTED_COLOR = Color.parseColor("#5D4037");
    private static final int UNPAINTED_COLOR = Color.WHITE;

    private Paint cellPaint;

    public CellsGridPainter() {
        super();
        cellPaint = new Paint();
        cellPaint.setStyle(Paint.Style.FILL);
        cellPaint.setColor(BLACK_CELL);
        cellPaint.setAlpha(255);
    }


    @Override
    protected boolean considerCellValueWhenResizingCellMatrix(int row, int col, int value) {
        return value == WHITE_CELL || value == BLACK_CELL;
    }

    @Override
    protected void paintCell(Canvas canvas, int xLeft, int yTop, int row, int col) {
        final float strokeWidth = paint.getStrokeWidth();
        final float cellMinusStroke = cellSize - strokeWidth;
        setColorToPaint(cells[row][col]);
        canvas.drawRect(xLeft + strokeWidth, yTop + strokeWidth,
                xLeft + cellMinusStroke, yTop + cellMinusStroke, cellPaint);
    }

    private void setColorToPaint(int cellValue) {
        switch(cellValue) {
            case BLACK_CELL:
                cellPaint.setAlpha(0);
                cellPaint.setColor(PAINTED_COLOR);
                break;
            case WHITE_CELL:
                cellPaint.setAlpha(0);
                cellPaint.setColor(UNPAINTED_COLOR);
                break;
            default:
                cellPaint.setAlpha(0);
        }
    }

    @Override
    protected boolean onValueChangeAt(int row, int col, int value) {
        switch (value) {
            case BLACK_CELL: case WHITE_CELL: case UNKNOWN_CELL:
                return processKnownValueChange(row, col, value);
            default:
                processUnknownValue(row, col);
        }
        return true;
    }

    private boolean processKnownValueChange(int row, int col, int value) {
        final byte oldValue = cells[row][col];
        if (value == UNKNOWN_CELL || value == oldValue) {
            cells[row][col] = UNKNOWN_CELL;
            if (oldValue == UNKNOWN_CELL)
                return false;
            cells[row][0]--;
        } else {
            if (oldValue == UNKNOWN_CELL)
                cells[row][0]++;
            cells[row][col] = (byte) value;
        }
        return true;
    }

    private void processUnknownValue(int row, int col) {
        switch (cells[row][col]) {
            case UNKNOWN_CELL:
                cells[row][0]++;
                cells[row][col] = BLACK_CELL; break;
            case BLACK_CELL:
                cells[row][col] = WHITE_CELL; break;
            case WHITE_CELL:
                cells[row][0]--;
                cells[row][col] = UNKNOWN_CELL; break;
            default:
                cells[row][col] = UNKNOWN_CELL;
        }
    }
}
