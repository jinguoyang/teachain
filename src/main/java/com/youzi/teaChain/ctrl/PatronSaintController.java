package com.youzi.teaChain.ctrl;

import com.alibaba.fastjson.JSON;
import com.youzi.teaChain.bean.TTeachainLogin;
import com.youzi.teaChain.bean.TUser;
import com.youzi.teaChain.bean.Wallet;
import com.youzi.teaChain.service.*;
import net.sf.json.JSONObject;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import redis.clients.jedis.Jedis;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
@Scope(value = "singleton")    //非scope=”singleton”单例模式注解，每次请求单独的Action，线程安全
@RequestMapping("/patronSaint")
public class PatronSaintController {

    @Resource
    private PatronSaintService patronSaintService;
    @Resource
    private UserService userService;
    @Resource
    private RechargeService rechargeService;
    @Resource
    private RedisClient redisClient;
    @Resource
    private CommonController commonController;
    @Resource
    private WalletService walletService;
    @Resource
    private LoginService loginService;

    /**
     * 守护神游戏登陆
     * 2019-01-18 10:14:02
     * @param uuid 用户uuid
     * @return java.util.Map
     */
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    @ResponseBody
    public Map login(String uuid) {
        Map<String, Object> map = new HashMap<>();
        Map<String, Object> shop = new HashMap<>();
        try (Jedis jedis = redisClient.getResource()) {
            if (!jedis.exists("user:" + uuid)) {
                if (userService.getTUserByUuid(uuid) != null) {
                    patronSaintService.initUser(uuid, jedis);
                }else {
                    map.put("code", 500);
                    map.put("msg", "用户初始化出错");
                    return map;
                }
            }
            patronSaintService.synDiamondFromMYSQL(jedis, uuid);
            patronSaintService.genMoneyTeaByTime(jedis, uuid, null);
            Map user = patronSaintService.getRedisUser(jedis, uuid);
            String redisDog = jedis.get("dogs:" + uuid);
            Map<String, List<Long>> dogs = JSON.parseObject(redisDog, Map.class);
            List genMoneyList = JSON.parseArray(jedis.hget("globalDeal", "genMoneyMap"));
            Map shopInfoMap = JSON.parseObject(jedis.get("shop:" + uuid), Map.class);
            shop.put("genMoney", genMoneyList);
            shop.put("price", shopInfoMap.get("price"));
            if (user != null && dogs != null && shop.size() != 0) {
                map.put("code", 200);
                map.put("user", user);
                map.put("dogs", dogs);
                map.put("shop", shop);
            } else {
                map.put("code", 500);
            }
            return map;
        }catch (Exception e){
            map.put("code", 500);
            map.put("msg", "网络繁忙，请稍后重试!");
            return map;
        }
    }

    /**
     * @Description: 移动或合成狗
     * @param uuid 用户uuid
     * @param target 目标狗索引
     * @param partner 被移动狗索引
     * @return java.util.Map
     * @Date 2018-12-21 17:06:06
     */
    @RequestMapping(value = "/combineDogs", method = RequestMethod.POST)
    @ResponseBody
    public Map combine(String uuid, int target, int partner) {
        Map<String,Object> map = new HashMap<>();
        if(target == partner){
            map.put("code", 500);
            map.put("msg", "位置异常");
            return map;
        }
        try(Jedis jedis = redisClient.getResource()) {
            try {
                if (redisClient.lock(jedis, uuid, 5000, 10000)) {
                    String dogs = jedis.get("dogs:" + uuid);
                    Map dogsMap = JSONObject.fromObject(dogs);
                    int[] levelArrays = JSON.parseObject(dogsMap.get("level").toString(), int[].class);
                    BigDecimal[] timeArrays = JSON.parseObject(dogsMap.get("time").toString(), BigDecimal[].class);
                    patronSaintService.genMoneyTeaByTime(jedis, uuid, dogsMap);
                    if (levelArrays[partner] == 0) {
                        map.put("code", 500);
                        return map;
                    }
                    int newMaxLevel = 0;
                    if (levelArrays[target] != -1 || levelArrays[partner] != -1) {
                        if (levelArrays[target] == levelArrays[partner] && levelArrays[target] != 0) {
                            levelArrays[target] = levelArrays[partner] + 1;
                            timeArrays[target] = new BigDecimal(new Date().getTime());
                            levelArrays[partner] = 0;
                            timeArrays[partner] = new BigDecimal("0");
                            newMaxLevel = patronSaintService.setUserAfterChangeDogs(jedis, uuid, levelArrays);
                        } else {
                            int tempLevel = levelArrays[target];
                            levelArrays[target] = levelArrays[partner];
                            levelArrays[partner] = tempLevel;
                            BigDecimal tempTime = timeArrays[target];
                            timeArrays[target] = timeArrays[partner];
                            timeArrays[partner] = tempTime;
                        }
                        dogsMap.put("level", levelArrays);
                        dogsMap.put("time", timeArrays);
                        jedis.set("dogs:" + uuid, JSON.toJSON(dogsMap).toString());
                        Map user = patronSaintService.getRedisUser(jedis, uuid);
                        Map<String, Object> shop = new HashMap<>();
                        String genMoney = jedis.hget("globalDeal", "genMoneyMap");
                        List genMoneyList = JSON.parseArray(genMoney);
                        String shopInfo = jedis.get("shop:" + uuid);
                        Map shopInfoMap = JSON.parseObject(shopInfo, Map.class);
                        shop.put("genMoney", genMoneyList);
                        shop.put("price", shopInfoMap.get("price"));
                        map.put("code", 200);
                        map.put("user", user);
                        map.put("dogs", dogsMap);
                        map.put("shop", shop);
                        map.put("newMaxLevel", newMaxLevel);
                    } else {
                        map.put("code", 500);
                        map.put("msg", "位置未解锁");
                    }
                    return map;
                } else {
                    map.put("code", 500);
                    map.put("msg", "网络错误,请稍后重试");
                    return map;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                map.put("code", 500);
                map.put("msg", "服务器阻塞");
                return map;
            } finally {
                redisClient.unlock(jedis, uuid);
            }
        }catch (Exception e){
            map.put("code", 500);
            map.put("msg", "网络繁忙，请稍后重试!");
            return map;
        }
    }

    /**
     * 商店购买
     * 2018-12-19 10:26:57
     * @param uuid 用户uuid
     * @param level 购买狗的等级
     * @return java.util.Map
     */
    @RequestMapping(value = "/buyDogs", method = RequestMethod.POST)
    @ResponseBody
    public Map buyDogs(String uuid, int level) {
        Map<String,Object> map = new HashMap<>();
        try(Jedis jedis = redisClient.getResource()) {
            try {
                if (redisClient.lock(jedis, uuid, 5000, 10000)) {
                    Map user = jedis.hgetAll("user:" + uuid);
                    int maxLevel = Integer.parseInt(user.get("maxLevel").toString());
                    if (maxLevel <= 5) { // todo 主动改变
                        level = 1;
                    } else if (level + 4 > maxLevel) {
                        map.put("code", 500);
                        map.put("msg", "购买等级不可大于最大等级的4级");
                        return map;
                    }

                    // 读取dogs
                    String dogs = jedis.get("dogs:" + uuid);
                    Map dogsMap = JSONObject.fromObject(dogs);
                    int[] levelArrays = JSON.parseObject(dogsMap.get("level").toString(), int[].class);
                    BigDecimal[] timeArrays = JSON.parseObject(dogsMap.get("time").toString(), BigDecimal[].class);
                    patronSaintService.genMoneyTeaByTime(jedis, uuid, dogsMap);
                    // 判断要放置的零号位
                    int zero = -1;
                    for (int i = 0; i < levelArrays.length; i++) {
                        if (levelArrays[i] == 0) {
                            zero = i;
                            break;
                        }
                    }
                    if (zero == -1) {
                        map.put("code", 500);
                        map.put("msg", "没有可以放置的位置了");
                        return map;
                    }

                    // 读取shop
                    String pos = jedis.get("shop:" + uuid);
                    Map shopMap = JSONObject.fromObject(pos);
                    int[] countArrays = JSON.parseObject(shopMap.get("count").toString(), int[].class);
                    BigDecimal[] priceArrays = JSON.parseObject(shopMap.get("price").toString(), BigDecimal[].class);
                    int count = countArrays[level - 1];
                    BigDecimal money = priceArrays[level - 1];

                    Map globalDeal = jedis.hgetAll("globalDeal");
                    // 算法系数
                    String inc = globalDeal.get("increasePara").toString();
                    BigDecimal[] increasePara = JSON.parseObject(inc, BigDecimal[].class);
                    // 算法初始价格
                    String pri = globalDeal.get("initPrice").toString();
                    BigDecimal[] price = JSON.parseObject(pri, BigDecimal[].class);

                    // 计算钱包
                    BigDecimal golden = new BigDecimal(jedis.hget("user:" + uuid, "golden"));
                    if (golden.compareTo(money) < 0) {
                        map.put("code", 500);
                        map.put("msg", "您的金币不足！");
                        return map;
                    }
                    // 计算新价格
                    BigDecimal newPrice = calcMoneyByCount(level, count + 1, increasePara, price);
                    // 计算新金币
                    BigDecimal newMoney = golden.subtract(money);
                    jedis.hset("user:" + uuid, "golden", String.valueOf(newMoney));
                    // 存入总等级
                    int allLevel = Integer.parseInt(user.get("allLevel").toString());
                    jedis.hset("user:" + uuid, "allLevel", String.valueOf(allLevel + level));

                    // 修改dogs
                    levelArrays[zero] = level;
                    timeArrays[zero] = new BigDecimal(new Date().getTime());
                    dogsMap.put("level", levelArrays);
                    dogsMap.put("time", timeArrays);
                    jedis.set("dogs:" + uuid, JSON.toJSON(dogsMap).toString());

                    // 修改shop
                    countArrays[level - 1] = count + 1;
                    priceArrays[level - 1] = newPrice;
                    String[] priceArraysString = new String[priceArrays.length];
                    for (int i = 0; i < priceArrays.length; i++) {
                        priceArraysString[i] = priceArrays[i].toString();
                    }

                    BigDecimal genTea = patronSaintService.getGenTeaSpeed(allLevel + level);
                    jedis.hset("user:" + uuid, "genTea", String.valueOf(genTea));
                    Map newUser = patronSaintService.getRedisUser(jedis, uuid);

                    shopMap.put("count", countArrays);
                    shopMap.put("price", priceArraysString);
                    jedis.set("shop:" + uuid, JSON.toJSON(shopMap).toString());
                    Map<String, Object> newShop = new HashMap<>();
                    String genMoney = jedis.hget("globalDeal", "genMoneyMap");
                    List genMoneyList = JSON.parseArray(genMoney);
                    newShop.put("genMoney", genMoneyList);
                    newShop.put("price", priceArraysString);

                    map.put("code", 200);
                    map.put("user", newUser);
                    map.put("dogs", dogsMap);
                    map.put("shop", newShop);
                    map.put("position", zero);
                    return map;
                } else {
                    map.put("code", 500);
                    map.put("msg", "网络错误,请稍后重试");
                    return map;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                map.put("code", 500);
                map.put("msg", "服务器阻塞");
                return map;
            } finally {
                redisClient.unlock(jedis, uuid);
            }
        }catch (Exception e){
            map.put("code", 500);
            map.put("msg", "网络繁忙，请稍后重试!");
            return map;
        }
    }

    /**
     * 卖掉狗狗
     * 2018-12-19 14:15:26
     * @param uuid 用户uuid
     * @param target 被删除狗索引
     * @return java.util.Map
     */
    @RequestMapping(value = "/delDogs", method = RequestMethod.POST)
    @ResponseBody
    public Map delDogs(String uuid, int target) {
        Map<String,Object> map = new HashMap<>();
        if (target > 9 || target < 0) {
            map.put("warning", "少来臭不要脸的请求接口！");
            return map;
        }
        try(Jedis jedis = redisClient.getResource()) {
            try {
                if (redisClient.lock(jedis, uuid, 5000, 10000)) {
                    // 读取dogs
                    String dogs = jedis.get("dogs:" + uuid);
                    Map dogsMap = JSONObject.fromObject(dogs);
                    int[] levelArrays = JSON.parseObject(dogsMap.get("level").toString(), int[].class);
                    BigDecimal[] timeArrays = JSON.parseObject(dogsMap.get("time").toString(), BigDecimal[].class);
                    patronSaintService.genMoneyTeaByTime(jedis, uuid, dogsMap);
                    int level = levelArrays[target];
                    if (level == -1 || level == 0) {
                        map.put("code", 500);
                        map.put("msg", "您要售出的狗狗等级有误！");
                        return map;
                    }
                    int temp = 0;
                    for (int levelArray : levelArrays) {
                        if (levelArray != -1 && levelArray != 0) {
                            temp += 1;
                        }
                    }
                    if (temp == 1) {
                        map.put("code", 500);
                        map.put("msg", "场上的狗狗数量不能少于一只！");
                        return map;
                    }

                    Map globalDeal = jedis.hgetAll("globalDeal");
                    // 算法初始价格
                    String pri = globalDeal.get("initPrice").toString();
                    BigDecimal[] price = JSON.parseObject(pri, BigDecimal[].class);
                    BigDecimal initPrice = price[level - 1];
                    BigDecimal sellPrice = initPrice.multiply(new BigDecimal("0.8")).setScale(0, BigDecimal.ROUND_HALF_UP);

                    // 计算钱包
                    Map user = jedis.hgetAll("user:" + uuid);
                    BigDecimal golden = new BigDecimal(user.get("golden").toString());
                    jedis.hset("user:" + uuid, "golden", String.valueOf(golden.add(sellPrice)));
                    // 存入总等级
                    int allLevel = Integer.parseInt(user.get("allLevel").toString());
                    jedis.hset("user:" + uuid, "allLevel", String.valueOf(allLevel - level));

                    // 修改dogs
                    levelArrays[target] = 0;
                    timeArrays[target] = BigDecimal.ZERO;
                    dogsMap.put("level", levelArrays);
                    dogsMap.put("time", timeArrays);
                    jedis.set("dogs:" + uuid, JSON.toJSON(dogsMap).toString());

                    BigDecimal genTea = patronSaintService.getGenTeaSpeed(allLevel - level);
                    jedis.hset("user:" + uuid, "genTea", String.valueOf(genTea));
                    //        patronSaintService.setUserAfterChangeDogs(jedis,uuid,levelArrays);
                    Map newUser = patronSaintService.getRedisUser(jedis, uuid);

                    map.put("code", 200);
                    map.put("user", newUser);
                    map.put("dogs", dogsMap);
                    map.put("sellPrice", sellPrice.toString());
                    return map;
                } else {
                    map.put("code", 500);
                    map.put("msg", "网络错误,请稍后重试");
                    return map;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                map.put("code", 500);
                map.put("msg", "服务器阻塞");
                return map;
            } finally {
                redisClient.unlock(jedis, uuid);

            }
        }catch (Exception e){
            map.put("code", 500);
            map.put("msg", "网络繁忙，请稍后重试!");
            return map;
        }
    }

    /**
     * 狗狗排行榜
     * 2018-12-20 13:45:50
     * @param uuid 用户uuid
     * @return
     */
    @RequestMapping(value = "/dogsRank", method = RequestMethod.POST)
    @ResponseBody
    public Map dogsRank(String uuid) {
        Map<String,Object> map = new HashMap<>();
        try(Jedis jedis = redisClient.getResource()) {
            patronSaintService.synRedisToMYSQL(jedis, uuid);
//      int ranking = patronSaintService.getRankByUuid(uuid);
            List<Map> list = patronSaintService.getRankList();
            if (list != null) {
                for (Map info : list) {
                    String golden;
                    try {
                        golden = info.get("golden").toString();
                    } catch (Exception e) {
                        map.put("code", 500);
                        map.put("msg", "获取排行榜异常！");
                        return map;
                    }
                    info.put("golden", golden);
                }
                map.put("code", 200);
                map.put("ranking", 0);
                map.put("list", list);
            } else {
                map.put("code", 500);
                map.put("msg", "获取排行榜失败！");
            }
            return map;
        }catch (Exception e){
            map.put("code", 500);
            map.put("msg", "网络繁忙，请稍后重试!");
            return map;
        }

    }

    /**
     * 狗狗钻石充值(暂弃)
     * @param uuid
     * @param money
     * @return
     * @throws Exception
     */
    public Map wxPay(String uuid, BigDecimal money, BigDecimal diffMoney) throws Exception {
        String body;
        if (diffMoney.compareTo(BigDecimal.ZERO) > 0) {
            money = diffMoney;
            body = "开坑钻石补足充值 " + money.toString() + "元";
        } else {
            body = "开坑钻石充值 " + money.toString() + "元";
        }
        String amount = money.multiply(new BigDecimal("100")).setScale(0, BigDecimal.ROUND_DOWN).toString();       // 单位为分

        return rechargeService.spliceRecharge(uuid, amount, body, "E");
    }

    /**
     * 通过邀请人数解锁6、7号坑位(暂弃)
     * 2018-12-21 17:30:33
     * @param uuid
     * @param target
     * @return
     */
    public Map checkInviteCounts(String uuid, int target) {
        Map<String,Object> map = new HashMap<>();
        try(Jedis jedis = redisClient.getResource()) {

            // 茶叶额外加速 = vip0.1 + 开坑固定倍数
            int vip = Integer.parseInt(jedis.hget("user:" + uuid, "vip"));
            BigDecimal genTeaMultiple;

            if (target == 5) {
                TUser user = userService.getTUserByUuid(uuid);
                int counts = userService.getInviterCount(user.getId());
                if (counts >= 2) {
//                if (addHole(uuid, target, map, jedis)) return map;
                    genTeaMultiple = vip > 0 ? new BigDecimal("0.3") : new BigDecimal("0.2");
                    jedis.hset("user:" + uuid, "genTeaMultiple", String.valueOf(genTeaMultiple));
                } else {
                    map.put("code", 500);
                    map.put("msg", "有效邀请人数不足2人或使用钻石开启");
                    return map;
                }
            } else if (target == 6) {
                TUser user = userService.getTUserByUuid(uuid);
                int counts = userService.getInviterCount(user.getId());
                if (counts >= 6) {
//                if (addHole(uuid, target, map, jedis)) return map;
                    genTeaMultiple = vip > 0 ? new BigDecimal("0.3") : new BigDecimal("0.2");
                    jedis.hset("user:" + uuid, "genTeaMultiple", String.valueOf(genTeaMultiple));
                } else {
                    map.put("code", 500);
                    map.put("msg", "有效邀请人数不足6人或使用钻石开启");
                    return map;
                }
            } else {
                map.put("code", 500);
                map.put("msg", "无法开启其他坑位");
                return map;
            }
            map.put("code", 200);
            // 获取更新后的redis信息返回
            return map;
        }catch (Exception e){
            map.put("code", 500);
            map.put("msg", "网络繁忙，请稍后重试!");
            return map;
        }
    }

    /**
     * 求对应等级狗的价格
     * 2018-12-18 15:48:42
     * @param level     等级
     * @param count     数量
     * @param increasePara  系数数组
     * @param price     价格对应数组
     * @return
     */
    private BigDecimal calcMoneyByCount(int level, int count, BigDecimal[] increasePara, BigDecimal[] price) {
        BigDecimal a = BigDecimal.ZERO;
        if (level < 5) {
            a = increasePara[0];
        } else if (level < 15) {
            a = increasePara[1];
        } else if (level < 25) {
            a = increasePara[2];
        } else if (level < 31) {
            a = increasePara[3];
        }
        return price[level - 1].multiply(a.pow(count)).setScale(0, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * @Description: 注入阳光
     * @param uuid 用户uuid
     * @param sun 注入阳光数
     * @return java.util.Map
     * @Date 2019-01-07 10:11:18
     */
    @RequestMapping(value = "/putSun", method = RequestMethod.POST)
    @ResponseBody
    public Map putSun(String uuid, BigDecimal sun) {
        Map<String,Object> res = new HashMap<>();
        Wallet wallet = walletService.getWalletByUuid(uuid);
        Integer transLock= wallet.getTransLock();
        if (transLock==2){
            res.put("code", 500);
            res.put("msg", "投入失败，请联系客服进行人工审核！");
            return res;
        }

        TTeachainLogin ttl = loginService.getLoginInfoByUuid(uuid);
        if (ttl.getStatus() == 2) {
            res.put("code", 500);
            res.put("msg", "安全模式，您的账号存在被盗风险！\n" +
                    "进入官方QQ群869800795解除安全模式。");
            return res;
        }

        Map patronSaintUser = patronSaintService.getPatronSaintUserByUuid(uuid);
        try {
            Integer maxLevel = (Integer) patronSaintUser.get("maxLevel");
            if (maxLevel <= 5) {
                if (ttl.getStatus() != 2) {
                    Date createTime = ttl.getCreateTime();
                    DateFormat df = new SimpleDateFormat("yyyyMMdd");
                    int createDateInt = Integer.valueOf(df.format(createTime));
                    int currDateInt = Integer.valueOf(df.format(new Date()));
                    if (createDateInt != currDateInt) {
                        // 拒绝
                        loginService.setStatusByUuid(uuid, 2);
                        res.put("code", 500);
                        res.put("msg", "安全模式，您的账号存在被盗风险！\n" +
                                "进入官方QQ群869800795解除安全模式。");
//                    walletService.setTransLock(uuid, 2);
//                    res.put("msg", "投入失败，请联系客服进行人工审核！");
                        return res;
                    }
                } else {

                }
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        if (sun==null)sun=new BigDecimal(0);
        sun = sun.setScale(0, BigDecimal.ROUND_DOWN);
        try(Jedis jedis = redisClient.getResource()) {
            try {
                if (sun.compareTo(BigDecimal.ZERO) <= 0) {
                    res.put("code", 500);
                    res.put("msg", "注入阳光数必须大于0");
                    return res;
                }
                if (redisClient.lock(jedis, uuid, 5000, 10000)) {
                    BigDecimal beforeSun = new BigDecimal(jedis.hget("user:" + uuid, "tea"));
                    if (beforeSun.compareTo(sun) >= 0) {
                        BigDecimal nowSun = beforeSun.subtract(sun);
                        jedis.hset("user:" + uuid, "tea", nowSun.toString());

                        Map<String, Object> map = new HashMap<>();
                        Map user = patronSaintService.getPatronSaintUserByUuid(uuid);
                        int putDay = user.containsKey("putDay") ? Integer.valueOf(user.get("putDay").toString()) : 0;
                        DateFormat df = new SimpleDateFormat("yyyyMMdd");
                        int date = Integer.valueOf(df.format(new Date()));
                        // 配置距离第一期开服时间后注入阳光
                        if (new Date().getTime() < 1548259200000L) {
                            if (20190123 != putDay) {
                                map.put("date", 20190123);
                            }
                        } else {
                            if (date != putDay) { // 会不会时间异常呢？
                                map.put("date", date);
                            }
                        }
                        map.put("tea", nowSun); // 剩余阳光数
                        map.put("uuid", uuid);
                        map.put("sun", sun);    //本次投放阳光数
                        patronSaintService.updateUserByParam(map);
                        patronSaintService.createPutSunLog(uuid, sun);
                        res.put("code", 200);
                    } else {
                        res.put("code", 500);
                        res.put("msg", "余额不足");
                    }
                } else {
                    res.put("code", 500);
                    res.put("msg", "网络错误,请稍后重试");
                }
                return res;
            } catch (InterruptedException e) {
                e.printStackTrace();
                res.put("code", 500);
                res.put("msg", "服务器阻塞");
                return res;
            } finally {
                redisClient.unlock(jedis, uuid);
            }
        }catch (Exception e){
            res.put("code", 500);
            res.put("msg", "网络繁忙，请稍后重试!");
            return res;
        }
    }

    /**
     * @Description:    开坑
     * @param uuid      用户uuid
     * @param target    解锁坑位 从0开始计数
     * @param tradeNo   充值单号
     * @param type      1 钻石 2 微信
     * @return java.util.Map
     * @Date 2019-01-08 17:33:57
     */
    @RequestMapping(value = "/unLockHole", method = RequestMethod.POST)
    @ResponseBody
    public Map unLockHole(String uuid, int target,String tradeNo,Integer type) {
        if (type == null)type=1; // 调试用
        Map<String,Object> map = new HashMap<>();
        try(Jedis jedis = redisClient.getResource()) {
            // 限制开坑位置 不限制有可能异常
            if (target < 5 || target > 8) {
                map.put("code", 500);
                map.put("msg", "当前位置无法开启坑位");
                return map;
            }
            try {
                if (redisClient.lock(jedis, uuid, 5000, 10000)) {
                    BigDecimal[] openCost = JSON.parseObject(jedis.hget("globalDeal", "openHoleCost"), BigDecimal[].class);
                    String dogs = jedis.get("dogs:" + uuid);
                    Map dogsMap = JSONObject.fromObject(dogs);
                    int[] levelArrays = JSON.parseObject(dogsMap.get("level").toString(), int[].class);
                    int level = levelArrays[target];
                    if (levelArrays[target - 1] == -1) {
                        map.put("code", 500);
                        map.put("msg", "请先开启第" + (target) + "坑位");
                        return map;
                    }
                    if (level != -1) {
                        map.put("code", 500);
                        map.put("msg", "请不要重复开启相同坑位");
                        return map;
                    }
                    if (type == 1) {    // 钻石
                        patronSaintService.synDiamondFromMYSQL(jedis, uuid); // todo 无心跳 无任何操作 后台与前端钻石显示不一致 钻石不足
                        TUser user = userService.getTUserByUuid(uuid);
                        int diamond = user.getDiamond();
                        Boolean aa = userService.updateDiamondFunc(uuid, openCost[target - 5], 1);
                        if (!aa) {
                            map.put("code", 500);
                            map.put("msg", "钻石不足");
                            return map;
                        }
                        jedis.hset("user:" + uuid, "diamond", String.valueOf(new BigDecimal(diamond).subtract(openCost[target - 5])));
                    } else if (type == 2) { //微信
                        Map param = rechargeService.spliceOrderQuery(uuid, tradeNo);
                        if (Integer.parseInt(param.get("code").toString()) == 200) {
                            BigDecimal money = new BigDecimal(param.get("realAmount").toString());
//                        if (money.compareTo(new BigDecimal("18")) != 0 &&
//                                money.compareTo(new BigDecimal("54")) != 0 &&
//                                money.compareTo(new BigDecimal("108")) != 0 &&
//                                money.compareTo(new BigDecimal("486")) != 0) {
//                            map.put("code", 500);
//                            map.put("msg", "充值失败，请求微信查询订单失败");
//                            return map;
//                        }
                            // 分润
                            if (commonController.updateProfit(uuid, money, openCost[target - 5], 1, "微信充值开坑", map))
                                return map;
                        } else {
                            map.put("code", 500);
                            map.put("msg", "请求微信查询订单失败");
                            return map;
                        }
                    } else {
                        map.put("code", 500);
                        map.put("msg", "请求类型错误");
                        return map;
                    }
                    map.put("code", 200);
                    // redis 开坑加速
                    if (!patronSaintService.setHoleAndSpeed(uuid, target, levelArrays, dogsMap, jedis)) {
                        map.put("code", 500);
                        map.put("msg", "开坑加速失败");
                    }
                    return map;
                } else {
                    map.put("code", 500);
                    map.put("msg", "网络错误,请稍后重试");
                    return map;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                map.put("code", 500);
                map.put("msg", "服务器阻塞");
                return map;
            } catch (Exception e) {
                e.printStackTrace();
                map.put("code", 500);
                map.put("msg", "解锁空位异常");
                return map;
            } finally {
                redisClient.unlock(jedis, uuid);
            }
        }catch (Exception e){
            map.put("code", 500);
            map.put("msg", "网络繁忙，请稍后重试!");
            return map;
        }
    }

    /**
     * @Description: 领取每日登陆奖励
     * @param uuid 用户uuid
     * @return java.util.Map
     * @Date 2019-01-07 10:11:18
     */
    @RequestMapping(value = "/getDailyReward", method = RequestMethod.POST)
    @ResponseBody
    public Map getDailyReward(String uuid) {
        Map<String,Object> map = new HashMap<>();
        try(Jedis jedis = redisClient.getResource()) {
            try {
                if (redisClient.lock(jedis, uuid, 5000, 10000)) {
                    long currentTime = System.currentTimeMillis();
                    Map user = patronSaintService.getRedisUser(jedis, uuid);
                    long rewardTime = Long.valueOf(user.get("rewardTime").toString());
                    long rewardZero = rewardTime / (1000 * 3600 * 24) * (1000 * 3600 * 24) - TimeZone.getDefault().getRawOffset();
                    int days = (int) ((currentTime - rewardZero) / (1000 * 3600 * 24));
                    if (days > 0) {
                        int rewardStatus = Integer.valueOf(user.get("rewardStatus").toString());
                        rewardStatus = rewardStatus == 7 ? 0 : rewardStatus;
                        int[] rewardType = JSON.parseObject(jedis.hget("globalDeal", "rewardType"), int[].class);
                        int[] rewardNumber = JSON.parseObject(jedis.hget("globalDeal", "rewardNumber"), int[].class);

                        switch (rewardType[rewardStatus]) {
                            case 1://金币
                                BigDecimal golden = new BigDecimal(user.get("golden").toString());
                                BigDecimal newGolden = golden.add(new BigDecimal(rewardNumber[rewardStatus]));
                                jedis.hset("user:" + uuid, "golden", newGolden.toString());
                                break;
                            case 2:// 阳光
                                BigDecimal tea = new BigDecimal(user.get("tea").toString());
                                BigDecimal newTea = tea.add(new BigDecimal(rewardNumber[rewardStatus]));
                                jedis.hset("user:" + uuid, "tea", newTea.toString());
                                break;
                            case 3:// 钻石
                                userService.setDiamond(uuid, rewardNumber[rewardStatus]);
                                int newDiamond = rewardNumber[rewardStatus] + Integer.valueOf(user.get("diamond").toString());
                                jedis.hset("user:" + uuid, "diamond", String.valueOf(newDiamond));
                                break;
                            default:
                                map.put("code", 500);
                                map.put("msg", "领取类型有误");
                                return map;
                        }
                        jedis.hset("user:" + uuid, "rewardStatus", String.valueOf(rewardStatus + 1));
                        jedis.hset("user:" + uuid, "rewardTime", String.valueOf(currentTime));
                        map.put("code", 200);
                    } else {
                        map.put("code", 500);
                        map.put("msg", "不可重复领取");
                    }
                    return map;
                } else {
                    map.put("code", 500);
                    map.put("msg", "网络错误,请稍后重试");
                    return map;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                map.put("code", 500);
                map.put("msg", "服务器阻塞");
                return map;
            } finally {
                redisClient.unlock(jedis, uuid);

            }
        }catch (Exception e){
            map.put("code", 500);
            map.put("msg", "网络繁忙，请稍后重试!");
            return map;
        }
    }

    /**
     * @Description: 查询每日登陆奖励相关信息
     * @param uuid 用户uuid
     * @return java.util.Map
     * @Date 2019-01-07 10:11:18
     */
    @RequestMapping(value = "/preDailyRewardInfo", method = RequestMethod.POST)
    @ResponseBody
    public Map preDailyRewardInfo(String uuid) {
        Map<String,Object> map = new HashMap<>();
        try(Jedis jedis = redisClient.getResource()) {
            try {
                int[] rewardType = JSON.parseObject(jedis.hget("globalDeal", "rewardType"), int[].class);
                int[] rewardNumber = JSON.parseObject(jedis.hget("globalDeal", "rewardNumber"), int[].class);

                int rewardStatus = Integer.valueOf(jedis.hget("user:" + uuid, "rewardStatus"));
                long rewardTime = Long.valueOf(jedis.hget("user:" + uuid, "rewardTime"));

                long rewardZero = rewardTime / (1000 * 3600 * 24) * (1000 * 3600 * 24) - TimeZone.getDefault().getRawOffset();
                long currentTime = System.currentTimeMillis();
                int days = (int) ((currentTime - rewardZero) / (1000 * 3600 * 24));

                if (rewardStatus == 7 && days > 0) {
                    rewardStatus = 0;
                    jedis.hset("user:" + uuid, "rewardStatus", "0");
                }

                map.put("code", 200);
                map.put("rewardType", rewardType);
                map.put("rewardNumber", rewardNumber);
                map.put("rewardStatus", rewardStatus);
                map.put("rewardTime", rewardTime);
                return map;
            } catch (Exception e) {

                map.put("code", 500);
                map.put("msg", "获取每日奖励信息异常!");
                return map;
            }
        }catch (Exception e){
            map.put("code", 500);
            map.put("msg", "网络繁忙，请稍后重试!");
            return map;
        }
    }
}