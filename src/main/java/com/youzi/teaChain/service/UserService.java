package com.youzi.teaChain.service;

import com.alibaba.fastjson.JSON;
import com.youzi.teaChain.bean.TTeachainLogin;
import com.youzi.teaChain.bean.TTeachainPowerUp;
import com.youzi.teaChain.bean.TUser;
import com.youzi.teaChain.bean.User;
import com.youzi.teaChain.common.SpringContextUtils;
import com.youzi.teaChain.ctrl.TodayCalcController;
import com.youzi.teaChain.dao.PowerUpMapper;
import com.youzi.teaChain.dao.UserMapper;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.UncategorizedSQLException;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.youzi.teaChain.common.StringUtil.getNumberRandom;

@Service
public class UserService {
    
    private Logger log = LoggerFactory.getLogger(UserService.class);

    @Resource
    private UserMapper userMapper;
    @Resource
    private PowerUpMapper powerUpMapper;
    @Resource
    private LoginService loginService;
    @Resource
    private ShareService shareService;
    @Resource
    private RedisClient redisClient;
    @Resource
    private PatronSaintService patronSaintService;

    public TUser getTUserByUuid(String uuid) {
        try {
            return userMapper.getTUserByUuid(uuid);
        }catch (Exception e){
            e.printStackTrace();
            log.error("getTUserByUuid 查询用户出错：" + e.getMessage());
            return null;
        }
    }

    /* 登陆验证 */
    public TUser checkLogin(String username, String password) {
        try {
            //根据用户名实例化用户对象
            User user = userMapper.getUserByName(username);
//            if (user != null && user.getPassword().equals(SHA.encryptSHA(password))) {
            if (user != null) {
                return userMapper.getTUserByName(username);
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("checkLogin 登陆验证出错：" + e.getMessage());
            return null;
        }
        return null;
    }

    public Boolean setAwardStateByUuid(String uuid) {
        try {
            userMapper.setAwardStateByUuid(uuid);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("setAwardStateByUuid 领取奖励出错：" + e.getMessage());
            return false;
        }
    }

    public Boolean checkUserName(String phone) {
        try {
            int counts = userMapper.checkUserName(phone);
            return counts > 0;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("checkUserName 未找到该用户：" + e.getMessage());
            return false;
        }
    }

    public Boolean checkIdCardNum(String idCardNum) {
        try {
            int counts = userMapper.checkIdCardNum(idCardNum);
            return counts > 0;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("checkIdCardNum 查询身份证号出错：" + e.getMessage());
            return false;
        }
    }

    public Boolean checkWechatNum(String wechatNum) {
        try {
            int counts = userMapper.checkWechatNum(wechatNum);
            return counts > 0;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("checkWechatNum 查询微信号出错：" + e.getMessage());
            return false;
        }
    }

    public Boolean insertTUserByWXUser(Map param) {
        try {
            userMapper.insertTUserByWXUser(param);
            return true;
        } catch (UncategorizedSQLException e){
            try {
                String newName = "玩家"+getNumberRandom(100000, 999999);
                param.put("nickName", newName);
                log.info("insertTUserByWXUser 保存用户信息出错 更换用户名："+ newName + e.getMessage());
                userMapper.insertTUserByWXUser(param);
                return true;
            }catch (Exception ex) {
                ex.printStackTrace();
                log.error("insertTUserByWXUser 保存用户微信信息出错：" + ex.getMessage());
                return false;
            }
        }catch (Exception e) {
            e.printStackTrace();
            log.error("insertTUserByWXUser 保存用户微信信息出错：" + e.getMessage());
            return false;
        }
    }

    public String getIdByUuid(String uuid) {
        try {
            return userMapper.getIdByUuid(uuid);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("getIdByUuid 通过uuid取id出错：" + e.getMessage());
            return null;
        }
    }

    public String getUuidById(int id) {
        try {
            return userMapper.getUuidById(id);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("getUuidById 通过id取uuId出错：" + e.getMessage());
            return null;
        }
    }

    public Boolean updateWXInfo(Map param) {
        try {
            userMapper.updateWXInfo(param);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("updateWXInfo 更新用户微信信息出错：" + e.getMessage());
            return false;
        }
    }

    public Boolean setIdCard(String uuid,String idCardNum,String idCardName) {
        try {
            userMapper.setIdCard(uuid,idCardNum,idCardName);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("SetIdCard 保存身份证信息出错：" + e.getMessage());
            return false;
        }
    }

    public Boolean setWechatNum(String uuid,String wechatNum) {
        try {
            userMapper.setWechatNum(uuid,wechatNum);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("setWechatNum 设置绑定微信号出错：" + e.getMessage());
            return false;
        }
    }

    public List selectCalcUser(BigDecimal minCalcPower, BigDecimal minTeaCoin){
        try {
            List list = userMapper.selectCalcUser(minCalcPower, minTeaCoin);
            return list;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("selectCalcUser 查询计算用户出错：" + e.getMessage());
            return null;
        }
    }

    public BigDecimal selectTotalCalcPower(BigDecimal minCalcPower, BigDecimal minTeaCoin){
        try {
            return userMapper.selectTotalCalcPower(minCalcPower, minTeaCoin);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("selectTotalCalcPower 查询所有符合配置用户总算力出错：" + e.getMessage());
            return BigDecimal.ZERO;
        }
    }

    public Map getPersonTeaCoin(String uuid){
        try {
            TodayCalcController TccCtrl = SpringContextUtils.getBean(TodayCalcController.class);
            Map tcc = TccCtrl.calcPersonTeaCoin(uuid);
            tcc.remove("code");
            return tcc;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("getPersonTeaCoin 查询所有用户tcc出错：" + e.getMessage());
            return null;
        }
    }

    public Boolean updateUserPhoneByUuid(String uuid, BigInteger phone){
        try {
            userMapper.updateUserPhoneByUuid(uuid, phone);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("updateUserPhoneByUuid 更新用户表手机号出错：" + e.getMessage());
            return false;
        }
    }

    public Boolean changeTotalMoney(String uuid, BigDecimal count,int type){
        try {
//            BigDecimal negativeCount = count.multiply(new BigDecimal(-1));
            userMapper.changeTotalMoney(uuid, count, type);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("changeTotalMoney 更改人民币出错：" + e.getMessage());
            return false;
        }
    }

    public Boolean updateCalcPower(String uuid, String support, int supportType, BigDecimal count,int type, int temp){
        try {
            userMapper.updateCalcPower(uuid,count,type,temp);
            TUser tUser = getTUserByUuid(uuid);
            BigDecimal calcPower = tUser.getCalcPower();
            Integer tradeLicense=tUser.getTradeLicense();
            Date now = new Date();
            TTeachainPowerUp tTeachainPowerUp = new TTeachainPowerUp(uuid,support,supportType,count.doubleValue(),temp,now,new Date(now.getTime()+30000));
            powerUpMapper.createPowerUp(tTeachainPowerUp);
            if(calcPower.compareTo(new BigDecimal("35")) > 0 && tradeLicense!=1){
                setTradeLicense(uuid);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("updateCalcPower 更新算力：" + e.getMessage());
            return false;
        }
    }

    public Boolean setSuperior(String uuid, String support, int supportType, BigDecimal count,int type, int temp){ //弃用
        try {
            userMapper.updateCalcPower(uuid,count,type,temp);
            TUser tUser = getTUserByUuid(uuid);
            BigDecimal calcPower = tUser.getCalcPower();
            Integer tradeLicense=tUser.getTradeLicense();
            Date now = new Date();
            TTeachainPowerUp tTeachainPowerUp = new TTeachainPowerUp(uuid,support,supportType,count.doubleValue(),temp,now,new Date(now.getTime()+30000));
            powerUpMapper.createPowerUp(tTeachainPowerUp);
            if(calcPower.compareTo(new BigDecimal("35")) > 0 && tradeLicense!=1){
                setTradeLicense(uuid);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("setSuperior 设置上级失败：" + e.getMessage());
            return false;
        }
    }

    public Boolean openHoleAuto(String uuid){
        try {
            TUser tUser = getTUserByUuid(uuid);
            Integer a = tUser.getHasFirstTCC();
            Integer hasSuperior = tUser.getSuperior();
            int superior;
            String superior_uuid;
            if (a.equals(1)) {
                if (hasSuperior == 0) { // 现 禁止 注册后再扫码绑定
//                    TTeachainLogin tTeachainLogin = loginService.getLoginInfoByUuid(uuid);
//                    superior = shareService.selectShareUserId(tTeachainLogin.getOpenId(), tTeachainLogin.getUnionId());
                    userMapper.setHasFirstTCC(uuid, 2);
//                    if (superior == 0) {
                        return true;
//                    }
//                    setSuperior(uuid);
//                    superior_uuid = getUuidById(superior);
                } else { //老用户
                    superior = hasSuperior;
                    superior_uuid = getUuidById(hasSuperior);
                    userMapper.setHasFirstTCC(uuid, 2);
                }
                try(Jedis jedis = redisClient.getResource()){
                    int count = userMapper.getInviterCount(superior);
                    if (!jedis.exists("dogs:" + superior_uuid)) {
                        patronSaintService.initUser(superior_uuid, jedis);
                    }
                    String dogs = jedis.get("dogs:" + superior_uuid);
                    Map dogsMap = JSONObject.fromObject(dogs);
                    int[] levelArrays = JSON.parseObject(dogsMap.get("level").toString(), int[].class);
                    if (count > 5) {
                        if (levelArrays[5] == -1) {
                            levelArrays[5] = 0;
                            patronSaintService.setHoleAndSpeed(superior_uuid, 6, levelArrays, dogsMap, jedis);
                        } else if (levelArrays[6] == -1) {
                            patronSaintService.setHoleAndSpeed(superior_uuid, 6, levelArrays, dogsMap, jedis);
                        }
                    } else if (count > 1) {
                        if (levelArrays[5] == -1) {
                            patronSaintService.setHoleAndSpeed(superior_uuid, 5, levelArrays, dogsMap, jedis);
                        }
                    }
                }catch (Exception e){
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("openHoleAuto 自动开坑异常：" + e.getMessage());
            return false;
        }
    }

    public Boolean setTradeLicense(String uuid){
        try {
            userMapper.setTradeLicense(uuid);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("setTradeLicense 设置交易市场权限失败：" + e.getMessage());
            return false;
        }
    }

    public List<Map> getRankList(int type){
        try {
            List<Map> list = null;
            if(type == 1){
                list =  userMapper.getRankListByCalc();
            }else if (type == 2){
                list = userMapper.getRankListByRecharge();
            }else if (type == 3){
                list = userMapper.getRankListByTCC();
            }
            return list;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("getRankList 获取排行榜发生异常!：" + e.getMessage());
            return null;
        }
    }

    public List<Map> getCalcRelation(String uuid){
        try {
            return userMapper.getCalcRelation(uuid);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("getCalcRelation 设置交易市场权限失败：" + e.getMessage());
            return null;
        }
    }

    public Boolean setSuperior(String uuid){
        try {
            TTeachainLogin tTeachainLogin = loginService.getLoginInfoByUuid(uuid);
            int superior = shareService.selectShareUserId(tTeachainLogin.getOpenId(), tTeachainLogin.getUnionId());
            if (superior != 0) {
                String superior_uuid = getUuidById(superior);
                if (!superior_uuid.equals(uuid)) {
                    userMapper.setSuperior(uuid, superior);
                }
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("setSuperior 设置上级失败："+"uuid:"+ uuid + e.getMessage());
            return false;
        }
    }

    public Boolean setDiamond(String uuid,int diamond){
        try {
            userMapper.setDiamond(uuid,diamond);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("setDiamond 修改钻石失败：" + e.getMessage());
            return false;
        }
    }

    public int getInviterCount(int id){
        try {
            return userMapper.getInviterCount(id);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("getInviterCount 通过id获取有效下级人数失败：" + e.getMessage());
            return 0;
        }
    }

    public Boolean updateDiamondFunc(String uuid, BigDecimal diamond, int type){
        try {
            int aa = userMapper.updateDiamondFunc(uuid, diamond, type);
            return aa == 200;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("updateDiamondFunc 通过事务修改钻石失败：" + e.getMessage());
            return false;
        }
    }

    public Boolean setVipAndDay(String uuid,int vipLevel,int vipValidDay){
        try {
            userMapper.setVipAndDay(uuid, vipLevel, vipValidDay);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("setVipAndTime 修改vip时间错误：" + e.getMessage());
            return false;
        }
    }

    public Boolean updateCNYFunc(String uuid, BigDecimal cny, int type){
        try {
            int aa = userMapper.updateCNYFunc(uuid, cny, type);
            return aa == 200;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("updateCNYFunc 通过事务修改CNY失败：" + e.getMessage());
            return false;
        }
    }

    public boolean checkVipStatus(String uuid, TUser tUser, Jedis jedis) {
        try {
            DateFormat dfDay = new SimpleDateFormat("yyyyMMdd");
            int nowDay = Integer.valueOf(dfDay.format(new Date()));
            int vipValidDay = tUser.getVipValidDay();
            if (tUser.getVipLevel() != 0 && vipValidDay < nowDay) {
                tUser.setVipLevel(0);
                setVipAndDay(uuid, 0, vipValidDay);
                Integer vipLevel = Integer.valueOf(jedis.hget("user:" + uuid, "vip"));

                if (vipLevel != 0) {
                    jedis.hset("user:" + uuid, "vip", "0");
                    BigDecimal genTeaMultiple = new BigDecimal(jedis.hget("user:" + uuid, "genTeaMultiple"));
                    BigDecimal[] vipSpeed = JSON.parseObject(jedis.hget("globalDeal", "vipSpeed"), BigDecimal[].class);
                    genTeaMultiple = genTeaMultiple.subtract(vipSpeed[vipLevel]);
                    jedis.hset("user:" + uuid, "genTeaMultiple", genTeaMultiple.toString());
                }
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("checkVipStatus 判断vip状态失败：" + e.getMessage());
            return false;
        }
    }

    public List<Map> getLowerRelation(String uuid, int type){
        try {
            return userMapper.getLowerRelation(uuid, type);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("getLowerRelation 获取用户下级失败：" + e.getMessage());
            return null;
        }
    }

}
