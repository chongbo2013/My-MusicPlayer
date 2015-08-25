package com.lewa.player.service;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.lewa.Lewa;
import com.lewa.player.IMediaPlaybackService;
import com.lewa.player.MediaPlaybackService;
import com.lewa.player.MusicUtils;
import com.lewa.player.db.DBService;
import com.lewa.player.model.Album;
import com.lewa.player.model.Playlist;
import com.lewa.player.model.Song;
import com.lewa.player.model.SongCollection;
import com.lewa.util.Constants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import  com.lewa.kit.ActivityHelper;

/**
 * TURTLE PLAYER
 * <p/>
 * Licensed under MIT & GPL
 * <p/>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE
 * OR OTHER DEALINGS IN THE SOFTWARE.
 * <p/>
 * More Information @ www.turtle-player.co.uk
 *
 * @author Simon Honegger (Hoene84)
 */


public class PlayerServiceConnector extends ObservableService {
    private static final String TAG = "PlayerServiceConnector";

    final ContextWrapper contextWrapper;
    final Object waitOnConnectionLock = new Object();
    IMediaPlaybackService service = null;

    public PlayerServiceConnector(ContextWrapper contextWrapper) {
        this.contextWrapper = contextWrapper;
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className,
                                       IBinder binder) {
            synchronized (waitOnConnectionLock) {
                Log.i(TAG, "Service connected.");
                service = IMediaPlaybackService.Stub.asInterface(binder);
                //TODO: register callback here.
                waitOnConnectionLock.notifyAll();
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            synchronized (waitOnConnectionLock) {
                Log.i(TAG, "Service disconnected.");

                if (service != null) {
//                    playerServiceBinder.unregister(mMessenger);
                    service = null;
                }
            }
        }
    };

    public void releasePlayer() {
        synchronized (waitOnConnectionLock) {
            if (service != null) {
                Log.i(TAG, "Release player.");
//                playerServiceBinder.unregister(mMessenger);
                contextWrapper.unbindService(mConnection);
                service = null;
            }
        }
    }

    public void connectPlayer(final ConnectionListener connectionListener) {
        new AsyncTask<Void, Void, IMediaPlaybackService>() {
            @Override
            protected IMediaPlaybackService doInBackground(Void... params) {
                Thread.currentThread().setName(Thread.currentThread().getName() + ":connectPlayer");
                synchronized (waitOnConnectionLock) {
                    if (service == null) {
                        try {
                            contextWrapper.bindService(new Intent(contextWrapper, MediaPlaybackService.class), mConnection, Context.BIND_AUTO_CREATE);
                            waitOnConnectionLock.wait();
                        } catch (InterruptedException e) {
                            Log.e(TAG, "wait on service was interupted", e);
                        }
                    }
                    return service;
                }
            }

            @Override
            protected void onPostExecute(IMediaPlaybackService service) {
                if (service != null && connectionListener != null) {
                    connectionListener.connected(service);
                } else {
                    Log.i(TAG, "omitting Player call: " + connectionListener);
                }
            }

        }.execute();
    }

    public void playFile(final String path) {
        connectPlayer(new ConnectionListener() {
            @Override
            public void connected(IMediaPlaybackService service) {
                try {
                    service.openFile(path);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });
    }

//    public void playSong(final String songJson) {
//        connectPlayer(new ConnectionListener() {
//            @Override
//            public void connected(IMediaPlaybackService service) {
//                try {
//                    service.playSong(songJson);
//                } catch (RemoteException e) {
//                    e.printStackTrace();
//                }
//            }
//        });
//    }

    public void play() {
        connectPlayer(new ConnectionListener() {
            @Override
            public void connected(IMediaPlaybackService service) {
                try {
                    service.play();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void pause() {
        connectPlayer(new ConnectionListener() {
            @Override
            public void connected(IMediaPlaybackService service) {
                try {
                    service.pause();
                    Log.d(TAG, "Pause playing.");
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void stop() {
        connectPlayer(new ConnectionListener() {
            @Override
            public void connected(IMediaPlaybackService service) {
                try {
                    service.stop();
                    Log.d(TAG, "Stop player.");
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void previous() {
        connectPlayer(new ConnectionListener() {
            @Override
            public void connected(IMediaPlaybackService service) {
                try {
                    service.prev();
                    Log.d(TAG, "Play previous song.");
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void next() {
        connectPlayer(new ConnectionListener() {
            @Override
            public void connected(IMediaPlaybackService service) {
                try {
                    service.next();
                    Log.d(TAG, "Play next song.");
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public boolean isPlaying() {
        if (service != null) {
            try {
                return service.isPlaying();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        return false;
    }

    public void seek(final long position) {
        if (service != null) {
            try {
                service.seek(position);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public void setQueuePosition(final int position) {
        connectPlayer(new ConnectionListener() {
            @Override
            public void connected(IMediaPlaybackService service) {
                try {
                    service.setQueuePosition(position);
                    Log.d(TAG, "Set queue position: " + position);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void removeSongs(final Collection<Song> songs) {
        Log.d(TAG, "Remove song from playing queue, size: " + songs.size());
        final Collection<Song> toBeRemovedSongs = new ArrayList<Song>();
        toBeRemovedSongs.addAll(songs);

        connectPlayer(new ConnectionListener() {
            @Override
            public void connected(IMediaPlaybackService service) {
                Log.d(TAG, "Service connected, song size: " + toBeRemovedSongs.size());

                try {
                    for (Song song : toBeRemovedSongs) {
                        Log.d(TAG, "Remove song from playing queue: " + song.getId());
                        service.removeTrack(song.getId());
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public long position() {
        if (service != null) {
            try {
                return service.position();
            } catch (RemoteException e) {
                e.printStackTrace();
                return 0;
            }
        } else {
            return 0;
        }
    }
    
    public long duration() {
    	
    	if(service != null) {
    		try {
    			return service.duration();
    		} catch (RemoteException e) {
                e.printStackTrace();
                return 0;
            }
        } else {
            return 0;
        }
    }

    public void setRepeatAndShuffleMode(final int repeatMode, final int shuffleMode) {
        connectPlayer(new ConnectionListener() {
            @Override
            public void connected(IMediaPlaybackService service) {
                try {
                    service.setRepeatMode(repeatMode);
                    service.setShuffleMode(shuffleMode);
                    Lewa.setRepeatAndShuffleMode(repeatMode, shuffleMode);
                    Log.d(TAG, "Set repeat and shuffle mode.");
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public IMediaPlaybackService service() {
        return service;
    }

    public void playSongCollection(Activity activity, final SongCollection songCollection, final int position) {
        Log.i(TAG, "Play collection: " + songCollection.getType() + ", position: " + position);
        if (songCollection == null) return;

        if (Lewa.getPlayingCollection() != null && !songCollection.equals(Lewa.getPlayingCollection())) {
            Lewa.getPlayingCollection().clear();
        }

        Lewa.isShowedToast = false;
        Lewa.setPlayingCollection(songCollection);

        List<Song> songs = songCollection.getSongs();

        if (songs != null && songs.size() > 0) {
            long[] ids = new long[songs.size()];

            Set<Song> playingOnlineSongs = new HashSet<Song>();

            for (int i = 0; i < songs.size(); i++) {
                Song song = songs.get(i);

                if (song.getType() == Song.TYPE.ONLINE) {
                    ids[i] = song.getId().longValue() * -1;
                    playingOnlineSongs.add(song);
                } else {
                    ids[i] = song.getId().longValue();
                }
            }

            Lewa.setPlayingOnlineSongs(playingOnlineSongs);
            playAll(activity, ids, position);
        }
    }

    /**
     * from lewa MusicUtils
     */


    public void shuffleAll(Activity context, Cursor cursor) {
        playAll(context, cursor, -1, true);
    }

    public void playAll(Activity context, Cursor cursor) {
        playAll(context, cursor, 0, false);
    }

    public void playAll(Activity context, Cursor cursor, int position) {
        playAll(context, cursor, position, false);
    }

    public void playAll(Activity context, long[] list, int position) {
        playAll(context, list, position, false);
    }

    private void playAll(Activity context, Cursor cursor, int position, boolean force_shuffle) {

        long[] list = MusicUtils.getSongListForCursor(cursor);
        playAll(context, list, position, force_shuffle);
    }

    private void playAll(Activity context, long[] list, int position, boolean force_shuffle) {
        
        if (list == null || list.length == 0 || service == null) {
            return;
        }
        try {
        	MusicUtils.mHasSongs=true;
            if (force_shuffle) {
                service.setShuffleMode(MediaPlaybackService.SHUFFLE_NORMAL);
            }
            long curid = service.getAudioId();
            int curpos = service.getQueuePosition();
            if (position != -1 && curpos == position && position < list.length && curid == list[position]) {
                // The selected file is the file that's currently playing;
                // figure out if we need to restart with a new playlist,
                // or just launch the playback activity.
                long[] playlist = service.getQueue();
                if (Arrays.equals(list, playlist)) {
                    // we don't need to set a new list, but we should resume playback if needed
                    service.play();
                    return; // the 'finally' block will still run
                }
            }
            if (position < 0) {
                position = 0;
            }
            service.open(list, force_shuffle ? -1 : position);
            // service.play();
        } catch (RemoteException ex) {
        } finally {

        }
    }
}
