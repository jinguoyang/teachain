package com.youzi.teaChain.dao;

import com.youzi.teaChain.bean.TTeachainTransLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface TransLogMapper {

    List<Map<String, Object>> selectTransLogByUuid(@Param(value = "uuid") String uuid);

    void insertTransLog(TTeachainTransLog ttrl);

    Integer selectWarningTrans(@Param(value = "uuid") String uuid,@Param(value = "aimUuid") String aimUuid,@Param(value = "createTime") String createTime);

}
