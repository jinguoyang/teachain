package com.youzi.teaChain.service;

import com.youzi.teaChain.bean.TTeachainTransLog;
import com.youzi.teaChain.dao.TransLogMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class TransLogService {
    
    private Logger log = LoggerFactory.getLogger(TransLogService.class);

    @Resource
    private TransLogMapper transLogMapper;

    public List selectTransLogByUuid(String uuid) {
        try {
            return transLogMapper.selectTransLogByUuid(uuid);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("selectTransLogByUuid 通过Uuid查询转账记录错误：" + e.getMessage());
            return null;
        }
    }

    public Boolean insertTransLog(TTeachainTransLog ttrl) {
        try {
            transLogMapper.insertTransLog(ttrl);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("insertTransLog 添加转账记录出错：" + e.getMessage());
            return false;
        }
    }

    public int selectWarningTrans(String uuid,String aimUuid,String createTime) {
        try {
            return transLogMapper.selectWarningTrans(uuid,aimUuid,createTime);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("insertTransLog 添加转账记录出错：" + e.getMessage());
            return 0;
        }
    }

}
