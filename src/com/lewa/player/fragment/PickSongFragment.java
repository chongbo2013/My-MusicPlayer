package com.lewa.player.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.lewa.Lewa;
import com.lewa.player.R;
import com.lewa.player.adapter.SongPickAdapter;
import com.lewa.player.db.DBService;
import com.lewa.player.listener.EditPlaylistListener;
import com.lewa.player.listener.PlayStatusBackgroundListener;
import com.lewa.player.model.Song;
import com.lewa.view.ClearEditText;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by wuzixiu on 12/14/13.
 */
public class PickSongFragment extends BaseFragment implements View.OnClickListener, TextWatcher {
    private static final String TAG = "PickSongFragment";//.class.getName();

    ImageButton mBackBtn;
    ImageButton mSelectBtn;
    LinearLayout mSaveBtn;
    ClearEditText mSearchEt;
    ListView mSongLv;
    ImageView mCoverIv;
    private boolean mSelectAll = false;
    private SongPickAdapter mSongPickAdapter;
    private EditPlaylistListener mEditPlaylistListener;
    List<Song> mSongs = null;
    List<Song> mAllSongs = null;

    public PickSongFragment() {
    }

    public static PickSongFragment newInstance() {
        return new PickSongFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_pick_song, container, false);
        mBackBtn = (ImageButton) rootView.findViewById(R.id.bt_back);
        mBackBtn.setOnClickListener(this);
        mSelectBtn = (ImageButton) rootView.findViewById(R.id.bt_select_all);
        mSelectBtn.setOnClickListener(this);
        mSaveBtn = (LinearLayout) rootView.findViewById(R.id.bt_add_song);
        mSaveBtn.setOnClickListener(this);
        mSearchEt = (ClearEditText) rootView.findViewById(R.id.et_search);
        mSongLv = (ListView) rootView.findViewById(R.id.lv_song);
        mCoverIv = (ImageView) rootView.findViewById(R.id.iv_cover);

        mSongPickAdapter = new SongPickAdapter(mEditPlaylistListener);
        mSongLv.setAdapter(mSongPickAdapter);
        mSongLv.setOnItemClickListener(mSongPickAdapter);
        mSearchEt.addTextChangedListener(this);

        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mEditPlaylistListener = (EditPlaylistListener) activity;
        } catch (ClassCastException cce) {
            Log.e(TAG, "Activity should implement EditPlaylistListener.");
        }
    }

    @Override
    public void onResume() {
        mPlayStatusListener = new PlayStatusBackgroundListener(OnlinePlaylistFragment.class.getName(), mCoverIv);
        super.onResume();
        Lewa.getAndBlurCurrentCoverUrl(mPlayStatusListener);

        try {
            mSongs = DBService.loadAllSongs();
            mAllSongs = mSongs;
            mSongs.removeAll(mEditPlaylistListener.getPickedSongs());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        mSongPickAdapter.setData(mSongs);
        mEditPlaylistListener.toggleSong(null);
    }

    public void onHiddenChanged(boolean hidden) {
        if (!hidden) {
            mSongs.removeAll(mEditPlaylistListener.getPickedSongs());
            mSongPickAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_back:
                Log.v(TAG, "Hide PickSongFragment.");
                mEditPlaylistListener.hidePickSongFragment();
                hideKeyboard();
                break;
            case R.id.bt_select_all:
                if (!mSelectAll) {
                    mSelectBtn.setImageResource(R.drawable.se_btn_unselect_all);
                    mEditPlaylistListener.selectAll(mSongs);
                    mSongPickAdapter.notifyDataSetChanged();
                    mSelectAll = true;
                } else {
                    mSelectBtn.setImageResource(R.drawable.se_btn_select_all);
                    mEditPlaylistListener.unSelectAll();
                    mSongPickAdapter.notifyDataSetChanged();
                    mSelectAll = false;
                }
                break;
            case R.id.bt_add_song:
                Log.v(TAG, "Do pick.");
                mSelectAll = false;
                mEditPlaylistListener.doPick();
                mEditPlaylistListener.hidePickSongFragment();
                break;
            case R.id.cb_select:
                Song song = (Song) v.getTag(R.id.tag_entity);
                mEditPlaylistListener.toggleSong(song);
                break;
        }
    }

    public void toggleSelectState(int selectedSongSize) {
        if(mSongs.size() == selectedSongSize && 0 != selectedSongSize) {
            mSelectAll = true; 
            mSelectBtn.setImageResource(R.drawable.se_btn_unselect_all);
        } else {
            mSelectAll = false;
            mSelectBtn.setImageResource(R.drawable.se_btn_select_all);
        }

    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count,
                                  int after) {

    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        mSearchEt.setClearIconVisible(s.length() > 0);
        try {
            if (s.length() > 0) {
                mSongs = DBService.findSongsByName(s.toString());
            } else {
                mSongs = mAllSongs;
            }
            mSongPickAdapter.setData(mSongs);
            mSongPickAdapter.notifyDataSetChanged();
        } catch (SQLException e) {

        }
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mSearchEt.getWindowToken(), 0);
    }

}
