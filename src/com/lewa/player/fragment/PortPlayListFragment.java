package com.lewa.player.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;

import com.lewa.Lewa;
import com.lewa.player.R;
import com.lewa.player.adapter.PlayingSongAdapter;
import com.lewa.player.helper.PlaylistHelper;
import com.lewa.player.listener.PlayControlListener;
import com.lewa.player.listener.PlayStatusListener;
import com.lewa.player.model.PlayStatus;
import com.lewa.player.model.Song;
import com.lewa.player.online.OnlineLoader;

import java.lang.ref.WeakReference;
import android.widget.Toast;

/**
 * Created by wuzixiu on 11/27/13.
 */
public class PortPlayListFragment extends PlayBaseFragment implements View.OnClickListener, View.OnTouchListener {

    private final String TAG = "PortPlayListFragment";  //.class.getName();

    ListView mSongLv;
    ImageButton mDownloadAllBtn;

    ImageButton mCoverBtn;
    ImageButton mMoreBtn;
    ViewGroup mMoreLo;
    private PlayingSongAdapter mPlayingSongAdapter;
    private PlayControlListener mPlayControlListener = null;
    public PortPlayListFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_playlist_port, container, false);
        mSongLv = (ListView) rootView.findViewById(R.id.lv_song);
        mDownloadAllBtn = (ImageButton) rootView.findViewById(R.id.bt_downloadall);
        //mCoverBtn = (ImageButton) rootView.findViewById(R.id.bt_cover);
        mMoreBtn = (ImageButton) rootView.findViewById(R.id.bt_more);
        mMoreLo = (ViewGroup) rootView.findViewById(R.id.llo2);
        super.initSettingBtn(rootView, this);
        mDownloadAllBtn.setOnClickListener(this);
        mArtistBtn = mDownloadAllBtn;
        mMoreBtn.setOnClickListener(this);
        //mCoverBtn.setOnClickListener(this);
        mPlayingSongAdapter = new PlayingSongAdapter(this);
        mSongLv.setAdapter(mPlayingSongAdapter);
        mPlayStatusListener = new MyPlayStatusListener();

        mSongLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                Song song = (Song) view.getTag(R.id.tag_entity);
                if(null == song) {
                    return;
                }
                //Song song=(Song) mPlayingSongAdapter.getItem(position);
                if(!OnlineLoader.isWiFiActive(getActivity())&& OnlineLoader.isNetworkAvailable()&& song.getType()==Song.TYPE.ONLINE) {
                    //pr955005 modify by wjhu
//                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//                    builder.setTitle(R.string.traffic_tip_text)
//                        .setMessage(R.string.traffic_tip_message_text)
//                        .setPositiveButton(getResources().getString(R.string.continue_play_text), new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int id) {
//                                dialog.dismiss();
                                Lewa.playerServiceConnector().setQueuePosition(position);
//                            }
//                            })
//                        .setNegativeButton(getResources().getString(R.string.cancel_cn_text), new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int id) {
//                                dialog.dismiss();
//                            }
//                        }).create().show();

                } else if(!OnlineLoader.isWiFiActive(getActivity()) && !OnlineLoader.isNetworkAvailable()&&song.getType()==Song.TYPE.ONLINE) {//!OnlineLoader.IsConnection(getActivity())){
                /*AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(R.string.no_network)
                    .setMessage(R.string.no_network_text)
                    .setPositiveButton(getResources().getString(R.string.ok_cn_text), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();                            
                    }
                    }) .create().show();*/
                    Toast.makeText(getActivity(), Lewa.string(R.string.no_network_text), Toast.LENGTH_SHORT).show();
                }else {
                    Lewa.playerServiceConnector().setQueuePosition(position);
                    mPlayControlListener.resetBg(0);
                }

                }
            });

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
            Log.e(TAG, "Activity should implement PlayControlListener.");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mPlayingSongAdapter.setData(PlaylistHelper.getPlayingSongs());
        PlaylistHelper.scrollToPlayingSong(mSongLv, mPlayingSongAdapter);
    }

   protected void refreshSongInfo() {
        if(null == mSongLv) {   //not init bug : 64429
            return;
        }
        refreshPlayStatus(Lewa.getPlayStatus());
    }

    @Override
    public void onClick(View v) {
        int vid = v.getId();
        Song song = null;
        PlayStatus playStatus = Lewa.getPlayStatus();
        if (playStatus != null) {
            song = Lewa.getPlayStatus().getPlayingSong();
        }

        switch (vid) {
            case R.id.bt_play:

                break;

            case R.id.bt_more:
                showOrHideMoreView(mMoreLo);
                break;
            case R.id.bt_downloadall:
            	//pr941186 add by wjhu begin
            	//push this button will download the song,and then this button
            	//will be unclickable
            	Song songToDownload = (Song) v.getTag(R.id.tag_entity);
                if(null != songToDownload) {
                    downloadSong(songToDownload);                    
                    mPlayingSongAdapter.notifyDataSetChanged();
                } else {
                    downloadSong(song);                  
                    refreshSongInfo();
                }
                mDownloadAllBtn.setClickable(false);
                mDownloadAllBtn.setAlpha(0.3f);//30%
                //pr941186 add by wjhu end
                break;
            case R.id.bt_add_to_list:
                addTo(song, null);
                break;
            case R.id.bt_as_bell:
                setAsBell(song);
                break;
            case R.id.bt_edit:
                editSong(song);
                break;
            case R.id.bt_download:
                Song downloadSong = (Song) v.getTag(R.id.tag_entity);
                if(null != downloadSong) {
                    downloadSong(downloadSong);                    
                    mPlayingSongAdapter.notifyDataSetChanged();
                } else {
                    downloadSong(song);                  
                    refreshSongInfo();
                }
                break;
            case R.id.bt_share:
                shareSong(song);
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

    class MyPlayStatusListener implements PlayStatusListener {
        private WeakReference<PortPlayListFragment> ref = null;

        public MyPlayStatusListener() {
            this.ref = new WeakReference<PortPlayListFragment>(PortPlayListFragment.this);
        }

        @Override
        public String getId() {
            return PortPlayListFragment.class.getName();
        }

        @Override
        public void onPlayStatusChanged(PlayStatus status) {
            PortPlayListFragment fragment = ref.get();

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
            PortPlayListFragment fragment = ref.get();

            if (fragment != null) {
                fragment.refreshPlaySongState(fragment, localId);
                fragment.mPlayingSongAdapter.songDownloaded();
            }
        }
    }

    void refreshPlayStatus(PlayStatus playStatus) {
        super.refreshPlayStatus(playStatus);
        mPlayingSongAdapter.setData(PlaylistHelper.getPlayingSongs());	//add by sjxu for refresh play list in time
        mPlayingSongAdapter.notifyDataSetChanged();
        PlaylistHelper.scrollToPlayingSong(mSongLv, mPlayingSongAdapter);
        Song playingSong = Lewa.getPlayingSong();
        if (playingSong != null) {
        	if (playingSong.getType() == Song.TYPE.LOCAL) {
        		mDownloadAllBtn.setAlpha(0.3f);
        		mDownloadAllBtn.setClickable(false);
        	}
        }
        refreshMoreBarState(playingSong);
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
                mShareBtn.setVisibility(View.VISIBLE);
            }
        }
    }*/

}
