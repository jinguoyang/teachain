package com.youzi.teaChain.bean;


import java.math.BigDecimal;
import java.util.Date;

public class TTeachainGoods {

    private Integer id;
    private String name;
    private BigDecimal tccPrice;
    private BigDecimal rmbPrice;
    private String image;
    private Integer stock;
    private Integer sort;
    private Integer payType;
    private Integer deliveryType;
    private BigDecimal deliveryMoney;
    private Integer type;
    private Date createTime;
    private Date offShelvesTime;


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public BigDecimal getTccPrice() {
        return tccPrice;
    }

    public void setTccPrice(BigDecimal tccPrice) {
        this.tccPrice = tccPrice;
    }


    public BigDecimal getRmbPrice() {
        return rmbPrice;
    }

    public void setRmbPrice(BigDecimal rmbPrice) {
        this.rmbPrice = rmbPrice;
    }


    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }


    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }


    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }

    public Integer getPayType() {
        return payType;
    }

    public void setPayType(Integer payType) {
        this.payType = payType;
    }

    public Integer getDeliveryType() {
        return deliveryType;
    }

    public void setDeliveryType(Integer deliveryType) {
        this.deliveryType = deliveryType;
    }

    public BigDecimal getDeliveryMoney() {
        return deliveryMoney;
    }

    public void setDeliveryMoney(BigDecimal deliveryMoney) {
        this.deliveryMoney = deliveryMoney;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }


    public Date getOffShelvesTime() {
        return offShelvesTime;
    }

    public void setOffShelvesTime(Date offShelvesTime) {
        this.offShelvesTime = offShelvesTime;
    }

}
