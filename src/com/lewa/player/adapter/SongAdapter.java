package com.lewa.player.adapter;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.baidu.music.download.DownloadEntry;
import com.baidu.music.download.DownloadStatus;
import com.lewa.Lewa;
import com.lewa.il.OnDownloadSongClickListener;
import com.lewa.il.MusicInterfaceLayer.OnDownloadProgressChangeListener;
import com.lewa.il.MusicInterfaceLayer.OnDownloadStatusChangeListener;
import com.lewa.player.R;
import com.lewa.player.db.DBService;
import com.lewa.player.model.MusicDownloadStatus;
import com.lewa.player.model.Playlist;
import com.lewa.player.model.Song;
import com.lewa.player.model.SongCollection;
import com.lewa.player.online.AppDownloadManager;
import com.lewa.util.Constants;
import com.lewa.util.DateUtils;
import com.lewa.util.LewaUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class SongAdapter extends BaseAdapter implements OnDownloadProgressChangeListener,OnDownloadStatusChangeListener,OnDownloadSongClickListener{

    private static final String TAG = "SongAdapter";

    private List<Song> mData = new ArrayList<Song>();
    private SongCollection mSongCollection;
    private Playlist.TYPE mPlayListType;
    private Playlist mPlayList;
    private HashMap<Long, Song> downloadSongs=new HashMap<Long, Song>();
    private long lastUpdateTime;
    private static long MINUPDATEDIFF=1000;
    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Song song = (Song) v.getTag(R.id.tag_entity);
            if (song != null && song.getId() != null) {
                Lewa.downloadSong(song);
            }
        }
    };

    public SongAdapter() {
    }

    @Override
    public int getCount() {
        int count = mSongCollection == null ? 0 : mSongCollection.getCount();

        Log.i(TAG, "item count = " + count);
        return count;
    }

    @Override
    public Object getItem(int position) {
//        return mData.get(position);
        return mSongCollection.getSong(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = Lewa.inflater().inflate(R.layout.album_song_item, null);
            holder = new ViewHolder(convertView);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Song song = null;
        if(mSongCollection.getType() == SongCollection.Type.ALBUM && null != mSongCollection.albumArtistName) {
            song = mData.get(position);
        } else {
            song = mSongCollection.getSong(position);
        }
        song.setCollection(mSongCollection);
        if (song.getType() == Song.TYPE.LOCAL||isDownloadList()) {
            holder.downloadBtn.setVisibility(View.GONE);
            if(song.getType() == Song.TYPE.LOCAL)
               holder.progress_ll.setVisibility(View.GONE);
        } else {
            holder.downloadBtn.setVisibility(View.VISIBLE);
            holder.downloadBtn.setTag(R.id.tag_entity, song);
            holder.downloadBtn.setOnClickListener(mOnClickListener);
        }

        holder.songNameTv.setText(song.getName());

        if (mSongCollection.getType() == SongCollection.Type.PLAYLIST && mPlayList != null && !mPlayList.isLocal()) {
        	holder.songDurationTv.setVisibility(View.VISIBLE);
            if (song.getArtist() != null && song.getArtist().getName() != null) {
                holder.songDurationTv.setText(song.getArtist().getName());
            } else {
                holder.songDurationTv.setText("");
            }
        } else {
        	if(isDownloadList()&&song.getType()==Song.TYPE.ONLINE){
        		MusicDownloadStatus downStatus=song.getDownStatus();
        		if(downStatus==null){
        			holder.songDurationTv.setText(R.string.wait_for_download);
        		}else{
        			showDownloadStatus(holder, song, downStatus);
        		}
        	}else{
        		holder.songDurationTv.setVisibility(View.VISIBLE);
        		 if (song.getArtist() != null && song.getArtist().getName() != null) {
                     holder.songDurationTv.setText(song.getArtist().getName());
        		 }else if (song.getDuration() != null && song.getDuration() > 0) {
 	                String durationStr = DateUtils.m2s2(new Date(song.getDuration()));
 	                holder.songDurationTv.setText(durationStr);
 	            }
        	}
        }

        convertView.setTag(R.id.tag_entity, song);
        convertView.setTag(holder);

        return convertView;
    }

	private void showDownloadStatus(ViewHolder holder, Song song,
			MusicDownloadStatus downStatus) {
		int status=downStatus.getStatus();
		if(status==DownloadStatus.STATUS_RUNNING){
			holder.songDurationTv.setVisibility(View.GONE);
			holder.progress_ll.setVisibility(View.VISIBLE);
			long currentBytes=downStatus.getCurrentBytes();
			long totalBytes=downStatus.getTotalBytes();
			holder.progressTv.setText(getProgressText(currentBytes, totalBytes));
			if(totalBytes!=0){
				holder.progressBar.setIndeterminate(false);
				holder.progressBar.setProgress((int) (100*currentBytes/totalBytes));
				holder.progressBar.setSecondaryProgress((int) (100*currentBytes/totalBytes));
			}else{
				holder.progressBar.setIndeterminate(true);
			}
		}else if(status!=DownloadStatus.STATUS_SUCCESS && status!=DownloadStatus.STATUS_ALREADY_EXIST){
			holder.progress_ll.setVisibility(View.GONE);
			holder.songDurationTv.setVisibility(View.VISIBLE);
			String msg=handleErrorMsg(song.getId(), status);
			holder.songDurationTv.setText(msg);
		}
	}

    public void setData(SongCollection songCollection) {
        if (songCollection != null) {
            Log.i(TAG, "Set data: " + songCollection.getType().name());

            this.mSongCollection = songCollection;

            if (songCollection != null && songCollection.getType() == SongCollection.Type.PLAYLIST) {
                mPlayList = (Playlist) songCollection.getOwner();
                if(isDownloadList()){
                	mData= this.mSongCollection.getSongs();
                	downloadSongs.clear();
                	for(int i=0;i<mData.size();i++){
                		Song song=mData.get(i);
                		if(song.getType()==Song.TYPE.ONLINE){
                			downloadSongs.put(song.getId(), song);
                		}
                	}
                }
//            if (playlist != null) {
//                mPlayListType = playlist.getType();
//            } else {
//                mPlayListType = null;
//            }
            }
        }

        if (songCollection != null && songCollection.getType() == SongCollection.Type.ALBUM && null != mSongCollection.albumArtistName) {
            mData= this.mSongCollection.getSongs();
        }
        notifyDataSetChanged();
    }

    public void songDownloaded() {
        if (mSongCollection != null) {
            DBService.matchSongs(mSongCollection.getSongs());
            notifyDataSetChanged();
        }
    }

    static class ViewHolder {
        ImageButton downloadBtn;
        TextView songNameTv;
        TextView songDurationTv;
        ProgressBar progressBar;
        TextView progressTv;
        LinearLayout progress_ll;
        public ViewHolder(View view) {
            downloadBtn = (ImageButton) view.findViewById(R.id.bt_download);
            songNameTv = (TextView) view.findViewById(R.id.tv_song_name);
            songDurationTv = (TextView) view.findViewById(R.id.tv_song_duration);
            progressTv=(TextView)view.findViewById(R.id.progress_text);
            progressBar=(ProgressBar)view.findViewById(R.id.download_progress);
            progress_ll=(LinearLayout)view.findViewById(R.id.progress_ll);
        }
    }

    public SongCollection getSongCollection() {
        return mSongCollection;
    }
    
    private boolean isDownloadList(){
        if(mPlayList!=null)
        	return mPlayList.getType()==Playlist.TYPE.DOWNLOAD;
        return false;
    }

	@Override
	public void onStatusChange(long musicId, int status) {
		// TODO Auto-generated method stub
	  	LewaUtils.logE(TAG, "download status: "+status);
		updateDownStatus(musicId,status);
		if(status==DownloadStatus.STATUS_SUCCESS || status==DownloadStatus.STATUS_ALREADY_EXIST){
			downloadSongs.remove(musicId);
		}
	}
	
	private void updateDownStatus(long musicId,int status){
		Song song=downloadSongs.get(musicId);
		if(song!=null){
			int position=mData.indexOf(song);
			MusicDownloadStatus downStatus=new MusicDownloadStatus();
			downStatus.setStatus(status);
			song.setDownStatus(downStatus);
			mData.set(position, song);
			downloadSongs.put(musicId, song);
			updateProgress(true);
		}
	}
	private String handleErrorMsg(long musicId, int status) {
		// TODO Auto-generated method stub
		Song song=downloadSongs.get(musicId);
		String msg=Lewa.context().getString(R.string.wait_for_download);
//		if(song!=null){
//			switch (status) {
//				case DownloadStatus.STATUS_NETWORK_NOT_AVAILABLE:
//					msg=Lewa.context().getString(R.string.no_network);
//					break;
//				case DownloadStatus.USER_ACTION_DOWNLOAD:
//					msg=Lewa.context().getString(R.string.wait_for_download);
//					break;
//				case DownloadStatus.STATUS_PENDING:
//					msg=Lewa.context().getString(R.string.wait_for_download);
//					break;
//				case DownloadStatus.STATUS_RUNNING_PAUSED:
//					msg=Lewa.context().getString(R.string.download_resume);
//					break;
//				case DownloadStatus.STATUS_SONG_ID_ERR:
//					msg=Lewa.context().getString(R.string.download_incorrect_Id);
//					break;
//				case DownloadStatus.STATUS_INSUFFICIENT_SPACE_ERROR:
//					msg=Lewa.context().getString(R.string.no_avaliable_space);
//					break;
//				case DownloadStatus.STATUS_DEVICE_NOT_FOUND_ERROR:
//					msg=Lewa.context().getString(R.string.sdcard_missing_title);
//					break;
//				default:
//					break;
//			}
//		}
		if(status >= DownloadStatus.STATUS_BAD_REQUEST && status<=DownloadStatus.STATUS_URL_NOT_FOUND){
			msg=Lewa.context().getString(R.string.download_fail);
		}else if(status == DownloadStatus.STATUS_SONG_COPY_ERR || status == DownloadStatus.STATUS_SONG_ID_ERR){
			msg=Lewa.context().getString(R.string.download_fail);
		}else if(status == DownloadStatus.STATUS_RUNNING_PAUSED || status == Constants.DOWNLOAD_STATUS_STOP){
			msg=Lewa.context().getString(R.string.download_resume);
			
		}
		return msg;
	}

	@Override
	public void onProgressChange(long musicId, long mCurrentBytes,
			long mTotalBytes,int status) {
		// TODO Auto-generated method stub
		Song song=downloadSongs.get(musicId);
		if(song!=null){
			int position=mData.indexOf(song);
			MusicDownloadStatus downStatus=new MusicDownloadStatus();
			downStatus.setCurrentBytes(mCurrentBytes);
			downStatus.setTotalBytes(mTotalBytes);
			downStatus.setStatus(status);
			song.setDownStatus(downStatus);
			mData.set(position, song);
			downloadSongs.put(musicId, song);
			updateProgress(false);
		}
	}
	
	private void updateProgress(boolean isStatusChange){
		synchronized (mData) {
			if(System.currentTimeMillis()-lastUpdateTime>MINUPDATEDIFF||isStatusChange){
				mSongCollection.setSongs(mData);
				notifyDataSetChanged();
				lastUpdateTime=System.currentTimeMillis();
			}
		}
	}
	
	private String getProgressText(long mCurrentBytes,long mTotalBytes){
		StringBuilder builder=new StringBuilder();
		builder.append(formatSize(mCurrentBytes)).append("M").append("/").append(formatSize(mTotalBytes)).append("M");
		return builder.toString();
	}
	
	private String formatSize(long bytes){
		long n=1024*1024;
		return String.format("%.1f", (float)bytes/n);
	}

	@Override
	public boolean onDownloadSongClick(Context context,Song song) {
		// TODO Auto-generated method stub
		if(isDownloadList()&&song.getType()==Song.TYPE.ONLINE){
			MusicDownloadStatus downStatus=song.getDownStatus();
			int status=downStatus.getStatus();
			LewaUtils.logE(TAG, "onDownloadSongClick status : "+status);
			switch (status) {
			case DownloadStatus.STATUS_RUNNING:
				AppDownloadManager.getInstance(context).pauseDownload(song);
				break;
			case DownloadStatus.STATUS_RUNNING_PAUSED:
				AppDownloadManager.getInstance(context).resumeDownload(song);
				break;
			default:
				Lewa.downloadSong(song);
				break;
			}
			return true;
		}
		return false;
	}
}
