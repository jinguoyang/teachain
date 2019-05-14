package com.youzi.teaChain.dao;

import com.youzi.teaChain.bean.DeliveryAddress;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface TeaOrderMapper {

    List<DeliveryAddress> getDeliveryAddressByUuid(String uuid);

    List<DeliveryAddress> getDefaultDeliveryAddress(String uuid);

    DeliveryAddress getDeliveryAddressById(String id);

    String getUuidByDeliveryAddressId(String id);

    void addDeliveryAddress(Map map);

    void updateDeliveryAddress(Map map);

    void setDefaultDeliveryAddress(@Param(value = "id")String id,@Param(value = "isdefault")Integer isdefault);

    void deleteDeliveryAddress(String id);

}
