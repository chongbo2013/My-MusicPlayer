package com.lewa.player.activity;

import lewa.support.v7.app.ActionBar.LayoutParams;
import lewa.support.v7.app.ActionBarActivity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import lewa.support.v7.view.ActionMode;
import lewa.support.v7.view.ActionMode.Callback;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.lewa.ExitApplication;
import com.lewa.player.MusicUtils;
import com.lewa.player.R;
import com.lewa.player.adapter.MusicFolderAdapter;
import com.lewa.view.NowPlayingController;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.ArrayList;


public class SelectFolderActivity extends ActionBarActivity implements MusicUtils.Defs, OnItemClickListener {

    private static final String TAG = "SelectFolderActivity";
    private static final int ID_MUSIC_NO_SONGS_CONTENT = 100;
    
    private ArrayList<String> mFolderPathInDB = new ArrayList<String>();
    private ArrayList<String> mPathList = new ArrayList<String>();
    private MusicFolderAdapter mAdapter;
    private int mIsOuter;
    private LinearLayout mLinear;
    private NowPlayingController Artistactionbar;
    private String mSelectedPath;
    private int mNoSongsPaddingTop;
    private String mSelectedTitle;
    private Bitmap bitmap;
    private ListView mList;
    private BitmapDrawable bitmapDrawable;
    private ActionMode mActionMode;

    public SelectFolderActivity() {
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        
        ExitApplication exit = (ExitApplication) getApplication();
        exit.addActivity(this);

        Intent intent = this.getIntent();
        mIsOuter = intent.getIntExtra("isOuter", 0);

        if (mIsOuter == 1) {
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            setTheme(android.R.style.Theme_Holo_Light_NoActionBar);
            setContentView(R.layout.alltracklist);
            Artistactionbar = (NowPlayingController) findViewById(R.id.nowplaying_track);
            Artistactionbar.setActivity(this);
            
            if (MusicUtils.sService != null && Artistactionbar != null) {
                Artistactionbar.setMediaService(MusicUtils.sService);
            }

            mList = (ListView) findViewById(R.id.select_folder_list);
            mList.setCacheColorHint(0);
            mList.setDivider(null);
            mList.setOnCreateContextMenuListener(this);

            mNoSongsPaddingTop = getResources().getDimensionPixelOffset(
                    R.dimen.no_songs_padding_top);
            setupNoSongsView();

            IntentFilter f = new IntentFilter();
            f.addAction(Intent.ACTION_MEDIA_SCANNER_STARTED);
            f.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
            f.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
            f.addAction(Intent.ACTION_MEDIA_SHARED);
            f.addDataScheme("file");
            registerReceiver(mScanListener, f);

        } else {
            setTheme(R.style.SpiltActionTheme);
            setContentView(R.layout.select_folder);
            this.setTitle(R.string.select_folder);
            mList = (ListView) findViewById(R.id.select_folder_list);
            mList.setCacheColorHint(0);
        }

        if (mIsOuter == 1) {
            mLinear = (LinearLayout) findViewById(R.id.linear_trackpage);
            mLinear.post(new Runnable() {
                public void run() {
                    // TODO Auto-generated method stub
                    Bitmap back = MusicUtils.getDefaultBg(getApplicationContext(), 0);
                    MusicUtils.setBackground(new WeakReference<View>(mLinear), back);
                }
            });
        }
        if(mIsOuter!=1){
        	mActionMode = startSupportActionMode(new SFCallBack(SelectFolderActivity.this));
        	setAdapter();
        }
    }

    private BroadcastReceiver mScanListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Intent.ACTION_MEDIA_SCANNER_STARTED.equals(action)
                    || Intent.ACTION_MEDIA_SCANNER_FINISHED.equals(action)) {
                MusicUtils.setSpinnerState(SelectFolderActivity.this);
                mReScanHandler.sendEmptyMessage(0);
            }
            if (Intent.ACTION_MEDIA_UNMOUNTED.equals(action)) {
                mReScanHandler.sendEmptyMessage(1);
            }
        }
    };

    private Handler mReScanHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                mAdapter = new MusicFolderAdapter(SelectFolderActivity.this,
                        mPathList, mFolderPathInDB, 2,getApplicationContext());
            } else {
                mPathList = MusicUtils.getPathList(SelectFolderActivity.this);
                String[] folderPathinDB = MusicUtils
                        .getFolderPath(SelectFolderActivity.this);
                if (mFolderPathInDB != null && mFolderPathInDB.size() != 0) {
                    mFolderPathInDB.clear();
                }

                if (mPathList != null && folderPathinDB != null) {

                    if (folderPathinDB.length > 0) {
                        for (int i = 0; i < folderPathinDB.length; i++) {
                            mFolderPathInDB.add(folderPathinDB[i]);
                        }
                    } else {
                        for (int i = 0; i < mPathList.size(); i++) {
                            mFolderPathInDB.add(mPathList.get(i));
                        }
                    }

                    mAdapter = new MusicFolderAdapter(SelectFolderActivity.this,
                            mPathList, mFolderPathInDB, mIsOuter,getApplicationContext());
                }
            }
            if (mAdapter != null) {
                mList.setAdapter(mAdapter);
                mAdapter.notifyDataSetChanged();
            }
            setupNoSongsView();
            if (MusicUtils.sService != null && Artistactionbar != null) {
                Artistactionbar.setMediaService(MusicUtils.sService);
            }
        }
    };
	

    @Override
    protected void onDestroy() {
        mAdapter = null;
        if (Artistactionbar != null) {
            Artistactionbar.destroyNowplaying();
        }
        if (mIsOuter == 1) {
            unregisterReceiver(mScanListener);
        }

        if(bitmap!=null&&!bitmap.isRecycled()){
            bitmap.recycle();
            bitmap=null;
        }

        ExitApplication exit = (ExitApplication) getApplication();
        exit.removeActivity(this);
        System.gc();
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        if(mIsOuter==1) {
            setAdapter();
        }
        super.onResume();
    }

    private void setAdapter() {
        mPathList = MusicUtils.getPathList(this);
        String[] folderPathinDB = MusicUtils.getFolderPath(this);

        mFolderPathInDB.clear();
        if (mPathList != null && folderPathinDB != null) {
            if (folderPathinDB.length > 0) {
                for (int i = 0; i < folderPathinDB.length; i++) {
                    if(mPathList.contains(folderPathinDB[i])){
                        mFolderPathInDB.add(folderPathinDB[i]);
                    }else{
                        MusicUtils.deleteFolderPath(getApplicationContext(), folderPathinDB[i]);
                    }
                }
            } else {
                for (int i = 0; i < mPathList.size(); i++) {
                    mFolderPathInDB.add(mPathList.get(i));
                }
            }

            mAdapter = new MusicFolderAdapter(this, mPathList, mFolderPathInDB,
                                                    mIsOuter, getApplicationContext());
            if (mAdapter != null) {
                mList.setAdapter(mAdapter);
                mList.setOnItemClickListener(SelectFolderActivity.this);
            }
        }
    }

    public void setItemState(int position, boolean isSelect) {
        String path = mPathList.get(position);
        if (isSelect == true) {
            if (!mFolderPathInDB.contains(path)) {
                mFolderPathInDB.add(path);
            }
        } else {
            mFolderPathInDB.remove(path);
        }
    }

	private void selectDone() {
		if (mFolderPathInDB != null && mFolderPathInDB.size() == 0) {
            Toast.makeText(this, R.string.no_folder, 500).show();
            return;
        }

        String[] path = new String[mFolderPathInDB.size()];
        for (int i = 0; i < path.length; i++) {
            path[i] = mFolderPathInDB.get(i);
        }
        String nowPlayingpath=MusicUtils.getSongPath(getApplicationContext(), MusicUtils.getCurrentAudioId());
        MusicUtils.updateFolderPath(this, path);
        finish();

        Intent intent = new Intent();
        intent.setClass(this, LibraryActivity.class);
        intent.putExtra("folder", true);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        if(nowPlayingpath!=null&&!mFolderPathInDB.contains(nowPlayingpath.substring(0, nowPlayingpath.lastIndexOf("/")))){
            Intent filterIntent=new Intent(MusicUtils.ACTION_FILTER);
            sendBroadcast(filterIntent);
        }
	}

    protected void onListItemClick(ListView l, View v, int position, long id) {
        // TODO Auto-generated method stub
        if (mIsOuter == 0) {
            CheckBox itemCheckBox = (CheckBox) v.getTag();
            itemCheckBox.setChecked(!itemCheckBox.isChecked());
        }
    }

    public void setSelectedFolder(String path) {
        mSelectedPath = path;
    }

/*
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenuInfo menuInfo) {
        menu.add(0, PLAY_SELECTION, 0, R.string.play_selection);
        menu.add(0, ADD_TO_PLAYLIST, 0,R.string.add_to_playlist);

        AdapterContextMenuInfo mi = (AdapterContextMenuInfo) menuInfo;
        int position = mi.position;
        View selectView = mAdapter.getView(position, null, null);
        mSelectedPath = selectView.getTag().toString();
        mSelectedTitle = mSelectedPath.substring(
                mSelectedPath.lastIndexOf("/") + 1, mSelectedPath.length());
        menu.setHeaderTitle(mSelectedTitle);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        long[] selectList = MusicUtils
                .getSongListForFolder(this, mSelectedPath);
        switch (item.getItemId()) {
        case PLAY_SELECTION: {
            // play the selected album
            MusicUtils.playAll(this, selectList, 0);
            return true;
        }
        
        case ADD_TO_PLAYLIST:
            AlertDialog.Builder builder=new AlertDialog.Builder(this);
            builder.setTitle(R.string.add_to_playlist);
            MusicUtils.makePlaylistMenu(this, builder, false, selectList);
            return true;

        case PLAYLIST_SELECTED: {
            long playlist = item.getIntent().getLongExtra("playlist", 0);
            MusicUtils.addToPlaylist(this, selectList, playlist);
            return true;
        }

        case QUEUE: {
            MusicUtils.addToCurrentPlaylist(this, selectList);
            return true;
        }

        case NEW_PLAYLIST: {
            MusicUtils.addToNewPlaylist(this, selectList, -1);
            return true;
        }
        }
        return super.onContextItemSelected(item);
    }

      @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                Intent intent = new Intent(this, SettingActivity.class);
//                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
                return true;
            }
        }
        return false;
    }
*/

    public void setupNoSongsView() {
        View view = findViewById(ID_MUSIC_NO_SONGS_CONTENT);

        if (MusicUtils.mHasSongs == false) {
            if (view != null) {
                view.setVisibility(View.VISIBLE);
            } else {
                view = getLayoutInflater().inflate(R.layout.music_no_songs,
                		(ListView) findViewById(R.id.select_folder_list), false);
                view.setId(ID_MUSIC_NO_SONGS_CONTENT);
                // view.setPadding(0, mNoSongsPaddingTop, 0, 0);
                addContentView(view, new LayoutParams(
                        LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
            }
            TextView txtView = (TextView) view.findViewById(R.id.text_no_songs);
            String status = Environment.getExternalStorageState();
            if (!(status.equals(Environment.MEDIA_MOUNTED))) {
                txtView.setText(R.string.nosd);
            } else {
                txtView.setText(R.string.no_folders);
            }
        } else {
            if (view != null)
                view.setVisibility(View.GONE);
        }
    }
    

    //pr938097 modify by wjhu begin
    //to support the new lewa actionbar
    private class SFCallBack implements Callback {
    	private Context mContext;
    	public SFCallBack(Context context){
    		mContext=context;
    	}
		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			// TODO Auto-generated method stub
			switch (item.getItemId()) {
			case lewa.support.v7.appcompat.R.id.action_mode_right_button :
				if(isSelectAll()){
					mode.setRightActionButtonResource(lewa.R.drawable.ic_menu_select_all);
					unselectAll();
				}else{
                    		mode.setRightActionButtonResource(lewa.R.drawable.ic_menu_clear_select);
                    		selectAll();
				}
				MusicUtils.updateActionModeTitle(mode, mContext, mFolderPathInDB.size());
				break;
			case R.id.action_done:
				selectDone();
				break;
			}
			return true;
		}

		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			// TODO Auto-generated method stub
			mode.setRightActionButtonVisibility(View.VISIBLE);
                    if(isSelectAll()) {
			    mode.setRightActionButtonResource(lewa.R.drawable.ic_menu_clear_select);
                    } else {
                        mode.setRightActionButtonResource(lewa.R.drawable.ic_menu_select_all);
                    }
			int count=mFolderPathInDB.size();
			MusicUtils.updateActionModeTitle(mode, mContext, count);
			MenuInflater inflater=((ActionBarActivity) mContext).getMenuInflater();
                    inflater.inflate(R.menu.operation_menu, menu);
			return true;
		}

		@Override
		public void onDestroyActionMode(ActionMode arg0) {
			// TODO Auto-generated method stub
			finish();
		}

		@Override
		public boolean onPrepareActionMode(ActionMode arg0, Menu arg1) {
			// TODO Auto-generated method stub
			return false;
		}

		public void onItemCheckedStateChanged(ActionMode mode, int arg1,
				long arg2, boolean arg3) {
			int count=mFolderPathInDB.size();
			MusicUtils.updateActionModeTitle(mode, mContext, count);
                    Log.i(TAG, "onItemCheckedStateChanged");
                    if(isSelectAll()) {
			    mode.setRightActionButtonResource(lewa.R.drawable.ic_menu_clear_select);
                    } else {
                        mode.setRightActionButtonResource(lewa.R.drawable.ic_menu_select_all);
                    }
		}
    	
    }
    
//    private class ModeCallback implements ListView.MultiChoiceModeListener{
//    	private Context mContext;
//    	public ModeCallback(Context context){
//    		mContext=context;
//    	}
//		@Override
//		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
//			// TODO Auto-generated method stub
//			switch (item.getItemId()) {
//			case lewa.R.id.action_mode_right_button:
//				if(isSelectAll()){
//					//mode.setRightActionButtonResource(lewa.R.drawable.ic_menu_select_all);
//					unselectAll();
//				}else{
//                    		//mode.setRightActionButtonResource(lewa.R.drawable.ic_menu_clear_select);
//                    		selectAll();
//				}
//				MusicUtils.updateActionModeTitle(mode, mContext, mFolderPathInDB.size());
//				break;
//			case R.id.action_done:
//				selectDone();
//				break;
//			}
//			return true;
//		}
//
//		@Override
//		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
//			// TODO Auto-generated method stub
//			//mode.setRightActionButtonVisibility(View.VISIBLE);
//                    if(isSelectAll()) {
//			    //mode.setRightActionButtonResource(lewa.R.drawable.ic_menu_clear_select);
//                    } else {
//                        //mode.setRightActionButtonResource(lewa.R.drawable.ic_menu_select_all);
//                    }
//			int count=mFolderPathInDB.size();
//			MusicUtils.updateActionModeTitle(mode, mContext, count);
//			MenuInflater inflater=((ActionBarActivity) mContext).getMenuInflater();
//                    inflater.inflate(R.menu.operation_menu, menu);
//			return true;
//		}
//
//		@Override
//		public void onDestroyActionMode(ActionMode arg0) {
//			// TODO Auto-generated method stub
//			finish();
//		}
//
//		@Override
//		public boolean onPrepareActionMode(ActionMode arg0, Menu arg1) {
//			// TODO Auto-generated method stub
//			return false;
//		}
//
//		@Override
//		public void onItemCheckedStateChanged(ActionMode mode, int arg1,
//				long arg2, boolean arg3) {
//			int count=mFolderPathInDB.size();
//			MusicUtils.updateActionModeTitle(mode, mContext, count);
//                    Log.i(TAG, "onItemCheckedStateChanged");
//                    if(isSelectAll()) {
//			    //mode.setRightActionButtonResource(lewa.R.drawable.ic_menu_clear_select);
//                    } else {
//                        //mode.setRightActionButtonResource(lewa.R.drawable.ic_menu_select_all);
//                    }
//		}
//    	
//    }
//pr938097 modify by wjhu end
    public void updateActionModeTitle(){
    	if(mActionMode!=null){
            MusicUtils.updateActionModeTitle(mActionMode, getApplicationContext(), mFolderPathInDB.size());
            if(isSelectAll()) {
                mActionMode.setRightActionButtonResource(lewa.R.drawable.ic_menu_clear_select);
            } else {
                mActionMode.setRightActionButtonResource(lewa.R.drawable.ic_menu_select_all);
            }
    	}
    }
    
   public void selectAll(){
	   for(int i=0;i<mPathList.size();i++){
		   String path=mPathList.get(i);
		   if (!mFolderPathInDB.contains(path)) {
               mFolderPathInDB.add(path);
           }
	   }
	   mAdapter.notifyDataSetChanged();
   }
   
   public void unselectAll(){
	   mFolderPathInDB.clear();
	   mAdapter.notifyDataSetChanged();
   }
   
   public boolean isSelectAll(){
	   return mPathList.size()==mFolderPathInDB.size();
   }

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		CheckBox itemCheckBox = (CheckBox) view.getTag();
		itemCheckBox.setChecked(!itemCheckBox.isChecked());
	}

}
