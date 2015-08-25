package com.lewa.player.fragment;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.media.audiofx.Visualizer;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.TextView;

import com.baidu.music.onlinedata.LyricManager.LyricDownloadListener;
import com.lewa.Lewa;
import com.lewa.player.IMediaPlaybackService;
import com.lewa.player.MediaPlaybackService;
import com.lewa.player.MusicUtils;
import com.lewa.player.R;
import com.lewa.player.activity.ArtistAlbumListActivity;
import com.lewa.player.listener.PlayControlListener;
import com.lewa.player.listener.PlayStatusListener;
import com.lewa.player.model.Artist;
import com.lewa.player.model.PlayStatus;
import com.lewa.player.model.Song;
import com.lewa.player.online.DownLoadLrc;
import com.lewa.player.online.OnlineLoader;
import com.lewa.util.Constants;
import com.lewa.util.LewaUtils;
import com.lewa.view.lyric.Lyric;
import com.lewa.view.lyric.LyricAdapter;
import com.lewa.view.lyric.LyricView;
import com.lewa.view.lyric.PlayListItem;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by wuzixiu on 11/27/13.
 */
public class PortLyricFragment extends PlayBaseFragment implements View.OnClickListener, View.OnTouchListener, AbsListView.OnScrollListener, AdapterView.OnItemClickListener, MusicUtils.Defs {

    private static final String TAG = "PortLyricFragment";

    LyricView mLyricView;
    LyricAdapter mLyricAdapter;
    TextView nolrc_tv;
    //ImageButton mArtistBtn;
    ImageButton mCoverBtn;
    ImageButton mMoreBtn;
    ViewGroup mMoreLo;
    private PlayControlListener mPlayControlListener = null;
    private long currentSongId;
    private String curTrackName;
    private String curArtistName;
    //    private SeekBar playingSeek = null;
    private long mPosOverride = -1;
    private PlayListItem currentLrc;
    private Lyric mLyric;
    private boolean hasLrc = false;
    private long mDuration;
    private boolean paused;
    private long downProcess;
    private int bufferPercent = 0;
    //    private TextView mTotalTime;
//    private TextView mCurrentTime;
    private int ScreenDisertyDpi = 0;
    private Visualizer visualizer;
    private int UPDATE_SPECTRUM = 1;
    private float alpha = 1.0f;
    private int vertical_padding;
    private long REFRESH_MILLIONS = 500;

    private int mLyricSrollState = SCROLL_STATE_IDLE;
    ResumeScrollTask resumeScrollTask;
    final private Timer resumeTimer = new Timer("resumeTimer");
    ///fix bug:48772 start
    private boolean mIsRegReceiver = false;
    ///fix bug:48772 end

    public PortLyricFragment() {
        
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPlayStatusListener = new MyPlayStatusListener(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = getLayoutInflater(savedInstanceState).inflate(R.layout.fragment_lyric_port, null);
        
        super.initSettingBtn(rootView, this);
        
        mLyricView = (LyricView) rootView.findViewById(R.id.lv_lyric);
        nolrc_tv = (TextView) rootView.findViewById(R.id.tv_nolrc);
        mArtistBtn = (ImageButton) rootView.findViewById(R.id.bt_artist);
        mMoreBtn = (ImageButton) rootView.findViewById(R.id.bt_more);
        mMoreLo = (ViewGroup) rootView.findViewById(R.id.llo2);
        
        mLyricAdapter = new LyricAdapter(this.getActivity());
        mLyricView.setAdapter(mLyricAdapter);
        mLyricView.setOnScrollListener(this);
        mLyricView.setOnItemClickListener(this);        
        mArtistBtn.setOnClickListener(this);
        mMoreBtn.setOnClickListener(this);
        
        postUpdate();

        if (Lewa.getIntSetting(Constants.SETTINGS_KEY_DOWNLOAD_LRC, Constants.SETTINGS_DOWNLOAD_LRC_DEFAULT) == Constants.SETTINGS_DOWNLOAD_LRC_OFF) {
            nolrc_tv.setVisibility(View.VISIBLE);
            nolrc_tv.setText(Lewa.string(R.string.download_lrc_off_hint));
        }

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mPlayControlListener = (PlayControlListener) activity;
        } catch (ClassCastException cce) {           
            cce.printStackTrace();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        paused = false;
        queueNextRefresh(REFRESH_MILLIONS);
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction(MediaPlaybackService.META_CHANGED);
        filter.addAction(OnlineLoader.UPDATELRC);
        filter.addAction("lyricchanged");
        //filter.addAction("com.lewa.player.onlinetolocal"); //not use
        filter.addAction("com.lewa.player.refreshspectrum");
        filter.addAction(MediaPlaybackService.NEXT_ACTION);
        getActivity().registerReceiver(receiver, filter);
        ///fix bug:48772 start
        mIsRegReceiver = true;
        ///fix bug:48772 end
        refreshPlayStatus(Lewa.getPlayStatus());
    }

    @Override
    public void onPause() {
        // TODO Auto-generated method stub
        super.onPause();     
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.i(TAG, "LyricFragment stop.");
        paused = true;
        ///fix bug:48772 start
        if (mIsRegReceiver) {
            getActivity().unregisterReceiver(receiver);
            mIsRegReceiver = false;
        }
        ///fix bug:48772 end
        mHandler.removeMessages(REFRESH);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mLyricAdapter!=null)
        	mLyricAdapter.release();

        mLyricView = null;
        Log.i(TAG, "LyricFragment destroy.");
    }

    protected void refreshSongInfo() {
        
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (resumeScrollTask != null) {
            resumeScrollTask.cancel();
        }

        if (scrollState == SCROLL_STATE_IDLE) {
            resumeScrollTask = new ResumeScrollTask();
            resumeTimer.schedule(resumeScrollTask, 2000);
        } else {
            mLyricSrollState = scrollState;
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//        Sentence sentence = (Sentence) view.getTag(R.id.tag_entity);

//        if (sentence != null) {
//            Lewa.playerServiceConnector().seek(sentence.getFromTime());
//        }
    }

    private class ResumeScrollTask extends TimerTask {

        /*
         * (non-Javadoc)
         *
         * @see java.util.TimerTask#run()
         */
        @Override
        public void run() {
            mLyricSrollState = SCROLL_STATE_IDLE;
        }
    }

    public static class MyPlayStatusListener implements PlayStatusListener {
        private WeakReference<PortLyricFragment> ref = null;

        public MyPlayStatusListener(PortLyricFragment fragment) {
            this.ref = new WeakReference<PortLyricFragment>(fragment);
        }

        @Override
        public String getId() {
            return PortPlayFragment.class.getName();
        }

        @Override
        public void onPlayStatusChanged(PlayStatus status) {
            PortLyricFragment fragment = ref.get();

            if (fragment != null) {
                fragment.refreshPlayStatus(status);
            }
        }

        @Override
        public void onBackgroundReady(Bitmap bitmap) {

        }

        @Override
        public void onBluredBackgroundReady(Bitmap bitmap) {

        }

        @Override
        public void onStartGetBackground() {

        }

        @Override
        public void onSongDownloaded(Long onlineId, Long localId) {
            PortLyricFragment fragment = ref.get();
            if (fragment != null) {
                
                fragment.refreshPlaySongState(fragment, localId);
            }
            
        }
    }

    @Override
    void refreshPlayStatus(PlayStatus status) {
        super.refreshPlayStatus(status);
        if (status == null) {
            LewaUtils.logE(TAG, "PlayStatus is null");
            return;
        }

        Song playingSong = status.getPlayingSong();

        if (playingSong == null || playingSong.getId() == null) {
            LewaUtils.logE(TAG, "playingSong or playingSong getId() is null");
            return;
        }

        if (playingSong.getArtist() == null) {
            LewaUtils.logE(TAG, "playingSong getArtist() is null");
            setLyric(playingSong.getName(), playingSong.getId(), null);
        } else {
            setLyric(playingSong.getName(), playingSong.getId(), playingSong.getArtist().getName());
        }

        if(status.isPlaying()) {
            paused = false;
            queueNextRefresh(REFRESH_MILLIONS);
        } else {
            paused = true;
        }

        
        refreshMoreBarState(playingSong);
    }

    //protected void refreshMoreBarState(Song playingSong) {
        
    //}

    private BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(OnlineLoader.UPDATELRC)) {
                int stat = intent.getIntExtra("downStat", -1);
                LewaUtils.logE(TAG, "update lrc state : " + stat);
                if (stat == LyricDownloadListener.STATUS_SUCCESS) {
                    nolrc_tv.setVisibility(View.GONE);
                    updateDownLRC(intent.getStringExtra("title"));
                } else {
                    setLyricNOlrc(1);
                }
            } else if (intent.getAction().equals("lyricchanged")) {
                LewaUtils.logE(TAG, "lyricchanged");
                if (mLyricView != null) {
                    mLyricView.setVisibility(View.GONE);
                }
                nolrc_tv.setVisibility(View.GONE);
            }
        }
    };

    @Override
    public void onClick(View v) {
        int vid = v.getId();
        Song playingSong = Lewa.getPlayingSong();
        //PlayStatus playStatus = Lewa.getPlayStatus(); //it`s not use
        switch (vid) {
            case R.id.bt_play:

                break;
            case R.id.bt_artist:
                if (playingSong != null) {
                    Artist artist = playingSong.getArtist();
                    if (artist != null) {
                        mMoreLo.setVisibility(View.GONE);
                        Intent intent = new Intent(getActivity(), ArtistAlbumListActivity.class);
                        artist.setOnline(playingSong.getType() == Song.TYPE.ONLINE);
                        intent.putExtra(ArtistAlbumListActivity.ARG_ARTIST, artist);
                        getActivity().startActivity(intent);
                    }
                }
                break;
            case R.id.bt_more:
                showOrHideMoreView(mMoreLo);
                break;
            case R.id.bt_add_to_list:
                addTo(playingSong, null);
                break;
            case R.id.bt_as_bell:
                setAsBell(playingSong);
                break;
            case R.id.bt_edit:
                editSong(playingSong);
                break;
            case R.id.bt_download:
                downloadSong(playingSong);
                break;
            case R.id.bt_share:
                shareSong(playingSong);
                break;
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int vid = v.getId();

        if (R.id.l_play == vid) {

        }
        return false;
    }

    public void postUpdate() {
        mHandler.post(mUpdateResults);
    }

    private void setLrcFile(String trackName, File lrcFile) {
        currentLrc = new PlayListItem(trackName, null, 0L, true);
        long totalTime = 0;
        try {
            IMediaPlaybackService service = Lewa.playerServiceConnector().service();
            if (service != null)
                totalTime = service.duration();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        mLyric = new Lyric(lrcFile, currentLrc, totalTime);
        setLyricView();

    }

    public void setLyric(String trackName, long songid, String ArtistName) {
        if (songid != 0 && trackName != null) {
            currentSongId = songid;
            curTrackName = trackName;
            curArtistName = ArtistName;
            File lrcfile = null;
            if (Environment.getExternalStorageState().equals(
                    Environment.MEDIA_MOUNTED)) {

                String sdCardDir = Environment.getExternalStorageDirectory()
                        + Constants.SRC_PATH;
                // modify by zhaolei,120327,for lrc save
                // modified by wangliqiang
                lrcfile = new File(sdCardDir + trackName + "-" + curArtistName
                        + ".lrc"); // String.valueOf(songid)
                // end
                LewaUtils.logE(TAG, "get lyric from sdcard : " + trackName + "-" + curArtistName + ".lrc");
                if (lrcfile.exists()) {
                    LewaUtils.logE(TAG, "get lyric from sdcard lrcfile exists");
                    setLrcFile(trackName, lrcfile);
                } else if (songid > 0 && (lrcfile = setLocalLrc(songid)) != null) {
                    LewaUtils.logE(TAG, "get lyric from sdcard lrcfile not exists");
                    setLrcFile(trackName, lrcfile);
                } else {
                    LewaUtils.logE(TAG, "get lyric from online");
                    OnlineLoader.getSongLrc(trackName, ArtistName);
                    setLyricNOlrc(0);
                    // lyv.setmLyric(mLyric);
                }
            }

        }

    }

    public void setLyricView() {
        if (mLyricView != null) {
            LewaUtils.logE(TAG, "set mLyricView visible");
            mLyricView.setVisibility(View.VISIBLE);
            mLyricAdapter.setLyric(mLyric);
            //mLyricView.setSelection(0);	//del by sjxu for bug 61353
            mLyricView.setBackgroundColor(Lewa.resources().getColor(android.R.color.transparent));
        }

        nolrc_tv.setVisibility(View.GONE);
        hasLrc = true;
    }

    private File setLocalLrc(long trackId) {
        String trackPath = MusicUtils.getSongPath(Lewa.context(), trackId);
        if (trackPath == null) {
            return null;
        }
        int index = trackPath.lastIndexOf(".");
        if (index == -1)
            return null;
        String LrcPath = trackPath.substring(0, index);
        LrcPath = LrcPath + ".lrc";
        File lrcfile = new File(LrcPath);
        if (lrcfile.exists()) {
            return lrcfile;
        } else {
            return null;
        }

    }

    private void setLyricNOlrc(int ifnolrc) {
        LewaUtils.logE(TAG, "setLyricNOlrc");
        mLyric = null;
        int resourceId = R.string.loadlrc;
        if (ifnolrc == 1) {
            resourceId = R.string.lrc_down_notfound;
        }
        if (Lewa.getIntSetting(Constants.SETTINGS_KEY_DOWNLOAD_LRC, Constants.SETTINGS_DOWNLOAD_LRC_DEFAULT) == Constants.SETTINGS_DOWNLOAD_LRC_ON) {
            if (!OnlineLoader.isNetworkAvailable()) {
                resourceId = R.string.no_network;
            }
        } else {
            resourceId = R.string.download_lrc_off_hint;
        }
        nolrc_tv.setVisibility(View.VISIBLE);
        if (getActivity() != null)
            nolrc_tv.setText(getActivity().getString(resourceId));
        if (resourceId == R.string.lrc_down_notfound) {
            Animation animation = AnimationUtils.loadAnimation(Lewa.context(), R.anim.gong);
            nolrc_tv.startAnimation(animation);
        }

        if (mLyricView != null) {
            mLyricView.setVisibility(View.GONE);
        }

//        noLrcView.setText(resourceId);
//        noLrcView.setGravity(Gravity.CENTER);
        // mActivity.setContentView(lyv, new
        // LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
    }

    public void updateDownLRC(String title) {
        if (curTrackName.equals(title)) {
            LewaUtils.logE(TAG, "Lrc downloaded.");
            refreshPlayStatus(Lewa.getPlayStatus());
        } else {
            LewaUtils.logE(TAG, "Lrc downloaded, but playing song has been shifted, last: " + title + ", current: " + curTrackName);
        }
    }

    public void updateDuration(long duration) {
        if (hasLrc && mLyricView != null) {
            mLyricAdapter.setTime(duration);
            int intentSelection = mLyricAdapter.getSelection();
            int currentSelection = mLyricView.getSelectedItemPosition();

            if (intentSelection != currentSelection && mLyricSrollState == SCROLL_STATE_IDLE) {
                mLyricView.smoothScrollToPositionFromTop(intentSelection, mLyricView.getHeight() / 2 - Lewa.resources().getDimensionPixelSize(R.dimen.lyric_line_height), 300);
            }
        }

    }

    public Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            IMediaPlaybackService service = Lewa.playerServiceConnector().service();

            switch (msg.what) {
                case REFRESH:
                    try {
                        if (service != null && mLyric != null) {
                            updateDuration(service.position());
                            postUpdate();
                        }
                    } catch (RemoteException e) {
                        // TODO Auto-generated catch block
                    }
                    queueNextRefresh(REFRESH_MILLIONS);
                    break;
            }
        }

    };

    private long refreshNow() {
        IMediaPlaybackService service = Lewa.playerServiceConnector().service();
        if (mDuration <= 0 || MusicUtils
                .makeTimeString(Lewa.context(), mDuration / 1000).equals("353:45:35")) {
            try {
                if (service != null)
                    mDuration = service.duration();
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        if (service == null)
            return 500;
        try {
            long pos = mPosOverride < 0 ? service.position() : mPosOverride;
            long remaining = 1000 - (pos % 1000);

            if ((pos >= 0) && (mDuration > 0)) {

                if (!service.isPlaying()) {
                    remaining = 500;
                }
                
                if (MediaPlaybackService.isOnlinePlay) {
                    if (downProcess <= service.getDownProcess())
                        downProcess = service.getDownProcess();
                    if (downProcess >= 1000 && !MusicUtils.isScan) {
                        MusicUtils.isProtected = false;
                        MusicUtils.isScan = true;
                        MusicUtils.isLiving = true;
                        Intent intent = new Intent(MediaPlaybackService.SCANMUSIC);
                        Activity activity = getActivity();
                        if (activity != null) {
                            activity.sendBroadcast(intent);
                            activity.sendBroadcast(new Intent(MediaPlaybackService.DOWN_FINISHED));
                        }
                        if (MusicUtils.isFirst) {                           
                            MusicUtils.isFirst = false;
                        }
                    } else if (downProcess < 1000) {
                        MusicUtils.isScan = false;
                        MusicUtils.scan_finished = false;
                        MusicUtils.isProtected = true;
                    }
                    if (bufferPercent >= 99)
                        bufferPercent = 0;
                }
            } else {
                if (MediaPlaybackService.isOnlinePlay) {
                    bufferPercent = (service.getBufferPercent() > bufferPercent) ? service.getBufferPercent() : bufferPercent;
                }
            }
            // return the number of milliseconds until the next full second, so
            // the counter can be updated at just the right time
            return 100;
        } catch (RemoteException ex) {
            ex.printStackTrace();
        }
        return 100;
    }

    

    private void queueNextRefresh(long delay) {
        if (!paused) {
            Message msg = mHandler.obtainMessage(REFRESH);
            mHandler.removeMessages(REFRESH);
            mHandler.sendMessageDelayed(msg, delay);
        }
    }

    Runnable mUpdateResults = new Runnable() {
        public void run() {
            if (mLyricView != null)
                mLyricView.invalidate();
        }
    };

}
