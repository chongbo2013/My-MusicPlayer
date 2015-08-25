package com.lewa.view;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lewa.player.IMediaPlaybackService;
import com.lewa.player.MediaPlaybackService;
import com.lewa.player.MusicUtils;
import com.lewa.player.R;
import com.lewa.player.activity.PlayActivity;
import android.app.Activity;
import com.lewa.kit.ActivityHelper;


public class NowPlayingController extends ViewGroup {

    //	public static String UPDATE_ACTIONBAR = "com.lewa.player.ui.UPDATE_ACTIONBAR";
//	public static final String PAUSECLICK = "com.lewa.player.pauseclick";
    private ImageView nowplayingImg;
    private TextView nowplayingTxt;
    private TextView nowplayingArtist;
    private Context mContext;
    private ViewGroup thisView;
    private IMediaPlaybackService mService;
    private LinearLayout mNowPlayingInfo;
    private TextView mNowPlayingShuffle;
    private ImageView nowplayingNext;
    private RelativeLayout mNowLinear;

    //finally public static String

    public NowPlayingController(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        // TODO Auto-generated constructor stub
        View nowplaying;
        nowplaying = LayoutInflater.from(context).inflate(R.layout.nowplayingbar, null);
        nowplaying.setId(1);
        this.addView(nowplaying);

        nowplayingImg = (ImageView) nowplaying.findViewById(R.id.nowplayingimage);
        nowplayingTxt = (TextView) nowplaying.findViewById(R.id.nowplayingSong);
        nowplayingArtist = (TextView) nowplaying.findViewById(R.id.nowplayingArtist);
        mNowPlayingInfo = (LinearLayout) nowplaying.findViewById(R.id.nowplayingInfo);
        mNowPlayingShuffle = (TextView) nowplaying.findViewById(R.id.nowplayingShuffle);
        nowplayingNext = (ImageView) nowplaying.findViewById(R.id.nowplayingnext);
        mNowLinear = (RelativeLayout) nowplaying.findViewById(R.id.now_linear);

        IntentFilter filterReceiver = new IntentFilter();
        filterReceiver.addAction(MediaPlaybackService.META_CHANGED);
        filterReceiver.addAction(MediaPlaybackService.PLAYSTATE_CHANGED);
        mContext.getApplicationContext().registerReceiver(Receiver, filterReceiver);

        IntentFilter f = new IntentFilter();
        f.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
        f.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        f.addDataScheme("file");
        mContext.getApplicationContext().registerReceiver(mScanListener, f);

        thisView = this;

        nowplayingImg.setOnClickListener(touchlisen);
        nowplayingNext.setOnClickListener(touchlisen);
        mNowPlayingInfo.setOnClickListener(touchlisen);
        this.setBackgroundColor(0x60000000);

        this.getContext().getClass().toString().equals("MusicMainEntryActitiy");
    }

    private BroadcastReceiver mScanListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Intent.ACTION_MEDIA_UNMOUNTED.equals(action)) {

                mNowLinear.setVisibility(View.GONE);

            } else if (Intent.ACTION_MEDIA_SCANNER_FINISHED.equals(action)) {
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        updateNowPlayingBar();
                    }
                }, 3000);
            }
        }
    };

    public void setMediaService(IMediaPlaybackService service) {
        mService = service;
        if (mService != null) {
            updateNowPlayingBar();
        }
    }

    public void registReceiver() {
        IntentFilter filterReceiver = new IntentFilter();
        filterReceiver.addAction(MediaPlaybackService.META_CHANGED);
        filterReceiver.addAction(MediaPlaybackService.PLAYSTATE_CHANGED);
        mContext.getApplicationContext().registerReceiver(Receiver, filterReceiver);

        IntentFilter f = new IntentFilter();
        f.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
        f.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        f.addDataScheme("file");
        mContext.getApplicationContext().registerReceiver(mScanListener, f);
    }

    public void updateNowPlayingBar() {
        if (mService != null) {
            try {
                String trackName = mService.getTrackName();
                String artistName = mService.getArtistName();
                String albumName = mService.getAlbumName();

                if (MediaStore.UNKNOWN_STRING.equals(artistName)) {
                    artistName = getResources().getString(R.string.unknown_artist_name);
                }

                if (MediaStore.UNKNOWN_STRING.equals(albumName) || albumName == null || albumName.equals("null")) {
                    albumName = getResources().getString(R.string.unknown_album_name);
                }

                if (trackName == null || artistName == null) {

                    setNoSongPlayingView();

                } else {
                    nowplayingTxt.setText(trackName);
                    nowplayingArtist.setText(artistName + " - " + albumName);

                    setNowPlayingViewVisible();
                    setPauseButtonImage();
                }
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                nowplayingTxt.setText("");
                nowplayingArtist.setText("");
            }
        }
    }

    protected BroadcastReceiver Receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(MediaPlaybackService.META_CHANGED)) {
                if (mService != null) {
                    updateNowPlayingBar();
                }
            } else if (intent.getAction().equals(MediaPlaybackService.PLAYSTATE_CHANGED)) {
                setPauseButtonImage();

            }
        }

    };

    public void destroyNowplaying() {
        mContext.getApplicationContext().unregisterReceiver(Receiver);
        mContext.getApplicationContext().unregisterReceiver(mScanListener);
        this.removeAllViews();

    }

    public void unregistReceiver() {
        mContext.getApplicationContext().unregisterReceiver(Receiver);
        mContext.getApplicationContext().unregisterReceiver(mScanListener);
    }

    public void removeViews() {
        this.removeAllViews();
    }

	private Activity activity = null;
	public void setActivity(Activity activity) {
		this.activity = activity;
	}

    OnClickListener touchlisen = new OnClickListener() {

        public void onClick(View v) {
            // TODO Auto-generated method stub

            if (v.getId() == R.id.nowplayingInfo) {
		   if(null == activity ) {
	                Intent sPlayIntent = new Intent();
	                sPlayIntent.setClass(mContext, PlayActivity.class);
	                sPlayIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	                mContext.startActivity(sPlayIntent);
		   } else {
			ActivityHelper.goPlayWithClearTopAnim(activity, false);
		   }
            } else if (v.getId() == R.id.nowplayingimage) {
                doServicePlayResume();
            } else if (v.getId() == R.id.nowplayingShuffle) {
                if (MusicUtils.mHasSongs) {
                    thisView.setBackgroundColor(0x80CC9933);
			 if(null == activity ) {
	                    Intent sPlayIntent = new Intent();
	                    sPlayIntent.putExtra("isRandomAll", true);
	                    sPlayIntent.setClass(mContext, PlayActivity.class);
	                    sPlayIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	                    mContext.startActivity(sPlayIntent);
			 } else {
				ActivityHelper.goPlayWithClearTopAnim(activity, true);
			 }
                }
            } else if (v.getId() == R.id.nowplayingnext) {
                try {
                    if (mService != null) {
                        mService.next();
                    }
                } catch (RemoteException ex) {
                }
            }
        }

    };

    public void doServicePlayResume() {
        try {
            if (mService != null) {
                if (mService.isPlaying()) {
                    mService.pause();
                } else {
                    mService.play();
                }
            }
        } catch (RemoteException ex) {
        }
    }

    private void setPauseButtonImage() {
        try {
            if (nowplayingImg == null) {
                return;
            }
            if (mService != null && mService.isPlaying()) {
                nowplayingImg.setImageResource(R.drawable.top_pause_selector);
            } else {
                nowplayingImg.setImageResource(R.drawable.top_play_selector);
            }
        } catch (RemoteException ex) {
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        // TODO Auto-generated method stub
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            switch (child.getId()) {
                case 1:
                    child.setVisibility(View.VISIBLE);
                    child.measure(r - l, b - t);
                    child.layout(0, 0, r - l, b - t);
                    break;
                default:
                    //
            }
        }
    }

    public void setNoSongPlayingView() {
        thisView.setBackgroundColor(0x60000000);
        mNowLinear.setVisibility(View.GONE);
        mNowPlayingShuffle.setVisibility(View.VISIBLE);
        mNowPlayingShuffle.setGravity(Gravity.CENTER);
        if (MusicUtils.mHasSongs == false) {
            mNowPlayingShuffle.setText(R.string.phone_no_songs);
        } else {
            mNowPlayingShuffle.setText(R.string.click_to_shuffle);
            mNowPlayingShuffle.setClickable(true);
            mNowPlayingShuffle.setOnClickListener(touchlisen);
        }
    }

    private void setNowPlayingViewVisible() {
        thisView.setBackgroundColor(0x60000000);

        mNowLinear.setVisibility(View.VISIBLE);

        mNowPlayingShuffle.setVisibility(View.GONE);
        mNowPlayingShuffle.setClickable(false);
    }
}
