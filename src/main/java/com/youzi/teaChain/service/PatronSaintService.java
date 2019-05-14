package com.youzi.teaChain.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.youzi.teaChain.bean.TUser;
import com.youzi.teaChain.dao.PatronSaintMapper;
import com.youzi.teaChain.dao.PutSunLogMapper;
import com.youzi.teaChain.dao.UserMapper;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class PatronSaintService {
    private Logger log = LoggerFactory.getLogger(PatronSaintService.class);

    @Resource
    private PatronSaintMapper patronSaintMapper;
    @Resource
    private PutSunLogMapper putSunLogMapper;
    @Resource
    private UserMapper userMapper;

    public Map getRedisUser(Jedis jedis ,String uuid){
        Iterator<String> iter = jedis.hkeys("user:"+uuid).iterator();
        Map<String,Object> user = new HashMap<>();
        while (iter.hasNext()){
            String key = iter.next();
            String value = jedis.hget("user:"+uuid, key);
            if(!key.equals("golden")){
                BigDecimal valueC = new BigDecimal(value);
                user.put(key,valueC);
            }else {
                user.put(key,value);
            }
        }
        return user;
    }

    public int setUserAfterChangeDogs(Jedis jedis,String uuid,int[] levelArrays){
        Map user = getRedisUser(jedis,uuid);
        int allLevel = getAllLevel(levelArrays);
        int maxLevel = getMaxLevel(levelArrays);
        BigDecimal genTea = getGenTeaSpeed(allLevel);
        jedis.hset("user:" + uuid, "allLevel", String.valueOf(allLevel));
        jedis.hset("user:" + uuid, "genTea", String.valueOf(genTea));
        if (Integer.valueOf(user.get("maxLevel").toString())<maxLevel) {
            jedis.hset("user:" + uuid, "maxLevel", String.valueOf(maxLevel));
            int canBuyLevel = maxLevel<=5?1:maxLevel-4;
            jedis.hset("user:" + uuid, "canBuyLevel", String.valueOf(canBuyLevel));
            synRedisToMYSQL(jedis,uuid);
            return maxLevel;
        }
        return 0;
    }

    private int getAllLevel(int[] levelArrays){
        int allLevel = 0;
        for (int i:levelArrays){
            if(i != -1){
                allLevel+=i;
            }
        }
        return allLevel;
    }

    private int getMaxLevel(int[] levelArrays){
        int maxLevel = 0;
        for (int i:levelArrays){
            if (i >maxLevel){
                maxLevel = i;
            }
        }
        return maxLevel;
    }

    public BigDecimal getGenTeaSpeed(int allLevel){
        BigDecimal level = new BigDecimal(allLevel);
        return (level.pow(2).divide(new BigDecimal(500), 3, BigDecimal.ROUND_HALF_UP).add(level)).multiply(new BigDecimal(10)).setScale(0,BigDecimal.ROUND_HALF_UP);
    }

    public Boolean insertUser(Map param) {
        try {
            patronSaintMapper.insertUser(param);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("insertUser 添加守护神用户时出错：" + e.getMessage());
            return false;
        }
    }

    public Boolean updateUserByParam(Map param) {
        try {
            patronSaintMapper.updateUserByParam(param);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("updateUserByParam 更新守护神用户时出错：" + e.getMessage());
            return false;
        }
    }

    public int getRankByUuid(String uuid) {
        try {
            return patronSaintMapper.getRankByUuid(uuid);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("getRankByUuid 获取用户排行榜排名出错：" + e.getMessage());
            return 0;
        }
    }

    public List<Map> getRankList() {
        try {
            return patronSaintMapper.getRankList();
        } catch (Exception e) {
            e.printStackTrace();
            log.error("getRankList 获取排行榜出错：" + e.getMessage());
            return null;
        }
    }

    public void createPutSunLog(String uuid,BigDecimal sun) {
        try {
             putSunLogMapper.createPutSunLog(uuid,sun);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("createPutSunLog 创建投放阳光日志出错：" + e.getMessage());
        }
    }

    public Map getPatronSaintUserByUuid(String uuid){
        try {
            Map patronSaint = patronSaintMapper.getPatronSaintUserByUuid(uuid);
            if (patronSaint==null) return null;
            patronSaint.put("golden",patronSaint.get("golden").toString());
            DateFormat dayFormat = new SimpleDateFormat("yyyyMMdd");
            int nowDay = Integer.valueOf(dayFormat.format(new Date()));

            if (nowDay < 20190124 && patronSaint.get("putDay") != null && patronSaint.get("putDay").toString().equals("20190123")) {

            } else {
                if (patronSaint.get("putDay") == null || nowDay != Integer.parseInt(patronSaint.get("putDay").toString())) {
                    patronSaint.put("todaySun", 0);
                }
            }

            return patronSaint;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("getPatronSaintUserByUuid 获取守护神人物信息出错：" + e.getMessage());
            return null;
        }
    }

    public void genMoneyTeaByTime(Jedis jedis,String uuid,Map dogsMap){
        int vipLevel = Integer.valueOf(jedis.hget("user:" + uuid, "vip"));
        BigDecimal limitTime = new BigDecimal(3);// 普通玩家 最大离线时长 6小时
        if (vipLevel == 1){
            limitTime = new BigDecimal(8); // vip 最大离线时长 8小时
        }
        BigDecimal limitTime5 = limitTime.multiply(new BigDecimal(720)); // 离线每5秒生成数
        BigDecimal lastOnlineTime = new BigDecimal(jedis.hget("user:"+uuid,"onlineTime")).divide(new BigDecimal(1000),0,BigDecimal.ROUND_DOWN);
        BigDecimal nowTime = new BigDecimal(new Date().getTime());
        BigDecimal nowTime10 = nowTime.divide(new BigDecimal(1000),0,BigDecimal.ROUND_DOWN); // 截去毫秒

        // 生成金币
        if (dogsMap == null){
            String dogs = jedis.get("dogs:"+uuid);
            dogsMap = JSONObject.fromObject(dogs);
        }
        int[] levelArrays = JSON.parseObject(dogsMap.get("level").toString(), int[].class);
        BigDecimal[] timeArrays = JSON.parseObject(dogsMap.get("time").toString(), BigDecimal[].class);
        BigDecimal golden = new BigDecimal(jedis.hget("user:"+uuid,"golden"));
        String genMoney = jedis.hget("globalDeal","genMoneyMap");
        JSONArray genMoneyList = JSON.parseArray(genMoney);
        for (int i=0;i<levelArrays.length;i++) {
            if (levelArrays[i]==0||levelArrays[i]==-1){
                continue;
            }
            BigDecimal createTime = timeArrays[i].divide(new BigDecimal(1000),0,BigDecimal.ROUND_DOWN);
            BigDecimal nowCount = nowTime10.subtract(createTime).divide(new BigDecimal(5),0,BigDecimal.ROUND_DOWN);
            BigDecimal lastCount = lastOnlineTime.subtract(createTime).divide(new BigDecimal(5),0,BigDecimal.ROUND_DOWN);
            BigDecimal genCount = nowCount.subtract(lastCount);
            if (genCount.compareTo(limitTime5)>0){
                genCount = limitTime5;
            }
            golden = golden.add(genMoneyList.getBigDecimal(levelArrays[i]-1).multiply(genCount));
        }
        jedis.hset("user:"+uuid,"golden",golden.toString());

        // 生成茶叶
        BigDecimal genTeaSpeed = new BigDecimal(jedis.hget("user:"+uuid,"genTea"));
        BigDecimal genTeaMultiple = new BigDecimal(jedis.hget("user:"+uuid,"genTeaMultiple"));
        BigDecimal tea = new BigDecimal(jedis.hget("user:"+uuid,"tea"));
        // 取整分钟
        BigDecimal createTime = new BigDecimal(jedis.hget("user:"+uuid,"createTime"))
                .divide(new BigDecimal(100000),0,BigDecimal.ROUND_DOWN).multiply(new BigDecimal(100));
        BigDecimal nowCount = nowTime10.subtract(createTime).divide(new BigDecimal(60),0,BigDecimal.ROUND_DOWN);
        BigDecimal lastCount = lastOnlineTime.subtract(createTime).divide(new BigDecimal(60),0,BigDecimal.ROUND_DOWN);
        BigDecimal genCount = nowCount.subtract(lastCount);
        if (genCount.compareTo(limitTime.multiply(new BigDecimal(60))) > 0) {
            genCount = limitTime.multiply(new BigDecimal(60));
        }
        BigDecimal allGenTeaSpeed = genTeaSpeed.add(genTeaSpeed.multiply(genTeaMultiple));
        BigDecimal calcTea = genCount.multiply(allGenTeaSpeed.divide(new BigDecimal(60),2,BigDecimal.ROUND_HALF_UP));
        tea = tea.add(calcTea);
        jedis.hset("user:"+uuid,"tea",tea.toString());
        jedis.hset("user:"+uuid,"onlineTime",nowTime.toString());
    }

    public BigDecimal calcTotalSun(){
        try {
//            long current = System.currentTimeMillis();
//            long zero = current/(1000*3600*24)*(1000*3600*24) - TimeZone.getDefault().getRawOffset();
//            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//            String a = simpleDateFormat.format(zero);
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            String date = df.format(new Date());
            BigDecimal totalSun = putSunLogMapper.calcTotalSun(date);
            if(totalSun==null){totalSun = new BigDecimal(0);}
            return totalSun;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("calcTotalSun 计算总奖池出错：" + e.getMessage());
            return null;
        }
    }

    public void synDiamondFromMYSQL(Jedis jedis, String uuid){
        TUser tUser = userMapper.getTUserByUuid(uuid);
        Integer diamond = tUser.getDiamond();
        jedis.hset("user:"+uuid,"diamond",diamond.toString());
    }

    public void synRedisToMYSQL(Jedis jedis, String uuid){
        Map userRedis = jedis.hgetAll("user:"+uuid);
        userRedis.put("uuid",uuid);
        updateUserByParam(userRedis);
    }

    public boolean setHoleAndSpeed(String uuid, int target,int[] levelArrays, Map dogsMap, Jedis jedis) {
        try {
            levelArrays[target] = 0;
            dogsMap.put("level", levelArrays);
            jedis.set("dogs:" + uuid, JSON.toJSON(dogsMap).toString());
            // 茶叶额外加速 = vip0.1 + 开坑固定倍数
            BigDecimal[] vipSpeed = JSON.parseObject(jedis.hget("globalDeal","vipSpeed"),BigDecimal[].class);
            BigDecimal[] speedUp = JSON.parseObject(jedis.hget("globalDeal","speedUpByHole"),BigDecimal[].class);
            int vip = Integer.parseInt(jedis.hget("user:" + uuid, "vip"));
            BigDecimal genTeaMultiple = vipSpeed[vip].add(speedUp[target - 5]);
            jedis.hset("user:" + uuid, "genTeaMultiple", String.valueOf(genTeaMultiple));
            return true;
        }catch (Exception e){
            e.printStackTrace();
            log.error("setHoleAndSpeed 开坑加速异常：" + e.getMessage());
            return false;
        }
    }

    /**
     * 首次登陆守护神游戏 初始化
     * 2019-01-18 10:15:39
     * @param uuid 用户uuid
     * @return void
     */
    public void initUser(String uuid,Jedis jedis) {
        String time = String.valueOf(new Date().getTime());
        Map<String,Object> dogs = new HashMap<>();
        int[] levelList = {1, 0, 0, 0, 0, -1, -1, -1, -1};
        long[] timeList = {Long.parseLong(time), 0, 0, 0, 0, 0, 0, 0, 0};
        dogs.put("level", levelList);
        dogs.put("time", timeList);
        jedis.set("dogs:" + uuid, JSON.toJSON(dogs).toString());
        List listInitPrice = JSON.parseObject(jedis.hget("globalDeal", "initPrice"), List.class);
        Map<String,Object> shop = new HashMap<>();
        shop.put("count", new int[30]);
        shop.put("price", listInitPrice);
        jedis.set("shop:" + uuid, JSON.toJSON(shop).toString());
        Map<String,String> param = new HashMap<>();
        param.put("allLevel", "1");
        param.put("vip", "0");
        param.put("golden", "100");
        param.put("createTime", time);
        param.put("diamond", "0");
        param.put("tea", "0");
        param.put("maxLevel", "1");
        param.put("onlineTime", time);
        param.put("genTea", "1");
        param.put("genTeaMultiple", "0");
        param.put("canBuyLevel", "1");
        param.put("rewardStatus", "0");
        param.put("rewardTime", "0");
        jedis.hmset("user:" + uuid, param);
        param.put("uuid", uuid);
        insertUser(param);
    }
}
