package com.youzi.teaChain.ctrl;

import com.youzi.teaChain.bean.TTeachainCoinCalc;
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
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.youzi.teaChain.common.StringUtil.checkCellPhone;

@Controller
@Scope(value="singleton")	//非scope=”singleton”单例模式注解，每次请求单独的Action，线程安全
@RequestMapping("/index")
public class LoginController {
    private Logger log = LoggerFactory.getLogger(LoginController.class);

    @Resource
	private UserService userService;
    @Resource
	private LoginService loginService;
    @Resource
	private WalletService walletService;
    @Resource
	private CoinCalcService coinCalcService;
    @Resource
	private ConfigService configService;
    @Resource
	private CodeLogService codeLogService;
    @Resource
	private ShareService shareService;
    @Resource
    private RedisClient redisClient;
    @Resource
    private RechargeProfitLogService rechargeProfitLogService;

    @Value("${wx.appId}")
    private String appId;
    @Value("${wx.appSecret}")
    private String appSecret;

    /**
     * @Description: 模拟登录使用
     * @param name 用户名
     * @param password 密码
     * @return java.util.Map<java.lang.String,java.lang.Object>
     * @Date 2018-11-13 10:33:03
     */ 
	@RequestMapping(value = "/login", method = RequestMethod.POST)
    @ResponseBody
	public Map<String, Object> loginTest(Model model, HttpSession session, String name, String password){
	    Map<String, Object> map = new HashMap<>();
	    // 校验是否之前要登录，是清除session
        Object sUser = session.getAttribute("user");
        if (sUser != null) {
            session.invalidate();
        }
        Boolean aa = userService.checkUserName(name);
        if (!aa) {
            map.put("code", ErrorCode.NOTFOUNDPLAYER);
            map.put("msg", "该账号未注册！");
            return map;
        }
	    TUser user = userService.checkLogin(name, password);
		if(user == null){
//			System.out.println("-----login failed------");
            model.addAttribute("msg", "您的密码错误！");
            map.put("code", 500);
            map.put("msg", "您的密码错误！");
		}else {
//			System.out.println("-----login success------");
            session.setAttribute("user", user);
            map.put("tUser",user);
            map.put("code", 200);
//            return "views/home";
		}
        return map;
	}

	/**
	 * @Description: 手机号登录
	 * @param phone 手机号
	 * @param password 密码
	 * @return java.util.Map<java.lang.String,java.lang.Object>
	 * @Date 2018-11-13 10:39:38
	 */
    @RequestMapping(value = "/loginByPhone", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> loginByPhone(HttpServletRequest request,String phone, String password, String deviceID) throws Exception {
        Map<String, Object> map = new HashMap<>();
        TTeachainLogin tTeachainLogin = loginService.getLoginByPhone(phone);
        if (tTeachainLogin!=null && tTeachainLogin.getPassword() != null) {
            if (tTeachainLogin.getPassword().equals(SHA.encryptSHA(password))) {
                String uuid = tTeachainLogin.getUuid();
                TUser tUser = userService.getTUserByUuid(uuid);
                String ip = IPUtil.getIpAddr(request);
                loginService.setDeviceIdAndIpByPhone(phone,deviceID,ip);
                loginService.setStatusByDeviceId(deviceID,1);
                Map tcc = userService.getPersonTeaCoin(uuid);
                String openId = loginService.getLoginInfoByUuid(uuid).getOpenId();
                userService.openHoleAuto(uuid);
                map.put("code", 200);
                map.put("tUser", tUser);
                map.put("tcc", tcc);
                map.put("openId", openId);
            }else {
                map.put("code", 500);
                map.put("msg", "您的密码错误！");
            }
        }else{
            map.put("code",ErrorCode.NOTFOUNDPLAYER);
            map.put("msg", "该手机号未注册！");
        }
        return map;
    }

    @RequestMapping(value = "/loginByPhoneCode", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> loginByPhoneCode(HttpServletRequest request, String phone, int code, String deviceID){
        Map<String, Object> map = new HashMap<>();
        BigInteger phoneInt = checkCellPhone(phone);
        int resCode = codeLogService.selectPhoneCode("-1", phoneInt);
        if (resCode == 0) {
            map.put("code", 500);
            map.put("msg", "验证码已失效，请重新获取验证码！");
            return map;
        }
        if (code == resCode) {
            TTeachainLogin tTeachainLogin = loginService.getLoginByPhone(phone);
            String uuid = tTeachainLogin.getUuid();
            TUser tUser = userService.getTUserByUuid(uuid);
            String ip = IPUtil.getIpAddr(request);
            loginService.setDeviceIdAndIpByPhone(phone,deviceID,ip);
            loginService.setStatusByDeviceId(deviceID,1);
            Map tcc = userService.getPersonTeaCoin(uuid);
            String openId = loginService.getLoginInfoByUuid(uuid).getOpenId();
            userService.openHoleAuto(uuid);
            map.put("code", 200);
            map.put("tUser", tUser);
            map.put("tcc", tcc);
            map.put("openId", openId);
        } else {
            map.put("code", 500);
            map.put("msg", "验证码输入有误，请仔细确认后输入！");
        }
        return map;
    }

    /**
     * @Description: 账号登出
     * @param deviceID 设备ID
     * @return java.util.Map<java.lang.String,java.lang.Object>
     * @Date 2018-11-13 10:40:31
     */
    @RequestMapping(value = "/logout")
    @ResponseBody
    public Map<String, Object> logout(String deviceID){
        // 清除Session
//        session.invalidate();
        Map<String, Object> map = new HashMap<>();
        boolean logoutSuccess = loginService.logoutByDeviceId(deviceID);
        if (logoutSuccess){
            map.put("code",200);
        }else{
            map.put("code",500);
            map.put("msg","用户登出失败");
        }
        return map;
    }

    @RequestMapping(value = "/getWXAccess")
    @ResponseBody
    public void getWXAccess(Model model, HttpServletRequest request, HttpServletResponse response) throws Exception {
        // 微信加密签名
        String signature = request.getParameter("signature");
        // 时间戳
        String timestamp = request.getParameter("timestamp");
        // 随机数
        String nonce = request.getParameter("nonce");
        // 随机字符串
        String echostr = request.getParameter("echostr");
        PrintWriter out = response.getWriter();
        if (SignUtil.checkSignature(signature, timestamp, nonce)) {
            model.addAttribute(echostr);
            out.print(echostr);
        }
        out.close();
        out = null;
    }

    /**
     * 先获取access_token再获取ticket
     * 2018-9-17 15:01:21
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/getTicket")
    @ResponseBody
    public Map getTicket(HttpServletRequest httpServletRequest ,HttpSession session) throws Exception {
        Object expiresTimeO = session.getAttribute("expiresTime");
        // 校验是否请求过token
        if (expiresTimeO != null) {
            long expiresTime = (Long) expiresTimeO;
            Date date = new Date();
            // 校验token是否过期
            if (expiresTime <= date.getTime()/1000) {
                return getTicketHttp(session);
            } else {
                Map res_ticket = (Map) session.getAttribute("ticketMap");
                return res_ticket;
            }
        } else {
            return getTicketHttp(session);
        }
	}

    /**
     * 获取ticket的Http请求
     * @param session
     * @return
     */
	public Map getTicketHttp(HttpSession session) {
        String url = "https://api.weixin.qq.com/cgi-bin/token";
        Map<String, Object> map = new HashMap<>();
        map.put("grant_type", "client_credential");
        map.put("appid", appId);
        map.put("secret", appSecret);
        String getToken = HttpPostGet.getPost(url, map);
        Map res_token = (Map) JSONObject.fromObject(getToken);
        String access_token = null;
        if (res_token.containsKey("access_token")) {
            access_token = res_token.get("access_token").toString();
            int time = (Integer) res_token.get("expires_in");
            Date date = new Date();
            long expiresTime = date.getTime()/1000 + time;
            session.setAttribute("expiresTime", expiresTime);
        } else {
            return res_token;
        }
        String url2 = "https://api.weixin.qq.com/cgi-bin/ticket/getticket";
        Map<String, Object> map2 = new HashMap<>();
        map2.put("access_token", access_token);
        map2.put("type", "jsapi");
        String getTicket = HttpPostGet.getPost(url2, map2);
        Map res_ticket = (Map) JSONObject.fromObject(getTicket);
        session.setAttribute("ticketMap", res_ticket);
        return res_ticket;
    }

    /**
     * @Description: 首页领取茶币 (暂弃)
     * @param uuid 用户uuid
     * @param index 领取茶币索引
     * @return java.util.Map<java.lang.String,java.lang.Object>
     * @Date 2018-11-13 10:36:09
     */
//    @RequestMapping(value = "/getTCCFromTree")
//    @ResponseBody
    public Map<String, Object> getTCCFromTree(String uuid,int index){
        Map<String, Object> res = new HashMap<>();
        // 绑定手机 加算力
        synchronized (this) {
            TUser tUser = userService.getTUserByUuid(uuid);
            Integer a = tUser.getHasFirstTCC();
            Integer hasSuperior = tUser.getSuperior();
            if (a == null || a.equals(0)) {
                TTeachainLogin tTeachainLogin = loginService.getLoginInfoByUuid(uuid);
                int superior = shareService.selectShareUserId(tTeachainLogin.getOpenId(), tTeachainLogin.getUnionId());
                String superior_uuid = userService.getUuidById(superior);
                if (superior != 0 && hasSuperior == null && !superior_uuid.equals(uuid)) {
//                    userService.setSuperior(uuid, superior);
//                    userService.updateCalcPower(superior_uuid, uuid, 1, new BigDecimal("3"), 0, 0);   /原加算力无用
//                    userService.openHoleAuto(superior, superior_uuid, jedis);
                }
            }
            TTeachainCoinCalc tTeachainCoinCalc = coinCalcService.selectCoinCalc(uuid);
            Integer checkTime = configService.distributeTCCTime();
            BigDecimal tcc = BigDecimal.ZERO;
            TTeachainCoinCalc newTCC = new TTeachainCoinCalc();
            newTCC.setUuid(uuid);
            if (checkTime == 3) {
                switch (index) {
                    case 0:
                        newTCC.setTeaCoin15(tcc);
                        tcc = tTeachainCoinCalc.getTeaCoin15();
                        break;
                    case 1:
                        newTCC.setTeaCoin16(tcc);
                        tcc = tTeachainCoinCalc.getTeaCoin16();
                        break;
                    case 2:
                        newTCC.setTeaCoin17(tcc);
                        tcc = tTeachainCoinCalc.getTeaCoin17();
                        break;
                    case 3:
                        newTCC.setTeaCoin18(tcc);
                        tcc = tTeachainCoinCalc.getTeaCoin18();
                        break;
                    case 4:
                        newTCC.setTeaCoin19(tcc);
                        tcc = tTeachainCoinCalc.getTeaCoin19();
                        break;
                    case 5:
                        newTCC.setTeaCoin20(tcc);
                        tcc = tTeachainCoinCalc.getTeaCoin20();
                        break;
                    case 6:
                        newTCC.setTeaCoin21(tcc);
                        tcc = tTeachainCoinCalc.getTeaCoin21();
                        break;
                    default:
                        res.put("code", 500);
                        res.put("msg", "未到领取时间,领取失败!");
                        return res;
                }
            } else if (checkTime == 2) {
                switch (index) {
                    case 0:
                        newTCC.setTeaCoin8(tcc);
                        tcc = tTeachainCoinCalc.getTeaCoin8();
                        break;
                    case 1:
                        newTCC.setTeaCoin9(tcc);
                        tcc = tTeachainCoinCalc.getTeaCoin9();
                        break;
                    case 2:
                        newTCC.setTeaCoin10(tcc);
                        tcc = tTeachainCoinCalc.getTeaCoin10();
                        break;
                    case 3:
                        newTCC.setTeaCoin11(tcc);
                        tcc = tTeachainCoinCalc.getTeaCoin11();
                        break;
                    case 4:
                        newTCC.setTeaCoin12(tcc);
                        tcc = tTeachainCoinCalc.getTeaCoin12();
                        break;
                    case 5:
                        newTCC.setTeaCoin13(tcc);
                        tcc = tTeachainCoinCalc.getTeaCoin13();
                        break;
                    case 6:
                        newTCC.setTeaCoin14(tcc);
                        tcc = tTeachainCoinCalc.getTeaCoin14();
                        break;
                    default:
                        res.put("code", 500);
                        res.put("msg", "未到领取时间,领取失败!");
                        return res;
                }
            } else if (checkTime == 1) {
                switch (index) {
                    case 0:
                        newTCC.setTeaCoin1(tcc);
                        tcc = tTeachainCoinCalc.getTeaCoin1();
                        break;
                    case 1:
                        newTCC.setTeaCoin2(tcc);
                        tcc = tTeachainCoinCalc.getTeaCoin2();
                        break;
                    case 2:
                        newTCC.setTeaCoin3(tcc);
                        tcc = tTeachainCoinCalc.getTeaCoin3();
                        break;
                    case 3:
                        newTCC.setTeaCoin4(tcc);
                        tcc = tTeachainCoinCalc.getTeaCoin4();
                        break;
                    case 4:
                        newTCC.setTeaCoin5(tcc);
                        tcc = tTeachainCoinCalc.getTeaCoin5();
                        break;
                    case 5:
                        newTCC.setTeaCoin6(tcc);
                        tcc = tTeachainCoinCalc.getTeaCoin6();
                        break;
                    case 6:
                        newTCC.setTeaCoin7(tcc);
                        tcc = tTeachainCoinCalc.getTeaCoin7();
                        break;
                    default:
                        res.put("code", 500);
                        res.put("msg", "未到领取时间,领取失败!");
                        return res;
                }
            } else {
                res.put("code", 500);
                res.put("msg", "未到领取时间,领取失败!");
                return res;
            }

            coinCalcService.updateTCC(newTCC);
            Boolean changeBalanceSuccess = walletService.updateWalletBalanceFunc(uuid, tcc, 0);
            if (changeBalanceSuccess) {
                res.put("code", 200);
            } else {
                res.put("code", 500);
                res.put("msg", "领取失败!");
            }
            return res;
        }
    }

    /**
     * 获取茶币排行榜(老版本)
     * @param uuid
     * @param type
     * @return
     */
//    @RequestMapping(value = "/getRankList", method = RequestMethod.POST) // 20190131 关
//    @ResponseBody
    public Map<String, Object> getRankList(String uuid, int type){
        Map<String, Object> res = new HashMap<>();
        List<Map> rankList = userService.getRankList(type);
        if (rankList != null){
            int selfRank = 0;
            for (int i=0;i<rankList.size();i++){
                if(rankList.get(i).get("uuid").equals(uuid)){
                    selfRank = i+1;
                }
                rankList.get(i).remove("uuid");
            }
            res.put("code",200);
            res.put("rankList",rankList);
            res.put("self",selfRank);
        }else {
            res.put("code",500);
            res.put("msg","查询排行榜发生异常!");
        }
        return res;
    }

    @RequestMapping(value = "/getCalcRelation", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> getCalcRelation(String uuid){
        Map<String, Object> res = new HashMap<>();
        List<Map> calcRelation=userService.getCalcRelation(uuid);
        if(calcRelation!=null){
            res.put("code",200);
            res.put("calcRelation",calcRelation);
        }else {
            res.put("code", 500);
            res.put("msg", "查询异常!");
        }
        return res;
    }

    /**
     * 获取个人分润列表
     * 2019-1-8 10:09:41
     * @param uuid  用户标识
     * @return
     */
    @RequestMapping(value = "/getPersonProfit", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> getPersonProfit(String uuid){
        Map<String, Object> map = new HashMap<>();
        List list = rechargeProfitLogService.getPersonProfitList(uuid);
        String diamondUpDownRadio = configService.selectConfig("diamondUpDownRadio");
        String cnyUpDownRadio = configService.selectConfig("cnyUpDownRadio");
        if (list != null) {
            map.put("code",200);
            map.put("profitList",list);
            map.put("diamondRadio",new BigDecimal(diamondUpDownRadio));
            map.put("cnyRadio",new BigDecimal(cnyUpDownRadio));
        } else {
            map.put("code", 500);
            map.put("msg", "查询分润列表异常!");
        }
        return map;
    }

    /**
     * 查询用户的邀请下级
     * 2019-1-29 14:34:32
     * @param uuid
     * @return
     */
    @RequestMapping(value = "/getLowerRelation", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> getLowerRelation(String uuid, int type){
        Map<String, Object> res = new HashMap<>();
        List<Map> relation = userService.getLowerRelation(uuid, type);        // type: 0.有效邀请下级 1.无效下级
        if(relation != null){
            res.put("code",200);
            res.put("relation",relation);
        }else {
            res.put("code", 500);
            res.put("msg", "查询异常!");
        }
        return res;
    }
}
