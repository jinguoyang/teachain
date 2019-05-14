package com.youzi.teaChain.service;

import com.youzi.teaChain.dao.RechargeProfitLogMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Service
public class RechargeProfitLogService {
    private Logger log = LoggerFactory.getLogger(RechargeProfitLogService.class);

    @Resource
    private RechargeProfitLogMapper rechargeProfitLogMapper;

    public Boolean createRechargeProfitLog(Map param) {
        try {
            rechargeProfitLogMapper.createRechargeProfitLog(param);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("createRechargeProfitLog 保存钻石充值下级分润日志出错：" + e.getMessage());
            return false;
        }
    }

    public List getPersonProfitList(String uuid) {
        try {
            return rechargeProfitLogMapper.getPersonProfitList(uuid);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("getPersonProfitList 通过uuid获取分润出错：" + e.getMessage());
            return null;
        }
    }

}
