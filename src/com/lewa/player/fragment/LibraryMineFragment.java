package com.lewa.player.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.lewa.player.R;
import com.lewa.Lewa;
import com.lewa.kit.ActivityHelper;
import com.lewa.player.adapter.LibraryMineAdapter;
import com.lewa.player.db.DBService;
import com.lewa.player.listener.LibraryListener;
import com.lewa.player.model.Playlist;
import com.lewa.player.model.Playlist.TYPE;
import com.lewa.player.model.SongCollection;
import com.lewa.util.Constants;
import com.lewa.view.MaskEndListView;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by wuzixiu on 11/27/13.
 */
public class LibraryMineFragment extends BaseFragment implements View.OnClickListener, AdapterView.OnItemClickListener{
    private static final String TAG = "LibraryMineFragment";

    MaskEndListView mLv;

    private LibraryMineAdapter mAdapter;
    private LibraryListener mLibraryListener;

    private List<Playlist> mPlaylists;

    public LibraryMineFragment() {
        
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_mine_lv, container, false);
        mLv = (MaskEndListView) rootView.findViewById(R.id.lv);

        View footerView = Lewa.inflater().inflate(R.layout.footer_library_mine, null);
        View mCreatePlaylistBtn = footerView.findViewById(R.id.bt_create_playlist);
        mCreatePlaylistBtn.setOnClickListener(this);
        mLv.addFooterView(footerView);
        
        mAdapter = new LibraryMineAdapter(this);
        mLv.setOnItemClickListener(this);
        mLv.setAdapter(mAdapter);

        
        mLv.setOnTrackListener(mLibraryListener);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        refreshData();
    }

    public void refreshData() {
        if(!isAdded()) {
            return;
        }
                       
        try {
            mPlaylists = DBService.findPlaylistsForMine();
        } catch (SQLException e) {                  
            mPlaylists = null;
            e.printStackTrace();                 
        }

        mAdapter.setData(mPlaylists);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        if (!hidden) {
            refreshData();
        }
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

    @Override
    public void onItemClick(AdapterView<?> parent, View view,
                            final int position, long id) {
        Playlist playlist = (Playlist) view.getTag(R.id.tag_entity);
        
        if(playlist != null && playlist.getType() == Playlist.TYPE.DOWNLOAD) {
            Constants.show_down_tip=false;
            mAdapter.notifyDataSetChanged();
        }
        
        if (playlist != null) {
            mLibraryListener.showSongInfoListFragment(playlist.getName(), 
                                        new SongCollection(SongCollection.Type.PLAYLIST, playlist));
        } 
        
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bt_create_playlist:
                ActivityHelper.goEditPlaylist(getActivity(), null);
                break;
            case R.id.bt_edit_playlist:
                openEditDialog(view);
                break;           
            case R.id.bt_play:
                mAdapter.reset();
                Playlist playlist = (Playlist) view.getTag(R.id.tag_entity);
                SongCollection songCollection = new SongCollection(SongCollection.Type.PLAYLIST, playlist);

                Lewa.playerServiceConnector().playSongCollection(getActivity(), songCollection, -1);
                break;
        }
    }

    public void openEditDialog(final View view) {
        final Playlist playlist = (Playlist) view.getTag(R.id.tag_entity);

        CharSequence[] items = new CharSequence[]{getResources().getString(R.string.item_modify_text), 
                                                getResources().getString(R.string.item_remove_text)};
        
        AlertDialog.Builder mEditBuilder = new AlertDialog.Builder(this.getActivity());
        
        mEditBuilder.setTitle(getResources().getString(R.string.playlist_dialog_title))
            .setCancelable(true)
            .setItems(items, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int position) {
                    if (position == 0) {
                        dialogInterface.dismiss();
                        ActivityHelper.goEditPlaylist(getActivity(), playlist.getId());
                    } else {
                        dialogInterface.dismiss();
                        openConfirmDialog(playlist);
                    }
                }
        }).create().show();

    }

    public void openConfirmDialog(final Playlist playlist) {
        AlertDialog.Builder mConfirmBuilder = new AlertDialog.Builder(this.getActivity());
        mConfirmBuilder.setTitle(getResources().getString(R.string.playlist_dialog_title))
            .setMessage(getResources().getString(R.string.remove_dialog_message_text))
            .setCancelable(true)
            .setPositiveButton(getResources().getString(R.string.ok_text), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    try {
                        DBService.removePlaylist(playlist);
                        mPlaylists.remove(playlist);
                        mAdapter.setData(mPlaylists);
                        mAdapter.notifyDataSetChanged();
                        dialog.dismiss();
                    } catch (SQLException e) {
                        e.printStackTrace();            
                    }
                }
            })
            .setNegativeButton(getResources().getString(R.string.cancel_text), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                }
            }).create().show();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}
