package com.lewa.player.activity;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import lewa.support.v7.app.ActionBar;

import com.lewa.player.R;


public class SongHoundActivity extends BaseActivity implements View.OnClickListener {

    private ImageButton backBtn;

    private ImageButton houndHisBtn;

    private static final int HOUND_FRAGMENT = 0;
    private static final int SUCCESS_FRAGMENT = 1;
    private static final int FAILED_FRAGMENT = 2;
    private static int curFRAGMENT = HOUND_FRAGMENT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        hasBackground = true;
        setContentView(R.layout.activity_song_hound);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        backBtn = (ImageButton) findViewById(R.id.bt_back);
        houndHisBtn = (ImageButton) findViewById(R.id.bt_hound_histoy);
        backBtn.setOnClickListener(this);
        houndHisBtn.setOnClickListener(this);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new HoundFragment())
                    .commit();
        }
    }

    @Override
    public void onClick(View v) {
        int vid = v.getId();

        switch (vid) {
            case R.id.bt_back:
                switchFragment();
                break;
            case R.id.bt_hound_histoy:
                Intent intent = new Intent(this, HoundHistoryActivity.class);
                startActivity(intent);
                break;
        }
    }

    private void switchFragment() {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        switch (curFRAGMENT) {
            case HOUND_FRAGMENT:
                //back to other activity
                break;
            case SUCCESS_FRAGMENT:
                transaction.replace(R.id.container, new HoundFragment());
                transaction.addToBackStack(null);
                transaction.commit();
                curFRAGMENT = HOUND_FRAGMENT;
                break;
            case FAILED_FRAGMENT:
                transaction.replace(R.id.container, new HoundFragment());
                transaction.addToBackStack(null);
                transaction.commit();
                curFRAGMENT = HOUND_FRAGMENT;
                break;
        }
    }

    /**
     * A song hound fragment containing a simple view.
     */
    public static class HoundFragment extends Fragment implements View.OnClickListener {
        private Button houndBtn;

        public HoundFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_hound_begin, container, false);
            houndBtn = (Button) rootView.findViewById(R.id.bt_hound);
            houndBtn.setOnClickListener(HoundFragment.this);

            return rootView;
        }

        @Override
        public void onClick(View v) {
            int vid = v.getId();

            switch (vid) {
                case R.id.bt_hound:
                    FragmentTransaction transaction = this.getActivity().getFragmentManager().beginTransaction();
                    transaction.replace(R.id.container, new HoundSuccessFragment());
                    transaction.addToBackStack(null);
                    transaction.commit();
                    curFRAGMENT = SUCCESS_FRAGMENT;
                    break;
            }
        }

    }

    /**
     * A song hound fragment containing a simple view.
     */
    public static class HoundSuccessFragment extends Fragment implements View.OnClickListener {
        private Button houndBtn;
        private ImageButton favoriteBtn;
        private ImageButton downloadBtn;
        private ImageButton lyricBtn;

        public HoundSuccessFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_hound_success, container, false);
            houndBtn = (Button) rootView.findViewById(R.id.bt_hound);
            favoriteBtn = (ImageButton) rootView.findViewById(R.id.bt_favorite);
            downloadBtn = (ImageButton) rootView.findViewById(R.id.bt_download);
            lyricBtn = (ImageButton) rootView.findViewById(R.id.bt_lyric);
            houndBtn.setOnClickListener(HoundSuccessFragment.this);
            favoriteBtn.setOnClickListener(HoundSuccessFragment.this);
            downloadBtn.setOnClickListener(HoundSuccessFragment.this);
            lyricBtn.setOnClickListener(HoundSuccessFragment.this);

            return rootView;
        }

        @Override
        public void onClick(View v) {
            int vid = v.getId();

            switch (vid) {
                case R.id.bt_hound:
                    FragmentTransaction transaction = this.getActivity().getFragmentManager().beginTransaction();
                    transaction.replace(R.id.container, new HoundFragment());
                    transaction.addToBackStack(null);
                    transaction.commit();
                    curFRAGMENT = HOUND_FRAGMENT;
                    break;
            }
        }

    }

    /**
     * A song hound fragment failed containing a simple view.
     */
    public static class HoundFailedFragment extends Fragment implements View.OnClickListener {
        private Button rehoundBtn;

        public HoundFailedFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_hound_failed, container, false);
            rehoundBtn = (Button) rootView.findViewById(R.id.bt_hound);
            rehoundBtn.setOnClickListener(HoundFailedFragment.this);

            return rootView;
        }

        @Override
        public void onClick(View v) {
            int vid = v.getId();

            switch (vid) {
                case R.id.bt_hound:
                    FragmentTransaction transaction = this.getActivity().getFragmentManager().beginTransaction();
                    transaction.replace(R.id.container, new HoundFragment());
                    transaction.addToBackStack(null);
                    transaction.commit();
                    curFRAGMENT = HOUND_FRAGMENT;
                    break;
            }
        }

    }

}