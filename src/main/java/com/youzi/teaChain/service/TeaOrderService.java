package com.youzi.teaChain.service;

import com.youzi.teaChain.bean.DeliveryAddress;
import com.youzi.teaChain.dao.TeaOrderMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Service
public class TeaOrderService {

    private Logger log = LoggerFactory.getLogger(TeaOrderService.class);

    @Resource
    private TeaOrderMapper teaOrderMapper;

    public Boolean addDeliveryAddress(Map map){
        try {
            List<DeliveryAddress> deliveryAddressList = teaOrderMapper.getDeliveryAddressByUuid(map.get("uuid").toString());
            int count = deliveryAddressList.size();
            if(count == 0) {
                map.put("isdefault",1);
            }else {
                if(map.containsKey("isdefault")&& map.get("isdefault").toString().equals("1")){
                    for (DeliveryAddress deliveryAddress:deliveryAddressList) {
                        teaOrderMapper.setDefaultDeliveryAddress(deliveryAddress.getId(),0);
                    }
                }
            }
            teaOrderMapper.addDeliveryAddress(map);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("addDeliveryAddress 添加收货地址出错：" + e.getMessage());
            return false;
        }
    }

    public List<DeliveryAddress> getDeliveryAddressByUuid(String uuid){
        try {
            return teaOrderMapper.getDeliveryAddressByUuid(uuid);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("getDeliveryAddress 查询收货地址出错：" + e.getMessage());
            return null;
        }
    }

    public DeliveryAddress getDefaultDeliveryAddress(String uuid){
        try {
            List<DeliveryAddress> defaultList = teaOrderMapper.getDefaultDeliveryAddress(uuid);
            int count = defaultList.size();
            if(count == 1){
                return defaultList.get(0);
            }else if(count < 1){
                return null;
            }else{
                for(int i=1;i<count;i++){
                    DeliveryAddress deliveryAddress = defaultList.get(i);
                    teaOrderMapper.setDefaultDeliveryAddress(deliveryAddress.getId(),0);
                }
                return defaultList.get(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("getDefaultDeliveryAddress 查询默认收货地址出错：" + e.getMessage());
            return null;
        }
    }

    public String getUuidByDeliveryAddressId(String id){
        try {
            return teaOrderMapper.getUuidByDeliveryAddressId(id);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("getUuidByDeliveryAddressId 查询uuid出错：" + e.getMessage());
            return "";
        }
    }

    public DeliveryAddress getDeliveryAddressById(String id){
        try {
            return teaOrderMapper.getDeliveryAddressById(id);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("getDeliveryAddressById 查询收货地址出错：" + e.getMessage());
            return null;
        }
    }

    public Boolean updateDeliveryAddress(Map map){
        try {
            String id = map.get("id").toString();
            teaOrderMapper.updateDeliveryAddress(map);
            if(Integer.parseInt(map.get("isdefault").toString()) == 1){
                setDefaultDeliveryAddress(id);
            }else {
                changeOtherDefaultDeliveryAddress(id);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("updateDeliveryAddress 更新收货地址出错：" + e.getMessage());
            return false;
        }
    }

    public Integer deleteDeliveryAddress(String id){
        try {
            if(teaOrderMapper.getDeliveryAddressById(id)==null){
                return 0;
            }
            teaOrderMapper.deleteDeliveryAddress(id);
            changeOtherDefaultDeliveryAddress(id);
            return 1;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("deleteDeliveryAddress 删除收货地址出错：" + e.getMessage());
            return -1;
        }
    }

    private void setDefaultDeliveryAddress(String id){
        try {
            String uuid = getUuidByDeliveryAddressId(id);
            List<DeliveryAddress> deliveryAddressList = teaOrderMapper.getDefaultDeliveryAddress(uuid);
            for (DeliveryAddress deliveryAddress : deliveryAddressList) {
                teaOrderMapper.setDefaultDeliveryAddress(deliveryAddress.getId(), 0);
            }
            teaOrderMapper.setDefaultDeliveryAddress(id,1);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("setDefaultDeliveryAddress 设置默认收货地址出错：" + e.getMessage());
        }
    }

    private void changeOtherDefaultDeliveryAddress(String id){
        try {
            if(teaOrderMapper.getDeliveryAddressById(id).getIsdefault()==1){
                String uuid = getUuidByDeliveryAddressId(id);
                List<DeliveryAddress> deliveryAddressList = teaOrderMapper.getDeliveryAddressByUuid(uuid);
                teaOrderMapper.setDefaultDeliveryAddress(id,0);
                if (deliveryAddressList.size()>0){//todo
                    for (DeliveryAddress deliveryAddress : deliveryAddressList){
                        if(!deliveryAddress.getId().equals(id)){
                            teaOrderMapper.setDefaultDeliveryAddress(deliveryAddress.getId(),1);
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("changeOtherDefaultDeliveryAddress 设置默认收货地址出错：" + e.getMessage());
        }
    }
}
