package com.youzi.teaChain.dao;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface ConfigMapper {

    List<Map> selectCalcPowerToday();

    Map selectTeaCoinToday();

    List<Map> selectAllNumberConfig();

    String selectConfig(String keyName);

    void addProvidePeriod();
}
