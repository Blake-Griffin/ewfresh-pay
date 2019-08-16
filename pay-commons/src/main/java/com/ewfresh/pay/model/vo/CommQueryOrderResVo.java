package com.ewfresh.pay.model.vo;

import java.io.Serializable;
import java.util.List;

/**
 * description:
 *      商户发送查询订单请求(支持卡户信息判断)的响应头信息封装
 * @author: JiuDongDong
 * date: 2018/4/16.
 */
public class CommQueryOrderResVo implements Serializable {
//    private static final long serialVersionUID = 7928693390777250146L;

    private String msgId;// 报文标识号

    private String hdlSts;// 处理状态 A-成功  B-失败  K-未明

    private String bdFlg;// 业务体报文块存在标识 0-有包体 1-无包体

    private String rtnCd;// 报文处理返回码

    private List<BOCCommQueryOrderResBodyVo> commQueryOrderResBodyVoList;// 响应体

    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    public String getHdlSts() {
        return hdlSts;
    }

    public void setHdlSts(String hdlSts) {
        this.hdlSts = hdlSts;
    }

    public String getBdFlg() {
        return bdFlg;
    }

    public void setBdFlg(String bdFlg) {
        this.bdFlg = bdFlg;
    }

    public String getRtnCd() {
        return rtnCd;
    }

    public void setRtnCd(String rtnCd) {
        this.rtnCd = rtnCd;
    }

    public List<BOCCommQueryOrderResBodyVo> getCommQueryOrderResBodyVoList() {
        return commQueryOrderResBodyVoList;
    }

    public void setCommQueryOrderResBodyVoList(List<BOCCommQueryOrderResBodyVo> commQueryOrderResBodyVoList) {
        this.commQueryOrderResBodyVoList = commQueryOrderResBodyVoList;
    }
}
