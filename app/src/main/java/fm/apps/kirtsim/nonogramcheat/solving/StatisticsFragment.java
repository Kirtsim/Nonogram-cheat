package fm.apps.kirtsim.nonogramcheat.solving;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Locale;

import fm.apps.kirtsim.nonogramcheat.gallery.PuzzleImage;
import fm.apps.kirtsim.nonogramcheat.R;

/**
 *
 * Created by kirtsim on 16/03/2017.
 */

public class StatisticsFragment extends Fragment {
    public static final String TAG = StatisticsFragment.class.getSimpleName();
    private static final String PUZZLE_PARAM_OBJ = "puzzle.SF";
    private StatisticsFragmentListener listener;
    private PuzzleImage puzzle;
    private Locale locale;

    interface StatisticsFragmentListener {
        void onReturnButtonClicked();
    }

    public StatisticsFragment() {
    }

    public static StatisticsFragment newInstance(PuzzleImage puzzle) {
        StatisticsFragment fragment = new StatisticsFragment();
        Bundle args = new Bundle();
        args.putParcelable(PUZZLE_PARAM_OBJ, puzzle);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        locale = Locale.getDefault();
        Bundle args = getArguments();
        if (args != null)
            puzzle = args.getParcelable(PUZZLE_PARAM_OBJ);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootV = inflater.inflate(R.layout.fragment_statistics, container, false);
        if (puzzle != null)
            fillTextViewsWithPuzzleStats(rootV);
        rootV.findViewById(R.id.return_button).setOnClickListener(e -> listener.onReturnButtonClicked());
        return rootV;
    }

    private void fillTextViewsWithPuzzleStats(View rootV) {
        ((TextView) rootV.findViewById(R.id.total_time)).setText(nanoToMinutesString(puzzle.getTotalSolveTime()));
        ((TextView) rootV.findViewById(R.id.total_time_line_solv)).setText(nanoToMinutesString(puzzle.getLineSolvingTime()));
        ((TextView) rootV.findViewById(R.id.backtrack_iterations)).setText(String.valueOf(puzzle.getBackTrackIterations()));
        ((TextView) rootV.findViewById(R.id.maxStackLoad)).setText(String.valueOf(puzzle.getMaxSolvingStackLoad()));
        ((TextView) rootV.findViewById(R.id.lines_processed)).setText(String.valueOf(puzzle.getLinesProcessed()));
        String strBacktrackingIncluded = puzzle.getBackTrackIterations() == 0 ? "no" : "yes";
        ((TextView) rootV.findViewById(R.id.backtracking_used)).setText(strBacktrackingIncluded);
    }

    private String nanoToMinutesString(long nano) {
        return String.format(locale,"%3.4f s", (nano / 1000000000.0f));
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof StatisticsFragmentListener)
            listener = (StatisticsFragmentListener) context;
        else
            throw new IllegalArgumentException("context must implement " + StatisticsFragmentListener.class.getSimpleName());
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }
}
