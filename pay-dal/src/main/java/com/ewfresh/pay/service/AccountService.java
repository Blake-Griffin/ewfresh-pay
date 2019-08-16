package com.ewfresh.pay.service;

import com.ewfresh.pay.model.Account;

import java.util.List;

/**
 * Description:
 * @author DuanXiangming
 * Date 2018/4/11
 */
public interface AccountService {


    List<Account> getAccountsByUid(Long uid);

}
