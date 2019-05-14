package com.youzi.teaChain.dao;

import com.youzi.teaChain.bean.Wallet;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.Map;

@Mapper
public interface WalletMapper {

    Wallet getWalletByUuid(String uuid);

    void insertWalletByWXUser(Map param);

    void updateBalanceByUser(@Param(value = "uuid") String uuid, @Param(value = "coin")BigDecimal coin,
                             @Param(value = "type") int type);

    void setPasswordByUuid(@Param(value = "password")String password,@Param(value = "uuid")String uuid);

    BigDecimal getBalanceByUuid(@Param(value = "uuid")String uuid);

    String selectUserByAddressPhone(@Param(value = "address")String address, @Param(value = "phone") String phone);

    void updateRollBack(Wallet wallet);

    void setTransLock(@Param(value = "uuid")String uuid,@Param(value = "transLock")Integer transLock);

    void todaySunExtendYunCoin(@Param(value = "type") int type);

    int updateWalletBalanceFunc(@Param(value = "uuid") String uuid, @Param(value = "balance") BigDecimal balance,
                                @Param(value = "type") int type);
}
