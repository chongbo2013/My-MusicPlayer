package com.lewa.player.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.lewa.Lewa;
import com.lewa.player.R;
import com.lewa.player.adapter.TopListAdapter;
import com.lewa.player.helper.PlaylistHelper;
import com.lewa.player.listener.CallbackListener;
import com.lewa.player.listener.CallbackPlayListener;
import com.lewa.player.listener.LibraryListener;
import com.lewa.player.listener.PlayStatusBackgroundListener;
import com.lewa.player.model.Playlist;
import com.lewa.player.model.SongCollection;
import com.lewa.view.MaskEndListView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wuzixiu on 11/27/13.
 */
public class TopListFragment extends BaseFragment implements View.OnClickListener, AdapterView.OnItemClickListener {
    private static final String TAG = TopListFragment.class.getName();
    private ImageButton mBackBtn;
    private MaskEndListView mTopListLv;
    private TopListAdapter mAdapter;
    ImageView mCoverIv;

    private LibraryListener mLibraryListener;

    public TopListFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_top_list, container, false);
        initViews(rootView);
        return rootView;
    }

    private void initViews(View rootView) {
        mBackBtn = (ImageButton) rootView.findViewById(R.id.bt_back);
        mTopListLv = (MaskEndListView) rootView.findViewById(R.id.lv_top_list);
        mCoverIv = (ImageView) rootView.findViewById(R.id.iv_cover);
        mAdapter = new TopListAdapter(this);
        mTopListLv.setAdapter(mAdapter);
        mBackBtn.setOnClickListener(this);
        mTopListLv.setOnItemClickListener(this);
        mTopListLv.setOnTrackListener(mLibraryListener);

    }

    @Override
    public void onResume() {
        mPlayStatusListener = new PlayStatusBackgroundListener(OnlinePlaylistFragment.class.getName(), mCoverIv);
        super.onResume();
        Lewa.getAndBlurCurrentCoverUrl(mPlayStatusListener);

        List<Playlist> playlists = new ArrayList<Playlist>();
        Playlist newCountDown = new Playlist(7l, Lewa.string(R.string.new_songs_list), Playlist.TYPE.TOP_LIST_NEW, 1, 1, null);
        Playlist hotCountdown = new Playlist(8l, Lewa.string(R.string.hot_songs_list), Playlist.TYPE.TOP_LIST_HOT, 2, 2, null);
        playlists.add(newCountDown);
        playlists.add(hotCountdown);
        mAdapter.setData(playlists);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mLibraryListener = (LibraryListener) activity;
        } catch (ClassCastException cce) {
            Log.e(TAG, "Activity should implement LibraryListener.");
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, final int position, long item) {
        Playlist topList = (Playlist) view.getTag(R.id.tag_entity);
        if(null != topList){
        	mLibraryListener.showSongInfoListFragment(topList.getName(), new SongCollection(SongCollection.Type.PLAYLIST, topList));
        }
    
        /*mLibraryListener.showSongList(view, new SongCollection(SongCollection.Type.PLAYLIST, topList), new CallbackListener() {
            @Override
            public void doStuffAfterOpenSubWindow() {
                mAdapter.openSubWindow(position);
            }

            @Override
            public void doSutffAfterCloseSubWindow() {
                mAdapter.closeSubWindow();
            }
        });*/
        
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_back:
                mLibraryListener.hideSongList();
                mLibraryListener.hideTopListFragment();
                break;
            case R.id.bt_play:
                mAdapter.reset();
                final Playlist toplist = (Playlist) v.getTag(R.id.tag_entity);

                prepareForPlay(new CallbackPlayListener() {
                    @Override
                    public void execute() {
                        if (toplist.getType() == Playlist.TYPE.TOP_LIST_NEW) {
                            PlaylistHelper.getTopListNewSongs(getActivity(), toplist, new PlaylistHelper.GetSongsListener() {
                                @Override
                                public void onGotSongs(Playlist filledPlaylist) {
                                    Lewa.playerServiceConnector().playSongCollection(getActivity(), new SongCollection(SongCollection.Type.PLAYLIST, filledPlaylist), -1);
                                }
                            });
                        } else if (toplist.getType() == Playlist.TYPE.TOP_LIST_HOT) {
                            PlaylistHelper.getTopListHotSongs(getActivity(), toplist, new PlaylistHelper.GetSongsListener() {
                                @Override
                                public void onGotSongs(Playlist filledPlaylist) {
                                    Lewa.playerServiceConnector().playSongCollection(getActivity(), new SongCollection(SongCollection.Type.PLAYLIST, filledPlaylist), -1);
                                }
                            });
                        }
                    }
                });
                break;
        }
    }
}
