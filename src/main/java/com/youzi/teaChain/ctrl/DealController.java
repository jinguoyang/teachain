package com.youzi.teaChain.ctrl;

import com.alibaba.fastjson.JSON;
import com.youzi.teaChain.bean.TTeachainCoinCalc;
import com.youzi.teaChain.bean.TTeachainLogin;
import com.youzi.teaChain.bean.TUser;
import com.youzi.teaChain.bean.Wallet;
import com.youzi.teaChain.common.ErrorCode;
import com.youzi.teaChain.common.QrCodeUtils;
import com.youzi.teaChain.common.StringUtil;
import com.youzi.teaChain.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import redis.clients.jedis.Jedis;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

//import org.apache.log4j.logging.LogFactory;

@Scope(value = "singleton")
@Controller
public class DealController {
    private Logger log = LoggerFactory.getLogger(DealController.class);

    @Resource
    private UserService userService;
    @Resource
    private WalletService walletService;
    @Resource
    private ConfigService configService;
    @Resource
    private LoginService loginService;
    @Resource
    private PatronSaintService patronSaintService;
    @Resource
    private RechargeService rechargeService;
    @Resource
    private CoinCalcService coinCalcService;
    @Resource
    private CommonController commonController;
    @Resource
    private RedisClient redisClient;

    /**
     * @Description: 用户信息查询
     * @param uuid 用户uuid
     * @return java.util.Map<java.lang.String,java.lang.Object>
     * @Date 2018-11-13 10:36:09
     */
    @RequestMapping(value = "/selectUserInfo")
    @ResponseBody
    public Map<String, Object> selectUserInfo(String uuid) {
        Map<String, Object> res = new HashMap<>();
        try(Jedis jedis = redisClient.getResource()) {
            TUser tUser = userService.getTUserByUuid(uuid);
            if (tUser != null) {
                Wallet wallet = walletService.getWalletByUuid(uuid);
                wallet.setPayPassword(null);
                Map tcc = userService.getPersonTeaCoin(uuid);
                // 同步redis数据
                patronSaintService.synRedisToMYSQL(jedis, uuid);
                Map patronSaint = patronSaintService.getPatronSaintUserByUuid(uuid);
                BigDecimal totalSun = patronSaintService.calcTotalSun();
                Map<String, BigDecimal> configMap = configService.selectAllNumberConfig();

                Map<String, BigDecimal> brokeInfo = new HashMap<>();
                brokeInfo.put("brokerageBuy", configMap.get("brokerageBuy"));
                brokeInfo.put("brokerageSell1", configMap.get("brokerageSell1"));
                brokeInfo.put("brokerageSell2", configMap.get("brokerageSell2"));

                TTeachainLogin ttl = loginService.getLoginInfoByUuid(uuid);
                // 判断是否生成二维码
                File upload = new File("/teaChain/static/QrCode/" + ttl.getOpenId() + ".png");
                if (!upload.exists()) shareQrCode(uuid);

                // 查询vip是否过期
                userService.checkVipStatus(uuid, tUser, jedis);

                long currTime = new Date().getTime();
                Map<String, Object> timePeriod = new HashMap<>();
                try {
                    Long timeDifference = 0L;
                    DateFormat df = new SimpleDateFormat("HH:mm:ss");
                    String dateS = df.format(new Date());
                    Date date = df.parse(dateS);
//                    List timeList = configService.selectCalcPowerToday();
//                    Map time1 = (Map) timeList.get(0);
//                    String hour1 = time1.get("keyValue").toString();
//                    Map time2 = (Map) timeList.get(1);
//                    String hour2 = time2.get("keyValue").toString();
//                    Map time3 = (Map) timeList.get(2);
//                    String hour3 = time3.get("keyValue").toString();
//                    Date dt1 = df.parse(hour1 + ":00:00");
//                    Date dt2 = df.parse(hour2 + ":00:00");
//                    Date dt3 = df.parse(hour3 + ":00:00");
//                    if (date.getTime() >= dt3.getTime()) {
//                        timeDifference = dt1.getTime() + 86400000 - date.getTime();
//                    } else if (date.getTime() >= dt2.getTime()) {
//                        timeDifference = dt3.getTime() - date.getTime();
//                    } else if (date.getTime() >= dt1.getTime()) {
//                        timeDifference = dt2.getTime() - date.getTime();
//                    } else if (date.getTime() <= dt1.getTime()) {
//                        timeDifference = dt1.getTime() - date.getTime();
//                    }

                    Date dt = df.parse("24:00:00");
                    timeDifference = dt.getTime() - date.getTime();

                    timePeriod.put("timeDifference", timeDifference);
                } catch (ParseException e) {
                    e.printStackTrace();
                    log.error("获取每日更新茶币时间转换出错：" + e.getMessage());
                }
                String value = configMap.get("providePeriod").toString();
                String providePeriod = StringUtil.autoComplementZero(value, 4);

                timePeriod.put("period", providePeriod);
                timePeriod.put("minProvideCalcPower", configMap.get("minProvideCalcPower").toString());
                timePeriod.put("minProvideTeaCoin", configMap.get("minProvideTeaCoin").toString());

                Long status = loginService.getLoginInfoByUuid(uuid).getStatus();

                Map<String, Object> rate = new HashMap<>();
                rate.put("rate", configMap.get("brokerageSell2"));
                rate.put("transMinRate", configMap.get("transMinRate"));
                rate.put("transMinCounts", configMap.get("transMinCounts"));

                DateFormat dayFormat = new SimpleDateFormat("yyyyMMdd");
                int nowDay = Integer.valueOf(dayFormat.format(new Date()));
                TTeachainCoinCalc ttcc = coinCalcService.selectCoinCalc(uuid);
                if (ttcc.getTodayCoin().compareTo(BigDecimal.ZERO) > 0 && nowDay == ttcc.getEndTime()) {
                    Map<String, Object> showSunLog = new HashMap<>();
                    showSunLog.put("lastPeriod", StringUtil.autoComplementZero(String.valueOf(Integer.parseInt(value) - 1), 4));
                    showSunLog.put("userSun", ttcc.getTeaCoin4());
                    showSunLog.put("sun", ttcc.getTeaCoin5());
                    showSunLog.put("getCoin", ttcc.getTodayCoin());
                    res.put("showSunLog", showSunLog);

                    TTeachainCoinCalc ttcc2 = new TTeachainCoinCalc();
                    ttcc2.setUuid(uuid);
                    ttcc2.setTodayCoin(BigDecimal.ZERO);
                    coinCalcService.updateTCC(ttcc2);
                }

                res.put("code", 200);
                res.put("tUser", tUser);
                res.put("tcc", tcc);
                res.put("wallet", wallet);
                res.put("config", brokeInfo);
                res.put("timePeriod", timePeriod);
                res.put("status", status);
                res.put("trans", rate);
                res.put("patronSaint", patronSaint);
                res.put("totalSun", totalSun);
            } else {
                res.put("code", ErrorCode.NOTFOUNDPLAYER);
                res.put("msg", "未查询到当前用户!");
            }
            return res;
        }catch (Exception e){
            res.put("code", 500);
            res.put("msg", "网络繁忙，请稍后重试!");
            return res;
        }
    }

    /**
     * @Description: 通过uuid生成分享二维码
     * @param uuid 用户uuid
     * @return java.util.Map<java.lang.String,java.lang.Object>
     * @Date 2018-12-5 14:19:08
     */
    @RequestMapping(value = "/shareQrCode")
    @ResponseBody
    public Map<String, Object> shareQrCode(String uuid) {
        Map<String,Object> map = new HashMap<>();
        TTeachainLogin ttl = loginService.getLoginInfoByUuid(uuid);
        String url = "http://tree.youziqipai.cn/share.html?openId=" + ttl.getOpenId();

        try {
//            File classPath = new File(ResourceUtils.getURL("classpath:").getPath());
//            File upload = new File(classPath.getAbsolutePath(),"static/upload/QrCode/");
//            if(!upload.exists()) upload.mkdirs();
//            upload.getAbsolutePath()
            String add = "/teaChain/static/QrCode";

            String imgName = QrCodeUtils.createQrCode(ttl.getOpenId(), add, url, 200, 200);
            if (imgName != null) {
                map.put("code", 200);
                map.put("imgName", imgName);
            } else {
                map.put("code", 500);
                map.put("msg", "生成分享二维码失败");
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            map.put("code", 500);
            map.put("msg", "获取static图片目录失败");
        } catch (Exception e) {
            e.printStackTrace();
            map.put("code", 500);
            map.put("msg", "生成分享二维码失败");
        }
        return map;
    }

    /**
     * @Description: 购买vip
     * @param uuid 用户uuid
     * @param tradeNo 交易单号 type为1时 可不传
     * @param payType 付款方式 1 CNY 2 微信
     * @return java.util.Map<java.lang.String,java.lang.Object>
     * @Date 2019-01-09 10:09:58
     */
    @RequestMapping(value = "/buyVip")
    @ResponseBody
    public Map<String, Object> buyVip(String uuid,String tradeNo,int payType) throws Exception { // 前台限制 不可连续点击
        synchronized (this) {
            Map<String,Object> map = new HashMap<>();
            TUser tUser = userService.getTUserByUuid(uuid);
            Integer vipLevel = tUser.getVipLevel();
            Integer vipValidTime = tUser.getVipValidDay();
            BigDecimal vipCost = new BigDecimal("30");
            if (payType == 1) {
                BigDecimal CNY = tUser.getTotalMoney();
                if (CNY.compareTo(vipCost) < 0) {
                    map.put("code", 500);
                    map.put("msg", "CNY不足！");
                    return map;
                } else {
                    Boolean changeMoneySuccess = userService.updateCNYFunc(uuid, vipCost, 1);
                    if (!changeMoneySuccess){
                        map.put("code", 500);
                        map.put("msg", "扣除CNY失败！");
                        return map;
                    }
                }
            } else if (payType == 2){
                Map param = rechargeService.spliceOrderQuery(uuid, tradeNo);
                if (Integer.parseInt(param.get("code").toString()) == 200) {
                    BigDecimal amount = new BigDecimal(param.get("realAmount").toString());
                    if (amount.compareTo(vipCost)<0){
                        map.put("code", 500);
                        map.put("msg", "微信充值金额有误！");
                        return map;
                    }
                }else {
                    return param;
                }
            }
            Date now = new Date();
            DateFormat df = new SimpleDateFormat("yyyyMMdd");
            int date = Integer.valueOf(df.format(now));
            long month = 3600L * 1000L * 24L * 29L; // 有效期三十天
            // 若thirty为20190110 则会员有效期至2019-01-11 00:00:00
            int thirty = Integer.valueOf(df.format(new Date(now.getTime() + month)));
            // 未到期续约
            if (vipLevel != 0 && date < vipValidTime){
                Date validDate = df.parse(Integer.valueOf(vipValidTime+ 1).toString());
                thirty = Integer.valueOf(df.format(new Date(validDate.getTime() + month)));
            }

            Boolean setSQLSuccess = userService.setVipAndDay(uuid, 1, thirty);
            if (setSQLSuccess) {
                try(Jedis jedis = redisClient.getResource()) {
                    int currentVip = Integer.valueOf(jedis.hget("user:" + uuid, "vip"));
                    BigDecimal genTeaMultiple = new BigDecimal(jedis.hget("user:" + uuid, "genTeaMultiple"));
                    BigDecimal[] vipSpeed = JSON.parseObject(jedis.hget("globalDeal", "vipSpeed"), BigDecimal[].class);

                    if (currentVip == 0) {
                        genTeaMultiple = genTeaMultiple.add(vipSpeed[1]);
                        jedis.hset("user:" + uuid, "genTeaMultiple", genTeaMultiple.toString());
                        jedis.hset("user:" + uuid, "vip", "1");
                    }
                }catch (Exception e){
                    map.put("code", 500);
                    map.put("msg", "网络繁忙，请稍后重试!");
                    return map;
                }
                if(commonController.updateProfit(uuid,vipCost,vipCost.multiply(BigDecimal.TEN),1,"购买vip",map))
                    return map;
            } else {
                map.put("code", 500);
                map.put("msg", "CNY购买会员失败！");
            }
            map.put("code", 200);
            return map;
        }
    }

    /**
     * @Description: 会员中心
     * @param uuid 用户uuid
     * @return java.util.Map<java.lang.String,java.lang.Object>
     * @Date 2019-01-10 14:21:10
     */
    @RequestMapping(value = "/vipCenterIndex")
    @ResponseBody
    public Map<String, Object> vipCenterIndex(String uuid) throws ParseException {
        Map<String, Object> map = new HashMap<>();
        try(Jedis jedis = redisClient.getResource()) {
            TUser tUser = userService.getTUserByUuid(uuid);
            if (tUser != null) {
                if (!userService.checkVipStatus(uuid, tUser, jedis)) {
                    map.put("code", 500);
                    map.put("msg", "判断vip状态异常!");
                    return map;
                }
                int vipValidDay = tUser.getVipValidDay();
                // 0未购买过会员 1会员有效 2会员过期
                int vipStatus = vipValidDay == 0 ? 0 : tUser.getVipLevel() == 0 ? 2 : 1;
                String customerServicesWX = configService.selectConfig("customerServicesWX");
                BigDecimal[] vipSpeed = JSON.parseObject(jedis.hget("globalDeal", "vipSpeed"), BigDecimal[].class);
                long vipValid = 0;
                if (vipValidDay != 0) {
                    DateFormat df = new SimpleDateFormat("yyyyMMdd");
                    vipValid = df.parse(String.valueOf(vipValidDay + 1)).getTime();
                }
                map.put("code", 200);
                map.put("vipStatus", vipStatus);
                map.put("vipValidDay", vipValid);
                map.put("sunSpeed", vipSpeed[1]);
                map.put("customerServicesWX", customerServicesWX);
            } else {
                map.put("code", ErrorCode.NOTFOUNDPLAYER);
                map.put("msg", "未查询到当前用户!");
            }
            return map;
        }catch (Exception e){
            map.put("code", 500);
            map.put("msg", "网络繁忙，请稍后重试!");
            return map;
        }
    }

    /**
     * @Description: 心跳 同步 增长金币及茶叶
     * @param uuid 用户uuid
     * @return java.util.Map
     * @Date 2018-12-21 17:06:06
     */
    @RequestMapping(value = "/heartbeat", method = RequestMethod.POST)
    @ResponseBody
    public Map heartbeat(String uuid) {
        Map<String,Object> map = new HashMap<>();
        try(Jedis jedis = redisClient.getResource()) {
            patronSaintService.genMoneyTeaByTime(jedis, uuid, null);
            map.put("code", 200);
            return map;
        }catch (Exception e){
            map.put("code", 500);
            map.put("msg", "网络繁忙，请稍后重试!");
            return map;
        }
    }

}
