package fm.apps.kirtsim.nonogramcheat;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.lang.ref.WeakReference;

import cellsAndDescriptions.PuzzleArraysController;
import cellsAndDescriptions.PuzzleCentering;
import db.DbHelperSingleton;
import fm.apps.kirtsim.nonogramcheat.gallery.PuzzleImage;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class PuzzleFragment extends Fragment implements AdapterView.OnItemSelectedListener {
    public static final String TAG = "PuzzleFragment";
    public static final byte EDIT_TOOLS_EXCLUDED = 0;
    public static final byte EDIT_TOOLS_INCLUDED = 1;

    public static final byte INIT_MODE_1 = 1;
    public static final byte INIT_MODE_2 = 2;

    private static final String INIT_MODE_PARAM     = "PuzzleF.initMode";
    private static final String EDIT_TOOLS_PARAM    = "PuzzleF.include_settings";
    private static final String ROW_PARAM           = "PuzzleF.row_count";
    private static final String COL_PARAM           = "PuzzleF.col_count";
    private static final String NEXT_DESC_PARAM     = "PuzzleF.next_desc";
    private static final String DESC_SETTER_PROGRESS_PARAM = "PuzzleF.dSetter_progress";
    private static final String INCLUDE_MENU_PARAM  = "PuzzleF.include_menu";
    private static final String PUZZLE_CENTER_PARAM  = "puzzleCenter.PF";

    private PuzzleFragmentListener listener;
    private PuzzleView puzzleView;
    private DbHelperSingleton dbHelper;
    private byte centerFlag;

    public interface PuzzleFragmentListener {
        PuzzleImage onPuzzleImageRequestedForSaving();
    }

    public PuzzleFragment() {}

    public static PuzzleFragment newInstance(int rowCount, int colCount, int nextDescriptor, int editTools,
                                             boolean isEditable,
                                             boolean includeMenu,
                                             byte puzzleCenterFlag) {
        PuzzleFragment fragment = new PuzzleFragment();
        Bundle args = createBundleFromParams(INIT_MODE_1, editTools, isEditable, includeMenu, puzzleCenterFlag);
        args.putInt(ROW_PARAM, rowCount);
        args.putInt(COL_PARAM, colCount);
        args.putInt(NEXT_DESC_PARAM, nextDescriptor);
        fragment.setArguments(args);
        return fragment;
    }

    public static PuzzleFragment newInstance(byte[] hDescs, byte[] vDescs, byte[] cells, int editTools,
                                             boolean isEditable,
                                             boolean includeMenu,
                                             byte puzzleCenterFlag) {
        PuzzleFragment fragment = new PuzzleFragment();
        Bundle args = createBundleFromParams(INIT_MODE_2, editTools, isEditable, includeMenu, puzzleCenterFlag);
        args.putByteArray(PuzzleView.V_DESCRIPTIONS_PARAM, vDescs);
        args.putByteArray(PuzzleView.H_DESCRIPTIONS_PARAM, hDescs);
        args.putByteArray(PuzzleView.PUZZLE_CELLS_PARAM, cells);
        fragment.setArguments(args);
        return fragment;
    }

    private static  Bundle createBundleFromParams(int initMode, int editTools,
                                                  boolean isEditable, boolean includeMenu, byte centerFlag) {
        Bundle args = new Bundle();
        args.putInt(INIT_MODE_PARAM, initMode);
        args.putInt(EDIT_TOOLS_PARAM, editTools);
        args.putBoolean(INCLUDE_MENU_PARAM, includeMenu);
        args.putBoolean(PuzzleView.IS_PUZ_EDITABLE_PARAM, isEditable);
        args.putByte(PUZZLE_CENTER_PARAM, centerFlag);
        return args;
    }

    public int getNumberOfIncorrectDescLines() {
        return puzzleView.getIncorrectDescsCount();
    }

    public Point getRowAndColCount() {
        Bundle args = getArguments();
        if (args != null)
            return new Point(args.getInt(ROW_PARAM, 1), args.getInt(COL_PARAM, 1));
        else if (puzzleView != null)
            return puzzleView.getRowColCount();
        return new Point(1, 1);
    }

    public void setNextDescriptor(int value) {
        if (puzzleView != null)
            puzzleView.setNextDescriptorToAdd(value);
        else {
            Bundle args = getArguments() != null ? getArguments() : new Bundle();
            args.putInt(NEXT_DESC_PARAM, value);
        }
    }

    public void setPuzzleEditable(boolean editable) {
        if (puzzleView != null)
            puzzleView.setIsEditable(editable);
    }

    public void enableMoveModeInPuzzle(boolean enable) {
        if (puzzleView != null) {
            boolean isMoveMode = puzzleView.isInMoveMode();
            if (enable != isMoveMode)
                puzzleView.switchMoveMode();
        }

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        boolean includeMenuItems = false;
        if (savedInstanceState != null) {
            includeMenuItems = savedInstanceState.getBoolean(INCLUDE_MENU_PARAM, false);
            centerFlag = savedInstanceState.getByte(PUZZLE_CENTER_PARAM, PuzzleCentering.FLAG_CENTER_PUZZLE);
        } else {
            Bundle args = getArguments();
            if (args != null) {
                includeMenuItems = args.getBoolean(INCLUDE_MENU_PARAM, false);
                centerFlag = args.getByte(PUZZLE_CENTER_PARAM, PuzzleCentering.FLAG_CENTER_PUZZLE);
            }
        }
        setHasOptionsMenu(includeMenuItems);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedState) {
        View root = inflater.inflate(R.layout.fragment_puzzle, container, false);
        puzzleView = (PuzzleView) root.findViewById(R.id.puzzleView);
        puzzleView.setCenterFlag(centerFlag);
        if (savedState == null) {
            initializeFromArguments();
        }
        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState == null) {
            Bundle arguments = getArguments();
            if(arguments.getInt(EDIT_TOOLS_PARAM, EDIT_TOOLS_EXCLUDED) == EDIT_TOOLS_INCLUDED)
                addEditTools(new Point(arguments.getInt(ROW_PARAM, 1), arguments.getInt(COL_PARAM, 1)), false);
        } else
            initializeFromSavedState(savedInstanceState);
    }

    private void initializeFromArguments() {
        Bundle arguments = getArguments();
        if (arguments != null && puzzleView != null) {
            switch (arguments.getInt(INIT_MODE_PARAM, INIT_MODE_1)) {
                case INIT_MODE_1:
                    initPuzzle(arguments.getInt(ROW_PARAM, 1), arguments.getInt(COL_PARAM, 1));
                    break;
                case INIT_MODE_2:
                    initPuzzle( arguments.getByteArray(PuzzleView.H_DESCRIPTIONS_PARAM),
                                arguments.getByteArray(PuzzleView.V_DESCRIPTIONS_PARAM),
                                arguments.getByteArray(PuzzleView.PUZZLE_CELLS_PARAM));
            }
            if (puzzleView != null) {
                final boolean isEditable = arguments.getBoolean(PuzzleView.IS_PUZ_EDITABLE_PARAM, false);
                puzzleView.setNextDescriptorToAdd(arguments.getInt(NEXT_DESC_PARAM, 0));
                puzzleView.setIsEditable(isEditable);
                if (!isEditable && !puzzleView.isInMoveMode())
                    puzzleView.switchMoveMode();
            }
        }
    }

    private void initializeFromSavedState(Bundle savedState) {
        final int editState = savedState.getInt(EDIT_TOOLS_PARAM, EDIT_TOOLS_EXCLUDED);
        if (editState == EDIT_TOOLS_INCLUDED) {
            Point rowsCols = new Point(savedState.getInt(ROW_PARAM, 1), savedState.getInt(COL_PARAM, 1));
            addEditTools(rowsCols, savedState.getBoolean(PuzzleView.IS_MOVE_MODE_PARAM, false));
            setDescSetterProgress(savedState.getInt(DESC_SETTER_PROGRESS_PARAM, 0));
        }
    }

    private void setDescSetterProgress(int progress) {
        View root = getView();
        if (root != null) {
            SeekBar dSetter = (SeekBar) root.findViewById(R.id.descSetterSB);
            if (dSetter != null)
                dSetter.setProgress(progress);
        }
    }

    public void initPuzzle(int rows, int cols) {
        if (rows > 1 && rows < 31 && cols > 1 && cols < 31)
            puzzleView.setUpDefaultPuzzle(rows, cols);
    }

    public void initPuzzle(byte[] hDescriptions, byte[] vDescriptions, byte[] pCells) {
        if (hDescriptions != null && vDescriptions != null && pCells != null) {
            puzzleView.initPuzzle(
                    PuzzleArraysController.convertCellsFromSaveFormBackToOriginal(hDescriptions),
                    PuzzleArraysController.convertCellsFromSaveFormBackToOriginal(vDescriptions),
                    PuzzleArraysController.convertCellsFromSaveFormBackToOriginal(pCells));
        }

    }

    public void addEditTools(Point rowCols, boolean isInMoveMode) {
        ViewGroup layout = (ViewGroup) getView();
        if (layout != null) {
            ViewGroup container = (ViewGroup) layout.findViewById(R.id.descSetterHolder);
            View.inflate(getContext(), R.layout.puzzle_tool_layout, container);
            final ImageButton movePaintB = (ImageButton) container.findViewById(R.id.move_paint_button);
            setMovePaintButtonBackground(movePaintB, isInMoveMode);
            movePaintB.setOnClickListener(e -> onMovePaintButtonClicked(movePaintB));
            Spinner paintToolsSpinner = (Spinner) layout.findViewById(R.id.paint_tools_spinner);
            paintToolsSpinner.setAdapter(ArrayImgAdapter.newInstance(getContext()));
            paintToolsSpinner.setOnItemSelectedListener(this);
            SeekBar dSetter = (SeekBar) container.findViewById(R.id.descSetterSB);
            TextView tv = (TextView) container.findViewById(R.id.puzzle_toolTextView);
            dSetter.setOnSeekBarChangeListener(new DescSetterListener(this, tv));
            setStartValuesToDescSetter(dSetter, rowCols);
        }
    }

    private void onMovePaintButtonClicked(ImageButton button) {
        setMovePaintButtonBackground(button, puzzleView.switchMoveMode());
    }

    private void setMovePaintButtonBackground(ImageButton button, boolean isMoveMode) {
        if (isMoveMode)
            button.setBackgroundResource(R.drawable.paint_b_48);
        else
            button.setBackgroundResource(R.drawable.move_b_24);
    }


    private void setStartValuesToDescSetter(SeekBar dSetter, Point rowsCols) {
        int max = Math.max(rowsCols.x, rowsCols.y);
        if (max == 0) max++;
        dSetter.setMax(max -1);
        dSetter.setProgress(dSetter.getMax() /2);
        puzzleView.setNextDescriptorToAdd(dSetter.getProgress() + 1);
    }

    public void removeEditTools() {
        ViewGroup layout = (ViewGroup) getView();
        if (layout != null) {
            ViewGroup container = (ViewGroup) layout.findViewById(R.id.descSetterHolder);
            container.removeAllViews();
        }
    }

    public void saveDescriptionsAndCellsIntoBundle(Bundle data) {
        if (puzzleView != null)
            puzzleView.saveAllCellsIntoBundle(data);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.share_save_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.save_menu_item:
                onSavePuzzlePicture();
                return true;
            case R.id.share_menu_item:
                onSharePuzzleImage();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void onSharePuzzleImage() {
        try {
            String tempFilePath = getTemporaryPathForImage();
            boolean directoriesAvailable = createParentFilesIfNeeded(tempFilePath);
            Bitmap bitmap = getPuzzleBitmap();
            if (directoriesAvailable && savePuzzleToFile(bitmap, tempFilePath)) {
                File puzzlePath = new File(tempFilePath);
                if (puzzlePath.exists()) {
                    Uri uri = FileProvider.getUriForFile(getContext(),
                            "fm.apps.kirtsim.nonogramcheat.fileprovider", puzzlePath);
                    Intent shareI = createShareIntent(uri);
                    startActivity(Intent.createChooser(shareI, "Choose app"));
                }
            }
        } catch (Exception e) { Log.e(TAG, "share exception", e); }
    }

    private String getTemporaryPathForImage() {
        return getContext().getCacheDir() + "tempImages/img.png";
    }

    private Intent createShareIntent(Uri imageSource) {
        Intent shareI = new Intent(Intent.ACTION_SEND);
        shareI.setType("image/png");
        shareI.putExtra(Intent.EXTRA_STREAM, imageSource);
        shareI.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        return shareI;
    }


    private void onSavePuzzlePicture() {
        try {
            Bitmap bitmap = getPuzzleBitmap();
            String imagePath = buildImagePath();
            boolean parentFilesPresent = createParentFilesIfNeeded(imagePath);
            if (parentFilesPresent && savePuzzleToFile(bitmap, imagePath)) {
                savePuzzleToDatabase(imagePath);
                Toast.makeText(getContext(), "Image saved", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (Exception e) { Log.e(TAG, e.toString(), e); }
        Toast.makeText(getContext(), "Could not save image", Toast.LENGTH_SHORT).show();
    }

    private Bitmap getPuzzleBitmap() {
        puzzleView.setDrawingCacheEnabled(true);
        return puzzleView.getDrawingCache();
    }

    private String buildImagePath() {
        File file = getContext().getFilesDir();
        StringBuilder pathBuilder = new StringBuilder(file.getAbsolutePath());
        pathBuilder.append("/").append("images/p")
                .append(System.currentTimeMillis()).append(".png");
        return pathBuilder.toString();
    }

    private boolean createParentFilesIfNeeded(String filePath) {
        File file = new File(filePath);
        File parentFile = file.getParentFile();
        if (!parentFile.exists())
            parentFile.mkdirs();
        return parentFile.exists();
    }

    private void savePuzzleToDatabase(String imagePath) {
        PuzzleImage puzzleImage = listener.onPuzzleImageRequestedForSaving();
        puzzleImage.setSaveDate(System.currentTimeMillis());
        puzzleImage.setStoreLocation(imagePath);
        if (dbHelper == null)
            dbHelper = DbHelperSingleton.getInstance(getContext());
        dbHelper.addPuzzleImage(puzzleImage);
    }

    private boolean savePuzzleToFile(Bitmap puzzlePic, String fullFilePath) {
        boolean success;
        try (FileOutputStream outputStream = new FileOutputStream(fullFilePath);
             BufferedOutputStream bos = new BufferedOutputStream(outputStream)) {
            success = puzzlePic.compress(Bitmap.CompressFormat.PNG, 100, bos);
            bos.flush();
            bos.close();
        } catch (Exception ex) {
            Log.e(TAG, "EXCEPTION:", ex); success = false;
        }
        return success;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof PuzzleFragmentListener)
            listener = (PuzzleFragmentListener) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.closeDatabase();
            dbHelper = null;
        }
    }


    @SuppressWarnings("RestrictedApi")
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Point rowsCols = getRowAndColCount();
        outState.putInt(ROW_PARAM, rowsCols.x);
        outState.putInt(COL_PARAM, rowsCols.y);
        outState.putByte(PUZZLE_CENTER_PARAM, centerFlag);
        saveEditToolsStateWithToolsIfNeeded(outState);
        if (hasOptionsMenu())
            outState.putBoolean(INCLUDE_MENU_PARAM, true);
    }

    private void saveEditToolsStateWithToolsIfNeeded(Bundle outState) {
        View root = getView();
        int editToolsState = EDIT_TOOLS_EXCLUDED;
        if (root != null) {
            SeekBar descSetter = (SeekBar) root.findViewById(R.id.descSetterSB);
            if (descSetter != null) {
                outState.putInt(DESC_SETTER_PROGRESS_PARAM, descSetter.getProgress());
                outState.putBoolean(PuzzleView.IS_MOVE_MODE_PARAM, puzzleView.isInMoveMode());
                editToolsState = EDIT_TOOLS_INCLUDED;
            }
        }
        outState.putInt(EDIT_TOOLS_PARAM, editToolsState);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Integer bitmap = (Integer) parent.getItemAtPosition(position);
        switch (bitmap) {
            case R.drawable.black_white_48:
                puzzleView.setPaintMode(PuzzleView.PAINT_MODE_BLACK_WHITE); break;
            case R.drawable.paint_black:
                puzzleView.setPaintMode(PuzzleView.PAINT_MODE_BLACK_ONLY); break;
            case R.drawable.paint_white:
                puzzleView.setPaintMode(PuzzleView.PAINT_MODE_WHITE_ONLY); break;
            case R.drawable.erase_dark_brown_48:
                puzzleView.setPaintMode(PuzzleView.PAINT_MODE_ERASE); break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {}


    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
    *                       S E E K B A R   L I S T E N E R                *
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
    private static class DescSetterListener implements SeekBar.OnSeekBarChangeListener {
        private WeakReference<PuzzleFragment> fragmentRef;
        private TextView textView;

        DescSetterListener(PuzzleFragment puzzleFragment, TextView txtView) {
            fragmentRef = new WeakReference<>(puzzleFragment);
            this.textView = txtView != null ? txtView :
                    new TextView(puzzleFragment.getActivity().getApplicationContext());
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (textView != null)
                textView.setText(String.valueOf(progress + 1));
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            PuzzleFragment fragment = fragmentRef.get();
            if (fragment != null)
                fragment.setNextDescriptor(seekBar.getProgress() + 1);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {}
    }


     /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
    *                  S P I N N E R   A R R.  A D A P T E R                *
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
     private static class ArrayImgAdapter extends ArrayAdapter<Integer> {
         private Integer[] images;

         static ArrayImgAdapter newInstance(Context context) {
             Integer[] images = new Integer[] {R.drawable.black_white_48,
                     R.drawable.paint_black, R.drawable.paint_white, R.drawable.erase_dark_brown_48};
             return new ArrayImgAdapter(context, images);
         }

         ArrayImgAdapter(@NonNull Context context, @NonNull Integer[] images) {
             super(context, android.R.layout.simple_spinner_item, images);
             this.images = images;
         }

         @Override
         public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
             return getImageAt(position);
         }

         @NonNull
         @Override
         public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
             return getImageAt(position);
         }

         private View getImageAt(int position) {
             ImageView img = new ImageView(getContext());
             img.setBackgroundResource(images[position]);
             img.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
             return img;
         }
     }
}
