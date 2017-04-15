package gridPainting;

import android.util.Log;

/**
 *
 * Created by kharos on 13/04/2017.
 */
class LineVisibilityCalculator {
    private int viewStart, viewEnd;
    private int lineCount;
    private int cellSize;
    private int offset;
    private int viewScroll;
    private int lastVisibleLine;
    private int firstVisibleCoordinate;
    private int firstVisibleLine;
    private int lastVisibleCoordinate;

    private final IntConsumer firstVisibleCoordinateAssigner;
    private final IntConsumer lastVisibleCoordinateAssigner;
    private final IntConsumer firstVisibleLineAssigner;
    private final IntConsumer lastVisibleLineAssigner;
    private final int FIRST_VISIBLE_ADDITION;
    private final int LAST_VISIBLE_SUBTRACTION;

    private LineVisibilityCalculator(Builder builder, int addition, int subtraction) {
        this.firstVisibleCoordinateAssigner = builder.firstVisibleCoordinateAssigner;
        this.lastVisibleCoordinateAssigner = builder.lastVisibleCoordinateAssigner;
        this.firstVisibleLineAssigner = builder.firstVisibleLineAssigner;
        this.lastVisibleLineAssigner = builder.lastVisibleLineAssigner;
        this.FIRST_VISIBLE_ADDITION = addition;
        this.LAST_VISIBLE_SUBTRACTION = subtraction;

    }

    @FunctionalInterface
    interface IntConsumer {
        void accept(int number);
    }

    static class Builder {
        private IntConsumer firstVisibleCoordinateAssigner;
        private IntConsumer lastVisibleCoordinateAssigner;
        private IntConsumer firstVisibleLineAssigner;
        private IntConsumer lastVisibleLineAssigner;
        private boolean areRows;

        Builder firstVisibleCoordinateAssigner(IntConsumer assigner) {
            this.firstVisibleCoordinateAssigner = assigner;
            return this;
        }

        Builder lastVisibleCoordinateAssigner(IntConsumer assigner) {
            this.lastVisibleCoordinateAssigner = assigner;
            return this;
        }

        Builder firstVisibleLineAssigner(IntConsumer assigner) {
            this.firstVisibleLineAssigner = assigner;
            return this;
        }

        Builder lastVisibleLineAssigner(IntConsumer assigner) {
            this.lastVisibleLineAssigner = assigner;
            return this;
        }

        Builder linesAreRows(boolean areRows) {
            this.areRows = areRows;
            return this;
        }

        LineVisibilityCalculator build() {
            final int addition = areRows ? 0 : 1;
            final int subtraction = areRows ? 1 : 0;
            return new LineVisibilityCalculator(this, addition, subtraction);
        }
    }

    void cellSize(int cellSize) {
        this.cellSize = cellSize;
    }

    void offset(int offset) {
        this.offset = offset;
    }

    void viewScroll(int viewScroll) {
        this.viewScroll = viewScroll;
    }

    void viewBounds(int viewStart, int viewEnd) {
        this.viewStart = viewStart;
        this.viewEnd = viewEnd;
    }

    void lineCount(int lineCount) {
        this.lineCount = lineCount;
    }

    void computeAndUpdate() {
        evaluateFirsts();
        evaluateLasts();
        updateResults();
    }

    private void evaluateFirsts() {
        final int relOffset = offset + viewScroll;
        if (relOffset < viewStart) {
            firstVisibleCoordinate = offset + ((viewStart - relOffset) / cellSize) * cellSize;
            firstVisibleLine = (firstVisibleCoordinate - offset) / cellSize + FIRST_VISIBLE_ADDITION;
        } else {
            firstVisibleCoordinate = offset;
            firstVisibleLine = FIRST_VISIBLE_ADDITION;
        }
    }

    private void evaluateLasts() {
        final int gridEnd = offset + (lineCount * cellSize);
        final int relGridEnd = gridEnd + viewScroll;
        if (relGridEnd > viewEnd) {
            lastVisibleCoordinate = gridEnd - (((relGridEnd - viewEnd) / cellSize) * cellSize);
            lastVisibleLine = (lastVisibleCoordinate - offset) / cellSize - LAST_VISIBLE_SUBTRACTION;
        } else {
            lastVisibleCoordinate = gridEnd;
            lastVisibleLine = lineCount - LAST_VISIBLE_SUBTRACTION;
        }
    }

    private void updateResults() {
        firstVisibleLineAssigner.accept(firstVisibleLine);
        lastVisibleLineAssigner.accept(lastVisibleLine);
        firstVisibleCoordinateAssigner.accept(firstVisibleCoordinate);
        lastVisibleCoordinateAssigner.accept(lastVisibleCoordinate);
    }


}
