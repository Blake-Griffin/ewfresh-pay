package com.ewfresh.pay.manager.impl;

import com.alibaba.fastjson.TypeReference;
import com.ewfresh.commons.client.MsgClient;
import com.ewfresh.commons.util.ItvJsonUtil;
import com.ewfresh.pay.configure.Bill99PayConfigure;
import com.ewfresh.pay.manager.BankAccountManager;
import com.ewfresh.pay.model.BankAccount;
import com.ewfresh.pay.model.vo.BankAccountVo;
import com.ewfresh.pay.redisService.BankAccountRedisService;
import com.ewfresh.pay.service.BankAccountService;
import com.ewfresh.pay.util.BankCardSwitchUtil;
import com.ewfresh.pay.util.Constants;
import com.ewfresh.pay.util.ResponseData;
import com.ewfresh.pay.util.ResponseStatus;
import com.ewfresh.pay.util.bill99.FinderSignService;
import com.ewfresh.pay.util.boc.MyHttp;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;

import static com.ewfresh.commons.util.ItvJsonUtil.jsonToObj;

/**
 * Created by 王耀辉 on 2018/4/19.
 */
@Component
public class BankAccountManagerImpl implements BankAccountManager {
    @Autowired
    private BankAccountService bankAccountService;
    @Autowired
    private BankAccountRedisService bankAccountRedisService;
    private Logger logger = LoggerFactory.getLogger(getClass());
    @Value("${http_msg}")
    private String msgUrl;
    private String  key= "验证码";
    private Integer codeSize = 6;//验证码长度
    @Autowired
    private Bill99PayConfigure bill99PayConfigure;
    @Autowired
    private MsgClient msgClient;
    @Override
    public void delBankAccountById(ResponseData responseData, Integer id, Long uid, String code) {
        String bankCode = bankAccountRedisService.getBankCode(uid);
        logger.info("delBankAccountById bankCode={},code={}",bankCode,code);
        if (code.equals(bankCode)) {
            bankAccountService.delBankAccountById(id);
            responseData.setCode(ResponseStatus.OK.getValue());
            responseData.setMsg(ResponseStatus.OK.name());
        } else {
            responseData.setCode(ResponseStatus.CODEERR.getValue());
            responseData.setMsg(ResponseStatus.CODEERR.name());
        }
    }

    @Override
    public void addBankAccount(ResponseData responseData, BankAccount record, String code) {
        Long userId = record.getUserId();
        String bankCode = bankAccountRedisService.getBankCode(userId);
        if (code.equals(bankCode)) {
            String cardCode = record.getCardCode();
            if (StringUtils.isNotBlank(cardCode)) {
                BankAccount bankAccoutByBankCode = bankAccountService.getBankAccoutByBankCode(cardCode);
                if(bankAccoutByBankCode == null){
                 bankAccountService.addBankAccount(record);
                 responseData.setCode(ResponseStatus.OK.getValue());
                 responseData.setMsg(ResponseStatus.OK.name());
                }else {
                    responseData.setCode(ResponseStatus.BANKREPEAT.getValue());
                    responseData.setMsg(ResponseStatus.BANKREPEAT.name());
                }
            } else {
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg(ResponseStatus.PARAMNULL.name());
            }
        } else {
            responseData.setCode(ResponseStatus.CODEERR.getValue());
            responseData.setMsg(ResponseStatus.CODEERR.name());
        }
    }

    @Override
    public void getBankAccountById(ResponseData responseData, Long id, Short type)throws Exception  {
        //此处responseData 转移到controller
        if(type == Constants.TRADE_TYPE_3){
            List<BankAccountVo> bankAccountById = bankAccountService.getBankAccountById(id, Constants.SHORT_ZERO);
            List<Map<String, String>> maps = bankcardList(id);
            List<BankAccountVo> bankAccountVos = encapsulationBankcardList(maps, bankAccountById);
            //java.lang.NullPointerException
            if (CollectionUtils.isEmpty(bankAccountVos)) {
                return;
            }
            for (BankAccountVo bankAccountVo : bankAccountVos) {
                String s = BankCardSwitchUtil.cipherOriginCardCode(bankAccountVo.getWholeBankName());
                bankAccountVo.setWholeBankName(s);
            }
            responseData.setEntity(bankAccountVos);
        }else {
            List<BankAccountVo> bankAccountById = bankAccountService.getBankAccountById(id, type);
            //java.lang.NullPointerException
            if (CollectionUtils.isEmpty(bankAccountById)) {
                return;
            }
            for (BankAccountVo bankAccountVo : bankAccountById) {
                String s = BankCardSwitchUtil.cipherOriginCardCode(bankAccountVo.getWholeBankName());
                bankAccountVo.setWholeBankName(s);
            }
            responseData.setEntity(bankAccountById);
        }

    }

    public void bankAccountCode(ResponseData responseData, Long uid,Integer bankId){
        BankAccount bankByCardCode = bankAccountService.getBankByCardCode(uid, bankId);
        String phone = bankByCardCode.getMobilePhone();
        String randomNumCode = getRandomNumCode(codeSize);
        msgClient.postMsg(msgUrl,phone,uid.toString(),randomNumCode,key);
        bankAccountRedisService.addBankCodeByRedis(uid,randomNumCode);
        responseData.setCode(ResponseStatus.OK.getValue());
        responseData.setMsg(ResponseStatus.OK.name());
    }

    public  String getRandomNumCode(Integer number){
        String codeNum = "";
        Integer [] numbers = {0,1,2,3,4,5,6,7,8,9};
        Random random = new Random();
        for (int i = 0; i < number; i++) {
            int next = random.nextInt(10000);//目的是产生足够随机的数，避免产生的数字重复率高的问题
            codeNum+=numbers[next%10];
        }
        return codeNum;
    }

    public List<Map<String, String>>  bankcardList(Long id) throws Exception {
        // 1.获取配置信息
        String hatPrivateKey = bill99PayConfigure.getHatPrivateKey();
        String domainName = bill99PayConfigure.getDomainName();
        String platformCode = bill99PayConfigure.getPlatformCode();
        String hatPublicKey = bill99PayConfigure.getHatPublicKey();
        HashMap<String, String> msgMap = new HashMap<>();
        msgMap.put("uId", id.toString());
        msgMap.put("platformCode", platformCode);
        FinderSignService finderSignService = new FinderSignService();
        String sign = finderSignService.sign(platformCode, ItvJsonUtil.toJson(msgMap), hatPrivateKey);
        MyHttp bindHttpDeal = new MyHttp();
        Map<String, Object> post = bindHttpDeal.post(domainName + "/person/bankcard/list", ItvJsonUtil.toJson(msgMap), UUID.randomUUID().toString(), platformCode, sign);
        logger.info("the signMsg is ------->{}", sign);
        logger.info("[post={}]", post);
        String content = String.valueOf(post.get("content"));
        CloseableHttpResponse response1 = (CloseableHttpResponse) post.get("response");
        boolean verify = finderSignService.verify(response1, content, hatPublicKey);
        logger.info("the res is --------------------->{}",verify);
        Map<String, String> stringStringMap = ItvJsonUtil.jsonToObj(content, new TypeReference<Map<String, String>>() {
        });
        List<Map<String, String>> maps =null;
        Object rspCode = stringStringMap.get("rspCode");
        if (rspCode.equals(Constants.BILL99_RSPCODE_0000)) {
            Object bindCardList = stringStringMap.get("bindCardList");
            //java.lang.NullPointerException
            if (bindCardList == null) {
                return null;
            }
             maps = ItvJsonUtil.jsonToObj(bindCardList.toString(), new TypeReference<List<Map<String, String>>>() {
            });
        }
        return maps;
    }


   public  List<BankAccountVo> encapsulationBankcardList(List<Map<String, String>> maps ,List<BankAccountVo> lists){
       HashMap<String, BankAccountVo> stringBankAccountVoHashMap = new HashMap<>();
       //封装要处理的参数
       for (BankAccountVo list : lists) {
           String cardCode = list.getWholeBankName();
           logger.info("[cardCode={}]", cardCode);
           stringBankAccountVoHashMap.put(cardCode,list);
       }
       //java.lang.NullPointerException
       if (CollectionUtils.isEmpty(maps)) {
           return null;
       }
       for (Map<String, String> map : maps) {
           String bankAcctId = map.get("bankAcctId");
           logger.info("[bankAcctId={}]", bankAcctId);
           BankAccountVo bankAccountVo = stringBankAccountVoHashMap.get(bankAcctId);
           if(bankAccountVo != null){
               String status = map.get("status");
               logger.info("[status={}]", status);
               bankAccountVo.setStatus(status);
           }
       }
       //
       Collection<BankAccountVo> values = stringBankAccountVoHashMap.values();
       ArrayList<BankAccountVo> bankAccountVos = new ArrayList<>();
       for (BankAccountVo value : values) {
           bankAccountVos.add(value);
       }

       return bankAccountVos;
   }


}
