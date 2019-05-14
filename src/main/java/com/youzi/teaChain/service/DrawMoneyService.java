package com.youzi.teaChain.service;

import com.youzi.teaChain.bean.DrawMoneyOrder;
import com.youzi.teaChain.bean.TUser;
import com.youzi.teaChain.dao.ConfigMapper;
import com.youzi.teaChain.dao.DrawMoneyMapper;
import com.youzi.teaChain.dao.UserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;

@Service
public class DrawMoneyService {
    private Logger log = LoggerFactory.getLogger(DrawMoneyService.class);

    @Resource
    private DrawMoneyMapper drawMoneyMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private ConfigMapper configMapper;

    public Integer createDrawOrder(String uuid,BigDecimal count,String wechatNum) {
        try {
            TUser tUser = userMapper.getTUserByUuid(uuid);
            BigDecimal totalMoney = tUser.getTotalMoney();
            String wechatBrokerageFix = configMapper.selectConfig("wechatBrokerageFix");
            String wechatBrokeragePercent = configMapper.selectConfig("wechatBrokeragePercent");
            BigDecimal brokerage1 = new BigDecimal(wechatBrokerageFix);
            BigDecimal brokerage2 = new BigDecimal(wechatBrokeragePercent);
            BigDecimal totalBrokerage = count.multiply(brokerage2).add(brokerage1);
            if(count.compareTo(totalMoney)>0){
                log.info("createDrawOrder 用户余额不足");
                return 0;
            }else{
                drawMoneyMapper.createDrawOrder(uuid,count,totalBrokerage,wechatNum);
                userMapper.changeTotalMoney(uuid,count,1);
                return 1;
            }
        }catch (Exception e){
            e.printStackTrace();
            log.error("createDrawOrder 创建订单出错：" + e.getMessage());
            return -1;
        }
    }

    public BigDecimal getCurrentDrawMoney(String uuid) {
        try {
            BigDecimal currentDrawMoney = drawMoneyMapper.getCurrentDrawMoney(uuid);
            if(currentDrawMoney==null){
                return new BigDecimal("0");
            }
            return currentDrawMoney;
        }catch (Exception e){
            e.printStackTrace();
            log.error("getCurrentDrawMoney 查询用户当前提现金额出错：" + e.getMessage());
            return null;
        }
    }

    public List<DrawMoneyOrder> getDrawOrder(String uuid) {
        try {
            return drawMoneyMapper.getDrawMoneyOrder(uuid);
        }catch (Exception e){
            e.printStackTrace();
            log.error("getDrawOrder 查询用户提现订单出错：" + e.getMessage());
            return null;
        }
    }

    public Boolean setDelFlagDrawOrder(String id) {
        try {
            drawMoneyMapper.setDelFlagDrawOrder(id);
            return true;
        }catch (Exception e){
            e.printStackTrace();
            log.error("setDelFlagDrawOrder 逻辑删除用户提现订单出错：" + e.getMessage());
            return false;
        }
    }

}
