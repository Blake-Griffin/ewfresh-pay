package com.ewfresh.pay.dao;

import com.ewfresh.pay.model.AccCheckMistake;

public interface AccCheckMistakeDao {
    int deleteByPrimaryKey(Integer id);

    int insert(AccCheckMistake record);

    int insertSelective(AccCheckMistake record);

    AccCheckMistake selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(AccCheckMistake record);

    int updateByPrimaryKey(AccCheckMistake record);
}