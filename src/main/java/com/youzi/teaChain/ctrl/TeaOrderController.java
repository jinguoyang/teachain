package com.youzi.teaChain.ctrl;

import com.alibaba.fastjson.JSON;
import com.youzi.teaChain.bean.*;
import com.youzi.teaChain.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@Scope(value = "singleton")    //非scope=”singleton”单例模式注解，每次请求单独的Action，线程安全
@RequestMapping("/teaOrder")
public class TeaOrderController {
    private Logger log = LoggerFactory.getLogger(TeaOrderController.class);

    @Resource
    private TeaOrderService teaOrderService;
    @Resource
    private GoodsOrderService goodsOrderService;
    @Resource
    private WalletService walletService;
    @Resource
    private UserService userService;
    @Resource
    private ConfigService configService;
    @Resource
    private LoginService loginService;

    /**
     * @Description: 查询当前用户所有收货地址
     * @param uuid 用户uuid
     * @return java.util.Map<java.lang.String,java.lang.Object>
     * @Date 2018-11-13 10:42:33
     */
    @RequestMapping(value = "/getDeliveryAddress")
    @ResponseBody
    public Map<String, Object> getDeliveryAddress(String uuid){
        Map<String, Object> res = new HashMap<>();
        List<DeliveryAddress> deliveryAddressList = teaOrderService.getDeliveryAddressByUuid(uuid);
        if(deliveryAddressList!=null){
            res.put("code",200);
            res.put("deliveryAddressList",deliveryAddressList);
        }else {
            res.put("code",500);
            res.put("msg","查询收货地址出错!");
        }
        return res;
    }

    /**
     * @Description: 获取默认收货地址
     * @param uuid 用户uuid
     * @return java.util.Map<java.lang.String,java.lang.Object>
     * @Date 2018-11-13 10:42:59
     */
    @RequestMapping(value = "/getDefaultDeliveryAddress")
    @ResponseBody
    public Map<String, Object> getDefaultDeliveryAddress(String uuid){
        Map<String, Object> res = new HashMap<>();
        DeliveryAddress deliveryAddress = teaOrderService.getDefaultDeliveryAddress(uuid);
        if(deliveryAddress!=null){
            res.put("code",200);
            res.put("deliveryAddress",deliveryAddress);
        }else {
            res.put("code",500);
            res.put("msg","查询默认收货地址出错!");
        }
        return res;
    }

    /**
     * @Description: 添加收货地址
     * @param map 收货地址信息 内含name gender phone address houseNumber tag isdefault(1为默认 0为非默认)
     * @param uuid 用户uuid
     * @return java.util.Map<java.lang.String,java.lang.Object>
     * @Date 2018-11-13 10:43:25
     */
    @RequestMapping(value = "/addDeliveryAddress")
    @ResponseBody
    public Map<String, Object> addDeliveryAddress(String map, String uuid){
        Map jsonMap = JSON.parseObject(map, Map.class);
        Map<String, Object> res = new HashMap<>();
        jsonMap.put("uuid",uuid);
        Boolean addDeliveryAddressSuccess = teaOrderService.addDeliveryAddress(jsonMap);
        if(addDeliveryAddressSuccess!=null){
            res.put("code",200);
        }else {
            res.put("code",500);
            res.put("msg","添加收货地址出错!");
        }
        return res;
    }

    /**
     * @Description: 更改收货地址
     * @param map 收货地址信息 内含id name gender phone address houseNumber tag isdefault(1为默认 0为非默认)
     * @param uuid 用户uuid
     * @return java.util.Map<java.lang.String,java.lang.Object>
     * @Date 2018-11-13 10:46:27
     */ 
    @RequestMapping(value = "/updateDeliveryAddress")
    @ResponseBody
    public Map<String, Object> updateDeliveryAddress(String map,String uuid){
        Map jsonMap = JSON.parseObject(map, Map.class);
        Map<String, Object> res = new HashMap<>();
        String uuid_back = teaOrderService.getUuidByDeliveryAddressId(jsonMap.get("id").toString());
        if(uuid_back.equals(uuid)){
            Boolean updateDeliveryAddressSuccess = teaOrderService.updateDeliveryAddress(jsonMap);
            if(updateDeliveryAddressSuccess!=null){
                res.put("code",200);
            }else {
                res.put("code",500);
                res.put("msg","修改收货地址出错!");
            }
        }else {
            res.put("code",500);
            res.put("msg","无权修改该收货地址!");
        }

        return res;
    }

    /**
     * @Description: 删除收货地址
     * @param id 收货地址id
     * @return java.util.Map<java.lang.String,java.lang.Object>
     * @Date 2018-11-13 10:47:49
     */ 
    @RequestMapping(value = "/deleteDeliveryAddress")
    @ResponseBody
    public Map<String, Object> deleteDeliveryAddress(String id){//todo 无限制
        Map<String, Object> res = new HashMap<>();
        Integer deleteDeliveryAddressSuccess = teaOrderService.deleteDeliveryAddress(id);
        if(deleteDeliveryAddressSuccess==1){
            res.put("code",200);
        }else if (deleteDeliveryAddressSuccess==0){
            res.put("code",500);
            res.put("msg","该收货地址不存在!");
        }else {
            res.put("code",500);
            res.put("msg","删除收货地址出错!");
        }
        return res;
    }

    /**
     * @Description: 获取当前用户商品订单
     * @param uuid 用户uuid
     * @return java.util.Map<java.lang.String,java.lang.Object>
     * @Date 2018-11-14 02:23:49
     */
    @RequestMapping(value = "/getGoodsOrder")
    @ResponseBody
    public Map<String, Object> getGoodsOrder(String uuid){
        Map<String, Object> res = new HashMap<>();
        try {
            List<Map<String, Object>> tTeachainGoodsOrderList = goodsOrderService.getGoodsOrderByUuid(uuid);
            List<Map<String, Object>> mapList = new ArrayList<>();
            if (!tTeachainGoodsOrderList.isEmpty()) {
                for (Map<String, Object> tgoMap : tTeachainGoodsOrderList) {
                    DeliveryAddress deliveryAddress = teaOrderService.getDeliveryAddressById(tgoMap.get("address_id").toString());
                    Map<String, Object> a = new HashMap<>();
                    a.put("tTeachainGoodsOrder", tgoMap);
                    a.put("deliveryAddress", deliveryAddress);
                    mapList.add(a);
                }
            }
            res.put("code", 200);
            res.put("tTeachainGoodsOrderList", mapList);
            return res;
        }catch (Exception e){
            res.put("code", 500);
            res.put("msg", "查询货物订单失败!");
            return res;
        }
    }

    /**
     * 领取兑换奖励
     * 2019-1-24 15:51:41
     * @param uuid 用户uuid
     * @param goodsId 商品id
     * @param addressId 邮寄地址id
     * @param amount 商品数量
     * @return java.util.Map<java.lang.String,java.lang.Object>
     */
    @RequestMapping(value = "/getAwardGoods")
    @ResponseBody
    public Map<String, Object> getAwardGoods(String uuid, String goodsId, int addressId, int amount){
        Map<String, Object> res = new HashMap<>();
        if (amount<1){
            res.put("code", 500);
            res.put("msg", "商品数量有误!");
            return res;
        }
        synchronized (this) {
            Wallet wallet = walletService.getWalletByUuid(uuid);
            Integer transLock= wallet.getTransLock();
            if (transLock==2){
                res.put("code", 500);
                res.put("msg", "交易失败，请联系客服进行人工审核！");
                return res;
            }
            TTeachainLogin ttl = loginService.getLoginInfoByUuid(uuid);
            if (ttl.getStatus() == 2) {
                res.put("code", 500);
                res.put("msg", "安全模式，您的账号存在被盗风险！\n" +
                        "进入官方QQ群869800795解除安全模式。");
                return res;
            }
            TTeachainGoods tTeachainGoods = goodsOrderService.getGoodsById(goodsId);
            if (tTeachainGoods == null) {
                res.put("code", 500);
                res.put("msg", "找不到该商品或该商品已下架!");
                return res;
            }
            if (tTeachainGoods.getType() == 0) {
                res.put("code", 500);
                res.put("msg", "该商品已下架!");
                return res;
            }
            if (tTeachainGoods.getStock() < amount) {
                res.put("code", 500);
                res.put("msg", "该商品库存不足!");
                return res;
            }
            String addressUuid = teaOrderService.getUuidByDeliveryAddressId(Integer.toString(addressId));
            if (addressUuid.equals("") || !addressUuid.equals(uuid)) {
                res.put("code", 500);
                res.put("msg", "收货地址不属于本账户!");
                return res;
            }

            BigDecimal tccPrice = BigDecimal.ZERO, cnyPrice = BigDecimal.ZERO;
            int payType = tTeachainGoods.getPayType();
            if (payType == 1) {
                tccPrice = tTeachainGoods.getTccPrice();
            } else if (payType == 2) {
                cnyPrice = tTeachainGoods.getRmbPrice();
            } else if (payType == 3) {
                tccPrice = tTeachainGoods.getTccPrice();
                cnyPrice = tTeachainGoods.getRmbPrice();
            } else {
                res.put("code", 500);
                res.put("msg", "该商品支付方式有误，请联系客服!");
                return res;
            }

            BigDecimal tccDeliveryPrice = BigDecimal.ZERO, cnyDeliveryPrice = BigDecimal.ZERO;
            int deliveryType = tTeachainGoods.getDeliveryType();
            TUser tUser = userService.getTUserByUuid(uuid);
            if (deliveryType == 1) {//tcc当邮费
                tccDeliveryPrice = tTeachainGoods.getDeliveryMoney();
            } else if (deliveryType == 2) {
                cnyDeliveryPrice = tTeachainGoods.getDeliveryMoney();
            } else if (deliveryType != 3) {
                res.put("code", 500);
                res.put("msg", "该商品邮费类型有误，请联系客服!");
                return res;
            }

            BigDecimal tccTotal = tccPrice.multiply(new BigDecimal(amount)).add(tccDeliveryPrice);
            BigDecimal cnyTotal = cnyPrice.multiply(new BigDecimal(amount)).add(cnyDeliveryPrice);
            if (wallet.getBalance().compareTo(tccTotal) < 0) {
                res.put("code", 500);
                res.put("msg", "云币不足！");
                return res;
            }
            if (tUser.getTotalMoney().compareTo(cnyTotal) < 0) {
                res.put("code", 500);
                res.put("msg", "CNY不足，请充值！");
                return res;
            }

            Boolean aa = walletService.updateWalletBalanceFunc(uuid, tccTotal, 1);
            if (!aa) {
                res.put("code", 500);
                res.put("msg", "云币不足！");
                return res;
            }
            Boolean bb = userService.updateCNYFunc(uuid, cnyTotal, 1);       // 0.增加 1.减少
            if (!bb) {
                walletService.updateWalletBalanceFunc(uuid, tccTotal, 0);
                res.put("code", 500);
                res.put("msg", "CNY不足，请充值！");
                return res;
            }

            Boolean cc = goodsOrderService.updateStock(tTeachainGoods.getId(), amount,1);
            if (!cc) {
                walletService.updateWalletBalanceFunc(uuid, tccTotal, 0);
                userService.updateCNYFunc(uuid, cnyTotal, 0);
                res.put("code", 500);
                res.put("msg", "减少商品库存时出错！");
                return res;
            }

            Map<String, Object> map = new HashMap<>();
            map.put("uuid", uuid);
            map.put("goods_id", goodsId);
            map.put("amount", amount);
            map.put("pay_type", payType);
            map.put("goods_tcc", tccPrice);
            map.put("goods_money", cnyPrice);
            map.put("delivery_type", deliveryType);
            if (deliveryType == 1) {
                map.put("delivery_money", tccDeliveryPrice);
            } else {
                map.put("delivery_money", cnyDeliveryPrice);
            }
            map.put("address_id", addressId);
            map.put("total_tcc", tccTotal);
            map.put("total_cny", cnyTotal);
            Boolean addGoodsOrderSuccess = goodsOrderService.addGoodsOrder(map);
            if (addGoodsOrderSuccess) {
                res.put("code", 200);
            } else {
                goodsOrderService.updateStock(tTeachainGoods.getId(), amount,0);
                walletService.updateWalletBalanceFunc(uuid, tccTotal, 0);
                userService.updateCNYFunc(uuid, cnyTotal, 0);
                res.put("code", 500);
                res.put("msg", "添加货物订单失败!");
            }
            return res;
        }
    }

    /**
     * @Description: 获取物品信息
     * @param type 商品类型 1.商城 2.兑换奖励
     * @return java.util.Map<java.lang.String,java.lang.Object>
     * @Date 2018-11-19 02:11:33
     */
    @RequestMapping(value = "/getGoodsInfo")
    @ResponseBody
    public Map<String, Object> getGoodsInfo(int type){
        Map<String, Object> res = new HashMap<>();
        List<TTeachainGoods> tTeachainGoodsList = goodsOrderService.getGoodsInfo(type);
        String rmbDeliveryPrice = configService.selectConfig("rmbDeliveryPrice");
        String tccDeliveryPrice = configService.selectConfig("tccDeliveryPrice");
        if (tTeachainGoodsList!=null){
            res.put("code",200);
            res.put("tTeachainGoodsList",tTeachainGoodsList);
            res.put("rmbDeliveryPrice",rmbDeliveryPrice);
            res.put("tccDeliveryPrice",tccDeliveryPrice);
        }else {
            res.put("code",500);
            res.put("msg","查询商品失败!");
        }
        return res;
    }
}
