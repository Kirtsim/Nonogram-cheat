package fm.apps.kirtsim.nonogramcheat.solving;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.SystemClock;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import fm.apps.kirtsim.nonogramcheat.R;
import solver.Puzzle;

public class SolveUIFragment extends Fragment {
    public static final String TAG = "SolveUIFragment";

    private static final String STATUS_TEXT_PARAM = "statusText:SUIF";
    private static final String START_TIME_PARAM = "startTime:SUIF";
    private static final String SOLVING_FINISHED_BOOL = "solvingFinished:SUIF";
    private static final String SOLVING_STATUS_INT = "solvingStatus:SUIF";
    private static final String TIMER_TEXT = "timerText:SUIF";

    Chronometer timer;
    private long startOfTiming;
    private SolveResultListener listener;
    private int solvingStatus;
    private boolean solvingFinished;

    interface SolveResultListener {
        void onDisplayPuzzleRequest();
        void onPuzzleStatsRequest();
    }

    public SolveUIFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_solve_process, container, false);
        timer = (Chronometer) root.findViewById(R.id.solve_timer);
        root.findViewById(R.id.result_button).setOnClickListener(v -> sendRequestToActivity());
        root.findViewById(R.id.statistics_button).setOnClickListener(v -> onPuzzleStatsRequested());
        if (savedInstanceState == null)
            initializeAsNew();
        return root;
    }

    private void initializeAsNew() {
        displayAfterSolveButtons(false);
        startOfTiming = SystemClock.elapsedRealtime();
        solvingStatus = -1;
        timer.setBase(startOfTiming);
        timer.start();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            View rootView = getView();
            initializeFromSavedState(savedInstanceState, rootView);
        }
    }

    private void sendRequestToActivity() {
        if (listener != null)
            listener.onDisplayPuzzleRequest();
        else {
            displayToast("Could not display solution");
            Log.e(TAG, "listener is null!");
        }
    }

    private void onPuzzleStatsRequested() {
        if (listener != null) {
            listener.onPuzzleStatsRequest();
        } else {
            displayToast("Could not display statistics");
            Log.e(TAG, "listener is null!");
        }
    }

    private void displayToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void initializeFromSavedState(Bundle savedState, View root) {
        solvingFinished = savedState.getBoolean(SOLVING_FINISHED_BOOL, false);
        startOfTiming = savedState.getLong(START_TIME_PARAM, SystemClock.elapsedRealtime());
        timer.setBase(startOfTiming);
        ((TextView) root.findViewById(R.id.statusTV)).setText(savedState.getString(STATUS_TEXT_PARAM));
        if (solvingFinished) {
            onPuzzleSolvingFinished(savedState.getInt(SOLVING_STATUS_INT, Puzzle.CONFLICT));
            timer.setText(savedState.getString(TIMER_TEXT));
        }
        else {
            Resources res = getResources();
            timer.start();
            getTextView(R.id.solveStatusHeadingTV).setText(res.getText(R.string.solve_in_progress_title));
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(SOLVING_FINISHED_BOOL, solvingFinished);
        outState.putLong(START_TIME_PARAM, startOfTiming);
        outState.putString(STATUS_TEXT_PARAM, (String) getTextView(R.id.statusTV).getText());
        if (solvingFinished) {
            outState.putInt(SOLVING_STATUS_INT, solvingStatus);
            outState.putString(TIMER_TEXT, (String) timer.getText());
        }
    }

    private TextView getTextView(int id) {
        TextView tv = null;
        View rootV = getView();
        if (rootV != null)
            tv = (TextView) rootV.findViewById(id);
        return tv != null ? tv : new TextView(getContext());
    }

    public void onPuzzleSolvingFinished(int status) {
        timer.stop();
        solvingFinished = true;
        solvingStatus = status;
        hideProgressBar();
        updateSolveStatusImage();
        Resources res = getResources();
        if (res != null) {
            getTextView(R.id.solveStatusHeadingTV).setText(res.getString(R.string.solving_finished_title));
            if (status == Puzzle.SOLVED) {
                getTextView(R.id.statusTV).setText(res.getString(R.string.solution_found));
                displayAfterSolveButtons(true);
            } else {
                getTextView(R.id.statusTV).setText(res.getString(R.string.no_sol_found));
            }
        }
    }

    private void hideProgressBar() {
        View root = getView();
        if (root != null) {
            ProgressBar progress = (ProgressBar) root.findViewById(R.id.solveProgressBar);
            if (progress != null)
                progress.setVisibility(View.INVISIBLE);
        }
    }

    public void displayAfterSolveButtons(boolean display) {
        View root = getView();
        if (root == null)
            return;
        Button puzzleButton = (Button) root.findViewById(R.id.result_button);
        Button statsButton = (Button) root.findViewById(R.id.statistics_button);
        final int visible = display ? View.VISIBLE : View.INVISIBLE;
        if (puzzleButton != null) {
            puzzleButton.setVisibility(visible);
            puzzleButton.setClickable(display);
        }
        if (statsButton != null) {
            statsButton.setVisibility(visible);
            statsButton.setClickable(display);
        }
    }

    private void updateSolveStatusImage() {
        View root = getView();
        if (root != null) {
            ImageView img = (ImageView) root.findViewById(R.id.solve_status_image);
            if (img == null) return;
            switch (solvingStatus) {
                case Puzzle.SOLVED:
                    img.setBackgroundResource(R.drawable.solve_success_w_48);
                    break;
                case Puzzle.CONFLICT:case Puzzle.PAINTED: case Puzzle.UNCHANGED:
                    img.setBackgroundResource(R.drawable.solve_fail_w_48);
                    break;
                default:
                    img.setBackgroundResource(R.drawable.wait_icon_w_48);
            }


        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof SolveResultListener)
            listener = (SolveResultListener) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        this.listener = null;
    }
}
