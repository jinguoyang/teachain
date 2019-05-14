package com.youzi.teaChain.service;

import com.youzi.teaChain.bean.TTeachainCodeLog;
import com.youzi.teaChain.dao.CodeLogMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigInteger;

@Service
public class CodeLogService {
    
    private Logger log = LoggerFactory.getLogger(CodeLogService.class);

    @Resource
    private CodeLogMapper codeLogMapper;

    public Boolean checkApiCallCounts(String uuid) {
        try {
            int a = codeLogMapper.checkApiCallCounts(uuid);
            return a > 3;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("checkApiCallCounts 检查手机号发送验证码次数发生错误：" + e.getMessage());
            return true;
        }
    }

    public int selectPhoneCode(String uuid, BigInteger phone) {
        try {
            return codeLogMapper.selectPhoneCode(uuid, phone);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("selectPhoneCode 查询手机号验证码时发生错误：" + e.getMessage());
            return 0;
        }
    }

    public Boolean insertCodeLog(TTeachainCodeLog ttcl) {
        try {
            codeLogMapper.insertCodeLog(ttcl);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("insertCodeLog 添加手机验证码：" + e.getMessage());
            return false;
        }
    }

}
