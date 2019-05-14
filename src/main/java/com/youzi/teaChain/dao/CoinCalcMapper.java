package com.youzi.teaChain.dao;

import com.youzi.teaChain.bean.TTeachainCoinCalc;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CoinCalcMapper {

    TTeachainCoinCalc selectCoinCalc(@Param(value = "uuid") String uuid);

    void insertCoinCalcByUser(@Param(value = "uuid") String uuid);

    void updateTCC(TTeachainCoinCalc tTCC2);
}
