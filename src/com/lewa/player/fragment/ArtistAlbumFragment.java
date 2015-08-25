package com.lewa.player.fragment;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.lewa.Lewa;
import com.lewa.kit.ActivityHelper;
import com.lewa.player.MusicUtils;
import com.lewa.player.R;
import com.lewa.player.adapter.AlbumAdapter;
import com.lewa.player.db.DBService;
import com.lewa.player.listener.AlbumListener;
import com.lewa.player.listener.LibraryListener;
import com.lewa.player.listener.CallbackListener;
import com.lewa.player.listener.PlayStatusBackgroundListener;
import com.lewa.player.model.Album;
import com.lewa.player.model.AlbumCursorIndex;
import com.lewa.player.model.Artist;
import com.lewa.player.model.Playlist;
import com.lewa.player.model.Song;
import com.lewa.player.model.SongCollection;
import com.lewa.util.StringUtils;
import com.lewa.view.MaskEndListView;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by wuzixiu on 11/27/13.
 */
public class ArtistAlbumFragment extends BaseFragment implements View.OnClickListener, AdapterView.OnItemClickListener {
    private static final String TAG = "ArtistAlbumFragment"; //.class.getName();

    public static final String ARG_ARTIST_ID = "artistId";
    public static final String ARG_ARTIST_NAME = "artistName";
    public static final String ARG_IS_ONLINE = "isOnline";

    ImageButton mBackBtn;
    ImageView mCoverIv;
    TextView mArtistNameTv;
    TextView mNoAlbumTv;
    MaskEndListView mAlbumLv;
    private Cursor mAlbumCursor;
    private AlbumAdapter mAdapter;
    private List<Album> mAlbums;
    private AlbumListener mAlbumListener;
    private Long artistId;


    public ArtistAlbumFragment() {
    }

    public static ArtistAlbumFragment newInstance(Long artistId, String artistName, boolean isOnline) {
        ArtistAlbumFragment artistAlbumFragment = new ArtistAlbumFragment();
        Bundle args = new Bundle();

        if (!StringUtils.isBlank(artistName)) {
            args.putString(ARG_ARTIST_NAME, artistName);
        }
        if (artistId != null) {
            args.putLong(ARG_ARTIST_ID, artistId);
        }
        args.putBoolean(ARG_IS_ONLINE, isOnline);

        artistAlbumFragment.setArguments(args);

        return artistAlbumFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_album_list, container, false);
        mBackBtn = (ImageButton) rootView.findViewById(R.id.bt_back);
        mArtistNameTv = (TextView) rootView.findViewById(R.id.tv_artist_name);
        mCoverIv = (ImageView) rootView.findViewById(R.id.iv_cover);
        mAlbumLv = (MaskEndListView) rootView.findViewById(R.id.lv_album);
        mNoAlbumTv = (TextView) rootView.findViewById(R.id.llo1);

        mAdapter = new AlbumAdapter(this);
        mAlbumLv.setAdapter(mAdapter);
        mAlbumLv.setOnItemClickListener(this);
        mBackBtn.setOnClickListener(this);
        mAlbumLv.setOnTrackListener(mAlbumListener);

        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mAlbumListener = (AlbumListener) activity;
        } catch (ClassCastException cce) {
            Log.e(TAG, "Activity should implement LibraryListener.");
        }
    }

    @Override
    public void onResume() {
        mPlayStatusListener = new PlayStatusBackgroundListener(ArtistAlbumFragment.class.getName(), mCoverIv);
        super.onResume();
        Lewa.getAndBlurCurrentCoverUrl(mPlayStatusListener);

        refreshAlbumsData();
        String artistName = getArguments().getString("artistName");
		if (MediaStore.UNKNOWN_STRING.equals(artistName)) {
			mArtistNameTv.setText(getString(
					R.string.unknown_artist_name));
		} else {
			mArtistNameTv.setText(artistName);
		}
    }

    public void refreshAlbumsData() {
        artistId = getArguments().getLong(ARG_ARTIST_ID);
        String artistName = getArguments().getString(ARG_ARTIST_NAME);
        boolean isOnline = getArguments().getBoolean(ARG_IS_ONLINE);

        try {
            if (isOnline) {
                if (StringUtils.isBlank(artistName)) return;

                Artist artist = DBService.findArtistByName(artistName);

                if (artist != null) {
                    artistId = artist.getId();
                }
            }

            if (mAlbumCursor != null && !mAlbumCursor.isClosed()) {
                mAlbumCursor.close();
            }
            
            mAlbums = new ArrayList<Album>();
            
            if (artistId != null && artistId > 0) {
                mAlbumCursor = DBService.loadAlbumsOfArtist(artistId);

                if (mAlbumCursor != null && mAlbumCursor.getCount() > 0) {
                    mAlbumCursor.moveToFirst();
                    while (!mAlbumCursor.isAfterLast()) {
                        AlbumCursorIndex cursorIndex = new AlbumCursorIndex(mAlbumCursor);
                        Album album = Album.fromCursor(mAlbumCursor, cursorIndex);
                        int songNum = DBService.loadSongCountOfAlbum(album.getId(), artistName);
                        if (songNum > 0) {
                            album.setSongNum(songNum);
                            mAlbums.add(album);
                        }
                        mAlbumCursor.moveToNext();
                    }
                }
                
                if (mAlbumCursor != null && !mAlbumCursor.isClosed()) {
                    mAlbumCursor.close();
                }
                
                int totalSongNum = DBService.loadSongCountOfArtist(artistId);
                if(totalSongNum > 0) {  //init all song item of artist
                    Album album = new Album();
                    album.setId(0l);
                    album.setSongNum(totalSongNum);
                    album.setName(getResources().getString(R.string.text_all_local_songs));
                    mAlbums.add(0, album);
                }
            } else if (artistId != null && artistId  ==0) { //all albums
                List<Song> songs = DBService.loadAllSongs();
                Map<Long, Integer> mAlbumMap = new HashMap<Long, Integer>();
                for(Song song : songs) {
                    if(mAlbumMap.containsKey(song.getAlbum().getId())) {
                        mAlbumMap.put(song.getAlbum().getId(), mAlbumMap.get(song.getAlbum().getId()) + 1);
                    } else {
                        mAlbumMap.put(song.getAlbum().getId(), 1);
                    }
                }
                
                mAlbumCursor = DBService.loadAlbums();

                mAlbums = new ArrayList<Album>();
                if(mAlbumCursor!=null){
                    mAlbumCursor.moveToFirst();
                    while(!mAlbumCursor.isAfterLast()) {
                        AlbumCursorIndex cursorIndex = new AlbumCursorIndex(mAlbumCursor);
                        Album album = Album.fromCursor(mAlbumCursor, cursorIndex);
                        int songNum = mAlbumMap.containsKey(album.getId()) ? mAlbumMap.get(album.getId()) : 0;
                        if(songNum > 0) {
                            album.setSongNum(songNum);
                            mAlbums.add(album);
                        }
                        mAlbumCursor.moveToNext();
                    }
                    mAlbumCursor.close();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        if(mAlbums.size() == 0) {
            mNoAlbumTv.setVisibility(View.VISIBLE);
            mAlbumLv.setVisibility(View.GONE);
        } else {
            mNoAlbumTv.setVisibility(View.GONE);
            mAdapter.setData(mAlbums);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, final int position, long item) {
        Album album = (Album) view.getTag(R.id.tag_entity);
        if(null == album) {
            return;
        }
        
        if(null == album.getId() || 0 == album.getId()) {
            String artistName = getArguments().getString(ARG_ARTIST_NAME);
            SongCollection songCollection = new SongCollection(SongCollection.Type.PLAYLIST, new Playlist(new Artist(artistId, artistName)));
            mAlbumListener.showSongInfoListFragment(artistName, songCollection);
        } else{
            mAlbumListener.showSongInfoListFragment(album.getName(), new SongCollection(SongCollection.Type.ALBUM, album));
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bt_back:
                //mAlbumListener.hideSongList();
                mAlbumListener.onBack();
                break;
            case R.id.bt_play:
                mAdapter.reset();
                Album album = (Album) view.getTag(R.id.tag_entity);

                if (album.getId() == null || album.getId() == 0) {
                    String artistName = getArguments().getString(ARG_ARTIST_NAME);
                    Lewa.playerServiceConnector().playSongCollection(getActivity(), new SongCollection(SongCollection.Type.PLAYLIST, new Playlist(new Artist(artistId, artistName))), 0);
                } else {
                    Lewa.playerServiceConnector().playSongCollection(getActivity(), new SongCollection(SongCollection.Type.ALBUM, album), 0);
                }
                break;
        }
    }

    @Override
    public void onStop() {

        super.onStop();
    }

    @Override
    public void onDestroy() {
        if (mAlbumCursor != null) {
            mAlbumCursor.close();
        }

        super.onDestroy();
    }
}
