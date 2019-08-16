package com.ewfresh.pay.redisDao;

import java.util.Map;

/**
 * description:
 * @author: Wangyaohui
 * date:   2018/4/16
 * 
 */

public interface OrderRedisDao {
     /**
      * description:根据订单id查询下单时的数据
      *
      * @author: wangyaohui
      * @param:orderId
      * 要查询的id
      * @return:Map
      * 返回的结果集
      * date: 2018/4/16
      */
     Map<String,String> getPayOrder(String orderId);

     /**
      * description:通过订单id删除记录
      *
      * @author: wangyaohui
      * @param:orderId
      * 订单orderId
      * date: 2018/4/16
      */
     void delPayOrder(String orderId);

     /**
      * description:订单修改失败之后放入redis离线更改订单状态
      * @author wangziyuan
      * @param map 修改订单所用参数
      */
     void modifyOrderStatusParam(Map<String,String> map);

     void supplementModifyDis(Map<String,String> map);

     void shopBond(Map<String,String> map);
}
