package com.youzi.teaChain.dao;

import com.youzi.teaChain.bean.TTeachainOrdersTrade;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CurrencyTradeMapper {
    void addTradeOrder(TTeachainOrdersTrade tTeachainOrdersTrade);

    TTeachainOrdersTrade getTradeOrderById(String id);

    Integer getTradeOrderCountByStatus(@Param(value = "uuid") String uuid,@Param(value = "type") int type);

    Integer getTradeOrderCountByUuid(@Param(value = "uuid") String uuid,@Param(value = "status") int status);

    List<TTeachainOrdersTrade> getTradeOrderByStatus(@Param(value = "type") int type, @Param(value = "index") int index, @Param(value = "pageSize") int pageSize, @Param(value = "uuid") String uuid);

    List<TTeachainOrdersTrade> getTradeOrderByUuid(@Param(value = "uuid") String uuid,@Param(value = "status") int status, @Param(value = "index") int index, @Param(value = "pageSize") int pageSize);

    void endTradeOrder(@Param(value = "uuid") String uuid, @Param(value = "id") String id);

    void stopTradeOrder(String orderId);

    Integer getTradeOrderLock(@Param(value = "id") String id);

    void setTradeOrderLock(@Param(value = "id") String id,@Param(value = "lock") Integer lock);
}
