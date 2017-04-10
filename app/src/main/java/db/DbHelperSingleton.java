package db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * Created by kirtsim on 27/02/2017.
 */

public class DbHelperSingleton extends SQLiteOpenHelper {
    private static final int DB_VERSION = 1;
    private static final String DB_NAME = "puzzleManager";

    private static DbHelperSingleton dbHelper;


    public static synchronized DbHelperSingleton getInstance(Context context) {
        if (dbHelper == null)
            dbHelper = new DbHelperSingleton(context.getApplicationContext());
        return dbHelper;
    }

    private DbHelperSingleton(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String CREATE_TABLES = "CREATE TABLE " + PuzzleImgEntry.TABLE + "("
                + PuzzleImgEntry.KEY_ID + " INTEGER PRIMARY KEY,"
                + PuzzleImgEntry.ROW_COUNT + " INTEGER,"
                + PuzzleImgEntry.COL_COUNT + " INTEGER,"
                + PuzzleImgEntry.SOLVED + " INTEGER,"
                + PuzzleImgEntry.SOLVE_TIME_TOTAL + " INTEGER,"
                + PuzzleImgEntry.SOLVE_TIME_LINE + " INTEGER,"
                + PuzzleImgEntry.TOTAL_BTCK_ITER + " INTEGER,"
                + PuzzleImgEntry.LINES_PROCESSED + " INTEGER,"
                + PuzzleImgEntry.MAX_STACK_LOAD + " INTEGER,"
                + PuzzleImgEntry.SAVE_DATE + " INTEGER,"
                + PuzzleImgEntry.FILE_PATH + " VARCHAR(100)" + ")";
        db.execSQL(CREATE_TABLES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + PuzzleImgEntry.TABLE);
        onCreate(db);
    }

    public void addPuzzleImage(PuzzleImage puzzleImage) {
        SQLiteDatabase db = this.getWritableDatabase();
        if (db != null) {
            ContentValues values = createContentValuesFromPuzzlePic(puzzleImage);
            db.insert(PuzzleImgEntry.TABLE, null, values);
        }
    }

//    public PuzzleImage getPuzzleImage(int id) {
//        SQLiteDatabase db = this.getWritableDatabase();
//        Cursor cursor = db.query(
//                PuzzleImgEntry.TABLE,
//                getAllPuzzlePicEntryColumns(),
//                PuzzleImgEntry.KEY_ID + "=?",
//                new String[] {String.valueOf(id)},
//                null, null, null, null);
//        PuzzleImage puzzleImage = null;
//        if (cursor != null && cursor.moveToFirst()) {
//            puzzleImage = extractPuzzleImgFromCursor(cursor);
//            cursor.close();
//        }
//        return puzzleImage == null ? new PuzzleImage() : puzzleImage;
//    }

//    private String[] getAllPuzzlePicEntryColumns() {
//        String[] cols =  new String[PuzzleImgEntry.ATTRIBUTES_COUNT];
//        cols[PuzzleImgEntry.I_ID]           = PuzzleImgEntry.KEY_ID;
//        cols[PuzzleImgEntry.I_ROW_COUNT]    = PuzzleImgEntry.ROW_COUNT;
//        cols[PuzzleImgEntry.I_COL_COUNT]    = PuzzleImgEntry.COL_COUNT;
//        cols[PuzzleImgEntry.I_SOLVE_TIME_TOTAL]    = PuzzleImgEntry.SOLVE_TIME_TOTAL;
//        cols[PuzzleImgEntry.I_SOLVE_TIME_LINE]    = PuzzleImgEntry.SOLVE_TIME_LINE;
//        cols[PuzzleImgEntry.I_TOTAL_BTCK_ITER]    = PuzzleImgEntry.TOTAL_BTCK_ITER;
//        cols[PuzzleImgEntry.I_LINES_PROCESSED]    = PuzzleImgEntry.LINES_PROCESSED;
//        cols[PuzzleImgEntry.I_MAX_STACK_LOAD]    = PuzzleImgEntry.MAX_STACK_LOAD;
//        cols[PuzzleImgEntry.I_SOLVED]       = PuzzleImgEntry.SOLVED;
//        cols[PuzzleImgEntry.I_SAVE_DATE]    = PuzzleImgEntry.SAVE_DATE;
//        cols[PuzzleImgEntry.I_FILE_PATH]    = PuzzleImgEntry.FILE_PATH;
//        return cols;
//    }

    private PuzzleImage extractPuzzleImgFromCursor(Cursor cursor) {
        try {
            PuzzleImage puzzle =  new PuzzleImage(
                    cursor.getInt(PuzzleImgEntry.I_ID),
                    cursor.getInt(PuzzleImgEntry.I_ROW_COUNT),
                    cursor.getInt(PuzzleImgEntry.I_COL_COUNT),
                    cursor.getInt(PuzzleImgEntry.I_SOLVED) != 0,
                    cursor.getLong(PuzzleImgEntry.I_SAVE_DATE),
                    cursor.getString(PuzzleImgEntry.I_FILE_PATH));
            puzzle.setPuzzleStatistics(
                    cursor.getLong(PuzzleImgEntry.I_SOLVE_TIME_TOTAL),
                    cursor.getLong(PuzzleImgEntry.I_SOLVE_TIME_LINE),
                    cursor.getInt(PuzzleImgEntry.I_TOTAL_BTCK_ITER),
                    cursor.getLong(PuzzleImgEntry.I_LINES_PROCESSED),
                    cursor.getInt(PuzzleImgEntry.I_MAX_STACK_LOAD));
            return puzzle;
        } catch (Exception ex) {Log.e(this.getClass().getSimpleName(), "EXCEPTION:", ex);}
        return new PuzzleImage();
    }

    private ContentValues createContentValuesFromPuzzlePic(PuzzleImage puzzleImage) {
        ContentValues values = new ContentValues();
        values.put(PuzzleImgEntry.ROW_COUNT, puzzleImage.getRows());
        values.put(PuzzleImgEntry.COL_COUNT, puzzleImage.getCols());
        values.put(PuzzleImgEntry.SOLVED, puzzleImage.isSolved());
        values.put(PuzzleImgEntry.SOLVE_TIME_TOTAL, puzzleImage.getTotalSolveTime());
        values.put(PuzzleImgEntry.SOLVE_TIME_LINE, puzzleImage.getLineSolvingTime());
        values.put(PuzzleImgEntry.TOTAL_BTCK_ITER, puzzleImage.getBackTrackIterations());
        values.put(PuzzleImgEntry.LINES_PROCESSED, puzzleImage.getLinesProcessed());
        values.put(PuzzleImgEntry.MAX_STACK_LOAD, puzzleImage.getMaxSolvingStackLoad());
        if (puzzleImage.getSaveDate() < 1)
            puzzleImage.setSaveDate(System.currentTimeMillis());
        values.put(PuzzleImgEntry.SAVE_DATE, puzzleImage.getSaveDate());
        values.put(PuzzleImgEntry.FILE_PATH, puzzleImage.getStoreLocation());
        return values;
    }

    public List<PuzzleImage> getAllPuzzlePics() {
        Cursor cursor = selectAll(PuzzleImgEntry.TABLE);
        if (cursor != null) {
            List<PuzzleImage> puzzlePictures = new ArrayList<>(cursor.getCount());
            fillPuzzlePicListFromCursor(puzzlePictures, cursor);
            cursor.close();
            return puzzlePictures;
        }
        return new ArrayList<>(0);
    }

    private void fillPuzzlePicListFromCursor(List<PuzzleImage> list, Cursor cursor) {
        if (!cursor.moveToFirst())
            return;
        do {
            PuzzleImage puzzlePicture = extractPuzzleImgFromCursor(cursor);
            if (puzzlePicture.getStoreLocation() != null)
                list.add(puzzlePicture);
        } while (cursor.moveToNext());
    }

//    public int getPuzzlePicCount() {
//        Cursor cursor = selectAll(PuzzleImgEntry.TABLE);
//        if (cursor != null) {
//            cursor.close();
//            return cursor.getCount();
//        }
//        return 0;
//    }

    private Cursor selectAll(String table) {
        final String SELECT_QUERY = "SELECT * FROM " +  table;
        SQLiteDatabase db = this.getWritableDatabase();
        return db.rawQuery(SELECT_QUERY, null);
    }




//    public int updatePuzzlePic(PuzzleImage puzzlePicture) {
//        SQLiteDatabase db = this.getWritableDatabase();
//        ContentValues values = createContentValuesFromPuzzlePic(puzzlePicture);
//        return db.update(PuzzleImgEntry.TABLE,
//                    values,
//                    PuzzleImgEntry.KEY_ID + "= ?",
//                    new String[] {String.valueOf(puzzlePicture.getId())});
//    }

    public boolean deletePuzzlePic(PuzzleImage puzzlePicture) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(PuzzleImgEntry.TABLE,
                PuzzleImgEntry.KEY_ID + "=?",
                new String[]{String.valueOf(puzzlePicture.getId())}) == 1;
    }

    public int deleteMultiplePuzzlePics(List<PuzzleImage> puzzlePictures) {
        SQLiteDatabase db = this.getWritableDatabase();
        String ids = createIDsFromPuzzlePics(puzzlePictures);
        return db.delete(PuzzleImgEntry.TABLE, PuzzleImgEntry.KEY_ID + " IN (" + ids + ")", null);
    }

    private String createIDsFromPuzzlePics(List<PuzzleImage> puzzlePictures) {
        try {
            final int count = puzzlePictures.size();
            StringBuilder idBuilder = new StringBuilder(count * 2);
            for (int i = 0; i < count; i++) {
                idBuilder.append(puzzlePictures.get(i).getId());
                idBuilder.append(",");
            }
            if (idBuilder.length() > 0)
                idBuilder.deleteCharAt(idBuilder.length() -1);
            return idBuilder.toString();
        } catch (NullPointerException ex) {
            Log.e(this.getClass().getSimpleName(), "creating ids failed", ex);
        }
        return "";
    }



    public void closeDatabase() {
        getWritableDatabase().close();
    }

    private static class PuzzleImgEntry {
        static final String TABLE = "PuzzlePicture";
        static final String KEY_ID       = "id";
        static final String ROW_COUNT    = "row_count";
        static final String COL_COUNT    = "col_count";
        static final String SOLVED       = "solved";
        static final String SOLVE_TIME_TOTAL = "solve_time_total";
        static final String SOLVE_TIME_LINE  = "solve_line_time";
        static final String TOTAL_BTCK_ITER = "total_bck_iter";
        static final String LINES_PROCESSED = "lines_processed";
        static final String MAX_STACK_LOAD = "max_stack_load";
        static final String SAVE_DATE    = "save_date";
        static final String FILE_PATH    = "file_path";
//        static final byte ATTRIBUTES_COUNT = 11;

        static final byte I_ID          = 0;
        static final byte I_ROW_COUNT   = 1;
        static final byte I_COL_COUNT   = 2;
        static final byte I_SOLVED      = 3;
        static final byte I_SOLVE_TIME_TOTAL = 4;
        static final byte I_SOLVE_TIME_LINE = 5;
        static final byte I_TOTAL_BTCK_ITER     = 6;
        static final byte I_LINES_PROCESSED     = 7;
        static final byte I_MAX_STACK_LOAD      = 8;
        static final byte I_SAVE_DATE   = 9;
        static final byte I_FILE_PATH   = 10;
    }
}
