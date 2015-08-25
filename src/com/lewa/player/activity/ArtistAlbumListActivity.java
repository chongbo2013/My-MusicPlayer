package com.lewa.player.activity;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.lewa.Lewa;
import com.lewa.player.R;
import com.lewa.player.adapter.AlbumAdapter;
import com.lewa.player.adapter.SongAdapter;
import com.lewa.player.fragment.ArtistAlbumFragment;
import com.lewa.player.fragment.SongInfoListFragment;

import com.lewa.player.listener.AlbumListener;
import com.lewa.player.listener.CallbackListener;
import com.lewa.player.model.Artist;
import com.lewa.player.model.Playlist;
import com.lewa.player.model.Song;
import com.lewa.player.model.SongCollection;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.support.v4.app.FragmentManager;
import lewa.support.v7.app.ActionBar;



import java.util.List;
import com.lewa.kit.ActivityHelper;


public class ArtistAlbumListActivity extends BaseFragmentActivity implements AlbumListener, AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {
    private static final String TAG = "ArtistAlbumListActivity";//.class.getName();
    public static final String ARG_ARTIST = "ARTIST";
    private final String TAG_ALBUM_FRAGMENT = "ALBUM";
    private final String FT_TAG_SONG_INFO_LIST = "song_info_list";

    ListView mSongsLv;
    LinearLayout mSongsLo;

    LinearLayout mSongInfoListContainer;

    View mSongFooterView;

    private Animation mHorizontalShowAction, mHorizontalHiddenAction;

    private SongAdapter mSongAdapter;
    private AlbumAdapter mAdapter;
    private CallbackListener mCallbackListener;

    private Artist mArtist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //hasBackground = true;
        View rootView = Lewa.inflater().inflate(R.layout.activity_albumlist, null, false);
        setContentView(rootView);
        //getWindow().getDecorView().setSystemUiVisibility(0x10000000 | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        /*mSongsLv = (ListView) rootView.findViewById(R.id.lv_song);
        mSongsLo = (LinearLayout) rootView.findViewById(R.id.llo2);
        mSongAdapter = new SongAdapter();
        mSongFooterView = Lewa.inflater().inflate(R.layout.album_songs_footer, null);
        mSongsLo.setVisibility(View.GONE);
        mSongsLv.addFooterView(mSongFooterView);
        mSongsLv.setAdapter(mSongAdapter);
        mSongsLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Lewa.playerServiceConnector().playSongCollection(ArtistAlbumListActivity.this, mSongAdapter.getSongCollection(), position);
            }
        });*/
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        mSongInfoListContainer = (LinearLayout) rootView.findViewById(R.id.song_info_container);
        Bundle extras = getIntent().getExtras();

        if (extras == null) {
            mArtist = new Artist();
        } else {
            mArtist = (Artist) extras.getSerializable(ARG_ARTIST);
        }

        getSupportFragmentManager().beginTransaction()
                .add(R.id.container, ArtistAlbumFragment.newInstance(mArtist.getId(), mArtist.getName(), mArtist.isOnline()), TAG_ALBUM_FRAGMENT)
                .commit();

        initAnimations();
    }

    private void initAnimations() {
        //TODO: difine this with xml
        mHorizontalShowAction = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f);
        mHorizontalShowAction.setDuration(getResources().getInteger(R.integer.short_anim_time));

        mHorizontalHiddenAction = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 1.0f,
                Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f);
        mHorizontalHiddenAction.setDuration(getResources().getInteger(R.integer.short_anim_time));
    }
    
    @Override
    public void showSongList(View view, SongCollection collection, CallbackListener listener) {
        /*this.mCallbackListener = listener;
        listener.doStuffAfterOpenSubWindow();
        List<Song> songs = null;
        switch (collection.getType()) {
            case ALBUM:
                mSongFooterView.setVisibility(View.VISIBLE);
                mSongsLo.setVisibility(View.VISIBLE);
                mSongsLv.setAdapter(mSongAdapter);

                mSongAdapter.setData(collection);
                break;
            case PLAYLIST:
                Playlist playlist = (Playlist) collection.getOwner();
                switch (playlist.getType()) {
                    case ARTIST:
                        mSongFooterView.setVisibility(View.VISIBLE);
                        mSongsLo.setVisibility(View.VISIBLE);
                        mSongsLv.setAdapter(mSongAdapter);

                        mSongAdapter.setData(collection);
                        break;
                }
                break;
        }*/
    }

    @Override
    public boolean hideSongList() {
        /*if(mSongsLo.isShown()) {
            mSongsLo.setVisibility(View.GONE);
            return true;
        }*/
        return false;
    }

    
    SongInfoListFragment mSongInfoListFragment = null;
    public void showSongInfoListFragment(String title, SongCollection songCollection){
        mSongInfoListFragment = SongInfoListFragment.newInstance(title, songCollection, false);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.song_info_container, mSongInfoListFragment, FT_TAG_SONG_INFO_LIST)
                .addToBackStack("song_info")
                .commit();
        showSongInfoListContainer();
    }
    
    public void hideSongInfoListFragment(){
        hideSongInfoContainer();
    }

    public void showSongInfoListContainer() {
        if (!mSongInfoListContainer.isShown()) {
            mSongInfoListContainer.setVisibility(View.VISIBLE);
            mSongInfoListContainer.startAnimation(mHorizontalShowAction);
        }
    }

    public void hideSongInfoContainer() {
        if (mSongInfoListContainer.isShown()) {
            mSongInfoListContainer.setVisibility(View.GONE);
            mSongInfoListContainer.startAnimation(mHorizontalHiddenAction);
        }
    }

    @Override
    public void showAlbumFragment(Long artistId, String artistName) {

    }

    @Override
    public void hideAlbumFragment() {

    }

	public void finish() {
		super.finish();
	}

    @Override
    public void onBack() {
        this.finish();
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

    }

    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
        return false;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            /*if (mSongsLv.isShown()) {
                if (mCallbackListener != null) {
                    mCallbackListener.doSutffAfterCloseSubWindow();
                }
                hideSongList();
                return false;
            }*/

            if (mSongInfoListContainer.isShown()) {
                mSongInfoListContainer.setVisibility(View.GONE);
                mSongInfoListContainer.startAnimation(mHorizontalHiddenAction);
                return false;
            }
            

        }
        return super.onKeyDown(keyCode, event);
    }

    public void onBackPressed() {
        getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE); 
        super.onBackPressed();
    }
    
    @Override
    public void showMiniPlayerView() {

    }

    @Override
    public void hideMiniPlayerView() {

    }

    public void showBatchCheckSongFragment(){

    }

    public void showBatchCheckSongFragment(Long songId){

    }

    public void setSongCollection(SongCollection songCollection){

    }

    public SongCollection getSongCollection(){
        return null;
    }
}
