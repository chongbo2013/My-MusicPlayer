package com.lewa.player.model;

/**
 * Created by Administrator on 13-12-8.
 */
public class MineItem extends BaseModel {

    private String name;

    private String coverUri;

    private Long count;

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

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }
}
