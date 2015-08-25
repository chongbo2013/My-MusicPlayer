package com.lewa.player.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.lewa.Lewa;
import com.lewa.kit.ActivityHelper;
import com.lewa.player.R;
import com.lewa.player.activity.BaseFragmentActivity;
import com.lewa.player.listener.CallbackPlayListener;
import com.lewa.player.listener.PlayStatusListener;
import com.lewa.player.model.PlayStatus;
import com.lewa.player.online.OnlineLoader;
import java.lang.ref.WeakReference;
import android.graphics.Bitmap;
import com.lewa.util.BlurRunnable;



/**
 * Created by wuzixiu on 1/6/14.
 */
public class BaseFragment extends Fragment {

    private static final String TAG = "BaseFragment";//.class.getName();
    boolean hasBackground = false;
    PlayStatusListener mPlayStatusListener = null;

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        Log.i(TAG, "On resume.");
        super.onResume();
        Lewa.registerPlayStatusListener(mPlayStatusListener);

        PlayStatus playStatus = Lewa.getPlayStatus();

        if (playStatus != null) {
            refreshPlayStatus(playStatus);

            if(hasBackground && mPlayStatusListener != null) {
                Lewa.getAndBlurCurrentCoverUrl(mPlayStatusListener);
            }
        } else {
            BlurRunnable.blurDefaultBg(this.getActivity().getResources(), mPlayStatusListener);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.i(TAG, "On stop.");
        Lewa.unregisterPlayStatusListener(mPlayStatusListener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "On destroy.");
    }

    protected void initPlayerStatusListener() {
        Log.i(TAG, "initPlayerStatusListener");
        mPlayStatusListener = new MyPlayStatusListener();
    };

    protected void refreshSongsData(Long onlineId, Long localId) {  //this funcation is used for MyPlayStatusListener###onSongDownloaded
        //funcation is null, this subclass implements this.
    }

    void refreshPlayStatus(PlayStatus playStatus) {

    }

    protected void prepareForPlay(final CallbackPlayListener callbackPlayListener) {
        if(!OnlineLoader.isWiFiActive(getActivity()) && OnlineLoader.isNetworkAvailable()) {
//            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//            builder.setTitle(R.string.traffic_tip_text)
//                    .setMessage(R.string.traffic_tip_message_text)
//                    .setPositiveButton(getResources().getString(R.string.continue_play_text), new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int id) {
//                            dialog.dismiss();
                            callbackPlayListener.execute();
//                        }
//                    })
//                    .setNegativeButton(getResources().getString(R.string.cancel_cn_text), new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int id) {
//                            dialog.dismiss();
//                            ActivityHelper.goLibraryMine(getActivity());
//                        }
//                    }).create().show();

        } else if(!OnlineLoader.isWiFiActive(getActivity()) && !OnlineLoader.isNetworkAvailable()) {//!OnlineLoader.IsConnection(getActivity())){
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.no_network)
                    .setMessage(R.string.no_network_text)
                    .setPositiveButton(getResources().getString(R.string.ok_cn_text), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();                            
                        }
                    }) .create().show();            
        } else {
            callbackPlayListener.execute();
        }

    }

    protected String getName() {
        return this.getClass().getName();
    }

    class MyPlayStatusListener implements PlayStatusListener {
        private WeakReference<BaseFragment> ref = null;

        public MyPlayStatusListener() {
            this.ref = new WeakReference<BaseFragment>(BaseFragment.this);
        }

        @Override
        public String getId() {
            //return BaseFragment.class.getName();
            BaseFragment fragment = ref.get();
            return fragment.getName();
        }

        @Override
        public void onPlayStatusChanged(PlayStatus status) {
        Log.i(TAG, "onPlayStatusChanged ............");
            BaseFragment fragment = ref.get();
Log.i(TAG, "onPlayStatusChanged");
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
            BaseFragment fragment = ref.get();

            if (fragment != null) {
                fragment.refreshSongsData(onlineId, localId);
            }
        }
    }
}
