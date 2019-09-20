package com.youzi.teaChain.service;

import com.youzi.teaChain.bean.TTeachainRechargeAlllog;
import com.youzi.teaChain.dao.RechargeAllLogMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Service
public class RechargeAllLogService {
    private Logger log = LoggerFactory.getLogger(RechargeAllLogService.class);

    @Resource
    private RechargeAllLogMapper rechargeAllLogMapper;

    public int createRechargeAllLog(Map param) {
        try {
            param.put("id", 0);
            rechargeAllLogMapper.createRechargeAllLog(param);
            return (Integer) param.get("id");
        } catch (Exception e) {
            e.printStackTrace();
            log.error("rechargeAllLogMapper 保存全平台充值日志出错：" + e.getMessage());
            return 0;
        }
    }

    public List<TTeachainRechargeAlllog> getRechargeAllLog(String uuid) {
        try {
            return rechargeAllLogMapper.getRechargeAllLog(uuid);
        }catch (Exception e){
            e.printStackTrace();
            log.error("getRechargeOrder 根据用户查询全平台充值日志出错：" + e.getMessage());
            return null;
        }
    }

}
