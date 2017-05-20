package fm.apps.kirtsim.nonogramcheat.gallery;


import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.support.v4.content.FileProvider;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.io.File;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;

import db.DbHelperSingleton;
import fm.apps.kirtsim.nonogramcheat.R;

public class GalleryListFragment extends Fragment {

    public static final String TAG = "GalleryListFragment";
    private static final String LAYOUT_MANAGER_PARAM = "GalleryListFragment.layoutManager";

    private GalleryFragmentListener listener;
    private RecyclerView recyclerView;
    private Menu menu;
    private MenuInflater menuInflater;

    interface GalleryFragmentListener {
        void onPuzzleClicked(ArrayList<PuzzleImage> puzzles, int position);
    }

    public GalleryListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar ab = ((AppCompatActivity)getActivity()).getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setTitle("Saved puzzles");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        View rootView = inflater.inflate(R.layout.fragment_gallery_list, container, false);
        initRecyclerView(rootView);
        PuzzleLoaderTask puzzleLoader = new PuzzleLoaderTask(this);
        puzzleLoader.execute();
        return rootView;
    }

    private void initRecyclerView(View rootView) {
        final int THREE_COLUMNS = 2;
        recyclerView = (RecyclerView) rootView.findViewById(R.id.puzzlePicsRV);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutMngr =
                new GridLayoutManager(getActivity().getApplicationContext(), THREE_COLUMNS);
        recyclerView.setLayoutManager(layoutMngr);
        recyclerView.setAdapter(new PuzzleListAdapter(this, new ArrayList<>(0)));
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof GalleryFragmentListener)
            listener = (GalleryFragmentListener) context;
        else
            throw new IllegalArgumentException("Must implement the " + GalleryFragmentListener.class.getSimpleName());
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
        recyclerView = null;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(LAYOUT_MANAGER_PARAM,recyclerView.getLayoutManager().onSaveInstanceState());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        this.menu = menu;
        this.menuInflater = inflater;
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.delete_menu_item:
                deleteSelectedPuzzles();
                displayShareAndDeleteActions(false);
                return true;
            case R.id.share_menu_item:
                shareSelectedPuzzles();
                return true;
            case android.R.id.home:
                PuzzleListAdapter adapter = (PuzzleListAdapter)recyclerView.getAdapter();
                if (adapter.getSelectedCount() == 0)
                    NavUtils.navigateUpFromSameTask(getActivity());
                else {
                    adapter.deselectAll();
                    displayShareAndDeleteActions(false);
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void displayShareAndDeleteActions(boolean display) {
        menu.clear();
        String title;
        ActionBar ab = ((AppCompatActivity)getActivity()).getSupportActionBar();
        if (display) {
            menuInflater.inflate(R.menu.share_delete_menu, menu);
            title = "Selection";
        } else {
            menuInflater.inflate(R.menu.gallery_menu, menu);
            title = "Saved puzzles";
        }
        if (ab != null)
            ab.setTitle(title);
    }

    private void deleteSelectedPuzzles() {
        PuzzleListAdapter adapter = (PuzzleListAdapter) recyclerView.getAdapter();
        PuzzleImage[] selected = adapter.getSelectedPuzzles();
        ArrayList<PuzzleImage> toDelete = new ArrayList<>(selected.length);
        for (PuzzleImage puzzle : selected) {
            File file = new File(puzzle.getStoreLocation());
            if (file.exists())
                if (file.delete())
                    toDelete.add(puzzle);
        }
        DbHelperSingleton dbHelper = DbHelperSingleton.getInstance(getContext());
        final int numOfDeleted = dbHelper.deleteMultiplePuzzlePics(toDelete);
        adapter.deleteSelectedPuzzles();
        if (numOfDeleted < selected.length)
            Toast.makeText(getContext(), "Could not delete all puzzles.", Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(getContext(), "Puzzles deleted.", Toast.LENGTH_SHORT).show();
    }

    private void shareSelectedPuzzles() {
        try {
            PuzzleImage[] selected = ((PuzzleListAdapter)recyclerView.getAdapter()).getSelectedPuzzles();
            ArrayList<Uri> uris = new ArrayList<>(selected.length);
            final Context context = getContext();
            //noinspection ForLoopReplaceableByForEach
            for(int i = 0; i < selected.length; i++) {
                File file = new File(selected[i].getStoreLocation());
                if (file.exists())
                    uris.add(FileProvider.getUriForFile(context, "fm.apps.kirtsim.nonogramcheat.fileprovider", file));
            }
            final Intent shareIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
            shareIntent.setType("image/png");
            shareIntent.putExtra(Intent.EXTRA_STREAM, uris);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(shareIntent, "Choose app"));
        } catch (Exception e) { Log.e(TAG, "share exception", e); }
    }


    public void onDataPrepared(ArrayList<PuzzleImage> puzzles) {
        try {
            PuzzleListAdapter adapter = (PuzzleListAdapter) recyclerView.getAdapter();
            adapter.setPuzzleImages(puzzles);
            adapter.notifyDataSetChanged();
            if (puzzles.isEmpty() && getView() != null)
                getView().setBackgroundResource(R.drawable.empty_list);
        } catch(NullPointerException ex) {
            Log.e(getClass().getSimpleName(), "exception: ", ex);
            Toast.makeText(getContext(), R.string.puzzle_load_err, Toast.LENGTH_SHORT).show();
        }
    }

    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
    *  ----------------------- L O A D E R    T A S K ------------------------------
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
    private static class PuzzleLoaderTask extends AsyncTask<Void, Void, ArrayList<PuzzleImage>> {
        WeakReference<GalleryListFragment> fragment;

        PuzzleLoaderTask(GalleryListFragment fragment) {
            super();
            this.fragment = new WeakReference<>(fragment);
        }

        @Override
        protected ArrayList<PuzzleImage> doInBackground(Void... params) {
            DbHelperSingleton dbHelperSingleton = DbHelperSingleton.getInstance(fragment.get().getContext());
            ArrayList<PuzzleImage> puzzles = (ArrayList<PuzzleImage>) dbHelperSingleton.getAllPuzzlePics();
            dbHelperSingleton.closeDatabase();
            Collections.sort(puzzles, (p1, p2) -> Long.compare(p1.getSaveDate(), p2.getSaveDate()));
            return puzzles;
        }

        @Override
        protected void onPostExecute(ArrayList<PuzzleImage> puzzleImages) {
            fragment.get().onDataPrepared(puzzleImages);
        }
    }

    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
    *  -----------------------        A D A P T E R      ------------------------------
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
    private static class PuzzleListAdapter extends RecyclerView.Adapter<PuzzleListAdapter.ViewHolder> {
        private static final String TAG = PuzzleListAdapter.class.getSimpleName();
        private final WeakReference<GalleryListFragment> fragmentRef;
        private SparseArray<PuzzleImage> selectedPuzzles;
        private ArrayList<PuzzleImage> puzzleImages;

        private final SimpleDateFormat dateFormat;
        private final Date date;

        PuzzleListAdapter(GalleryListFragment fragment, ArrayList<PuzzleImage> puzzleImages) {
            super();
            fragmentRef = new WeakReference<>(fragment);
            date = new Date();
            dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            this.puzzleImages = puzzleImages;
            selectedPuzzles = new SparseArray<>();
        }

        @Override
        public PuzzleListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            ViewGroup rowItem = (ViewGroup) LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.gallery_list_layout, parent, false);
            return new ViewHolder(rowItem, this);
        }

        @Override
        public void onBindViewHolder(PuzzleListAdapter.ViewHolder holder, int position) {
            final PuzzleImage puzzle = puzzleImages.get(position);
            final Context c = fragmentRef.get().getContext();
            date.setTime(puzzle.getSaveDate());
            holder.date.setText(dateFormat.format(date));
            setLabelBackground(holder.label, position);
            Glide.with(c).load(puzzle.getStoreLocation())
                    .placeholder(R.drawable.placeholder)
                    .crossFade()
                    .into(holder.pictureIV);
        }

        private void setLabelBackground(ViewGroup label, int position) {
            if (selectedPuzzles.get(position) != null) {
                label.setBackgroundColor(Color.rgb(201, 249, 255));
            } else {
                label.setBackgroundColor(Color.rgb(234, 200, 154));
            }
        }

        void onItemClicked(int position) {
            if (selectedPuzzles.size() == 0) {
                fragmentRef.get().listener.onPuzzleClicked(puzzleImages, position);
                return;
            }
            onSelectAction(position);
        }

        void onLongClick(int position) {
            onSelectAction(position);
        }

        private void onSelectAction(int position) {
            final int oldCount = selectedPuzzles.size();
            selectOrDeselect(position);
            if (oldCount == 0)
                fragmentRef.get().displayShareAndDeleteActions(true);
            else if(selectedPuzzles.size() == 0)
                fragmentRef.get().displayShareAndDeleteActions(false);
        }

        private void selectOrDeselect(int position) {
            if (selectedPuzzles.get(position) == null)
                selectedPuzzles.put(position, puzzleImages.get(position));
            else
                selectedPuzzles.remove(position);
            this.notifyItemChanged(position);
        }

        private void setPuzzleImages(ArrayList<PuzzleImage> newPuzzles) {
            puzzleImages = newPuzzles;
            selectedPuzzles.clear();
        }

        private int getSelectedCount() {
            return selectedPuzzles.size();
        }

        private PuzzleImage[] getSelectedPuzzles() {
            final int count = selectedPuzzles.size();
            PuzzleImage[] ret = new PuzzleImage[count];
            for (int i = 0; i < count; i++)
                ret[i] = selectedPuzzles.valueAt(i);
            return ret;
        }

        private void deselectAll() {
            final int count = selectedPuzzles.size();
            for (int i = 0; i < count; i++)
                this.notifyItemChanged(selectedPuzzles.keyAt(i));
            selectedPuzzles.clear();
        }

        private void deleteSelectedPuzzles() {
            if (selectedPuzzles.size() == 1)
                puzzleImages.remove(selectedPuzzles.keyAt(0));
            else if (selectedPuzzles.size() != 0)
                puzzleImages = copyToANewList();
            selectedPuzzles.clear();
            this.notifyDataSetChanged();
        }

        private ArrayList<PuzzleImage> copyToANewList() {
            final int selCount = selectedPuzzles.size();
            final int oldCount = puzzleImages.size();
            ArrayList<PuzzleImage> newPics = new ArrayList<>(oldCount - selCount);
            int lastIndex = 0;
            for (int i = 0; i < selCount; i++) {
                final int index = selectedPuzzles.keyAt(i);
                copyToWithinRange(newPics, lastIndex, index);
                lastIndex = index + 1;
            }
            if (lastIndex < oldCount)
                copyToWithinRange(newPics, lastIndex, oldCount);
            return newPics;
        }

        private void copyToWithinRange(ArrayList<PuzzleImage> newPics, int startInc, int endExc) {
            try {
                for (; startInc < endExc; startInc++)
                    newPics.add(puzzleImages.get(startInc));
            } catch (NullPointerException | ArrayIndexOutOfBoundsException ex) {
                Log.e(TAG, "copying exception: ", ex);
            }
        }

        @Override
        public int getItemCount() {
            return puzzleImages.size();
        }

        /* -------- V I E W   H O L D E R ----------*/
        static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,
                View.OnLongClickListener {
            ImageView pictureIV;
            TextView date;
            ViewGroup label;
            PuzzleListAdapter adapter;

            ViewHolder(View itemView, PuzzleListAdapter adapter) {
                super(itemView);
                pictureIV = (ImageView) itemView.findViewById(R.id.gi_puzzPreviewIV);
                label = (ViewGroup) itemView.findViewById(R.id.list_row_label);
                date = (TextView) label.findViewById(R.id.list_item_dateTV);
                this.adapter = adapter;
                itemView.setOnClickListener(this);
                itemView.setOnLongClickListener(this);
            }

            @Override
            public void onClick(View v) {
                int pos = this.getAdapterPosition();
                adapter.onItemClicked(pos);
            }

            @Override
            public boolean onLongClick(View v) {
                adapter.onLongClick(getAdapterPosition());
                return true;
            }
        }
    }
}
