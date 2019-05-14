package com.youzi.teaChain.dao;

import com.youzi.teaChain.bean.TTeachainGoods;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface GoodsOrderMapper {
    List<Map<String, Object>> getGoodsOrderByUuid(@Param(value = "uuid")String uuid);

    void addGoodsOrder(Map map);

    void reduceStock(@Param(value = "id") int id, @Param(value = "amount") int amount);

    int updateStock(@Param(value = "id") int id, @Param(value = "amount") int amount, @Param(value = "type") int type);

    List<TTeachainGoods> getGoodsInfo(int type);

    TTeachainGoods getGoodsById(@Param(value = "id")String id);
}
