package com.lewa.player.model;

import java.io.Serializable;

/**
 * Created by Administrator on 13-12-8.
 */
public class BaseModel implements Serializable {
    public static final String ID = "id";

    String initial;
    Long id;
    

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getInitial() {
        return initial;
    }

    public void setInitial(String initial) {
        this.initial = initial;
    }
}
