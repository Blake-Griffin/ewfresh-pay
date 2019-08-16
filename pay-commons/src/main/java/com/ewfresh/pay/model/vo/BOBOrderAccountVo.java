package com.ewfresh.pay.model.vo;

import java.io.Serializable;
import java.util.List;

/**
 * description:
 *      封装BOB对账响应数据
 * @author: JiuDongDong
 * date: 2018/4/23.
 */
public class BOBOrderAccountVo implements Serializable {

    private static final long serialVersionUID = -1340188011274764174L;

    private String encoding;// 编码格式

    private String signMethod;// 签名方法 取值：01（表示采用RSA）

    private String certId;// 此处返回北京银行证书序列号

    private String signature;// 签名域

    private String merId;// 商户号

    private String date;// 交易日期 清算时间

    private String count;// 交易明细域中具体明细的数量

    private String reserve1;// 保留域1 暂未启用

    private String reserve2;// 保留域2 暂未启用

    private List<BOBAccountVo> bobAccountVoList;// BOB交易明细list

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public String getSignMethod() {
        return signMethod;
    }

    public void setSignMethod(String signMethod) {
        this.signMethod = signMethod;
    }

    public String getCertId() {
        return certId;
    }

    public void setCertId(String certId) {
        this.certId = certId;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getMerId() {
        return merId;
    }

    public void setMerId(String merId) {
        this.merId = merId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getCount() {
        return count;
    }

    public void setCount(String count) {
        this.count = count;
    }

    public String getReserve1() {
        return reserve1;
    }

    public void setReserve1(String reserve1) {
        this.reserve1 = reserve1;
    }

    public String getReserve2() {
        return reserve2;
    }

    public void setReserve2(String reserve2) {
        this.reserve2 = reserve2;
    }

    public List<BOBAccountVo> getBobAccountVoList() {
        return bobAccountVoList;
    }

    public void setBobAccountVoList(List<BOBAccountVo> bobAccountVoList) {
        this.bobAccountVoList = bobAccountVoList;
    }
}
