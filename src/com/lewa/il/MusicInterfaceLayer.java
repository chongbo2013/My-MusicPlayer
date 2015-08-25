package com.lewa.il;

import android.content.Context;
import android.content.Intent;
import android.os.RemoteException;

import com.baidu.music.image.component.ImageManager2;
import com.baidu.music.manager.Image;
import com.baidu.music.manager.ImageManager;
import com.baidu.music.manager.ImageManager.IImageLoadCallback;
import com.baidu.music.model.AlbumList;
import com.baidu.music.model.Artist;
import com.baidu.music.model.ArtistList;
import com.baidu.music.model.Channel;
import com.baidu.music.model.LrcPic;
import com.baidu.music.model.LrcPicList;
import com.baidu.music.model.Music;
import com.baidu.music.model.MusicList;
import com.baidu.music.model.Radio;
import com.baidu.music.model.RadioList;
import com.baidu.music.model.SearchResult;
import com.baidu.music.model.SearchSuggestion;
import com.baidu.music.model.TopListDescriptionList;
import com.baidu.music.model.Topic;
import com.baidu.music.model.TopicList;
import com.baidu.music.onlinedata.ArtistManager.ArtistListener;
import com.baidu.music.onlinedata.FreshMusicManager;
import com.baidu.music.onlinedata.FreshMusicManager.FreshMusicListener;
import com.baidu.music.onlinedata.ArtistManager;
import com.baidu.music.onlinedata.LyricManager;
import com.baidu.music.onlinedata.LyricManager.LyricDownloadListener;
import com.baidu.music.onlinedata.OnlineManagerEngine;
import com.baidu.music.onlinedata.RadioManager;
import com.baidu.music.onlinedata.RadioManager.RadioListener;
import com.baidu.music.onlinedata.SearchManager;
import com.baidu.music.onlinedata.SearchManager.LrcPicSearchListener;
import com.baidu.music.onlinedata.SearchManager.SearchListener;
import com.baidu.music.onlinedata.TopListManager;
import com.baidu.music.onlinedata.TopListManager.TopListListener;
import com.baidu.music.onlinedata.TopicManager;
import com.baidu.music.onlinedata.TopicManager.TopicListener;
import com.lewa.Lewa;
import com.lewa.player.IMediaPlaybackService;
import com.lewa.player.online.OnlineLoader;
import com.lewa.util.Constants;
import com.lewa.util.LewaUtils;

import java.util.List;

public class MusicInterfaceLayer {
	private static final String TAG = "MusicInterfaceLayer";
    public int new_songs_limit = 25;
    public int hot_songs_limit = 25;

    private static MusicInterfaceLayer mInstance;

    private boolean isRequestingForNewsongs = false;

    private boolean isRequestingForHotsongs = false;

    private boolean isRequestAlbums = false;
    private boolean isRequestAlbumSongs = false;
    private boolean isRequestArtists = false;
    private boolean isRequestArtistSongs = false;
    private boolean isRequestArtistAlbums = false;

    private boolean isRequestRadioList = false;
    private boolean isRequestPulicRadioSongList = false;
    private boolean isRequestSingerRadioSongList = false;
    private boolean isSearchSong=false;
    private boolean isGetSuggestion=false;
    
    //new interface begin
    private CustomFreshMusicListener customFreshMusicListener;
    private CustomTopListListener customTopListListener;
    private CustomTopicListener hotAlbumlistener;
    private CustomTopicListener hotAlbumSongslistener;
    private CustomArtistListener hotArtistListListener;
    private CustomArtistListener artistMusicListListener;
    private CustomRadioListener radioListListener;
    private CustomRadioListener radioPublicSongListListener;
    //new interface end
    public static final String ARTIST_PATH = "/LEWA/music/artist/";
    public static final String ALBUM_PATH = "/LEWA/music/album/";
    public static final String SRC_PATH="/LEWA/music/lrc/";
    //new interface begin
    public interface OnGetNewSongListener {
        void onGetNewSong(List<Music> newSongs);
    }

    public interface OnGetHotSongListener {
        void onGetHotSong(List<Music> hotSongs);
    }
    public interface OnGetTopicListListener{
    	void onGetTopicList(List<Topic> topics);
    }
    
    public interface OnGetTopicListener{
    	void onGetTopic(List<Music> lists);
    }
    
    public interface OnGetHotArtistListListener{
    	void onGetHotArtistList(List<Artist> artists);
    }
    
    public interface OnGetArtistMusicListListener{
    	void onGetArtistMusicList(List<Music> musics);
    }
    
    public interface OnGetRadioListListener {
        void onGetRadioList(List<Radio> lists);
    }
    
    public interface OnGetRadioSongListListener {
        void onGetRadioSongList(List<Music> lists);
    }
    
    public interface OnDownloadProgressChangeListener{
    	void onProgressChange(long musicId,long mCurrentBytes,long mTotalBytes,int status);
    }
    
    public interface OnDownloadStatusChangeListener{
    	void onStatusChange(long musicId,int status);
    }
    
    public interface OnSearchMusicListener{
    	void onSearchMusic(List<Music> lists);
    }
    
    public interface OnGetSuggestionListener{
    	void onGetSuggestion(List<String> datas);
    }
    
    public enum DownloadType{
    	LYRIC,ARTIST,ALBUM
    }
    //new interface end


    public static MusicInterfaceLayer getInstance() {
        if (mInstance == null) {
            mInstance = new MusicInterfaceLayer();
        }

        return mInstance;
    }

    /**
     * Param:
     * limit - request item size limit.max:100,
     */
    public synchronized void requestForNewsongs(Context context, int limit, OnGetNewSongListener onGetNewSonglistener) {
        if (context == null) {
            return;
        }
        FreshMusicManager freshMusicManager=OnlineManagerEngine.getInstance(context).getFreshMusicManager(context);
        customFreshMusicListener=new CustomFreshMusicListener(context, onGetNewSonglistener);
        if(!isRequestingForNewsongs){
        	freshMusicManager.getFreshMusicListAsync(context, limit,customFreshMusicListener);
        	isRequestingForNewsongs = true;
        }
    }

    /*
     * Type - 榜单的具体类型
     *       OnlineTopListDataManager.EXTRA_TYPE_NEW_SONGS or OnlineTopListDataManager.EXTRA_TYPE_HOT_SONGS
     * pageNo - 请求列表的页码
     * pageSize - 请求的每页所含条目数目。注意：允许最大为100，否则都按100返回
     */
    public synchronized void requestForHotsongs(Context context, String type, int requestNum,
                                                OnGetHotSongListener onGetHotSonglistener) {
        if (context == null) {
            return;
        }
        TopListManager topListManager=OnlineManagerEngine.getInstance(context).getTopListManager(context);
        customTopListListener=new CustomTopListListener(context, onGetHotSonglistener);
        if(!isRequestingForHotsongs){
        	topListManager.getTopListAsync(context, type, 1, requestNum,customTopListListener);
        	isRequestingForHotsongs = true;
        }
    }

    public void requestHotAlbum(Context context, int num, OnGetTopicListListener onGetTopicListListener) {
        if (context == null) {
            return;
        }

        TopicManager topicManager=OnlineManagerEngine.getInstance(context).getTopicManager(context);
        hotAlbumlistener=new CustomTopicListener(context, onGetTopicListListener);
        if(!isRequestAlbums){
        	topicManager.getTopicListAsync(context, 1, num, hotAlbumlistener);
        	isRequestAlbums = true;
        }
    }

    public void requestHotAlbumSongs(Context context, String albumCodeName, OnGetTopicListener onGetTopicListener) {
        if (context == null) {
            return;
        }
        
        TopicManager topicManager=OnlineManagerEngine.getInstance(context).getTopicManager(context);
        hotAlbumSongslistener=new CustomTopicListener(context, onGetTopicListener);
        if(!isRequestAlbumSongs){
        	topicManager.getTopicAsync(context, albumCodeName,hotAlbumSongslistener);
        	isRequestAlbumSongs = true;
        }
    }

    public void requestHotSinger(Context context, int requestNum, OnGetHotArtistListListener onGetHotArtistListListener) {
        if (context == null) {
            return;
        }

        ArtistManager artistManager=OnlineManagerEngine.getInstance(context).getArtistManager(context);
        hotArtistListListener=new CustomArtistListener(context, onGetHotArtistListListener);
        if(!isRequestArtists){
        	artistManager.getHotArtistListAsync(context, 1, requestNum,hotArtistListListener);
        	isRequestArtists = true;
        }
    }

    public void requestHotSingerSongs(Context context, int Num, String singerId, int songspageNo,
                                      OnGetArtistMusicListListener onGetArtistMusicListListener) {
        if (!OnlineLoader.isWiFiActive(context) && !OnlineLoader.IsConnection(context)) {
            return;
        }
        ArtistManager artistManager=OnlineManagerEngine.getInstance(context).getArtistManager(context);
        artistMusicListListener=new CustomArtistListener(context, onGetArtistMusicListListener);
        if(!isRequestArtistSongs){
        	artistManager.getArtistMusicListAsync(context,Integer.parseInt(singerId), songspageNo, Num, artistMusicListListener);
        	isRequestArtistSongs=true;
        }
    }


    public void requestRadioList(Context context, OnGetRadioListListener onGetRadioListListener) {
//        /**
//         * !!important
//         * overwrite the old listener on each request, or response will be dispatched to the destroied activity or fragment
//         */
    	RadioManager radioManager=OnlineManagerEngine.getInstance(context).getRadioManager(context);
    	radioListListener=new CustomRadioListener(context, onGetRadioListListener);
    	if(!isRequestRadioList){
    		radioManager.getRadioListAsync(radioListListener);
    		isRequestRadioList = true;
    	}
    }

    public void requestRadioPublicSongList(Context context, long channelId, int pageSize, int pageNo,
                                           OnGetRadioSongListListener onGetRadioSongListListener) {
    	RadioManager radioManager=OnlineManagerEngine.getInstance(context).getRadioManager(context);
    	radioPublicSongListListener=new CustomRadioListener(context, onGetRadioSongListListener);
    	if(!isRequestPulicRadioSongList){
    		radioManager.getPublicChannelAsync(channelId, pageSize, pageNo,radioPublicSongListListener);
    		isRequestPulicRadioSongList=true;
    	}
    }
    
    public void downLoadLrcPicAsync(Context context,String title,String artist,DownloadType type){
    	switch (type) {
    	case ALBUM:
		case ARTIST:
			SearchManager searchManager=OnlineManagerEngine.getInstance(context).getSearchManager(context);
			searchManager.getLyricPicAsync(context, title, artist, new CustomLrcPicSearchListener(context,type));
			break;
		case LYRIC:	
			downLrcAsync(context, title, artist);
			break;
		}
    }
    
    
    public void searchMusicAsync(Context context,String key,int pageNo,int pageSize,OnSearchMusicListener onSearchMusicListener){
    	LewaUtils.logE(TAG, "searchMusicAsync");
    	SearchManager searchManager=OnlineManagerEngine.getInstance(context).getSearchManager(context);
    	if(!isSearchSong){
    		searchManager.searchMusicAsync(key, pageNo, pageSize, new CustomSearchListener(onSearchMusicListener));
    		isSearchSong=true;
    	}
    }
    
    public void getSuggestionAsync(Context context,String key,OnGetSuggestionListener onGetSuggestionListener){
    	LewaUtils.logE(TAG, "getSuggestionAsync");
    	SearchManager searchManager=OnlineManagerEngine.getInstance(context).getSearchManager(context);
    	if(!isGetSuggestion){
    		searchManager.getSearchSuggestionAsync(key, new CustomSearchListener(onGetSuggestionListener));
    		isGetSuggestion=true;
    	}
    }
    
    //new interface begin
    private class CustomFreshMusicListener implements FreshMusicListener{
    	private OnGetNewSongListener onGetNewSongListener;
    	public CustomFreshMusicListener(Context context,OnGetNewSongListener onGetNewSongListener){
    		this.onGetNewSongListener=onGetNewSongListener;
    	}
    	
		@Override
		public void onGetFreshMusic(MusicList musicList) {
			// TODO Auto-generated method stub
			if(musicList!=null&&musicList.mItems!=null){
				List<Music>  lists=musicList.mItems;
				if(onGetNewSongListener!=null)
					onGetNewSongListener.onGetNewSong(lists);
			}else{
				if(onGetNewSongListener!=null)
					onGetNewSongListener.onGetNewSong(null);
			}
			isRequestingForNewsongs=false;
		}
    }
    
    private class CustomTopListListener implements TopListListener{
    	private OnGetHotSongListener onGetHotSongListener;
    	public CustomTopListListener(Context context,OnGetHotSongListener onGetHotSongListener){
    		this.onGetHotSongListener=onGetHotSongListener;
    	}
		@Override
		public void onGetDescriptionList(TopListDescriptionList description) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onGetTopList(MusicList musicList) {
			// TODO Auto-generated method stub
			if(musicList!=null&&musicList.mItems!=null){
				 List<Music> lists=musicList.mItems;
				 if(onGetHotSongListener!=null)
					onGetHotSongListener.onGetHotSong(lists);
			}else{
				if(onGetHotSongListener!=null)
					onGetHotSongListener.onGetHotSong(null);
			}
            isRequestingForHotsongs = false;
		}
    }
    
    
    private class CustomTopicListener implements TopicListener{
    	private OnGetTopicListListener onGetTopicListListener;
    	private OnGetTopicListener onGetTopicListener;
    	public CustomTopicListener(Context context,OnGetTopicListListener onGetTopicListListener){
    		this.onGetTopicListListener=onGetTopicListListener;
    	}
    	public CustomTopicListener(Context context,OnGetTopicListener onGetTopicListener){
    		this.onGetTopicListener=onGetTopicListener;
    	}
		@Override
		public void onGetTopic(Topic topic) {
			// TODO Auto-generated method stub
			if(topic!=null&&topic.mItems!=null){
				List<Music> lists=topic.mItems;
				if(onGetTopicListener!=null)
					onGetTopicListener.onGetTopic(lists);
			}else{
				if(onGetTopicListener!=null)
					onGetTopicListener.onGetTopic(null);
			}
			isRequestAlbumSongs=false;
		}

		@Override
		public void onGetTopicList(TopicList topicList) {
			// TODO Auto-generated method stub
			if(topicList!=null&&topicList.mItems!=null){
				List<Topic> topics=topicList.mItems;
				if(onGetTopicListListener!=null)
					onGetTopicListListener.onGetTopicList(topics);
			}else{
				if(onGetTopicListListener!=null)
					onGetTopicListListener.onGetTopicList(null);
			}
			isRequestAlbums=false;
		}
    	
    }
    
    private class CustomArtistListener implements ArtistListener{
    	private OnGetHotArtistListListener onGetHotArtistListListener;
    	private OnGetArtistMusicListListener onGetArtistMusicListListener;
    	public CustomArtistListener(Context context,OnGetHotArtistListListener onGetHotArtistListListener){
    		this.onGetHotArtistListListener=onGetHotArtistListListener;
    	}
    	
    	public CustomArtistListener(Context context,OnGetArtistMusicListListener onGetArtistMusicListListener){
    		this.onGetArtistMusicListListener=onGetArtistMusicListListener;
    	}
		@Override
		public void onGetArtist(Artist artist) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onGetArtistAlbumList(AlbumList albumList) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onGetArtistMusicList(MusicList musicList) {
			// TODO Auto-generated method stub
			if(musicList!=null&&musicList.mItems!=null){
				List<Music> musics=musicList.mItems;
				if(onGetArtistMusicListListener!=null)
					onGetArtistMusicListListener.onGetArtistMusicList(musics);
			}else{
				if(onGetArtistMusicListListener!=null)
					onGetArtistMusicListListener.onGetArtistMusicList(null);
			}
			isRequestArtistSongs=false;
		}

		@Override
		public void onGetHotArtistList(ArtistList artistList) {
			// TODO Auto-generated method stub
			if(artistList!=null&&artistList.mItems!=null){
				List<Artist> artists=artistList.mItems;
				if(onGetHotArtistListListener!=null)
					onGetHotArtistListListener.onGetHotArtistList(artists);
			}else{
				if(onGetHotArtistListListener!=null)
					onGetHotArtistListListener.onGetHotArtistList(null);
			}
			isRequestArtists=false;
		}
    	
    }
    
    private class CustomRadioListener implements RadioListener{
		private OnGetRadioListListener onGetRadioListListener;
    	private OnGetRadioSongListListener onGetRadioSongListListener;
    	public CustomRadioListener(Context context,OnGetRadioListListener onGetRadioListListener){
    		this.onGetRadioListListener=onGetRadioListListener;
    	}
    	
    	public CustomRadioListener(Context context,OnGetRadioSongListListener onGetRadioSongListListener){
    		this.onGetRadioSongListListener=onGetRadioSongListListener;
    	}
		@Override
		public void onGetArtistChannel(Channel channel) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onGetPublicChannel(Channel channel) {
			// TODO Auto-generated method stub
			if(channel!=null&&channel.mItems!=null){
				List<Music> musics=channel.mItems;
				if(onGetRadioSongListListener!=null)
					onGetRadioSongListListener.onGetRadioSongList(musics);
			}else{
				if(onGetRadioSongListListener!=null)
					onGetRadioSongListListener.onGetRadioSongList(null);
			}
			isRequestPulicRadioSongList=false;
		}

		@Override
		public void onGetRadioList(RadioList radioList) {
			// TODO Auto-generated method stub
			if(radioList!=null&&radioList.mItems!=null){
				List<Radio> radios=radioList.mItems;
				if(onGetRadioListListener!=null)
					onGetRadioListListener.onGetRadioList(radios);
			}else{
				if(onGetRadioListListener!=null)
					onGetRadioListListener.onGetRadioList(null);
			}
			isRequestRadioList=false;
		}
    	
    }
    
    private class CustomLrcPicSearchListener implements LrcPicSearchListener{
    	private DownloadType type;
    	private Context mContext;
    	public CustomLrcPicSearchListener(Context context,DownloadType type){
    		this.type=type;
    		this.mContext=context;
    	}
		@Override
		public void onGetLrcPicList(LrcPicList lrcPicList) {
			// TODO Auto-generated method stub
			if(lrcPicList!=null&&lrcPicList.getItems()!=null){
				List<LrcPic> lrcPics=lrcPicList.getItems();
				if(lrcPics.size()<=0)
					return;
				LrcPic lrcPic=lrcPics.get(0);
				switch (type) {
				case ARTIST:
					downPic(mContext,lrcPic.getAvatarBig(),lrcPic.getAuthor(),type);
					break;
				case ALBUM:
					break;
				}
			}
		}
    	
    }
    public void downPic(Context context,String url,String name,DownloadType type) {
    	// TODO Auto-generated method stub
    	DownPicCallback downPicCallback=new DownPicCallback(context, type, name);
    	ImageManager.request(url, downPicCallback, ARTIST_PIC_WIDTH, ARTIST_PIC_HEIGHT, 0, false, false);
    }
    
    public void downLrcAsync(Context context,String title,String artist){
    	LyricManager lyricManager=OnlineManagerEngine.getInstance(context).getLyricManager(context);
    	try{
    		lyricManager.getLyricFileAsync(title, artist, LewaUtils.getExternalPath(Constants.SRC_PATH),new CustomLyricDownloadListener(context,title));
    	} catch (Exception e) {
    		// Rui Wei added this try catch, for evoque bug id: 981848, log id: 1261077
    	}
    }
    
    private class CustomLyricDownloadListener implements LyricDownloadListener{
    	private String srcTitle;
    	private Context mContext;
    	public CustomLyricDownloadListener(Context context,String srcTitle){
    		this.srcTitle=srcTitle;
    		this.mContext=context;
    	}
		@Override
		public void onDownloaded(int status, String lyricPath, String title, String artist) {
			// TODO Auto-generated method stub
			OnlineLoader.SendtoUpdateLRC(mContext, srcTitle, artist, status);
		}
    	
    }
    
    private class DownPicCallback implements IImageLoadCallback{
    	private DownloadType type;
    	private String name;
    	private String path;
    	private Context mContext;
    	public DownPicCallback(Context context,DownloadType type,String name){
    		this.type=type;
    		this.name=name;
    		this.mContext=context;
    	}
		@Override
		public void onLoad(String url, Image image) {
			// TODO Auto-generated method stub
			switch (type) {
			case ARTIST:
				if(name!=null&&image!=null){
					path=LewaUtils.getArtistPicPath(name);
					image.save(path);
				}
				try {
                    IMediaPlaybackService service = Lewa.playerServiceConnector().service();
                    if (service != null && service.getArtistName() != null && !service.getArtistName().contains(name)) {
                        return;
                    }
                } catch (RemoteException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
				if (url == null || image == null) {
                    OnlineLoader.SendtoUpdate(null, name);
                } else {
                    OnlineLoader.SendtoUpdate(path, name);
                    Intent intent = new Intent("com.lewa.player.updateDownloadImage");
                    mContext.sendBroadcast(intent);
                }
				break;
			case ALBUM:
				
				break;
			}
		}
    	
    }
    
    private class CustomSearchListener implements SearchListener{
    	private OnSearchMusicListener onSearchMusicListener;
    	private OnGetSuggestionListener onGetSuggestionListener;
    	public CustomSearchListener(OnSearchMusicListener onSearchMusicListener){
    		this.onSearchMusicListener=onSearchMusicListener;
    	}
    	
    	public CustomSearchListener(OnGetSuggestionListener onGetSuggestionListener){
    		this.onGetSuggestionListener=onGetSuggestionListener;
    	}
    	
		@Override
		public void onGetSearchSuggestion(SearchSuggestion suggestion) {
			// TODO Auto-generated method stub
			if(suggestion!=null&&suggestion.mItems!=null){
				List<String> datas = suggestion.mItems;
				if(onGetSuggestionListener!=null)
					onGetSuggestionListener.onGetSuggestion(datas);
			}else{
				LewaUtils.logE(TAG, "onGetSearchSuggestion suggestion is null");
				if(onGetSuggestionListener!=null)
					onGetSuggestionListener.onGetSuggestion(null);
			}
			isGetSuggestion = false;
		}

		@Override
		public void onSearchAlbumPicture(String url) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onSearchArtistAvatar(String url) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onSearchLyric(String url) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onSearchMusic(SearchResult result) {
			// TODO Auto-generated method stub
			if(result!=null&&result.mItems!=null){
				List<Music> lists=result.mItems;
				if(onSearchMusicListener!=null)
					onSearchMusicListener.onSearchMusic(lists);
			}else{
				LewaUtils.logE(TAG, "onSearchMusic result is null");
				if(onSearchMusicListener!=null)
					onSearchMusicListener.onSearchMusic(null);
			}
			isSearchSong=false;
		}
    	
    }
    private static final int ARTIST_PIC_WIDTH =256;
    private static final int ARTIST_PIC_HEIGHT =256;
    //new interface end

}

