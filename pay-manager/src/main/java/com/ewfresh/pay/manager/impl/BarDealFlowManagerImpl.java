package com.ewfresh.pay.manager.impl;

import com.ewfresh.pay.manager.BarDealFlowManager;
import com.ewfresh.pay.model.BarDealFlow;
import com.ewfresh.pay.model.vo.BarDealFlowDownLoadVo;
import com.ewfresh.pay.model.vo.BarDealFlowOne;
import com.ewfresh.pay.model.vo.BarDealFlowTwoVo;
import com.ewfresh.pay.model.vo.BarDealFlowVo;
import com.ewfresh.pay.service.BarDealFlowService;
import com.ewfresh.pay.util.*;
import com.github.pagehelper.PageInfo;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author: LouZiFeng
 * @Description: 白条交易流水逻辑处理层
 * @Date: 2019/3/20
 */
@Component
public class BarDealFlowManagerImpl implements BarDealFlowManager {
    @Autowired
    private BarDealFlowService barDealFlowService;
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public Workbook exportWhiteLimitbyUid(ResponseData responseData, String title, HashMap<String, Object> stringObjectHashMap) {
        List<BarDealFlowTwoVo> accountFlows = barDealFlowService.exportWhiteLimitbyUid(stringObjectHashMap);
//        List<BarDealFlowOne> barDealFlowOnes = ExpBarDealFlowInfo(accountFlows);
        ExportBarDealFlowEnumOne[] values = ExportBarDealFlowEnumOne.values();
        Workbook workbook = new HSSFWorkbook();
        Map<String, String> map = new HashMap<>();
        for (ExportBarDealFlowEnumOne value : values) {
            map.put(value.name(), value.getValue());
        }
        POIUtil.setChineseMap(map);
        workbook = POIUtil.exportExcel(workbook, accountFlows, title, BarDealFlowTwoVo.class);
        logger.info("Excl BarDealFlow list to manager is ok");
        return workbook;
    }

    @Override
    public ResponseData getWhiteLimitbyUid(ResponseData responseData, Map<String, Object> map, Integer pageNumber, Integer pageSize) {
        PageInfo<BarDealFlowTwoVo> whiteBarPageInfo = barDealFlowService.getWhiteLimitbyUid(map, pageNumber, pageSize);
        List<BarDealFlowTwoVo> list = whiteBarPageInfo.getList();
        responseData.setEntity(list);
        responseData.setCurPage(pageNumber);
        responseData.setPageCount(whiteBarPageInfo.getPages());
        responseData.setTotal(Long.toString(whiteBarPageInfo.getTotal()));
        responseData.setCode(ResponseStatus.OK.getValue());
        responseData.setMsg(ResponseStatus.OK.name());
        return responseData;
    }

    @Override
    public ResponseData getBarDealFlowByUid(ResponseData responseData, Map<String, Object> map, Integer pageNumber, Integer pageSize) {
        PageInfo<BarDealFlow> whiteBarPageInfo = barDealFlowService.getBarDealByUid(map, pageNumber, pageSize);
        List<BarDealFlow> list = whiteBarPageInfo.getList();
        responseData.setEntity(list);
        responseData.setCurPage(pageNumber);
        responseData.setPageCount(whiteBarPageInfo.getPages());
        responseData.setTotal(Long.toString(whiteBarPageInfo.getTotal()));
        responseData.setCode(ResponseStatus.OK.getValue());
        responseData.setMsg(ResponseStatus.OK.name());
        return responseData;
    }

    @Override
    public Workbook exportBarDealFlowList(ResponseData responseData, String title, HashMap<String, Object> stringObjectHashMap) {

        List<BarDealFlow> accountFlows = barDealFlowService.exportBarDealFlowList(stringObjectHashMap);
        List<BarDealFlowOne> barDealFlowOnes = ExpBarDealFlowInfo(accountFlows);
        ExportBarDealFlowEnum[] values = ExportBarDealFlowEnum.values();
        Workbook workbook = new HSSFWorkbook();
        Map<String, String> map = new HashMap<>();
        for (ExportBarDealFlowEnum value : values) {
            map.put(value.name(), value.getValue());
        }
        POIUtil.setChineseMap(map);
        workbook = POIUtil.exportExcel(workbook, barDealFlowOnes, title, BarDealFlowOne.class);
        logger.info("Excl BarDealFlow list to manager is ok");
        return workbook;
    }
    /**
     * description: 查询白条使用情况
     * @author  huboyang
     * @param
     */
    @Override
    public void getBarDealFlow(ResponseData responseData, Map<String, Object> map, Integer pageNumber, Integer pageSize) {
        PageInfo<BarDealFlow> whiteBarPageInfo = barDealFlowService.getBarDealFlow(map, pageNumber, pageSize);
        List<BarDealFlow> list = whiteBarPageInfo.getList();
        responseData.setCurPage(pageNumber);
        responseData.setPageCount(whiteBarPageInfo.getPages());
        responseData.setTotal(Long.toString(whiteBarPageInfo.getTotal()));
        responseData.setEntity(list);
        responseData.setCode(ResponseStatus.OK.getValue());
        responseData.setMsg(ResponseStatus.OK.name());
    }

    @Override
    public void exportBarDealFlow(ResponseData responseData, String title, HashMap<String, Object> map, HttpServletResponse response) {
        ServletOutputStream output = null;
        List<BarDealFlowDownLoadVo> barDealFlow = barDealFlowService.getBarDeal(map);
        Workbook workbook = new HSSFWorkbook();
        Map<String, String> newMap = new HashMap<>();
        ExportBarDealFlow[] values = ExportBarDealFlow.values();
        for (ExportBarDealFlow value : values) {
            newMap.put(value.name(), value.getValue());
        }
        POIUtil.setChineseMap(newMap);
        workbook = POIUtil.exportExcel(workbook, barDealFlow, title, BarDealFlowDownLoadVo.class);
        logger.info("Excl BarDealFlow list to manager is ok");
        try {
            output = response.getOutputStream();
            response.reset();
            response.setHeader("Content-disposition", "attachment; filename=" + URLEncoder.encode(title + ".xls", "UTF-8"));
            response.setContentType("application/msexcel");
            workbook.write(output);
            logger.info("It is ok in exportAccountsList");
        }catch (Exception e){
            logger.error("IoException" );
        }

    }
    public enum ExportBarDealFlow {
        uname("客户名称"), usedLimit("当前使用额度"),  occTime("时间");

        private String value;
        ExportBarDealFlow(String value){
            this.value=value;
        }
        public String getValue(){
            return value;
        }
    }


    /**
     * @Description: 包装需要导出的交易流水明细
     * @Author louzifeng
     * @Date 2019/3/20
     */
    public List<BarDealFlowOne> ExpBarDealFlowInfo(List<BarDealFlow> list) {
        ArrayList<BarDealFlowOne> barDealFlowOnes = new ArrayList<>();
        for (BarDealFlow barDealFlow : list) {
            BarDealFlowOne barDealFlowOne = new BarDealFlowOne();
            barDealFlowOne.setId(barDealFlow.getId());//交易编号
            barDealFlowOne.setOccTime(barDealFlow.getOccTime());//交易时间
            barDealFlowOne.setAmount(barDealFlow.getAmount());//余额
            getDesp(barDealFlow, barDealFlowOne);
            barDealFlowOne.setOrderId(barDealFlow.getOrderId());
            barDealFlowOnes.add(barDealFlowOne);
        }
        return barDealFlowOnes;
    }

    public BarDealFlowOne getDesp(BarDealFlow barDealFlow, BarDealFlowOne barDealFlowOne) {
        Short dealType = barDealFlow.getDealType();
        if (dealType.shortValue() == Constants.ORDER_PAYMENT.shortValue()) {
            barDealFlowOne.setDealType(Constants.REMENT);//订单付款
        } else if (dealType.shortValue() == Constants.ORDER_REFUND.shortValue()) {
            barDealFlowOne.setDealType(Constants.REFUND);//订单退款
        } else if (dealType.shortValue() == Constants.ORDER_REPAYMENT.shortValue()) {
            barDealFlowOne.setDealType(Constants.REPAYMENT);//还款
        }
        return barDealFlowOne;
    }
}

