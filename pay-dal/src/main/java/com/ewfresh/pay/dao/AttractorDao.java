package com.ewfresh.pay.dao;

import com.ewfresh.pay.model.Attractor;

public interface AttractorDao {
    int deleteByPrimaryKey(Integer id);

    int insert(Attractor record);

    int insertSelective(Attractor record);

    Attractor selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Attractor record);

    int updateByPrimaryKey(Attractor record);
}