package com.youzi.teaChain.dao;

import com.youzi.teaChain.bean.TTeachainRechargeAlllog;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface RechargeAllLogMapper {

    int createRechargeAllLog(Map param);

    List<TTeachainRechargeAlllog> getRechargeAllLog(String uuid);

}
