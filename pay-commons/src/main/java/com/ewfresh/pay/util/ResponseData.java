package com.ewfresh.pay.util;

import java.io.Serializable;

/**
 * This class return the response infomations.
 */
public class ResponseData implements Serializable {
    private static final long serialVersionUID = 1L;
    private String code;//状态码
    private String msg;//状态注释
    private String total;//总条数
    private int pageCount;//总页数
    private int curPage;//当前页
    private Object entity;//返回的结果集
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String  getTotal() {return total;}
    public void setTotal(String total) {
        this.total = total;
    }

    public int getPageCount() {
        return pageCount;
    }

    public void setPageCount(int pageCount) {
        this.pageCount = pageCount;
    }

    public int getCurPage() {
        return curPage;
    }

    public void setCurPage(int curPage) {
        this.curPage = curPage;
    }

    public Object getEntity() {
        return entity;
    }

    public void setEntity(Object entity) {
        this.entity = entity;
    }

    @Override
    public String toString() {
        return "ResponseData{" +
                "code='" + code + '\'' +
                ", msg='" + msg + '\'' +
                ", total='" + total + '\'' +
                ", pageCount=" + pageCount +
                ", curPage=" + curPage +
                ", entity=" + entity +
                '}';
    }
}

