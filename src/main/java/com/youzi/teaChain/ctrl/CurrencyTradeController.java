package com.youzi.teaChain.ctrl;

import com.youzi.teaChain.bean.TTeachainOrdersTrade;
import com.youzi.teaChain.bean.TUser;
import com.youzi.teaChain.bean.Wallet;
import com.youzi.teaChain.common.ErrorCode;
import com.youzi.teaChain.common.SHA;
import com.youzi.teaChain.service.ConfigService;
import com.youzi.teaChain.service.CurrencyTradeService;
import com.youzi.teaChain.service.UserService;
import com.youzi.teaChain.service.WalletService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Controller
@Scope(value="prototype")	//非scope=”singleton”单例模式注解，每次请求单独的Action，线程安全
@RequestMapping("/trade")
public class CurrencyTradeController {
    private Logger log = LoggerFactory.getLogger(CurrencyTradeController.class);

    @Resource
    private CurrencyTradeService currencyTradeService;
    @Resource
    private UserService userService;
    @Resource
    private WalletService walletService;
    @Resource
    private ConfigService configService;

    /**
     * @Description: 创建交易订单
     * @param uuid 用户uuid
     * @param price 交易单价
     * @param count 交易数量
     * @param type 交易类型 1.买入 2.卖出
     * @param payPassword 支付密码
     * @return java.util.Map<java.lang.String,java.lang.Object>
     * @Date 2018-11-19 14:13:30
     */ 
//    @RequestMapping(value = "/createTradeOrder")// 20190131 关
//    @ResponseBody
    public Map<String, Object> createTradeOrder(String uuid, BigDecimal price, BigDecimal count, Integer type, String payPassword) throws Exception {
        Map<String, Object> map = new HashMap<>();
        if (true){
            map.put("code",500);
            map.put("msg","交易市场已关闭!");
            return map;
        }
        BigDecimal amount = price.multiply(count);
        Wallet wallet = walletService.getWalletByUuid(uuid);
        BigDecimal balance = wallet.getBalance();
        String pay_password = wallet.getPayPassword();
        TUser tUser = userService.getTUserByUuid(uuid);
        String idCard = tUser.getIdCardNum();
        BigDecimal total = tUser.getTotalMoney();
        Integer tradeLicense = tUser.getTradeLicense();
        String brokerageSell1 = configService.selectConfig("brokerageSell1");
        String brokerageSell2 = configService.selectConfig("brokerageSell2");
        if(idCard==null){
            map.put("code",500);
            map.put("msg","请先绑定身份证!");
            return map;
        }
//        else if(calcPower.compareTo(new BigDecimal("200"))<0){
//            map.put("code",500);
//            map.put("msg","需累计获得100茶币,或算力大于200方可出让茶币!");
//            return map;
//        }
        BigDecimal licenseCost;
        if (tradeLicense==1){
            licenseCost = new BigDecimal("0");
        }else {
            licenseCost = new BigDecimal("20");
        }
        if (pay_password.equals(SHA.encryptSHA(payPassword))){
            if (type == 1){
                BigDecimal brokerage1 = amount.multiply(new BigDecimal(brokerageSell1));
                BigDecimal brokerage2 = amount.multiply(new BigDecimal(brokerageSell2));
                TTeachainOrdersTrade tTeachainOrdersTrade = new TTeachainOrdersTrade(uuid,price,count,amount,brokerage1,brokerage2,type);
                if (total.compareTo(amount.add(brokerage1).add(brokerage2))>=0){
                    Boolean addTradeOrderSuccess = currencyTradeService.addTradeOrder(tTeachainOrdersTrade,brokerage1,licenseCost);
                    total = userService.getTUserByUuid(uuid).getTotalMoney();
                    balance = walletService.getWalletByUuid(uuid).getBalance();
                    if (addTradeOrderSuccess){
                        map.put("code",200);
                    }else{
                        map.put("code",500);
                        map.put("msg","添加交易订单失败！");
                    }
                }else {
                    map.put("code",ErrorCode.LACKMONEY);
                    map.put("msg","RMB不足！");
                }
            }else if(type == 2){
                BigDecimal brokerage1 = count.multiply(new BigDecimal(brokerageSell1));
                BigDecimal brokerage2 = count.multiply(new BigDecimal(brokerageSell2));
                TTeachainOrdersTrade tTeachainOrdersTrade = new TTeachainOrdersTrade(uuid,price,count,amount,brokerage1,brokerage2,type);
                if (balance.compareTo(count.add(brokerage1).add(brokerage2).add(licenseCost))>=0){
                    Boolean addTradeOrderSuccess = currencyTradeService.addTradeOrder(tTeachainOrdersTrade,brokerage1,licenseCost);
                    total = userService.getTUserByUuid(uuid).getTotalMoney();
                    balance = walletService.getWalletByUuid(uuid).getBalance();
                    if (addTradeOrderSuccess){
                        if(tradeLicense!=1){
                            userService.setTradeLicense(uuid);
                        }
                        map.put("code",200);
                    }else{
                        map.put("code",500);
                        map.put("msg","添加交易订单失败");
                    }
                }else {
                    map.put("code",ErrorCode.LACKTCC);
                    map.put("msg","TCC不足！");
                }
            }else {
                map.put("code",500);
                map.put("msg","交易类型错误！");
            }
        }else {
            map.put("code",500);
            map.put("msg","支付密码错误！");
        }
        map.put("totalMoney",total);
        map.put("totalTCC",balance);
        return map;
    }
    
    /**
     * @Description: 完成交易订单
     * @param uuid 用户uuid
     * @param orderId 订单单号
     * @param payPassword 支付密码
     * @return java.util.Map<java.lang.String,java.lang.Object>
     * @Date 2018-11-19 14:15:13
     */ 
//    @RequestMapping(value = "/endTradeOrder")// 20190131 关
//    @ResponseBody
    public Map<String, Object> endTradeOrder(String uuid, String orderId, String payPassword) throws Exception {
        Map<String, Object> map = new HashMap<>();
        if (true){
            map.put("code",500);
            map.put("msg","交易市场已关闭!");
            return map;
        }
        Wallet wallet = walletService.getWalletByUuid(uuid);
        String pay_password = wallet.getPayPassword();
        BigDecimal balance = wallet.getBalance();
        TTeachainOrdersTrade tTeachainOrdersTrade = currencyTradeService.getTradeOrderById(orderId);
        BigDecimal amount = tTeachainOrdersTrade.getAmount();
        BigDecimal count = tTeachainOrdersTrade.getCount();
        String home_uuid = tTeachainOrdersTrade.getHomeUuid();
        Long type = tTeachainOrdersTrade.getType();
        String brokerageBuy = configService.selectConfig("brokerageBuy");
        TUser tUser = userService.getTUserByUuid(uuid);
        BigDecimal totalMoney = tUser.getTotalMoney();
        if (pay_password.equals(SHA.encryptSHA(payPassword))){
            if(tTeachainOrdersTrade.getStatus()==0){
                if(type == 1){
                    BigDecimal brokerage = count.multiply(new BigDecimal(brokerageBuy));
                    if (balance.compareTo(count.add(brokerage))>=0){
                        Integer addTradeOrderSuccess = currencyTradeService.endTradeOrder(uuid,home_uuid,orderId,amount,count,brokerage,type);
                        totalMoney = userService.getTUserByUuid(uuid).getTotalMoney();
                        balance = walletService.getWalletByUuid(uuid).getBalance();
                        if (addTradeOrderSuccess==1){
                            map.put("code",200);
                        }else if (addTradeOrderSuccess==-1){
                            map.put("code",500);
                            map.put("msg","完成交易失败！");
                        }else {
                            map.put("code",500);
                            map.put("msg","订单已失效!");
                        }
                    }else {
                        map.put("code",ErrorCode.LACKTCC);
                        map.put("msg","TCC不足！");
                    }
                }else if(type == 2){
                    BigDecimal a = new BigDecimal(0.01);
                    BigDecimal brokerage = amount.multiply(new BigDecimal(brokerageBuy));
                    brokerage = brokerage.compareTo(a)<0?a:brokerage;
                    if (totalMoney.compareTo(amount.add(brokerage))>=0){
                        Integer addTradeOrderSuccess = currencyTradeService.endTradeOrder(uuid,home_uuid,orderId,amount,count,brokerage,type);
                        totalMoney = userService.getTUserByUuid(uuid).getTotalMoney();
                        balance = walletService.getWalletByUuid(uuid).getBalance();
                        if (addTradeOrderSuccess==1){
                            map.put("code",200);
                        }else if (addTradeOrderSuccess==-1){
                            map.put("code",500);
                            map.put("msg","完成交易失败！");
                        }else {
                            map.put("code",500);
                            map.put("msg","订单已失效!");
                        }
                    }else {
                        map.put("code",ErrorCode.LACKMONEY);
                        map.put("msg","RMB不足！");
                    }
                }else {
                    map.put("code", 500);
                    map.put("msg", "交易失败，订单类型不确定！");
                }
            }else {
                map.put("code",500);
                map.put("msg","交易失败，该笔交易已过期！");
            }
        }else {
            map.put("code",500);
            map.put("msg","支付密码错误！");
        }
        map.put("totalMoney",totalMoney);
        map.put("totalTCC",balance);
        return map;
    }

    /**
     * @Description: 查询市场上正在进行的交易订单
     * @param type 交易类型 1.买入 2.卖出
     * @param index 页号索引 0开始
     * @param pageSize 页面大小
     * @return java.util.Map<java.lang.String,java.lang.Object>
     * @Date 2018-11-19 14:16:13
     */ 
//    @RequestMapping(value = "/getAllTradeOrder")// 20190131 关
//    @ResponseBody
    public Map<String, Object> getAllTradeOrder(String uuid, int type,int index,int pageSize){
        Map<String, Object> map = new HashMap<>();
        List<TTeachainOrdersTrade> tTeachainOrdersTradeList = currencyTradeService.getTradeOrderByStatus(index,pageSize,type,uuid);
        Integer orderCount = currencyTradeService.getTradeOrderCountByStatus(uuid,type);
        Integer pageCount = (orderCount%pageSize)==0?orderCount/pageSize:(orderCount/pageSize)+1;
        if(tTeachainOrdersTradeList!=null){
            map.put("code",200);
            map.put("tTeachainOrdersTradeList",tTeachainOrdersTradeList);
            map.put("pageCount",pageCount);
        }else {
            map.put("code",500);
            map.put("msg","查询交易市场失败");
        }
        return map;
    }

    /**
     * @Description: 查询关于自己的订单
     * @param uuid 用户uuid
     * @param index 页号索引 0开始
     * @param pageSize 页面大小
     * @return java.util.Map<java.lang.String,java.lang.Object>
     * @Date 2018-11-19 14:17:04
     */
//    @RequestMapping(value = "/getSelfTradeOrder")// 20190131 关
//    @ResponseBody
    public Map<String, Object> getSelfTradeOrder(String uuid, int status, int index,int pageSize){
        Map<String, Object> map = new HashMap<>();
        List<TTeachainOrdersTrade> tTeachainOrdersTradeList = currencyTradeService.getTradeOrderByUuid(uuid,status, index,pageSize);
        Integer orderCount = currencyTradeService.getTradeOrderCountByUuid(uuid,status);
        Integer pageCount = (orderCount%pageSize)==0?orderCount/pageSize:(orderCount/pageSize)+1;
        if(tTeachainOrdersTradeList!=null){
            map.put("code",200);
            map.put("tTeachainOrdersTradeList",tTeachainOrdersTradeList);
            map.put("pageCount",pageCount);
        }else {
            map.put("code",500);
            map.put("msg","查询交易市场失败");
        }
        return map;
    }

    /**
     * @Description: 撤销交易
     * @param uuid 用户uuid
     * @param orderId 订单号
     * @return java.util.Map<java.lang.String,java.lang.Object>
     * @Date 2018-11-28 10:04:32
     */
//    @RequestMapping(value = "/stopTradeOrder") // 20190131 关
//    @ResponseBody
    public Map<String, Object> stopTradeOrder(String uuid, String orderId){
        Map<String, Object> map = new HashMap<>();
        if (true){
            map.put("code",500);
            map.put("msg","交易市场已关闭!");
            return map;
        }
//        Wallet wallet = walletService.getWalletByUuid(uuid);
        TTeachainOrdersTrade tTeachainOrdersTrade = currencyTradeService.getTradeOrderById(orderId);
        long type = tTeachainOrdersTrade.getType();
        String home_uuid = tTeachainOrdersTrade.getHomeUuid();
        long status = tTeachainOrdersTrade.getStatus();
        if (status == 0) {
//            if (SHA.encryptSHA(payPassword).equals(wallet.getPay_password())) {
                if (home_uuid.equals(uuid)) {
                    Boolean stopTradeOrderSuccess = currencyTradeService.stopTradeOrder(orderId, type, home_uuid);
                    if (stopTradeOrderSuccess) {
                        map.put("code", 200);
                    } else {
                        map.put("code", 500);
                        map.put("msg", "取消失败！");
                    }
                } else {
                    map.put("code", 500);
                    map.put("msg", "非交易所有者，无权取消该交易！");
                }
//            } else {
//                map.put("code", 500);
//                map.put("msg", "支付密码错误！");
//            }
        } else {
            map.put("code", 500);
            map.put("msg", "交易已完成，不可取消!");
        }
        return map;
    }
}
