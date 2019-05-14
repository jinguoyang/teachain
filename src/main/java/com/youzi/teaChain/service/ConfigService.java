package com.youzi.teaChain.service;

import com.youzi.teaChain.dao.ConfigMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ConfigService {
    
    private Logger log = LoggerFactory.getLogger(ConfigService.class);

    @Resource
    private ConfigMapper configMapper;

    /**
     * 查询每日发放茶币小时数
     * @return
     */
    public List selectCalcPowerToday() {
        try {
            return configMapper.selectCalcPowerToday();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 查询每日发放茶币小时数
     * @return
     */
    public Map selectTeaCoinToday() {
        try {
            return configMapper.selectTeaCoinToday();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String selectConfig(String keyName) {
        try {
            return configMapper.selectConfig(keyName);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Map<String, BigDecimal> selectAllNumberConfig() {
        try {
            Map<String, BigDecimal> configMap = new HashMap<>();
            List<Map> config = configMapper.selectAllNumberConfig();
            for (Map eachConfig : config) {
                configMap.put(eachConfig.get("keyName").toString(), new BigDecimal(eachConfig.get("keyValue").toString()));
            }
            return configMap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Integer distributeTCCTime() {
        try {
            DateFormat df = new SimpleDateFormat("HH:mm:ss");
            String dateS = df.format(new Date());
            Date date = df.parse(dateS);
            List calcPowerTimeList = configMapper.selectCalcPowerToday();
            Map time1 = (Map) calcPowerTimeList.get(0);
            String hour1 = time1.get("keyValue").toString();
            Map time2 = (Map) calcPowerTimeList.get(1);
            String hour2 = time2.get("keyValue").toString();
            Map time3 = (Map) calcPowerTimeList.get(2);
            String hour3 = time3.get("keyValue").toString();
            Date dt1 = df.parse(hour1+":00:00");
            Date dt2 = df.parse(hour2+":00:00");
            Date dt3 = df.parse(hour3+":00:00");
            if (date.getTime() > dt3.getTime()){
                return 3;
            } else if (date.getTime() > dt2.getTime()) {
                return 2;
            } else if (date.getTime() > dt1.getTime()) {
                return 1;
            } else return 0;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("distributeTCCTime 更新用户茶币计算表出错" + e.getMessage());
            return -1;
        }
    }

    public void addProvidePeriod() {
        try {
            configMapper.addProvidePeriod();
        } catch (Exception e) {
            e.printStackTrace();
            log.error("addProvidePeriod 增加茶币发放期间出错" + e.getMessage());
        }
    }

}
