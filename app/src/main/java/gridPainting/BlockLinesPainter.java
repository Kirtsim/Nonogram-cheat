package gridPainting;

import android.graphics.Canvas;

/**
 *
 * Created by kharos on 12/04/2017.
 */
class BlockLinesPainter {
    private Canvas canvas;
    private int blockSize;
    private int currentPosition;
    private int upperBound;
    private boolean includeFlags;
    private int flagNumber;
    private int flagPosition;
    private int lastLinePosition;


    @FunctionalInterface
    interface FlagPaintConsumer {
        void accept(int flag, int coordinate, int flagPosition);
    }

    @FunctionalInterface
    interface LinePaintConsumer {
        void accept(Canvas canvas, int currentPosition);
    }

    BlockLinesPainter() {
    }

    BlockLinesPainter drawTo(Canvas canvas) {
        this.canvas = canvas;
        return this;
    }

    BlockLinesPainter startAt(int firstLinePosition) {
        currentPosition = firstLinePosition;
        return this;
    }

    BlockLinesPainter dontPaintBeyond(int lastVisibleLinePosition) {
        upperBound = lastVisibleLinePosition;
        return this;
    }

    BlockLinesPainter flagValueStartsWith(int startingFlagValue) {
        flagNumber = startingFlagValue;
        return this;
    }

    BlockLinesPainter positionFlagAt(int flagPosition) {
        this.flagPosition = flagPosition;
        return this;
    }

    BlockLinesPainter includeFlags(boolean include) {
        includeFlags = include;
        return this;
    }

    BlockLinesPainter drawLastLineAt(int lastLinePosition) {
        this.lastLinePosition = lastLinePosition;
        return this;
    }

    BlockLinesPainter blockSize(int blockSize) {
        this.blockSize = blockSize;
        return this;
    }

    void paint(LinePaintConsumer lineDrawing, FlagPaintConsumer flagDrawing) {
        while (currentPosition < upperBound) {
            lineDrawing.accept(canvas, currentPosition);
            if (includeFlags)
                flagDrawing.accept(flagNumber, currentPosition, flagPosition);
            currentPosition += blockSize;
            flagNumber += 5;
        }
        lineDrawing.accept(canvas, lastLinePosition);
    }
}
