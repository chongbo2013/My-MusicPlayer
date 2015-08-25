package com.lewa.player.fragment;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.TextView;

import com.lewa.Lewa;
import com.lewa.player.R;
import com.lewa.player.adapter.AlbumAdapter;
import com.lewa.player.db.DBService;
import com.lewa.player.fragment.SearchFragment.SearchType;
import com.lewa.player.listener.CallbackListener;
import com.lewa.player.listener.LibraryListener;
import com.lewa.player.model.Album;
import com.lewa.player.model.AlbumCursorIndex;
import com.lewa.player.model.Playlist;
import com.lewa.player.model.Song;
import com.lewa.player.model.SongCollection;
import com.lewa.view.ClearEditText;
import com.lewa.view.MaskEndListView;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by wuzixiu on 11/27/13.
 */
public class LibraryAlbumFragment extends Fragment implements View.OnClickListener, AdapterView.OnItemClickListener, View.OnFocusChangeListener {
    private static final String TAG = ArtistAlbumFragment.class.getName();

    ImageButton mBackBtn;
    TextView mArtistNameTv;
    MaskEndListView mAlbumLv;
    ClearEditText searchEt;

    private AlbumAdapter mAdapter;
    private Cursor mAlbumCursor;
    private List<Album> mAlbums;
    private LibraryListener mLibraryListener;

    private Map<Long, Integer> mAlbumMap;

    public LibraryAlbumFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_lv, container, false);
        mBackBtn = (ImageButton) rootView.findViewById(R.id.bt_back);
        mArtistNameTv = (TextView) rootView.findViewById(R.id.tv_artist_name);
        mAlbumLv = (MaskEndListView) rootView.findViewById(R.id.lv);

        //View searchBar = inflater.inflate(R.layout.search_bar, null, false);
        //searchEt = (ClearEditText) searchBar.findViewById(R.id.et_search);
        //searchEt.setOnFocusChangeListener(this);
        //mAlbumLv.addHeaderView(searchBar);
        mAdapter = new AlbumAdapter(this);
        mAlbumLv.setAdapter(mAdapter);
//        mAlbumLv.setSelection(1);
        mAlbumLv.setOnItemClickListener(this);
        mAlbumLv.setOnTrackListener(mLibraryListener);

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
        super.onResume();

        try {
            if (mAlbumCursor != null && !mAlbumCursor.isClosed()) {
                mAlbumCursor.close();
            }
            List<Song> songs = DBService.loadAllSongs();
            mAlbumMap = new HashMap<Long, Integer>();
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
            Album album = new Album();
            album.setId(0l);
            album.setSongNum(DBService.countSongs());
            album.setName(getResources().getString(R.string.text_all_local_songs));
            mAlbums.add(0, album);
            mAdapter.setData(mAlbums);
        } catch (SQLException e) {
            e.printStackTrace();
            Log.e(TAG, "Failed to query albums: \n" + e.getMessage());
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, final int position, long item) {
        Album album = (Album) view.getTag(R.id.tag_entity);
        SongCollection songCollection = null;

        if (album.getId() == 0) {
            songCollection = new SongCollection(SongCollection.Type.PLAYLIST, new Playlist(Playlist.TYPE.ALL));
        } else {
            songCollection = new SongCollection(SongCollection.Type.ALBUM, album);
        }

//        if (album.getId() == 0) {
//            songCollection.setType(SongCollection.Type.PLAYLIST);
//            Playlist playlist = new Playlist();
//            playlist.setType(Playlist.TYPE.ALL);
//            songCollection.setOwner(playlist);
//        } else {
//            songCollection.setType(SongCollection.Type.ALBUM);
//            songCollection.setOwner(album);
//        }
        mLibraryListener.showSongList(view, songCollection, new CallbackListener() {
            @Override
            public void doStuffAfterOpenSubWindow() {
                mAdapter.openSubWindow(position - 1);
            }

            @Override
            public void doSutffAfterCloseSubWindow() {
                mAdapter.closeSubWindow();
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bt_play:
                Album album = (Album) view.getTag(R.id.tag_entity);
                SongCollection songCollection;

                if (album.getId() == 0) {
                    songCollection = new SongCollection(SongCollection.Type.PLAYLIST, new Playlist(Playlist.TYPE.ALL));
                } else {
                    songCollection = new SongCollection(SongCollection.Type.ALBUM, album);
                }

                Lewa.playerServiceConnector().playSongCollection(getActivity(), songCollection, -1);
                break;
        }

    }

    @Override
    public void onFocusChange(View view, boolean hasFocus) {
        if (hasFocus) {
            //searchEt.clearFocus();
            mLibraryListener.showSearchFragment(SearchType.LOCAL);
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
