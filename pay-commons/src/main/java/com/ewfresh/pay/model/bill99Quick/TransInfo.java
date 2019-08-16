package com.ewfresh.pay.model.bill99quick;

public class TransInfo {

    private String postUrl;             //提交地址
    private boolean FLAG;               //用来判断选用的解析函数
    private String recordeText_1;       //用来记录XML内容格式字段，用来记录XML第一个标志内容格式字段
    private String recordeText_2;       //当标记的内容多时，用来记录XML第二个标志内容格式字段
    private String recordeText_3;       //用来记录XML内容格式字段，用来记录XML第一个标志内容格式字段
    private String recordeText_4;       //当标记的内容多时，用来记录XML第二个标志内容格式字段
    private String txnType;             //交易类型

    ///TransInfo transInfo= new TransInfo();
    public String getPostUrl() {
        return postUrl;
    }

    public void setPostUrl(String postUrl) {
        this.postUrl = postUrl;
    }

    public boolean isFLAG() {
        return FLAG;
    }

    public void setFLAG(boolean flag) {
        FLAG = flag;
    }

    public String getRecordeText_1() {
        return recordeText_1;
    }

    public void setRecordeText_1(String recordeText_1) {
        this.recordeText_1 = recordeText_1;
    }

    public String getRecordeText_2() {
        return recordeText_2;
    }

    public void setRecordeText_2(String recordeText_2) {
        this.recordeText_2 = recordeText_2;
    }

    public String getTxnType() {
        return txnType;
    }

    public void setTxnType(String txnType) {
        this.txnType = txnType;
    }

    public String getRecordeText_3() {
        return recordeText_3;
    }

    public void setRecordeText_3(String recordeText_3) {
        this.recordeText_3 = recordeText_3;
    }

    public String getRecordeText_4() {
        return recordeText_4;
    }

    public void setRecordeText_4(String recordeText_4) {
        this.recordeText_4 = recordeText_4;
    }
    /*
    public static TransInfo setTxnType(String txnType) {
		TransInfo transInfo=new TransInfo();
		transInfo.setRecordeText_1("GetDynNumContent");
		transInfo.setRecordeText_2("ErrorMsgContent");
		txnType = txnType;
		return transInfo;
	}
	*/
}
