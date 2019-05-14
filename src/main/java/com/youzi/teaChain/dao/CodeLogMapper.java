package com.youzi.teaChain.dao;

import com.youzi.teaChain.bean.TTeachainCodeLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigInteger;

@Mapper
public interface CodeLogMapper {

    int checkApiCallCounts(@Param(value = "uuid") String uuid);

    int selectPhoneCode(@Param(value = "uuid") String uuid, @Param(value = "phone") BigInteger phone);

    void insertCodeLog(TTeachainCodeLog ttcl);

}
