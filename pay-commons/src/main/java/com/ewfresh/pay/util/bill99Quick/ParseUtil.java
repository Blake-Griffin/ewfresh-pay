package com.ewfresh.pay.util.bill99quick;

import com.ewfresh.pay.model.bill99quick.TransInfo;

import java.util.HashMap;

/**
 * @Description: 快钱VOP_CNP的Interface接口程序
 * @Copyright (c) 上海快钱信息服务有限公司
 * @version 2.0
 */

/**
 * 该类用来拼接XML串和解析XML
 */
@SuppressWarnings("unchecked")
public class ParseUtil {
    /**
     * 具体解析XML方法，返回一个HashMap
     * resXml：快钱返回的XML数据流
     */
    public HashMap parseXML(String resXml, TransInfo transInfo) {
        //TransInfo transInfo= new TransInfo();

        HashMap returnRespXml = null;
        ParseXMLUtil pxu = ParseXMLUtil.initParseXMLUtil();//初始化ParseXMLUtil
        if (resXml != null) {
            System.out.println("TransInfo.isFLAG()= " + transInfo.isFLAG());
            //下面判断选用那个方法获取XML数据
            if (transInfo.isFLAG()) {
                returnRespXml = pxu.returnXMLData(pxu.parseXML(resXml), transInfo.getRecordeText_1(), transInfo.getRecordeText_2());
            } else {
                returnRespXml = pxu.returnXMLDataList(pxu.parseXML(resXml), transInfo.getRecordeText_1(), transInfo.getRecordeText_2(), transInfo.getRecordeText_3(), transInfo.getRecordeText_4());
            }
        }
        return returnRespXml;
    }
}
