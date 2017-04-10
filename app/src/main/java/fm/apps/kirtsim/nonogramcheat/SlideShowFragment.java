package fm.apps.kirtsim.nonogramcheat;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
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
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.io.File;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import db.DbHelperSingleton;
import db.PuzzleImage;


/**
 * A simple {@link Fragment} subclass.
 */
public class SlideShowFragment extends DialogFragment {
    public static final String TAG = SlideShowFragment.class.getSimpleName();
    private static final String PICTURES_PARAM = "SlideShowFragment.pictures";
    private static final String PIC_POSITION_PARAM = "SlideShowFragment.pos";

    private SlideShowFragmentListener listener;
    private Date date;
    private SimpleDateFormat dateFormat;
    private ViewPager viewPager;
    private MyVPAdapter adapter;
    private TextView labelCount, labelDate;
    private int currentItemIndex;

    interface SlideShowFragmentListener {
        void onPresentationFinished();
    }

    public SlideShowFragment() {}

    public static SlideShowFragment newInstance(ArrayList<PuzzleImage> puzzles, int pos) {
        SlideShowFragment fragment = new SlideShowFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(PICTURES_PARAM, puzzles);
        bundle.putInt(PIC_POSITION_PARAM, pos);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initHelperObjectsIfNull();
        ArrayList<PuzzleImage> puzzles = getPuzzlesFromArgs(getArguments());
        adapter = new MyVPAdapter(this, puzzles);
    }

    private void initHelperObjectsIfNull() {
        if (date == null)
            date = new Date();
        if (dateFormat == null)
            dateFormat = new SimpleDateFormat("dd/MM/yyyy  hh:mm:ss", Locale.getDefault());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_slide_show, container, false);
        int position = getPositionFromBundle(savedInstanceState != null ? savedInstanceState : getArguments());
        labelCount = (TextView) root.findViewById(R.id.labelCount);
        labelDate = (TextView) root.findViewById(R.id.labelDate);
        viewPager = (ViewPager) root.findViewById(R.id.viewPager);
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new PageChangeListener(this));
        viewPager.setCurrentItem(position);

        displayPuzzleInfo(position);
        setHasOptionsMenu(true);
        return root;
    }

    private int getPositionFromBundle(Bundle bundle) {
        if (bundle != null)
            return bundle.getInt(PIC_POSITION_PARAM, 0);
        return 0;
    }

    private ArrayList<PuzzleImage> getPuzzlesFromArgs(Bundle args) {
        ArrayList<PuzzleImage> puzzles;
        if (args != null) {
            puzzles = args.getParcelableArrayList(PICTURES_PARAM);
        } else
            puzzles = new ArrayList<>(0);
        return puzzles;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        View root = getView();
        if (root != null) {
            ViewPager pager = (ViewPager) root.findViewById(R.id.viewPager);
            outState.putInt(PIC_POSITION_PARAM, pager.getCurrentItem());
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof SlideShowFragmentListener)
            listener = (SlideShowFragmentListener) context;
        else
            throw new IllegalArgumentException("must implement " + SlideShowFragmentListener.class.getSimpleName());
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
        viewPager = null;
        labelDate = labelCount = null;
        adapter = null;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        final boolean ENABLE = true;
        super.onCreateOptionsMenu(menu, inflater);
        if (adapter != null && adapter.puzzles.size() > 0) {
            enableMenuItem(menu.findItem(R.id.delete_menu_item), ENABLE);
            enableMenuItem(menu.findItem(R.id.share_menu_item1), ENABLE);
        }
    }

    private void enableMenuItem(MenuItem item, boolean enable) {
        if (item != null) {
            item.setVisible(enable);
            item.setEnabled(enable);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.share_menu_item1:
                sharePicture();
                return true;
            case R.id.delete_menu_item:
                deletePicture();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void sharePicture() {
        try {
            File location = new File(adapter.getPuzzleAt(currentItemIndex).getStoreLocation());
            if (location.exists()) {
                Uri uri = FileProvider.getUriForFile(getContext(), "fm.apps.kirtsim.nonogramcheat.fileprovider", location);
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("image/png");
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.putExtra(Intent.EXTRA_STREAM, uri);
                startActivity(Intent.createChooser(intent, "Choose an app"));
                return;
            }
        } catch (Exception e) { Log.e(TAG, "exception sharing", e); }
        Toast.makeText(getContext(), "could not find image", Toast.LENGTH_SHORT).show();
    }

    private void deletePicture() {
        PuzzleImage pic = adapter.getPuzzleAt(currentItemIndex);
        if (deleteFile(pic.getStoreLocation())) {
            DbHelperSingleton dbHelper = DbHelperSingleton.getInstance(getContext());
            dbHelper.deletePuzzlePic(pic);
            Toast.makeText(getContext(), "Puzzle deleted", Toast.LENGTH_SHORT).show();
            adapter.deletePuzzleAt(currentItemIndex);
            resolveChangesAfterDeletion();
        } else
            Toast.makeText(getContext(), "Could not delete puzzle", Toast.LENGTH_SHORT).show();
    }

    private boolean deleteFile(String location) {
        File file = new File(location);
        return file.exists() && file.delete();
    }

    private void resolveChangesAfterDeletion() {
        final int count = adapter.puzzles.size();
        if (count == 0)
            finishPresentation();
        if (currentItemIndex == count)
            currentItemIndex--;
        displayPuzzleInfo(currentItemIndex);
    }

    private void finishPresentation() {
        if (listener != null)
            listener.onPresentationFinished();
    }

    private void displayPuzzleInfo(int position) {
        date.setTime(adapter.getPuzzleAt(position).getSaveDate());
        currentItemIndex = position;
        String stringDate = dateFormat.format(date);
        String countText = ++position + " of " + adapter.getCount();
        labelCount.setText(countText);
        labelDate.setText(stringDate);
        labelCount.bringToFront();
        labelDate.bringToFront();
    }




    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
    *  ----------------------- P A G E   L I S T E N E R ------------------------------
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    private static class PageChangeListener implements ViewPager.OnPageChangeListener {

        WeakReference<SlideShowFragment> fragmentRef;

        PageChangeListener(SlideShowFragment fragmentRef) {
            super();
            this.fragmentRef = new WeakReference<>(fragmentRef);
        }

        @Override
        public void onPageSelected(int position) {
            fragmentRef.get().displayPuzzleInfo(position);
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}
        @Override
        public void onPageScrollStateChanged(int state) {}
    }




    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
    *  -----------------------       A D A P T E R       ------------------------------
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    private static class MyVPAdapter extends PagerAdapter {
        private WeakReference<SlideShowFragment> fragment;
        private ArrayList<PuzzleImage> puzzles;
        private LayoutInflater layoutInflater;


        MyVPAdapter(SlideShowFragment fragment) {
            super();
            this.fragment = new WeakReference<>(fragment);
            layoutInflater = (LayoutInflater) fragment.getContext().
                    getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        MyVPAdapter(SlideShowFragment fragment, ArrayList<PuzzleImage> puzzles) {
            this(fragment);
            this.puzzles = puzzles;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View view = layoutInflater.inflate(R.layout.image_full_screen, container, false);
            ImageView imageV = (ImageView) view.findViewById(R.id.full_image_iv);
            PuzzleImage puzzleI = puzzles.get(position);
            Glide.with(fragment.get().getContext())
                    .load(puzzleI.getStoreLocation())
                    .crossFade()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(imageV);
            container.addView(view);
            return view;
        }

        @SuppressWarnings("SuspiciousMethodCalls")
        @Override
        public int getItemPosition(Object object) {
            final int index = puzzles.indexOf(object);
            return index != -1 ? index : POSITION_NONE;
        }

        PuzzleImage getPuzzleAt(int position) {
            if (position >= 0 && position < puzzles.size())
                return puzzles.get(position);
            return new PuzzleImage();
        }

        PuzzleImage deletePuzzleAt(int position) {
            if (position >= 0 && position < puzzles.size()) {
                PuzzleImage pic = puzzles.remove(position);
                this.notifyDataSetChanged();
                return pic;
            }
            return new PuzzleImage();
        }

        @Override
        public int getCount() {
            return puzzles.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }
    }

}
