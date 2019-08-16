package com.ewfresh.pay.service;

import com.ewfresh.pay.model.BankAccount;
import com.ewfresh.pay.model.vo.BankAccountVo;

import java.util.List;

/**
 * Created by 王耀辉 on 2018/4/19.
 */
public interface BankAccountService {
    /**
     * Description:通过id删除一条银行卡绑定信息
     *
     * @param:id
     * 要删除的数据id
     * @author:wangyaohui
     * date: 11:54 2018/4/19
     *
     **/
    void delBankAccountById(Integer id);
    /**
     * Description:增加一条银行卡绑定信息
     *
     * @param:record
     * 要增加的信息
     * @author:wangyaohui
     * date: 11:54 2018/4/19
     *
     **/
    int addBankAccount(BankAccount record);
    /**
     * Description:通过id查询一条银行卡绑定信息
     *
     * @param:id
     * 要查询的数据id
     * @author:wangyaohui
     * date: 11:54 2018/4/19
     *
     **/
    List<BankAccountVo> getBankAccountById(Long id, Short type);
    /**
     * Description:通过id修改一条银行卡绑定信息
     *
     * @param:record
     * 要修改的数据
     * @author:wangyaohui
     * date: 11:54 2018/4/19
     *
     **/
    int updateBankAccountById(BankAccountVo bankAccount);

    List<BankAccount> getAllAbleBanksByUserId(Long userId);

    BankAccount getBill99BankInfoByCardCode(String cardCode);

    List<BankAccount> getDefaultBankByUserId(Long userId);

    void updateIsDefaultByCardCode(Short isDef, String cardCode);

    void updateIsDefaultByCardCodes(List<BankAccount> bankAccountList);

    void updateIsAbleByCardCode(Short isAble, String cardCode);

    int insertBankAccount(BankAccount bankAccount);

    void updateBankAccountByCardCode(BankAccount bankAccount);

    void updateBothStatus(BankAccount bankAccountThis, BankAccount bankAccountAnother);

    BankAccount getBill99BankInfoByPayToken(String payToken);

    void updatePhoneChangedExpired(Long userId, String newMobilePhone, String oldMobilePhone,
                                   Short newPhoneChangedExpired, Short oldPhoneChangedExpired);

    /**
     * Description:通过银行卡
     *
     * @param:record
     * 要修改的数据
     * @author:wangyaohui
     * date: 11:54 2018/4/19
     *
     **/
    BankAccount  getBankAccoutByBankCode(String cardCode);

    /**
     * Description: 根据id 获取 mobilePhone
     * @author: ZhaoQun
     * @param id
     * @return:  string mobilePhone
     * date: 2018/11/6 10:00
     */
    String getMobilePhoneByid(Integer id);

    BankAccount  getBankByCardCode( Long uid,  Integer bankId);

    /**
     * Description: 查询用户默认银行卡
     * @author: ZhaoQun
     * @param uid
     * date: 2019/4/24 11:20
     */
    List<BankAccount> getDefaultBankByUid(Long uid);

    /**
     * Description: 修改用户默认银行卡信息
     * @author: ZhaoQun
     * @param bankAccountList
     * date: 2019/4/24 11:20
     */
    void updateIsDefaultById(List<BankAccount> bankAccountList);
}
