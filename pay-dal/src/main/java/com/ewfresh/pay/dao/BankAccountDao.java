package com.ewfresh.pay.dao;

import com.ewfresh.pay.model.BankAccount;
import com.ewfresh.pay.model.vo.BankAccountVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface BankAccountDao {
    /**
     * Description:通过id删除一条银行卡绑定信息
     *
     * @param:id
     * 要删除的数据id
     * @author:wangyaohui
     * date: 11:54 2018/4/19
     *
     **/
    int delBankAccountById(Integer id);
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
     * Description:通过用户id查询其银行卡绑定信息
     *
     * @param:id
     * 要查询的数据id
     * @author:wangyaohui
     * date: 11:54 2018/4/19
     *
     **/
    List<BankAccountVo> getBankAccountById(@Param("id") Long id,@Param("type") Short type);
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
    // 查询用户在快钱的所有银行卡   jiudongdong
    List<BankAccount> getAllBill99BanksByUserId(Long userId);
    // 查询用户在快钱绑定的的所有有效的快捷银行卡   jiudongdong
    List<BankAccount> getAllAbleBanksByUserId(Long userId);
    // 根据银行卡号查询用户快钱的卡信息   jiudongdong
    BankAccount getBill99BankInfoByCardCode(String cardCode);
    // 查询用户在快钱的默认银行卡   jiudongdong
    List<BankAccount> getBill99DefaultBankByUserId(Long userId);
    // 根据卡号更改快钱默认银行卡状态   jiudongdong
    void updateIsDefaultByCardCode(@Param("isDef") Short isDef, @Param("cardCode") String cardCode);
    // 批量更新-根据卡号更改快钱默认银行卡状态   jiudongdong
    void updateIsDefaultByCardCodes(List<BankAccount> bankAccountList);
    // 根据卡号更改快钱银行卡失效状态   jiudongdong
    void updateIsAbleByCardCode(@Param("isAble") Short isAble, @Param("cardCode") String cardCode);
    // 插入一条新的绑卡信息      jiudongdong
    int insertBankAccount(BankAccount bankAccount);
    // 根据卡号更改快钱银行卡状态      jiudongdong
    void updateBankAccountByCardCode(BankAccount bankAccount);
    // 根据签约协议号查询用户快钱的有效的卡信息   jiudongdong
    BankAccount getBill99BankInfoByPayToken(String payToken);
    // 将用户某个手机号下绑定的快钱快捷银行卡置为失效   jiudongdong
    void updatePhoneChangedExpired(@Param("userId") Long userId, @Param("mobilePhone") String mobilePhone, @Param("phoneChangedExpired") Short phoneChangedExpired);
    //根据id更改银行卡失效状态 wangyaohui
    void   updateIsAbleById(@Param("id")Integer id,@Param("isAble") Short isAble);
    //根据用户更改银行卡默认
    void updateIsDefByUserId(BankAccount record);
    //根据银行卡查询信息
    BankAccount  getBankAccoutByBankCode(String cardCode);

    //根据id 获取 mobilePhone    zhaoqun
    String getMobilePhoneByid(Integer id);
    //
    BankAccount  getBankByCardCode(@Param("uid") Long uid, @Param("bankId") Integer bankId);

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