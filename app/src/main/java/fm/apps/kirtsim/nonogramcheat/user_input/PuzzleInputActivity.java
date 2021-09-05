package fm.apps.kirtsim.nonogramcheat.user_input;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Point;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import cellsAndDescriptions.PuzzleCentering;
import fm.apps.kirtsim.nonogramcheat.MainActivity;
import fm.apps.kirtsim.nonogramcheat.PuzzleFragment;
import fm.apps.kirtsim.nonogramcheat.R;
import fm.apps.kirtsim.nonogramcheat.solving.SolverActivity;


public class PuzzleInputActivity extends AppCompatActivity {
    public static final String TAG = "PuzzleInputActivity";
    private static final String INPUT_STATE_PARAM = "puzzleInputActivity.inputState";
    private static final int INPUT_STATE_PUZZ_SIZE = 0;
    private static final int INPUT_STATE_PUZZ_DESC = 1;
    private static final int INPUT_STATE_PUZZ_FINAL = 2;

    private int inputState;
    private Fragment currentFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_puzzle_input);
        if (savedInstanceState != null)
            initFromSavedState(savedInstanceState);
        else {
            initializeAsNew();
        }
        if (inputState != INPUT_STATE_PUZZ_FINAL)
            initContinueButton();
    }

    private void initFromSavedState(Bundle savedInstanceState) {
        inputState = savedInstanceState.getInt(INPUT_STATE_PARAM, 0);
        FragmentManager manager = getSupportFragmentManager();
        switch (inputState) {
            case INPUT_STATE_PUZZ_SIZE:
                currentFragment = manager.findFragmentByTag(PuzzleSizeFragment.TAG); break;
            case INPUT_STATE_PUZZ_DESC:
                currentFragment = manager.findFragmentByTag(PuzzleFragment.TAG); break;
            case INPUT_STATE_PUZZ_FINAL:
                currentFragment = manager.findFragmentByTag(PuzzleFragment.TAG);
                removeButtonFromButtonHolder();
                addSolveButton();
        }
    }

    private void initializeAsNew() {
        currentFragment = new PuzzleSizeFragment();
        registerNewFragment(PuzzleSizeFragment.TAG, null);
        initContinueButton();
    }

    private void initContinueButton() {
        Button contButton = (Button) findViewById(R.id.continueButtonGrid);
        contButton.setOnClickListener(v -> onContinueButtonClicked());
    }

    private void registerNewFragment(String fragmentTag, String prevFragTag) {
        FragmentTransaction txn = getSupportFragmentManager().beginTransaction();
        txn.replace(R.id.inputFragmentContainer, currentFragment, fragmentTag);
        if (prevFragTag != null)
            txn.addToBackStack(prevFragTag);
        txn.commit();
    }

    private void onContinueButtonClicked() {
        switch(inputState) {
            case INPUT_STATE_PUZZ_SIZE:
                toDescriptionStateFromSizeState();
                break;
            case INPUT_STATE_PUZZ_DESC:
                if (validateDescriptions())
                    toFinalStateFromDescriptionState();
        }
    }

    private void toDescriptionStateFromSizeState() {
        Point rowCol = retrieveRowsAndColumnsFromPuzzleSizeFragment();
        final int NEXT_DESC_ZERO = 0;
        final boolean NO_MENU = false;
        final boolean EDITABLE = true;

        currentFragment = PuzzleFragment.newInstance(rowCol.x, rowCol.y, NEXT_DESC_ZERO,
                PuzzleFragment.EDIT_TOOLS_INCLUDED, EDITABLE, NO_MENU, PuzzleCentering.FLAG_CENTER_FIT_H_DESCRIPTIONS);
        registerNewFragment(PuzzleFragment.TAG, PuzzleSizeFragment.TAG);
        inputState = INPUT_STATE_PUZZ_DESC;
    }

    private void toFinalStateFromDescriptionState() {
        ((PuzzleFragment) currentFragment).removeEditTools();
        removeButtonFromButtonHolder();
        addSolveButton();
        setEditModeOnPuzzle(false);
        setPuzzleMovable(true);
        showWarningMessage(true);
        inputState = INPUT_STATE_PUZZ_FINAL;
    }

    private boolean validateDescriptions() {
        int incorrect = 0;
        if (currentFragment instanceof PuzzleFragment)
            incorrect = ((PuzzleFragment) currentFragment).getNumberOfIncorrectDescLines();
        if (incorrect == 0)
            return true;
        else {
            Resources res = getResources();
            String toBe = res.getString(R.string.is);
            String line = res.getString(R.string.line_txt);
            if (incorrect > 1) {
                toBe = res.getString(R.string.are);
                line = res.getString(R.string.lines_txt);
            }
            String message = String.format(res.getString(R.string.incorrect_descs), toBe, incorrect, line);
            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
            return false;
        }

    }

    private Point retrieveRowsAndColumnsFromPuzzleSizeFragment() {
        if (currentFragment instanceof PuzzleSizeFragment)
            return ((PuzzleSizeFragment) currentFragment).getRowsAndColumns();
        return new Point(0, 0);
    }

    private void addSolveButton() {
        ViewGroup container = (ViewGroup) findViewById(R.id.buttonHolderInputActivity);
        View.inflate(getApplicationContext(), R.layout.solve_button, container);
        Button solveB = (Button) container.findViewById(R.id.solveImageButton);
        if (solveB != null)
            solveB.setOnClickListener(v -> onSolveButtonCLicked());
    }

    private void onSolveButtonCLicked() {
        Bundle data = new Bundle();
        if (currentFragment instanceof PuzzleFragment)
            ((PuzzleFragment)currentFragment).saveDescriptionsAndCellsIntoBundle(data);
        Intent solveIntent = new Intent(getApplicationContext(), SolverActivity.class);
        solveIntent.putExtras(data);
        startActivity(solveIntent);
    }

    private void removeButtonFromButtonHolder() {
        ViewGroup container = (ViewGroup) findViewById(R.id.buttonHolderInputActivity);
        container.removeAllViews();
    }

    private void setEditModeOnPuzzle(boolean editable) {
        if (currentFragment instanceof PuzzleFragment)
            ((PuzzleFragment)currentFragment).setPuzzleEditable(editable);
    }

    private void setPuzzleMovable(boolean movable) {
        if (currentFragment instanceof PuzzleFragment)
            ((PuzzleFragment)currentFragment).enableMoveModeInPuzzle(movable);
    }

    private void addContinueButton() {
        ViewGroup container = (ViewGroup) findViewById(R.id.buttonHolderInputActivity);
        View.inflate(getApplicationContext(), R.layout.cont_button_layout, container);
        Button contButton = (Button) container.findViewById(R.id.continueButtonGrid);
        contButton.setOnClickListener(v -> onContinueButtonClicked());
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(INPUT_STATE_PARAM, inputState);
    }

    @Override
    public void onBackPressed() {
        FragmentManager manager = getSupportFragmentManager();
        if (manager.getBackStackEntryCount() > 0) {
            switch (inputState) {
                case INPUT_STATE_PUZZ_FINAL:
                    toDescStateFromFinalState();
                    break;
                case INPUT_STATE_PUZZ_DESC:
                    toSizeStateFromDescState(manager);
            }
        } else
            super.onBackPressed();
    }

    private void toDescStateFromFinalState() {
        removeButtonFromButtonHolder();
        if (currentFragment instanceof PuzzleFragment)
            ((PuzzleFragment)currentFragment).addEditTools(getRowAndColCountFromPuzzleFragment(), true);
        addContinueButton();
        setEditModeOnPuzzle(true);
        showWarningMessage(false);
        inputState = INPUT_STATE_PUZZ_DESC;
    }

    private void toSizeStateFromDescState(FragmentManager manager) {
        manager.popBackStack();
        currentFragment = manager.findFragmentByTag(PuzzleSizeFragment.TAG);
        inputState = INPUT_STATE_PUZZ_SIZE;
    }

    private Point getRowAndColCountFromPuzzleFragment() {
        if (currentFragment instanceof PuzzleFragment)
            return ((PuzzleFragment) currentFragment).getRowAndColCount();
        return new Point(1, 1);
    }

    private void showWarningMessage(boolean show) {
        ViewGroup msg = (ViewGroup) findViewById(R.id.warning_holder);
        if (msg != null) {
            msg.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
            if(show)
                msg.bringToFront();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.base_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.home_menu_item) {
            startActivity(MainActivity.createHomeIntent(this));
            return true;
        }
        return false;
    }
}
