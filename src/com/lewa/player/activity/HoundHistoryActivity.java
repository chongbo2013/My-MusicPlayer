package com.lewa.player.activity;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ListView;
import lewa.support.v7.app.ActionBar;

import com.lewa.player.R;
import com.lewa.player.adapter.SongAdapter;
import com.lewa.player.db.DBService;
import com.lewa.player.model.Song;

import java.sql.SQLException;
import java.util.List;


public class HoundHistoryActivity extends BaseActivity implements View.OnClickListener {

    private ImageButton backBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        hasBackground = true;
        setContentView(R.layout.activity_song_hound_histoty);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        backBtn = (ImageButton) findViewById(R.id.bt_back);
        backBtn.setOnClickListener(this);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new HoundHistoryFragment())
                    .commit();
        }
    }

    @Override
    public void onClick(View v) {
        int vid = v.getId();

        switch (vid) {
            case R.id.bt_back:
                this.finish();
                break;
        }
    }

    /**
     * A song hound fragment containing a simple view.
     */
    public static class HoundHistoryFragment extends Fragment implements View.OnClickListener {
        private ListView historyLv;
        private SongAdapter songAdapter;

        public HoundHistoryFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_hound_history, container, false);
            historyLv = (ListView) rootView.findViewById(R.id.lv_song_history);
            List<Song> songs = null;

            try {
                songs = DBService.loadAllSongs();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            songAdapter = new SongAdapter();
            historyLv.setAdapter(songAdapter);
//            songAdapter.setData(songs, null);

            return rootView;
        }

        @Override
        public void onClick(View v) {
            int vid = v.getId();

            switch (vid) {

            }
        }

    }

}