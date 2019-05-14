package com.youzi.teaChain.service;

import com.youzi.teaChain.bean.TTeachainCoinCalc;
import com.youzi.teaChain.dao.CoinCalcMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class CoinCalcService {
    
    private Logger log = LoggerFactory.getLogger(CoinCalcService.class);

    @Resource
    private CoinCalcMapper coinCalcMapper;

    public TTeachainCoinCalc selectCoinCalc(String uuid) {
        try {
            TTeachainCoinCalc tTeachainCoinCalc = coinCalcMapper.selectCoinCalc(uuid);
            return tTeachainCoinCalc;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("selectCoinCalc 查询用户茶币计算表出错" + e.getMessage());
            return null;
        }
    }

    public void insertCoinCalcByUser(String uuid) {
        try {
            coinCalcMapper.insertCoinCalcByUser(uuid);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("insertCoinCalcByUser 添加用户茶币计算表出错" + e.getMessage());
        }
    }

    public void updateTCC(TTeachainCoinCalc tTCC2) {
        try {
            coinCalcMapper.updateTCC(tTCC2);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("updateTCC 更新用户茶币计算表出错" + e.getMessage());
        }
    }
}
