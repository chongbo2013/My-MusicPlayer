package com.lewa.player.fragment;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.lewa.Lewa;
import com.lewa.player.R;
import com.lewa.player.adapter.LibraryArtistAdapter;
import com.lewa.player.db.DBService;
import com.lewa.player.fragment.SearchFragment.SearchType;
import com.lewa.player.helper.ViewHelper;
import com.lewa.player.listener.CallbackListener;
import com.lewa.player.listener.LibraryListener;
import com.lewa.player.model.Artist;
import com.lewa.player.model.ArtistCursorIndex;
import com.lewa.player.model.Playlist;
import com.lewa.player.model.Song;
import com.lewa.player.model.SongCollection;
import com.lewa.util.CharacterParser;
import com.lewa.util.PinyinComparator;
import com.lewa.util.StringComparator;
import com.lewa.view.ClearEditText;
import com.lewa.view.MaskEndListView;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.lewa.player.model.Album;
import java.lang.Long;
import com.lewa.view.ABCFastIndexer;
import com.lewa.util.LewaUtils;
import android.graphics.Color;
import android.content.res.Configuration;


/**
 * Created by wuzixiu on 11/27/13.
 */
public class LibraryArtistFragment extends Fragment implements View.OnClickListener, AdapterView.OnItemClickListener, View.OnFocusChangeListener, AbsListView.OnScrollListener,
                    ABCFastIndexer.OnTouchingLetterChangedListener{
    private final static String TAG = "LibraryArtistFragment";//.class.getName();

    MaskEndListView mArtistLv;
    ClearEditText mSearchEt;
    //private SideBar mSideBar;
    //private TextView mDialog;
    private LibraryArtistAdapter mAdapter;
    public ABCFastIndexer mFastIndexer;

    private Cursor mArtistCursor;
    
    private LibraryListener mLibraryListener;
    private CharacterParser mCharacterParser;
    private PinyinComparator pinyinComparator;
    private StringComparator strComparator;
    private static ArrayList<String> marrFirstLetters = new ArrayList<String>();
    
    public String[] letters ={ "#", "A", "B", "C",
                "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P",
                "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z" };

    public LibraryArtistFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCharacterParser = CharacterParser.getInstance();
        pinyinComparator = new PinyinComparator();
        strComparator = new StringComparator();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_library_artist, container, false);
        mArtistLv = (MaskEndListView) rootView.findViewById(R.id.lv);

        //mDialog = (TextView) rootView.findViewById(R.id.dialog);
        //mSearchEt = (ClearEditText) searchBar.findViewById(R.id.et_search);
        //mSideBar = (SideBar) rootView.findViewById(R.id.sidebar);
        mFastIndexer = (ABCFastIndexer)rootView.findViewById(R.id.fast_indexer);
        mArtistLv.setOnScrollListener(this);
        //mSideBar.setTextView(mDialog);
        //mArtistLv.addHeaderView(searchBar);
        mAdapter = new LibraryArtistAdapter(this);
        mArtistLv.setAdapter(mAdapter);
//        mArtistLv.setSelection(1);
        mArtistLv.setOnItemClickListener(this);
        if (mFastIndexer != null) {
            if (letters != null) {
                mFastIndexer.setLetters(letters);
                mFastIndexer.invalidate();
            }
            mFastIndexer.setOnTouchingLetterChangedListener(this);
            mFastIndexer.setPopBackgroundResource(R.drawable.abcfastindexer_pop_bg);
            mFastIndexer.setmAbcfastTopPadding(LewaUtils.calculteFastIndexPaddingTop(this.getActivity()));
        }
        //mSearchEt.setOnFocusChangeListener(this);
        //mArtistLv.setOnTrackListener(mLibraryListener);

        //设置右侧触摸监听
        /*mSideBar.setOnTouchingLetterChangedListener(new SideBar.OnTouchingLetterChangedListener() {

            @Override
            public void onTouchingLetterChanged(String s) {
                //该字母首次出现的位置
                int position = mAdapter.getPositionForSection(s.charAt(0));
                if (position != -1) {
                    mArtistLv.setSelection(position);
                }

            }
        });*/
        if(this.getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            visible = false;
        } else {
            visible = true;
        }
        return rootView;
    }

    private boolean visible = true;
        
    public void setFastIndexVisibility(boolean visible) {
        this.visible = visible; 
        if(null != mFastIndexer) {
            if(visible) {
                mFastIndexer.setVisibility(View.VISIBLE);
            } else {
                mFastIndexer.setVisibility(View.INVISIBLE);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(!isAdded()) {
            return;
        }

        if(null != mFastIndexer) {
            if(visible) {
                mFastIndexer.setVisibility(View.VISIBLE);
            } else {
                mFastIndexer.setVisibility(View.INVISIBLE);
            }
        }
        marrFirstLetters.clear();

        refreshArtistsData();
    }

    public void refreshArtistsData() {
        if(!isAdded()) {
            return;
        }
        try {
            if (mArtistCursor != null && !mArtistCursor.isClosed()) {
                mArtistCursor.close();
            }
            
            List<Song> songs = DBService.loadAllSongs();
            Map<Long, Integer> mArtistMap = new HashMap<Long, Integer>();
            HashMap mAlbumMap = new HashMap<Long, Integer>();
            for (Song song : songs) {
                if (mArtistMap.containsKey(song.getArtist().getId())) {
                    mArtistMap.put(song.getArtist().getId(), mArtistMap.get(song.getArtist().getId()) + 1);
                } else {
                    mArtistMap.put(song.getArtist().getId(), 1);
                }

                if(!mAlbumMap.containsKey(song.getAlbum().getId())) {
                    mAlbumMap.put(song.getAlbum().getId(), 1);
                } 
            }
            mArtistCursor = DBService.loadArtists();

            List<Artist> mArtists = new ArrayList<Artist>();

            if (mArtistCursor != null && mArtistCursor.getCount() > 0) {
                mArtistCursor.moveToFirst();
                CharacterParser characterParser = CharacterParser.getInstance();
                ArtistCursorIndex cursorIndex = new ArtistCursorIndex(mArtistCursor);
                
                while (!mArtistCursor.isAfterLast()) {
                    Artist artist = Artist.fromCursor(mArtistCursor, cursorIndex);
                    int songNum = mArtistMap.containsKey(artist.getId()) ? mArtistMap.get(artist.getId()) : 0;
                    
                    if (songNum > 0) {
                        artist.setSongNum(songNum);
                        String pinyin = characterParser.getSelling(artist.getName());
                        String sortString = pinyin.substring(0, 1).toUpperCase();
                        if (sortString.matches("[A-Z]")) {
                            artist.setInitial(sortString.toUpperCase());
                        } else {
                            artist.setInitial("#");
                        }
                        marrFirstLetters.add(artist.getInitial());
                        mArtists.add(artist);
                    }
                    mArtistCursor.moveToNext();
                }
                mArtistCursor.close();
            }

            Artist headerArtist = new Artist();
            headerArtist.setName(getResources().getString(R.string.text_all_album));
            headerArtist.setInitial("A");
            headerArtist.setSongNum(DBService.countSongs());
            
            /*Cursor  mAlbumCursor = DBService.loadAlbums();
            if (mAlbumCursor != null && mAlbumCursor.getCount() > 0) {
                headerArtist.setAlbumNum(mAlbumCursor.getCount());
                mAlbumCursor.close();
            }*/
            headerArtist.setAlbumNum(mAlbumMap.size());
            headerArtist.setId(0l);
            marrFirstLetters.add("A");
            Collections.sort(mArtists, pinyinComparator);
            Collections.sort(marrFirstLetters, strComparator);

            mArtists.add(0, headerArtist);

            mAdapter.setData(mArtists);
        } catch (SQLException e) {
            e.printStackTrace();
            Log.e(TAG, "Failed to query artist info: \n" + e.getMessage());
        }
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
    public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
        Artist artist = (Artist) view.getTag(R.id.tag_entity);
        SongCollection songCollection;

        if (artist == null) return;

        if (artist.getId() == null || artist.getId() == 0) {
            mLibraryListener.showAlbumFragment(new Long(0), getResources().getString(R.string.text_all_album));
        } else {
            if(!mLibraryListener.hideSongList()) {
                mLibraryListener.showAlbumFragment(artist.getId(), artist.getName());
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bt_play:
                Artist artist = (Artist) view.getTag(R.id.tag_entity);

                if (artist.getId() == 0) {  //play all songs
                    SongCollection songCollection = new SongCollection(SongCollection.Type.PLAYLIST, new Playlist(Playlist.TYPE.ALL));
                    Lewa.playerServiceConnector().playSongCollection(getActivity(), songCollection, -1);
                } else { //play artist.getName() `s songs
                   Lewa.playerServiceConnector().playSongCollection(getActivity(), new SongCollection(SongCollection.Type.PLAYLIST, new Playlist(artist)), 0);
                }

                break;
        }
    }

    @Override
    public void onFocusChange(View view, boolean hasFocus) {
        if (hasFocus) {
            //mSearchEt.clearFocus();
            mLibraryListener.showSearchFragment(SearchType.LOCAL);
        }
    }

    @Override
    public void onStop() {
        Log.i(TAG, "on stop.");

        super.onStop();
    }

    @Override
    public void onDestroy() {
        if (mArtistCursor != null) {
            mArtistCursor.close();
        }
        super.onDestroy();
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
    }

    @Override
    public void onTouchingLetterChanged(String head) {
        if (marrFirstLetters != null && marrFirstLetters.size() > 0) {
            int letterIndex = marrFirstLetters.indexOf(head);
            if (-1 == letterIndex) {
                return;
            }
            //final ListView list = getListView();
            int diff = mArtistLv.getHeaderViewsCount();;
            if (mAdapter.getisNeedHideStarred())  {
                diff -= mAdapter.getStarredCount();
            }
            mArtistLv.setSelection(letterIndex+diff);
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem,
                         int visibleItemCount, int totalItemCount) {
        int headersCnt = ((ListView) view).getHeaderViewsCount();
        int lettersSize = marrFirstLetters.size();
        if (marrFirstLetters != null && lettersSize > 0) {
            String letter = null;
            int position = 0;
            if (firstVisibleItem < headersCnt) {
                position = 0;
            } else {
                position = firstVisibleItem - headersCnt;
            }
            if (position >= 0 && position < lettersSize) {
                letter = marrFirstLetters.get(position);
                mFastIndexer.drawThumb(letter);
            }
        }
    }
}
