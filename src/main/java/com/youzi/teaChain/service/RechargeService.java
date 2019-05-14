package com.youzi.teaChain.service;

import com.youzi.teaChain.bean.TTeachainRechargeLog;
import com.youzi.teaChain.bean.TUser;
import com.youzi.teaChain.common.HttpPostGet;
import com.youzi.teaChain.common.StringUtil;
import com.youzi.teaChain.common.wxpay.sdk.WXPayConstants;
import com.youzi.teaChain.dao.RechargeMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RechargeService {
    private Logger log = LoggerFactory.getLogger(RechargeService.class);

    @Resource
    private RechargeMapper rechargeMapper;
    @Resource
    private UserService userService;

    @Value("${wx.appId}")
    private String appId;
    @Value("${wx.pay.number}")
    private String mch_id;
    @Value("${wx.pay.secret}")
    private String key;

    /**
     * 拼装微信支付预订单
     * @param uuid
     * @param amount
     * @param body
     * @return
     * @throws Exception
     */
    public Map spliceRecharge(String uuid, String amount, String body, String uType) throws Exception {
        Map<String, Object> map = new HashMap<>();

        String url = "https://api.mch.weixin.qq.com/pay/unifiedorder";
        Map<String, String> data = new HashMap<>();
        data.put("appid", appId);
        data.put("mch_id", mch_id);
        data.put("nonce_str", StringUtil.generateNonceStr());
        data.put("body", body);
        String tradeNo = "WX" + StringUtil.getOneAUuid(uuid.substring(0, 4));
        data.put("out_trade_no", tradeNo);
        data.put("total_fee", amount);
//        data.put("openid", openId);       // jssdk使用
        data.put("spbill_create_ip", "47.98.147.188");
        data.put("notify_url", "http://47.98.147.188:8050/recharge/notify");
        data.put("trade_type", "APP");  // 此处指定为公众号支付
        data.put("attach", uuid + uType);
        String sign = StringUtil.generateSignature(data, key, WXPayConstants.SignType.MD5);
        System.out.println(sign);
        data.put("sign", sign);

        try {
            String xmlString = StringUtil.mapToXml(data);
            String res = HttpPostGet.getPost(url, xmlString);
//            String res = "<xml><return_code><![CDATA[SUCCESS]]></return_code>\n" +
//                    "<return_msg><![CDATA[OK]]></return_msg>\n" +
//                    "<appid><![CDATA[wx58bcb0806f1a9c32]]></appid>\n" +
//                    "<mch_id><![CDATA[1517536871]]></mch_id>\n" +
//                    "<nonce_str><![CDATA[saifMEexLOZbK2Xx]]></nonce_str>\n" +
//                    "<sign><![CDATA[8397920FA9ABBC079A694A6D739670E7]]></sign>\n" +
//                    "<result_code><![CDATA[SUCCESS]]></result_code>\n" +
//                    "<prepay_id><![CDATA[wx30175024917983750f3b14401850963024]]></prepay_id>\n" +
//                    "<trade_type><![CDATA[APP]]></trade_type>\n" +
//                    "</xml>";
            String return_code = res.substring(res.indexOf("return_code"), res.indexOf("/return_code"));
            String code = return_code.substring(return_code.indexOf("CDATA[") + 6, return_code.indexOf("]]"));
            if ("SUCCESS".equals(code)) {
                String res_code = res.substring(res.indexOf("result_code"), res.indexOf("/result_code"));
                String code_res = res_code.substring(res_code.indexOf("CDATA[") + 6, res_code.indexOf("]]"));
                if ("SUCCESS".equals(code_res)) {
                    String prepay_id = res.substring(res.indexOf("prepay_id") + 19, res.indexOf("/prepay_id") - 4);
                    Map webMap = webResMap(appId, key, prepay_id);
                    map.put("code", 200);
                    map.put("webMap", webMap);
                    map.put("prepay_id", prepay_id);
                    map.put("tradeNo", tradeNo);
                } else {
                    String err_code_des = res.substring(res.indexOf("err_code_des") + 22, res.indexOf("/err_code_des") - 4);
                    map.put("code", 500);
                    map.put("msg", err_code_des);
                }
            } else {
                String msg = res.substring(res.indexOf("return_msg") + 20, res.indexOf("/return_msg") - 4);
                map.put("code", 500);
                map.put("msg", msg);
            }

        } catch (Exception e) {
            e.printStackTrace();
            log.error("调用微信支付统一支付接口出错" + e.getMessage());
            map.put("code", 500);
            map.put("msg", "调用微信支付统一支付接口出错");
            return map;
        }
        return map;
    }

    private Map webResMap(String appID, String key, String prepay_id) throws Exception {
        Map<String, String> webMap = new HashMap<>();
        webMap.put("appid", appID);
        webMap.put("partnerid", mch_id);
        webMap.put("prepayid", prepay_id);
        webMap.put("package", "Sign=WXPay");
        webMap.put("noncestr", StringUtil.generateNonceStr());
        webMap.put("timestamp", String.valueOf(new Date().getTime() / 1000));
        String paySign = StringUtil.generateSignature(webMap, key, WXPayConstants.SignType.MD5);
        System.out.println(paySign);
        webMap.put("sign", paySign);
        return webMap;
    }

    /**
     * 拼装微信支付查询接口
     * @param uuid
     * @param tradeNo
     * @return
     * @throws Exception
     */
    public Map spliceOrderQuery(String uuid, String tradeNo) throws Exception {
        Map<String, Object> map = new HashMap<>();
        TUser user = userService.getTUserByUuid(uuid);
        if (user == null) {
            map.put("code", 500);
            map.put("msg", "用户不存在");
            return map;
        }

        int checkCount = rechargeMapper.checkTradeNo(tradeNo);
        if (checkCount > 0) {
            map.put("code", 500);
            map.put("msg", "请不要循环调用充值接口");
            return map;
        }

        // 阻塞1秒，方便查询微信下单接口
//        Thread.sleep(1000L);

        String url = "https://api.mch.weixin.qq.com/pay/orderquery";
        Map<String, String> data = new HashMap<>();
        data.put("appid", appId);
        data.put("mch_id", mch_id);
        data.put("out_trade_no", tradeNo);
        data.put("nonce_str", StringUtil.generateNonceStr());
        String sign = StringUtil.generateSignature(data, key, WXPayConstants.SignType.MD5);
        System.out.println(sign);
        data.put("sign", sign);

        String res;
        try {
            String xmlString = StringUtil.mapToXml(data);
            res = HttpPostGet.getPost(url, xmlString);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("调用微信支付查询接口出错" + e.getMessage());
            map.put("code", 500);
            map.put("msg", "调用微信支付查询接口出错");
            return map;
        }

        String return_code = res.substring(res.indexOf("return_code"), res.indexOf("/return_code"));
        String code = return_code.substring(return_code.indexOf("CDATA[") + 6, return_code.indexOf("]]"));
        // 调用成功
        if ("SUCCESS".equals(code)) {
            String res_code = res.substring(res.indexOf("result_code"), res.indexOf("/result_code"));
            String code_res = res_code.substring(res_code.indexOf("CDATA[") + 6, res_code.indexOf("]]"));
            // 返回成功
            if ("SUCCESS".equals(code_res)) {
                String trade_state = res.substring(res.indexOf("trade_state") + 21, res.indexOf("/trade_state") - 4);
                // 交易成功
                if ("SUCCESS".equals(trade_state)) {
                    BigDecimal amount = BigDecimal.ZERO;
                    Map<String, Object> param = new HashMap<>();
                    try {
                        String total_fee = res.substring(res.indexOf("total_fee") + 10, res.indexOf("/total_fee") - 1);
                        String bank_type = res.substring(res.indexOf("bank_type") + 19, res.indexOf("/bank_type") - 4);
                        String transaction_id = res.substring(res.indexOf("transaction_id") + 24, res.indexOf("/transaction_id") - 4);
                        String out_trade_no = res.substring(res.indexOf("out_trade_no") + 22, res.indexOf("/out_trade_no") - 4);
                        String time_end = res.substring(res.indexOf("time_end") + 18, res.indexOf("/time_end") - 4);
                        param.put("uuid", user.getUuid());
                        param.put("transNo", transaction_id);
                        param.put("tradeNo", out_trade_no);
                        param.put("amount", Integer.parseInt(total_fee));
                        param.put("status", 1);
                        param.put("bankType", bank_type);
                        param.put("timeEnd", time_end);
                        amount = new BigDecimal(Integer.parseInt(total_fee)).divide(new BigDecimal(100));
                        createRechargeLog(param);
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                        log.error("创建充值日志出错，无伤大雅:" + e.getMessage());
                    } finally {
                        map.put("code", 200);
                        map.put("param", param);
                        map.put("realAmount", amount);
                    }
                } else {
                    map.put("code", 500);
                    map.put("msg", "支付失败，请重新下单支付");
                }
            } else {
                String err_code_des = res.substring(res.indexOf("err_code_des") + 22, res.indexOf("/err_code_des") - 4);
                map.put("code", 500);
                map.put("msg", err_code_des);
            }
        } else {
            String msg = res.substring(res.indexOf("return_msg") + 20, res.indexOf("/return_msg") - 4);
            map.put("code", 500);
            map.put("msg", msg);
        }
        return map;
    }

    public Boolean createRechargeLog(Map param) {
        try {
            rechargeMapper.createRechargeLog(param);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("createRechargeLog 保存充值日志出错：" + e.getMessage());
            return false;
        }
    }

    public List<TTeachainRechargeLog> getRechargeOrder(String uuid) {
        try {
            return rechargeMapper.getRechargeOrder(uuid);
        }catch (Exception e){
            e.printStackTrace();
            log.error("getRechargeOrder 根据用户查询充值日志出错：" + e.getMessage());
            return null;
        }
    }

    public int checkTradeNo(String tradeNo) {
        try {
            return rechargeMapper.checkTradeNo(tradeNo);
        }catch (Exception e){
            e.printStackTrace();
            log.error("checkTradeNo 根据微信充值订单号查询记录数出错：" + e.getMessage());
            return 0;
        }
    }

}
