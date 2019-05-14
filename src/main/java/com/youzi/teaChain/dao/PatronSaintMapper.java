package com.youzi.teaChain.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface PatronSaintMapper {

    void insertUser(Map param);

    void updateUserByParam(Map param);

    Integer getRankByUuid(@Param(value = "uuid") String uuid);

    List<Map> getRankList();

    Map getPatronSaintUserByUuid(@Param(value = "uuid")String uuid);

}
