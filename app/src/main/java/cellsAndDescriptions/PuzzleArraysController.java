package cellsAndDescriptions;


/**
 *
 * Created by kirtsim on 13/02/2017.
 */

public class PuzzleArraysController {

    public static byte[][] convertToDescriptionsForSolving(byte[][] horDescs,
                                                           byte[][] vertDescs) {
        byte[][] newDescs = new byte[horDescs.length + vertDescs[0].length - 1][];
        settleHorizontalDescriptions(horDescs);
        settleVerticalDescriptions(vertDescs);
        addHorizontalsToTheNewDescriptions(newDescs, horDescs);
        addVerticalsToTheNewDescriptions(newDescs, vertDescs, horDescs.length);
        return newDescs;
    }

    @SuppressWarnings("WeakerAccess")
    public static void settleHorizontalDescriptions(byte[][] descriptions) {
        final int rowCount = descriptions.length;
        final int colCount = rowCount == 0 ? 0 : descriptions[0].length;
        for (int r = 0; r < rowCount; r++) {
            int toFind = descriptions[r][0];
            int nextIndex = colCount - 1;
            for (int c = nextIndex; c > 0 && toFind > 0; c--) {
                if (descriptions[r][c] != 0) {
                    if (c < nextIndex) {
                        descriptions[r][nextIndex] = descriptions[r][c];
                        descriptions[r][c] = 0;
                    }
                    toFind--;
                    nextIndex--;
                }
            }
        }
    }

    @SuppressWarnings("WeakerAccess")
    public static void settleVerticalDescriptions(byte[][] descriptions) {
        final int rowCount = descriptions.length;
        final int colCount = rowCount == 0 ? 0: descriptions[0].length;
        for (int c = 1; c < colCount; c++) {
            int nextIndex = rowCount - 1;
            for (int r = rowCount - 1; r > -1; r--) {
                if (descriptions[r][c] != 0) {
                    if (r < nextIndex) {
                        descriptions[nextIndex][c] = descriptions[r][c];
                        descriptions[r][c] = 0;
                    }
                    nextIndex--;
                }
            }
        }
    }

    private static void addHorizontalsToTheNewDescriptions(byte[][] newDescs,
                                                           byte[][] horDescs) {
        final int hRowCount = horDescs.length;
        final int hColCount = horDescs[0].length;
        for (int r = 0; r < hRowCount; r++) {
            final int toFind = horDescs[r][0];
            int nextCol = toFind -1;
            newDescs[r] = new byte[toFind];
            final int endCol = hColCount - toFind;
            for (int c = hColCount -1; c >= endCol; c--) {
                newDescs[r][nextCol] = horDescs[r][c];
                nextCol--;
            }
        }
    }

    private static void addVerticalsToTheNewDescriptions(byte[][] newDescs,
                                                         byte[][] verDescs, int fromRow) {
        final int rowCount = verDescs.length;
        final int colCount = verDescs[0].length;
        for (int c = 1; c < colCount; c++) {
            final int totalDescs = getNumOfDescsInColumn(verDescs, c);
            newDescs[fromRow] = new byte[totalDescs];
            int column = totalDescs -1;
            final int endRow = rowCount - totalDescs;
            for (int r = rowCount - 1; r >= endRow; r--) {
                newDescs[fromRow][column] = verDescs[r][c];
                column--;
            }
            fromRow++;
        }
    }

    private static int getNumOfDescsInColumn(byte[][] descs, int col) {
        int startRow = descs.length -1;
        int sum = 0;
        while(startRow > -1 && descs[startRow][col] > 0) {
            sum++;
            startRow--;
        }
        return sum;
    }

    public static byte[][] createDescriptionSums(byte[][] descriptions) {
        final byte[][] descSums = new byte[descriptions.length][];
        for (int r = 0; r < descriptions.length; r++) {
            final int cols = descriptions[r].length;
            descSums[r] = new byte[cols];
            if (cols > 0)
                descSums[r][0] = descriptions[r][0];
            for (int c = 1; c < cols; c++)
                descSums[r][c] = (byte) (descSums[r][c-1] + descriptions[r][c] + 1);
        }
        return descSums;
    }

    @SuppressWarnings("ForLoopReplaceableByForEach")
    public static byte[] convertCellsToFormForSaving(byte[][] cells) {
        final int rows = cells.length;
        final int cols = rows > 0 ? cells[0].length : 0;
        byte[] savable = new byte[rows * cols + 2];
        savable[0] = (byte)rows; savable[1] = (byte)cols;
        if (rows > 0 && cols > 0) {
            int savableIndex = 2;
            for (int r = 0; r < rows; r++) {
                int toFind = cells[r][0];
                int c = 1;
                byte cell;
                savable[savableIndex++] = (byte)toFind;
                for (; c < cols && toFind > 0; c++, savableIndex++) {
                    cell = cells[r][c];
                    if (cell > 0) {
                        savable[savableIndex] = cell;
                        toFind--;
                    }
                }
                savableIndex += cols - c;
            }
        }
        return savable;
    }


    public static byte[][] convertCellsFromSaveFormBackToOriginal(byte[] saved) {
        if (saved.length < 2)
            return new byte[][] {{}};
        final int rows = saved[0];
        final int cols = saved[1];
        final byte[][] cells = new byte[rows][cols];
        if (rows > 0 && cols > 0) {
            int savedIndex = 2;
            byte currentDesc;
            for (int r = 0; r < rows; r++) {
                int toFind = saved[savedIndex++];
                cells[r][0] = (byte)toFind;
                int c = 1;
                for (; c < cols && toFind > 0; c++, savedIndex++) {
                    currentDesc = saved[savedIndex];
                    if (currentDesc > 0) {
                        cells[r][c] = currentDesc;
                        toFind--;
                    }
                }
                savedIndex += cols - c;
            }
        }
        return cells;
    }

    private static final long LINE_BASE    = 0x4000000000000000L;
    private static final long BLACK_CELL   = 0x2000000000000000L;
    private static final long WHITE_CELL   = 0x1000000000000000L;
    private static final long UNKNOWN_CELL = 0x3000000000000000L;

    public static long[] convertCellsIntoLines(byte[][] cells) {
        if (!areCellsValid(cells))
            return new long[] {LINE_BASE};
        final int rows = cells.length;
        final int cols = cells[0].length;
        long[] lines = createDefaultLines(rows + cols - 1);
        for (int r = 0; r < rows; r++) {
            int lineCol = 0;
            for (int cellCol = 1; cellCol < cols; cellCol++, lineCol++) {
                long cellColour = UNKNOWN_CELL;
                switch (cells[r][cellCol]) {
                    case 1: cellColour = WHITE_CELL; break;
                    case 2: cellColour = BLACK_CELL;
                }
                lines[r] |= (cellColour >>> 2*lineCol);
                lines[rows + lineCol] |= (cellColour >>> 2*r);
            }
        }
        return lines;
    }

    private static boolean areCellsValid(byte[][] array) {
        return array != null && array.length > 0 && array[0].length > 0;
    }

    private static long[] createDefaultLines(int count) {
        long[] lines = new long[count];
        for (int i = 0; i < count; i++)
            lines[i] = LINE_BASE;
        return lines;
    }

    public static byte[] convertLinesIntoCells(long[] lines, int rows) {
        final int cols = lines.length - rows;
        final byte[] cells = new byte[rows * (cols + 1) + 2];
        cells[0] = (byte)rows;
        cells[1] = (byte)(cols + 1);
        int cellIndex = 2;
        int lineIndex = 0;
        while (lineIndex < rows) {
            int paintedFound = 0;
            final int indexForFound = cellIndex++;
            long line = lines[lineIndex++] << 2;
            for (int i = 0; i < cols; i++, line <<= 2) {
                long cell = line & 0xC000000000000000L;
                if (cell == 0x8000000000000000L) {
                    cells[cellIndex] = 2;
                    paintedFound++;
                } else if (cell == 0x4000000000000000L) {
                    cells[cellIndex] = 1;
                    paintedFound++;
                } else
                    cells[cellIndex] = 0;
                cellIndex++;
            }
            cells[indexForFound] = (byte)paintedFound;
        }
        return cells;
    }

}
