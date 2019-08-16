package com.ewfresh.pay.dao;

import com.ewfresh.pay.model.ShopCheck;

public interface ShopCheckDao {
    int deleteByPrimaryKey(Integer id);

    int insert(ShopCheck record);

    int insertSelective(ShopCheck record);

    ShopCheck selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(ShopCheck record);

    int updateByPrimaryKey(ShopCheck record);
}