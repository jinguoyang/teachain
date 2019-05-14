package com.youzi.teaChain.dao;

import com.youzi.teaChain.bean.TTeachainPowerUp;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PowerUpMapper {
    void createPowerUp(TTeachainPowerUp map);

}
