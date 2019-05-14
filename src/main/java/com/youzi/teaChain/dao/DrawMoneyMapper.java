package com.youzi.teaChain.dao;

import com.youzi.teaChain.bean.DrawMoneyOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface DrawMoneyMapper {
    void createDrawOrder(@Param(value = "uuid")String uuid, @Param(value = "count")BigDecimal count, @Param(value = "totalBrokerage")BigDecimal totalBrokerage, @Param(value = "wechatNum")String wechatNum);

    BigDecimal getCurrentDrawMoney(@Param(value = "uuid")String uuid);

    List<DrawMoneyOrder> getDrawMoneyOrder(@Param(value = "uuid")String uuid);

    void setDelFlagDrawOrder(@Param(value = "id")String id);
}

