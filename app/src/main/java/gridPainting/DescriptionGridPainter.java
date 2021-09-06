package gridPainting;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

/**
 *
 * Created by kirtsim on 22/01/2017.
 */

public class DescriptionGridPainter extends GridPainter {
    public static final int HORIZONTAL = 0;
    public static final int VERTICAL = 1;


    private int orientation;
    private Rect rect;

    public DescriptionGridPainter(int orientation) {
        super();
        if (orientation > 1 || orientation < 0)
            this.orientation = HORIZONTAL;
        this.orientation = orientation;
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setTextSize(35);
        rect = new Rect();
    }

    @Override
    protected boolean onValueChangeAt(int row, int col, int value) {
        final byte oldValue = cells[row][col];
        if (value < 1 || value == oldValue) {
            cells[row][col] = 0;
            if (oldValue == 0)
                return false;
            cells[row][0]--;
        } else {
            if (oldValue == 0)
                cells[row][0]++;
            cells[row][col] = (byte) value;
        }
        return true;
    }

    public int getOrientation() {
        return this.orientation;
    }

    @Override
    protected void processCellSizeChange(int change) {
        super.processCellSizeChange(change);
        paint.setTextSize(cellSize * 0.7f);
    }

    @Override
    protected boolean considerCellValueWhenResizingCellMatrix(int row, int col, int value) {
        return value > 0;
    }

    @Override
    protected void paintCell(Canvas canvas, int xLeft, int yTop, int row, int col) {
        final int value = cells[row][col];
        if (value > 0) {
            final int cellHalve = cellSize / 2;
            final String descriptor = String.valueOf(value);
            paint.getTextBounds(descriptor, 0, descriptor.length(), rect);
            final float xpos = xLeft + cellHalve - rect.width() / 2f - rect.left;
            final float ypos = yTop + cellHalve + rect.height() /2f - rect.bottom;
            canvas.drawText(descriptor, xpos, ypos, paint);
        }
    }
}

