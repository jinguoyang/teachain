package com.youzi.teaChain.bean;


import java.math.BigDecimal;
import java.util.Date;

public class TTeachainGoodsOrder {

    private String id;
    private String uuid;
    private Integer goodsId;
    private Integer amount;
    private Integer payType;
    private BigDecimal goodsTcc;
    private BigDecimal goodsMoney;
    private Integer deliveryType;
    private BigDecimal deliveryMoney;
    private Integer addressId;
    private Integer status;
    private Date createTime;
    private Date payTime;
    private Date deliveryTime;
    private Date finishTime;
    private BigDecimal totalTcc;
    private BigDecimal totalCny;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }


    public Integer getGoodsId() {
        return goodsId;
    }

    public void setGoodsId(Integer goodsId) {
        this.goodsId = goodsId;
    }


    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    public BigDecimal getGoodsTcc() {
        return goodsTcc;
    }

    public void setGoodsTcc(BigDecimal goodsTcc) {
        this.goodsTcc = goodsTcc;
    }

    public BigDecimal getDeliveryMoney() {
        return deliveryMoney;
    }

    public void setDeliveryMoney(BigDecimal deliveryMoney) {
        this.deliveryMoney = deliveryMoney;
    }

    public Integer getDeliveryType() {
        return deliveryType;
    }

    public void setDeliveryType(Integer deliveryType) {
        this.deliveryType = deliveryType;
    }

    public Integer getPayType() {
        return payType;
    }

    public void setPayType(Integer payType) {
        this.payType = payType;
    }


    public BigDecimal getGoodsMoney() {
        return goodsMoney;
    }

    public void setGoodsMoney(BigDecimal goodsMoney) {
        this.goodsMoney = goodsMoney;
    }


    public Integer getAddressId() {
        return addressId;
    }

    public void setAddressId(Integer addressId) {
        this.addressId = addressId;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }


    public Date getPayTime() {
        return payTime;
    }

    public void setPayTime(Date payTime) {
        this.payTime = payTime;
    }


    public Date getDeliveryTime() {
        return deliveryTime;
    }

    public void setDeliveryTime(Date deliveryTime) {
        this.deliveryTime = deliveryTime;
    }


    public Date getFinishTime() {
        return finishTime;
    }

    public void setFinishTime(Date finishTime) {
        this.finishTime = finishTime;
    }

    public BigDecimal getTotalTcc() {
        return totalTcc;
    }

    public void setTotalTcc(BigDecimal totalTcc) {
        this.totalTcc = totalTcc;
    }

    public BigDecimal getTotalCny() {
        return totalCny;
    }

    public void setTotalCny(BigDecimal totalCny) {
        this.totalCny = totalCny;
    }
}
