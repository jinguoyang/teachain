package com.youzi.teaChain.service;


import com.youzi.teaChain.bean.TTeachainGoods;
import com.youzi.teaChain.dao.GoodsOrderMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Service
public class GoodsOrderService {
    private Logger log = LoggerFactory.getLogger(GoodsOrderService.class);

    @Resource
    private GoodsOrderMapper goodsOrderMapper;

    public List<Map<String, Object>> getGoodsOrderByUuid(String uuid) {
        try {
            return goodsOrderMapper.getGoodsOrderByUuid(uuid);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("getGoodsOrderByUuid 通过uuid查询订单出错：" + e.getMessage());
            return null;
        }
    }

    public List<TTeachainGoods> getGoodsInfo(int type) {
        try {
            return goodsOrderMapper.getGoodsInfo(type);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("getGoodsInfo 查询商品信息出错：" + e.getMessage());
            return null;
        }
    }

    public Boolean addGoodsOrder(Map map) {
        try {
            goodsOrderMapper.addGoodsOrder(map);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("addGoodsOrder 添加商品订单出错：" + e.getMessage());
            return false;
        }
    }

    public TTeachainGoods getGoodsById(String id) {
        try {
            return goodsOrderMapper.getGoodsById(id);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("getGoodsById 通过id查询商品出错：" + e.getMessage());
            return null;
        }
    }

    public Boolean reduceStock(int id, int amount) {
        try {
            goodsOrderMapper.reduceStock(id, amount);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("reduceStock 减少商品库存出错：" + e.getMessage());
            return false;
        }
    }

    public Boolean updateStock(int id, int amount,int type) {
        try {
            int aa = goodsOrderMapper.updateStock(id, amount, type);
            return aa == 200;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("updateStock 更改商品库存出错：" + e.getMessage());
            return false;
        }
    }
}
