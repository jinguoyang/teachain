package com.youzi.teaChain.dao;

import com.youzi.teaChain.bean.TTeachainLogin;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigInteger;
import java.util.Map;

@Mapper
public interface LoginMapper {
    TTeachainLogin getLoginInfoByUuid(@Param(value = "uuid") String uuid);

    TTeachainLogin getLoginByDeviceId(@Param(value = "deviceId") String deviceId);

    TTeachainLogin checkLoginByOpenId(@Param(value = "openId") String openId);

    TTeachainLogin getLoginByPhone(@Param(value = "phone")String phone);

    int countDeviceId(@Param(value = "deviceId") String deviceId);

    void setOtherDeviceIdByOpenId(@Param(value = "openId")String openId,@Param(value = "deviceId")String deviceId);

    void setPasswordByUuid(@Param(value = "password")String password,@Param(value = "uuid")String uuid);

    void setStatusByUuid(@Param(value = "uuid")String uuid,@Param(value = "status")int status);

    boolean checkDeviceIdByOpenId(@Param(value = "openId")String openId,@Param(value = "deviceId")String deviceId,@Param(value = "ip")String ip);

    boolean checkDeviceIdByPhone(@Param(value = "phone")String phone,@Param(value = "deviceId")String deviceId,@Param(value = "ip")String ip);

    void setDeviceIdAndIpByOpenId(@Param(value = "openId")String openId,@Param(value = "deviceId")String deviceId,@Param(value = "ip")String ip);

    void setDeviceIdAndIpByPhone(@Param(value = "phone")String phone,@Param(value = "deviceId")String deviceId,@Param(value = "ip")String ip);

    void setStatusByDeviceId(@Param(value = "deviceId")String deviceId,@Param(value = "status")Integer status);

    void setUnionIdByOpenId(@Param(value = "openId")String openId,@Param(value = "unionId")String unionId);

    void insertTTeachainLogin(Map param);

    void updateLoginPhoneByUuid(@Param(value = "uuid") String uuid, @Param(value = "phone") BigInteger phone);

    Integer checkPhone(@Param(value = "phone")String phone);

}
