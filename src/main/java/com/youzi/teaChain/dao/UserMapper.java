package com.youzi.teaChain.dao;

import com.youzi.teaChain.bean.TUser;
import com.youzi.teaChain.bean.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;

@Mapper
public interface UserMapper {

    TUser getTUserByUuid(String uuid);

    String getIdByUuid(String uuid);

    String getUuidById(@Param(value = "id") int id);

    User getUserByName(String username);

    TUser getTUserByName(String username);

    int checkUserName(@Param(value = "phone") String phone);

    int checkWechatNum(@Param(value = "wechatNum") String wechatNum);

    int checkIdCardNum(@Param(value = "idCardNum") String idCardNum);

    void insertTUserByWXUser(Map param);

    void updateWXInfo(Map param);

    void setIdCard(@Param(value = "uuid") String uuid,@Param(value = "idCardNum") String idCardNum,@Param(value = "idCardName") String idCardName);

    void setWechatNum(@Param(value = "uuid") String uuid,@Param(value = "wechatNum") String wechatNum);

    void setStatusByDeviceId(@Param(value = "deviceId")String deviceId,@Param(value = "status")Integer status);

    void setAwardStateByUuid(@Param(value = "uuid")String uuid);

    void setHasPayPassword(@Param(value = "uuid")String uuid,@Param(value = "code")Integer code);

    void setHasLoginPassword(@Param(value = "uuid")String uuid,@Param(value = "code")Integer code);

    List<Map> selectCalcUser(@Param(value = "minCalcPower") BigDecimal minCalcPower,
                              @Param(value = "minTeaCoin") BigDecimal minTeaCoin);

    List<Map> getRankListByCalc();

    List<Map> getRankListByRecharge();

    List<Map> getRankListByTCC();

    List<Map> getCalcRelation(String uuid);

    BigDecimal selectTotalCalcPower(@Param(value = "minCalcPower") BigDecimal minCalcPower,
                                    @Param(value = "minTeaCoin")  BigDecimal minTeaCoin);

    void updateUserPhoneByUuid(@Param(value = "uuid") String uuid, @Param(value = "phone") BigInteger phone);

    void changeTotalMoney(@Param(value = "uuid") String uuid, @Param(value = "count") BigDecimal count,
                          @Param(value = "type") int type);

    void updateCalcPower(@Param(value = "uuid") String uuid, @Param(value = "count") BigDecimal count,
                         @Param(value = "type") int type, @Param(value = "temp") int temp);

    void setTradeLicense(String uuid);

    void setSuperior(@Param(value = "uuid") String uuid, @Param(value = "superior") int superior);

    void setHasFirstTCC(@Param(value = "uuid") String uuid, @Param(value = "hasFirstTCC") int hasFirstTCC);

    Integer getInviterCount(@Param(value = "id") int id);

    void setDiamond(@Param(value = "uuid") String uuid, @Param(value = "diamond") int diamond);

    void setVipAndDay(@Param(value = "uuid") String uuid, @Param(value = "vipLevel") int vipLevel, @Param(value = "vipValidDay") int vipValidDay);

    int updateDiamondFunc(@Param(value = "uuid") String uuid, @Param(value = "diamond") BigDecimal diamond,
                          @Param(value = "type") int type);

    int updateCNYFunc(@Param(value = "uuid") String uuid, @Param(value = "cny") BigDecimal cny,
                      @Param(value = "type") int type);

    List<Map> getLowerRelation(@Param(value = "uuid") String uuid, @Param(value = "type") int type);
}
