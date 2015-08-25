package com.lewa.player.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.baidu.music.model.Channel;
import com.baidu.music.model.Radio;
import com.lewa.Lewa;
import com.lewa.il.MusicInterfaceLayer;
import com.lewa.player.R;
import com.lewa.player.activity.LibraryActivity;
import com.lewa.player.adapter.LibraryBrowseAdapter;
import com.lewa.player.db.DBService;
import com.lewa.player.fragment.SearchFragment.SearchType;
import com.lewa.player.helper.PlaylistHelper;
import com.lewa.player.listener.CallbackPlayListener;
import com.lewa.player.listener.LibraryListener;
import com.lewa.player.model.Playlist;
import com.lewa.player.model.SongCollection;
import com.lewa.util.LewaUtils;
import com.lewa.view.ClearEditText;
import com.lewa.view.MaskEndGridView;

import java.lang.ref.WeakReference;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import com.lewa.player.online.OnlineLoader;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.widget.Toast;



/**
 * Created by wuzixiu on 11/27/13.
 */
public class LibraryBrowseFragment extends BaseFragment implements View.OnClickListener, View.OnFocusChangeListener, View.OnTouchListener { //AdapterView.OnItemClickListener, 
    private static final String TAG = "LibraryBrowseFragment";//.class.getName();

    private View rootView;
    private ClearEditText searchEt;
    private MaskEndGridView mGv;
    private LibraryBrowseAdapter mAdapter;
    private LibraryListener mLibraryListener;
    final List<Playlist> mPlaylists = new ArrayList<Playlist>();
    private boolean dataRequested = false;
    private static final int REFRESH = 0;
    private boolean requestFail=false;

    public LibraryBrowseFragment() {
        mAdapter = new LibraryBrowseAdapter(this, mPlaylists);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_library_browse, container, false);
        //searchEt = (ClearEditText) rootView.findViewById(R.id.et_search);
        //View searchBar = inflater.inflate(R.layout.search_bar_browser, null, false);
        //searchEt =  (ClearEditText) searchBar.findViewById(R.id.et_search);
        mGv = (MaskEndGridView) rootView.findViewById(R.id.gv);
        //mGv.addHeaderView(searchBar);
        mGv.setOnTrackListener(mLibraryListener);
        mGv.setAdapter(mAdapter);
        //mGv.setOnItemClickListener(this);
        //searchEt.setOnFocusChangeListener(this);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, "On resume, list size: " + mPlaylists.size());

        try {
            if (!dataRequested) {
                mPlaylists.clear();
                mPlaylists.addAll(DBService.findPlaylistsForBrowse());
                mAdapter.notifyDataSetChanged();
                Log.i(TAG, "Start get radio channels.");
                MusicInterfaceLayer.getInstance().requestRadioList(getActivity(), new MyOnGetRadioListListener(this));
            } else {
                Log.i(TAG, "Data requested, just refresh.");
                mAdapter.notifyDataSetChanged();
            }

        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Failed to query playlists: \n" + e.getMessage());
        }
    }

    public static class MyOnGetRadioListListener implements MusicInterfaceLayer.OnGetRadioListListener {
        private WeakReference<LibraryBrowseFragment> ref = null;

        public MyOnGetRadioListListener(LibraryBrowseFragment fragment) {
            ref = new WeakReference<LibraryBrowseFragment>(fragment);
        }

		@Override
		public void onGetRadioList(List<Radio> lists) {
			   Log.i(TAG, "Got radio channels." + (lists == null ? 0 : lists.size()));

	            LibraryBrowseFragment fragment = ref.get();
	            if (fragment == null) return;

//	            Message msg = fragment.mHandler.obtainMessage(REFRESH);
//	            msg.obj = lists;
//	            fragment.mHandler.sendMessage(msg);
	            fragment.refreshList(lists);
		}
    }

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case REFRESH:
                    refreshList((List<Radio>) msg.obj);
                    break;
            }
        }
    };

    private void refreshList(List<Radio> lists) {
        mPlaylists.clear();
//        List<Playlist> playlists = new ArrayList<Playlist>();

        try {
            mPlaylists.addAll(DBService.findPlaylistsForBrowse());
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (lists != null) {
            for (Channel data : lists.get(0).getItems()) {
                mPlaylists.add(Playlist.fromBdChannel(data));
            }
            requestFail=false;
        }else{
        	requestFail=true;
        }

        Log.i(TAG, "Refresh playlist gridview: " + mPlaylists.size());
//            mAdapter.setData(playlists);
//            mGv.setAdapter(mAdapter);
//        Log.i(TAG, "Playlists: " + mPlaylists);
//        mPlaylists.clear();
//        mPlaylists.addAll(playlists);
        mAdapter.notifyDataSetChanged();
//            mGv.invalidateViews();
        dataRequested = true;

    }
    
    public void requestRadioList(){
    	LewaUtils.logE(TAG, "requestRadioList again requestFail: "+requestFail);
    	if(requestFail&&isAdded()){
    		 MusicInterfaceLayer.getInstance().requestRadioList(getActivity(), new MyOnGetRadioListListener(this));
    	}
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);

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

    public void onStop() {
        super.onStop();
        Log.i(TAG, "On stop, list size: " + mPlaylists.size());
    }

    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "On destroy, list size: " + mPlaylists.size());
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()) {
            case R.id.iv:
            	//pr942250 add by wjhu
            	//to play fm music when press the image
            	final Playlist playlisttmp = (Playlist) v.getTag(R.id.tag_entity);
                handleItemClickWithCheckNetwork(playlisttmp);
                break;
            case R.id.button:
                final Playlist playlist = (Playlist) v.getTag(R.id.tag_entity);
                handleItemClickWithCheckNetwork(playlist);
                break;
        }
    }

    @Override
    public void onFocusChange(View view, boolean hasFocus) {
        if (hasFocus) {
            searchEt.clearFocus();
            mLibraryListener.showSearchFragment(SearchType.ONLINE);
//            Log.i(TAG, "Adapter list size: " + mAdapter.size());
        }
    }

    /*@Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final Playlist playlist = (Playlist) mAdapter.getItem(position);
        handleItemClickWithCheckNetwork(playlist);

    }*/
    private void handleItemClickWithCheckNetwork(final Playlist playlist) {
        if(!OnlineLoader.isWiFiActive(getActivity()) && !OnlineLoader.isNetworkAvailable()) {//!OnlineLoader.IsConnection(getActivity())){
            Toast.makeText(getActivity(), Lewa.string(R.string.no_network_text), Toast.LENGTH_SHORT).show();
        } else {

            handleItemClick(playlist);
        }
    }
    private void handleItemClick(final Playlist playlist) {
        if (playlist == null) return;

        switch (playlist.getType()) {
            case ONLINE_CATEGORY:
                mLibraryListener.showOnlinePlaylistFragment();
                break;
            case TOP_LIST_CATEGORY:
                mLibraryListener.showTopListFragment();
                break;
            case ALL_STAR_CATEGORY:
                mLibraryListener.showAllStarFragment();
                break;
            case FM:
                prepareForPlay(new CallbackPlayListener() {
                    @Override
                    public void execute() {
                        PlaylistHelper.getFMSongs(getActivity(), playlist, new PlaylistHelper.GetSongsListener() {
                            @Override
                            public void onGotSongs(Playlist playlist) {
                                SongCollection songCollection = new SongCollection(SongCollection.Type.PLAYLIST, playlist);
                                Lewa.playerServiceConnector().playSongCollection(getActivity(), songCollection, -1);
                            }
                        });
                    }
                });
                break;
        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        return false;
    }
}
