package com.lewa.player.model;

import com.lewa.player.model.Song;
/**
 * Created by sjxu on 2014-10-21.
 */
public class HistoryBean {
	public enum INFO_TYPE{
		ONLINE,
		LOCAL,
		HISTORY
	}

	private Object owner;
	private String songInfo;
	private INFO_TYPE infoType;

	public HistoryBean() {
		this.owner = null;
		this.songInfo = null;
		this.infoType = null;
	}

	public HistoryBean(Object owner, INFO_TYPE infoType) {
		this.owner = owner;
		this.infoType = infoType;
		songInfo = "";  //init songInfo
		if(INFO_TYPE.ONLINE == infoType || INFO_TYPE.HISTORY == infoType) {
			songInfo = (String)owner;
		} else {    //local
			Song song = (Song)owner;
			if(song != null) {
				songInfo = (song.getArtist() != null ? song.getArtist().getName() : "") + "\t" + song.getName();
			}
		}
	}

	public Object getOwner() {
		return owner;
	}

	public void setInfo() {
		this.songInfo = songInfo;
	}

	public String getInfo() {
		return songInfo;
	}

	public void setType( INFO_TYPE infoType) {
		this.infoType = infoType;
	}

	public INFO_TYPE getType() {
		return infoType;
	}
};
    
