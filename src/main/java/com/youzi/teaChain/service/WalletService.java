package com.youzi.teaChain.service;

import com.youzi.teaChain.bean.Wallet;
import com.youzi.teaChain.dao.UserMapper;
import com.youzi.teaChain.dao.WalletMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Map;

@Service
public class WalletService {

    private Logger log = LoggerFactory.getLogger(WalletService.class);

    @Resource
    private WalletMapper walletMapper;
    @Resource
    private UserMapper userMapper;

    public Boolean insertWalletByWXUser(Map param) {
        try {
            walletMapper.insertWalletByWXUser(param);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("insertWalletByWXUser 保存用户出错：" + e.getMessage());
            return false;
        }
    }

    public Wallet getWalletByUuid(String uuid) {
        try {
            return walletMapper.getWalletByUuid(uuid);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("getWalletByUuid 通过uuid查询钱包出错：" + e.getMessage());
            return null;
        }
    }

    public Boolean updateBalanceByUser(String uuid, BigDecimal coin, int type) {
        try {
            walletMapper.updateBalanceByUser(uuid, coin, type);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("updateBalanceByUser 更新用户茶币数量出错：" + e.getMessage());
            return false;
        }
    }

    public Boolean setPasswordByUuid(String password,String uuid) {
        try {
            walletMapper.setPasswordByUuid(password,uuid);
            userMapper.setHasPayPassword(uuid,1);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("setPasswordByUuid 设置支付密码出错：" + e.getMessage());
            return false;
        }
    }

    public BigDecimal getBalanceByUuid(String uuid) {
        try {
            return walletMapper.getBalanceByUuid(uuid);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("getBalanceByUuid 通过uuid获取茶币出错：" + e.getMessage());
            return null;
        }
    }

    public String selectUserByAddressPhone(String address, String phone) {
        try {
            return walletMapper.selectUserByAddressPhone(address, phone);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("selectUserByAddressPhone 通过钱包地址和手机号获取用户出错：" + e.getMessage());
            return null;
        }
    }

    public void updateRollBack(Wallet wallet) {
        try {
            walletMapper.updateRollBack(wallet);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("updateRollBack 通过实体类恢复钱包表出错：" + e.getMessage());
        }
    }

    public void setTransLock(String uuid,Integer transLock) {
        try {
            walletMapper.setTransLock(uuid,transLock);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("setTransLock 设置钱包表交易许可出错：" + e.getMessage());
        }
    }

    public void todaySunExtendYunCoin(int type) {
        try {
            walletMapper.todaySunExtendYunCoin(type);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("todaySunExtendYunCoin 调用阳光池发放云币存储过程出错：" + e.getMessage());
        }
    }

    public Boolean updateWalletBalanceFunc(String uuid, BigDecimal balance, int type){
        try {
            int aa = walletMapper.updateWalletBalanceFunc(uuid, balance, type);
            if (aa == 200) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("updateWalletBalanceFunc 通过事务修改CNY失败：" + e.getMessage());
            return false;
        }
    }
}