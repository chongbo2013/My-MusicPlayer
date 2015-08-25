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

import com.baidu.music.model.Music;
import com.baidu.music.model.Topic;
import com.lewa.Lewa;
import com.lewa.il.MusicInterfaceLayer;
import com.lewa.player.R;
import com.lewa.player.adapter.OnlinePlaylistAdapter;
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
public class OnlinePlaylistFragment extends BaseFragment implements AdapterView.OnItemClickListener, View.OnClickListener {
    private static final String TAG = OnlinePlaylistFragment.class.getName();
    MaskEndListView mPlaylistLv;
    ImageButton mBackBtn;
    ImageView mCoverIv;

    private OnlinePlaylistAdapter mAdapter;

    static List<Playlist> playlists = new ArrayList<Playlist>();
    private LibraryListener mLibraryListener;
    private boolean dataRequested = false;

    public OnlinePlaylistFragment() {

    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_online_playlist, container, false);
        mCoverIv = (ImageView) rootView.findViewById(R.id.iv_cover);
        mBackBtn = (ImageButton) rootView.findViewById(R.id.bt_back);
        mBackBtn.setOnClickListener(this);
        mPlaylistLv = (MaskEndListView) rootView.findViewById(R.id.lv);
        mAdapter = new OnlinePlaylistAdapter(this, playlists);
        mPlaylistLv.setAdapter(mAdapter);
        mPlaylistLv.setOnItemClickListener(this);
        mPlaylistLv.setOnTrackListener(mLibraryListener);

        return rootView;
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
    public void onResume() {
        mPlayStatusListener = new PlayStatusBackgroundListener(OnlinePlaylistFragment.class.getName(), mCoverIv);
        super.onResume();

        Lewa.getAndBlurCurrentCoverUrl(mPlayStatusListener);

        if (!dataRequested) {
            Log.i(TAG, "Get playlists.");
            try {
                MusicInterfaceLayer.getInstance().requestHotAlbum(getActivity(), 100, new MusicInterfaceLayer.OnGetTopicListListener() {
					
					@Override
					public void onGetTopicList(List<Topic> lists) {
						// TODO Auto-generated method stub
						 Log.i(TAG, "Got hot albums: " + (lists == null ? 0 : lists.size()));
	                        playlists.clear();
	                        if (lists != null) {
	                            for (Topic data : lists) {
	                                playlists.add(Playlist.fromBdHotAlbum(data));
	                            }
//	                            mAdapter.setData(playlists);
	                        }
	                        mAdapter.notifyDataSetChanged();

	                        dataRequested = true;
					}
				}); 
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "Failed to query playlists: \n" + e.getMessage());
            }
        } else {
            Log.i(TAG, "Data requested, just refresh.");
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, final int position, long id) {
       /*mLibraryListener.showSongList(view, new SongCollection(SongCollection.Type.PLAYLIST, view.getTag(R.id.tag_entity)), new CallbackListener() {
            @Override
            public void doStuffAfterOpenSubWindow() {
                //hide other play btn
                mAdapter.openSubWindow(position);
            }

            @Override
            public void doSutffAfterCloseSubWindow() {
                mAdapter.closeSubWindow();
            }
        });*/
        Playlist playlist = (Playlist)view.getTag(R.id.tag_entity);
        
        mLibraryListener.showSongInfoListFragment(playlist.getName(), new SongCollection(SongCollection.Type.PLAYLIST, playlist));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_back:
                mLibraryListener.hideSongList();
                mLibraryListener.hideTrackFragment();
                break;
            case R.id.bt_play:
                mAdapter.reset();
                final Playlist playlist = (Playlist) v.getTag(R.id.tag_entity);
                prepareForPlay(new CallbackPlayListener() {
                    @Override
                    public void execute() {
                        PlaylistHelper.getOnlinePlaylistSongs(getActivity(), playlist, new PlaylistHelper.GetSongsListener() {
                            @Override
                            public void onGotSongs(Playlist filledPlaylist) {
                                Lewa.playerServiceConnector().playSongCollection(getActivity(), new SongCollection(SongCollection.Type.PLAYLIST, filledPlaylist), -1);
                            }
                        });
                    }
                });

                break;
        }
    }

}
