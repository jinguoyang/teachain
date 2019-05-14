package com.youzi.teaChain.ctrl;

import com.youzi.teaChain.bean.DrawMoneyOrder;
import com.youzi.teaChain.bean.TTeachainLogin;
import com.youzi.teaChain.bean.Wallet;
import com.youzi.teaChain.common.SHA;
import com.youzi.teaChain.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@Scope(value = "prototype")    //非scope=”singleton”单例模式注解，每次请求单独的Action，线程安全
@RequestMapping("/drawMoney")
public class DrawMoneyController {
    private Logger log = LoggerFactory.getLogger(DrawMoneyController.class);

    @Resource
    private DrawMoneyService drawMoneyService;
    @Resource
    private WalletService walletService;
    @Resource
    private UserService userService;
    @Resource
    private ConfigService configService;
    @Resource
    private LoginService loginService;

    /**
     * @Description: 创建提现订单
     * @param uuid 用户uuid
     * @param count 提现金额
     * @param payPassword 支付密码
     * @return java.util.Map<java.lang.String,java.lang.Object>
     * @Date 2018-11-13 10:37:16
     */
    @RequestMapping(value = "/createDrawOrder")
    @ResponseBody
    public Map<String, Object> createDrawOrder(String uuid, BigDecimal count, String payPassword) throws Exception {
        Map<String, Object> map = new HashMap<>();
        Wallet wallet = walletService.getWalletByUuid(uuid);
        Integer transLock= wallet.getTransLock();
        if (transLock==2){
            map.put("code", 500);
            map.put("msg", "提现失败，请联系客服进行人工审核！");
            return map;
        }
        TTeachainLogin ttl = loginService.getLoginInfoByUuid(uuid);
        if (ttl.getStatus() == 2) {
            map.put("code", 500);
            map.put("msg", "安全模式，您的账号存在被盗风险！进入官方QQ群869800795解除安全模式。");
            return map;
        }
        String wechatBrokerageLimit = configService.selectConfig("wechatBrokerageLimit");
        if(count.compareTo(new BigDecimal(wechatBrokerageLimit))<0){
            map.put("code",500);
            map.put("msg","提现金额必须大于"+wechatBrokerageLimit+"！");
            return map;
        }
        String wechatNum = userService.getTUserByUuid(uuid).getWechatNumber();
        if(wechatNum==null){
            map.put("code",500);
            map.put("msg","请先绑定微信！");
            return map;
        }
        if(SHA.encryptSHA(payPassword).equals(wallet.getPayPassword())){
            int createDrawOrderSuccess = drawMoneyService.createDrawOrder(uuid,count,wechatNum);
            if (createDrawOrderSuccess==1){
                map.put("code",200);
            }else if(createDrawOrderSuccess==0){
                map.put("code",500);
                map.put("msg","余额不足");
            }else{
                map.put("code",500);
                map.put("msg","申请提现失败");
            }
        } else {
        map.put("code", 500);
        map.put("msg", "支付密码错误！");
        }
        return map;
    }

    /**
     * @Description: 查询用户提现订单
     * @param uuid 用户uuid
     * @return java.util.Map<java.lang.String,java.lang.Object>
     * @Date 2018-11-13 10:38:10
     */
    @RequestMapping(value = "/getDrawOrder")
    @ResponseBody
    public Map<String, Object> getDrawOrder(String uuid){
        Map<String, Object> map = new HashMap<>();
        List<DrawMoneyOrder> drawMoneyOrderList = drawMoneyService.getDrawOrder(uuid);
        if (drawMoneyOrderList!=null){
            map.put("code",200);
            map.put("drawMoneyOrderList",drawMoneyOrderList);
        }else{
            map.put("code",500);
            map.put("msg","获取提现订单失败");
        }
        return map;
    }

    /**
     * @Description: 逻辑删除提现订单
     * @param id 订单id
     * @return java.util.Map<java.lang.String,java.lang.Object>
     * @Date 2018-11-13 11:26:28
     */
    @RequestMapping(value = "/delDrawOrder")
    @ResponseBody
    public Map<String, Object> delDrawOrder(String id){
        Map<String, Object> map = new HashMap<>();
        Boolean delDrawOrderSuccess = drawMoneyService.setDelFlagDrawOrder(id);
        if (delDrawOrderSuccess){
            map.put("code",200);
        }else{
            map.put("code",500);
            map.put("msg","刪除提现订单失败");
        }
        return map;
    }
}
