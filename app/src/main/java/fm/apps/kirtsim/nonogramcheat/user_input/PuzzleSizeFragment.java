package fm.apps.kirtsim.nonogramcheat.user_input;


import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import fm.apps.kirtsim.nonogramcheat.R;


public class PuzzleSizeFragment extends Fragment {

    public static final String TAG = "PuzzleSizeFragment";

    private TextView rowCountTV, colCountTV;

    public PuzzleSizeFragment() {}

    public Point getRowsAndColumns() {
        final int rows = rowCountTV == null ? 0 : Integer.parseInt(String.valueOf(rowCountTV.getText()));
        final int cols = colCountTV == null ? 0 : Integer.parseInt(String.valueOf(colCountTV.getText()));
        return new Point(rows, cols);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_puzzle_size, container, false);
        rowCountTV = (TextView) rootView.findViewById(R.id.rowsSizeTV);
        colCountTV = (TextView) rootView.findViewById(R.id.colsSizeTV);
        SeekBar rowSetter = (SeekBar) rootView.findViewById(R.id.rowsSetterSB);
        boundSeekBarToTextView(rowSetter, rowCountTV);
        SeekBar colSetter = (SeekBar) rootView.findViewById(R.id.colsSetterSB);
        boundSeekBarToTextView(colSetter, colCountTV);
        return rootView;
    }

    private void boundSeekBarToTextView(SeekBar seekBar, TextView textView) {
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textView.setText(String.valueOf(progress + 2));
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }
}
