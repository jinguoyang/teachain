package com.youzi.teaChain.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ShareMapper {

    int selectShareUserId(@Param(value = "openId") String openId, @Param(value = "unionId") String unionId);

}
