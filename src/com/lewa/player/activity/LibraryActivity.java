package com.lewa.player.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.baidu.music.SDKEngine;
import com.baidu.music.SDKInterface;
import com.baidu.music.manager.JobManager;
import com.baidu.music.oauth.OAuthException;
import com.baidu.music.oauth.OAuthInterface;
import com.baidu.music.oauth.OAuthManager;
import com.baidu.music.oauth.OAuthInterface.onAuthorizeFinishListener;
import com.lewa.ExitApplication;
import com.lewa.Lewa;
import com.lewa.il.OnDownloadSongClickListener;
import com.lewa.player.IMediaPlaybackService;
import com.lewa.player.MediaPlaybackService;
import com.lewa.player.MusicUtils;
import com.lewa.player.R;
import com.lewa.player.SleepModeManager;
import com.lewa.player.adapter.LibraryFragmentAdapter;
import com.lewa.player.adapter.SongAdapter;
import com.lewa.player.db.DBService;
import com.lewa.player.enums.LibraryModule;
import com.lewa.player.enums.StarCatalog;
import com.lewa.player.fragment.AllStarFragment;
import com.lewa.player.fragment.ArtistAlbumFragment;
import com.lewa.player.fragment.ArtistFragment;
import com.lewa.player.fragment.BatchEditPlayListFragment;
import com.lewa.player.fragment.LibraryLocalFragment;

import com.lewa.player.fragment.LibraryArtistFragment;
import com.lewa.player.fragment.LibraryBrowseFragment;
import com.lewa.player.fragment.LibraryMineFragment;
import com.lewa.player.fragment.OnlinePlaylistFragment;
import com.lewa.player.fragment.SearchFragment;
import com.lewa.player.fragment.TopListFragment;
import com.lewa.player.fragment.SearchFragment.SearchType;
import com.lewa.player.fragment.SongInfoListFragment;
import com.lewa.player.helper.PlaylistHelper;
import com.lewa.player.listener.CallbackListener;
import com.lewa.player.listener.CallbackPlayListener;
import com.lewa.player.listener.LibraryListener;
import com.lewa.player.listener.PlayControlListener;
import com.lewa.player.listener.PlayStatusListener;
import com.lewa.player.model.PlayStatus;
import com.lewa.player.model.Playlist;
import com.lewa.player.model.Song;
import com.lewa.player.model.SongCollection;
import com.lewa.player.online.AppDownloadManager;
import com.lewa.util.LewaUtils;
import com.lewa.view.HorizontalHideListView;

import com.lewa.kit.ActivityHelper;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import lewa.support.v7.app.ActionBar;
import android.view.Window;

import android.view.animation.AnimationUtils;
import android.graphics.BitmapFactory;
import com.lewa.util.BlurRunnable;

import com.lewa.player.db.DBService;
import com.baidu.music.manager.JobManager;
import com.baidu.music.onlinedata.OnlineManagerEngine;
import com.lewa.player.online.AppDownloadManager;




public class LibraryActivity extends BaseFragmentActivity implements LibraryListener, PlayControlListener, View.OnClickListener, SDKInterface { //AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener,
    private static final String TAG = "LibraryActivity"; 
    private final static String CLIENT_ID = "GGHe48qPA6VvGSwASGBoSUaq";
    private final static String CLIENT_SECRET = "vSUMsQaz6fwHWN0AbfrkaMsAj9q7UZVI";
    private final static String SCOPE = "music_media_basic,music_musicdata_basic,music_search_basic,music_media_premium";
    public final static String ARG_LIBRARY = "LIBRARY";
    public final static String ARG_DOWNLOAD = "DOWNLOAD";
    
    private final static int MAX_PAGES = 10;	
    public final static int UPDATE_BG_IMAGE = 1;
    
    private static boolean mKeyboardStatus = false;
    private static boolean hasCheckedSdcard = false;
    
    private SDKEngine engine;
    private OAuthManager oAuthManager;
    
    TextView mTvTitle;
    ViewPager mVp;
    RelativeLayout mLoCopyright;
    ImageView mCoverIv;
    ImageView mMiniCoverIv;
    TextView mMiniSongTv;
    TextView mMiniArtistTv;
    View mSongFooterView;
    ViewGroup mMiniPlayerView;
    ImageView[] mImageViews;
    ViewGroup mIndicators;
    LinearLayout mSecondPageContainer;
    LinearLayout mThirdPageContainer;
    LinearLayout mFourthPageContainer;
    LinearLayout mFifthPageContainer;
    LinearLayout mSongInfoListContainer;
    ImageButton mPlayBtn;
    ImageButton mNextBtn;
    View mMiniCover;

    //private ImageView mPopupArrowIv;
    //private View mArrowView;

    
    LibraryModule[] mModules;
    private LibraryFragmentAdapter mFragmentAdapter;
    private AnimationDrawable jumpyAnim;
    private Animation mHorizontalShowAction, mHorizontalHiddenAction;
    //private Animation mVerticalShowAction, mVerticalHiddenAction;

    private CallbackListener mCallbackListener;

    private SongCollection mSongCollection;
    private Song mLongClickSong;
    
    private LibraryLocalFragment libraryLocalFragment;
    private LibraryArtistFragment libraryArtistFragment;
    private LibraryMineFragment libraryMineFragment;
    private LibraryBrowseFragment libraryBrowseFragment;
    private AppDownloadManager appDownloadManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initEngine();
        //hasBackground = true;
        View rootView = (ViewGroup) getLayoutInflater().inflate(R.layout.activity_library, null);
        setContentView(rootView);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
          
        mCoverIv = (ImageView) rootView.findViewById(R.id.iv_cover);
        mMiniCoverIv = (ImageView) rootView.findViewById(R.id.iv_mini_cover);
        mMiniSongTv = (TextView) rootView.findViewById(R.id.tv_title);
        mMiniArtistTv = (TextView) rootView.findViewById(R.id.artist_name);
        mMiniPlayerView = (ViewGroup) rootView.findViewById(R.id.llo1);
        mMiniPlayerView.setOnClickListener(this);
        mTvTitle = (TextView) rootView.findViewById(R.id.tv_label);
        mVp = (ViewPager) rootView.findViewById(R.id.vp);

        mPlayBtn = (ImageButton) rootView.findViewById(R.id.bt_play);
        mPlayBtn.setOnClickListener(this);
        mNextBtn = (ImageButton) rootView.findViewById(R.id.bt_next);
        mNextBtn.setOnClickListener(this);
        //mMiniCover = rootView.findViewById(R.id.lo_mini_cover);
        mMiniCoverIv.setOnClickListener(this);
        ((ImageButton) rootView.findViewById(R.id.setting)).setOnClickListener(this);
        ((ImageButton) rootView.findViewById(R.id.search)).setOnClickListener(this);
        
        //mCoverIv.setAlpha(0.9f);
        mIndicators = (ViewGroup) rootView.findViewById(R.id.l_view_group);
        mSongInfoListContainer = (LinearLayout) rootView.findViewById(R.id.song_info_container);
        mSecondPageContainer = (LinearLayout) rootView.findViewById(R.id.l_secondary_page_container);
        mThirdPageContainer = (LinearLayout) rootView.findViewById(R.id.l_third_page_container);
        mFourthPageContainer = (LinearLayout) rootView.findViewById(R.id.l_fourth_page_container);
        mFifthPageContainer = (LinearLayout) rootView.findViewById(R.id.l_fifth_page_container);


        jumpyAnim = (AnimationDrawable) getResources().getDrawable(R.drawable.anim_slide_icon);
        startIconAnim();
        mSongFooterView = Lewa.inflater().inflate(R.layout.album_songs_footer, null);
        mImageViews = new ImageView[mIndicators.getChildCount()];

        for (int i = 0; i < mIndicators.getChildCount(); i++) {
            mImageViews[i] = (ImageView) mIndicators.getChildAt(i);
        }

        List<Fragment> fragments = new ArrayList<Fragment>();

        libraryLocalFragment = new LibraryLocalFragment();      
        libraryArtistFragment = new LibraryArtistFragment();
        libraryBrowseFragment = new LibraryBrowseFragment();
        libraryMineFragment = new LibraryMineFragment();
        
        fragments.add(libraryLocalFragment);
        fragments.add(libraryArtistFragment);
        fragments.add(libraryMineFragment);
        fragments.add(libraryBrowseFragment);
        
        mFragmentAdapter = new LibraryFragmentAdapter(getSupportFragmentManager(), fragments);//, this);
        mVp.setAdapter(mFragmentAdapter);
        mVp.setOnPageChangeListener(new GuidePageChangeListener());
        
        mModules = LibraryModule.values();
        mVp.setCurrentItem(0);	
        mImageViews[0].setBackgroundResource(R.drawable.whitepoint);
        mTvTitle.setText(mModules[0].value());
        mPlayStatusListener = new MyPlayStatusListener(this);

        initAnimations();
    }

    private void initEngine() {
        LewaUtils.logE(TAG, "initEngine");
        engine = SDKEngine.getInstance();
        engine.init(getApplicationContext(), CLIENT_ID, CLIENT_SECRET, SCOPE, this);
        oAuthManager = OAuthManager.getInstance(getApplicationContext());
        if(oAuthManager.validate()< 5 * 24 * 60 *60) {//0){
            LewaUtils.logE(TAG, "oAuthManager.validate()<0");
            oAuthManager.authorize(new OAuthInterface.onAuthorizeFinishListener() {
                @Override
                public void onAuthorizeFinish(int status) {
                    LewaUtils.logE(TAG, "onAuthorizeFinish status = "+status);
                    if(libraryBrowseFragment!=null&&status==OAuthException.SUCCESS){
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                libraryBrowseFragment.requestRadioList();
                            }
                        }, 500);
                    }
                }
            });
        }
    }
    

    @Override
    public void onResume() {
        String state = Environment.getExternalStorageState();
        if (!state.equals(Environment.MEDIA_MOUNTED) && !hasCheckedSdcard) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.no_sdcard_title_text)
                    .setMessage(R.string.no_sdcard_message_text)
                    .setCancelable(false)
                    .setPositiveButton(getResources().getString(R.string.ok_cn_text), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            JobManager.stop();
                            SleepModeManager.setSleepTime(Lewa.context(), 0);
                            SleepModeManager.deleteSleepTime(Lewa.context());
                            Intent exitIntent = new Intent(Intent.ACTION_MAIN);
                            exitIntent.addCategory(Intent.CATEGORY_HOME);
                            exitIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(exitIntent);
                            android.os.Process.killProcess(android.os.Process.myPid());
                        }
                    }).create().show();
            hasCheckedSdcard = true;
        }

        Intent i = getIntent();
        if(null != i) {
            String action = i.getAction();
            if (ARG_LIBRARY.equals(action)) {
                mVp.setCurrentItem(3);
            }

            String tag = i.getStringExtra("tag");
            if("download".equals(tag)) {
                mVp.setCurrentItem(2);
                try {
                    Playlist playlist = DBService.findPlaylistForDownload();
                    if(null != playlist ){
                    showSongInfoListFragment(playlist.getName(), 
                                        new SongCollection(SongCollection.Type.PLAYLIST, playlist), false);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.i(TAG, "e = " +e);
                }
            }

            i.putExtra("tag", "");
            setIntent(i);
        }

        mKeyboardStatus = false;

        SearchFragment searchFragment = (SearchFragment) getSupportFragmentManager().findFragmentByTag("search");
        if (searchFragment != null) {
            searchFragment.hideKeyboard();
        }

		// pr937003 wjhu add begin
		// to restore the status of mini player
		Song playSong = Lewa.getPlayingSong();
		if (null != playSong) {
			mMiniSongTv.setText(playSong.getName());
			String artistName = playSong.getArtist().getName();
			if (MediaStore.UNKNOWN_STRING.equals(artistName)) {
				mMiniArtistTv.setText(getString(
						R.string.unknown_artist_name));
			} else {
				mMiniArtistTv.setText(artistName);
			}
			Lewa.loadArtistAvatar(playSong.getArtist().getName(),
					mPlayStatusListener);
		} else {
			mMiniSongTv.setText("");
			mMiniArtistTv.setText("");
			mMiniCoverIv.setImageResource(R.drawable.cover);
		}
		// pr937003 wjhu add end
        super.onResume();
    }

    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    @Override
    public void resetBg(int position) {

    }

    /*  //os6 not use
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

            Lewa.playerServiceConnector().playSongCollection(LibraryActivity.this, sc, 0);
        }
    }*/

    private void initAnimations() {
        //TODO: difine this with xml
        mHorizontalShowAction = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f);
        mHorizontalShowAction.setDuration(getResources().getInteger(R.integer.short_anim_time));

        mHorizontalHiddenAction = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 1.0f,
                Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f);
        mHorizontalHiddenAction.setDuration(getResources().getInteger(R.integer.short_anim_time));

        /*mVerticalShowAction = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f);
        mVerticalHiddenAction = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 1.0f,
                Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f);
        mVerticalShowAction.setDuration(getResources().getInteger(android.R.integer.config_shortAnimTime));
        mVerticalHiddenAction.setDuration(getResources().getInteger(android.R.integer.config_shortAnimTime));*/
    }


    protected void startIconAnim() {
        if (jumpyAnim != null && !jumpyAnim.isRunning()) {
            jumpyAnim.start();
        }
    }

    private void setBackgroundX(int scrollPosition) {

    }

    /*@Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
    }*/

    public void showSecondPageContainer() {
        if (!mSecondPageContainer.isShown()) {
            mSecondPageContainer.setVisibility(View.VISIBLE);
            mSecondPageContainer.startAnimation(mHorizontalShowAction);
        }
    }

    public void hideSecondPageContainer() {
        if (mSecondPageContainer.isShown()) {
            mSecondPageContainer.setVisibility(View.GONE);
            mSecondPageContainer.startAnimation(mHorizontalHiddenAction);
        }
    }

    public void showThirdPageContainer() {
        if (!mThirdPageContainer.isShown()) {
            mThirdPageContainer.setVisibility(View.VISIBLE);
            mThirdPageContainer.startAnimation(mHorizontalShowAction);
        }
    }

    public void hideThirdPageContainer() {
        if (mThirdPageContainer.isShown()) {
            mThirdPageContainer.setVisibility(View.GONE);
            mThirdPageContainer.startAnimation(mHorizontalHiddenAction);
        }
    }

    public void showFourthPageContainer() {
        if (!mFourthPageContainer.isShown()) {
            mFourthPageContainer.setVisibility(View.VISIBLE);
            mFourthPageContainer.startAnimation(mHorizontalShowAction);
        }
    }

    public void hideFourthPageContainer() {
        if (mFourthPageContainer.isShown()) {
            mFourthPageContainer.setVisibility(View.GONE);
            mFourthPageContainer.startAnimation(mHorizontalHiddenAction);
        }
    }

    public void showFifthPageContainer() {
        if (!mFifthPageContainer.isShown()) {
            mFifthPageContainer.setVisibility(View.VISIBLE);
            mFifthPageContainer.startAnimation(mHorizontalShowAction);
        }
    }

    public void hideFifthPageContainer() {
        if (mFifthPageContainer.isShown()) {
            mFifthPageContainer.setVisibility(View.GONE);
            mFifthPageContainer.startAnimation(mHorizontalHiddenAction);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_next:
                next();
                break;
            case R.id.bt_play:
                PlayStatus playStatus = Lewa.getPlayStatus();
                if (playStatus.isPlaying()) {
                    pause();
                } else {
                    play();
                }
                break;                
            case R.id.iv_mini_cover:               
            case R.id.llo1:
            	//pr 937782 modify by wjhu begin
        		//to avoid fc
            	Song song = Lewa.getPlayingSong();
            	if (song != null) {
            		ActivityHelper.goPlayAnim(this);
            	}
            	//pr 937782 modify by wjhu end
                
                break;

            case R.id.setting:
                startActivity(new Intent(this, SettingActivity.class));
                break;

            case R.id.search:
                showSearchFragment(SearchType.ONLINE);
                break;

        }
    }

    /*@Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
        Song song = (Song) view.getTag(R.id.tag_entity);
        mLongClickSong = song;
        showBatchCheckSongFragment();
        return true;
    }*/

    public void setLongClickSong(Song song){
    	 mLongClickSong = song;
    }

    // 指引页面更改事件监听器
    class GuidePageChangeListener implements ViewPager.OnPageChangeListener {
        @Override
        public void onPageScrollStateChanged(int arg0) {
            hideSongList();
            if(arg0 == 2) {
                showMiniPlayerView();
            }
        }

        @Override
        public void onPageScrolled(int position, float offset, int offsetPixels) {
//            parallaxIv.setCurrent_position(position);
//            parallaxIv.setCurrent_offset(offset);
        }

        @Override
        public void onPageSelected(int position) {

            if (position == 3) {    //browser panel
                //mLoCopyright.setVisibility(View.VISIBLE);
            } else {
                //mLoCopyright.setVisibility(View.GONE);
            }
            int length = mImageViews.length;
            for (int i = 0; i < length; i++) {

                if (position != i) {
                    mImageViews[i].setBackgroundResource(R.drawable.greypoint);
                } else {
                    //mImageViews[position].setBackgroundResource(R.drawable.bluepoint);
                    mImageViews[position].setBackgroundResource(R.drawable.whitepoint);
                    mTvTitle.setText(mModules[i].value());
                }
            }
        }

    }

    public static final String FT_TAG_SONG_INFO_LIST = "song_info_list";
    SongInfoListFragment mSongInfoListFragment = null;
    public void showSongInfoListFragment(String title, SongCollection songCollection) {
        mSongInfoListFragment = SongInfoListFragment.newInstance(title, songCollection, true);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.song_info_container, mSongInfoListFragment, FT_TAG_SONG_INFO_LIST)
                .addToBackStack("song_info")
                .commit();
        showSongInfoListContainer(true); 
    }

    public void showSongInfoListFragment(String title, SongCollection songCollection, boolean isNeedAnim) {
        mSongInfoListFragment = SongInfoListFragment.newInstance(title, songCollection, true);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.song_info_container, mSongInfoListFragment, FT_TAG_SONG_INFO_LIST)
                .addToBackStack("song_info")
                .commit();
        showSongInfoListContainer(isNeedAnim); 
    }

    public void hideSongInfoListFragment() {
        hideSongInfoContainer();
    }


    public void showSongInfoListContainer(boolean isNeedAnim) {
        if (!mSongInfoListContainer.isShown()) {
            mSongInfoListContainer.setVisibility(View.VISIBLE);
            if(isNeedAnim) {
                mSongInfoListContainer.startAnimation(mHorizontalShowAction);
            }
        }
    }

    public void hideSongInfoContainer() {
        if (mSongInfoListContainer.isShown()) {
            mSongInfoListContainer.setVisibility(View.GONE);
            mSongInfoListContainer.startAnimation(mHorizontalHiddenAction);
        }
    }

    @Override
    public void showAllStarFragment() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.l_secondary_page_container, new AllStarFragment())
                .addToBackStack("name1")
                .commit();
        showSecondPageContainer();
    }

    @Override
    public void hideAllStarFragment() {
        hideSecondPageContainer();
    }

    @Override
    public void showArtistFragment(StarCatalog catalog) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.l_third_page_container, new ArtistFragment(catalog))
                .addToBackStack("name2")
                .commit();
        showThirdPageContainer();
    }

    @Override
    public void hideArtistFragment() {
        hideThirdPageContainer();
    }

    ArtistAlbumFragment mArtistAlbumFragment = null;
    @Override
    public void showAlbumFragment(Long artistId, String artistName) {
        mArtistAlbumFragment = ArtistAlbumFragment.newInstance(artistId, artistName, false);
        getSupportFragmentManager().beginTransaction()                
                .replace(R.id.l_fourth_page_container, mArtistAlbumFragment)
                .addToBackStack("name3")
                .commit();
        showFourthPageContainer();
    }

    @Override
    public void hideAlbumFragment() {
        hideFourthPageContainer();
    }

    @Override
    public void showOnlinePlaylistFragment() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.l_secondary_page_container, new OnlinePlaylistFragment())
                .addToBackStack("name4")
                .commit();
        showSecondPageContainer();
    }

    @Override
    public void hideTrackFragment() {
        hideSecondPageContainer();
    }

    @Override
    public void showTopListFragment() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.l_secondary_page_container, new TopListFragment())
                .addToBackStack("name5")
                .commit();
        showSecondPageContainer();
    }

    @Override
    public void hideTopListFragment() {
        hideSecondPageContainer();
    }

    @Override
    public void showBatchCheckSongFragment() {
        showBatchCheckSongFragment(null);
    }

    public void showBatchCheckSongFragment(Long songId) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.l_fifth_page_container, BatchEditPlayListFragment.newInstance(songId), "BatchEdit")
                .addToBackStack("name6")
                .commit();
        showFifthPageContainer();
    }

    @Override
    public void hideBatchCheckSongFragment() {
        hideFifthPageContainer();

        Log.v(TAG, "Current item: " + mVp.getCurrentItem());
        if (mVp.getCurrentItem() == 3) {
            libraryMineFragment.refreshData();
        }
    }

    @Override
    public boolean getKeyboardStatus() {
        return mKeyboardStatus;
    }

    @Override
    public boolean setKeyboardStatus(boolean keyboardStatus) {
        return mKeyboardStatus = false;
    }

    @Override
    public void showSongList(View view, SongCollection collection, CallbackListener listener) {
        //os6 not use this func
        /*if (mSongCollection != null && !mSongCollection.equals(collection)) {
            mSongCollection.clear();
        }
        mSongCollection = collection;
        this.mCallbackListener = listener;
        listener.doStuffAfterOpenSubWindow();
        mArrowView = view;
        resetPopupArrowView(view);

        int[] locScreen = new int[2];
        int[] lvLocScreen = new int[2];
        view.getLocationOnScreen(locScreen);
        mSongsLo.getLocationOnScreen(lvLocScreen);
        float y1 = locScreen[1] + view.getHeight() / 2.0f - lvLocScreen[1] - mCursorIv.getHeight() / 2.0f;
        mCursorIv.setY(y1);

        if (!mSongsLo.isShown()) {
            mSongsLo.startAnimation(mHorizontalShowAction);
        }
        switch (collection.getType()) {
            case ALBUM:
                mSongFooterView.setVisibility(View.VISIBLE);
                mSongsLo.setVisibility(View.VISIBLE);
                mSongAdapter.setData(collection);
                break;
            case PLAYLIST:
                mSongFooterView.setVisibility(View.GONE);
                mSongsLo.setVisibility(View.VISIBLE);

                try {
                    Playlist playlist = (Playlist) collection.getOwner();
                    switch (playlist.getType()) {
                        case ONLINE:
                            PlaylistHelper.getOnlinePlaylistSongs(LibraryActivity.this, playlist, new MyGetSongsListener(collection));
                            break;
                        case ALL_STAR:
                            PlaylistHelper.getAllStarSongs(LibraryActivity.this, playlist, new MyGetSongsListener(collection));
                            break;
                        case TOP_LIST_NEW:
                            PlaylistHelper.getTopListNewSongs(LibraryActivity.this, playlist, new MyGetSongsListener(collection));
                            break;
                        case TOP_LIST_HOT:
                            PlaylistHelper.getTopListHotSongs(LibraryActivity.this, playlist, new MyGetSongsListener(collection));
                            break;
//                        case ALL:
//                            mSongAdapter.setData(collection);
//                            break;
                        case DOWNLOAD:
                        	setDownloadListener();
                        default:
                            mSongAdapter.setData(mSongCollection);
                            break;
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
        }*/
    }

    //os 6 not use this func
    /*public class MyGetSongsListener implements PlaylistHelper.GetSongsListener {
        private SongCollection collection;

        public MyGetSongsListener(SongCollection collection) {
            this.collection = collection;
        }

        @Override
        public void onGotSongs(Playlist playlist) {
            Log.i(TAG, "Return from baidu api: " + playlist.getType().name() + "\t size: " + (playlist.getSongs() == null ? 0 : playlist.getSongs().size()));

            //FIXME: check the consistence of the two collection, to decide if the list will be refreshed or not.
//            if (mSongCollection != null && mSongCollection.equals(collection)) {
            mSongCollection.setOwner(playlist);
            DBService.matchSongs(mSongCollection.getSongs());
            mSongAdapter.setData(mSongCollection);
//            }
        }
    }*/

    private void resetPopupArrowView(View view) {
        //os 6 not use this func
        /*Object tagObj = view.getTag(R.id.view_hold);
        if (tagObj != null) {
            mPopupArrowIv = (ImageView) tagObj;
            if (!mSongsLo.isShown()) {
                mPopupArrowIv.startAnimation(mHorizontalShowAction);
            }
            mPopupArrowIv.setVisibility(View.VISIBLE);
        }*/
    }

    @Override
    public boolean hideSongList() {
        //os 6 not use this func
        /*if (mSongsLo.isShown()) {
            if (mCallbackListener != null) {
                mCallbackListener.doSutffAfterCloseSubWindow();
            }
            if (mPopupArrowIv != null) {
                mPopupArrowIv.startAnimation(mHorizontalHiddenAction);
                mSongsLo.startAnimation(mHorizontalHiddenAction);
                mPopupArrowIv.setVisibility(View.GONE);
            } else {
                mSongsLo.startAnimation(mHorizontalHiddenAction);
            }

            mSongsLo.setVisibility(View.GONE);
            removeDownloadListener();
            return true;
        }*/
        return false;
    }

    @Override
    public void showMiniPlayerView() {
        if (!mMiniPlayerView.isShown()) {
            mMiniPlayerView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_in_up));
            mMiniPlayerView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void hideMiniPlayerView() {
        if(true) {
            return;
        }
        if (mMiniPlayerView.isShown()) {
            mMiniPlayerView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_out_down));
            mMiniPlayerView.setVisibility(View.GONE);
        }
    }

    @Override
    public Song getLongClickSong() {
        return mLongClickSong;
    }

    @Override
    public List<Song> getSongs() {
        return mSongCollection.getSongs();
//        return mSongs;
    }

    @Override
    public void setSongCollection(SongCollection songCollection) {
        mSongCollection = songCollection;
    }

    @Override
    public SongCollection getSongCollection() {
        return mSongCollection;
    }

    @Override
    public void removeLocalSongsFromCollection(List<Song> songs) {
        try {
            for (Song song : songs) {
                DBService.removeSongFromMediaStore(song);
                //remove local file

                DBService.removeSongFile(song);
            }

            mSongCollection.exceptData(songs);
            Lewa.playerServiceConnector().removeSongs(songs);
        } catch (Exception e) {
            e.printStackTrace();
        }

        libraryArtistFragment.refreshArtistsData();	//for bug 60188
        if(null != mArtistAlbumFragment) {
            mArtistAlbumFragment.refreshAlbumsData();
        }
    }

    @Override
    public void removeSongsFromPlaylist(List<Song> songs, boolean removeFile) {
        List<Song> toBeRemovedPlayingSongs = new ArrayList<Song>();
        boolean isCollectionPlaying = mSongCollection.equals(Lewa.getPlayingCollection());

        try {
            for (Song song : songs) {
                Playlist playlist = (Playlist) mSongCollection.getOwner();
                if (playlist.getType() != Playlist.TYPE.ALL) {
                    //remove song from playlistsong
                    DBService.removePlaylistSong(playlist.getId(), song);
                }

                if (removeFile) {
                    if (song.getType() == Song.TYPE.LOCAL) {
                        DBService.removeSongFromMediaStore(song);
                    }
                    //remove local file
                    DBService.removeSongFile(song);
                }

                if (isCollectionPlaying || (removeFile && song.getType() == Song.TYPE.LOCAL)) {
                    toBeRemovedPlayingSongs.add(song);
                }
            }

            mSongCollection.exceptData(songs);
            SongCollection playCollection = Lewa.getPlayingCollection();
            if(null != playCollection) {
                playCollection.exceptData(songs);
            }
            Lewa.playerServiceConnector().removeSongs(toBeRemovedPlayingSongs);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        libraryArtistFragment.refreshArtistsData();	//for bug 60188
        if(null != mArtistAlbumFragment) {
            mArtistAlbumFragment.refreshAlbumsData();
        }
    }

    @Override
    public void showSearchFragment(SearchType type) {
        //if (mSongsLo.isShown()) {
            //hideSongList();
        //} else {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.l_fifth_page_container,SearchFragment.getInstance(type), "search")
                    .addToBackStack("name7")
                    .commit();
            mKeyboardStatus = true;
            showFifthPageContainer();
        //}
    }

    @Override
    public void hideSearchFragment() {
        mSongCollection = null;
        mKeyboardStatus = false;
        SearchFragment searchFragment = (SearchFragment) getSupportFragmentManager().findFragmentByTag("search");
        if (searchFragment != null) {
            searchFragment.hideKeyboard();
            searchFragment.updateSearchHistroy();
        }
        hideFifthPageContainer();
    }

    @Override
    public void onBack() {
        hideAlbumFragment();
    }


    protected void refreshSongsInfo() {
        refreshSongView();
        if(Lewa.getPlayingSong() == null) {
            mMiniCoverIv.setImageResource(R.drawable.cover);
            mMiniSongTv.setText("");
            mMiniArtistTv.setText("");
        }
    }
    @Override
    public void refreshSongView() {

        if (mSongInfoListContainer.isShown()) {
            mSongInfoListFragment.refreshSongsState(mSongCollection);
        }

        if(mVp.isShown()) {
            libraryLocalFragment.refreshSongListInfo();
            libraryMineFragment.refreshData();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mFifthPageContainer.isShown()) {
                Fragment batchEditFrag = getSupportFragmentManager().findFragmentByTag("BatchEdit");
                SearchFragment searchFragment = (SearchFragment) getSupportFragmentManager().findFragmentByTag("search");
                if (searchFragment != null) {
                    searchFragment.hideKeyboard();
                    searchFragment.updateSearchHistroy();
                }
                if (batchEditFrag != null) {
                    Log.v(TAG, "hide batch edit fragment.");
                    hideBatchCheckSongFragment();
                } else {
                    hideFifthPageContainer();
                }
                
                refreshSongView();
                return false;
            } else if(mSongInfoListContainer.isShown()) {
                hideSongInfoListFragment();
                Log.i(TAG, " mSongInfoListContainer");
                return false;
            /*} else if (mSongsLv.isShown()) {
                hideSongList();
                Log.i(TAG, " mSongsLv");
                return false;*/
            } else if (mFourthPageContainer.isShown()) {
                 Log.i(TAG, " hideFourthPageContainer");
                hideFourthPageContainer();
                return false;
            } else if (mThirdPageContainer.isShown()) {
                hideThirdPageContainer();
                Log.i(TAG, " mThirdPageContainer");
                return false;
            } else if (mSecondPageContainer.isShown()) {
                hideSecondPageContainer();
                Log.i(TAG, " mSecondPageContainer");
                return false;
            }
        }
        mKeyboardStatus = false;
        return super.onKeyDown(keyCode, event);
    }

    public Handler mHandler = new Handler() {
        public void handleMessage(Message message) {
            switch (message.what) {
                case UPDATE_BG_IMAGE:
                    Bitmap bitmap = (Bitmap) message.obj;
                    mCoverIv.setImageBitmap(bitmap);
                    break;
            }
        }
    };
    

    @Override
    public void showPlaylist() {

    }

    @Override
    public void showPlay() {

    }

    @Override
    public void play() {
        if (Lewa.getPlayingCollection() == null) {
            Lewa.playerServiceConnector().playSongCollection(LibraryActivity.this, new SongCollection(SongCollection.Type.PLAYLIST, new Playlist(Playlist.TYPE.ALL)), 0);
        } else {
            Lewa.playerServiceConnector().play();
        }
    }

    @Override
    public void playPlaylist(Playlist playlist) {

    }

    @Override
    public void playSong(Song song) {
//        Lewa.playerServiceConnector().playSong(song.toJsonString());
    }

    @Override
    public void pause() {
        Lewa.playerServiceConnector().pause();
    }

    @Override
    public void previous() {
        Lewa.playerServiceConnector().previous();
    }

    @Override
    public void next() {
        Lewa.playerServiceConnector().next();
    }

    public class MyPlayStatusListener implements PlayStatusListener {
        private WeakReference<LibraryActivity> ref = null;

        public MyPlayStatusListener(LibraryActivity libraryActivity) {
            this.ref = new WeakReference<LibraryActivity>(libraryActivity);
        }

        @Override
        public String getId() {
            return LibraryActivity.class.getName();
        }

        @Override
        public void onPlayStatusChanged(PlayStatus status) {
            LibraryActivity activity = ref.get();

            if (activity != null) {
                activity.refreshPlayStatus(status);

                SongInfoListFragment songInfoListFragment = (SongInfoListFragment)activity.getSupportFragmentManager().findFragmentByTag(FT_TAG_SONG_INFO_LIST);
                if(null != songInfoListFragment){
                	songInfoListFragment.refreshPlayStatus(status);
                }
                Song playSong = status.getPlayingSong();
                if(null != playSong) {
                    activity.mMiniSongTv.setText(playSong.getName());
                    //pr938966 add by wjhu begin
                    //if it is "<unknown>" replace it 
                    String artistName = playSong.getArtist().getName();
        			if (MediaStore.UNKNOWN_STRING.equals(artistName)) {
        				activity.mMiniArtistTv.setText(getString(
        						R.string.unknown_artist_name));
        			} else {
        				activity.mMiniArtistTv.setText(artistName);
        			}
        			//pr938966 add by wjhu end
                } else {
                    activity.mMiniSongTv.setText("");
                    activity.mMiniArtistTv.setText("");
                    activity.mMiniCoverIv.setImageResource(R.drawable.cover);	//for bug 65406 
                }
            }
        }

        @Override
        public void onBackgroundReady(Bitmap bitmap) {
            Log.i(TAG, "Got new background.");
            LibraryActivity activity = ref.get();

            if (activity != null) {
                activity.mCoverIv.setImageBitmap(bitmap);
                activity.mMiniCoverIv.setImageBitmap(bitmap);
                Song playingSong = Lewa.getPlayingSong();
                if(null != playingSong) {
                    activity.mMiniSongTv.setText(playingSong.getName());
                    String artistName = playingSong.getArtist().getName();
        			if (MediaStore.UNKNOWN_STRING.equals(artistName)) {
        				activity.mMiniArtistTv.setText(getString(
        						R.string.unknown_artist_name));
        			} else {
        				activity.mMiniArtistTv.setText(artistName);
        			}
                }
            }
        }

        @Override
        public void onBluredBackgroundReady(Bitmap bitmap) {
            LibraryActivity activity = ref.get();

            if (activity != null) {
                activity.mCoverIv.setImageBitmap(bitmap);
            }
        }

        @Override
        public void onStartGetBackground() {
            LibraryActivity activity = ref.get();

            if (activity != null) {
                activity.setDefaultBgs();
            }
        }

        @Override
        public void onSongDownloaded(Long onlineId, Long localId) {
            LibraryActivity activity = ref.get();

            if (activity != null) {
                //activity.mSongAdapter.songDownloaded();
                SearchFragment searchFragment = (SearchFragment)activity.getSupportFragmentManager().findFragmentByTag("search");
                if(searchFragment!=null){
                	searchFragment.onSongsDownloaded();
                }

                SongInfoListFragment songInfoListFragment = (SongInfoListFragment)activity.getSupportFragmentManager().findFragmentByTag(FT_TAG_SONG_INFO_LIST);
                if(null != songInfoListFragment){
                	songInfoListFragment.onSongsDownloaded();
                }

                int currItem = mVp.getCurrentItem();
                if (currItem != -1 && currItem < mFragmentAdapter.getCount()) {
                    Fragment fragment = mFragmentAdapter.getItem(currItem);
                    if (fragment != null) {
                        fragment.onResume();
                    }
                }
            }
        }
    }

    public void setDefaultBgs() {
        //mCoverIv.setImageResource(R.drawable.cover);
        //new Thread(new BlurRunnable(this, mPlayStatusListener)).start();

        BlurRunnable.blurDefaultBg(this.getResources(), mPlayStatusListener);
        mMiniCoverIv.setImageResource(R.drawable.cover);

        Song playingSong = Lewa.getPlayingSong();
        if(null != playingSong) {
            mMiniSongTv.setText(playingSong.getName());
            String artistName = playingSong.getArtist().getName();
			if (MediaStore.UNKNOWN_STRING.equals(artistName)) {
				mMiniArtistTv.setText(getString(
						R.string.unknown_artist_name));
			} else {
				mMiniArtistTv.setText(artistName);
			}
        } else {
            mMiniSongTv.setText("");
            mMiniArtistTv.setText("");
        }
        
    }

    @Override
    void refreshPlayStatus(PlayStatus playStatus) {
        Song playingSong = playStatus.getPlayingSong();

        if (playStatus.isPlaying() && null != playingSong) {
            mPlayBtn.setImageDrawable(Lewa.resources().getDrawable(R.drawable.se_btn_pause_small));
        } else {
            mPlayBtn.setImageDrawable(Lewa.resources().getDrawable(R.drawable.se_btn_play_small));
        }
    }

	public void onBackPressed() {
        if(null != PlayActivity.playInstance) {
            PlayActivity.playInstance.finish();
            PlayActivity.playInstance = null;
        }
        getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        super.onBackPressed();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ExitApplication exit = (ExitApplication) getApplication();
        exit.rmAActivity(this);
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        LewaUtils.logE(TAG, "onConfigurationChanged");
        if(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            this.libraryLocalFragment.setFastIndexVisibility(false);
            this.libraryArtistFragment.setFastIndexVisibility(false);
        } else {
            this.libraryLocalFragment.setFastIndexVisibility(true);
            this.libraryArtistFragment.setFastIndexVisibility(true);
        }
    }

	@Override
	public void onAccountTokenInvalid() {
		// TODO Auto-generated method stub
		LewaUtils.logE(TAG, "onAccountTokenInvalid");
	}

	@Override
	public void onOrdinaryInvalid() {
		// TODO Auto-generated method stub
		LewaUtils.logE(TAG, "onOrdinaryInvalid");
		OAuthManager.getInstance(getApplicationContext()).authorize(new onAuthorizeFinishListener() {
			@Override
			public void onAuthorizeFinish(int result) {
				LewaUtils.logE(TAG, "onAuthorizeFinish result = "+result);
			}
		});
	}

       //os 6 not use this func
	/*public void initAppDownloadManager(){
		if(appDownloadManager==null)
			appDownloadManager=AppDownloadManager.getInstance(getApplicationContext());
	}
	
	public void setDownloadListener(){
		LewaUtils.logE(TAG, "setDownloadListener");
		initAppDownloadManager();
		appDownloadManager.setOnDownloadProgressChangeListener(mSongAdapter);
		appDownloadManager.setOnDownloadStatusChangeListener(mSongAdapter);
	}
	
	public void removeDownloadListener(){
		if(appDownloadManager!=null){
			appDownloadManager.setOnDownloadProgressChangeListener(null);
			appDownloadManager.setOnDownloadStatusChangeListener(null);
		}
	}

        
	private class CustomOnitemClickListener implements OnItemClickListener{
		private OnDownloadSongClickListener onDownloadSongClickListener;
		private Context mContext;
		public CustomOnitemClickListener(Context context,OnDownloadSongClickListener onDownloadSongClickListener){
			this.onDownloadSongClickListener=onDownloadSongClickListener;
			this.mContext=context;
		}
		@Override
		public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
            Song song = (Song) mSongAdapter.getItem(position);
			if(onDownloadSongClickListener!=null){
				boolean isDownloading=onDownloadSongClickListener.onDownloadSongClick(mContext, song);
				if(isDownloading)
					return;
			}
			 if (song.getType() == Song.TYPE.ONLINE) {
                 prepareForPlay(new MyCallbackPlayListener(song, position));
             } else {
                 Lewa.playerServiceConnector().playSongCollection(LibraryActivity.this, mSongAdapter.getSongCollection(), position);
             }
		}

	}*/
}
