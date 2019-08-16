package com.ewfresh.pay.dao;

import com.ewfresh.pay.model.Account;

import java.util.List;

public interface AccountDao {

    int deleteByPrimaryKey(Long id);

    int insert(Account record);

    int insertSelective(Account record);

    int updateByPrimaryKeySelective(Account record);

    int updateByPrimaryKey(Account record);




}