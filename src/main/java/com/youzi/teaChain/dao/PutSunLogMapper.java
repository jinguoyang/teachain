package com.youzi.teaChain.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;

@Mapper
public interface PutSunLogMapper {

    void createPutSunLog(@Param(value = "uuid") String uuid, @Param(value = "sun") BigDecimal sun);

    BigDecimal calcTotalSun(@Param(value = "zero")String zero);

}
