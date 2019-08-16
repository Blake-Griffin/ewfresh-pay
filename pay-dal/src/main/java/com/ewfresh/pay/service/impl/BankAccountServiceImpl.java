package com.ewfresh.pay.service.impl;

import com.ewfresh.pay.dao.BankAccountDao;
import com.ewfresh.pay.model.BankAccount;
import com.ewfresh.pay.model.vo.BankAccountVo;
import com.ewfresh.pay.service.BankAccountService;
import com.ewfresh.pay.util.Constants;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created by 王耀辉 on 2018/4/19.
 */
@Service
public class BankAccountServiceImpl implements BankAccountService {
    @Autowired
    private BankAccountDao bankAccountDao;

    @Override
    @Transactional
    public void delBankAccountById(Integer id) {
         bankAccountDao.updateIsAbleById(id, Constants.SHORT_ZERO);
    }

    @Override
    @Transactional
    public int addBankAccount(BankAccount record) {
        bankAccountDao.updateIsDefByUserId(record);
        record.setIsDef(Constants.SHORT_ONE);
        return bankAccountDao.addBankAccount(record);
    }

    @Override
    public List<BankAccountVo> getBankAccountById(Long id, Short type) {
        return bankAccountDao.getBankAccountById(id,type);
    }

    @Override
    @Transactional
    public int updateBankAccountById(BankAccountVo bankAccount) {
        return bankAccountDao.updateBankAccountById(bankAccount);
    }

    /**
     * Description: 查询用户在快钱绑定的的所有有效的快捷银行卡
     * @author: JiuDongDong
     * @param userId 用户id
     * @return java.util.List<com.ewfresh.pay.model.BankAccount> 所有有效的快捷银行卡
     * date: 2018/9/22 13:41
     */
    @Override
    public List<BankAccount> getAllAbleBanksByUserId(Long userId) {
        List<BankAccount> bankAccountList = bankAccountDao.getAllAbleBanksByUserId(userId);
        return bankAccountList;
    }

    /**
     * Description: 根据银行卡号查询用户在快钱的该卡信息
     * @author: JiuDongDong
     * @param cardCode  卡号
     * @return com.ewfresh.pay.model.BankAccount 卡信息
     * date: 2018/9/22 12:11
     */
    @Override
    public BankAccount getBill99BankInfoByCardCode(String cardCode) {
        BankAccount bill99BankInfo = bankAccountDao.getBill99BankInfoByCardCode(cardCode);
        return bill99BankInfo;
    }

    /**
     * Description:  查询用户在快钱的默认银行卡
     * @author: JiuDongDong
     * @param userId  用户id
     * @return java.util.List<com.ewfresh.pay.model.BankAccount> 默认银行卡
     * date: 2018/9/21 17:07
     */
    @Override
    public List<BankAccount> getDefaultBankByUserId(Long userId) {
        List<BankAccount> defaultBanks = bankAccountDao.getBill99DefaultBankByUserId(userId);
        return defaultBanks;
    }

    /**
     * Description: 根据卡号更改快钱默认银行卡状态
     * @author: JiuDongDong
     * @param isDef     是否默认
     * @param cardCode  银行卡号
     * date: 2018/9/21 17:40
     */
    @Override
    public void updateIsDefaultByCardCode(Short isDef, String cardCode) {
        bankAccountDao.updateIsDefaultByCardCode(isDef, cardCode);
    }

    /**
     * @author: JiuDongDong
     * @param bankAccountList 待更新默认状态集合
     * date: 2018/9/22 10:51
     */
    @Override
    public void updateIsDefaultByCardCodes(List<BankAccount> bankAccountList) {
        bankAccountDao.updateIsDefaultByCardCodes(bankAccountList);
    }

    /**
     * Description: 根据卡号更改快钱银行卡失效状态
     * @author: JiuDongDong
     * @param isAble        是否可用
     * @param cardCode      卡号
     * date: 2018/9/22 12:39
     */
    @Override
    public void updateIsAbleByCardCode(Short isAble, String cardCode) {
        bankAccountDao.updateIsAbleByCardCode(isAble, cardCode);
    }

    /**
     * Description: 插入一条新的绑卡信息
     * @author: JiuDongDong
     * @param bankAccount  绑卡信息
     * @return int 插入的数量
     * date: 2018/9/22 13:20
     */
    @Override
    public int insertBankAccount(BankAccount bankAccount) {
        return bankAccountDao.insertBankAccount(bankAccount);
    }

    /**
     * Description: 根据卡号更改快钱银行卡状态
     * @author: JiuDongDong
     * @param bankAccount 绑卡信息
     * date: 2018/9/22 13:21
     */
    @Override
    public void updateBankAccountByCardCode(BankAccount bankAccount) {
        bankAccountDao.updateBankAccountByCardCode(bankAccount);
    }

    /**
     * Description: 更改2个卡信息
     * @author: JiuDongDong
     * @param bankAccountThis       卡1消息
     * @param bankAccountAnother    卡2消息
     * date: 2018/9/22 16:35
     */
    @Transactional
    @Override
    public void updateBothStatus(BankAccount bankAccountThis, BankAccount bankAccountAnother) {
        if (null != bankAccountThis) {
            bankAccountDao.updateBankAccountByCardCode(bankAccountThis);
        }
        if (null != bankAccountAnother) {
            bankAccountDao.updateBankAccountByCardCode(bankAccountAnother);
        }
    }

    /**
     * Description: 根据签约协议号查询用户快钱的有效的卡信息
     * @author: JiuDongDong
     * @param payToken  签约协议号
     * @return com.ewfresh.pay.model.BankAccount
     * date: 2018/9/27 15:45
     */
    @Override
    public BankAccount getBill99BankInfoByPayToken(String payToken) {
        BankAccount bankAccount = bankAccountDao.getBill99BankInfoByPayToken(payToken);
        return bankAccount;
    }

    /**
     * Description: 将用户某个手机号下绑定的快钱快捷银行卡置为失效
     * @author: JiuDongDong
     * @param userId    用户id
     * @param newMobilePhone   新手机号
     * @param oldMobilePhone   旧手机号
     * @param newPhoneChangedExpired   手机号变更失效：0否  1是
     * @param oldPhoneChangedExpired   手机号变更失效：0否  1是
     * date: 2018/11/5 15:02
     */
    @Transactional
    @Override
    public void updatePhoneChangedExpired(Long userId, String newMobilePhone, String oldMobilePhone,
                                          Short newPhoneChangedExpired, Short oldPhoneChangedExpired) {
        if (StringUtils.isNotBlank(newMobilePhone))
            bankAccountDao.updatePhoneChangedExpired(userId, newMobilePhone, newPhoneChangedExpired);
        if (StringUtils.isNotBlank(oldMobilePhone))
            bankAccountDao.updatePhoneChangedExpired(userId, oldMobilePhone, oldPhoneChangedExpired);
    }

    @Override
    public BankAccount getBankAccoutByBankCode(String cardCode) {
        return bankAccountDao.getBankAccoutByBankCode(cardCode);
    }

    /**
     * Description: 根据id 获取 mobilePhone
     * @author: ZhaoQun
     * @param id
     * @return:  string mobilePhone
     * date: 2018/11/6 10:00
     */
    @Override
    public String getMobilePhoneByid(Integer id) {
        return bankAccountDao.getMobilePhoneByid(id);
    }

    @Override
    public BankAccount getBankByCardCode(Long uid, Integer bankId) {
        return bankAccountDao.getBankByCardCode(uid,bankId);
    }

    /**
     * Description: 查询用户默认银行卡
     * @author: ZhaoQun
     * @param uid
     * date: 2019/4/24 11:20
     */
    @Override
    public List<BankAccount> getDefaultBankByUid(Long uid) {
        return bankAccountDao.getDefaultBankByUid(uid);
    }

    /**
     * Description: 修改用户默认银行卡信息
     * @author: ZhaoQun
     * @param bankAccountList
     * date: 2019/4/24 11:20
     */
    @Override
    public void updateIsDefaultById(List<BankAccount> bankAccountList) {
        bankAccountDao.updateIsDefaultById(bankAccountList);
    }
}
