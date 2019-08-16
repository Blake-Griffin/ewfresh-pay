package com.ewfresh.pay.dao;

import com.ewfresh.pay.model.AccCheck;

import java.util.List;
import java.util.Map;

public interface AccCheckDao {
    int deleteByPrimaryKey(Integer id);

    int insert(AccCheck record);

    int insertSelective(AccCheck record);

    AccCheck selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(AccCheck record);

    int updateByPrimaryKey(AccCheck record);

    List<AccCheck> getAccCheckByParam(Map<String, Object> paramMap);
}