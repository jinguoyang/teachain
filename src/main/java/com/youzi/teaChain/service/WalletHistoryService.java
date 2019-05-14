package com.youzi.teaChain.service;

import com.youzi.teaChain.dao.WalletHistoryMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class WalletHistoryService {
    
    private Logger log = LoggerFactory.getLogger(WalletHistoryService.class);

    @Resource
    private WalletHistoryMapper walletHistoryMapper;

    public void updateHistory() {
        try {
            walletHistoryMapper.delete2History();
            walletHistoryMapper.update2History();
            walletHistoryMapper.insertWH();
        } catch (Exception e) {
            e.printStackTrace();
            log.error("delete2History 更新昨天所有钱包历史出错" + e.getMessage());
        }
    }
}
