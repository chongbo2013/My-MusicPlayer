package com.lewa.player.model;

/**
 * Created by wuzixiu on 1/5/14.
 */
public class PlayStatus {
    private boolean isPlaying;

    private int repeatMode;

    private int shuffleMode;

    private Song playingSong;

    private SongCollection playingCollection;

    public boolean isPlaying() {
        return isPlaying;
    }

    public void setPlaying(boolean isPlaying) {
        this.isPlaying = isPlaying;
    }

    public Song getPlayingSong() {
        return playingSong;
    }

    public int getRepeatMode() {
        return repeatMode;
    }

    public void setRepeatMode(int repeatMode) {
        this.repeatMode = repeatMode;
    }

    public int getShuffleMode() {
        return shuffleMode;
    }

    public void setShuffleMode(int shuffleMode) {
        this.shuffleMode = shuffleMode;
    }

    public void setPlayingSong(Song playingSong) {
        this.playingSong = playingSong;
    }

    public SongCollection getPlayingCollection() {
        return playingCollection;
    }

    public void setPlayingCollection(SongCollection playingCollection) {
        this.playingCollection = playingCollection;
    }
}
