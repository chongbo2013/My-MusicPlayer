package com.lewa.player.adapter;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.lewa.Lewa;
import com.lewa.player.R;
import com.lewa.player.db.DBService;
import com.lewa.player.model.Artist;
import com.lewa.player.model.ArtistCursorIndex;
import com.lewa.player.model.Playlist;
import com.lewa.player.online.DownLoadAsync;
import com.lewa.util.LewaUtils;
import com.lewa.util.StringUtils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import com.lewa.player.model.Song;
import com.lewa.player.model.SongCollection;

import android.content.Context;
import com.lewa.player.MusicUtils;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import java.util.HashMap;
import com.baidu.music.download.DownloadEntry;
import com.baidu.music.download.DownloadStatus;

import com.lewa.il.OnDownloadSongClickListener;
import com.lewa.il.MusicInterfaceLayer.OnDownloadProgressChangeListener;
import com.lewa.il.MusicInterfaceLayer.OnDownloadStatusChangeListener;
import com.lewa.player.model.MusicDownloadStatus;
import com.lewa.player.online.AppDownloadManager;
import com.lewa.util.Constants;
import com.lewa.player.model.PlayStatus;

import android.graphics.drawable.AnimationDrawable;








public class SongInfoAdapter extends ScanBaseAdapter implements SectionIndexer, OnDownloadProgressChangeListener,OnDownloadStatusChangeListener,OnDownloadSongClickListener{

    private static final boolean DEBUG = false;
    private static final String TAG = "SongInfoAdapter";
    private List<Song> mData = new ArrayList<Song>();
    private Cursor mArtistCurcor;
    private Artist mHeaderArtist;
    private View.OnClickListener mListener;
    private boolean mIsCursor = false;
    private ArtistCursorIndex mCursorIndex;
    private Context context = null;
    SongCollection songCollection = null;
    private Playlist mPlayList;
    private long lastUpdateTime;
    private static long MINUPDATEDIFF=1000;
    private HashMap<Long, Song> downloadSongs = new HashMap<Long, Song>();
    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Song song = (Song) v.getTag(R.id.tag_entity);
            if (song != null && song.getId() != null) {
                Lewa.downloadSong(song);
                v.setEnabled(false);
                v.setAlpha(0.5f);
            }
        }
    };

    public SongInfoAdapter(View.OnClickListener listener) {
        this.mListener = listener;
    }

    public SongInfoAdapter(Context context) {
        this.context = context;
    }

    @Override
    public int getCount() {
        
        if (mIsCursor) {
            return mArtistCurcor.getCount() + 1;
        }
        return this.mData != null ? mData.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public List<Song> getList() {
        return mData;
    }

    public List<Song> getData() {
        return mData;
    }

    public SongCollection getCollection() {
        return songCollection;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = Lewa.inflater().inflate(R.layout.fragment_song_info_item, null);
            viewHolder = new ViewHolder(convertView);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        Song song = (Song) mData.get(position);
       
        long playingSongId = -1;
        PlayStatus ps = Lewa.getPlayStatus();

        if (ps != null) {
            Song playingSong = ps.getPlayingSong();

            if (playingSong != null) {
                Long id = playingSong.getId();
                if (id != null) {
                    //TODO: use id and type to check equality
                    playingSongId = playingSong.getId().longValue();
                }
            }
        }
        

        if(Song.TYPE.LOCAL == song.getType() ) {//|| isDownloadList()) {
            viewHolder.downloadBtn.setVisibility(View.GONE);

            viewHolder.songDurationTv.setVisibility(View.VISIBLE);
        } else {
            viewHolder.downloadBtn.setVisibility(View.VISIBLE);
            viewHolder.downloadBtn.setTag(R.id.tag_entity, song);
            viewHolder.downloadBtn.setOnClickListener(mOnClickListener);
            viewHolder.songDurationTv.setVisibility(View.GONE);
            Song downloadingSong = Lewa.getDownloadingSong().get(song.getId());
            if(null == downloadingSong) {                    
                viewHolder.downloadBtn.setEnabled(true);
                viewHolder.downloadBtn.setAlpha(1.0f);               
            } else {    //song is downloading
                viewHolder.downloadBtn.setAlpha(0.5f);
                viewHolder.downloadBtn.setEnabled(false);    

            }
        }

        viewHolder.songNameTv.setText(song.getName());
        long songDuration = song.getDuration() == null ? 0 : song.getDuration() / 1000;
        String durTime = MusicUtils.makeTimeString(context, songDuration);
        viewHolder.songDurationTv.setText(durTime);
        String artistName = song.getArtist().getName();
		if (MediaStore.UNKNOWN_STRING.equals(artistName)) {
			viewHolder.songInfoTv.setText(context.getResources().getString(
					R.string.unknown_artist_name));
		} else {
			viewHolder.songInfoTv.setText(artistName);
		}

        convertView.setTag(R.id.tag_entity, song);
        convertView.setTag(viewHolder);
        

        if (song.getId() != null && song.getId() == playingSongId) {
            viewHolder.nowPlayingView.setVisibility(View.VISIBLE);
            //viewHolder.songNameTv.setTextAppearance(convertView.getContext(), R.style.TextView_Middle_Blue);
            if(null != ps) {
                AnimationDrawable anim = (AnimationDrawable) viewHolder.nowPlayingView.getBackground();  
                
                if(ps.isPlaying()) {
                    anim.start();  
                } else {
                    anim.stop();  
                }
            }
        } else {
            //viewHolder.nowPlayingView.setVisibility(View.GONE);
            viewHolder.nowPlayingView.setVisibility(View.INVISIBLE);
            viewHolder.songNameTv.setTextAppearance(convertView.getContext(), R.style.TextView_Middle_White);
        }

        return convertView;
    }

    /**
     * 根据ListView的当前位置获取分类的首字母的Char ascii值
     */
    public int getSectionForPosition(int position) {
        return mData.get(position).getInitial().charAt(0);
    }

    /**
     * 根据分类的首字母的Char ascii值获取其第一次出现该首字母的位置
     */
    public int getPositionForSection(int section) {
        for (int i = 0; i < getCount(); i++) {
            String sortStr = mData.get(i).getInitial();
            char firstChar = sortStr.toUpperCase().charAt(0);
            if (firstChar == section) {
                return i;
            }
        }

        return -1;
    }

    @Override
    public Object[] getSections() {
        return null;
    }

    public void setData(Cursor songCursor, Song artist) {
        /*mIsCursor = true;
        mArtistCurcor = artistCursor;
        if (artistCursor != null) {
            mCursorIndex = new ArtistCursorIndex(artistCursor);
        }

        mHeaderArtist = artist;*/
        notifyDataSetChanged();
    }

    public void setData(SongCollection songCollection) {
        if(null == songCollection) {
            return;
        }
        
        mIsCursor = false;
        this.songCollection =  songCollection;
        mData = songCollection.getSongs();
        
        if(SongCollection.Type.PLAYLIST == songCollection.getType()) {
            mPlayList = (Playlist)this.songCollection.getOwner();
            if(isDownloadList()) {
                
                downloadSongs.clear();
                for(int i = 0; i < mData.size(); i++) {
                    Song song  = mData.get(i);
                    if(song.getType() == Song.TYPE.ONLINE) {
                        downloadSongs.put(song.getId(), song);
                    }
                }
            }
        }
        
        notifyDataSetChanged();
    }

    private void showDownloadStatus(ViewHolder holder, Song song,
        MusicDownloadStatus downStatus) {
        /*int status=downStatus.getStatus();
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
        }*/
    }

    public void songDownloaded() {
        if(songCollection != null) {
            DBService.matchSongs(songCollection.getSongs());
            notifyDataSetChanged();
        }
    }

    public SongCollection getSongCollection() {
        return songCollection;
    }

    private boolean isDownloadList(){
        if(null != mPlayList) {
        	return mPlayList.getType() == Playlist.TYPE.DOWNLOAD;
        }
        return false;
    }

    public void onStatusChange(long musicId, int status) {
        updateDownStatus(musicId, status);
        if(DownloadStatus.STATUS_SUCCESS == status || DownloadStatus.STATUS_ALREADY_EXIST == status) {
            downloadSongs.remove(musicId);
        }
    }

    public void updateDownStatus(long musicId, int status) {
        Song song=downloadSongs.get(musicId);
        if(song!=null){
            int position=mData.indexOf(song);
            MusicDownloadStatus downStatus=new MusicDownloadStatus();
            downStatus.setStatus(status);
            song.setDownStatus(downStatus);
            if(position< mData.size()){
                mData.set(position, song);
                downloadSongs.put(musicId, song);
                updateProgress(true);
            }else{
            	Log.i(TAG,"POSITION ERROR");
            }
        }
    }

    private String handleErrorMsg(long musicId, int status) {
        // TODO Auto-generated method stub
        Song song=downloadSongs.get(musicId);
        String msg=Lewa.context().getString(R.string.wait_for_download);

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
                songCollection.setSongs(mData);
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
        if(isDownloadList() && song.getType() == Song.TYPE.ONLINE){
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

    static class ViewHolder {
        TextView songNameTv;
        TextView songDurationTv;
        TextView songInfoTv;
        ImageButton downloadBtn;
        ImageView nowPlayingView;

        public ViewHolder(View view) {
            nowPlayingView = (ImageView) view.findViewById(R.id.iv_now_playing_icon);
            downloadBtn = (ImageButton) view.findViewById(R.id.bt_download);
            songNameTv = (TextView) view.findViewById(R.id.tv_title);
            songDurationTv = (TextView) view.findViewById(R.id.tv_duration);
            songInfoTv = (TextView) view.findViewById(R.id.tv_info);
        }
    }

}
