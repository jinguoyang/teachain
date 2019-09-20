package com.youzi.teaChain.dao;

import com.youzi.teaChain.bean.TTeachainRechargeLog;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface RechargeMapper {

    int createRechargeLog(Map param);

    List<TTeachainRechargeLog> getRechargeOrder(String uuid);

    int checkTradeNo(String tradeNo);

}
