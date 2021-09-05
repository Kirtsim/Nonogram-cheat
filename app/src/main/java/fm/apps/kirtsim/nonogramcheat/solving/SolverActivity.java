package fm.apps.kirtsim.nonogramcheat.solving;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import cellsAndDescriptions.PuzzleCentering;
import fm.apps.kirtsim.nonogramcheat.gallery.PuzzleImage;
import fm.apps.kirtsim.nonogramcheat.MainActivity;
import fm.apps.kirtsim.nonogramcheat.PuzzleFragment;
import fm.apps.kirtsim.nonogramcheat.PuzzleView;
import fm.apps.kirtsim.nonogramcheat.R;

public class SolverActivity extends AppCompatActivity implements SolverFragment.SolverFragmentListener,
                            SolveUIFragment.SolveResultListener, PuzzleFragment.PuzzleFragmentListener,
                            StatisticsFragment.StatisticsFragmentListener{
    private SolverFragment solverFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_solver);
        solverFragment = (SolverFragment) getFragmentByTag(SolverFragment.TAG);
        if (solverFragment == null && savedInstanceState == null)
            initializeAsNew(getIntent().getExtras());
    }

    private void initializeAsNew(Bundle data) {
        if (data != null) {
            final boolean doNotAddToBackStack = false;
            solverFragment = SolverFragment.newInstance(data);
            addOrReplaceFragment(solverFragment, SolverFragment.TAG, null, doNotAddToBackStack);
            SolveUIFragment solveF = new SolveUIFragment();
            addOrReplaceFragment(solveF, SolveUIFragment.TAG, null, doNotAddToBackStack);
        }
    }

    private void addOrReplaceFragment(Fragment fragment, String newFTag, String oldFTag, boolean addToBackStack) {
        Fragment oldFragment = null;
        FragmentManager manager = getSupportFragmentManager();
        if (oldFTag != null)
            oldFragment = manager.findFragmentByTag(oldFTag);
        FragmentTransaction txn = manager.beginTransaction();

        if (oldFragment != null)
            txn.remove(oldFragment);
        txn.add(R.id.upperFragmentHolder, fragment, newFTag);
        if (addToBackStack)
            txn.addToBackStack(oldFTag);
        txn.commit();
    }

    private Fragment getFragmentByTag(String fragmentTag) {
        return getSupportFragmentManager().findFragmentByTag(fragmentTag);
    }

    @Override
    public void onSolvingFinished(int status, byte[] resultCells) {
        SolveUIFragment f = (SolveUIFragment) getFragmentByTag(SolveUIFragment.TAG);
        if (f != null)
            f.onPuzzleSolvingFinished(status);
    }

    private boolean registerNewPuzzleFragment(byte[] puzzleCells) {
        Bundle arguments = getIntent().getExtras();
        if (puzzleCells != null && arguments != null) {
            final boolean INCLUDE_MENU = true;
            final boolean NOT_EDITABLE = false;
            PuzzleFragment pf = PuzzleFragment.newInstance(
                    arguments.getByteArray(PuzzleView.H_DESCRIPTIONS_PARAM),
                    arguments.getByteArray(PuzzleView.V_DESCRIPTIONS_PARAM),
                    puzzleCells, PuzzleFragment.EDIT_TOOLS_EXCLUDED,
                    NOT_EDITABLE,
                    INCLUDE_MENU,
                    PuzzleCentering.FLAG_CENTER_FIT_PUZZLE_CELLS);
            final boolean addToBackStack = true;
            addOrReplaceFragment(pf, PuzzleFragment.TAG, null, addToBackStack);
            return true;
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.base_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (getSupportFragmentManager().getBackStackEntryCount() == 1) {
                    getSupportFragmentManager().popBackStack();
                } else
                    onBackPressed();
                return true;
            case R.id.home_menu_item:
                startActivity(MainActivity.createHomeIntent(this));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDisplayPuzzleRequest() {
        byte[] resultCells = solverFragment != null ? solverFragment.getResultCells() : null;
        if (!registerNewPuzzleFragment(resultCells))
            Toast.makeText(this, "something went wrong :(", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPuzzleStatsRequest() {
        final boolean addToBackStack = true;
        PuzzleImage puzzle = solverFragment != null ? solverFragment.getPuzzleImage() : new PuzzleImage();
        StatisticsFragment statisticsFragment = StatisticsFragment.newInstance(puzzle);
        addOrReplaceFragment(statisticsFragment, StatisticsFragment.TAG, null, addToBackStack);
    }

    @Override
    public PuzzleImage onPuzzleImageRequestedForSaving() {
        if (solverFragment != null)
            return solverFragment.getPuzzleImage();
        return null;
    }

    @Override
    public void onReturnButtonClicked() {
        getSupportFragmentManager().popBackStack();
    }
}
