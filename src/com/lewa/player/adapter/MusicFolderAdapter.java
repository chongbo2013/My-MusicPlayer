package com.lewa.player.adapter;

import android.content.AsyncQueryHandler;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.lewa.player.MusicUtils;
import com.lewa.player.R;
import com.lewa.player.activity.SelectFolderActivity;

import java.util.ArrayList;


public class MusicFolderAdapter extends BaseAdapter {

    private TextView mFolderTitle;
    private TextView mFolderPath;
    private CheckBox mCheckBox;
    private ArrayList<String> mPathList;
    private ArrayList<String> mPathListInDB;
    private SelectFolderActivity mActivity;
    private int mIsOuter;
    private AsyncQueryHandler mQueryHandler;
    private Context mContext;
    public MusicFolderAdapter(SelectFolderActivity activity, ArrayList<String> list, ArrayList<String> listInDB, int isOut, Context context) {
        mContext=context;
        // TODO Auto-generated constructor stub
        mPathList = list;
        mPathListInDB = listInDB;
        mActivity = activity;
        mIsOuter = isOut;
 //       mQueryHandler = new QueryHandler(mActivity.getContentResolver());
    }
    
    public int getCount() {
        // TODO Auto-generated method stub
        
        if(mIsOuter == 0) {
            if(mPathList != null) {
                return mPathList.size();
            } 
        } else if(mIsOuter == 1){
            if(mPathListInDB != null) {
                return mPathListInDB.size();
            }
        }
        return 0;
    }

    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }
    
    public AsyncQueryHandler getQueryHandler() {
        return mQueryHandler;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        View view;

        view = LayoutInflater.from(mActivity).inflate(
                R.layout.folder_list_item, null);
        mFolderTitle = (TextView) view.findViewById(R.id.folder_title);
        mFolderPath = (TextView) view.findViewById(R.id.folder_path);
        mCheckBox = (CheckBox) view.findViewById(R.id.folder_checked);

        String path;
        if (mIsOuter == 1) {
            path = mPathListInDB.get(position);
        } else {
            path = mPathList.get(position);
        }
        String titleStr = path.substring(path.lastIndexOf("/") + 1,
                path.length());
        String title="";
		try {
			title = titleStr.replaceFirst(titleStr.substring(0, 1), titleStr
			        .substring(0, 1).toUpperCase());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			title=titleStr;
		}
		// pr938097 modify by wjhu
		// String pathStr = null;
		// if (path.startsWith("/mnt/sdcard")) {
		// pathStr = path.substring(4, path.length());
		// }else if(path.startsWith("/storage/sdcard")){
		// pathStr = path.substring(8, path.length());
		// }

        mFolderPath.setText(path);

        if (mIsOuter == 1) {
            ImageView folderIcon = (ImageView) view
                    .findViewById(R.id.folder_icon);
            folderIcon.setImageResource(R.drawable.folder_outer);
            int count = MusicUtils.getSongListForFolder(mActivity, path).length;
            mFolderTitle.setText(title + " (" + count + ") ");
            mCheckBox.setVisibility(View.GONE);
            LinearLayout folderInfo = (LinearLayout) view
                    .findViewById(R.id.folder_info);
            LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT,
                    LayoutParams.WRAP_CONTENT);
            mFolderTitle.setLayoutParams(lp);
            mFolderPath.setLayoutParams(lp);
            view.setTag(path);
        } else {
            mFolderTitle.setTextColor(mContext.getResources().getColor(com.lewa.internal.R.color.primary_text_holo_light));
            mFolderTitle.setText(title);
            mCheckBox.setVisibility(View.VISIBLE);
            mCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {

                public void onCheckedChanged(CompoundButton buttonView,
                        boolean isChecked) {
                    // TODO Auto-generated method stub
                    mActivity.setItemState(position, isChecked);
                    mActivity.updateActionModeTitle();
                }
            });

            if (mPathListInDB != null && mPathListInDB.size() > 0) {
                if (mPathListInDB.contains(path)) {
                    mCheckBox.setChecked(true);
                } else {
                    mCheckBox.setChecked(false);
                }
            }
            view.setTag(mCheckBox);
        }
        return view;
    }
}
