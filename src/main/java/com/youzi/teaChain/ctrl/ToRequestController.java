package com.youzi.teaChain.ctrl;

import com.youzi.teaChain.bean.TTeachainLogin;
import com.youzi.teaChain.bean.TUser;
import com.youzi.teaChain.common.*;
import com.youzi.teaChain.service.*;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import redis.clients.jedis.Jedis;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Controller
@Scope(value = "prototype")    //非scope=”singleton”单例模式注解，每次请求单独的Action，线程安全
@RequestMapping("/toRequest")
public class ToRequestController {
    private Logger log = LoggerFactory.getLogger(ToRequestController.class);

    @Resource
    private UserService userService;
    @Resource
    private WalletService walletService;
    @Resource
    private CoinCalcService coinCalcService;
    @Resource
    private LoginService loginService;
    @Resource
    private PatronSaintController patronSaintController;
    @Resource
    private PatronSaintService patronSaintService;
    @Resource
    private RedisClient redisClient;

    @Value("${wx.appId}")
    private String appId;
    @Value("${wx.appSecret}")
    private String appSecret;

    /**
     * @Description: 微信号登录
     * @param code 请求微信授权使用
     * @param deviceID 设备ID
     * @param flag 1.自动登录 2.微信登录
     * @return java.util.Map<java.lang.String,java.lang.Object>
     * @Date 2018-11-13 10:48:39
     */
    @RequestMapping(value = "/getWXData", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> getWXData(HttpServletRequest request, String code, String deviceID, Integer flag) throws Exception {
//        Object auth2TokenTime = session.getAttribute("auth2TokenTime");
        // 校验是否请求过token
        if (flag == 1 && deviceID.equals("unknown")){
            flag = 2;
        }
        if (flag == 1) {
            Map<String, Object> map = new HashMap<>();
            int count = loginService.countDeviceId(deviceID);
            if (count != 1) {
                loginService.clearOtherDeviceIdByOpenId("1", deviceID);
                try {
//                    map.put("code", 5001);
                    map.put("code", ErrorCode.NOTFOUNDPLAYER);
                } catch (Exception e) {
                    e.printStackTrace();
                    log.error(e.getMessage());
                }
                map.put("msg", "设备ID异常，请使用微信登录");
            } else {
                TTeachainLogin tTeachainLogin = loginService.getLoginByDeviceId(deviceID);
                if(tTeachainLogin != null) {
                    String uuid = tTeachainLogin.getUuid();
                    Date lastFreshTime = tTeachainLogin.getLastFreshTime();
                    Date curTime = new Date();
                    // 15天强制走一下正常登陆流程 for 同步微信信息
                    if(curTime.getTime() - lastFreshTime.getTime() < 60*60*15*24*1000){     // 十五天过期
                        String curPlayerOpenId = tTeachainLogin.getOpenId();
                        String ip = IPUtil.getIpAddr(request);
                        boolean updateDeviceSuccess = loginService.setDeviceIdAndIpByOpenId(curPlayerOpenId, deviceID,ip);// 更改设备id
                        if (updateDeviceSuccess) {
                            loginService.setStatusByDeviceId(deviceID,1);
                            Map tcc = userService.getPersonTeaCoin(uuid);
                            TUser tUser = userService.getTUserByUuid(uuid);
                            String openId = loginService.getLoginInfoByUuid(uuid).getOpenId();
                            if (patronSaintService.getPatronSaintUserByUuid(uuid)==null) {
                                try(Jedis jedis = redisClient.getResource()) {
                                    patronSaintService.initUser(uuid, jedis);
                                }catch (Exception e){
                                    map.put("code", 500);
                                    map.put("msg", "网络繁忙，请稍后重试!");
                                    return map;
                                }
                            }
                            Boolean openHoleSuccess = userService.openHoleAuto(uuid);
                            if (tUser!=null && tcc!=null && openId!=null && openHoleSuccess){
                                map.put("code", 200);
                                map.put("tUser", tUser);
                                map.put("tcc",tcc);
                                map.put("openId",openId);
                            }else {
                                map.put("code", 500);
                                map.put("msg", "登录异常!");
                            }
                        } else {
                            map.put("code", 500);
                            map.put("msg", "更改设备出错!");
                        }
                    }else{
                        loginService.logoutByDeviceId(deviceID);
                        loginService.clearOtherDeviceIdByOpenId("1",deviceID);
                        map.put("code", 500);
                        map.put("msg", "设备过期!");
                    }
                }else { // 目前走不到 待修改 app无登录状态
                    loginService.logoutByDeviceId(deviceID);
                    map.put("code", 500);
                    map.put("msg", "该设备未正常下线!");
                }
            }
            return map;
        } else {
            return getWXDataHttp(request, code, deviceID);
        }
    }

    /**
     * @Description: 获取授权用户信息的Http请求
     * @param code 请求微信授权使用
     * @param deviceID 设备ID
     * @return java.util.Map<java.lang.String,java.lang.Object>
     * @Date 2018-10-18 14:18:24
     */
    public Map<String, Object> getWXDataHttp(HttpServletRequest request, String code, String deviceID) throws Exception {
        Map<String, Object> map = new HashMap<>();
        String url = "https://api.weixin.qq.com/sns/oauth2/access_token";
        Map<String, Object> param = new HashMap<>();
        param.put("appid", appId);
        param.put("secret", appSecret);
        param.put("code", code);
        param.put("grant_type", "authorization_code");

        System.out.println("请求access_token前" + new Date());
        String getToken = HttpPostGet.getPost(url, param);
        System.out.println("请求access_token后" + new Date());
        Map res_token = JSONObject.fromObject(getToken);
        String access_token;
        String openId;
        if (res_token.containsKey("access_token")) {
            access_token = res_token.get("access_token").toString();
            openId = res_token.get("openid").toString();
//            String refresh_token = res_token.get("refresh_token").toString();
//            int time = (Integer) res_token.get("expires_in");
//            Date date = new Date();
//            long auth2TokenTime = date.getTime()/1000 + time;
//            session.setAttribute("auth2TokenTime", auth2TokenTime);
//            session.setAttribute("auth2Token", res_token);
//            session.setAttribute("refresh_token", refresh_token);
        } else {
            String msg = res_token.get("errmsg").toString();
            map.put("code", 500);
            map.put("msg", msg);
            return map;
        }
        return getWXTuser(request, openId, access_token, deviceID);
    }

    /**
     * @Description: 通过openId查找User返回Map
     * @param openId 微信openId 用户唯一标识
     * @param access_token 请求用户信息使用的授权token
     * @param deviceID 设备ID
     * @return java.util.Map<java.lang.String,java.lang.Object>
     * @Date 2018-10-18 16:51:25
     */
    private Map<String, Object> getWXTuser(HttpServletRequest request, String openId, String access_token, String deviceID) throws Exception {
        Map<String, Object> map3 = new HashMap<>();
        // 查询该用户是否已注册 微信登录通过openid 自动登录通过deviceID
        String url2 = "https://api.weixin.qq.com/sns/userinfo";
        Map<String, Object> map2 = new HashMap<>();
        map2.put("access_token", access_token);
        map2.put("openid", openId);
        map2.put("lang", "zh_CN");
        System.out.println("请求userinfo前" + new Date());
        String getUserInfo = HttpPostGet.getPost(url2, map2);
        System.out.println("请求userinfo后" + new Date());
        Map res_userInfo = JSONObject.fromObject(getUserInfo);
        String nickName = null;
        String headimgurl = null;
        int sex = 0;
        String unionId = null;
        String ip = IPUtil.getIpAddr(request);
//            String province = null;
//            String country = null;
        if (res_userInfo.containsKey("openid")) {
            nickName = res_userInfo.get("nickname").toString();
            String reg = "[\ud83c\udc00-\ud83c\udfff]|" +
                    "[\ud83d\udc00-\ud83d\udfff]|" +
                    "[\ud83e\udc00-\ud83e\udfff]|" +
                    "[\u2600-\u27ff]|" +
                    "[\uDB40\uDC00]" +
                    "";
            Pattern emoji = Pattern.compile(reg, Pattern.UNICODE_CASE | Pattern.CASE_INSENSITIVE);
            Matcher emojiMatcher = emoji.matcher(nickName);
            if (emojiMatcher.find()) {
                //将所获取的表情转换为*
                nickName = emojiMatcher.replaceAll("*");
            }
            headimgurl = res_userInfo.get("headimgurl").toString();
//            System.out.println(res_userInfo.get("headimgurl") + "------头像获取------");
            sex = Integer.parseInt(res_userInfo.get("sex").toString());
            unionId = res_userInfo.get("unionid").toString();
//                country = res_userInfo.get("country").toString();
//                province = res_userInfo.get("province").toString();
//            String Xxx = res_userInfo.get("Xxx").toString();  // 暂时只取昵称
        }
        Map<String, Object> param1 = new HashMap<>();
        String uuid = SHA.encryptSHA(openId);
        param1.put("uuid", uuid);
//        param1.put("phone", 0);
//            param1.put("icon", StringUtil.icon);
        param1.put("icon", headimgurl);
        param1.put("sex", sex);
//            param.put("country", country);
//            param.put("province", province);
        param1.put("openId", openId);
        param1.put("nickName", nickName);
        param1.put("deviceId", deviceID);
        param1.put("unionId", unionId);
        param1.put("ip", ip);

        try {
            if (patronSaintService.getPatronSaintUserByUuid(uuid) == null) {
                try (Jedis jedis = redisClient.getResource()) {
                    patronSaintService.initUser(uuid, jedis);
                } catch (Exception e) {
                    map3.put("code", 500);
                    map3.put("msg", "网络繁忙，请稍后重试!");
                    return map3;
                }
            }
            TTeachainLogin tTeachainLogin = loginService.checkLoginByOpenId(openId);
            loginService.clearOtherDeviceIdByOpenId(openId, deviceID);
            // 没有该openId就注册一个
            if (tTeachainLogin != null) {
                Boolean aa = userService.updateWXInfo(param1);
                Boolean bb = loginService.setDeviceIdAndIpByOpenId(openId, deviceID, ip);
                Boolean cc = loginService.updateUnionId(openId, unionId);
//            TTeachainLogin tTeachainLogin1 = loginService.checkLoginByOpenId(openId);
                TUser tUser = userService.getTUserByUuid(uuid);
                Boolean openHoleSuccess = userService.openHoleAuto(uuid);
                if (aa && bb && cc && tUser != null && openHoleSuccess) {
                    loginService.setStatusByDeviceId(deviceID, 1);
                    Map tcc = userService.getPersonTeaCoin(tUser.getUuid());
                    map3.put("code", 200);
                    map3.put("tUser", tUser);
                    map3.put("tcc", tcc);
                    map3.put("openId", openId);
                } else {
                    map3.put("code", 500);
                    map3.put("msg", "微信授权用户更新出错");
                }
            } else {
                System.out.println("注册用户前" + new Date());
                Boolean aa = userService.insertTUserByWXUser(param1);
                Boolean bb = loginService.insertTTeachainLogin(param1);
                TTeachainLogin tTeachainLogin1 = loginService.checkLoginByOpenId(openId);
                TUser newtUser = userService.getTUserByUuid(uuid);
                Map<String, Object> param2 = new HashMap<>();
                param2.put("uuid", uuid);
                String coin_address = StringUtil.createCoinAddress(uuid);
                param2.put("coin_address", coin_address);
                Boolean cc = walletService.insertWalletByWXUser(param2);
                Boolean setSuperiorSuccess = userService.setSuperior(uuid);
                if (aa && bb && cc && tTeachainLogin1 != null && setSuperiorSuccess) {
                    loginService.setStatusByDeviceId(deviceID, 1);
                    coinCalcService.insertCoinCalcByUser(uuid);
                    Map tcc = userService.getPersonTeaCoin(uuid);
                    System.out.println("注册用户后" + new Date());
                    map3.put("code", 200);
                    map3.put("tUser", newtUser);
                    map3.put("tcc", tcc);
                    map3.put("openId", openId);
                } else {
                    map3.put("code", 500);
                    map3.put("msg", "微信授权用户注册出错");
                }
            }
            return map3;
        }catch (Exception e){
            map3.put("code", 500);
            map3.put("msg", "设置用户信息出错");
            return map3;
        }
    }

}
