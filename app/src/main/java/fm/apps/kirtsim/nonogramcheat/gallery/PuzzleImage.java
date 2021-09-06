package fm.apps.kirtsim.nonogramcheat.gallery;

import android.os.Parcel;
import android.os.Parcelable;

/**
 *
 * Created by kirtsim on 27/02/2017.
 */

public class PuzzleImage implements Parcelable{
    private int id;
    private int rows;
    private int cols;
    private boolean solved;

    private long totalSolveTime;
    private long lineSolvingTime;
    private int  backTrackIterations;
    private long linesProcessed;
    private int  maxSolvingStackLoad;

    private long saveDate;
    private String storeLocation;

    public PuzzleImage() {
        storeLocation = "";
    }

    public PuzzleImage(int rows, int cols, boolean solved, long saveDate, String saveLoc) {
        this.rows = rows;
        this.cols = cols;
        this.solved = solved;
        this.saveDate = saveDate;
        this.storeLocation = saveLoc;
    }

    public PuzzleImage(int id, int rows, int cols, boolean solved, long saveDate, String saveLoc) {
        this.id = id;
        this.rows = rows;
        this.cols = cols;
        this.solved = solved;
        this.saveDate = saveDate;
        this.storeLocation = saveLoc;
    }

    public void setPuzzleStatistics(long totalSolveTime, long lineSolvingTime, int totalBackTrackIterations,
                                    long totalLinesProcessed, int maxSolvingStackLoad) {
        this.totalSolveTime = totalSolveTime;
        this.lineSolvingTime = lineSolvingTime;
        this.backTrackIterations = totalBackTrackIterations;
        this.linesProcessed = totalLinesProcessed;
        this.maxSolvingStackLoad = maxSolvingStackLoad;

    }


    @SuppressWarnings("WeakerAccess")
    protected PuzzleImage(Parcel in) {
        id = in.readInt();
        rows = in.readInt();
        cols = in.readInt();
        solved = in.readByte() != 0;
        totalSolveTime = in.readLong();
        lineSolvingTime = in.readLong();
        backTrackIterations = in.readInt();
        linesProcessed = in.readLong();
        maxSolvingStackLoad = in.readInt();
        saveDate = in.readLong();
        storeLocation = in.readString();
    }

    public static final Creator<PuzzleImage> CREATOR = new Creator<PuzzleImage>() {
        @Override
        public PuzzleImage createFromParcel(Parcel in) {
            return new PuzzleImage(in);
        }

        @Override
        public PuzzleImage[] newArray(int size) {
            return new PuzzleImage[size];
        }
    };

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getStoreLocation() {
        return storeLocation;
    }

    public long getSaveDate() {
        return saveDate;
    }

    @SuppressWarnings("WeakerAccess")
    public int getRows() {
        return rows;
    }

    @SuppressWarnings("WeakerAccess")
    public int getCols() {
        return cols;
    }

    @SuppressWarnings("WeakerAccess")
    public boolean isSolved() {
        return solved;
    }

    public long getTotalSolveTime() {
        return totalSolveTime;
    }

    public long getLineSolvingTime() {
        return lineSolvingTime;
    }

    public int getBackTrackIterations() {
        return backTrackIterations;
    }

    public long getLinesProcessed() {
        return linesProcessed;
    }

    public int getMaxSolvingStackLoad() {
        return maxSolvingStackLoad;
    }



    public void setStoreLocation(String storeLocation) {
        this.storeLocation = storeLocation;
    }

    public void setSaveDate(long saveDate) {
        this.saveDate = saveDate;
    }

    @Override
    public String toString() {
        return "PuzzlePicture [_id: " + this.id +", _filePath: " + storeLocation + "]";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeInt(rows);
        dest.writeInt(cols);
        dest.writeByte((byte) (solved ? 1 : 0));

        dest.writeLong(totalSolveTime);
        dest.writeLong(lineSolvingTime);
        dest.writeInt(backTrackIterations);
        dest.writeLong(linesProcessed);
        dest.writeInt(maxSolvingStackLoad);

        dest.writeLong(saveDate);
        dest.writeString(storeLocation);
    }
}
