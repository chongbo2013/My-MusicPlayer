package com.lewa.player.model;

import java.util.Date;

/**
 * Created by Administrator on 13-12-7.
 */
public class TopList extends BaseModel {

    private String name;

    private String coverUri;

    private Date updateTime;

    private TYPE type;

    public TopList() {}

    public TopList(TYPE type, String name, String coverUri, Date updateTime) {
        this.type = type;
        this.name = name;
        this.coverUri = coverUri;
        this.updateTime = updateTime;
    }

    public TYPE getType() {
        return type;
    }

    public void setType(TYPE type) {
        this.type = type;
    }

    public static enum TYPE {
        NEW, HOT
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCoverUri() {
        return coverUri;
    }

    public void setCoverUri(String coverUri) {
        this.coverUri = coverUri;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}
