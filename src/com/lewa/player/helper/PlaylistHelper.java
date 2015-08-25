package com.lewa.player.helper;

import android.app.Activity;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ListView;

import com.baidu.music.model.Music;
import com.baidu.music.onlinedata.TopListManager;
import com.lewa.Lewa;
import com.lewa.il.MusicInterfaceLayer;
import com.lewa.player.IMediaPlaybackService;
import com.lewa.player.adapter.PlayingSongAdapter;
import com.lewa.player.db.DBService;
import com.lewa.player.model.Album;
import com.lewa.player.model.Artist;
import com.lewa.player.model.Pagination;
import com.lewa.player.model.PlayStatus;
import com.lewa.player.model.Playlist;
import com.lewa.player.model.PlaylistSong;
import com.lewa.player.model.Song;
import com.lewa.util.Constants;
import com.lewa.util.LewaUtils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by wuzixiu on 1/10/14.
 */
public class PlaylistHelper {
    private static final String TAG = "PlaylistHelper";//.class.getName();

    public static class DefaultOnGetHotAlbumSongsListener implements MusicInterfaceLayer.OnGetTopicListener {
        private Playlist playlist;
        private GetSongsListener getSongsListener;

        public DefaultOnGetHotAlbumSongsListener(Playlist playlist, GetSongsListener getSongsListener) {
            this.playlist = playlist;
            this.getSongsListener = getSongsListener;
        }
		@Override
		public void onGetTopic(List<Music> lists) {
			// TODO Auto-generated method stub
		     onGetOnlineSongs(playlist, getSongsListener, lists);
		}
    }

    public static void getOnlinePlaylistSongs(final Activity activity, final Playlist playlist, final GetSongsListener getSongsListener) {
        if (playlist == null) return;
        Log.i(TAG, "Get songs of online playlist: " + playlist.getBdCode());
        MusicInterfaceLayer.getInstance().requestHotAlbumSongs(activity, playlist.getBdCode(), new DefaultOnGetHotAlbumSongsListener(playlist, getSongsListener));
    }

    public static void getAllStarSongs(final Activity activity, final Playlist playlist, final GetSongsListener getSongsListener) {
        if (playlist == null) return;
        Log.i(TAG, "Get songs of all star: " + playlist.getBdCode());
        Artist artist = playlist.getArtist();
        MusicInterfaceLayer.getInstance().requestHotSingerSongs(activity, Pagination.DEFAULT_PAGE_SIZE, String.valueOf(artist.getId()), 1, new MusicInterfaceLayer.OnGetArtistMusicListListener() {
			
			@Override
			public void onGetArtistMusicList(List<Music> musics) {
				// TODO Auto-generated method stub
			    onGetOnlineSongs(playlist, getSongsListener, musics);
			}
		});
    }

    public static void getFMSongs(final Activity activity, final Playlist playlist, final GetSongsListener getSongsListener) {
        if (playlist == null) return;
        Log.i(TAG, "Get songs of FM: " + playlist.getBdCode());
        Pagination page = new Pagination();
        MusicInterfaceLayer.getInstance().requestRadioPublicSongList(activity, playlist.getId(), page.getPageSize(), page.pageNo, new MusicInterfaceLayer.OnGetRadioSongListListener() {
			@Override
			public void onGetRadioSongList(List<Music> lists) {
				// TODO Auto-generated method stub
				onGetOnlineSongs(playlist, getSongsListener, lists);
			}
        });
    }

    public static void getTopListNewSongs(final Activity activity, final Playlist playlist, final GetSongsListener getSongsListener) {
        if (playlist == null) return;
        Log.i(TAG, "Get songs of top list new: " + playlist.getBdCode());
        MusicInterfaceLayer.getInstance().requestForNewsongs(activity, Pagination.DEFAULT_PAGE_SIZE, new MusicInterfaceLayer.OnGetNewSongListener() {
            @Override
            public void onGetNewSong(List<Music> newSongs) {
                onGetOnlineSongs(playlist, getSongsListener, newSongs);
            }
        });
    }

    public static void getTopListHotSongs(final Activity activity, final Playlist playlist, final GetSongsListener getSongsListener) {
        if (playlist == null) return;
        Log.i(TAG, "Get songs of top list hot: " + playlist.getBdCode());
        MusicInterfaceLayer.getInstance().requestForHotsongs(activity, TopListManager.EXTRA_TYPE_HOT_SONGS, Pagination.DEFAULT_PAGE_SIZE, new MusicInterfaceLayer.OnGetHotSongListener() {
            @Override
            public void onGetHotSong(List<Music> hotSongs) {
               onGetOnlineSongs(playlist, getSongsListener, hotSongs);
            }
        });
    }

    public interface GetSongsListener {
        public void onGotSongs(Playlist playlist);
    }

    public static List<Song> getPlayingSongs() {
        List<Song> songs = new ArrayList<Song>();
        IMediaPlaybackService playbackService = Lewa.playerServiceConnector().service();

        long[] playingSongIds = null;
        if (playbackService != null) {
            try {
                playingSongIds = playbackService.getQueue();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        Log.d(TAG, "Song id in playing list: ");
        if (playingSongIds != null && playingSongIds.length > 0) {
            List<Long> localIds = new ArrayList<Long>();
            try {
                for (int i = 0; i < playingSongIds.length; i++) {
                    long id = playingSongIds[i];
                    Log.d(TAG, "" + id);
                    if (id > 0) {
                        localIds.add(id);
                    }
                }

                List<Song> localSongs = DBService.findSongsByIds(localIds);
                Map<Long, Song> localSongsMap = new HashMap<Long, Song>();
                for (Song localSong : localSongs) {
                    localSongsMap.put(localSong.getId(), localSong);
                }

                for (long id : playingSongIds) {
                    if (id > 0) {
                        songs.add(localSongsMap.get(id));
                    } else {
                        songs.add(Lewa.getPlayingSong(Math.abs(id)));
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return songs;
    }

    public static void scrollToPlayingSong(ListView lv, PlayingSongAdapter adapter) {
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

        int position = 0;
        List<Song> songs = adapter.getList();

        if (songs == null) return;

        if (playingSongId >= 0 && songs != null) {
            for (int i = 0; i < songs.size(); i++) {
                Song song = songs.get(i);
                if (song != null && song.getId() != null && song.getId() == playingSongId) {
                    position = i;
                    break;
                }
            }
        }

        if (position > 0 && position <=3) {
            lv.setSelection(0);
        } else {
            lv.setSelection(position - 3);
        }
    }

	private static void onGetOnlineSongs(final Playlist playlist,
			final GetSongsListener getSongsListener, List<Music> lists) {
			try {
				if (lists != null) {
				    List<PlaylistSong> playlistSongs = new ArrayList<PlaylistSong>();
				    for (Music itemData : lists) {
				        PlaylistSong playlistSong = new PlaylistSong();
				        playlistSong.setSongType(Song.TYPE.ONLINE);
				        Song song = songFromMusic(playlist, itemData);
				        playlistSong.setSong(song);
				        playlistSongs.add(playlistSong);
				    }
				    playlist.setSongs(playlistSongs);
				}
				getSongsListener.onGotSongs(playlist);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}

	public static Song songFromMusic(final Playlist playlist, Music itemData) {
		Song song = new Song();
		song.setType(Song.TYPE.ONLINE);
		song.setOnlineId(itemData.mId);
		song.setId(Long.valueOf(itemData.mId));
		song.setName(itemData.mTitle);
//                     ------ bitrates 320 is need to buy ,and bitrates 256 need baidu account , so we set default 128 --------		        
//				        if(itemData.bitrates!=null){
//					        int size=itemData.bitrates.size();
//					        if(size>0){
//					        	song.setBitrate(itemData.bitrates.get(size-1));
//					        	LewaUtils.logE(TAG, itemData.mId+" : the max bitrates is "+itemData.bitrates.get(size-1));
//					        }else{
//					        	LewaUtils.logE(TAG, itemData.mId+" : itemData.bitrates size is 0");
//					        	song.setBitrate(Constants.DEFAULT_BITRATES);
//					        }
//				        }else{
//				        	LewaUtils.logE(TAG, itemData.mId+" : itemData.bitrates is null");
//				        	song.setBitrate(Constants.DEFAULT_BITRATES);
//				        }
		song.setBitrate(Constants.DEFAULT_BITRATES);
		song.setLossless(false);
		Artist artist = new Artist();
		try {
			if(itemData.mArtistId!=null&&!itemData.mArtistId.equalsIgnoreCase("null")){
				artist.setId(Long.valueOf(itemData.mArtistId));
			}else if(playlist!=null){
				artist.setId(playlist.getArtist().getId());
			}
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		artist.setName(itemData.mArtist);
		song.setArtist(artist);
		song.setCoverUrl(itemData.mPicSmall);
		song.setBigCoverUrl(itemData.mPicBig);
		if(!TextUtils.isEmpty(itemData.mAlbumTitle)){
			Album album=new Album();
			album.setName(itemData.mAlbumTitle);
			song.setAlbum(album);
		}
		return song;
	}
}
