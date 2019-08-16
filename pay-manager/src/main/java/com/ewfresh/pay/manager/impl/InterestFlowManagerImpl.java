package com.ewfresh.pay.manager.impl;



import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.ewfresh.commons.util.ItvJsonUtil;
import com.ewfresh.pay.manager.InterestFlowManager;
import com.ewfresh.pay.model.InterestFlow;
import com.ewfresh.pay.model.vo.InterestFlowVo;
import com.ewfresh.pay.service.InterestFlowService;
import com.ewfresh.pay.util.POIUtil;
import com.ewfresh.pay.util.ResponseData;
import com.github.pagehelper.PageInfo;

/**
 * Class description
 *
 *
 * @date    19/08/14
 * @author  huboyang
 */
@Component
public class InterestFlowManagerImpl implements InterestFlowManager {
    private Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private InterestFlowService interestFlowService;

    private enum ExportInterestFlowEnum {
        id("Id"), uname("用户名称"), billTime("账单生成时间"), repaidInterest("还逾期费金额"), repayTime("还款时间");
        private String value;
        ExportInterestFlowEnum(String value) {
            this.value = value;
        }
        public String getValue() {
            return value;
        }
    }

    @Override
    public Workbook exportInterestFlow(ResponseData responseData, String uname, String startTime, String endTime,
                                       String title) {
        logger.info("---------------------------> uname={},startTime={},endTime={},title={}", uname, startTime, endTime, title);
        List<InterestFlowVo> interestFlowList = interestFlowService.exportInterestFlow(uname, startTime, endTime);
        ExportInterestFlowEnum[] values           = ExportInterestFlowEnum.values();
        Workbook workbook= new HSSFWorkbook();
        Map<String, String> map = new HashMap<>();
        for (ExportInterestFlowEnum value : values) {
            map.put(value.name(), value.getValue());
        }
        POIUtil.setChineseMap(map);
        workbook = POIUtil.exportExcel(workbook, interestFlowList, title, InterestFlowVo.class);
        logger.info("Excl exportInterestFlow  is ok");
        return workbook;
    }

    @Override
    public void getInterestFlow(String uname, ResponseData responseData, Integer pageNumber, Integer pageSize,
                                String startTime, String endTime) {
        logger.info("uname={},startTime={},endTime={}", uname, startTime, endTime);
        PageInfo<InterestFlow> list1 = interestFlowService.getInterestFlowByCondition(uname, pageNumber, pageSize, startTime, endTime);
        List<InterestFlow> list = list1.getList();
        logger.info("list={}", ItvJsonUtil.toJson(list));
        responseData.setTotal(String.valueOf(list1.getTotal()));
        responseData.setPageCount(list1.getPages());
        responseData.setCurPage(pageNumber);
        responseData.setEntity(list);
    }
}

