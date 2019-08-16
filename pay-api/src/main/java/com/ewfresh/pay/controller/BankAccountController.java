package com.ewfresh.pay.controller;

import com.alibaba.fastjson.TypeReference;
import com.ewfresh.commons.Adopt;
import com.ewfresh.commons.util.ItvJsonUtil;
import com.ewfresh.pay.manager.BankAccountManager;
import com.ewfresh.pay.model.BankAccount;
import com.ewfresh.pay.util.ResponseData;
import com.ewfresh.pay.util.ResponseStatus;
import com.ewfresh.pay.util.ResponseUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;

/**
 * description:绑定银行卡的控制层
 *
 * @author: wangyaohui
 * @date 2018年4月21916:16:06
 */
@Controller
public class BankAccountController {
    @Autowired
    private BankAccountManager bankAccountManager;
    private Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Description: 用uid查询其绑定银行卡信息
     *
     * @param uid 要查询的id
     *  date: 2018/4/8 14:58
     * @author: wangyaohui
     */
    @Adopt
    @RequestMapping(value = "/t/get_bank_account_uid.htm")
    public void getBankAccountUid(HttpServletResponse response, Long uid,Short type) {
        logger.info("Get bank accout by uid [uid = {}]", uid);
        ResponseData responseData = new ResponseData();
        try {
            if (uid == null) {
                logger.warn("Get bank accout by uid param is null");
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg(ResponseStatus.PARAMNULL.name());
                return;
            }
            //
            bankAccountManager.getBankAccountById(responseData, uid,type);
            logger.info("Get bank accout by uid param is ok");
            responseData.setCode(ResponseStatus.OK.getValue());
            responseData.setMsg(ResponseStatus.OK.name());
        } catch (Exception e) {
            logger.error("Get bank accout by uid param is err", e);
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg(ResponseStatus.ERR.name());
        } finally {
            ResponseUtil.responsePrint(response, responseData, logger);
        }

    }

    /**
     * Description: 用uid增加其绑定银行卡信息
     *
     * @param bankAccount 要查询的id
     *   date: 2018/4/8 14:58
     * @author: wangyaohui
     */
    @Adopt
    @RequestMapping(value = "/t/add_bank_account.htm")
    public void addBankAccount(HttpServletResponse response, BankAccount bankAccount,String code) {
        logger.info("Add a area bank accout [uid = {}]", ItvJsonUtil.toJson(bankAccount));
        ResponseData responseData = new ResponseData();
        try {
            if (bankAccount == null||bankAccount.getUserId() ==null || StringUtils.isBlank(code) ) {
                logger.warn("Add a area bank accout param is null");
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg(ResponseStatus.PARAMNULL.name());
                return;
            }
            bankAccountManager.addBankAccount(responseData, bankAccount,code);
            logger.info("Add a area bank accout param is ok");
        } catch (Exception e) {
            logger.error("Add a area bank accout param is err", e);
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg(ResponseStatus.ERR.name());
        } finally {
            ResponseUtil.responsePrint(response, responseData, logger);
        }

    }

    /**
     * Description: 用id解除绑定银行卡信息
     *
     * @param id 要查询的id
     * date: 2018/4/8 14:58
     * @author: wangyaohui
     */
    @Adopt
    @RequestMapping(value = "/t/del_bank_account.htm")
    public void delBankAccount(HttpServletResponse response, Integer id,String code,Long uid) {
        logger.info("Delete bank accout by id [id = {}]", id);
        ResponseData responseData = new ResponseData();
        try {
            if (id == null || StringUtils.isBlank(code)) {
                logger.warn("Delete bank accout by id param is null");
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg(ResponseStatus.PARAMNULL.name());
                return;
            }
            bankAccountManager.delBankAccountById(responseData, id,uid,code);
            logger.info("Delete bank accout by id is ok");
        } catch (Exception e) {
            logger.error("Delete bank accout by id is err", e);
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg(ResponseStatus.ERR.name());
        } finally {
            ResponseUtil.responsePrint(response, responseData, logger);
        }

    }
    /**
     * Description: 用考号通过调用albaba的接口查询所属银行
     *
     * @param cardNo 要查询的卡号
     *  date: 2018/4/8 14:58
     * @author: wangyaohui
     */
    @Adopt
    @RequestMapping("/p/get_accout_code.htm")
    public void getAccoutCode(HttpServletResponse response, String cardNo) {
        logger.info("Delete bank accout by id [cardNo = {}]", cardNo);
        ResponseData responseData = new ResponseData();
        try {
            // 创建HttpClient实例
            String url = "https://ccdcapi.alipay.com/validateAndCacheCardInfo.json?_input_charset=utf-8&cardNo=";
            url += cardNo;
            url += "&cardBinCheck=true";
            StringBuilder sb = new StringBuilder();
            URL urlObject = new URL(url);
            URLConnection uc = urlObject.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(uc.getInputStream()));
            String inputLine = null;
            while ((inputLine = in.readLine()) != null) {
                sb.append(inputLine);
            }
            in.close();
            Map<String, Object> stringObjectMap = ItvJsonUtil.jsonToObj(sb.toString(), new TypeReference<Map<String, Object>>() {
            });
            responseData.setCode(ResponseStatus.OK.getValue());
            responseData.setMsg(ResponseStatus.OK.name());
            responseData.setEntity(stringObjectMap);
        } catch (Exception e) {
            logger.error("Delete bank accout by id is err", e);
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg(ResponseStatus.ERR.name());
        } finally {
            ResponseUtil.responsePrint(response, responseData, logger);
        }

    }
    @RequestMapping("/p/pay/bank_account_code.htm")
    public  void bankAccountCode(HttpServletResponse response,Long uid,Integer bankId){
        logger.info("Get bank account code :[uid = {}]", uid);
        ResponseData responseData = new ResponseData();
        try {
            if (uid == null) {
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg("Get bank account code is null");
                return;
            }
            bankAccountManager.bankAccountCode(responseData,uid,bankId);
        } catch (Exception e) {
            logger.error("Get bank account code is err", e);
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg(ResponseStatus.ERR.name());
        } finally {
            ResponseUtil.responsePrint(response, responseData, logger);
        }
    }
}
