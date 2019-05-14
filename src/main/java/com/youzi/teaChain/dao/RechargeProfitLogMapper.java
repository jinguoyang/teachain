package com.youzi.teaChain.dao;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface RechargeProfitLogMapper {

    void createRechargeProfitLog(Map param);

    List<Map> getPersonProfitList(String uuid);

}
