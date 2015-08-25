package com.lewa.player.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.lewa.Lewa;
import com.lewa.kit.ActivityHelper;
import com.lewa.kit.MyVolley;
import com.lewa.player.R;
import com.lewa.player.adapter.PlayFragmentAdapter;
import com.lewa.player.adapter.PlayHistoryAdapter;
import com.lewa.player.adapter.PlayingSongAdapter;
import com.lewa.player.db.DBService;
import com.lewa.player.fragment.PortLyricFragment;
import com.lewa.player.fragment.PortPlayFragment;
import com.lewa.player.fragment.PortPlayListFragment;
import com.lewa.player.helper.AnimationHelper;
import com.lewa.player.helper.PlaylistHelper;
import com.lewa.player.helper.ViewHelper;
import com.lewa.player.listener.PlayControlListener;
import com.lewa.player.listener.PlayStatusListener;
import com.lewa.player.model.PlayStatus;
import com.lewa.player.model.Playlist;
import com.lewa.player.model.Song;
import com.lewa.player.model.SongCollection;
import com.lewa.util.Blur;
import com.lewa.util.Constants;
import com.lewa.util.StringUtils;
import com.lewa.view.ImageViewParallax;
import com.lewa.view.MaskImage.MaskImageView;

import java.lang.ref.WeakReference;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import lewa.support.v7.app.ActionBar;
import lewa.support.v7.app.ActionBarActivity;
import android.view.WindowManager; 


public class PlayActivity extends BaseFragmentActivity implements View.OnClickListener, PlayControlListener, View.OnTouchListener {
    //private static final String TAG = PlayActivity.class.getName();
	private static final String TAG = "PlayActivity";
    public static final String ARG_PLAY = "PLAY";
    public static final int GO_LIBRARY_ACTION = 2;

    public static final String IS_PLAY_HIDDEN = "IS_PLAY_HIDDEN";
    public static final String PLAY_CURRENT_ITEM = "PLAY_CURRENT_ITEM";
    public static final String VIEW_FLAG = "VIEW_FLAG";
	public static final String FROM_WIDGET = "FROM_WIDGET";

    private static int mPlayCurrentItem = 1;
    private static boolean isPlayHidden = false;

    private ImageButton mBtLibraryInPlay;
    private ViewPager mPlayVp;
    private ViewGroup mPlayLo;
    //private ViewGroup mPlaylistLo;
    private PlayFragmentAdapter mFragmentAdapter;
    //private ViewPager mPlayHistoryVp;
    private MaskImageView mMiniCover;
    //private View mPlaylistVpContainer;
    private TextView mPlaylistName;
    //private PlayHistoryAdapter mPlayHistoryAdapter;
    private RelativeLayout mMaskLo;
    //private ImageButton mHomeBtn;
    private ImageButton mCoverBtn;
    private ImageView mBgIv;
    private ImageView mCleanBgIv;
    private ImageViewParallax mBlurredIv;
    private ImageView mFullBlurredIv;
    private LinearLayout mMask;
    ImageView mLoCopyright;

    private GestureDetector.SimpleOnGestureListener mScrollGestureListener = null;
    private GestureDetector mScrollGestureDector = null;

    private List<SongCollection> mPlayHistories = new ArrayList<SongCollection>();

    //play land
    private ListView mSongLv;
    private ImageView mLandPlayCoverIv;
    private View mLandPlayBtn;
    private ImageView mLandPlayIv;
    //private TextView mSongNameTv;
    private TextView mLandAlbumNameTv;
    //private TextView mLandSongNameTv1;
    private TextView mLandSongNameTv2;
    private PlayingSongAdapter mPlayingSongAdapter;

    //playhistory land
    private TextView mPlayListLandName;
    private TextView mPlayListLandSongName;

    int mPlayMaskMaxAlpha = 0;
    int mPlaylistMaskMaxAlpha = 0;
    int mMaxRadius = 0;
    private int mViewFlag = 0;
    private static final int VIEW_PLAY = 0;
    private static final int VIEW_PLAYLIST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //getWindow().getDecorView().setSystemUiVisibility(0x10000000 | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        mMaxRadius = Lewa.resources().getInteger(R.integer.cover_blur_radius);
        //hasBackground = true;
        if (savedInstanceState != null) {
            isPlayHidden = savedInstanceState.getBoolean(IS_PLAY_HIDDEN, false);
            mPlayCurrentItem = savedInstanceState.getInt(PLAY_CURRENT_ITEM, 1);
        }

        Bundle extras = getIntent().getExtras();
        if(null != extras) {
            boolean isFrom = extras.getBoolean(FROM_WIDGET, false);
            if(isFrom) {	//launch this activity from widget
                mPlayCurrentItem = 1; 
                mViewFlag = VIEW_PLAY;
            }
        }

        View rootView;
        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            rootView = initCommonViews(PlayHistoryAdapter.ViewType.PORTRAIT);
            initPortExtraViews(rootView);
        } else {
            rootView = initCommonViews(PlayHistoryAdapter.ViewType.LANDSCAPE);
            initLandExtraViews(rootView);
            ViewHelper.hideStatusBar(this);
        }
        
        mPlayStatusListener = new MyPlayStatusListener(this);

        mPlayMaskMaxAlpha = Math.round(Lewa.resources().getInteger(R.integer.play_mask_max_alpha_percent) * 2.55f);
        mPlaylistMaskMaxAlpha = Math.round(Lewa.resources().getInteger(R.integer.playlist_mask_max_alpha_percent) * 2.55f);
	 playInstance = this;
    }

    private View initCommonViews(PlayHistoryAdapter.ViewType viewType) {
        View rootView;

        if (viewType == PlayHistoryAdapter.ViewType.PORTRAIT) {
            rootView = (ViewGroup) getLayoutInflater().inflate(R.layout.activity_play, null);
        } else {
            rootView = (ViewGroup) getLayoutInflater().inflate(R.layout.activity_play_land, null);
        }

        setContentView(rootView);

         
        mPlayLo = (ViewGroup) rootView.findViewById(R.id.rl_play);
        //mSongNameTv = (TextView) rootView.findViewById(R.id.tv_song_name);
        mBgIv = (ImageView) rootView.findViewById(R.id.iv_cover);
        mFullBlurredIv = (ImageView) rootView.findViewById(R.id.iv_full_blurred);
        //mPlaylistLo = (ViewGroup) rootView.findViewById(R.id.rl_playlist);
        //mPlayHistoryVp = (ViewPager) rootView.findViewById(R.id.vp_playlist);
        //mPlaylistVpContainer = rootView.findViewById(R.id.rlo2);
        //mPlaylistName = (TextView) rootView.findViewById(R.id.tv_playlist_name);
        mMask = (LinearLayout) rootView.findViewById(R.id.lo_mask);
        mLoCopyright = (ImageView) rootView.findViewById(R.id.lo_copyright);

        //mPlayHistoryAdapter = new PlayHistoryAdapter(viewType, this);
        //mPlayHistoryVp.setAdapter(mPlayHistoryAdapter);
        //mPlayHistoryVp.setOffscreenPageLimit(3);
        //mPlayHistoryVp.setPageMargin(getResources().getDimensionPixelSize(R.dimen.page_margin));
        //mPlayHistoryVp.setOnPageChangeListener(new PlaylistOnPageChangeListener());

        //mHomeBtn = (ImageButton) rootView.findViewById(R.id.bt_home);
        //mHomeBtn.setOnClickListener(this);

        /*mPlaylistVpContainer.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // dispatch the events to the ViewPager, to solve the problem that we can swipe only the middle view.
                return mPlayHistoryVp.dispatchTouchEvent(event);
            }
        });*/

        return rootView;
    }

    private void initPortExtraViews(View rootView) {
	//add this for bug 65412 start
        final WindowManager.LayoutParams attrs = getWindow().getAttributes();
      attrs.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
      //getWindow().setAttributes(attrs);
      //getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
     //add this for bug 65412 end
        mCleanBgIv = (ImageView) rootView.findViewById(R.id.iv_origin_cover);
        mBlurredIv = (ImageViewParallax) rootView.findViewById(R.id.iv_blurred);
        mBtLibraryInPlay = (ImageButton) rootView.findViewById(R.id.bt_playlist);
        mMiniCover = (MaskImageView) rootView.findViewById(R.id.iv_mini_cover);
        mPlayVp = (ViewPager) rootView.findViewById(R.id.vp_fragment);
        //mPlaylistName = (TextView) rootView.findViewById(R.id.tv_playlist_name);

        //mMaskLo = (RelativeLayout) rootView.findViewById(R.id.rlo3);
        //mMaskLo.setOnTouchListener(this);
        mScrollGestureListener = new MyScrollGestureListener();
        mScrollGestureDector = new GestureDetector(this, mScrollGestureListener);
        rootView.findViewById(R.id.lo_library_trigger).setOnTouchListener(this);

        List<Fragment> fragments = new ArrayList<Fragment>();
        fragments.add(new PortPlayListFragment());
        fragments.add(new PortPlayFragment());
        fragments.add(new PortLyricFragment());
        mFragmentAdapter = new PlayFragmentAdapter(getSupportFragmentManager(), fragments);
        mPlayVp.setAdapter(mFragmentAdapter);
        mPlayVp.setOnPageChangeListener(new PlayOnPageChangeListener());
        mPlayVp.setCurrentItem(mPlayCurrentItem);
        mBtLibraryInPlay.setOnClickListener(this);
    }

    private void initLandExtraViews(View rootView) {
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);//add this for bug 65412 
        mSongLv = (ListView) rootView.findViewById(R.id.lv_song);
        mLandPlayCoverIv = (ImageView) rootView.findViewById(R.id.iv);
        mLandPlayBtn = rootView.findViewById(R.id.bt_play);
        mLandPlayBtn.setOnClickListener(this);
        mLandPlayIv = (ImageView) rootView.findViewById(R.id.iv_play_btn);
        mLandAlbumNameTv = (TextView) rootView.findViewById(R.id.tv_album_name);
        //mLandSongNameTv1 = (TextView) rootView.findViewById(R.id.tv_song_name);
        mLandSongNameTv2 = (TextView) rootView.findViewById(R.id.tv_song_name2);
        mFullBlurredIv.setVisibility(View.VISIBLE);
        //mCoverBtn = (ImageButton) rootView.findViewById(R.id.bt_cover);
        //mCoverBtn.setOnClickListener(this);
        mPlayLo.setOnTouchListener(this);

        mPlayingSongAdapter = new PlayingSongAdapter(this);
        mSongLv.setAdapter(mPlayingSongAdapter);
        mSongLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Lewa.playerServiceConnector().setQueuePosition(position);
            }
        });
    }

	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		//Log.i(TAG, "onNewIntent");
		setIntent(intent);

		
	}

    @Override
    public void onResume() {
    	Log.i(TAG, "onResume mViewFlag = " + mViewFlag);
		
		Bundle extras = getIntent().getExtras();
		if(null != extras) {
			boolean isFrom = extras.getBoolean(FROM_WIDGET, false);
			
			if(isFrom) {	//launch this activity from widget
				mPlayCurrentItem = 1; 
				mViewFlag = VIEW_PLAY;
				if(mPlayVp != null) {
					mPlayVp.setCurrentItem(mPlayCurrentItem);
				 }
				//Log.i(TAG, "************************");
			}
			//Log.i(TAG, "^^^^^^^^^^^^^");
		} 
		
		
        isPlayHidden = false;

        if (mViewFlag == VIEW_PLAY) {
            showPlay();
        } else {
            //showPlaylist();
        }

		// PR 940662 add by wjhu begin
		// load the cover
		Song playSong = Lewa.getPlayingSong();
		if (playSong != null) {
			Lewa.loadArtistAvatar(playSong.getArtist().getName(),
					mPlayStatusListener);
		}

		// PR 940662 add by wjhu end
        super.onResume();
    }

	public void finish() {
        super.finish();
		playInstance = null;
        ActivityHelper.leavePlayAnim(this);
    }
 

	public static ActionBarActivity playInstance = null;
    @Override
    public void onDestroy() {
        Lewa.unregisterPlayStatusListener(mPlayStatusListener);
        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle bundle) {
        if (bundle == null) {
            bundle = new Bundle();
        }

        bundle.putBoolean(IS_PLAY_HIDDEN, isPlayHidden);
        bundle.putInt(PLAY_CURRENT_ITEM, mPlayCurrentItem);
        bundle.putInt(VIEW_FLAG, mViewFlag);
        super.onSaveInstanceState(bundle);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        if (savedInstanceState != null) {
            mViewFlag = savedInstanceState.getInt(VIEW_FLAG);
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int vid = v.getId();

        if (R.id.lo_library_trigger == vid) {
            return mScrollGestureDector.onTouchEvent(event);
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        int vid = v.getId();

        switch (vid) {
            case R.id.bt_playlist:
                ActivityHelper.goLibraryAnim(this);
                break;
            case R.id.bt_play:
                Boolean isPlaying = false;
                if (mLandPlayBtn.getTag() != null) {
                    isPlaying = (Boolean) mLandPlayBtn.getTag();
                }

                if (isPlaying) {
                    pause();
                } else {
                    play();
                }

                break;
            case R.id.lo_playlist:
                SongCollection songCollection = (SongCollection) v.getTag(R.id.tag_entity);
                boolean playingTag = (Boolean) v.getTag(R.id.tag_type);

                if (playingTag) {
                    Lewa.playerServiceConnector().pause();
                } else {
                    Log.i(TAG, "Sc id: " + songCollection.getId() + ", Play sc id: " + (Lewa.getPlayingCollection() == null ? "" : Lewa.getPlayingCollection().getId()));
                    if (songCollection.equals(Lewa.getPlayingCollection())) {
                        Log.i(TAG, "Resume from pause.");
                        Lewa.playerServiceConnector().play();
                        showPlay();
                    } else {
                        songCollection.reset();
                        Log.i(TAG, "Play song collection: " + songCollection.toString());
                        Log.i(TAG, "Song number: " + songCollection.getCount());
                        switch (songCollection.getType()) {
                            case ALBUM:
                            case SINGLE:
                                Lewa.playerServiceConnector().playSongCollection(this, songCollection, -1);
                                showPlay();
                                break;
                            case PLAYLIST:
                                try {
                                    Playlist playlist = (Playlist) songCollection.getOwner();
                                    switch (playlist.getType()) {
                                        case ONLINE:
                                            PlaylistHelper.getOnlinePlaylistSongs(PlayActivity.this, playlist, new MyGetSongsListener(songCollection));
                                            break;
                                        case ALL_STAR:
                                            PlaylistHelper.getAllStarSongs(PlayActivity.this, playlist, new MyGetSongsListener(songCollection));
                                            break;
                                        case TOP_LIST_NEW:
                                            PlaylistHelper.getTopListNewSongs(PlayActivity.this, playlist, new MyGetSongsListener(songCollection));
                                            break;
                                        case TOP_LIST_HOT:
                                            PlaylistHelper.getTopListHotSongs(PlayActivity.this, playlist, new MyGetSongsListener(songCollection));
                                            break;
                                        case FM:
                                            playlist.setId(Long.valueOf(playlist.getBdCode()));
                                            PlaylistHelper.getFMSongs(PlayActivity.this, playlist, new MyGetSongsListener(songCollection));
                                            break;
//                        case ALL:
//                            mSongAdapter.setData(collection);
//                            break;
                                        default:
                                            Lewa.playerServiceConnector().playSongCollection(this, songCollection, -1);
                                            showPlay();
                                            break;
                                    }

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                break;
                        }
                    }
                }

                break;
            case  R.id.bt_download:
                 Song downloadSong = (Song) v.getTag(R.id.tag_entity);
                if(null != downloadSong) {
                    Lewa.downloadSong(downloadSong);
                    if(null != mPlayingSongAdapter) {
                        mPlayingSongAdapter.notifyDataSetChanged();
                    }
                }
            default:
                break;
        }
    }

    public class MyGetSongsListener implements PlaylistHelper.GetSongsListener {
        private SongCollection collection;

        public MyGetSongsListener(SongCollection collection) {
            this.collection = collection;
        }

        @Override
        public void onGotSongs(Playlist playlist) {
            Log.i(TAG, "Return from baidu api: " + playlist.getType().name() + "\t size: " + (playlist.getSongs() == null ? 0 : playlist.getSongs().size()));

            collection.setOwner(playlist);
            Lewa.playerServiceConnector().playSongCollection(PlayActivity.this, collection, -1);
            showPlay();
        }
    }

    @Override
    public void showPlaylist() {
        /*isPlayHidden = true;
        mViewFlag = VIEW_PLAYLIST;
        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            mMask.setBackgroundResource(R.color.a40_black);
            //mCoverBtn.setVisibility(View.GONE);
            mHomeBtn.setVisibility(View.VISIBLE);
            ViewHelper.hideStatusBar(this);
        } else {
            mMask.setBackgroundResource(R.drawable.d_bg_gradient_black_0_50);
            mBgIv.setVisibility(View.VISIBLE);
        }
//        mBlurredIv.setVisibility(View.VISIBLE);
        mFullBlurredIv.setVisibility(View.VISIBLE);
        mPlaylistLo.setVisibility(View.VISIBLE);
        mPlayLo.setVisibility(View.GONE);
        refreshPlayHistories();*/
    }

    @Override
    public void showPlay() {
        isPlayHidden = false;
        mViewFlag = VIEW_PLAY;
        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            mMask.setBackgroundResource(R.color.a40_black);
            mFullBlurredIv.setVisibility(View.VISIBLE);
            mBgIv.setVisibility(View.GONE);
            //mCoverBtn.setVisibility(View.VISIBLE);
            //mHomeBtn.setVisibility(View.GONE);
            ViewHelper.hideStatusBar(this);
            mPlayingSongAdapter.setData(PlaylistHelper.getPlayingSongs());
            PlaylistHelper.scrollToPlayingSong(mSongLv, mPlayingSongAdapter);
        } else {
            mMask.setBackgroundResource(R.drawable.d_bg_gradient_black_0_90);
//            mBlurredIv.setVisibility(View.GONE);
            mFullBlurredIv.setVisibility(View.GONE);
//            mPlayVp.setCurrentItem(1);
            resetBg(1);
        }
        //mPlaylistLo.setVisibility(View.GONE);
        mPlayLo.setVisibility(View.VISIBLE);
    }

    @Override
    public void play() {
        if (Lewa.getPlayingCollection() == null ||Lewa.getPlayingCollection().getCount() == 0 ) {
            Lewa.playerServiceConnector().playSongCollection(PlayActivity.this, new SongCollection(SongCollection.Type.PLAYLIST, new Playlist(Playlist.TYPE.ALL)), 0);
        } else {
            Lewa.playerServiceConnector().play();
        }
    }

    @Override
    public void playPlaylist(Playlist playlist) {
    }

    @Override
    public void playSong(Song song) {
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

    class PlayOnPageChangeListener implements ViewPager.OnPageChangeListener {
        int alpha = 0;
        int blurRadius = 0;

        @Override
        public void onPageScrollStateChanged(int position) {
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
//            Log.v(TAG, "Page scrolled: " + positionOffset + "\t" + positionOffsetPixels + "px");

            float curRadius = 0f;
//            Log.i(TAG, "Position: " + position + ", offset: " + positionOffset + ", pixels: " + positionOffsetPixels);
            float offset = positionOffset > 1 ? 1 : positionOffset;
            if (position != 1) {
                alpha = Math.round(mPlayMaskMaxAlpha * (1 - offset));
                curRadius = mMaxRadius * (1 - offset);
//                curRadius = curRadius < 1 ? 0 : curRadius - 1;
            } else {
                alpha = Math.round(mPlayMaskMaxAlpha * offset);
                curRadius = mMaxRadius * offset;
            }

            /*if (alpha.length() == 0) {
                alpha = "00";
            } else if (alpha.length() == 1) {
                alpha = "0" + alpha;
            }
            
            String color = "#" + alpha + "000000";*/
            if(alpha < 0) {
            	alpha = 0; 
            }
            if(alpha > 255) {
            	alpha = 255;
            }
//            Log.d(TAG, "ViewPager background color: " + color);
            mPlayVp.setBackgroundColor(Color.argb(alpha , 0, 0, 0));

            double radius = Math.ceil(curRadius) - 1;
            radius = radius < 1 ? 0 : radius + 0;
            if (radius != blurRadius) {
                blurRadius = (int) radius;
                Bitmap originBm = ((BitmapDrawable) mBgIv.getDrawable()).getBitmap();
                
                if (originBm != null) {
                    Log.i(TAG, "Blur radius: " + blurRadius);
//                    mHandler.postDelayed(new BlurTask(originBm, Math.round(curRadius)), 0);
                    mHandler.postDelayed(new BlurTask(originBm, blurRadius), 0);
                    mBlurredIv.reset(position, positionOffset);
                    mBlurredIv.postInvalidate();
//                    else {
//                        mBlurredIv.setImageBitmap(((BitmapDrawable) mCleanBgIv.getDrawable()).getBitmap());
//                    }
                }
            }
        }

        @Override
        public void onPageSelected(int position) {
            mPlayCurrentItem = position;

            if (position == 1) {
//                mBlurredIv.setImageBitmap(((BitmapDrawable) mCleanBgIv.getDrawable()).getBitmap());

                Log.i(TAG, "Page 1 selected.");
            }
        }
    }

    private class BlurTask extends Thread {
        Bitmap originBm;
        int radius;

        public BlurTask(Bitmap originBm, int radius) {
            this.originBm = originBm;
            this.radius = radius;
        }

        @Override
        public void run() {

            if (radius == 0) {
                mBlurredIv.setImageBitmap(((BitmapDrawable) mCleanBgIv.getDrawable()).getBitmap());
                Log.i(TAG, "No blur needed.");
            } else {
                Bitmap bluredBm = Blur.createBlurBitmap(originBm, radius);
                mFullBlurredIv.setVisibility(View.GONE);
                mBlurredIv.setImageBitmap(bluredBm);
            }
        }
    }

    TypeEvaluator typeEvaluator = AnimationHelper.getIntEvaluator();

    /*public class PlaylistOnPageChangeListener implements ViewPager.OnPageChangeListener {

        @Override
        public void onPageSelected(int position) {
            Log.i(TAG, "Page selected: " + position);

            final View view = mPlayHistoryVp.findViewWithTag(position);

            if (view != null) {
                final MaskImageView iv = (MaskImageView) view.findViewById(R.id.iv);
                final int largeSize = getResources().getDimensionPixelSize(R.dimen.playlist_cover_large_size);
                final int smallSize = getResources().getDimensionPixelSize(R.dimen.playlist_cover_small_size);

                Log.v(TAG, "Animate from " + iv.getSize() + " to " + largeSize);
                final int ivSize = iv.getSize();
                ValueAnimator upValueAnimator = ValueAnimator.ofObject(typeEvaluator, ivSize, largeSize);
                upValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        int curSize = (Integer) animation.getAnimatedValue();
                        iv.setMaskColor(-1);
                        iv.setSize(curSize);
                    }
                });

                upValueAnimator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        view.findViewById(R.id.iv_highlight).setVisibility(View.VISIBLE);
                        view.findViewById(R.id.llo2).setVisibility(View.VISIBLE);
                    }
                });

                upValueAnimator.setDuration(500);
                AnimatorSet scale = new AnimatorSet();

                if (mPlayHistoryAdapter.getCurrentPage() != position) {
                    View currentPage = mPlayHistoryVp.findViewWithTag(mPlayHistoryAdapter.getCurrentPage());

                    if (currentPage != null) {
                        final MaskImageView currentIv = (MaskImageView) currentPage.findViewById(R.id.iv);
                        currentPage.findViewById(R.id.iv_highlight).setVisibility(View.GONE);
                        currentPage.findViewById(R.id.llo2).setVisibility(View.GONE);

                        Log.v(TAG, "Animate from " + currentIv.getSize() + " to " + smallSize);

                        final int curIvSize = currentIv.getSize();
                        ValueAnimator downValueAnimator = ValueAnimator.ofObject(typeEvaluator, curIvSize, smallSize);
                        downValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

                            @Override
                            public void onAnimationUpdate(ValueAnimator animation) {
                                int curSize = (Integer) animation.getAnimatedValue();

                                String alpha = Integer.toHexString(Math.round(mPlaylistMaskMaxAlpha * (largeSize - curSize) / (largeSize - smallSize)));
                                if (alpha.length() == 0) {
                                    alpha = "00";
                                } else if (alpha.length() == 1) {
                                    alpha = "0" + alpha;
                                }

                                String color = "#" + alpha + "000000";

                                currentIv.setMaskColor(Color.parseColor(color));
//                                Log.i(TAG, "Mask color: " + color);

                                currentIv.setSize(curSize);
                            }
                        });
                        downValueAnimator.setDuration(500);
                        scale.play(upValueAnimator).with(downValueAnimator);
                    }
                } else {
                    scale.play(upValueAnimator);
                }

                scale.start();

                SongCollection playHistory = mPlayHistories.get(position);

                //mPlaylistName.setText(playHistory.getName());

                if (playHistory.getLastSong() != null && null != playHistory.getLastSong().getName()) {
                    mSongNameTv.setText(playHistory.getLastSong().getName());
                }
                mPlayHistoryAdapter.setCurrentPage(position);
                for (int i = 0; i < mPlayHistories.size(); i++) {
                    SongCollection songCollection = mPlayHistories.get(i);
                    View vpItem = mPlayHistoryVp.findViewWithTag(i);

                    if (vpItem != null) {
                        if (i == position) {
//                            vpItem.findViewById(R.id.iv_highlight).setVisibility(View.VISIBLE);
                        } else {
                            vpItem.findViewById(R.id.iv_highlight).setVisibility(View.GONE);
                        }
                    }
                }
            }
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            //if (mPlaylistVpContainer != null) {
                //mPlaylistVpContainer.invalidate();
            //}
        }

        @Override
        public void onPageScrollStateChanged(int arg0) {
        }
    }*/

    private class MyScrollGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDown(MotionEvent e) {
            Log.d(TAG, "Touch down on library trigger layout");
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2,
                                float distanceX, float distanceY) {
            Log.d(TAG, "Scroll distance: " + distanceX + ", " + distanceY);

            if (distanceY < -20) {
                ActivityHelper.goLibraryAnim(PlayActivity.this);
            }
            return false;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            Log.d(TAG, "On fling. ");
            return super.onFling(e1, e2, velocityX, velocityY);
        }
    }

    public Handler mHandler = new Handler() {
        public void handleMessage(Message message) {
            switch (message.what) {
                case GO_LIBRARY_ACTION:
                    Intent intent = new Intent(PlayActivity.this, LibraryActivity.class);
                    startActivity(intent);
                    //overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    break;
            }
        }
    };

    @Override
    public void onBackPressed() {
        if (isPlayHidden) {
            showPlay();
        } else {
            super.onBackPressed();
        }
    }

    public static class MyPlayStatusListener implements PlayStatusListener {
        private WeakReference<PlayActivity> ref = null;

        public MyPlayStatusListener(PlayActivity activity) {
            this.ref = new WeakReference<PlayActivity>(activity);
        }

        @Override
        public String getId() {
            return PlayActivity.class.getName();
        }

        @Override
        public void onPlayStatusChanged(PlayStatus status) {
            PlayActivity activity = ref.get();

            if (activity != null) {
                activity.refreshPlayStatus(status);
            }
        }

        @Override
        public void onBackgroundReady(Bitmap bitmap) {
            Log.i(TAG, "Got new background.");
            PlayActivity activity = ref.get();

            if (activity != null) {
                if(null != activity.mBgIv) activity.mBgIv.setImageBitmap(bitmap);

                if (activity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                    if(null != activity.mCleanBgIv) activity.mCleanBgIv.setImageBitmap(bitmap);
                    if(null != activity.mBlurredIv) activity.mBlurredIv.setImageBitmap(bitmap);
                } else {
                    if(null != activity.mLandPlayCoverIv) activity.mLandPlayCoverIv.setImageBitmap(bitmap);
                }
                if(null != activity.mFullBlurredIv) activity.mFullBlurredIv.setImageBitmap(Blur.createBlurBitmap(bitmap, 10));
            }
        }

        @Override
        public void onBluredBackgroundReady(Bitmap bitmap) {
            PlayActivity activity = ref.get();

            if (activity != null) {
                if (activity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                    if (activity.mPlayVp.getCurrentItem() != 1) {
                        activity.mBlurredIv.setImageBitmap(bitmap);
                    }
                }
                activity.mFullBlurredIv.setImageBitmap(bitmap);
            }
        }

        @Override
        public void onStartGetBackground() {
            PlayActivity activity = ref.get();

            if (activity != null) {
                activity.mBgIv.setImageResource(R.drawable.cover);

                if (activity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                    if(null != activity.mCleanBgIv) activity.mCleanBgIv.setImageResource(R.drawable.cover);
                    if(null != activity.mBlurredIv) activity.mBlurredIv.setImageResource(R.drawable.cover);
                } else {
                    if(null != activity.mLandPlayCoverIv) activity.mLandPlayCoverIv.setImageResource(R.drawable.cover);
                }
                Bitmap originBm = ((BitmapDrawable) activity.mBgIv.getDrawable()).getBitmap();
                activity.mFullBlurredIv.setImageBitmap(Blur.createBlurBitmap(originBm, 10));
            }
        }

        @Override
        public void onSongDownloaded(Long onlineId, Long localId) {
            PlayActivity activity = ref.get();

            if (activity != null && activity.mPlayingSongAdapter != null) {
                activity.mPlayingSongAdapter.songDownloaded();
            }
        }

    }

    @Override
    public void resetBg(int position) {
        mBlurredIv.reset(position, 0);
        mBlurredIv.postInvalidate();
    }

    @Override
    void refreshPlayStatus(PlayStatus playStatus) {
        if (playStatus == null) {
            return;
        }

        Song playingSong = playStatus.getPlayingSong();

        if (playingSong != null) {
            if (playingSong.getAlbum() != null && playingSong.getAlbum().getName() != null && mLandAlbumNameTv != null) {
                mLandAlbumNameTv.setText(playingSong.getAlbum().getName());
            }
            //mSongNameTv.setText(playingSong.getName());
            if (playingSong.getName() != null && mLandSongNameTv2 != null) { //&& mLandSongNameTv1 != null) {
                //mLandSongNameTv1.setText(playingSong.getName());
                mLandSongNameTv2.setText(playingSong.getName());
            }

            if (mLoCopyright != null) {
                if (playingSong.getType() == Song.TYPE.ONLINE) {
                    mLoCopyright.setVisibility(View.VISIBLE);
                } else {
                    mLoCopyright.setVisibility(View.GONE);
                }
            }
        }

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            mLandPlayBtn.setTag(playStatus.isPlaying());
            if (playStatus.isPlaying()) {
                mLandPlayIv.setImageDrawable(Lewa.resources().getDrawable(R.drawable.se_btn_pause));
            } else {
                mLandPlayIv.setImageDrawable(Lewa.resources().getDrawable(R.drawable.se_btn_play));
            }

            mPlayingSongAdapter.notifyDataSetChanged();
            PlaylistHelper.scrollToPlayingSong(mSongLv, mPlayingSongAdapter);
        }

        //refreshPlayHistories();
    }

    /*private void refreshPlayHistories() {
        Log.i(TAG, "Refresh play histories.");
        if (mPlayHistories != null) {
            for (SongCollection history : mPlayHistories) {
                if (!history.equals(Lewa.getPlayingCollection())) {
                    history.clear();
                }
            }
        }
        if (mPlayHistoryVp != null) {
            try {
                mPlayHistories = DBService.getPlayHistories();
            } catch (Exception e) {
                e.printStackTrace();
            }

            SongCollection playingSongCollection = Lewa.getPlayingCollection();
            
            mPlayHistoryAdapter.setData(mPlayHistories);
            int index = mPlayHistories.indexOf(playingSongCollection);
            if (index < 0) {
                index = 0;
            }

            if (mPlayHistories == null || mPlayHistories.size() == 0) return;
            mPlayHistoryAdapter.setCurrentPage(index);
            mPlayHistoryVp.setCurrentItem(index);

            SongCollection playHistory = mPlayHistories.get(index);
            Log.i("Play history: ", "Name: " + playHistory.getName());
            //mPlaylistName.setText(playHistory.getName());

            for (int i = 0; i < mPlayHistories.size(); i++) {
                SongCollection songCollection = mPlayHistories.get(i);
                Log.i(TAG, "History item:" + songCollection.toString());
                View vpItem = mPlayHistoryVp.findViewWithTag(i);

                if (vpItem != null) {
//                    Log.i(TAG, "vp item is not null.");
                    vpItem.setTag(R.id.tag_entity, songCollection);
                    MaskImageView iv = (MaskImageView) vpItem.findViewById(R.id.iv);
                    String coverUrl = songCollection.getCoverUrl();
                    if (!StringUtils.isBlank(coverUrl)) {
                        if (!coverUrl.startsWith("http")) {
                            Bitmap bitmap = Lewa.getLocalImage(coverUrl);
                            if (bitmap != null) {
                                iv.setImageBitmap(bitmap);
                            }
                        } else {
                            MyVolley.getImage(coverUrl, ImageLoader.getImageListener(iv, R.drawable.cover, R.drawable.cover));
                        }
                    } else {
                        iv.setImageResource(R.drawable.cover);
                    }

                    ImageButton playIv = (ImageButton) vpItem.findViewById(R.id.bt_playlist_play);
                    if (i == index) {
                        iv.setLayoutSize(Lewa.resources().getDimensionPixelSize(R.dimen.playlist_cover_large_size));
                        vpItem.findViewById(R.id.iv_highlight).setVisibility(View.VISIBLE);
                        vpItem.findViewById(R.id.llo2).setVisibility(View.VISIBLE);

                        if (Lewa.getPlayStatus().isPlaying()) {
                            playIv.setImageResource(R.drawable.se_btn_pause);
                            vpItem.setTag(R.id.tag_type, true);
                        } else {
                            playIv.setImageResource(R.drawable.se_btn_play);
                            vpItem.setTag(R.id.tag_type, false);
                        }
                    } else {
                        iv.setLayoutSize(Lewa.resources().getDimensionPixelSize(R.dimen.playlist_cover_small_size));
                        vpItem.findViewById(R.id.iv_highlight).setVisibility(View.GONE);
                        iv.setMaskColor(Lewa.resources().getColor(R.color.a40_black));
                        playIv.setImageResource(R.drawable.se_btn_play);
                        vpItem.setTag(R.id.tag_type, false);
                    }
                }
            }
        }
    }*/
}
