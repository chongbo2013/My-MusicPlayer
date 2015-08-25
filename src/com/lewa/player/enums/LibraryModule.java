package com.lewa.player.enums;

import com.lewa.player.R;

/**
 * Created by Administrator on 13-11-28.
 */
public enum LibraryModule {

    //SEARCH(1, R.string.title_browse), SINGER(2, R.string.title_artist), SUBJECT(3, R.string.title_album), MY(4, R.string.title_mine);
    SEARCH(1, R.string.title_local), SINGER(2, R.string.title_artist), SUBJECT(3, R.string.title_mine), MY(4, R.string.title_browse);

    private int id;
    private int strId;

    private LibraryModule(int id, int strId) {
        this.id = id;
        this.strId = strId;
    }

    public int getId() {
        return id;
    }

    public int value() {
        return strId;
    }
}
