package com.youzi.teaChain.service;

import com.youzi.teaChain.bean.TTeachainOrdersTrade;
import com.youzi.teaChain.dao.CurrencyTradeMapper;
import com.youzi.teaChain.dao.UserMapper;
import com.youzi.teaChain.dao.WalletMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;

@Service
public class CurrencyTradeService {
    private Logger log = LoggerFactory.getLogger(CurrencyTradeService.class);

    @Resource
    private CurrencyTradeMapper currencyTradeMapper;
    @Resource
    private WalletMapper walletMapper;
    @Resource
    private UserMapper userMapper;

    public Boolean addTradeOrder(TTeachainOrdersTrade tTeachainOrdersTrade,BigDecimal brokerage1,BigDecimal licenseCost) {
        try {
            BigDecimal brokerage2 = tTeachainOrdersTrade.getBrokerage2();
            String uuid = tTeachainOrdersTrade.getHomeUuid();
            BigDecimal count = tTeachainOrdersTrade.getCount();
            BigDecimal amount = tTeachainOrdersTrade.getAmount();
            currencyTradeMapper.addTradeOrder(tTeachainOrdersTrade);
            if (tTeachainOrdersTrade.getType() == 1){
                userMapper.changeTotalMoney(uuid,amount.add(brokerage1).add(brokerage2),1);
            }else {
                walletMapper.updateBalanceByUser(uuid,count.add(brokerage1).add(brokerage2).add(licenseCost),1);
            }
            return true;
        }catch (Exception e){
            e.printStackTrace();
            log.error("addTradeOrder 添加交易失败：" + e.getMessage());
            return false;
        }
    }

    public TTeachainOrdersTrade getTradeOrderById(String id) {
        try {
            return currencyTradeMapper.getTradeOrderById(id);
        }catch (Exception e){
            e.printStackTrace();
            log.error("getTradeOrderById 查询交易失败：" + e.getMessage());
            return null;
        }
    }

    public List<TTeachainOrdersTrade> getTradeOrderByStatus(int index, int pageSize, int type,String uuid) {
        try {
            return currencyTradeMapper.getTradeOrderByStatus(type,index,pageSize,uuid);
        }catch (Exception e){
            e.printStackTrace();
            log.error("getTradeOrderByStatus 查询交易订单失败：" + e.getMessage());
            return null;
        }
    }

    public List<TTeachainOrdersTrade> getTradeOrderByUuid(String uuid, int status, int index, int pageSize) {
        try {
            return currencyTradeMapper.getTradeOrderByUuid(uuid,status,index,pageSize);
        }catch (Exception e){
            e.printStackTrace();
            log.error("getTradeOrderByUuid 查询交易订单失败：" + e.getMessage());
            return null;
        }
    }

    public Integer getTradeOrderCountByStatus(String uuid,int type) {
        try {
            return currencyTradeMapper.getTradeOrderCountByStatus(uuid,type);
        }catch (Exception e){
            e.printStackTrace();
            log.error("getTradeOrderCount 查询交易数量失败：" + e.getMessage());
            return null;
        }
    }

    public Integer getTradeOrderCountByUuid(String uuid,int status) {
        try {
            return currencyTradeMapper.getTradeOrderCountByUuid(uuid,status);
        }catch (Exception e){
            e.printStackTrace();
            log.error("getTradeOrderCountByUuid 查询交易数量失败：" + e.getMessage());
            return null;
        }
    }

    public Boolean stopTradeOrder(String orderId,long type,String uuid) {
        try {
            TTeachainOrdersTrade tTeachainOrdersTrade = currencyTradeMapper.getTradeOrderById(orderId);
            BigDecimal totalMoney = tTeachainOrdersTrade.getAmount().add(tTeachainOrdersTrade.getBrokerage2());
            BigDecimal totalTCC = tTeachainOrdersTrade.getCount().add(tTeachainOrdersTrade.getBrokerage2());
            if (type==1){
                userMapper.changeTotalMoney(uuid,totalMoney,0);
            }else if(type==2){
                walletMapper.updateBalanceByUser(uuid,totalTCC,0);
            }else {
                return false;
            }
            currencyTradeMapper.stopTradeOrder(orderId);
            return true;
        }catch (Exception e){
            e.printStackTrace();
            log.error("stopTradeOrder 暂停交易失败：" + e.getMessage());
            return false;
        }
    }

    public Integer endTradeOrder(String uuid,String home_uuid, String id,BigDecimal amount,BigDecimal count,BigDecimal brokerage,Long type) {
        try {
            Integer lock = currencyTradeMapper.getTradeOrderLock(id);
            if(lock==0){
                currencyTradeMapper.setTradeOrderLock(id,1);
                if (type == 1){
                    userMapper.changeTotalMoney(uuid,amount,0);
                    walletMapper.updateBalanceByUser(home_uuid,count,0);
                    walletMapper.updateBalanceByUser(uuid,count.add(brokerage),1);
                }else if (type == 2) {
                    userMapper.changeTotalMoney(home_uuid, amount,0);
                    userMapper.changeTotalMoney(uuid, amount.add(brokerage),1);
                    walletMapper.updateBalanceByUser(uuid, count,0);
                }
                currencyTradeMapper.endTradeOrder(uuid, id);
                return 1;
            }else{
                return 0;
            }
        }catch (Exception e){
            currencyTradeMapper.setTradeOrderLock(id,0);
            e.printStackTrace();
            log.error("endTradeOrder 完成交易失败：" + e.getMessage());
            return -1;
        }
    }
}
