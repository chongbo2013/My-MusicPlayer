package com.lewa.player.model;

/**
 * Created by Administrator on 14-1-7.
 */
public class Pagination {

    public static final int DEFAULT_PAGE_SIZE = 50;
    public int pageNo = 0;
    public int pageSize;

    public int getPageNo() {
        return pageNo;
    }

    public void setPageNo(int pageNo) {
        this.pageNo = pageNo;
    }

    public int getPageSize() {
        if(pageSize == 0) {
            return DEFAULT_PAGE_SIZE;
        }
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }
}

