package com.youzi.teaChain.service;

import com.youzi.teaChain.dao.PowerUpMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;

@Service
public class PowerUpService {
    private Logger log = LoggerFactory.getLogger(PowerUpService.class);

    @Resource
    private PowerUpMapper powerUpMapper;

    public Boolean createPowerUp(Map map) {
        try {
//            powerUpMapper.createPowerUp(map);
            return true;
        }catch (Exception e){
            e.printStackTrace();
            log.error("createPowerUp 增加算力出错：" + e.getMessage());
            return false;
        }
    }
}
