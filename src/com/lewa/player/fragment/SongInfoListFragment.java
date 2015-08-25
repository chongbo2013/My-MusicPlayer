package com.lewa.player.fragment;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
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
import com.lewa.player.adapter.SongInfoAdapter;
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
import com.lewa.player.activity.BaseFragmentActivity;
import com.lewa.player.helper.PlaylistHelper;
import com.lewa.il.OnDownloadSongClickListener;
import android.content.Context;
import com.lewa.player.online.AppDownloadManager;
import com.lewa.util.LewaUtils;
import com.lewa.player.listener.CallbackPlayListener;
import com.lewa.player.model.PlayStatus;
import com.lewa.player.online.OnlineLoader;
import static com.lewa.player.model.SongCollection.Type;




/**
 * Created by sjxu on 08/27/14.
 */
public class SongInfoListFragment extends BaseFragment implements View.OnClickListener, //AdapterView.OnItemClickListener,
                                AdapterView.OnItemLongClickListener{
    private static final String TAG = "SongInfoListFragment";//.class.getName();

    public static final String ARG_ARTIST_ID = "artistId";
    public static final String ARG_ARTIST_NAME = "artistName";
    public static final String ARG_IS_ONLINE = "isOnline";
    public static final String ARG_IS_NEED_LONG_CLICK = "isNeedLongClick";

    ImageButton mBackBtn;
    ImageView mCoverIv;
    TextView mArtistNameTv;
    TextView mNoAlbumTv;
    MaskEndListView mAlbumLv;
    private Cursor mAlbumCursor;
    private SongInfoAdapter mAdapter;
    
    private List<Album> mAlbums;
    private AlbumListener mAlbumListener;
    private Long artistId;
    private SongCollection songCollection;

    public static SongInfoListFragment newInstance(String titleName, SongCollection songCollection, boolean isNeedLongClick) {
        
        SongInfoListFragment artistAlbumFragment = new SongInfoListFragment(songCollection);
        Bundle args = new Bundle();

        if (!StringUtils.isBlank(titleName)) {
            args.putString(ARG_ARTIST_NAME, titleName);
        }

        args.putBoolean(ARG_IS_NEED_LONG_CLICK, isNeedLongClick);
        

        artistAlbumFragment.setArguments(args);
        return artistAlbumFragment;
    }

    
    public SongInfoListFragment() { //add this constructor fun for bug 60194
        
    }

    public SongInfoListFragment(SongCollection songCollection) {
        this.songCollection = songCollection;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
    }
  

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_song_info, container, false);
        mBackBtn = (ImageButton) rootView.findViewById(R.id.bt_back);
        mArtistNameTv = (TextView) rootView.findViewById(R.id.tv_artist_name);
        mCoverIv = (ImageView) rootView.findViewById(R.id.iv_cover);
        mAlbumLv = (MaskEndListView) rootView.findViewById(R.id.lv_album);
        mNoAlbumTv = (TextView) rootView.findViewById(R.id.llo1);

        mAdapter = new SongInfoAdapter(this.getActivity());

        mAlbumLv.setAdapter(mAdapter);
        mAlbumLv.setOnItemClickListener(new CustomOnitemClickListener(this.getActivity(), mAdapter));
        if(getArguments().getBoolean(ARG_IS_NEED_LONG_CLICK)) {
            mAlbumLv.setOnItemLongClickListener(this);
        }
        mBackBtn.setOnClickListener(this);

        checkNetWork();

        return rootView;
    }

    private void checkNetWork() {
        if(OnlineLoader.isWiFiActive(this.getActivity()) || OnlineLoader.isNetworkAvailable()) return;
        
        if(null == songCollection || null == songCollection.getType() || Type.PLAYLIST != songCollection.getType()) return;	//null == songCollection for bug 61931
        Playlist playList = (Playlist)songCollection.getOwner();
        if(playList == null) return;       

        switch(playList.getType()) {
            case ALL_STAR:
            case TOP_LIST_HOT:
            case TOP_LIST_NEW:
            case ONLINE:
            case FM:
            case ONLINE_CATEGORY:
            case TOP_LIST_CATEGORY:
            case ALL_STAR_CATEGORY:
                mNoAlbumTv.setText(this.getActivity().getResources().getString(R.string.no_network_text));
                mAlbumLv.setVisibility(View.GONE);
                mNoAlbumTv.setVisibility(View.VISIBLE);
                
        }
      
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
        
        mPlayStatusListener = new PlayStatusBackgroundListener(SongInfoListFragment.class.getName(), mCoverIv);
        super.onResume();
        Lewa.getAndBlurCurrentCoverUrl(mPlayStatusListener);
        //for bug 65333 start 
        if(null != songCollection && SongCollection.Type.PLAYLIST == songCollection.getType()) {
            Playlist playList = (Playlist)songCollection.getOwner();
            if(null != playList && (Playlist.TYPE.FAVORITE == playList.getType()  || 
                            Playlist.TYPE.DOWNLOAD == playList.getType() ||Playlist.TYPE.RECENT_PLAY == playList.getType())) {
                songCollection.reset();
            }
        }
        //for bug 65333 end
        refreshSongsState(songCollection);
    }

    public void onSongsDownloaded(){
		if( null != mAdapter){
			mAdapter.songDownloaded();
		}
	}

    public void refreshPlayStatus(PlayStatus status) { 
        Log.i(TAG, "refreshPlayStatus");
        mAdapter.notifyDataSetChanged();
    }

    public class MyGetSongsListener implements PlaylistHelper.GetSongsListener {
        private SongCollection collection;

        public MyGetSongsListener(SongCollection collection) {
            this.collection = collection;
        }

        @Override
        public void onGotSongs(Playlist playlist) {
            Log.i(TAG, "Return from baidu api: " + playlist.getType().name() + "\t size: " + (playlist.getSongs() == null ? 0 : playlist.getSongs().size()));
            songCollection.setOwner(playlist);
            DBService.matchSongs(songCollection.getSongs());
            mAdapter.setData(songCollection);
        }
    }


    public void onItemClick(AdapterView<?> adapterView, View view, final int position, long item) {
        Lewa.playerServiceConnector().playSongCollection(this.getActivity(), mAdapter.getCollection(), position);
    }

    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
        Song song = (Song) view.getTag(R.id.tag_entity);
        mAlbumListener.setSongCollection(songCollection);
        ((BaseFragmentActivity)getActivity()).setLongClickSong(song);
        mAlbumListener.showBatchCheckSongFragment();
        
        return true;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bt_back:
                mAlbumListener.hideSongInfoListFragment();
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

    public void refreshSongsState(SongCollection collection) {
        if (songCollection != null && !songCollection.equals(collection)) {
            songCollection.clear();
        }
        songCollection = collection;
        
        mArtistNameTv.setText(getArguments().getString("artistName"));
        if(null == songCollection) {
            return;
        }
        switch (songCollection.getType()) {
            case ALBUM:
                mAdapter.setData(songCollection);
                break;
            case PLAYLIST:
                    try {
                        Playlist playlist = (Playlist) songCollection.getOwner();
                        Activity mActivity = this.getActivity();
                        switch (playlist.getType()) {
                            case ONLINE:
                                PlaylistHelper.getOnlinePlaylistSongs(mActivity, playlist, new MyGetSongsListener(songCollection));
                                break;
                            case ALL_STAR:
                                PlaylistHelper.getAllStarSongs(mActivity, playlist, new MyGetSongsListener(songCollection));
                                break;
                            case TOP_LIST_NEW:
                                PlaylistHelper.getTopListNewSongs(mActivity, playlist, new MyGetSongsListener(songCollection));
                                break;
                            case TOP_LIST_HOT:
                                PlaylistHelper.getTopListHotSongs(mActivity, playlist, new MyGetSongsListener(songCollection));
                                break;
                            case DOWNLOAD:
                        	     setDownloadListener();
                            default:
                                mAdapter.setData(songCollection);
                                break;
                        }
                    }catch (Exception e) {
                        e.printStackTrace();
                    }
                break;
        }
    }

    @Override
    public void onStop() {

        super.onStop();
    }

    private AppDownloadManager appDownloadManager;
    public void initAppDownloadManager(){
        if(appDownloadManager==null)
            appDownloadManager=AppDownloadManager.getInstance(this.getActivity().getApplicationContext());
    }

    public void setDownloadListener(){
        LewaUtils.logE(TAG, "setDownloadListener");
        initAppDownloadManager();
        appDownloadManager.setOnDownloadProgressChangeListener(mAdapter);
        appDownloadManager.setOnDownloadStatusChangeListener(mAdapter);
    }

    public void removeDownloadListener(){
        if(appDownloadManager!=null){
            appDownloadManager.setOnDownloadProgressChangeListener(null);
            appDownloadManager.setOnDownloadStatusChangeListener(null);
        }
    }

    @Override
    public void onDestroy() {
        if (mAlbumCursor != null) {
            mAlbumCursor.close();
        }

        super.onDestroy();
    }

    private class CustomOnitemClickListener implements AdapterView.OnItemClickListener{
        private OnDownloadSongClickListener onDownloadSongClickListener;
        private Context mContext;
        public CustomOnitemClickListener(Context context, OnDownloadSongClickListener onDownloadSongClickListener){
            this.onDownloadSongClickListener=onDownloadSongClickListener;
            this.mContext=context;
        }
        @Override
        public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
            //Song song = (Song) mAdapter.getItem(position);
            
            Song song = (Song) view.getTag(R.id.tag_entity);
            if(null == song) {
                return;
            }
            if(onDownloadSongClickListener!=null){
                boolean isDownloading=onDownloadSongClickListener.onDownloadSongClick(mContext, song);
                if(isDownloading)
                    return;
            }
            if (song.getType() == Song.TYPE.ONLINE) {
                ((BaseFragmentActivity)SongInfoListFragment.this.getActivity()).prepareForPlay(new MyCallbackPlayListener(song, position));
            } else {
                Lewa.playerServiceConnector().playSongCollection(SongInfoListFragment.this.getActivity(), mAdapter.getSongCollection(), position);
            }
        }

    }

    
    private class MyCallbackPlayListener implements CallbackPlayListener {
        private Song song = null;
        private int position = -1;

        public MyCallbackPlayListener(Song song, int position) {
            this.song = song;
            this.position = position;
        }

        @Override
        public void execute() {
            List<Song> songs = new ArrayList<Song>();
            songs.add(song);

            SongCollection sc = new SongCollection(SongCollection.Type.SINGLE, song);
            sc.setSongs(songs);

            Lewa.playerServiceConnector().playSongCollection(SongInfoListFragment.this.getActivity(), sc, 0);
        }
    }
}
