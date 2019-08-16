package com.ewfresh.pay;

import com.ewfresh.commons.client.HttpDeal;
import com.ewfresh.commons.util.ItvJsonUtil;
import com.ewfresh.pay.dao.BillDao;
import com.ewfresh.pay.model.vo.BillVo;
import com.ewfresh.pay.util.Constants;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Value;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by wangziyuan on 2018/4/20.
 */

public class test {
   /* public void download(){
        ApplicationContext context = new ClassPathXmlApplicationContext("spring-test-common.xml");
        AliPayManager aliPayManager = (AliPayManager) context.getBean("aliPayManagerImpl");
        try {
            aliPayManager.DownLoadBillToAdd();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/
   @Value("${http_idgen}")
   private String ID_URL;
   private BillDao billDao;

   @Test
   public  void testsss(){

      HttpDeal httpDeal = new HttpDeal();
      Map<String, String> map = new HashMap<>();
      map.put(Constants.ID_GEN_KEY, Constants.ID_RECHARGE_VALUE);

      String idStr = httpDeal.post("${http_update_order}", map, null,null);
      HashMap<String, Object> hashMap = ItvJsonUtil.jsonToObj(idStr, new HashMap<String, Object>().getClass());
      Object o = hashMap.get(Constants.ENTITY);

       System.out.println(Long.valueOf(String.valueOf(o)));

   }
   @Test
   public void asdad(){
      BillVo eeee = billDao.getBillByBillFlow("eeee");
      System.out.println(eeee);

   }

}

