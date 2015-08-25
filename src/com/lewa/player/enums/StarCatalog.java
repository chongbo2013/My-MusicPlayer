package com.lewa.player.enums;

/**
 * Created by Administrator on 13-11-28.
 */
public enum StarCatalog {

    CHINESE_MALE_SINGER(1, "华语男歌手"), CHINESE_SONGBIRD(2, "华语女歌手"), CHINESE_GROUP(3, "华语组合"),

    UA_MALE(4, "欧美男歌手"), UA_FEMALE(4, "欧美女歌手"), UA_GROUP(5, "欧美组合");

    private int id;
    private String name;

    private StarCatalog(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String value() {
        return name;
    }
}
