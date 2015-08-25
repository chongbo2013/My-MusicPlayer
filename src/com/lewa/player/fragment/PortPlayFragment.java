package com.lewa.player.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.lewa.ExitApplication;
import com.lewa.Lewa;
import com.lewa.kit.ActivityHelper;
import com.lewa.player.IMediaPlaybackService;
import com.lewa.player.MediaPlaybackService;
import com.lewa.player.R;
import com.lewa.player.activity.ArtistAlbumListActivity;
import com.lewa.player.db.DBService;
import com.lewa.player.listener.CallbackFavoriteListener;
import com.lewa.player.listener.PlayControlListener;
import com.lewa.player.listener.PlayStatusListener;
import com.lewa.player.model.Artist;
import com.lewa.player.model.PlayStatus;
import com.lewa.player.model.Playlist;
import com.lewa.player.model.PlaylistSong;
import com.lewa.player.model.Song;
import com.lewa.util.DateUtils;
import com.lewa.util.LewaUtils;

import java.lang.ref.WeakReference;
import java.sql.SQLException;
import java.util.Date;
import lewa.hotknot.HotKnotHelper;
import android.net.Uri;

/**
 * Created by wuzixiu on 11/27/13.
 */
public class PortPlayFragment extends PlayBaseFragment implements View.OnClickListener, HotKnotHelper.HotknotListener { // modify by jjlin for HotKnot  //, View.OnTouchListener {

    private static final String TAG = "PortPlayFragment"; //.class.getName();


    ImageButton mPrevBtn;
    ImageButton mPlayBtn;
    ImageButton mNextBtn;
    ImageButton mPlayModeBtn;
    ImageButton mHotknotButton;
    TextView mSongNameTv;
    TextView mPlaylistNameTv;
    SeekBar mSeekBar;
    TextView mPositionTv;
    TextView mDurationTv;
    //FrameLayout mLoPlay;
    //ImageButton mArtistBtn;
    ImageButton mCoverBtn;
    ViewGroup mMoreLo;
    ImageButton mMoreBtn;
    ImageButton mFavoriteBtn;
    //private GestureDetector mGestureDetector = null;
    //private ScaleGestureDetector mScaleGestureDetector = null;

    // modify by jjlin for HotKnot Begin
    public HotKnotHelper mHotKnotHelper = null;
    private Uri[] mHotknotUris ; 
    private static final int MSG_HOTKNOT_COMPLETE = 2;
    private static final int MSG_HOTKNOT_MODECHANGED = 3;
    private Activity mActivity = null;
    // modify by jjlin for HotKnot End

    private PlayControlListener mPlayControlListener = null;

    private Playlist mFavoritePlaylist;
    private boolean paused;
    private long mStartSeekPos = 0;
    private long mDuration;
    private long mLastSeekEventTime;
    private boolean mFromTouch = false;
    private long mPosOverride = -1;
    private static final int REFRESH = 1;
    private static int mRepeatMode = MediaPlaybackService.REPEAT_ALL;
    private static int mShuffleMode = MediaPlaybackService.SHUFFLE_NONE;

    public PortPlayFragment() {
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPlayStatusListener = new MyPlayStatusListener(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_play_port, container, false);
        mPrevBtn = (ImageButton) rootView.findViewById(R.id.bt_previous);
        mPrevBtn.setOnClickListener(this);
        mPlayBtn = (ImageButton) rootView.findViewById(R.id.bt_play);
        mPlayBtn.setOnClickListener(this);
        mNextBtn = (ImageButton) rootView.findViewById(R.id.bt_next);
        mNextBtn.setOnClickListener(this);
        mPlayModeBtn = (ImageButton) rootView.findViewById(R.id.bt_play_mode);
        mPlayModeBtn.setOnClickListener(this);
        mSongNameTv = (TextView) rootView.findViewById(R.id.tv_song_name);
        mSeekBar = (SeekBar) rootView.findViewById(R.id.seekbar);
        mPlaylistNameTv = (TextView) rootView.findViewById(R.id.tv_playlist_name);
        mPositionTv = (TextView) rootView.findViewById(R.id.tv_play_position);
        mDurationTv = (TextView) rootView.findViewById(R.id.tv_song_duration);
        //mLoPlay = (FrameLayout) rootView.findViewById(R.id.l_play);
        mArtistBtn = (ImageButton) rootView.findViewById(R.id.bt_artist);
        //mCoverBtn = (ImageButton) rootView.findViewById(R.id.bt_cover);
        mMoreLo = (ViewGroup) rootView.findViewById(R.id.llo2);
        mMoreBtn = (ImageButton) rootView.findViewById(R.id.bt_more);
        mHotknotButton = (ImageButton) rootView.findViewById(R.id.bt_hotknot);
        mFavoriteBtn = (ImageButton) rootView.findViewById(R.id.bt_favorite);
        super.initSettingBtn(rootView, this);

        //mLoPlay.setOnTouchListener(this);
        mArtistBtn.setOnClickListener(this);
        mMoreBtn.setOnClickListener(this);
        //mCoverBtn.setOnClickListener(this);
        mFavoriteBtn.setOnClickListener(this);

        View.OnClickListener clickListener = (View.OnClickListener) getActivity();

        mSeekBar.setOnSeekBarChangeListener(mSeekListener);
        mSeekBar.setMax(1000);

        //mScaleGestureDetector = new ScaleGestureDetector(getActivity(), new ZoomOutGestureDetector());

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        mActivity = activity;
        // modify by jjlin for HotKnot Begin
        mHotKnotHelper = new HotKnotHelper(mActivity);
        if (null != mHotKnotHelper) {
            mHotKnotHelper.initialize();
        }
        // modify by jjlin for HotKnot End

        try {
            mPlayControlListener = (PlayControlListener) activity;
        } catch (ClassCastException cce) {
            Log.e(TAG, "Activity should implement LibraryListener.");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        paused = false;
        long next = refreshNow();
        queueNextRefresh(next);
    }

    @Override
    public void onResume() {
        super.onResume();
        mMoreLo.setVisibility(View.GONE);
        mFavoritePlaylist = DBService.findSinglePlaylist(Playlist.TYPE.FAVORITE);
        refreshPlayStatus(Lewa.getPlayStatus());

    }

    @Override
    public void onStop() {
        paused = true;
        mHandler.removeMessages(REFRESH);
        // modify by jjlin for HotKnot Begin
        mHandler.removeMessages(MSG_HOTKNOT_MODECHANGED);
        mHandler.removeMessages(MSG_HOTKNOT_COMPLETE);
        // modify by jjlin for HotKnot End
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // modify by jjlin for HotKnot Begin
        if (null != mHotKnotHelper) {
            mHotKnotHelper.finalize();
        }
        mActivity = null;
        // modify by jjlin for HotKnot End
    }

    @Override
    public void onClick(View v) {
        int vid = v.getId();
        Song playingSong = Lewa.getPlayingSong();
        PlayStatus playStatus = Lewa.getPlayStatus();

        switch (vid) {

            case R.id.bt_previous:
                mPlayControlListener.previous();
                break;
            case R.id.bt_next:
                mPlayControlListener.next();
                mPlayControlListener.resetBg(1);
                break;
            case R.id.bt_play:
                boolean isPlaying = playStatus == null ? false : playStatus.isPlaying();

                if (isPlaying) {
                    mPlayControlListener.pause();
                } else {
                    mPlayControlListener.play();
                }

                break;
            case R.id.bt_play_mode:
                Log.i(TAG, "Repeat mode: " + mRepeatMode + "\tshuffle mode: " + mShuffleMode);
                if (mShuffleMode == MediaPlaybackService.SHUFFLE_NORMAL || mShuffleMode == MediaPlaybackService.SHUFFLE_AUTO) {
                    Lewa.playerServiceConnector().setRepeatAndShuffleMode(MediaPlaybackService.REPEAT_CURRENT, MediaPlaybackService.SHUFFLE_NONE);
                    mPlayModeBtn.setImageResource(R.drawable.se_btn_loop_single);
                    mRepeatMode = MediaPlaybackService.REPEAT_CURRENT;
                    mShuffleMode = MediaPlaybackService.SHUFFLE_NONE;
                    Toast.makeText(getActivity(), R.string.repeat_single, Toast.LENGTH_SHORT).show();
                } else {
                    if (mRepeatMode == MediaPlaybackService.REPEAT_ALL || mRepeatMode == MediaPlaybackService.REPEAT_NONE) {
                        Lewa.playerServiceConnector().setRepeatAndShuffleMode(MediaPlaybackService.REPEAT_ALL, MediaPlaybackService.SHUFFLE_NORMAL);
                        mPlayModeBtn.setImageResource(R.drawable.se_btn_shuffle);
                        mRepeatMode = MediaPlaybackService.REPEAT_NONE;
                        mShuffleMode = MediaPlaybackService.SHUFFLE_NORMAL;
                        Toast.makeText(getActivity(), R.string.shuffle, Toast.LENGTH_SHORT).show();
                    } else if (mRepeatMode == MediaPlaybackService.REPEAT_CURRENT) {
                        Lewa.playerServiceConnector().setRepeatAndShuffleMode(MediaPlaybackService.REPEAT_ALL, MediaPlaybackService.SHUFFLE_NONE);
                        mPlayModeBtn.setImageResource(R.drawable.se_btn_loop);
                        mRepeatMode = MediaPlaybackService.REPEAT_ALL;
                        mShuffleMode = MediaPlaybackService.SHUFFLE_NONE;
                        Toast.makeText(getActivity(), R.string.repeat_all, Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            case R.id.bt_library_in_playlist:
                mMoreLo.setVisibility(View.GONE);
                ActivityHelper.goLibrary(getActivity());

                Log.i("Play", "Go to library");
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
            case R.id.bt_favorite:
                mHandler.removeCallbacks(mDelayHideTask);
                if (mFavoritePlaylist != null && Lewa.getPlayStatus() != null && Lewa.getPlayStatus().getPlayingSong() != null) {
                    try {
                        if (DBService.isFavorited(Lewa.getPlayStatus().getPlayingSong())) {
                            mFavoritePlaylist.setSongNum(mFavoritePlaylist.getSongNum() - 1);
                            mFavoriteBtn.setImageResource(R.drawable.se_btn_favorite);
                            DBService.removePlaylistSong(mFavoritePlaylist.getId(), Lewa.getPlayStatus().getPlayingSong());
                            DBService.updatePlaylistSongNumber(mFavoritePlaylist.getId());
                            /*Added by ruiwei, for Modifying Toast style, 20150211, start*/
                            Context activity = ExitApplication.getTopActivity();
                            if(null == activity) {
                            	activity = getActivity();
                            }
                            Toast.makeText(activity, getActivity().getString(R.string.favorite_remove_successfully_toast_text), Toast.LENGTH_SHORT).show();
                            /*Added by ruiwei, for Modifying Toast style, 20150211, end*/
                        } else {
                            mFavoritePlaylist.setSongNum(mFavoritePlaylist.getSongNum() + 1);

                            PlaylistSong playlistSong = new PlaylistSong();
                            playlistSong.setSong(playingSong);
                            playlistSong.setPlaylist(mFavoritePlaylist);
                            playlistSong.setCreateTime(new Date());

                            DBService.savePlaylistSong(playlistSong, mFavoritePlaylist.getId(), true);
                            mFavoriteBtn.setImageResource(R.drawable.se_btn_favorited);
                            Toast.makeText(getActivity(), getActivity().getString(R.string.favorite_successfully_toast_text), Toast.LENGTH_SHORT).show();
                        }
                        DBService.updatePlaylistWithoutSongs(mFavoritePlaylist);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
                startCloseTask(mMoreLo);
                break;
            case R.id.bt_add_to_list:
                addTo(playingSong, new CallbackFavoriteListener() {
                    @Override
                    public void execute() {
                        mFavoriteBtn.setImageResource(R.drawable.btn_favorited);
                    }
                });
                break;
            case R.id.bt_as_bell:
                setAsBell(playingSong);
                break;
            case R.id.bt_edit:
                editSong(playingSong);
                break;
            case R.id.bt_download:
                Log.i(TAG, "download button clickable false ");
                //mDownloadBtn.setClickable(false);
                downloadSong(playingSong);
                break;
            case R.id.bt_share:
                shareSong(playingSong);
                break;
            case R.id.bt_hotknot:
                // modify by jjlin for HotKnot Begin
                if (null != mHotKnotHelper) {
                    if (!mHotKnotHelper.isSending()) {
                        mHotKnotHelper.setHotknotListener(this);
                        mHotKnotHelper.startSend(mHotknotUris, mActivity);
                    } else {
                        mHotKnotHelper.stopSend();
                        mHotKnotHelper.setHotknotListener(null);
                    }
                }
                // modify by jjlin for HotKnot End
                break;
        }
    }


    private SeekBar.OnSeekBarChangeListener mSeekListener = new SeekBar.OnSeekBarChangeListener() {
        private long newProgress = 0;
        
        public void onStartTrackingTouch(SeekBar bar) {
            mLastSeekEventTime = 0;
            mFromTouch = true;
            newProgress = 0;
        }

        public void onProgressChanged(SeekBar bar, int progress, boolean fromuser) {
            PlayStatus playStatus = Lewa.getPlayStatus();
            if (!fromuser || playStatus == null || playStatus.getPlayingSong() == null) return;
                newProgress = progress;
                Song playingSong = playStatus.getPlayingSong();

                long now = SystemClock.elapsedRealtime();
                if ((now - mLastSeekEventTime) > 250) {
                    mLastSeekEventTime = now;
                    mPosOverride = (playingSong.getDuration() == null ? 0 : playingSong.getDuration()) * progress / 1000;
                    Lewa.playerServiceConnector().seek(mPosOverride);
                    // trackball event, allow progress updates
                    if (!mFromTouch) {
                        refreshNow();
                        mPosOverride = -1;
                    }
            }

        }

        public void onStopTrackingTouch(SeekBar bar) {
            PlayStatus playStatus = Lewa.getPlayStatus();
            Song playingSong = playStatus.getPlayingSong();
            long pos = (playingSong == null ? 0 : playingSong.getDuration()) * newProgress / 1000;
            Lewa.playerServiceConnector().seek(pos);

            newProgress = 0; 	
            mPosOverride = -1;
            mFromTouch = false;
            
            //for lockscreen start
            Intent i = new Intent("com.lewa.player.refreshprogress");
            i.putExtra("duration", (playingSong == null ? 0 : playingSong.getDuration()));
            i.putExtra("position", pos);
            i.putExtra("time_stamp", System.currentTimeMillis());
            i.putExtra(MediaPlaybackService.EXTRA_IS_PLAYING, playStatus.isPlaying());           
            PortPlayFragment.this.getActivity().sendBroadcast(i);
            //for lockscreen end
            
            refreshNow();
        }
    };

    public static class MyPlayStatusListener implements PlayStatusListener {
        private WeakReference<PortPlayFragment> ref = null;

        public MyPlayStatusListener(PortPlayFragment fragment) {
            this.ref = new WeakReference<PortPlayFragment>(fragment);
        }

        @Override
        public String getId() {
            return PortPlayFragment.class.getName();
        }

        @Override
        public void onPlayStatusChanged(PlayStatus status) {
            PortPlayFragment fragment = ref.get();

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
            PortPlayFragment fragment = ref.get();
            if (fragment != null) {
                
                fragment.refreshPlaySongState(fragment, localId);
                
            }
        }
    }

    protected void refreshSongInfo() {
        refreshPlayStatus(Lewa.getPlayStatus());
    }

    @Override
    void refreshPlayStatus(PlayStatus playStatus) {
        if(null == playStatus) {
            return;
        }

        if(null == mSongNameTv) {
            return;
        }
        super.refreshPlayStatus(playStatus);
        Song playingSong = playStatus.getPlayingSong();

        if (playingSong != null) {
            // add by jjlin for HotKnot Begin
            if (null != mHotknotButton) {
                if (playingSong.getType() == Song.TYPE.LOCAL) {
                    mHotknotButton.setVisibility(View.VISIBLE);
                    mHotknotButton.setOnClickListener(this);
                    mHotknotUris = new Uri[] {
                        Uri.parse("file://" + playingSong.getPath())
                    };
                }
                else {
                    mHotknotButton.setVisibility(View.GONE);
                    mHotknotButton.setOnClickListener(null);
                }
            }
            // add by jjlin for HotKnot End
            if (playingSong.getName() != null) {
                mSongNameTv.setText(playingSong.getName());
            } else {
                mSongNameTv.setText("");
            }

            if (playingSong.getArtist() != null && playingSong.getArtist().getName() != null) {
            	//pr953057 modify by wjhu begin
            	//to replace the <unknown>
                String artistName = playingSong.getArtist().getName();
    			if (MediaStore.UNKNOWN_STRING.equals(artistName)) {
    				mPlaylistNameTv.setText(getString(
    						R.string.unknown_artist_name));
    			} else {
    				mPlaylistNameTv.setText(artistName);
    			}
    			//pr953057 modify by wjhu end
            } else {
                mPlaylistNameTv.setText("");
            }

            long position = Lewa.playerServiceConnector().position();
            long duration = Lewa.playerServiceConnector().duration();
            if(position > duration && 0 != duration) {
                position = duration;
            }
            
            if (position > 0) {
                mPositionTv.setText(DateUtils.m2s2(new Date(position)));
            } else {
                mPositionTv.setText("--:--");
            }

            if (duration > 0) {
                mDurationTv.setText(DateUtils.m2s2(new Date(duration)));
            } else {
                mDurationTv.setText("--:--");
            }
            try {
                if (!DBService.isFavorited(playingSong)) {
                    mFavoriteBtn.setImageResource(R.drawable.se_btn_favorite);
                } else {
                    mFavoriteBtn.setImageResource(R.drawable.btn_favorited);
                }
            } catch (SQLException e) {

            }
            long next = refreshNow();
            queueNextRefresh(next);
            //setBtnShow(playingSong);
            refreshMoreBarState(playingSong);
        } else {        
            mHandler.removeMessages(REFRESH);
            // add by jjlin for HotKnot Begin
            mHotknotUris = null;
            if (null != mHotknotButton) {
                mHotknotButton.setVisibility(View.GONE);
                mHotknotButton.setOnClickListener(null);
            }
            // add by jjlin for HotKnot End
            mFavoriteBtn.setImageResource(R.drawable.se_btn_favorite);
            mPositionTv.setVisibility(View.VISIBLE);
            mPositionTv.setText("--:--");
            mDurationTv.setText("--:--");
            mSongNameTv.setText("");
            mPlaylistNameTv.setText("");
            mSeekBar.setProgress(0);
            
        }

        if (playStatus.isPlaying() && null != playingSong) {
            mPlayBtn.setImageDrawable(Lewa.resources().getDrawable(R.drawable.se_btn_pause));
        } else {
            mPlayBtn.setImageDrawable(Lewa.resources().getDrawable(R.drawable.se_btn_play));
        }

        mRepeatMode = playStatus.getRepeatMode();
        mShuffleMode = playStatus.getShuffleMode();

        if (mShuffleMode == MediaPlaybackService.SHUFFLE_NORMAL) {
            mPlayModeBtn.setImageResource(R.drawable.se_btn_shuffle);
        } else {
            if (mRepeatMode == MediaPlaybackService.REPEAT_ALL || mRepeatMode == MediaPlaybackService.REPEAT_NONE) {
                mPlayModeBtn.setImageResource(R.drawable.se_btn_loop);
            } else if (mRepeatMode == MediaPlaybackService.REPEAT_CURRENT) {
                mPlayModeBtn.setImageResource(R.drawable.se_btn_loop_single);
            }
        }
    }

    /*protected void refreshMoreBarState(Song playingSong) {
        if (playingSong != null) {
            if (playingSong.getType() == Song.TYPE.LOCAL) {
                mEditBtn.setVisibility(View.VISIBLE);
                mSetAsBellBtn.setVisibility(View.VISIBLE);
                mDownloadBtn.setVisibility(View.GONE);
                mShareBtn.setVisibility(View.GONE);
            } else {
                mEditBtn.setVisibility(View.GONE);
                mSetAsBellBtn.setVisibility(View.GONE);
                mDownloadBtn.setVisibility(View.VISIBLE);
                mDownloadBtn.setClickable(true);
                mShareBtn.setVisibility(View.VISIBLE);
            }
        }
    }*/

    private void queueNextRefresh(long delay) {
        if (!paused) {
            Message msg = mHandler.obtainMessage(REFRESH);
            mHandler.removeMessages(REFRESH);
            mHandler.sendMessageDelayed(msg, delay);
        }
    }

    private long refreshNow() {
        IMediaPlaybackService service = Lewa.playerServiceConnector().service();
        if (service == null)
            return 500;
        try {
            mDuration = service.duration();

            long pos = mPosOverride < 0 ? service.position() : mPosOverride;
            if(pos > mDuration && 0 != mDuration) {
                pos = mDuration;
            }
            
            if ((pos > 0) && (mDuration > 0)) {
                mPositionTv.setText(DateUtils.m2s2(new Date(pos)));
                mDurationTv.setText(DateUtils.m2s2(new Date(mDuration)));
				//add for bug 65241 by sjxu start
                Song playingSong = Lewa.getPlayingSong();
                if(null != playingSong && 0 == playingSong.getDuration()) {
                    playingSong.setDuration(mDuration);
                }
				//add for bug 65241 by sjxu end
                int progress = (int) (1000 * pos / mDuration);
                mSeekBar.setProgress(progress);

                if (service.isPlaying()) {
                    mPositionTv.setVisibility(View.VISIBLE);
                } else {
                    // blink the counter
                    int vis = mPositionTv.getVisibility();
                    mPositionTv.setVisibility(vis == View.INVISIBLE ? View.VISIBLE : View.INVISIBLE);
                    return 500;
                }
            } else {
                mPositionTv.setText("--:--");
                mDurationTv.setText("--:--");
                mSeekBar.setProgress(0);
            }
            // calculate the number of milliseconds until the next full second, so
            // the counter can be updated at just the right time
            long remaining = 1000 - (pos % 1000);

            // approximate how often we would need to refresh the slider to
            // move it smoothly
            int width = mSeekBar.getWidth();
            if (width == 0) width = 320;
            long smoothrefreshtime = mDuration / width;

            if (smoothrefreshtime > remaining) return remaining;
            if (smoothrefreshtime < 20) return 20;
            return smoothrefreshtime;
        } catch (RemoteException ex) {
        }
        return 500;
    }

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case REFRESH:
                    long next = refreshNow();
                    queueNextRefresh(next);
                    break;
                // modify by jjlin for HotKnot Begin
                case MSG_HOTKNOT_COMPLETE:
                case MSG_HOTKNOT_MODECHANGED:
                    if (null != mHotknotButton) {
                        if (null != mHotKnotHelper && mHotKnotHelper.isSending()) {
                            mHotknotButton.setImageResource(R.drawable.se_btn_hotknot_transfer);
                        }
                        else {
                            mHotknotButton.setImageResource(R.drawable.se_btn_hotknot_normal); 
                        }
                    }
                    break;
                // modify by jjlin for HotKnot End
            }
        }
    };

    // modify by jjlin for HotKnot Begin    
    public void onHotKnotSendComplete() {
        Log.d(TAG, "HotKnot: onHotKnotSendComplete");
        Message message = new Message();
        message.what = MSG_HOTKNOT_COMPLETE;
        mHandler.sendMessage(message);
    }
    
    @Override
    public void onHotKnotModeChanged(boolean isInShareMode) {
        // TODO Auto-generated method stub
        Log.d(TAG, "HotKnot: onHotKnotModeChanged");
        Message message = new Message();
        message.what = MSG_HOTKNOT_MODECHANGED;
        mHandler.sendMessage(message);
    }
    // modify by jjlin for HotKnot End
}
