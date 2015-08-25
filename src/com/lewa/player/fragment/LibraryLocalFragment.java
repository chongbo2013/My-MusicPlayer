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
import com.lewa.player.adapter.LibraryLocalAdapter;
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
import com.lewa.player.listener.PlayStatusListener;
import com.lewa.player.model.PlayStatus;
import java.lang.ref.WeakReference;
import android.graphics.Bitmap;
import com.lewa.view.ABCFastIndexer;

import com.lewa.util.LewaUtils;



import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.lewa.player.activity.LibraryActivity;
import android.graphics.Color;
import android.content.res.Configuration;

/**
 * Created by sjxu on 08/27/2014.
 */
public class LibraryLocalFragment extends BaseFragment implements View.OnClickListener, AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener, AbsListView.OnScrollListener , //View.OnFocusChangeListener, 
                                                        ABCFastIndexer.OnTouchingLetterChangedListener{
    private final static String TAG = "LibraryLocalFragment"; 
    MaskEndListView mArtistLv;
    ClearEditText mSearchEt;
    public ABCFastIndexer mFastIndexer;
    //private TextView mDialog;
    private LibraryLocalAdapter mAdapter;
    TextView mNoSongTv;
    SongCollection songCollection = null;
    private Cursor mArtistCursor;
    private List<Artist> mArtists;
    private LibraryListener mLibraryListener;
    private CharacterParser mCharacterParser;
    private PinyinComparator pinyinComparator;
    private StringComparator strComparator;
    private static ArrayList<String> marrFirstLetters = new ArrayList<String>();

    private Map<Long, Integer> mArtistMap;

    public String[] letters ={ "#", "A", "B", "C",
            "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P",
            "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z" };

    public LibraryLocalFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCharacterParser = CharacterParser.getInstance();
        pinyinComparator = new PinyinComparator();
        strComparator = new StringComparator();
        initPlayerStatusListener();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_library_local, container, false);
        mArtistLv = (MaskEndListView) rootView.findViewById(R.id.lv);
        
        mFastIndexer = (ABCFastIndexer)rootView.findViewById(R.id.fast_indexer);
        if (mFastIndexer != null) {
            if (letters != null) {
                mFastIndexer.setLetters(letters);
                mFastIndexer.invalidate();
            }
            mFastIndexer.setOnTouchingLetterChangedListener(this);
            mFastIndexer.setPopBackgroundResource(R.drawable.abcfastindexer_pop_bg);
            mFastIndexer.setmAbcfastTopPadding(LewaUtils.calculteFastIndexPaddingTop(this.getActivity()));
        }
        
        mNoSongTv = (TextView) rootView.findViewById(R.id.llo1);
        mArtistLv.setOnScrollListener(this);
 
        mAdapter = new LibraryLocalAdapter(this.getActivity());
        mArtistLv.setAdapter(mAdapter);

        mArtistLv.setOnItemClickListener(this);
        mArtistLv.setOnItemLongClickListener(this);

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
        refreshSongListInfo();

        if(null != mFastIndexer) {
            if(visible) {
                mFastIndexer.setVisibility(View.VISIBLE);
            } else {
                mFastIndexer.setVisibility(View.INVISIBLE);
            }
        }
    }

    
    public void refreshSongListInfo() {
        marrFirstLetters.clear();
        List<Song> songs = null;

        try {
            if (mArtistCursor != null && !mArtistCursor.isClosed()) {
                mArtistCursor.close();
            }

            songCollection = new SongCollection(SongCollection.Type.PLAYLIST, new Playlist(Playlist.TYPE.ALL));
            songs = songCollection.getSongs();
            mArtistMap = new HashMap<Long, Integer>();
            for (Song song : songs) {
                if (mArtistMap.containsKey(song.getArtist().getId())) {
                    mArtistMap.put(song.getArtist().getId(), mArtistMap.get(song.getArtist().getId()) + 1);
                } else {
                    mArtistMap.put(song.getArtist().getId(), 1);
                }
            }

            if(songs.size() > 0) {
                CharacterParser characterParser = CharacterParser.getInstance();
                for(Song song : songs) {
                    String pinyin = characterParser.getSelling(song.getName());
                    String sortString = pinyin.substring(0, 1).toUpperCase();
                    if (sortString.matches("[A-Z]")) {
                        song.setInitial(sortString.toUpperCase());
                    } else {
                        song.setInitial("#");
                    }
                    marrFirstLetters.add(song.getInitial());
                }
                Collections.sort(songs, pinyinComparator);
                Collections.sort(marrFirstLetters, strComparator);
            }
            songCollection.setSongs(songs);
            if(0 == songs.size()) {
                mNoSongTv.setVisibility(View.VISIBLE);
                mArtistLv.setVisibility(View.GONE);
            }  else {
                mNoSongTv.setVisibility(View.GONE);
                mAdapter.setData(songCollection);
            }
        } catch (Exception e) {
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
        Lewa.playerServiceConnector().playSongCollection(this.getActivity(), mAdapter.getCollection(), position);
    }

    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
        Song song = (Song) view.getTag(R.id.tag_entity);
        mLibraryListener.setSongCollection(songCollection);
        ((LibraryActivity)getActivity()).setLongClickSong(song);
        mLibraryListener.showBatchCheckSongFragment();      
        return true;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bt_play:
                Artist artist = (Artist) view.getTag(R.id.tag_entity);

                if (artist.getId() == 0) {
                    SongCollection songCollection = new SongCollection(SongCollection.Type.PLAYLIST, new Playlist(Playlist.TYPE.ALL));
                    Lewa.playerServiceConnector().playSongCollection(getActivity(), songCollection, -1);
                }

                break;
        }
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

    @Override
    public void onTouchingLetterChanged(String head) {
        if (marrFirstLetters != null && marrFirstLetters.size() > 0) {
            int letterIndex = marrFirstLetters.indexOf(head);
            if (-1 == letterIndex) {
                return;
            }
            int diff = mArtistLv.getHeaderViewsCount();;
            if (mAdapter.getisNeedHideStarred())  {
                diff -= mAdapter.getStarredCount();
            }
            mArtistLv.setSelection(letterIndex+diff);
        }
    }

    protected void refreshPlayStatus(PlayStatus status) { 
        Log.i(TAG, "refreshPlayStatus");
        if(null == mAdapter) {
            return;
        }
        mAdapter.notifyDataSetChanged();
    }

    protected void refreshSongsData(Long onlineId, Long localId) {
        refreshSongListInfo();
    }

}
