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

import com.baidu.music.model.Artist;
import com.lewa.Lewa;
import com.lewa.il.MusicInterfaceLayer;
import com.lewa.player.R;
import com.lewa.player.adapter.AllStarAdapter;
import com.lewa.player.helper.PlaylistHelper;
import com.lewa.player.listener.CallbackListener;
import com.lewa.player.listener.CallbackPlayListener;
import com.lewa.player.listener.LibraryListener;
import com.lewa.player.listener.PlayStatusBackgroundListener;
import com.lewa.player.model.Pagination;
import com.lewa.player.model.Playlist;
import com.lewa.player.model.SongCollection;
import com.lewa.view.MaskEndListView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wuzixiu on 11/27/13.
 */
public class AllStarFragment extends BaseFragment implements View.OnClickListener, AdapterView.OnItemClickListener {
    private static final String TAG = AllStarFragment.class.getName();
    private ImageButton mBackBtn;
    private MaskEndListView mLv;
    private AllStarAdapter mAllStarAdapter;
    private LibraryListener mLibraryListener;
    ImageView mCoverIv;
    private boolean dataRequested = false;
    static List<Playlist> mPlaylists = new ArrayList<Playlist>();

    public AllStarFragment() {
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAllStarAdapter = new AllStarAdapter(this, mPlaylists);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_library_all_star, container, false);
        mBackBtn = (ImageButton) rootView.findViewById(R.id.bt_back);
        mLv = (MaskEndListView) rootView.findViewById(R.id.lv);
        mCoverIv = (ImageView) rootView.findViewById(R.id.iv_cover);
        mLv.setAdapter(mAllStarAdapter);
        mBackBtn.setOnClickListener(this);
        mLv.setOnTrackListener(mLibraryListener);
        mLv.setOnItemClickListener(this);

        return rootView;
    }

    @Override
    public void onResume() {
        mPlayStatusListener = new PlayStatusBackgroundListener(OnlinePlaylistFragment.class.getName(), mCoverIv);
        super.onResume();
        Lewa.getAndBlurCurrentCoverUrl(mPlayStatusListener);

        if (!dataRequested) {
            MusicInterfaceLayer.getInstance().requestHotSinger(getActivity(), Pagination.DEFAULT_PAGE_SIZE, new MusicInterfaceLayer.OnGetHotArtistListListener() {
				
				@Override
				public void onGetHotArtistList(List<Artist> artists) {
					// TODO Auto-generated method stub
				    Log.i(TAG, "Got all stars: " + (artists == null ? 0 : artists.size()));
                    mPlaylists.clear();
                    if (artists != null) {
                        for (Artist itemData : artists) {
                            mPlaylists.add(Playlist.fromBdHotArtist(itemData));
                        }
                    }
                    mAllStarAdapter.notifyDataSetChanged();

                    dataRequested = true;
				}
			}); 
        } else {
            Log.i(TAG, "Data requested, just refresh.");
            mAllStarAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, final int position, long item) {
        Playlist playlist = (Playlist) view.getTag(R.id.tag_entity);
        mLibraryListener.showSongInfoListFragment(playlist.getName(), new SongCollection(SongCollection.Type.PLAYLIST, playlist));
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
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_back:
                mLibraryListener.hideSongList();
                mLibraryListener.hideAllStarFragment();
                break;
            case R.id.bt_play:
//                mAllStarAdapter.reset();
                final Playlist playlist = (Playlist) v.getTag(R.id.tag_entity);
                prepareForPlay(new CallbackPlayListener() {
                    @Override
                    public void execute() {
                        PlaylistHelper.getAllStarSongs(getActivity(), playlist, new PlaylistHelper.GetSongsListener() {
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
