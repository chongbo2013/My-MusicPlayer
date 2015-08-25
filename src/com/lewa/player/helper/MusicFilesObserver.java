package com.lewa.player.helper;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.FileObserver;
import android.util.Log;

import com.lewa.player.MusicUtils;
import com.lewa.player.db.DBService;
import com.lewa.player.model.Song;
import com.lewa.Lewa;
import com.lewa.player.model.SongCollection;




/**
 * time: 2014/11/12
 * author: sjxu
 * purpose: monitor musice file change!
 */

public class MusicFilesObserver {
        private static final String TAG = "MusicFilesObserver";
	
	private static MusicFilesObserver mMusicFilesObserver = null;
	
	public static MusicFilesObserver getInstance(Context context) {
		if(null != mMusicFilesObserver) {
			return mMusicFilesObserver;
		}
		
		synchronized(MusicFilesObserver.class) {
			if(null == mMusicFilesObserver) {
				mMusicFilesObserver = new MusicFilesObserver(context);				
			}
		}
		return mMusicFilesObserver;
	}
	
	private Context context = null;
	private ArrayList<MusicFileObserver> musicFileObserverList = null;
	
	private MusicFilesObserver(Context context) {
		this.context = context;	
		
		initDirectoryObserver();
		startMonitorStorageState();
	}
	
	private boolean isStorageMounted() {
		if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			return true;
		}
		return false;
	}
	
	private void initDirectoryObserver() {
             if(!isStorageMounted()) {
                 return;
             }
             
		if(null != musicFileObserverList) {
			musicFileObserverList.clear();
			musicFileObserverList = null;
		}
		musicFileObserverList = new ArrayList<MusicFileObserver>();
		
		{// add observer of music directory that need watch from database
			String[] pathArray = MusicUtils.getFolderPath(context);
                    if(pathArray == null || pathArray.length == 0) {
                        ArrayList<String> pathList = MusicUtils.getPathList(context);
                        if(pathList == null || pathList.size() == 0) {
                            return;
                        }
                        pathArray = (String[]) pathList.toArray(new String[pathList.size()]);
                    }

                    for(String path : pathArray) {
                        MusicFileObserver observer = new MusicFileObserver(path);
                        if(null != observer) {
                            musicFileObserverList.add(observer);
                        }
                    }
                     
		}
	}
	
	public void startWatching() {
		if(!isStorageMounted()) {	//sd is unmounted
			return;
		}
		
		for(MusicFileObserver musicFileObserver: musicFileObserverList) {
			musicFileObserver.startWatching();
		}		
	}
	
	public void stopWatching() {
		for(MusicFileObserver musicFileObserver: musicFileObserverList) {
			musicFileObserver.stopWatching();
		}
	}
	
	public void restartWatching() {
		stopWatching();
		initDirectoryObserver();
		startWatching();
	}
	
	public synchronized void  updateMusicFile(String path) {	//watching file is change we need response
		if(!isStorageMounted()) {
			return;
		}

             if(null == path || 0 == path.length()) {
                Log.i(TAG, "Invalid path = " + path);
                return;
             }
		Log.i(TAG, "updateMusicFile path = " + path);

            /*Song song = DBService.findSongByPath(path);//.findSongsByName(path);//DBService.findSongByPath(path);
            if(null == song) {
                song = DBService.findSongByPathFromMediaStore(path);
            }

            //if(null == song) {
                //List<Song> songList = Lewa.getPlayingCollection().getSongs();
                //if(null != songList) {
                    //for(Song s : songList) {
                        //if(path.e
                    //}
                //}
            //}
            Log.i(TAG, "song = " + song);
            if(null != song) {
                try {
                    
                    //Log.i(TAG, "song = " + song);
                    SongCollection songCollection = Lewa.getPlayingCollection();
                    if(null != songCollection) {
                        List<Song> songList = songCollection.getSongs();
                        if(null != songList) {
                            for(Song tmpSong : songList) {
                                //Log.i(TAG, "tmpSOng = " + tmpSong);
                                if(tmpSong.getName().equals(song.getName())) {
                                    song = tmpSong;
                                }
                            }
                        }
                    }
                    DBService.removeSongFromMediaStore(song);   //this song is alread del , this is not useful

                    DBService.removeSongFile(song);
                    ArrayList<Song> toBeRemovedPlayingSongs = new ArrayList<Song>();
                    if(song.getId() == 0 && song.getLocalId()  != 0) {
                        song.setId(song.getLocalId());  //because this song is del, song id is online id ,so we chang it for del it from playing list
                    }
                    Log.i(TAG, "remove song = " + song);
                    toBeRemovedPlayingSongs.add(song);
                    Lewa.playerServiceConnector().removeSongs(toBeRemovedPlayingSongs);
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.i(TAG, "e = " + e);
                }
            }*/

            try {
                Song song = null;
                SongCollection songCollection = Lewa.getPlayingCollection();
                if(null != songCollection) {
                    List<Song> songList = songCollection.getSongs();
                    if(null != songList) {
                        for(Song tmpSong : songList) {
                            //Log.i(TAG, "tmpSOng = " + tmpSong);
                            String songPath = tmpSong.getPath();
                            if(null == songPath || 0 == songPath.length()) {    //online song will occured
                                continue;
                            }
                            if(tmpSong.getPath().equals(path)) {
                                song = tmpSong;
                            }
                        }
                    }
                }
                Log.i(TAG, "song = " + song);
                if(null != song) {
                    DBService.removeSongFromMediaStore(song);
                    DBService.removeSongFile(song);
                    Lewa.playerServiceConnector().removeSongs(new ArrayList<Song>(Arrays.asList(song)));
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.i(TAG, "e = " + e);
            }
            
	}
	
	private void startMonitorStorageState(){	//monitor sd mounted or unmounted
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
		intentFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
		intentFilter.addAction(Intent.ACTION_MEDIA_REMOVED);
		intentFilter.addAction(Intent.ACTION_MEDIA_SHARED);
		context.registerReceiver(mIntentReceiver, intentFilter);

	}

        protected void finalize() {
              context.unregisterReceiver(mIntentReceiver);
              if(null != musicFileObserverList) {
                stopWatching();
                musicFileObserverList.clear();
                musicFileObserverList = null;
              }
        }
	
	BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context arg0, Intent arg1) {
			if(null == arg1) {
				return;
			}
			String action = arg1.getAction();
			
			if(action.equals(Intent.ACTION_MEDIA_MOUNTED)) {
				initDirectoryObserver();
				startWatching();
			} else {	//the storage media is unmounted removed or shared
				stopWatching();
			}
		}
		
	};


        
        BroadcastReceiver scanSdFilesReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    String action = intent.getAction();
                    if (Intent.ACTION_MEDIA_SCANNER_FINISHED.equals(action)) {
                        restartWatching();
                    }
                }
            };
	
	class MusicFileObserver extends FileObserver {
            private String path = null;
		public MusicFileObserver(String path) {
			super(path);
                    this.path = path;
		}

		@Override
		public void onEvent(int event, String name) {
		        
			switch(event) {
                    case FileObserver.MOVED_FROM:   //64
                            if(!path.endsWith(".MP3") && !path.endsWith(".mp3")) {
                                return;
                            }
                          
			case FileObserver.DELETE:   //512			
			case FileObserver.MOVE_SELF:  //  2048               
				updateMusicFile(path+"/"+name);
				break;
			/*case FileObserver.MOVED_TO:	//128
                            if(path.endsWith(".MP3") || path.endsWith(".mp3")) {    //only support mp3 format music
                                updateMusicFile();
                            }
                            break;*/
			default:	//we don`t care this now
				
				break;
			}
		}
		
	}
}
