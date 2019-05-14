package com.youzi.teaChain.ctrl;

import com.youzi.teaChain.bean.TTeachainRechargeLog;
import com.youzi.teaChain.bean.TUser;
import com.youzi.teaChain.common.StringUtil;
import com.youzi.teaChain.common.XMLUtil;
import com.youzi.teaChain.common.wxpay.sdk.WXPayConstants;
import com.youzi.teaChain.service.LoginService;
import com.youzi.teaChain.service.RechargeService;
import com.youzi.teaChain.service.UserService;
import org.jdom2.JDOMException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@Scope(value = "prototype")    //非scope=”singleton”单例模式注解，每次请求单独的Action，线程安全
@RequestMapping("/recharge")
public class RechargeController {
    private Logger log = LoggerFactory.getLogger(RechargeController.class);

    @Resource
    private RechargeService rechargeService;
    @Resource
    private UserService userService;
    @Resource
    private LoginService loginService;
    @Resource
    private CommonController commonController;
    @Resource
    private DealController dealController;
    @Value("${wx.pay.secret}")
    private String key;

    /**
     * 充值自定义CNY或者直接充值VIP
     * @param uuid
     * @param money
     * @param type
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/wxPay", method = RequestMethod.POST)
    @ResponseBody
    public Map rechargeSilver(String uuid, BigDecimal money, int type) throws Exception {
        String openId = loginService.getLoginInfoByUuid(uuid).getOpenId();
        String amount = money.multiply(new BigDecimal(100)).setScale(0, BigDecimal.ROUND_DOWN).toString();       // 单位为分
        String body, uType;
        switch (type) {
            case 0:
                body = "云庄园充值CNY "+ money.toString() + "元";
                uType = "0";
                break;
            case 1:
                body = "云庄园充值VIP "+ money.toString() + "元/月";
                uType = "1";
                break;
            default:
                //待补充
                body = "";
                uType = "E";
        }
//        amount = "1";
        return rechargeService.spliceRecharge(uuid, amount, body, uType);
    }

    /**
     * 微信充值查询订单后再做CNY充值
     * @param tradeNo
     * @return
     * @throws Exception
     */
//    @RequestMapping(value = "/orderQuery", method = RequestMethod.POST)
//    @ResponseBody
    public Map orderQuery(String uuid, String tradeNo) throws Exception {
        Map<String, Object> map = new HashMap<>();
        Map param = rechargeService.spliceOrderQuery(uuid, tradeNo);
        if (param != null && Integer.parseInt(param.get("code").toString()) == 200) {
            BigDecimal amount = new BigDecimal(param.get("realAmount").toString());
//            amount = new BigDecimal("50");
            userService.updateCNYFunc(uuid, amount, 0);       // 0.增加 1.减少
//            userService.changeTotalMoney(uuid, amount, 0);
            map.put("code", 200);
            return map;
        } else {
            return param;
        }
    }

    /**
     * 充值自定义钻石
     * 2019-1-14 15:39:45
     * @param uuid
     * @param money     充值金额
     * @param type      0.充值自定义钻石 1.开坑充值差额钻石
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/wxPayDiamond", method = RequestMethod.POST)
    @ResponseBody
    public Map wxPayDiamond(String uuid, BigDecimal money, int type) throws Exception {
        String body, uType;
        switch (type) {
            case 0:
                body = "云庄园钻石充值 "+ money.toString() + "元";
                uType = "2";
                break;
            case 1:
                body = "云庄园游戏充值";
                uType = "3";
                break;
            default:
                //待补充
                body = "";
                uType = "E";
        }
        String amount = money.multiply(new BigDecimal("100")).setScale(0, BigDecimal.ROUND_DOWN).toString();       // 单位为分
        return rechargeService.spliceRecharge(uuid, amount, body, uType);
    }

    /**
     * 充值钻石微信订单查询
     * @param tradeNo
     * @return
     * @throws Exception
     */
//    @RequestMapping(value = "/diamondOrderQuery", method = RequestMethod.POST)
//    @ResponseBody
    public Map diamondOrderQuery(String uuid, String tradeNo) throws Exception {
        Map<String, Object> map = new HashMap<>();
        Map param = rechargeService.spliceOrderQuery(uuid, tradeNo);
        if (param != null && Integer.parseInt(param.get("code").toString()) == 200) {
            BigDecimal amount = new BigDecimal(param.get("realAmount").toString());
            userService.updateDiamondFunc(uuid, amount.multiply(BigDecimal.TEN), 0);       // 0.增加 1.减少
            if (commonController.updateProfit(uuid, amount, amount.multiply(BigDecimal.TEN), 0, "微信充值钻石", map))
                return map;
            map.put("code", 200);
            return map;
        } else {
            return param;
        }
    }

    /**
     * CNY充值钻石
     * 2019-1-17 11:13:55
     * @param uuid      用户标识
     * @param diamond   钻石数量
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/cnyRechargeDiamond", method = RequestMethod.POST)
    @ResponseBody
    public Map cnyRechargeDiamond(String uuid, BigDecimal diamond) throws Exception {
        Map<String, Object> map = new HashMap<>();
        if (diamond.compareTo(BigDecimal.ZERO) <= 0) {
            map.put("warning", "少来臭不要脸的刷接口！");
            return map;
        }
        diamond = diamond.setScale(0, BigDecimal.ROUND_DOWN);
        TUser tUser = userService.getTUserByUuid(uuid);
        BigDecimal CNY = tUser.getTotalMoney();
        BigDecimal reCny = diamond.divide(BigDecimal.TEN);
        if (CNY.compareTo(reCny) < 0) {
            map.put("code", 500);
            map.put("msg", "CNY不足！");
            return map;
        } else {
            Boolean changeMoneySuccess = userService.updateCNYFunc(uuid, reCny, 1);
            if (!changeMoneySuccess){
                map.put("code", 500);
                map.put("msg", "扣除CNY失败！");
                return map;
            }
        }
        userService.updateDiamondFunc(uuid, diamond, 0);       // 0.增加 1.减少
        if (commonController.updateProfit(uuid, reCny, diamond, 0, "微信充值钻石", map))
            return map;
        map.put("code", 200);
        return map;
    }

    @RequestMapping(value = "/getRechargeOrder")
    @ResponseBody
    public Map<String, Object> getRechargeOrder(String uuid){
        Map<String, Object> map = new HashMap<>();
        List<TTeachainRechargeLog> RechargeOrderList = rechargeService.getRechargeOrder(uuid);
        if (RechargeOrderList!=null){
            map.put("code",200);
            map.put("RechargeOrderList",RechargeOrderList);
        }else{
            map.put("code",500);
            map.put("msg","获取充值订单失败");
        }
        return map;
    }

    /**
     * 微信支付后给前台返回是否加载结果
     * 2019-1-28 14:30:55
     * @param tradeNo
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/getWXRes", method = RequestMethod.POST)
    @ResponseBody
    public Map getWXRes(String tradeNo) throws Exception {
        Map map = new HashMap();
        int waitTime = 5000;
        int sleepTime = 500;
        while (waitTime > 0) {
            int checkCount = rechargeService.checkTradeNo(tradeNo);
            if (checkCount > 0) {
                map.put("code", 200);
                return map;
            } else {
                Thread.sleep(sleepTime);
                waitTime-=sleepTime;
            }
        }
        map.put("code", 500);
        map.put("msg", "充值失败，请检查网络及应用");
        return map;
    }

    @RequestMapping(value = "/notify")
    @ResponseBody
    public String notify(HttpServletRequest request, HttpServletResponse response) throws IOException {
        System.out.println("微信支付回调");

//        PrintWriter writer = response.getWriter();
        InputStream inStream = request.getInputStream();
        ByteArrayOutputStream outSteam = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = 0;
        while ((len = inStream.read(buffer)) != -1) {
            outSteam.write(buffer, 0, len);
        }
        outSteam.close();
        inStream.close();
        String result = new String(outSteam.toByteArray(), "utf-8");
//        String result = "<xml><appid><![CDATA[wx5dc2c345cc3f4bf4]]></appid>\n" +
//                "<attach><![CDATA[496cc55b19fec167395436dc5f027ddd244167330]]></attach>\n" +
////                "<attach><![CDATA[39d9b835eb1b1b14c52193a43b927f8f329cee26]]></attach>\n" +
//                "<bank_type><![CDATA[LQT]]></bank_type>\n" +
//                "<cash_fee><![CDATA[1800]]></cash_fee>\n" +
//                "<fee_type><![CDATA[CNY]]></fee_type>\n" +
//                "<is_subscribe><![CDATA[N]]></is_subscribe>\n" +
//                "<mch_id><![CDATA[1496393002]]></mch_id>\n" +
//                "<nonce_str><![CDATA[2DtiJFfjBJS4Af0DKKKkgZj6zKRolntz]]></nonce_str>\n" +
//                "<openid><![CDATA[o6AXJ0Y9dEoYZ40H2bkNYw0pcF1M]]></openid>\n" +
//                "<out_trade_no><![CDATA[WX39d9I1548649023]]></out_trade_no>\n" +
//                "<result_code><![CDATA[SUCCESS]]></result_code>\n" +
//                "<return_code><![CDATA[SUCCESS]]></return_code>\n" +
//                "<sign><![CDATA[5F0EA2C513C890411A7463DB503A0E07]]></sign>\n" +
//                "<time_end><![CDATA[20190128121715]]></time_end>\n" +
//                "<total_fee>1800</total_fee>\n" +
//                "<trade_type><![CDATA[APP]]></trade_type>\n" +
//                "<transaction_id><![CDATA[4200000245201901282500825080]]></transaction_id>\n" +
//                "</xml>";
        System.out.println("微信支付通知结果：" + result);
        Map<String, String> map = null;

        try {
            map = XMLUtil.doXMLParse(result);
        } catch (JDOMException e) {
            e.printStackTrace();
            log.error("解析微信返回支付结果出错：" + e.getMessage());
        }
//        log.error("解析Map:" + map.toString());
         String result_code = map.get("result_code");
         String out_trade_no = map.get("out_trade_no");
         String return_code = map.get("return_code");

        if (checkSign(result)) {
//        if (true) {
            // 调用成功
            if ("SUCCESS".equals(return_code) && "SUCCESS".equals(result_code)) {
                String attach = map.get("attach");
                String uuid = attach.substring(0, attach.length() - 1);
                String uType = attach.substring(attach.length() - 1, attach.length());
                try {
                    switch (uType) {
                        case "0":
                            orderQuery(uuid, out_trade_no);
                            break;
                        case "1":
                            dealController.buyVip(uuid, out_trade_no, 2);
                            break;
                        case "2":
                            diamondOrderQuery(uuid, out_trade_no);
                            break;
                        case "3":
                            diamondOrderQuery(uuid, out_trade_no);
                            break;
                        case "E":
                            log.error("下单接口状态出错Map：" + map.toString());
                            break;
                        default:
                            log.error("没有该接口处理状态：" + map.toString());
                    }

                    return returnXML("SUCCESS");
                } catch (Exception e) {
                    e.printStackTrace();
                    log.error("回调请求对应方法时出错，出错uType为"+ uType + " ,Map:" + map.toString());
                    return returnXML("SUCCESS");
                }
            } else {
                return returnXML("SUCCESS");
            }
        } else {
            return returnXML("SUCCESS");
        }
    }

    private boolean checkSign(String xmlString) {

        Map<String, String> map = null;
        try {
            map = XMLUtil.doXMLParse(xmlString);
        } catch (Exception e) {
            e.printStackTrace();
        }

        String signFromAPIResponse = map.get("sign").toString();
            if (signFromAPIResponse == "" || signFromAPIResponse == null) {
                System.out.println("API返回的数据签名数据不存在，有可能被第三方篡改!!!");
                return false;
        }
        System.out.println("服务器回包里面的签名是:" + signFromAPIResponse);
        //清掉返回数据对象里面的Sign数据（不能把这个数据也加进去进行签名），然后用签名算法进行签名
        map.put("sign", "");
        //将API返回的数据根据用签名算法进行计算新的签名，用来跟API返回的签名进行比较
        String signForAPIResponse = getSign(map);
        if (!signForAPIResponse.equals(signFromAPIResponse)) {
            //签名验不过，表示这个API返回的数据有可能已经被篡改了
            System.out.println("API返回的数据签名验证不通过，有可能被第三方篡改!!! signForAPIResponse生成的签名为" + signForAPIResponse);
            return false;
        }
        System.out.println("恭喜，API返回的数据签名验证通过!!!");
        return true;
    }


    private String returnXML(String return_code) {
        return "<xml><return_code><![CDATA["
                + return_code
                + "]]></return_code><return_msg><![CDATA[OK]]></return_msg></xml>";
    }

    public String getSign(Map<String, String> map) {
        try {
            return StringUtil.generateSignature(map, key, WXPayConstants.SignType.MD5);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 恢复上级id没有在注册登录时绑定造成分润错误数据
     * @param uuid      下级用户标识
     * @param money     充值金额
     * @param logId     充值LogId
     * @return
     */
//    @RequestMapping(value = "/testRao")// 20190131 关
//    @ResponseBody
    public Boolean testRao(String uuid, BigDecimal money, int logId) {
        return commonController.testRao(uuid, money, money.multiply(BigDecimal.TEN), logId);

    }
}
