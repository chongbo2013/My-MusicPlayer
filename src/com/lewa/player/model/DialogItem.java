package com.lewa.player.model;

import java.io.Serializable;

/**
 * Created by Administrator on 13-12-15.
 */
public class DialogItem implements Serializable {

    public static int TYPE_CREATE_PLAY_LIST = 0;
    public static int TYPE_CUSTOMER_PLAY_LIST = 1;

    private Long id;

    private String name;

    private int type;

    private Object entity;

    public Object getEntity() {
        return entity;
    }

    public void setEntity(Object entity) {
        this.entity = entity;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
