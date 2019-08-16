package com.ewfresh.pay.manager;

import com.ewfresh.pay.model.BankAccount;
import com.ewfresh.pay.util.ResponseData;

/**
 * Created by 王耀辉 on 2018/4/19.
 */
public interface BankAccountManager {
    /**
     * Description:通过id删除一条银行卡绑定信息
     *
     * @param:id
     * 要删除的数据id
     * @author:wangyaohui
     * date: 11:54 2018/4/19
     *
     **/
    void delBankAccountById(ResponseData responseData, Integer id,Long uid,String code);
    /**
     * Description:增加一条银行卡绑定信息
     *
     * @param:record
     * 要增加的信息
     * @author:wangyaohui
     * date: 11:54 2018/4/19
     *
     **/
    void addBankAccount(ResponseData responseData,BankAccount record,String code);
    /**
     * Description:通过id查询一条银行卡绑定信息
     *
     * @param:id
     * 要查询的数据id
     * @author:wangyaohui
     * date: 11:54 2018/4/19
     *
     **/
    void getBankAccountById(ResponseData responseData,Long id,Short typ)throws Exception ;


    void   bankAccountCode(ResponseData responseData,Long uid,Integer bankId);


}
